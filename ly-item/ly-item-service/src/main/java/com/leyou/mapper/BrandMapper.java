package com.leyou.mapper;

import com.leyou.mappers.BaseMapper;
import com.leyou.pojo.Brand;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;


public interface BrandMapper extends BaseMapper<Brand,Long> {
    /**
     * 为品牌增加商品分类
     * @param cid
     * @param bid
     * @return
     */
    @Insert("INSERT INTO tb_category_brand VALUES(#{cid},#{bid})")
    int insertCategoryBrand(@Param("cid") Long cid, @Param("bid") Long bid);

    /**
     * 根据id删除品牌的商品分类
     * @param bid
     */
    @Delete("delete from tb_category_brand where brand_id = #{bid}")
    void deleteCategoryBrandByBrandId(@Param("bid")Long bid);

    /**
     * 根据商品分类id查询品牌
     * @param cid
     * @return
     */
    @Select("select * from tb_brand where id in (select brand_id from tb_category_brand where category_id = #{cid})")
    List<Brand> queryByCategoryId(@Param("cid") Long cid);
}
