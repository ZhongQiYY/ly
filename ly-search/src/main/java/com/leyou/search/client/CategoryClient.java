package com.leyou.search.client;

import com.leyou.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author bystander
 * @date 2018/9/22
 */
@FeignClient(value = "item-service",contextId = "second")
public interface CategoryClient extends CategoryApi {
}
