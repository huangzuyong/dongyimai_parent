package com.offcn.page.service;



public interface ItemPageService {
    /*
    * 生成商品的详情页
    * */
    public boolean  genItemHtml(Long goodsId);


    /*
    delete static page
    * */

    public boolean deleteItemHtml(Long[] goodsIds);
}
