<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<div class="modal-header" style="background-color:#f5f5f5;">
    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
    <h4 class="modal-title" id="myModalLabel" style="text-align:left;font-weight:bold;">编辑显示列</h4>
</div>
<div class="modal-body">
    <%--<div class="row" style="height:35px;">--%>
    <%--<div class="r_text" style="width:70px;padding-top:5px;">属性名：</div>--%>
    <%--<div class="r_box" style="width:450px;"><input name="" id="columnName" type="text" class="textbox1" style="width:200px;height:25px;">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input name="" type="button" class="btn2" style="width:60px" value="添加" onclick='columnAdd();'></div>--%>
    <%--</div>--%>
    <div class="row" style="margin-left:20px;">
        <div style="text-align:left;color:#428BCA;font-size:12px;">注：这里选择的显示列是除主键列以外的普通列！</div>
    </div>
    <div  class="tablebg1" style="height:35px;width:500px;margin-left:20px;margin-top:0px;">
        <div class="text_center" style="padding-top:10px">已添加属性（最多显示5列）</div>
    </div>
    <div  class="tablebg2" style="width:500px;height:175px;margin-left:20px;">
        <!-- <div style="margin-left:10px;height:160px;text-align:left;" id="columnList"> -->
        <table id="columnList" data-show-header="false" style="table-layout:fixed; word-wrap:break-word;border-collapse:collapse">
            <thead>
            <tr>
                <th data-field="state" data-checkbox="true"></th>
                <th data-field="property" data-align="center"></th>
                <th data-field="" data-align="center"></th>
                <%--<th class="col-operate-property" data-field="operate" data-align="right" data-formatter="propertyOperateFormatter" data-events="operateEvents"></th>--%>
            </tr>
            </thead>
        </table>
        <!-- </div> -->
        <!-- <div class="row" style="margin-left:10px;margin-top:10px;">
        	<input name="" type="button" class="btn2" style="width:60px" value="向上" onclick="goUp();">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input name="" type="button" class="btn2" style="width:60px" value="向下" onclick='goDown();'></div> -->
    </div>
    <div class="row" style="margin-left:20px;height:20px;margin-top:5px;"><div class="r_text14" id="columnEditStatus" style="width:500px;text-align:center"></div></div>
    <div class="row" style="margin-top:0px;">
        <div class="cy_text">
            <input id="btn_editproperty" name="" type="button" class="btn2" style="width:60px" value="确定" onclick='columnConfirm();'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <input id="btn_cancelproperty" name="" type="button" class="btn2" style="width:60px" value="取消" data-dismiss="modal">
        </div>
    </div>
</div>

<script type="text/javascript">
    var columns = [];
    var table_PKcol_data=[];
    //	var propertyToAdd = [];
    var propertyToShow = [];

    $(function(){
        $('#columnList').bootstrapTable({
            data: [],
            classes: 'table-condensed',
            undefinedText: '',
            formatNoMatches: function () {
                return '';
            }
        });
        columnEditWindow();
    });

    function propertyOperateFormatter(value, row, index) {
        return [
            '<input name="" type="button" class="propertyDelete btn3" style="width:50px;" value="移除" data-method="remove">',
        ];
    }

    window.operateEvents = {
        'click .propertyDelete': function (e, value, row, index) {
            columnDelete(row);
        },
        'click .recordEdit': function (e, value, row, index) {
            clickEditRecord(row);
        },
        'click .recordDelete': function (e, value, row, index) {
            clickDeleteRecord(row);
        },
        'click .recordMultiDelete': function (e, value, row, index) {
            deleteMultiRecord();
        }
    };

    function columnEditWindow(){
        debugger
        $("#columnName").attr("value", "");
        $("#columnEditStatus").html("");
        $.ajax({
            cache: false,
            type: "GET",
//	 			url: "/otscfgsvr/api/table/" + modal_table_name + "/display_columns",
//                url:"wls_Column_tableInfo.json",
//                url:"/otscfgsvr/api/table/" + modal_table_name ,
            url:"wls_colInfo_tableInfo.json",//请求某张表的所有的列的信息
            dataType: "json",
            timeout:30000,
            success:function(results, msg){
                if (results["errcode"] == 0) {
                    var all_columns = results["table_columns"];
                    var record_primary_keys = results["primary_key"];
                    var column_name;
                    var count_cols=0;
                    var count_PKcols=0;
                    var table_col_data=[];

                    var ifPKCol ;
                    for (var i = 0; i < all_columns.length; i++) {
                        column_name = all_columns[i]["col_name"];
                        if ($.inArray(column_name, record_primary_keys) > -1) {
                            ifPKCol = 1;
                        } else {
                            ifPKCol = 0;
                        }
//                                //动态加载显示列
//                                optionsCol += "<option value=\"" + i + "\" >" + column_name + "</option>";
//                                $("#displayColItem").html(optionsCol);
//                                $('#displayColItem').multipleSelect();
                        //动态加载主键列
                        if(!ifPKCol){
                            table_col_data[count_cols] = {"property": column_name,"state":true};
                            count_cols++;
                        }
                        if(ifPKCol){
                            table_PKcol_data[count_PKcols] = column_name;
                            count_PKcols++;
                        }

                    }
                    $('#columnList').bootstrapTable('destroy').bootstrapTable({
                        data: table_col_data,
                        classes: 'table-condensed',
                        undefinedText: '',
                        formatNoMatches: function () {
                            return '';
                        }
                    });
                }
                else {
                    $("#columnEditStatus").append("获取显示列信息错误: " + results["errcode"]);
                }
            },
            error: function(msg){
                if (msg["status"] != 404){
                    var errmsg = "获取显示列信息错误: " + getStructMsg(msg);
                    $("#columnEditStatus").append(errmsg);
                }
            }
        });
    }

    //	 	function columnAdd(){
    //	 		var column_name = $("#columnName").val();
    //	 		if (!column_name){
    //	 			return;
    //	 		}
    //	 		$("#columnEditStatus").html("");
    //	 		if (column_name == "hash_key" || column_name == "range_key") {
    //	 			$("#columnEditStatus").append("hash_key和range_key为不可编辑属性。");
    //	 			return;
    //	 		}
    //	 		for (var i=0; i<columns.length; i++){
    //	 			if (column_name == columns[i]["property"]){
    //	 				$("#columnEditStatus").append("该列已添加。");
    //		 			return;
    //	 			}
    //	 		}
    //	 		if (columns.length >= 5){
    //	 			$("#columnEditStatus").append("最多展示5列。");
    //	 			return;
    //	 		}
    //	 		columns.push({"property": htmlEscape(column_name),"state": true});
    //	 		$('#columnList').bootstrapTable('destroy').bootstrapTable({
    //    	        data: columns,
    //    	        classes: 'table-condensed',
    //                undefinedText: '',
    //                formatNoMatches: function () {
    //                	return '';
    //                }
    //    	    });
    //		}

    //		function columnDelete(editRow) {
    //			$("#columnEditStatus").html("");
    //			var dataArray = [];
    //        	var valuesArray = [];
    //        	dataArray[0] = editRow;
    //        	valuesArray[0] = editRow["property"];
    //        	$table = $('#columnList').bootstrapTable({
    //                data: dataArray
    //            });
    //			$table.bootstrapTable('remove', {
    //                field: 'property',
    //                values: valuesArray
    //            });
    //		}


    function columnConfirm(){
        debugger
//			propertyToAdd  = [];
        $("#columnEditStatus").html("");
        var $table = $('#columnList').bootstrapTable();
        var selects = $table.bootstrapTable('getSelections');
        propertyToShow = $.map(selects, function (row) {
            return htmlUnescape(row.property);
        });
        //todo 写编辑列
//            var record_cols_primary_keys = results["primary_key"];
//			for (var i=0; i<columns.length; i++) {
//				propertyToAdd.push(htmlUnescape(columns[i]["property"]));
//			}
//			var property_data = "\"propertyToAdd\":\"" + propertyToAdd.join(",") + "\",\"propertyToShow\":\"" + propertyToShow.join(",") + "\"";
        var property_data = table_PKcol_data;
        property_data.push.apply(property_data,propertyToShow);
        $.ajax({
            type: "POST",
//                url: "/otscfgsvr/api/table/" + modal_table_name + "/query",
            url:"wls_1_recordInfo.json",
            contentType: "application/json",
//	 			data: JSON.stringify({"columns": property_data}),
            data: JSON.stringify({"return_columns": property_data}),
            dataType: "json",
            timeout:30000,
            success:function(results, msg){
                if (results["errcode"] == 0) {
//	            		table_detail_query_columns = propertyToShow;
                    table_detail_query_columns = property_data;
                    getTableDetailShowColumns();
                    getRecordList();
                    $("#columnEditStatus").append("编辑显示列成功。");
                }
                else {
                    $("#columnEditStatus").append("保存显示列信息失败！错误: " + results["errcode"]);
                }
            },
            error: function(msg){
                var errmsg = "保存显示列信息失败！错误: " + getStructMsg(msg);
                $("#columnEditStatus").append(errmsg);
            }
        });
    }
</script>
