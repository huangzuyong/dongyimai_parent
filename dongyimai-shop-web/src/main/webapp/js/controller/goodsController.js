 //控制层
app.controller('goodsController' ,function($scope ,$controller,uploadService,$location,itemCatService,goodsService,typeTemplateService){

    $controller('baseController',{$scope:$scope});//继承
    //读取列表数据绑定到表单中
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}
		);
	}

	//分页
	$scope.findPage=function(page,rows){
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}
		);
	};

	//查询实体
	$scope.findOne=function(id){
		var id = $location.search()['id'];
		if(id==null){
		    return ;
        }
        goodsService.findOne(id).success(
            function (response) {
                $scope.entity=response;
                //向富文本编辑器添加商品介绍
                editor.html($scope.entity.goodsDesc.introduction);
                //显示图片列表
                $scope.entity.goodsDesc.itemImages= JSON.parse($scope.entity.goodsDesc.itemImages);
                //显示扩展属性
                $scope.entity.goodsDesc.customAttributeItems= JSON.parse($scope.entity.goodsDesc.customAttributeItems);
                //规格
                $scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);
                //SKU列表规格列转换
                for( var i=0;i< $scope.entity.itemList.length;i++ ) {
                    $scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
                }
            }
        );


	}


    //保存
    $scope.save=function(){

        //取富文本内容绑定给entity
        $scope.entity.goodsDesc.introduction = editor.html();

        var serviceObject;
        //根据id判断是新增还是修改
        if($scope.entity.goods.id  != null){//修改
            serviceObject = goodsService.update( $scope.entity  );
        }else{//新增
            serviceObject =  goodsService.add( $scope.entity  );
        }


        serviceObject.success(
			function(response){
				if(response.success){
                    alert(response.message);
                    $scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]},itemList:[]};//无列表在里面,直接置空,效率高
                    editor.html('');//清空富文本编辑器
                    location.href="goods.html";//跳转到商品列表页
				}else{
					alert(response.message);
				}
			}
		);
	};


	//批量删除
	$scope.dele=function(){
		//获取选中的复选框
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}
			}
		);
	};

	$scope.searchEntity={};//定义搜索对象

	//搜索
	$scope.search=function(page,rows){
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}
		);
	};

	//商品组合实体类
    $scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]},itemList:[]};


    /**
     * 上传图片
     */
    $scope.image_entity = {};
    $scope.uploadFile=function(){
        uploadService.uploadFile().success(function(response) {
            if(response.success){//如果上传成功，取出url
                $scope.image_entity.url=response.message;//设置文件地址
            }else{
                alert(response.message);
            }
        }).error(function() {
            alert("上传发生错误");
        });
    };
    $scope.add_image_entity=function(){
        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);
    };
    //列表中移除图片
    $scope.remove_image_entity=function(index){
        $scope.entity.goodsDesc.itemImages.splice(index,1);
    };

    //根据查询商品分类的一级下拉列表
    $scope.selectItemCat1List=function () {

        itemCatService.findByParentId(0).success(
            function(response){
                $scope.itemCat1List=response;
            }
        );
    };
    //读取二级分类  $watch在监控的category1Ids是前端定义的,这是angular的内置方法,一旦监控的量变化调用方法
    $scope.$watch('entity.goods.category1Id', function(newValue, oldValue) {
        //判断一级分类有选择具体分类值，在去获取二级分类
        if(newValue){//自带判断
            //根据选择的值，查询二级分类
            itemCatService.findByParentId(newValue).success(
                function(response){
                    $scope.itemCat2List=response;
                }
            );
        }
    });//读取三级分类
    $scope.$watch('entity.goods.category2Id', function(newValue, oldValue) {
        //判断二级分类有选择具体分类值，在去获取三级分类
        if(newValue){
            //根据选择的值，查询二级分类
            itemCatService.findByParentId(newValue).success(
                function(response){
                    $scope.itemCat3List=response;
                }
            );
        }
    });
    $scope.$watch('entity.goods.category3Id', function(newValue, oldValue) {
        //判断三级列表对应的模板id
        if(newValue){
            //根据选择的值，查询二级分类
            itemCatService.findOne(newValue).success(
                function(response){
                    $scope.entity.goods.typeTemplateId=response.typeId;//response返回的是后台的代码去给域中赋值
                }
            );
        }
    });
    $scope.$watch('entity.goods.typeTemplateId', function(newValue, oldValue) {
        if(newValue){
            typeTemplateService.findOne(newValue).success(
                function(response){
                    $scope.typeTemplate=response;//获取类型模板
                    //brandIds时字符串转换为json数据才可以读取为数组
                    $scope.typeTemplate.brandIds= JSON.parse( $scope.typeTemplate.brandIds);//品牌列表
                   /* $scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.typeTemplate.customAttributeItems);*/
                    if($location.search()['id']==null){
                        $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);//扩展属性
                    }				                }
            );
            //查询规格列表
            typeTemplateService.findSpecList(newValue).success(
                function(response){
                    $scope.specList=response;
                }
            );
        }
    });


    //保存选中的选项到数据库中
/*    $scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]},itemList:[]};*/
    $scope.updateSpecAttribute=function($event,name,value) {
        var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems, 'attributeName', name);
        if (object != null) {
            if ($event.target.checked) {
                object.attributeValue.push(value);
            } else {
                //取消勾选
                object.attributeValue.splice(object.attributeValue.indexOf(value), 1);//移除选项
                //如果选项都取消了，将此条记录移除
                if (object.attributeValue.length == 0) {
                    $scope.entity.goodsDesc.specificationItems.splice($scope.entity.goodsDesc.specificationItems.indexOf(object), 1);
                }
            }
        } else {
            $scope.entity.goodsDesc.specificationItems.push({"attributeName": name, "attributeValue": [value]});
        }
    }


    $scope.createItemList=function () {
        $scope.entity.itemList=[{spec:{},price:"",num:"",status:'0',isDefault:'0' } ];
        var item =$scope.entity.goodsDesc.specificationItems;
        for(var i = 0; i < item.length;i++){
            //双向绑定
            $scope.entity.itemList = addColumn( $scope.entity.itemList,item[i].attributeName,item[i].attributeValue );
        }
    }
    addColumn=function(skuList,attributeName,attributeValue){
        var newList = [];
        for(var i= 0; i<skuList.length;i++){
            var oldRow = skuList[i];
            for(var j=0;j <attributeValue.length;j++ ){
                //深克隆,在newRow和oldRow相等时,其实是将两个变量的指向同一地址的,但是当newRow被赋值的时候会把oldRow的原值被替换
                //所以需要进行深克隆,使oldRow的jsonS数据转换为string再转换为json,数据在赋值,但是地址在变换
                var newRow = JSON.parse(JSON.stringify(oldRow));
                newRow.spec[attributeName]=attributeValue[j];
                newList.push(newRow);
            }
        }
        return newList;
    };

    $scope.status = ['未审核','已审核','审核未通过','关闭'];

    $scope.itemCatList = [];

    $scope.findItemCatList=function () {
        itemCatService.findAll().success(
            function (response) {
                for(var i=0;i<response.length;i++){
                    $scope.itemCatList[response[i].id] = response[i].name;
                }
            }
        );
    };
    //根据规格名称和选项名称返回是否被勾选
    $scope.checkAttributeValue=function(specName,optionName){
        var items= $scope.entity.goodsDesc.specificationItems;
        var object= $scope.searchObjectByKey(items,'attributeName',specName);
        if(object==null){
            return false;
        }else{
            if(object.attributeValue.indexOf(optionName)>=0){
                return true;
            }else{
                return false;
            }
        }
    }

});