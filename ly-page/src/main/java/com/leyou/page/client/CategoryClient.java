package com.leyou.page.client;

import com.leyou.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "item-service",contextId = "2")
public interface CategoryClient extends CategoryApi {
}
