package com.leyou.auth.utils;


import com.leyou.auth.entity.UserInfo;
import com.leyou.auth.utils.JwtUtils;
import com.leyou.auth.utils.RsaUtils;
import org.junit.Before;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class JwtUtilsTest {

    private static final String publicKeyPath = "D:\\IdeaProject\\leyou\\ly-auth-center\\ly-auth-common\\bystander\\rsa.pub";
    private static final String privateKeyPath = "D:\\IdeaProject\\leyou\\ly-auth-center\\ly-auth-common\\bystander\\rsa.pri";

    private PrivateKey privateKey;
    private PublicKey publicKey;


    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(publicKeyPath, privateKeyPath, "234");
    }

    @Before
    public void testGetRsa() throws Exception {
        privateKey = RsaUtils.getPrivateKey(privateKeyPath);
        publicKey = RsaUtils.getPublicKey(publicKeyPath);
    }

    @Test
    public void generateToken() {
        //生成Token
        String s = JwtUtils.generateToken(new UserInfo(20L, "Jack"), privateKey, 5);
        System.out.println("s = " + s);
    }


    @Test
    public void parseToken() {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6MjAsInVzZXJuYW1lIjoiSmFjayIsImV4cCI6MTU3MDk1NTE5OX0.gToKLC_Ug2CKYsMKlj9B7cyD8nCcfv1M98BNbaWAauM4G3GfA_VDn-JtNJD19RBwwaPpm8k_MCaM27IadI-q6uU0DtEnzhk_WSwOOEhy94Pt_bh7N6CzTTK3oRQZHOjuJGtZGtmFmbra_aWtwBvWLEuehRectfO8osco8QyVnyo";
        UserInfo userInfo = JwtUtils.getUserInfo(publicKey, token);
        System.out.println("id:" + userInfo.getId());
        System.out.println("name:" + userInfo.getName());
    }

//    @Test
//    public void parseToken1() {
//    }
//
//    @Test
//    public void getUserInfo() {
//    }
//
//    @Test
//    public void getUserInfo1() {
//    }
}