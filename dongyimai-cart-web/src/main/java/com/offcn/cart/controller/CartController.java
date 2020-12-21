package com.offcn.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.offcn.cart.service.CartService;
import com.offcn.entity.Cart;
import com.offcn.entity.Result;
import com.offcn.util.CookieUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Reference(timeout = 60000)
    private CartService cartService;

    /*
    * 获取购物车
    * */
    @RequestMapping("/findCartList")
    public List<Cart> findCartList (){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String cartListString  = CookieUtil.getCookieValue(request, "cartList", "UTF-8");

        if (cartListString == null || cartListString.equals("")) {
            cartListString = "[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
        if (username .equals("anonymousUser")){
                return cartList_cookie;
      } else{
            List<Cart> cartList_redis = cartService.findCartListFromRedis(username);
            /*合并购物车*/
            if( cartList_cookie != null && cartList_cookie.size()>0) {
                cartList_redis = cartService.mergeCartList(cartList_redis, cartList_cookie);
                //清除本地cookie的数据
                CookieUtil.deleteCookie(request, response, "cartList");
                //将合并后的数据存入redis
                cartService.saveCartListToRedis(username, cartList_redis);
            }
                return cartList_redis;
        }

    }

    /*添加购物车
    * */
    @RequestMapping("/addGoodsToCartList")
    @CrossOrigin(origins="http://localhost:9105")
    public Result addGoodsToCartList(Long itemId, Integer num){

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("当前的登录用户"+username);
        try {
            List<Cart> cartList = findCartList();
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);

            if (username .equals("anonymousUser")){
                CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600, "UTF-8");
                System.out.println("添加进cookie成功");
            }else{
                cartService.saveCartListToRedis(username, cartList);
                System.out.println("添加进redis成功");
            }
            return new Result(true, "添加成功");
        }catch (Exception e){
            e.printStackTrace();
            return  new Result(false, "添加进cookie失败");
        }


    }

        @RequestMapping("/name")
        public Map showName(){
            String name = SecurityContextHolder.getContext().getAuthentication().getName();//得到登陆人账号
            Map map=new HashMap();
            map.put("loginName", name);
            return map;
        }


}
