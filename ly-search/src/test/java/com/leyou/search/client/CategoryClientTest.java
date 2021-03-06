package com.leyou.search.client;

import com.leyou.pojo.Category;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;



@RunWith(SpringRunner.class)
@SpringBootTest
public class CategoryClientTest {

    @Autowired
   private CategoryClient categoryClient;

    @Test
    public void testQueryCategory() {
        List<Category> list = categoryClient.queryCategoryByIds(Arrays.asList(1L, 2L, 3L));
        list.forEach(System.out::println);
    }

}