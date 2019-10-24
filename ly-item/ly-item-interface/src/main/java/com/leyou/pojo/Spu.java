package com.leyou.pojo;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Table(name = "tb_spu")
@Data
public class Spu {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        private Long brandId;
        private Long cid1; //一级类目
        private Long cid2; //二级类目
        private Long cid3; //三级类目
        private String title; //标题
        private String subTitle; //子标题
        private Boolean saleable; //是否上架
        private Boolean valid; //是否有效，逻辑删除用
        private Date createTime; //创建时间
        private Date lastUpdateTime; //最后修改时间

        @Transient //表示该属性并非一个到数据库表的字段的映射,ORM框架将忽略该属性.
        private String cname; //商品分类名称
        @Transient
        private String bname; //品牌名称
        @Transient
        private List<Sku> skus;
        @Transient
        private SpuDetail spuDetail;

}
