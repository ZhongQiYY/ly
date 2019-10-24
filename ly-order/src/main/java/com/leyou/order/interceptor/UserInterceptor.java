package com.leyou.order.interceptor;

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.order.config.JwtProperties;
import com.leyou.utils.CookieUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class UserInterceptor implements HandlerInterceptor {

    private JwtProperties prop;

    private static final ThreadLocal<UserInfo> threadLocal = new ThreadLocal<>();

    public UserInterceptor(JwtProperties prop) {
        this.prop = prop;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        获取cookie中的token
        String token = CookieUtils.getCookieValue(request, prop.getCookieName());
        try {
//            解析token
            UserInfo userInfo = JwtUtils.getUserInfo(prop.getPublicKey(), token);
//            使用request域传递user
//            request.setAttribute("user",userInfo);
//            同时也可以用线程容器保存传递user，一次请求就会开辟一个线程，
//            线程存储是一个map结构，key是线程本身，我们只需要传递value就行了。
            threadLocal.set(userInfo);
//            放行
            return true;
        }catch (Exception e){
            log.error("【购物车服务】 解析用户身份失败.", e);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        threadLocal.remove(); //线程传递完user值之后清空
    }

    /**
     * 为order提供拿到User的方法
     * @return
     */
    public static UserInfo getUser(){
        return threadLocal.get();
    }
}
