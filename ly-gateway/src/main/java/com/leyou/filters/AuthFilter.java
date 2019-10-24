package com.leyou.filters;

import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.config.FilterProperties;
import com.leyou.config.JwtProperties;
import com.leyou.utils.CookieUtils;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;


@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class AuthFilter extends ZuulFilter {

    @Autowired
    private JwtProperties props;

    @Autowired
    private FilterProperties filterprops;

    @Override
    public String filterType() {
        return FilterConstants.PRE_TYPE; //过滤器类型，前置过滤
    }

    @Override
    public int filterOrder() {
        return FilterConstants.PRE_DECORATION_FILTER_ORDER - 1; //过滤器顺序
    }

    /**
     * 判断是否过滤，白名单上的不过滤
     * @return
     */
    @Override
    public boolean shouldFilter() {
//        获取上下文
        RequestContext context = RequestContext.getCurrentContext();
//        获取request
        HttpServletRequest request = context.getRequest();
//        获取请求的url路径
        String path = request.getRequestURI();
//        判断是否放行，放行返回false，
        return !isAllowPath(path);
    }

    private boolean isAllowPath(String path) {
//        遍历白名单
        for (String allowPath : filterprops.getAllowPaths()) {
//            判断是否允许，即uri路径是否以白名单上的路径开头
            if(path.startsWith(allowPath)){
                return true;
            }
        }
        return false;
    }

    @Override
    public Object run() throws ZuulException {
//        获取上下文
        RequestContext context = RequestContext.getCurrentContext();
//        获取request
        HttpServletRequest request = context.getRequest();
//        获取cookie中的token
        String token = CookieUtils.getCookieValue(request, props.getCookieName());

        try {
//              解析token
            UserInfo userInfo = JwtUtils.getUserInfo(props.getPublicKey(), token);
//            TODO 如果有权限要校验的话在这里校验权限，可以根据userInfo的信息得到用户的权限，然后使用request取得他所要访问的路径，根据权限判断他是否有权限访问
        }catch (Exception e){
//            解析token失败，未登录拦截
            context.setSendZuulResponse(false);
//            返回状态码
            context.setResponseStatusCode(403);
        }
        return null;
    }
}
