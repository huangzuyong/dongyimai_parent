package com.offcn.cart.service;

import com.offcn.entity.Cart;

import java.util.List;

public interface CartService {
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num );
    /*redis查询购物车*/
    public List<Cart> findCartListFromRedis(String username);
    /*保存购物车到redis*/
    public void  saveCartListToRedis(String username,List<Cart> cartList );
    /*合并购物车*/
    public List<Cart> mergeCartList(List<Cart> cartList1,List<Cart> cartList2);
}
