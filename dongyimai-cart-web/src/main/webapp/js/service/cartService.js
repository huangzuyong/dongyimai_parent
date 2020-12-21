app.service('cartService',function ($http) {

    /*购物车列表*/
    this.findCartList = function () {
        return $http.get('cart/findCartList.do');
    };

    /*增删*/
    this.addGoodsToCartList = function (itemId,num) {
        return $http.get('cart/addGoodsToCartList.do?itemId='+itemId+'&num='+num);
    };

    //计算总件数,总价格
    this.sum=function (cartList) {

        var totalValue={totalNum:0, totalMoney:0.00};
        for(var i=0;i<cartList.length;i++){
            var cart = cartList[i];

            for(var j=0;j<cart.orderItemList.length;j++){
                totalValue.totalNum += cart.orderItemList[j].num;
                totalValue.totalMoney += cart.orderItemList[j].totalFee;
            }
        }
        return totalValue;
    };


    //读取列表数据绑定到表单中
    this.showName=function(){
        return $http.get('cart/name.do');
    }

        /*get address*/
    this.findAddressList = function () {
        return $http.get('address/findListByLoginUser.do')

    };
    //保存订单
    this.submitOrder=function(order){
        return $http.post('order/add.do',order);
    }


});