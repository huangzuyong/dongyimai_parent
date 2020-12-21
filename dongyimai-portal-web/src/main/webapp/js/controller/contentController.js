app.controller("contentController",function($scope,contentService) {

    $scope.contentList = [];

    $scope.findByCartegoryId = function (categoryId) {
        contentService.findByCartegoryId(categoryId).success(
            function (response) {
                $scope.contentList[categoryId] = response;
            }
        );
    };
    //跳转页面
    //搜索跳转
    $scope.search=function(){
        location.href="http://localhost:9022/search.html#?keywords="+$scope.keywords;
    }




})