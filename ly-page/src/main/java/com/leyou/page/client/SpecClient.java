package com.leyou.page.client;

import com.leyou.api.SpecApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "item-service",contextId = "4")
public interface SpecClient extends SpecApi {
}
