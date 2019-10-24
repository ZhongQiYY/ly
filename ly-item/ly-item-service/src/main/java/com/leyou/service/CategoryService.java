package com.leyou.service;

import com.leyou.enums.ExceptionEnum;
import com.leyou.exception.LyException;
import com.leyou.mapper.CategoryMapper;
import com.leyou.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * 根据父节点id查询商品分类信息
     * @param pid
     * @return
     */
    public List<Category> queryCategoryListByPid(Long pid) {
        Category category = new Category();
        category.setParentId(pid);
        List<Category> list = categoryMapper.select(category);//根据实体对象内非空的字段进行查询

//        判断list是否为空
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return list;
    }

    /**
     * 添加新节点
     * @param category
     */
    @Transactional
    public void saveCategory(Category category) {
//        1.首先设置id值为null
        category.setId(null);
//        2.保存
        categoryMapper.insert(category);
//        3.当点了加号新增后，该节点变为父节点
        Category parent = new Category();
        parent.setId(category.getParentId());
        parent.setIsParent(true);
        categoryMapper.updateByPrimaryKeySelective(parent); //根据主键更新属性不为null的值
    }

    /**
     * 根据节点id删除节点
     * @param id
     */
    @Transactional
    public void deleteCategory(Long id) {
//        先查询出需要删除的节点
        Category category = categoryMapper.selectByPrimaryKey(id);
//        判断category是否为空
        if(category == null){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
//        判断该节点是否为父节点，
//        如果是父节点，那么删除所有附带子节点，然后要维护中间表
//        如果是叶子节点，那么只删除自己，然后判断该叶子节点的父节点孩子的个数，如果不为0，则不做任何修改；如果为0，则修改父节点isParent的值为false，最后维护中间表。
        if(category.getIsParent()){
//            1.查询所有叶子节点(即最后一个节点)装入集合leafNode中，用来维护分类与品牌的中间表，中间表中有叶子节点id与品牌id
            List<Category> leafNode = new ArrayList<>();
            queryAllLeafNode(category,leafNode);

//            2.查询所有子节点（包括叶子节点）装入集合node中，作删除用
            List<Category> node = new ArrayList<>();
            queryAllNode(category,node);

//            3.删除父节点与子节点
            for(Category c : node){
                categoryMapper.delete(c);
            }

//            4.维护中间表
            for(Category c : leafNode){
                categoryMapper.deleteByCategoryIdInCategoryBrand(c.getId());
            }
        }else{
//            查询此节点的父节点孩子个数
            Example example = new Example(Category.class);
//            查询父节点id为category.getParentId()的该子节点的所有同级兄弟
            example.createCriteria().andEqualTo("parentId",category.getParentId());//相当于where parent_id=category.getParentId()
            List<Category> categories = categoryMapper.selectByExample(example);//相当于 select * from category where parent_id=category.getParentId()
            int num = categories.size();
//            如果删除自己父节点孩子数为0，则修改父节点isParent的值为false，如果不为0，则不做任何修改，最后维护中间表。
            if(num == 1){
//                1.没有兄弟，删除自己
                categoryMapper.deleteByPrimaryKey(category.getId());
//                2.把该叶子节点的父节点IsParent设置为false
                Category parent = new Category();
                parent.setId(category.getParentId());
                parent.setIsParent(false);
                categoryMapper.updateByPrimaryKeySelective(parent);
//                3.维护中间表
                categoryMapper.deleteByCategoryIdInCategoryBrand(category.getId());
            }else {
//                1.有兄弟，删除自己
                categoryMapper.deleteByPrimaryKey(category.getId());
//                2.维护中间表
                categoryMapper.deleteByCategoryIdInCategoryBrand(category.getId());
            }
        }
    }

    /**
     * 查询所有叶子节点(即最后一个节点)
     * @param category
     * @param leafNode
     */
    private void queryAllLeafNode(Category category, List<Category> leafNode){
        if(!category.getIsParent()){
            leafNode.add(category);
        }
//        通用mapper的example条件查询
        Example example = new Example(Category.class);
//        查询出该父节点的所有下一级子节点
        example.createCriteria().andEqualTo("parentId",category.getId());//相当于where parent_id=category.getId()
        List<Category> list = categoryMapper.selectByExample(example);//相当于 select * from category where parent_id=category.getId()

        for(Category category1 : list){
            queryAllLeafNode(category1, leafNode);
        }
    }

    /**
     * 查询所有子节点（包括叶子节点）
     * @param category
     * @param node
     */
    private void queryAllNode(Category category, List<Category> node){
        node.add(category);
//        通用mapper的example条件查询
        Example example = new Example(Category.class);
//        查询出该父节点的所有下一级子节点
        example.createCriteria().andEqualTo("parentId",category.getId());//相当于where parent_id=category.getId()
        List<Category> list = categoryMapper.selectByExample(example);//相当于 select * from category where parent_id=category.getId()

        for(Category category1 : list){
            queryAllNode(category1,node);
        }
    }

    /**
     * 查询数据库中最后一条数据
     * @return
     */
    public List<Category> queryLast() {
        return categoryMapper.selectLast();
    }

    /**
     * 编辑节点名字
     * @param category
     */
    @Transactional
    public void EditCategory(Category category) {
        categoryMapper.updateByPrimaryKeySelective(category);
    }

    /**
     * 根据品牌id查询分类
     * @param bid
     * @return
     */
    public List<Category> queryByBrandId(Long bid) {
        return categoryMapper.queryByBrandId(bid);
    }

    /**
     *根据多个id查询商品分类集合
     * @param ids
     * @return
     */
    public List<Category> queryByIds(List<Long> ids){
        List<Category> list = categoryMapper.selectByIdList(ids);
//        判断list是否为空
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return list;
    }

    /**
     * 根据商品分类id，查询商品分类名称
     * @param ids
     * @return
     */
    public List<String> queryNameByIds(List<Long> ids) {
        return categoryMapper.selectByIdList(ids).stream().map(Category::getName).collect(Collectors.toList());
    }

    /**
     * 根据商品分类id的集合，查询商品分类
     * @param ids
     * @return
     */
    public List<Category> queryCategoryByIds(List<Long> ids) {
        return categoryMapper.selectByIdList(ids);
    }
}
