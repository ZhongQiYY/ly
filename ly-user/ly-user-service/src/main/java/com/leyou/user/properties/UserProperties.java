package com.leyou.user.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ly.user")
public class UserProperties {
    String exchange;
    String routingKey;
    Long timeOut;
}
