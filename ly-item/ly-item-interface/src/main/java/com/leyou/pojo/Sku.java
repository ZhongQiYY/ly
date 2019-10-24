package com.leyou.pojo;


import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Table(name = "tb_sku")
@Data
public class Sku {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long spuId;
    private String title;
    private String images;
    private Double price;
    private String ownSpec; //商品特殊规格的键值对
    private String indexes; //商品特殊规格的下标
    private Boolean enable; //是否有效，逻辑删除用
    private Date createTime; //创建时间
    private Date lastUpdateTime; //最后修改时间
    @Transient //表示该属性并非一个到数据库表的字段的映射,ORM框架将忽略该属性.
    private Integer stock; //这里保存了一个库存字段，在数据库中是另外一张表保存的，方便查询
}
