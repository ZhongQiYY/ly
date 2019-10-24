package com.leyou.page.service;

import com.leyou.enums.ExceptionEnum;
import com.leyou.exception.LyException;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecClient;
import com.leyou.pojo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PageService {

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecClient specClient;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${ly.page.path}")
    private String dest;

    public Map<String, Object> loadModel(Long spuId) {
        Map<String, Object> model = new HashMap<>();
        Spu spu = goodsClient.querySpuBySpuId(spuId);

        //上架未上架，则不应该查询到商品详情信息，抛出异常
        if (!spu.getSaleable()) {
            throw new LyException(ExceptionEnum.GOODS_NOT_SALEABLE);
        }

        SpuDetail detail = spu.getSpuDetail();
        List<Sku> skus = spu.getSkus();
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        //查询三级分类
        List<Category> categories = categoryClient.queryCategoryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));

        List<SpecGroup> specs = specClient.querySpecsByCid(spu.getCid3());

        model.put("brand", brand);
        model.put("categories", categories);
        model.put("spu", spu);
        model.put("skus", skus);
        model.put("detail", detail);
        model.put("specs", specs);
        return model;
    }

    /**
     * 创建静态化页面，
     * 由于ly-page微服务在windows上，nginx在虚拟机上，所以产生的静态化页面并不能直接在nginx内路径上生成，
     * 还需要手动移动，详见视频G:\java高级\09 微服务电商【黑马乐优商城】·\13 - 商品详情页\3_LY5JU.mp4
     * 如果将该微服务与nginx部署到一起就能解决这个问题
     * @param spuId
     */
    public  void createHtml(Long spuId) {
        Context context = new Context();
        Map<String, Object> map = loadModel(spuId);
        context.setVariables(map);

        File file = new File(this.dest, spuId + ".html");
        //如果页面存在，先删除，后进行创建静态页
        if (file.exists()) {
            file.delete();
        }
        try (PrintWriter writer = new PrintWriter(file, "utf-8")) {
            templateEngine.process("item", context, writer);
        } catch (Exception e) {
            log.error("【静态页服务】生成静态页面异常", e);
        }
    }

    public void deleteHtml(Long id) {
        File file = new File(this.dest + id + ".html");
        if (file.exists()) {
            boolean flag = file.delete();
            if (!flag) {
                log.error("删除静态页面失败");
            }
        }
    }
}
