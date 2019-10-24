package com.leyou.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.enums.ExceptionEnum;
import com.leyou.exception.LyException;
import com.leyou.mapper.BrandMapper;
import com.leyou.pojo.Brand;
import com.leyou.vo.PageResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;


    /**
     * 查询品牌，并分页
     * @param page 当前页
     * @param rows 每页大小
     * @param sortBy 通过何种方式排序
     * @param desc true为"DESC"，false为"ASC"，
     * @param key where name like "%x%" or letter == 'x'  x为key的值，key用来传递查询条件
     * @return
     */
    public PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
//        分页，使用分页助手PageHelper
        PageHelper.startPage(page,rows);
//        过滤
        Example example = new Example(Brand.class);
        if(StringUtils.isNotBlank(key)){
//            过滤条件
            example.createCriteria().orLike("name","%"+key+"%")
                    .orEqualTo("letter",key.toUpperCase()); //相当于where name like "%x%" or letter == 'x'  x为key的值
        }
//        排序
        if(StringUtils.isNotBlank(sortBy)){
            String orderByClause = sortBy+(desc ? " DESC" : " ASC");
            example.setOrderByClause(orderByClause);
        }
        List<Brand> brands = brandMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(brands)){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
//        解析分页结果
        PageInfo<Brand> info = new PageInfo<>(brands);
        return new PageResult<>(info.getTotal(),brands);
    }

    /**
     * 新增品牌
     * @param brand 需要新增的品牌
     * @param cids 该新增品牌对应的商品分类id
     */
    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
//        新增品牌
        int count = brandMapper.insert(brand);
        if(count != 1){
            throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
        }
//        新增中间表
        for (Long cid : cids) {
            count = brandMapper.insertCategoryBrand(cid,brand.getId());
            if(count != 1){
                throw new LyException(ExceptionEnum.BRAND_SAVE_ERROR);
            }
        }
    }

    /**
     * 编辑修改品牌信息
     * @param brand
     * @param cids
     */
    @Transactional
    public void editBrand(Brand brand, List<Long> cids) {
//        一、更新品牌数据
        int count = brandMapper.updateByPrimaryKeySelective(brand);
        if(count != 1){
            throw new LyException(ExceptionEnum.UPDATE_BRAND_ERROR);
        }
//        二、更新品牌所属分类
//              1.先将品牌原本的分类删除
        brandMapper.deleteCategoryBrandByBrandId(brand.getId());
//              2.把修改后的新的分类添加进去
        for (Long cid : cids) {
            count = brandMapper.insertCategoryBrand(cid,brand.getId());
            if(count != 1){
                throw new LyException(ExceptionEnum.UPDATE_BRAND_ERROR);
            }
        }
    }

    /**
     * 根据id删除品牌
     * @param bid
     */
    @Transactional
    public void deleteBrand(Long bid) {
//        根据主键字段删除品牌信息
        int count = brandMapper.deleteByPrimaryKey(bid);
        if(count != 1){
            throw new LyException(ExceptionEnum.DELETE_BRAND_ERROR);
        }
//        维护中间表
        brandMapper.deleteCategoryBrandByBrandId(bid);
    }

    /**
     * 根据id查询品牌
     * @param id
     * @return
     */
    public Brand queryById(Long id){
        Brand brand = brandMapper.selectByPrimaryKey(id);
        if(brand == null){
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return brand;
    }

    /**
     * 根据商品分类id查询品牌
     * @param cid
     * @return
     */
    public List<Brand> queryByCategoryId(Long cid) {
        return brandMapper.queryByCategoryId(cid);
    }

    /**
     * 根据品牌id集合，查询品牌信息集合
     * @param ids
     * @return
     */
    public List<Brand> queryByBrandIds(List<Long> ids) {
        return brandMapper.selectByIdList(ids);
    }
}
