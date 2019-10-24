package com.leyou.page.client;

import com.leyou.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(value = "item-service",contextId = "3")
public interface GoodsClient extends GoodsApi {
}
