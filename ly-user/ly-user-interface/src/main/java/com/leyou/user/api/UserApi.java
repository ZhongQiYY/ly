package com.leyou.user.api;

import com.leyou.user.pojo.User;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RequestMapping("user")
public interface UserApi {

    @GetMapping("/check/{data}/{type}")
    Boolean checkData(@PathVariable("data") String data, @PathVariable("type") Integer type) ;
    /**
     * 发送验证码
     * @param phone
     * @return
     */
    @PostMapping("/send")
    void sendCode(@RequestParam("phone") String phone) ;
    /**
     * 用户注册
     * @param user
     * @param code
     * @return
     */
    @PostMapping("/register")
   void register(@Valid User user, @RequestParam("code") String code) ;

    /**
     * 根据用户名和密码查询用户
     * @param username
     * @param password
     * @return
     */
    @GetMapping("/query")
    User queryUser(@RequestParam("username") String username, @RequestParam("password") String password);
}
