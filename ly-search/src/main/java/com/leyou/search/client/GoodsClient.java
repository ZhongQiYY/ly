package com.leyou.search.client;

import com.leyou.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(value = "item-service",contextId = "third")
public interface GoodsClient extends GoodsApi {
}
