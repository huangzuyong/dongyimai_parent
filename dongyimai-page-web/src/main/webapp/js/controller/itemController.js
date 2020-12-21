//商品详细页（控制层）
app.controller('itemController',function($scope,$http){
    //数量操作
    $scope.addNum=function(x){
        $scope.num=$scope.num+x;
        if($scope.num<1){
            $scope.num=1;
        }
    };

    $scope.specificationItems={};//记录用户选择的规格
    //用户选择规格
    $scope.selectSpecification=function(name,value){
        $scope.specificationItems[name]=value;
		searchSku();
    };

    //判断某规格选项是否被用户选中
    $scope.isSelected=function(name,value){
        if($scope.specificationItems[name]==value){
            return true;
        }else{
            return false;
        }
    };

	//加载sku
	$scope.loadSku=function(){
		$scope.sku=skuList[0];
		$scope.specificationItems= JSON.parse(JSON.stringify($scope.sku.spec)) ;
	};

	//匹配对象
	matchObject=function(map1,map2){
		for(var k in map1){
		  if(map1[k] != map2[k]){
			return false;
		  }
		}
		for(var k in map2){
		  if(map2[k] != map1[k]){
			return false;
		  }
		}
		return true;

	};

	//查询sku
	searchSku=function(){
		for(var i = 0 ; i < skuList.length; i++){
			if(matchObject(skuList[i].spec,$scope.specificationItems)){
				$scope.sku=skuList[i];
				return ;
			}
		}
		//空值是的传参
		$scope.sku = {id :0 ,title:'QAQ',price:0};
	};

	//购物车
	$scope.addToCart=function(){
        $http.get('http://localhost:9107/cart/addGoodsToCartList.do?itemId='
            + $scope.sku.id +'&num='+$scope.num,{'withCredentials':true}).success(
            function(response){
                if(response.success){
                    location.href='http://localhost:9107/cart.html';//跳转到购物车页面
                }else{
                    alert(response.message);
                }
            }
        );
    };


});