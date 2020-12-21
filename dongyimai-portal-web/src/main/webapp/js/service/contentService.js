app.service('contentService',function ($http) {


    this.findByCartegoryId = function (categoryId) {
       return $http.get('../content/findByCartegoryId.do?categoryId='+categoryId);
    }

})