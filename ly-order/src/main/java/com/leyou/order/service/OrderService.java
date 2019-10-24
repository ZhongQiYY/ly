package com.leyou.order.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.auth.entity.UserInfo;
import com.leyou.dto.CartDto;
import com.leyou.enums.ExceptionEnum;
import com.leyou.exception.LyException;
import com.leyou.order.client.AddressClient;
import com.leyou.order.client.GoodsClient;
import com.leyou.order.dto.AddressDTO;
import com.leyou.order.dto.OrderDto;
import com.leyou.order.dto.OrderStatusEnum;
import com.leyou.order.interceptor.UserInterceptor;
import com.leyou.order.mapper.OrderDetailMapper;
import com.leyou.order.mapper.OrderMapper;
import com.leyou.order.mapper.OrderStatusMapper;
import com.leyou.order.pojo.Order;
import com.leyou.order.pojo.OrderDetail;
import com.leyou.order.pojo.OrderStatus;
import com.leyou.order.utils.PayHelper;
import com.leyou.pojo.Sku;
import com.leyou.utils.IdWorker;
import com.leyou.utils.JsonUtils;
import com.leyou.vo.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private PayHelper payHelper;

    @Autowired
    private PayLogService payLogService;

    @Autowired
    private AmqpTemplate amqpTemplate;

    /**
     * 创建订单
     * @param orderDto
     * @return
     */
    @Transactional
    public Long createOrder(OrderDto orderDto) {
//        1.新增订单order与orderDetail
        Order order = new Order();
//        1.1 订单编号，基本信息
        long orderId = idWorker.nextId(); //使用雪花算法生成唯一id
        order.setOrderId(orderId);
        order.setCreateTime(new Date());
        order.setPaymentType(orderDto.getPaymentType()); //支付类型

//        1.2 用户信息
        UserInfo user = UserInterceptor.getUser();
        order.setUserId(user.getId()); //用户id
        order.setBuyerNick(user.getName()); //买家昵称
        order.setBuyerRate(false); //买家是否已评价

//        1.3 收货人地址信息，应该从数据库中物流信息中获取，这里使用的是假的数据
        AddressDTO addressDTO = AddressClient.findById(orderDto.getAddressId());
        if (addressDTO == null) {
            // 商品不存在，抛出异常
            throw new LyException(ExceptionEnum.RECEIVER_ADDRESS_NOT_FOUND);
        }
        order.setReceiver(addressDTO.getName());
        order.setReceiverAddress(addressDTO.getAddress());
        order.setReceiverCity(addressDTO.getCity());
        order.setReceiverDistrict(addressDTO.getDistrict());
        order.setReceiverMobile(addressDTO.getPhone());
        order.setReceiverZip(addressDTO.getZipCode());
        order.setReceiverState(addressDTO.getState());

//        1.4 付款金额相关，首先把orderDto转化成map，其中key为skuId,值为购物车中该sku的购买数量num
        Map<Long, Integer> skuIdNumMap = orderDto.getCarts().stream().collect(Collectors.toMap(CartDto::getSkuId, CartDto::getNum, (a, b) -> b));
//        1.4.1查询商品信息，根据skuIds批量查询sku详情
        List<Sku> skus = goodsClient.querySkusByIds(new ArrayList<>(skuIdNumMap.keySet()));
        if (CollectionUtils.isEmpty(skus)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        double totalPay = 0.0; //定义总金额
        Integer num; //定义每一个商品的数量
        ArrayList<OrderDetail> orderDetails = new ArrayList<>();//准备订单详情的集合
        for (Sku sku : skus) {//总金额为所有商品的单价乘以数量的和
            num = skuIdNumMap.get(sku.getId());
            totalPay = num*sku.getPrice() + totalPay;

            /**
             * 填充订单详情的集合
             */
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setOwnSpec(sku.getOwnSpec());
            orderDetail.setSkuId(sku.getId());
            orderDetail.setTitle(sku.getTitle());
            orderDetail.setNum(num);
            orderDetail.setPrice(sku.getPrice().longValue());
            orderDetail.setImage(StringUtils.substringBefore(sku.getImages(), ","));

            orderDetails.add(orderDetail);
        }
//        double类型转化成long类型：new Double(totalPay).longValue()
        long total = new Double(totalPay).longValue();
        order.setTotalPay(total);//总金额
        order.setActualPay(total + order.getPostFee() - 0);//实付金额 = 总金额 + 邮费 - 优惠金额

        /**
         * 将order写入数据库
         */
        orderMapper.insertSelective(order);

        /**
         * 新增订单详情orderDetail
         */
        orderDetailMapper.insertList(orderDetails);


//        2.新增状态
//        填充orderStatus
        OrderStatus orderStatus = new OrderStatus();
        orderStatus.setOrderId(orderId);
        orderStatus.setStatus(OrderStatusEnum.INIT.value());
        orderStatus.setCreateTime(new Date());
        /**
         * 保存orderStatus
         */
        orderStatusMapper.insertSelective(orderStatus);

//        3.减库存
        goodsClient.decreaseStock(orderDto.getCarts());

        //todo 删除购物车中已经下单的商品数据, 采用异步mq的方式通知购物车系统删除已购买的商品，传送商品ID和用户ID
        HashMap<String, Object> map = new HashMap<>();
        try {
            map.put("skuIds", skuIdNumMap.keySet()); //值为sku商品的id的数组
            map.put("userId", user.getId());//值为用户id
            amqpTemplate.convertAndSend("ly.cart.exchange", "cart.delete", JsonUtils.toString(map));
        } catch (Exception e) {
            log.error("删除购物车消息发送异常，商品ID：{}", skuIdNumMap.keySet(), e);
        }

        log.info("生成订单，订单编号：{}，用户id：{}", orderId, user.getId());
        return orderId;
    }

    /**
     * 根据订单ID查询订单详情
     * @param orderId
     * @return
     */
    public Order queryOrderById(Long orderId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw new LyException(ExceptionEnum.ORDER_NOT_FOUND);
        }
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        List<OrderDetail> orderDetails = orderDetailMapper.select(orderDetail);
        order.setOrderDetails(orderDetails);
        OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(orderId);
        order.setOrderStatus(orderStatus);
        return order;
    }

    /**
     * 生成微信支付链接
     * @param orderId
     * @return
     */
    public String generateUrl(Long orderId) {
        //根据订单ID查询订单
        Order order = queryOrderById(orderId);
        //判断订单状态
        if (order.getOrderStatus().getStatus() != OrderStatusEnum.INIT.value()) {
            throw new LyException(ExceptionEnum.ORDER_STATUS_EXCEPTION);
        }

        //todo 这里传入一份钱，用于测试使用，实际中使用订单中的实付金额
        String url = payHelper.createPayUrl(orderId, "乐优商城测试", /*order.getActualPay()*/1L);
        if (StringUtils.isBlank(url)) {
            throw new LyException(ExceptionEnum.CREATE_PAY_URL_ERROR);
        }

        //生成支付日志
        payLogService.createPayLog(orderId, order.getActualPay());

        return url;
    }

    /**
     * 处理微信回调结果
     * @param msg
     */
    @Transactional
    public void handleNotify(Map<String, String> msg) {
        payHelper.handleNotify(msg);
    }

    /**
     * 分页查询所有订单
     * @param page
     * @param rows
     * @return
     */
    public PageResult<Order> queryOrderByPage(Integer page, Integer rows) {

        //开启分页
        PageHelper.startPage(page, rows);

        Example example = new Example(Order.class);

        //查询订单
        List<Order> orders = orderMapper.selectByExample(example);


        //查询订单详情
        for (Order order : orders) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(order.getOrderId());
            List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);

            order.setOrderDetails(orderDetailList);

            //查询订单状态
            OrderStatus orderStatus = orderStatusMapper.selectByPrimaryKey(order.getOrderId());
            order.setOrderStatus(orderStatus);
        }

        PageInfo<Order> pageInfo = new PageInfo<>(orders);

        return new PageResult<>(pageInfo.getTotal(), pageInfo.getPages(), pageInfo.getList());
    }
}
