package com.leyou.user.service;

import com.leyou.enums.ExceptionEnum;
import com.leyou.exception.LyException;
import com.leyou.user.mapper.UserMapper;
import com.leyou.user.pojo.User;
import com.leyou.user.properties.UserProperties;
import com.leyou.user.utils.CodecUtils;
import com.leyou.utils.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@EnableConfigurationProperties(UserProperties.class)
public class UserService {
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserProperties userProperties;

    private static final String KEY_PREFIX = "user:verify:phone:";

    /**
     * 校验用户名和电话是否已经存在
     * @param data
     * @param type
     * @return
     */
    public Boolean checkData(String data, Integer type) {
        User record = new User();
        switch(type){
            case 1:
                record.setUsername(data);
                break;
            case 2:
                record.setPhone(data);
                break;
            default:
                throw new LyException(ExceptionEnum.USER_DATA_TYPE_ERROR);
        }
        return userMapper.selectCount(record) == 0;
    }

    /**
     * 发送短信
     * @param phone
     */
    public void sendCode(String phone) {
//        生成key，作为redis内的字段使用
        String key = KEY_PREFIX+phone;
//        生成验证码
        String code = NumberUtils.generateCode(6);
        Map<String,String> msg = new HashMap<>();
        msg.put("phone", phone);
        msg.put("code",code);

//        发送验证码
        amqpTemplate.convertAndSend(userProperties.getExchange(),userProperties.getRoutingKey(),msg);

//        保存验证码
        redisTemplate.opsForValue().set(key,code,userProperties.getTimeOut(), TimeUnit.MINUTES);
    }

    /**
     * 注册
     * @param user
     * @param code
     */
    public void register(User user, String code) {
//        1.校验验证码
//        1.1从redis中获取到存入其中的code
        String cachecode = redisTemplate.opsForValue().get(KEY_PREFIX+user.getPhone());
        if(!StringUtils.equals(code,cachecode)){
            throw new LyException(ExceptionEnum.INVALID_VERIFY_CODE);
        }
//        2.对密码进行加密
//        2.1生成盐
        String salt = CodecUtils.generateSalt();
//        2.2保存盐
        user.setSalt(salt);
//        2.3密码加密
        user.setPassword(CodecUtils.md5Hex(user.getPassword(),salt));
//        创建时间保存
        user.setCreated(new Date());
//        3.写入数据库
        userMapper.insert(user);
    }

    public User queryUser(String username, String password) {
//        先根据用户名查出用户
        User user = userMapper.selectOne(new User(username));
        if(user == null){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }
//        取出数据库中的盐值
        String salt = user.getSalt();
//        对比传过来的密码加密后与数据库中保存的加密后的密码是否相同
        if(!StringUtils.equals(CodecUtils.md5Hex(password, salt),user.getPassword())){
            throw new LyException(ExceptionEnum.INVALID_USERNAME_PASSWORD);
        }

        return user;
    }
}
