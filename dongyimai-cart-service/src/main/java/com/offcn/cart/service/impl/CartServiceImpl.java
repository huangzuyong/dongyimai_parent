package com.offcn.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.offcn.cart.service.CartService;
import com.offcn.entity.Cart;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbOrderItem;
import jdk.nashorn.internal.ir.IfNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import sun.security.action.PutAllAction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service

public class CartServiceImpl implements CartService {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate ;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {

        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            System.out.println("不存在该商品");
        }
        if (!item.getStatus().equals("1")){
            System.out.println("该商品已经下架");
        }

        String sellerId = item.getSellerId();
        /*判断是否添加购物车*/
        Cart cart = searchCartBySellerId(cartList, sellerId);
        /*购物车为空*/
        if (cart == null){
            cart=new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            TbOrderItem orderItem = createOrderItem(item,num);
            List orderItemList=new ArrayList();
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            cartList.add(cart);
        }else {
            //判断购物车明细是否存在该商品
            TbOrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            if (orderItem == null){
                /*TbOrderItem item1 = createOrderItem(item, num);*/
                //5.1. 如果没有，新增购物车明细
                orderItem=createOrderItem(item,num);
                cart.getOrderItemList().add(orderItem);
            }else{

                orderItem.setNum(orderItem.getNum()+num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getNum()* orderItem.getPrice().doubleValue()));

                if(orderItem.getNum() <= 0){
                    cart.getOrderItemList().remove(orderItem);
                }
                if (cart.getOrderItemList().size() <= 0){
                    cartList.remove(cart);
                }
            }
        }


        return cartList;
    }

    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("获取用户名称"+username);
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList == null) {
            cartList = new ArrayList<Cart>();
        }

        return cartList;
    }



    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {

        System.out.println("保存商品"+username);
        redisTemplate.boundHashOps("cartList").put(username, cartList);

    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {

        System.out.println("合并购物车");
        for (Cart cart :cartList2){
            for (TbOrderItem orderItem : cart.getOrderItemList()){
                cartList1 = addGoodsToCartList(cartList1, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartList1;
    }

    /*根据商品的id查询购物车我对象---->商家相当于对象*/
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId){

        for (Cart cart :cartList){
            if (cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return  null;
    }

/*根据商品 明细 ID查询*/
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList , Long itemId ){

        for(TbOrderItem orderItem :orderItemList){
            if (orderItem.getItemId().longValue()==itemId.longValue()){
                return orderItem;
            }
        }
        return  null;
    }

    /*创建订单的明细*/
    private  TbOrderItem createOrderItem(TbItem item,Integer num){

        if (num < 0){
            System.out.println("你准备倒送我??");
        }

        TbOrderItem orderItem=new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        /*价格的高精度计量
        * 总价
        * */
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue()*num));
        return orderItem;

    }




}
