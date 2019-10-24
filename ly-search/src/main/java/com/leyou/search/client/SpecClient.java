package com.leyou.search.client;

import com.leyou.api.SpecApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "item-service",contextId = "fourth")
public interface SpecClient extends SpecApi {
}
