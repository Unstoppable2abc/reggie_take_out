package com.itheima.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.dto.DishDto;
import com.itheima.entity.Dish;
import com.itheima.entity.DishFlavor;
import com.itheima.mapper.DishMapper;
import com.itheima.service.DishFlavorService;
import com.itheima.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;

    /**
     * 新增菜品，同时保存口味数据
     * @param dishDto
     */
    @Transactional
    public void saveWithFlavor(DishDto dishDto) {
        //保存菜品信息到菜品表dish
        this.save(dishDto);

        //获取dish_id
        Long dishId = dishDto.getId();

        //菜品口味
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map((item) ->{
           //item对应每一个实体DishFlavor
           item.setDishId(dishId);
           return item;
        }).collect(Collectors.toList()); //设置完值重新转化为list集合，并重新赋值给自己

        //flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
        //保存菜品口味到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);
    }

    /**
     * 根据id查询菜品和对应菜品的口味
     * @param id
     * @return
     */
    public DishDto getByIdWithFlavor(Long id) {
        //查询菜品基本信息
        Dish dish = this.getById(id);

        DishDto dishDto = new DishDto();
        //对象属性拷贝
        BeanUtils.copyProperties(dish,dishDto);

        //查询菜品对应口味的基本信息，从dish_flavor表查询
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dish.getId());
        List<DishFlavor> flavors = dishFlavorService.list(queryWrapper);
        dishDto.setFlavors(flavors);

        return dishDto;
    }

    @Override
    @Transactional
    public void updateWithFlavor(DishDto dishDto) {
        //跟新dish基本信息
        this.updateById(dishDto);

        //先清理当前菜品对应的口味数据-----dish_flavor表的delete操作
        LambdaQueryWrapper<DishFlavor> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);

        //再添加当前菜品对应的口味数据------dish_flavor表的insert操作
        List<DishFlavor> flavors = dishDto.getFlavors();
        //设置dish_id属性值
        flavors = flavors.stream().map((item) -> {
            item.setDishId(dishDto.getId());
            return item;
        }).collect(Collectors.toList());

        dishFlavorService.saveBatch(flavors);
    }
}
