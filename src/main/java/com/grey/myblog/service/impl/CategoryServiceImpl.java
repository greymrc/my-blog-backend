package com.grey.myblog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.grey.myblog.dao.CategoryDAO;
import com.grey.myblog.model.dataobject.CategoryDO;
import com.grey.myblog.service.CategoryService;
import org.springframework.stereotype.Service;

/**
* @author grey
* @description 针对表【category(文章分类表)】的数据库操作Service实现
* @createDate 2026-01-15 11:49:53
*/
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryDAO, CategoryDO>
    implements CategoryService{

}



