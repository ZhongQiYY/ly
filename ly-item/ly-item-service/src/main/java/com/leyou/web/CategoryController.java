package com.leyou.web;

import com.leyou.pojo.Category;
import com.leyou.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 根据父节点id查询商品分类
     * @param pid
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategoryListByPid(@RequestParam("pid")Long pid){
        //如果pid的值为-1那么需要获取数据库中最后一条数据，参考https://blog.csdn.net/lyj2018gyq/article/details/82150316中第四点，为啥要搞这个功能
        if (pid == -1){
            return ResponseEntity.ok(categoryService.queryLast());
        }
        else {
            List<Category> list = categoryService.queryCategoryListByPid(pid);
            if (list == null) {
                //没有找到返回404
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            //找到返回200
            return ResponseEntity.ok(list);
        }
    }

    /**
     * 添加新节点
     * @param category
     * @return
     */
    @PostMapping("save")
    public  ResponseEntity<Void> saveCategory(Category category){
        categoryService.saveCategory(category);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 删除节点
     * @param id
     * @return
     */
    @DeleteMapping("delete/{id}")
    public  ResponseEntity<Void> deleteCategory(@PathVariable("id")Long id){
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 编辑节点名字
     * @return
     */
    @PutMapping("edit")
    public ResponseEntity<Void> EditCategory(Category category){
        categoryService.EditCategory(category);
        return ResponseEntity.accepted().build();
    }

    /**
     * 用于修改品牌信息时，商品分类的回显
     * @param bid
     * @return
     */
    @GetMapping("bid/{bid}")
    public ResponseEntity<List<Category>> queryByBrandId(@PathVariable("bid") Long bid){
        List<Category> list = categoryService.queryByBrandId(bid);
        if(list == null || list.size() < 1){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(list);
    }

    /**
     * 根据商品分类id查询商品分类名称
     * @param ids
     * @return
     */
    @GetMapping("names")
    public ResponseEntity<List<String>> queryNameByIds(@RequestParam("ids") List<Long> ids){
        List<String> list = categoryService.queryNameByIds(ids);
        if (list == null || list.size() < 1){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }else {
            return ResponseEntity.ok(list);
        }
    }

    /**
     * 根据商品分类id的集合，查询商品分类
     * @param ids
     * @return
     */
    @GetMapping("all")
    public ResponseEntity<List<Category>> queryCategoryByIds(@RequestParam("ids") List<Long> ids){
        return ResponseEntity.ok(categoryService.queryCategoryByIds(ids));
    }
}
