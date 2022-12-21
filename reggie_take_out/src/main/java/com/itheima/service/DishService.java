package com.itheima.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.dto.DishDto;
import com.itheima.entity.Dish;

public interface DishService extends IService<Dish> {

    //新增菜品，同时插入菜品对应的口味数据
    public void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品和对应菜品的口味
    public DishDto getByIdWithFlavor(Long id);

    //更新菜品信息以及对应的口味信息
    public void updateWithFlavor(DishDto dishDto);
}
