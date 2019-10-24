package com.leyou.sms.utils;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.leyou.sms.properties.SmsProperties;
import com.leyou.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@EnableConfigurationProperties(SmsProperties.class)
public class SmsUtils {

    @Autowired
    private SmsProperties prop;

    @Autowired
    private StringRedisTemplate template;

    private static final String SMS_PREFIX = "sms:phone:";
    private static final long SMS_MIN_INTERVAL_IN_MILLIS = 60000;

    //产品名称:云通信短信API产品,开发者无需替换
//    static final String product = "Dysmsapi";
    //产品域名,开发者无需替换
//    static final String domain = "dysmsapi.aliyuncs.com";

    public String sendSms(String signature, String templateCode, String phone, Map<String,Object> params) {

        try {

            String key = SMS_PREFIX + phone;

            String lastTime = template.opsForValue().get(key);
            if (StringUtils.isNotBlank(lastTime)) {
                //Redis中键不为空
                Long last = Long.valueOf(lastTime);
                if (System.currentTimeMillis() - last < SMS_MIN_INTERVAL_IN_MILLIS) {
                    //Redis中发送信息的手机号为超过1min,则返回空，不进行短信发送
                    log.info("【短信服务】短信发送频率过高，被拦截，手机号：{}", phone);
                    return null;
                }
            }

            //可自助调整超时时间
            System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
            System.setProperty("sun.net.client.defaultReadTimeout", "10000");

            //创建DefaultProfile  regionId默认为ydy。将、accessKey，SerectKey分别替换为客户在短信平台的accessKey，SerectKey
            DefaultProfile profile = DefaultProfile.getProfile("ydy", prop.getAccessKeyId(), prop.getAccessKeySecret());
            System.out.println(prop.getAccessKeyId()+"。。。。"+prop.getAccessKeySecret());
            IAcsClient client = new DefaultAcsClient(profile);
            //创建发送请求实体
            CommonRequest request = new CommonRequest();
            //request.setProtocol(ProtocolType.HTTPS);
            //短信平台提供get请求类型接口
            //request.setMethod(MethodType.POST);
            request.setVersion("2017-05-25");
            request.setAction("SendSms");

            //短信平台单发接口地址,xxx.xxx.xx为短信平台运营商的 域名 或 IP+端口
            request.setDomain("sms1.94008.com:8080");
            //发送参数
            //发送目标手机号
            request.putQueryParameter("PhoneNumbers", phone);
            //短信平台审核通过的短信签名
            request.putQueryParameter("SignName", signature);
            //短信平台审核通过的短信模板code
            request.putQueryParameter("TemplateCode", templateCode);
            //短信模板中的变量参数
            request.putQueryParameter("TemplateParam", JsonUtils.toString(params));
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());

            //存入Redis中，设置失效时间为1min
            template.opsForValue().set(key, String.valueOf(System.currentTimeMillis()), 1, TimeUnit.MINUTES);
            return response.getData();
        } catch (Exception e) {
            log.error("【短信服务】发送信息失败，手机号码：{}", phone);
            return null;
        }
    }
}