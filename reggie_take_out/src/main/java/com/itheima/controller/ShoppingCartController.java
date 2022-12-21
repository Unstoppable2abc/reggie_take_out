package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.common.BaseContext;
import com.itheima.common.R;
import com.itheima.entity.ShoppingCart;
import com.itheima.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
@Slf4j
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 加入购物车
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("购物车数据：{}",shoppingCart);

        //设置ShoppingCart中的userId属性，指定购物车是哪个用户的
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        //查询当前菜品或套餐是否在购物车中
        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);

        if (dishId != null) {
            //添加到购物车的是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else {
            //添加到购物车的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,setmealId);
        }

        ShoppingCart cart = shoppingCartService.getOne(queryWrapper);

        if (cart != null) {
            //如果已经存在，number属性加一即可
            Integer number = cart.getNumber();
            cart.setNumber(number+1);
            shoppingCartService.updateById(cart);
        }else {
            //如果不存在，则添加到购物车，数量默认是一
            shoppingCart.setNumber(1); //前端没有传，手动设置
            //创建时间也手动改设置一下
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cart = shoppingCart;
        }


        return R.success(cart);
    }

    /**
     * 减加入购物车的菜品或套餐数量
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){

        //设置ShoppingCart中的userId属性，指定购物车是哪个用户的
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);


        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();

        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);

        if (dishId != null) {
            //添加到购物车的是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else {
            //添加到购物车的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,setmealId);
        }

        ShoppingCart serviceOne = shoppingCartService.getOne(queryWrapper);
        Integer number = serviceOne.getNumber();
        serviceOne.setNumber(number-1);

        shoppingCartService.updateById(serviceOne);
        return R.success(serviceOne);
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车");
        //根据用户id查询购物车
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> shoppingCarts = shoppingCartService.list(queryWrapper);

        return R.success(shoppingCarts);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        //根据用户id清空该用户的购物车
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);

        shoppingCartService.remove(queryWrapper);

        return R.success("清空购物车成功");
    }
}
