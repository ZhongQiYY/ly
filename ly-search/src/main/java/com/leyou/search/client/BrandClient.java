package com.leyou.search.client;

import com.leyou.api.BrandApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "item-service",contextId = "first")
public interface BrandClient extends BrandApi {
}
