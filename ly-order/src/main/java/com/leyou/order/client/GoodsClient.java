package com.leyou.order.client;

import com.leyou.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "item-service",contextId = "orderService")
public interface GoodsClient extends GoodsApi {
}
