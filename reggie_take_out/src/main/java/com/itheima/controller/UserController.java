package com.itheima.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.common.R;
import com.itheima.entity.User;
import com.itheima.service.UserService;
import com.itheima.utils.SMSUtils;
import com.itheima.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 发送手机验证码
     * @param user
     * @return
     */
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession session){
        //获取用户手机号
        String phone = user.getPhone();

        if(!StringUtils.isEmpty(phone)){ //手机号不为空
            //生成随机4位验证码
            String code = ValidateCodeUtils.generateValidateCode(4).toString();
            log.info("验证码：{}",code);

            //调用阿里云提供的短信服务API完成短信发送
            //SMSUtils.sendMessage("阿里云短信测试","SMS_154950909",phone,"1234");

            //需要将生成的验证码保存到session，与用户填写的进行比对验证
            //session.setAttribute(phone,code);

            //将生成的验证码缓存到redis中，并设置有效期为5分钟
            redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);
            return R.success("手机验证码发送给成功");
        }

        return R.success("短信发送失败");
    }

    /**
     * 移动端用户登录
     * @param map
     * @param session
     * @return
     */
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map, HttpSession session){
        log.info(map.toString());

        //获取手机号
        String phone = map.get("phone").toString();

        //获取验证码
        String code = map.get("code").toString();

        //从session中获取保存的验证码
        //Object attributeCode = session.getAttribute(phone);

        //从redis中获取缓存的验证码
        Object attributeCode = redisTemplate.opsForValue().get(phone);

        //进行验证码的比对（页面提交的验证码和之前放到session中的验证码比对）
        if(attributeCode != null && attributeCode.equals(code)){
            //如果能够比对成功，说明登录成功

            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);

            User user = userService.getOne(queryWrapper);
            //判断当前用户是否为新用户，如果是新用户就自动完成注册
            if (user == null) {
                user = new User();
                user.setStatus(1);
                user.setPhone(phone);
                userService.save(user);
            }
            session.setAttribute("user",user.getId());

            //如果用户登录成功，删除缓存的验证码
            redisTemplate.delete(phone);

            //需要在浏览器保存user信息，返回user对象
            return R.success(user);
        }

        return R.error("登录失败");
    }

    @PostMapping("/loginout")
    public R<String> loginOut(HttpSession session){
        //清理session保存的当前登录用户的id
        session.removeAttribute("user");
        return R.success("退出成功");
    }
}
