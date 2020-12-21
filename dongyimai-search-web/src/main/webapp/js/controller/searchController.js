app.controller('searchController',function ($scope,$location,searchService) {

   //搜索
   $scope.search = function () {
        //转换字符串为数字,防止报错
       $scope.searchMap.pageNo= parseInt($scope.searchMap.pageNo) ;
       searchService.search($scope.searchMap).success(
           function (response) {
               $scope.resultMap = response;

               buildPageLabel();
           }
       )
   };
    //搜索对象
   $scope.searchMap={'keywords':'','category':'','brand':'','price':'','spec':{},'pageNo':1,'pageSize':10,'sortField':'','sort':''};


    //添加搜索项
    $scope.addSearchItem=function(key,value){
        $scope.searchMap.pageNo=1;
        if(key=='category' || key=='brand'|| key=='price'){//如果点击的是分类或者是品牌
            $scope.searchMap[key]=value;
        }else{
            $scope.searchMap.spec[key]=value;
        }
        $scope.search();//执行搜索
    };


    //移除复合搜索
    $scope.removeSearchItem=function(key){
        $scope.searchMap.pageNo=1;
        if(key=="category" ||  key=="brand"|| key=='price'){//如果是分类或品牌
            $scope.searchMap[key]="";
        }else{//否则是规格
            delete $scope.searchMap.spec[key];//移除此属性
        }
        $scope.search();//执行搜索
    };

    //分页标签
    buildPageLabel = function () {
        $scope.pageLabel=[];
        var maxPageNo= $scope.resultMap.totalPages;//得到最后页码
        var firstPage=1;//开始页码
        var lastPage=maxPageNo;//截止页码
        $scope.firstDot=true;
        $scope.lastDot=true;
        if($scope.resultMap.totalPages> 5){  //如果总页数大于5页,显示部分页码
            if($scope.searchMap.pageNo<=3){//如果当前页小于等于3
                lastPage=5; //前5页
                $scope.firstDot=false;
            }else if( $scope.searchMap.pageNo>=lastPage-2  ){//如果当前页大于等于最大页码-2
                firstPage= maxPageNo-4;
                $scope.lastDot=false;//后5页
            }else{ //显示当前页为中心的5页
                firstPage=$scope.searchMap.pageNo-2;
                lastPage=$scope.searchMap.pageNo+2;
                $scope.firstDot=false;
                $scope.lastDot=false;
            }
        }
        //循环产生页码标签
        for(var i=firstPage;i<=lastPage;i++){
            $scope.pageLabel.push(i);
        }
    };

    //绑定分页的页码
    $scope.queryByPage=function (pageNo) {
        if(pageNo< 1 || pageNo > $scope.resultMap.totalPages){
            return;
        }
        $scope.searchMap.pageNo = pageNo;
        $scope.search();
    };

    //判断是否是第一页

    $scope.isFirstPage=function () {
        if($scope.searchMap.pageNo == 1){
            return true;
        }else{
            return false;
        }

    };
    
    //显示当前页的颜色为active
    $scope.isCurrentPage=function (page) {

            if(parseInt($scope.searchMap.pageNo)==parseInt(page)){
                return true;
            }else {
                return false;
            }

    };
    //隐藏品牌
    $scope.keywordsIsBrand=function () {
        for (var i = 0 ; i < $scope.resultMap.brandList.length; i++){
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0){
                return false;
            }
        } 
        return true;
    };

    //排序方法
    $scope.sortSearch= function (sortField,sort) {
        $scope.searchMap.sortField=sortField;
        $scope.searchMap.sort= sort;
        $scope.search();

    };

    //接受页面跳转来的请求数据
    $scope.loadKeywords=function () {
        $scope.searchMap.keywords = $location.search()['keywords'];
        $scope.search();
    }


    
});