<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>

<div class="modal-header" style="background-color:#f5f5f5;">
	<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
	<h4 class="modal-title" id="myModalLabel" style="text-align:left;font-weight:bold;">表的创建-向导</h4>
</div>

<div class="modal-body">
	<div class="steps-container">
		<ul class="wizard-steps">
			<li id="wizard-step1" class="active">
				<span class="step">1</span>
				<span class="title">基本信息</span>
			</li>
			<li id="wizard-step2">
				<span class="step">2</span>
				<span class="title">列信息</span>
			</li>
		</ul>
	</div>
	<div id="myCarousel" class="carousel slide" data-wrap="false">
		<div class="carousel-inner">
			<div class="item active">
				<div style="height:360px;width:650px;">
					<div class="row" style="height:40px;margin-left:130px;"></div>
					<div class="row" id="rowTableInfoTc" style="margin-left:90px;height:180px;">
						<div  class="row" style="margin-top:0px;">
							<div class="r_text" style="width:100px;padding-top:3px;">*表名：</div>
							<div class="r_box" style='width:370px;'>
								<input id='tc_tableName' type='text' class='' style='width:350px;height:25px;' value=''>
								<span class="glyphicon glyphicon-exclamation-sign" title="注意：一旦创建表名不可更改！"></span>
							</div>
							<div class="r_text14" id="tc_noTableName" style="padding-top:10px;width:45px;display:none;">必填！</div>
						</div>

						<div  class="row" style="margin-top:0px;">
							<div class="r_text" style="width:100px;padding-top:3px;">描述信息：</div>
							<div class="r_box">
								<textarea id="tc_description" class="textbox1" style="width:350px;height:80px;max-height:160px;resize:none;" onKeypress="checkEnter(event)"></textarea>
							</div>
						</div>
					</div>
					<div class="row" style="margin-left:50px;margin-top:60px;height:40px;">
						<div class="r_text14" id="tc_infoStatus" data-name="tc_status" style="margin-left:10px;width:500px;text-align:center"></div>
					</div>
					<div  class="row" style="margin-top:0px;margin-left:0px;">
						<div class="r_box" style="width:100%;text-align:center">
							<input id="tc_cancel1" name="" type="button" class="btn2" style="width:80px" value="取消" data-dismiss="modal">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
							<input id="tc_forward1" name="" type="button" class="btn2" style="width:80px" value="继续>>" href="#myCarousel" onclick='checkTableInfo();'>
						</div>
					</div>
				</div>
			</div>

			<div class="item">
				<div style="height:360px;width:650px;">
					<div class="row" id="" style="width:100%;overflow-y:auto;margin-left:140px;">
						<div class="r_box" style="width:150px;"><div class="c_text" style="width:130px;padding-top:3px;">*列名</div></div>
						<div class="r_box" style="width:90px;"><div class="c_text" style="width:80px;padding-top:3px;">*数据类型</div></div>
						<div class="r_box" style="width:30px;"><div class="c_text" style="width:30px;padding-top:3px;">主键</div></div>
						<div class="r_box" style="width:30px;"><div class="c_text" style="width:30px;padding-top:3px;">排序</div></div>
					</div>
					<div class="row" id="tc_tableColumns" style="overflow-y:auto;height:256px;margin-top:0;">
						<div class="tablebg2" id="tc_tableColumn1" style="width:500px;height:40px;margin-left:140px;margin-bottom:5px;">
							<div class="r_box" style="width:150px;">
								<input name="tc_tableColumnName1" id="tc_tableColumnName1" type="text" class="textbox1" style="width:130px;height:25px;">
							</div>
							<div class="r_box" style="width:90px;">
								<select name="tc_tableColumnType1" id="tc_tableColumnType1" style="width:90px;height:25px;" onchange="onSelectColumnType(1)">
									<option value="0">int8</option>
									<option value="1">int16</option>
									<option value="2">int32</option>
									<option value="3">int64</option>
									<option value="4">float</option>
									<option value="5">double</option>
									<option value="6">bool</option>
									<option value="7">string</option>
								</select>
							</div>
							<div class="r_box" style="width:30px;text-align:center">
								<input name="tc_tableColumnPk1" id="tc_tableColumnPk1" type="checkbox" onclick="disableSeq(1)" title="注意：一旦创建主键不可更改！">
							</div>
							<div class="r_box" style="width:30px;">
								<input name="tc_tableColumnSeq1" id="tc_tableColumnSeq1" type="text" class="textbox1" style="width:30px;height:25px;" disabled="disabled">
							</div>

							<div class="r_box" style="width:40px;padding-top:3px;text-align:left;">
								<span class="glyphicon glyphicon-plus" title="增加列" onclick='addTableColumn() ;'></span>
							</div>
						</div>
						<div class="row" id="tc_lastTableColumn" style="display:none"></div>
					</div>


					<div class="row" style="margin-left:50px;margin-top:0px;height:40px;">
						<div class="r_text14" id="tc_columnStatus" data-name="tc_status" style="margin-left:10px;width:550px;text-align:center"></div>
					</div>
					<div class="row" style="margin-left:50px;margin-top:0px;height:40px;">
						<div class="r_text14" id="tableCreateStatus" data-name="tc_status" style="margin-left:10px;width:550px;text-align:center"></div>
					</div>

					<div  class="row" style="margin-top:0px;margin-left:20px;">
						<div class="r_box" style="width:100%;text-align:center">
							<input id="tc_back2" name="" type="button" class="btn2" style="width:80px" value="<<返回" href="#myCarousel" onclick='goBack(2);' data-slide="prev">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
							<input id="tc_create" name="" type="button" class="btn2" style="width:80px" value="创建"  onclick='tableCreate();'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
							<input id="tc_cancel2" name="" type="button" class="btn2" style="width:80px" value="取消" data-dismiss="modal">
						</div>
					</div>
				</div>
			</div>

		</div>
	</div>
</div>

<script type="text/javascript">
    var tc_columns=[];
    var tc_tablename = "";
    var tc_tabledesc = "";
    var tableAddColumnId = 2;

    $(function() {
        $('.carousel').carousel({
            interval: false
        }).unbind("keydown");

        $("input[type='text']").focus(function() {
            $("#tc_noTableName").css("display", "none");
            clearTcStatus();
        });
        $("select").focus(function() {
            clearTcStatus();
        });


    });


    function checkEnter(e)
    {
        var et=e||window.event;
        var keycode=et.charCode||et.keyCode;

        if ((keycode==13)|| (keycode == 92))
        {
            if(window.event)
                window.event.returnValue = false;
            else
                e.preventDefault();//for firefox
        }
    }


    function checkTableInfo() {
        $("#tc_infoStatus").html("");
        //表名只能包含拉丁字母或数字或下划线“_”，且只能以字母开头，且不能以下划线“_”开头和结尾，且不能包含连续下划线“_”。
        if(!isValidName($("#tc_tableName").val()))
        {
            $("#tc_infoStatus").html("");
            $("#tc_infoStatus").append("表命名错误: " + "表名只能包含拉丁字母或数字或下划线“_”，且只能以字母开头，且不能以下划线“_”开头和结尾，且不能包含连续下划线“_”。");
            return;
        }

        if(!$("#tc_tableName").val() || $("#tc_tableName").val().trim().length == 0){
            $("#tc_noTableName").show();
            return;
        }
        else{
            $("#tc_noTableName").hide();
        }

        tc_tablename = $("#tc_tableName").val().trim();
        tc_tabledesc = $("#tc_description").val();
        if(tc_tablename.length > 128) {
            $("#tc_infoStatus").html("");
            $("#tc_infoStatus").append("表名长度不能超过128个字符。");
            return;
        }

//	    else if(!rex_check_str1.test(tc_tablename)) {
//        else if(!rex_check_str_1.test(tc_tablename)) {
//            $("#tc_infoStatus").html("表名只能包含拉丁字母或数字或下划线“_”。");
//            return;
//        }

        if(tc_tabledesc.length > 256) {
            $("#tc_infoStatus").html("描述信息长度不能超过256个字符。");
            return;
        }

        $("#myCarousel").carousel('next');
        $("#wizard-step1").attr("class", "complete");
        $("#wizard-step2").attr("class", "active");
    }

    //    function initEncodingSelects(i) {
    //        initEncodingOption($("#tc_tableColumnType" + i).find("option:selected").text(), i, 0);
    //    }

    function onSelectColumnType(i) {
//        initEncodingSelects(i);

        if ($('#tc_tableColumnType' + i).attr("value") == "4"
            || $('#tc_tableColumnType' + i).attr("value") == "5"
            || $('#tc_tableColumnType' + i).attr("value") == "6") {
            $('#tc_tableColumnPk' + i).css("display", "none");
            $('#tc_tableColumnSeq' + i).css("display", "none");

            $('#tc_tableColumnPk' + i).attr("checked", false);
            $('#tc_tableColumnSeq' + i).attr("value", "");
        } else {
            $('#tc_tableColumnPk' + i).css("display", "inline");
            $('#tc_tableColumnSeq' + i).css("display", "inline");
        }
    }

    function checkColumnNameCreate(name) {
        return checkColumnName(name, 0);
    }

    function checkColumnName(name, position) {		//position: 0-table_create; 1-column_add
        var statusId = "";
        switch (position) {
            case 0:
                statusId = "#tc_columnStatus";
                break;
            case 1:
                statusId = "#columnAddStatus";
                break;
            default:
                break;
        }
        if (name == "") {
            $(statusId).html("列名不能为空。");
            return false;
        }
        if(name.length > 128) {
            $(statusId).html("列名长度不能超过128个字符。");
            return false;
        }

        if(!isValidName(name))
        {
            $(statusId).html("列名只能包含拉丁字母或数字或下划线“_”。，且只能以字母开头，且不能以下划线“_”开头和结尾，且不能包含连续下划线“_”。");
            return false;
        }
        return true;
    }


    //添加distinct()
    Array.prototype.distinct = function() {
        debugger
        var obj = {};
        for (var i = 0; i < this.length; i++) {
//            var item = this[i].name.toLowerCase();
            var item = this[i].col_name.toLowerCase();
            if (!obj.hasOwnProperty(item)) {
                obj[item] = item;
            }
        }
        return obj;
    };



    function checkTableColumn() {
        $("#tc_columnStatus").html("");
        tc_columns= [];
        var col_num=0;
        var tc_column = new Object();
        for (var i = 1; i < tableAddColumnId; i++) {
            if($('#tc_tableColumnName'+i).length>0) {
                tc_column = new Object();
                if (checkColumnNameCreate($("#tc_tableColumnName" + i).val().trim())) {
                    tc_column["col_name"] = $("#tc_tableColumnName" + i).val().trim();
                    tc_column["col_type"] = $("#tc_tableColumnType" + i).find("option:selected").text();
                }
                else {
                    return false;
                }
                tc_columns[col_num++] = tc_column;
            }


        }
        var tmp_col = tc_columns.distinct();
        if (Object.getOwnPropertyNames(tmp_col).length < tc_columns.length) {
            $("#tc_columnStatus").html("列名不可以重复。");
            return false;
        }
        return true;
    }


    function addTableColumn() {
        $("#tc_columnStatus").html("");
        addColumn(tableAddColumnId);
        tableAddColumnId++;
    }
    //加列
    function addColumn(id) {
        var htmltext = '';
        htmltext += '<div class="tablebg2" id="tc_tableColumn' + id + '" style="width:500px;height:40px;margin-left:140px;margin-bottom:5px;">'
            + '<div class="r_box" style="width:150px;">'
            + '<input name="tc_tableColumnName' + id + '" id="tc_tableColumnName' + id + '" type="text" class="textbox1" style="width:150px;height:25px;">'
            + '</div>'
            + '<div class="r_box" style="width:90px;">'
            + '<select name="tc_tableColumnType' + id + '" id="tc_tableColumnType' + id +'" style="width:90px;height:25px;" onchange="onSelectColumnType(' + id + ')">'
            + '<option value="0">int8</option>'
            + '<option value="1">int16</option>'
            + '<option value="2">int32</option>'
            + '<option value="3">int64</option>'
            + '<option value="4">float</option>'
            + '<option value="5">double</option>'
            + '<option value="6">bool</option>'
            + '<option value="7">string</option>'
            + '</select></div>'
            //            + '<div class="r_box" style="width:80px;">'
            //            + '<input name="tc_tableColumnDesc' + id + '" id="tc_tableColumnDesc' + id + '" type="text" class="textbox1" style="width:80px;height:25px;">'
            //            + '</div>'
            + '<div class="r_box" style="width:30px;text-align:center">'
            + '<input name="tc_tableColumnPk' + id + '" id="tc_tableColumnPk' + id + '" type="checkbox" onclick="disableSeq(' + id + ')"  title="注意：一旦创建主键不可更改！">'
            + '</div>'
            + '<div class="r_box" style="width:30px;">'
            + '<input name="tc_tableColumnSeq' + id + '" id="tc_tableColumnSeq' + id + '" type="text" class="textbox1" style="width:30px;height:25px;" disabled="disabled">'
            + '</div>'
            + '<div class="r_box" style="width:40px;padding-top:3px;text-align:left;">'
            + '<span class="glyphicon glyphicon-plus" title="增加列" onclick="addTableColumn();"></span>&nbsp;&nbsp;'
            + '<span class="glyphicon glyphicon-minus" title="删除列" onclick="removeTableColumn(this);"></span></div>'
            + '</div>';
        $("#tc_lastTableColumn").before(htmltext);
    }

    function removeTableColumn(obj) {
//        debugger
        $("#tc_columnStatus").html("");
//        $('#tc_tableColumn' + (tableAddColumnId-1)).remove();
//        $(obj).parents(".tablebg2").remove();//parents不可靠
        $(obj).closest(".tablebg2").remove();//id
//        tableAddColumnId--;//不用减id
    }


    function disableSeq(id) {
        if ($("#tc_tableColumnPk" + id).attr("checked")) {
            $('#tc_tableColumnSeq' + id).removeAttr("disabled");
        }
        else {
            $('#tc_tableColumnSeq' + id).attr("value","");
            $('#tc_tableColumnSeq' + id).attr("disabled", "disabled");
        }
    }

    //添加主键列排序检查
    function checkPkSeq(list) {
        list.sort(sortNumber);
        for (var i = 0; i < list.length; i++) {
            if (list[i] != (i+1)) {
                return false;
            }
        }
        return true;
    }

    function sortNumber(a,b) {
        return a - b;
    }

    //定义一个比较器
    function compare(propertyName) {
        return function(object1, object2) {
            var value1 = object1[propertyName];
            var value2 = object2[propertyName];
            if (value2 < value1) {
                return 1;
            } else if (value2 > value1) {
                return -1;
            } else {
                return 0;
            }
        }
    }




    function tableCreate(){
        debugger;
        if (!checkTableColumn()) {
            return;
        }
        else {
            var pk_num = 0;
            var tc_pks = [];
            var tc_pks_sort = [];
            var tc_pks_sort_name = [];
            var pk_seq_check = [];
            var pk_seq_check_index = 0;
            for (var i = 1; i < tableAddColumnId; i++) {
                if($('#tc_tableColumnName'+i).length>0){
                    if ($('#tc_tableColumnPk' + i).attr("checked")) {
                        if ($('#tc_tableColumnType' + i).attr("value") == "4" || $('#tc_tableColumnType' + i).attr("value") == "5" || $('#tc_tableColumnType' + i).attr("value") == "6") {
                            $("#tc_columnStatus").html("主键类型不可以为float，double或bool。");
                            return;
                        }
                        var r = /^[0-9]*[1-9][0-9]*$/;
                        if (!r.test($('#tc_tableColumnSeq' + i).val().trim())) {
                            $("#tc_columnStatus").html("主键排序必须为1开始的连续整数序列（1,2,3...）。请为主键输入正确的排序。");
                            return;
                        }
                        pk_seq_check[pk_seq_check_index++] = $('#tc_tableColumnSeq' + i).val().trim();
                        tc_pks[pk_num] = {"name":$('#tc_tableColumnName' + i).val().trim(),"seq":parseInt($('#tc_tableColumnSeq' + i).val().trim())};
                        pk_num++;
                    }
                }
            }
            tc_pks_sort = tc_pks.sort(compare("seq"));
            for (var ii = 0; ii < tc_pks_sort.length; ii++) {
                tc_pks_sort_name[ii] = tc_pks_sort[ii]["name"];
            }


            if (pk_num == 0) {
                $("#tc_columnStatus").html("请至少选中一个主键。");
                return;
            }
            if (!checkPkSeq(pk_seq_check)) {
                $("#tc_columnStatus").html("主键排序必须为1开始的连续整数序列（1,2,3...）。请为主键输入正确的排序。");
                return;
            }

            var tableCreateMap = {};
            tableCreateMap["table_desc"] = tc_tabledesc;
            tableCreateMap["table_columns"] = tc_columns;
            tableCreateMap["primary_key"] = tc_pks_sort_name;
//            tableCreateMap["create_time"] = tc_tableCreateTime;
//            tableCreateMap["modify_time"] = tc_tableModifyTime;
//            tableCreateMap["creator"] = tc_tableCreator;
//            tableCreateMap["modifier"] = tc_tableModifier;
            var url = "/otscfgsvr/api/table/";
            var ots_tableName = $("#tc_tableName").val();
            url += ots_tableName;
//            alert("创建表");
//            return;

            $.ajax({
                type: "POST",
//			url: "wls_add_tableInfo.json",//创建表
                url: url,
                data: JSON.stringify(tableCreateMap),
                dataType: "json",
                contentType: "application/json",
                timeout: 30000,
                success: function (results, msg) {
                    $("#tableCreateStatus").html("");
                    $("#tableCreateStatus").append("创建表").append(errorInfo(results["errcode"]));
                    if(results["errcode"] == 0)
                        getTableInfo();
                    $("#tc_create").removeAttr("disabled");
                    $("#tc_create").css("background-color","white");
                    $("#wizard-step2").attr("class", "complete");
                    $("#tableCreateStatus").html("创建表成功！");
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
    }


    function goBack(step) {
        clearTcStatus();
        var preStep = step - 1;
        $("#wizard-step" + preStep).attr("class", "active");
        $("#wizard-step" + step).removeClass("active");
    }


    function clearTcStatus() {
        $("[data-name=tc_status]").html("");
    }
</script>



