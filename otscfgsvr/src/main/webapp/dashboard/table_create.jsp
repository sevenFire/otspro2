<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%> 

<div class="modal-header" style="background-color:#f5f5f5;">
    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
            <h4 class="modal-title" id="myModalLabel" style="text-align:left;font-weight:bold;">表的创建</h4>
</div>
<div class="modal-body">
	<form class="" id="tableCreateForm" method="get" action="">
		<div  class="row" style="margin-top:0px;">
			<div class="r_text" style="width:110px;padding-top:3px;">*表名：</div>
     		<div class="r_box" style="width:165px;"><input name='tableNameText1' id='tableNameText1' type='text' class='' style='width:150px;height:25px;' value=''></div>
     	</div>
	   <div  class="row">
     		<div class="r_text" style="width:110px;padding-top:3px;">*主键类型：</div>
    			<div class="r_box" style="width:165px;">
    				<select name="keyTypeTableCreate" id="keyTypeTableCreate" onchange="keyTypeTableCheck()" class="table-select">
     					<option value="0">Hash键</option>
     					<option value="1" selected>Hash键 + Range键</option>
     				</select>
     			</div>
     			<div class="r_text" style="width:20px;padding-top:3px;text-align:left"><span class="glyphicon glyphicon-question-sign" title="详见帮助文档设计说明：【配置中心用户手册】->【表操作】->【创建表】"></span></div> 
     	</div>
		<div class="row">
			<div class="r_text" style="width:110px;padding-top:3px;">*Hash键类型：</div>
			<div class="r_box" style="width:165px;">
    			<select name="hashKeyTypeTableCreate" id="hashKeyTypeTableCreate" class="table-select">
     				<option value="0" selected>STRING</option>
     				<option value="1">NUMBER</option>
     				<option value="2">BINARY</option>
     			</select>
     		</div>
     	</div>
     	<div  class="row">
			<div class="r_text" style="width:110px;padding-top:3px;">Range键类型：</div>
			<div class="r_box" style="width:165px;">
    			<select name="rangeKeyTypeTableCreate" id="rangeKeyTypeTableCreate" class="table-select">
     				<option value="0" selected>STRING</option>
     				<option value="1">NUMBER</option>
     				<option value="2">BINARY</option>
     			</select>
     		</div>
     	</div>
   		<div  class="row">
     		<div class="r_text" style="width:110px;padding-top:3px;">压缩算法： </div>
    			<div class="r_box" style="width:165px;">
    				<select name="compression1" id="compression1" class="table-select">
     					<option value="0">NONE</option>
     					<option value="1" selected>SNAPPY</option>
     					<option value="2">LZ4</option>
     					<option value="3">GZ</option>
     					<!-- <option value="4">LZO</option> -->
     				</select>
     			</div>     
   		</div>
    	<!-- <div  class="row">
     		<div class="r_text" style="width:110px;padding-top:3px;">支持Mob：</div>
    			<div class="r_box" style="width:165px;">
    				<select name="enableMobSel1" id="enableMobSel1" onchange="enableMob1()" class="table-select">
     					<option value="1">是</option>
     					<option value="0" selected>否</option>
     				</select>
     			</div>
     	</div>
     	<div id="threshold1" class="row" style="display:none">	     				
	    	<div class="r_text" style="width:110px;padding-top:3px;">阈值：</div>
     		<div class="r_box" style="width:120px;"><input id="thresholdText1" name="thresholdText1" type="text" class="textbox1" style="width:120px;height:25px;" value="100"></div>
    		<div class="r_text" style="width:30px;padding-top:3px;text-align:left">KB</div>
    		 
    		<div class="r_box" >
    			<select id="thresholdUnit1">
     				<option value="kb" selected>KB</option>
     				<option value="m" >M</option>
     			</select>
     		</div> 
     		
		</div>  -->
       	<div  class="row">
     		<div class="r_text" style="width:110px;padding-top:3px;">描述：</div>
     		<div class="r_box" style="width:165px;"><input name="description1" id="description1" type="text" class="textbox1" style="width:150px;height:25px;"></div>
    	</div>
    	<div class="row" style="margin-top:0px;height:20px;"><div class="r_text14" id="tableCreateStatus" style="width:350px;text-align:center"></div></div>
    	<div  class="row" style="margin-top:0px;">
     		<div class="r_text" style="width:90px;padding-top:5px;"></div>
     		<div class="r_box" style="width:240px;">
     			<input id="btn_createtable" name="btn_createtable" type="submit" class="btn2" style="width:60px" value="创建">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
     			<input id="btn_canceltable" name="btn_createtable" type="button" class="btn2" style="width:60px" value="取消" data-dismiss="modal">
     		</div>
   		</div>
   		<div  class="row"></div>
	</form> 
</div>

 <script type="text/javascript">
 	var thresholdvalue=0;

 	$(function(){ 
		$("#tableCreateStatus").html("");
 		$("#tableNameText1").attr("value", "");
 		
 		$("input[type='text']").focus(function() {
			$("#tableCreateStatus").html("");
    	});
 		$.validator.setDefaults({
		    submitHandler: function() {
		    	tableCreate();
		    }
		});
 		$("#tableCreateForm").validate({
			debug: true,
			rules: {
				tableNameText1: {
					required: true,
					maxlength: 128
				},
				description1: {
					maxlength: 256
				}
			},
			messages: {
				thresholdText1: {
					range: "范围：{0}-{1}"
				}
			},
			errorClass: "r_text14",
			errorElement: "span",
		});
 	});
 	
 	/* function enableMob1() {
		if ($("#enableMobSel1").val() == 1) {
			$("#thresholdText1").rules("add",{required:true, digits:true, range:[100,10240]});
			document.getElementById("threshold1").style.display = 'block';
		}
		else if ($("#enableMobSel1").val() == 0) {
			$("#thresholdText1").rules("remove");
			document.getElementById("threshold1").style.display = 'none';			
		}	
		$("#tableCreateStatus").html("");
 	} */

 	function tableCreate(){
	 	$("#tableCreateStatus").html("");
	 	var tableCreateMap = {};
	 	
	 	//表名只能包含拉丁字母或数字或下划线“_”，且只能以字母开头，且不能以下划线“_”开头和结尾，且不能包含连续下划线“_”。
		if(!isValidName($("#tableNameText1").val()))
		{
			$("#tableCreateStatus").html("");
    		$("#tableCreateStatus").append("创建表错误: " + "表名只能包含拉丁字母或数字或下划线“_”，且只能以字母开头，且不能以下划线“_”开头和结尾，且不能包含连续下划线“_”。");
			return;
		}
		tableCreateMap["primary_key_type"] = $("#keyTypeTableCreate").val();
		tableCreateMap["hash_key_type"] = $("#hashKeyTypeTableCreate").val();
		
		if ($("#keyTypeTableCreate").val() == 1) {
			tableCreateMap["range_key_type"] = $("#rangeKeyTypeTableCreate").val();
		}
		
		tableCreateMap["compression_type"] = Number($("#compression1").val());
		/* tableCreateMap["mob_enabled"] = false;
		tableCreateMap["mob_threshold"] = 0;
		if($("#enableMobSel1").val() == 1) {	
			thresholdvalue = $("#thresholdText1").val();
			tableCreateMap["mob_enabled"] = true;
			tableCreateMap["mob_threshold"] = thresholdvalue;
		} */
		
		tableCreateMap["description"] = $("#description1").val();
		
 		$.ajax({
            type: "POST",
            url: "/otscfgsvr/api/table/" + $("#tableNameText1").val(),
            data: JSON.stringify(tableCreateMap),
            dataType: "json",
            contentType: "application/json",
            timeout: 30000,
            success: function (results, msg) {
            	$("#tableCreateStatus").html("");
				$("#tableCreateStatus").append("创建表").append(errorInfo(results["errcode"]));
				if(results["errcode"] == 0)
					getTableInfo();
            },
        	error: function(msg){
        		$("#tableCreateStatus").html("");
            	var errmsg = "创建表失败！错误: " + getStructMsg(msg);
            	if (errmsg.indexOf("Forbidden") >= 0)
            		errmsg += "（用户缺失创建表的权限！）";
            	$("#tableCreateStatus").append(errmsg);
            }
        });
	}
 	
 	function keyTypeTableCheck(){
 		if (document.getElementById("rangeKeyTableCreate-error")) {
 			$("#rangeKeyTableCreate-error").html("");
 		}
		if ($("#keyTypeTableCreate").val() == 0) {
			$("#rangeKeyTypeTableCreate").attr("disabled","disabled");
			$("#rangeKeyTypeTableCreate").css("background-color", "#EBEBE4");
			$("#rangeKeyTableCreate").attr("disabled","disabled");
		} else if ($("#keyTypeTableCreate").val() == 1){
			$("#rangeKeyTypeTableCreate").removeAttr("disabled");
			$("#rangeKeyTypeTableCreate").css("background-color", "white");
			$("#rangeKeyTableCreate").removeAttr("disabled");
		}
	}
 </script>
