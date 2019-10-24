package com.leyou.cart.service;

import com.leyou.auth.entity.UserInfo;
import com.leyou.cart.interceptor.UserInterceptor;
import com.leyou.cart.pojo.Cart;
import com.leyou.enums.ExceptionEnum;
import com.leyou.exception.LyException;
import com.leyou.utils.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "cart:user:id";

    /**
     * 添加购物车，主要修改的就是num数量，
     * 如果该用户原redis中有该商品，就把传过来的商品数量添加进入redis中
     * 如果该用户原redis中没有该商品，就对这个商品进行新增
     * @param cart
     */
    public void addCart(Cart cart) {
//        获取登录用户
        UserInfo user = UserInterceptor.getUser();
//        key 用户id
        String key = KEY_PREFIX+user.getId();
//        hashKey 商品id
        String hashKey = cart.getSkuId().toString();
//        将num记录下来
        Integer num = cart.getNum();
//        1.存入redis中的结构为map<String,map<String,String>>
//              第一层Map，Key是用户id
//              第二次Map，Key是购物车中商品id，值是购物车数据
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key); //给定了map<String,map<String,String>>，第一层的key为用户id
//        1.1判断当前商品是否存在
        if (operations.hasKey(hashKey)) {
//            存在，则修改数量
            String json = operations.get(hashKey).toString(); //数据库中存储的都是String，这里取出来的是json格式String字符串，需要手动转换成对象
            cart = JsonUtils.toBean(json, Cart.class); //将json格式的字符串，转换成对象，这是原来在redis中的商品
            cart.setNum(cart.getNum()+num); //原有的数量加上前端传过来的数量 = 现有的数量
        }
//              写入redis，存在则写入修改后的cart，不存在直接写入redis
            operations.put(hashKey,JsonUtils.toString(cart));
    }

    /**
     * 查询所有商品
     * @return
     */
    public List<Cart> queryCartList() {
//        获取登录用户
        UserInfo user = UserInterceptor.getUser();
//        key 用户id
        String key = KEY_PREFIX+user.getId();

        if(!redisTemplate.hasKey(key)){
            throw new LyException(ExceptionEnum.CART_NOT_FOUND);
        }

//        获取登录用户的所有购物车数据
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key); //给定了map<String,map<String,String>>，第一层的key为用户id
        List<Cart> carts = new ArrayList<>();
        for (Object o : operations.values()) {
            Cart cart = JsonUtils.toBean(o.toString(), Cart.class);
            carts.add(cart);
        }

        return carts;
    }

    /**
     * 修改购物车的商品数量
     * @param skuId
     * @param num
     */
    public void updateCartNum(Long skuId, Integer num) {
//        获取登录用户
        UserInfo user = UserInterceptor.getUser();
//        key 用户id
        String key = KEY_PREFIX+user.getId();
//        获取操作
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
//          operations.get(skuId.toString())得到的是map<String,map<String,String>>中第二层的value值
//          这个值是一个json格式的字符串类型，需要转换成Cart对象类型
        Cart cart = JsonUtils.toBean(operations.get(skuId.toString()).toString(), Cart.class);
        cart.setNum(num);
//        将Cart存回redis
        operations.put(skuId.toString(),JsonUtils.toString(cart));
    }

    /**
     * 向数据库插入未登录时，添加进入购物车的商品
     * @param carts
     * @return
     */
    public void insertCart(List<Cart> carts) {
//        获取登录用户
        UserInfo user = UserInterceptor.getUser();
//        key 用户id
        String key = KEY_PREFIX+user.getId();
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        for (Cart cart : carts) {
//            写入redis
            operations.put(cart.getSkuId().toString(),JsonUtils.toString(cart));
        }
    }

    /**
     * 删除一个商品
     * @param id
     */
    public void deleteCart(Long id) {
//        获取登录用户
        UserInfo user = UserInterceptor.getUser();
//        key 用户id
        String key = KEY_PREFIX+user.getId();
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        if (!operations.hasKey(id.toString())) {
            //该商品不存在
            throw new RuntimeException("购物车商品不存在, 用户：" + user.getId() + ", 商品：" + id);
        }
//        删除商品
        operations.delete(id.toString());
    }

    /**
     * 删除购物车中已经下单的商品数据, 采用异步mq的方式通知购物车系统删除已购买的商品，传送商品ID和用户ID
     * @param ids sku的id集合
     * @param userId 用户id
     */
    public void deleteCarts(List<Object> ids, Integer userId) {
//        key 用户id
        String key = KEY_PREFIX+userId;
//        获取操作
        BoundHashOperations<String, Object, Object> operations = redisTemplate.boundHashOps(key);
        for (Object id : ids) {
            operations.delete(id.toString()); //删除商品
        }
    }
}
