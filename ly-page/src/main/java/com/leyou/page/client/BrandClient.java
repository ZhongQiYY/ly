package com.leyou.page.client;

import com.leyou.api.BrandApi;
import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(value = "item-service",contextId = "1")
public interface BrandClient extends BrandApi {
}
