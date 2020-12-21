app.controller("baseController",function($scope) {

    var ids = [];
    $scope.getIds = function(id) {
        var index  = ids.indexOf(id);
        if(index>-1) { //说明该数字存在
            ids.splice(index, 1);
        }else { //说明该数字不存在
            ids.push(id);
        }
    //    由于ids是当前域的变量，要让另外一个brandController也是用该变量就需要将该变量赋到$scope域当中
    $scope.ids = ids;
    console.log($scope.ids);
    }

    $scope.reloadList = function() {
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }

    $scope.paginationConf = {
        currentPage : 1,
        totalItems : 10,
        itemsPerPage : 10,
        perPageOptions:[10,20,30,40],

        onChange : function () {
            $scope.reloadList();
        }
    }


})