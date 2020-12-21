
app.controller('brandController',function ($scope,$http,$controller,brandService) {

    $controller('baseController',{$scope:$scope});

    $scope.findAll=function () {
        brandService.findAll() .success(
            function (response) {
                $scope.list =response;
            }
        );
    };
    //对查询的对象进行分页
    $scope.findPage=function (page,rows) {
       brandService.findPage(page,rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;

            }
        );
    };




    $scope.findOne=function (id) {
        brandService.findOne(id).success(

            function (response) {
                $scope.entity = response;
            }

        );
    };

    $scope.saveBrand=function () {
        if ($scope.entity.id == null) {
           brandService.add($scope.entity).success(
                function (response) {
                    if (response.success){
                        $scope.reloadList();
                    }else{
                        alert(response.message);
                    }
                }
            );
        }else{
            brandService.update($scope.entity).success(
                function (response) {
                    if (response.success){
                        $scope.reloadList();
                    }else{
                        alert(response.message);
                    }
                }
            );
        }

    };


    $scope.deleteBrands= function () {
        brandService.delete($scope.selectIds).success(
            function(response){
                if(response.success){
                    $scope.reloadList();
                }
            }
        );
    };


    $scope.searchEntity={};//定义搜索对象
    $scope.search= function (page,rows){

        brandService.search(page,rows,$scope.searchEntity).success(

            function (response) {
                $scope.paginationConf.totalItems=response.total;//总记录数
                $scope.list=response.rows;//给列表变量赋值
            }

        );

    };
    $scope.brandList={data:[]};//品牌列表
    //读取品牌列表
    $scope.findBrandList=function(){
        brandService.selectOptionList().success(
            function(response){
                $scope.brandList={data:response};
            }
        );
    }


});