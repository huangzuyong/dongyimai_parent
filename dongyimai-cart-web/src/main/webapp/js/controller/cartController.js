app.controller('cartController',function ($scope,cartService) {

   $scope.findCartList = function () {
       cartService.findCartList().success(
           function (response) {
               $scope.cartList = response;
               $scope.totalValue=cartService.sum($scope.cartList);//求合计数
           }
       )
   }
   
   $scope.addGoodsToCartList = function (itemId,num) {
       cartService.addGoodsToCartList(itemId,num).success(
         function (response) {
             if (response.success){
                 $scope.findCartList();
             }else {
                 alert(response.message);
             }
         }  
       );
   };

    $scope.showName=function(){
        cartService.showName().success(
            function(response){
                $scope.loginName=response.loginName;
            }
        );
    }

    $scope.findAddressList = function () {
        cartService.findAddressList().success(
            function (response) {
                $scope.addressList = response;
                /*default Address*/

                for (var i = 0 ; i < $scope.addressList.length;i++){
                    if ($scope.addressList[i].isDefault == "1") {
                        $scope.address=$scope.addressList[i];
                        break;
                    }
                }


            }
        )

    };
    
    $scope.selectAddress= function (address) {

        $scope.address = address;
        
    };
    
    $scope.isSelectAddress= function (address) {

        if (address == $scope.address){
            return true ;
        } else{
            return false ;
        }

    };

    $scope.order ={paymentType:'1'};

    /*change payment*/
    $scope.selectPayType = function (type) {
        $scope.order.paymentType = type;
    };

    $scope.submitOrder = function () {

        $scope.order.receiverAreaName=$scope.address.address;//地址
        $scope.order.receiverMobile=$scope.address.mobile;//手机
        $scope.order.receiver=$scope.address.contact;//联系人

        cartService.submitOrder($scope.order).success(
            function (response) {
                if (response.success){
                    if ($scope.order.paymentType == '1'){
                        location.href="pay.html";
                    }else{
                        location.href="paysuccess.html";
                    }
                } else {
                    alert(response.message);
                }

            }
        )

    }

});