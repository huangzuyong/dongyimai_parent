app.controller('baseController',function ($scope) {

    $scope.reloadList=function(){
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    };

    //定义angular的分页对象
    $scope.paginationConf = {
        currentPage: 1,
        itemsPerPage: 10,
        perPageOptions: [10,20,30,40,50],
        onChange: function () {//1.点击变化;2.初始化触发
            //分页查询
            $scope.reloadList();
        }
    }
//勾选框
    $scope.selectIds = [];
    $scope.updateSelection=function ($event, id) {//id
        if($event.target.checked){//勾选,push
            $scope.selectIds.push(id);
        }else{//取消,splicen
            var index = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(index, 1);
        }
    };

    $scope.jsonToString=function (jsonString, key) {
        //json转换
        var jsonArray = JSON.parse(jsonString);
        var value= "";
        for(var i=0;i<jsonArray.length;i++){
            if(i>0){
                value += ","
            }
            value += jsonArray[i][key];
        }
        return value;
    };

    //从集合中按照key查询对象
    $scope.searchObjectByKey=function(list,key,keyValue){
        for(var i=0;i<list.length;i++){
            if(list[i][key]==keyValue){
                return list[i];
            }
        }
        return null;
    }

})