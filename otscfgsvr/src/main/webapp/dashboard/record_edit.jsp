<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="modal-header" style="background-color:#f5f5f5;">
    <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
    <h4 class="modal-title" id="myModalLabel" style="text-align:left;font-weight:bold;">编辑记录</h4>
</div>
<div class="modal-body">
    <%--<div class="row" style="text-align:left;margin-top:0px;">记录详细信息：</div>--%>
    <%--<div class="row" style="height:35px;"><textarea name="" id="recordEditContent" cols="" rows="" style="width:550px;height:210px;max-width:550px;max-height:210px;resize:none;"></textarea></div>--%>
    <div  class="row" style="margin-top:0px;width:550px;">
        <%--<div class="r_text" style="width:100px;padding-top:3px;">*主键列：</div>--%>
        <div  id="RColEditDiv" style="float:left;margin_left:10px;overflow-y:auto;height:250px;">
            <table id="RColEditTable" style="height:250px;table-layout:fixed; word-wrap:break-word;border-collapse:collapse;overflow:hidden">
                <thead>
                <tr>
                    <%--<th class="col-md-2" data-field="checkState" data-align="left" data-formatter="PKColQueryCheckFormatter" data-events="operateEvents">查询类型选择&nbsp;&nbsp;&nbsp;</th>--%>
                    <th class="col-md-2"  data-field="RColEditName" data-align="center" >列名&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</th>
                    <th class="col-md-6" data-field="RColEditValue" data-align="center" >列值&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</th>
                </tr>
                </thead>
            </table>
        </div>
        <%--<div id="RColEditDiv2" style="float:left;width:50%;">--%>
        <%--<div class="r_box">--%>
        <%--<input name='re_colName' id='re_colName' type='text' class='' style='width:350px;height:25px;' value='' >--%>
        <%--</div>--%>
        <%--</div>--%>
        <%--<div class="r_box" style='width:370px;'>--%>
        <%--&lt;%&ndash;<input name='re_colName' id='re_colName' type='text' class='' style='width:350px;height:25px;' value='' disabled="disabled">&ndash;%&gt;--%>
        <%--<span class="glyphicon glyphicon-exclamation-sign" title="注意：一旦创建列名不可更改！"></span>--%>
        <%--</div>--%>
        <%--<div class="r_text14" id="te_noTableName" style="padding-top:10px;width:45px;display:none;">必填！</div>--%>
    </div>
    <div class="row" style="margin-top:180px;height:20px;"><div class="r_text14" id="recordUpdateStatus" style="margin-left:10px;width:550px;text-align:center"></div></div>
    <div class="row" style="width:550px;margin-top:0px;">
        <div class="cy_text">
            <input id="btn_editrecord" name="" type="button" class="btn2" style="width:60px" value="更新" onclick='recordUpdate();'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
            <input id="btn_canceledit" name="" type="button" class="btn2" style="width:60px" value="取消" data-dismiss="modal">
        </div>
    </div>
</div>

<script type="text/javascript">
    //	var keytype = args_pktype;
    var recordToEdit = {};
    var recordKeys = [];
    var recordPK_data = [];
    var RColEditTable_data = [];

    var limit=5;
    var range_key_cursor_mark="*";
    $(function(){
//		$("#recordEditContent").attr("value", "");
        debugger
        recordUpdateWindow();
    });

    //todo 2018.12.25
    $('#PKColEditTable').bootstrapTable('destroy').bootstrapTable({
        data: [],
        classes: 'table',
        striped: true,
        formatNoMatches: function () {
            return '';
        },
    });
    function recordUpdateWindow(){
        debugger
//		 var queryUrl = "query_from=0&hash_key=" + encodeURIComponent(modal_hash_key);
//		 if (keytype == 1) {
//			 queryUrl += "&range_key=" + encodeURIComponent(modal_range_key);
//		 }
        $("#recordUpdateStatus").html("");
        for(var i=0; i <record_table_primary_keys.length;i++){
            recordPK_data[i]={"col_name":record_table_primary_keys[i],"col_value":modal_PKCols[i]};
        }
        var tableEditMap = {};
        tableEditMap["primary_key"] = recordPK_data;
        tableEditMap["limit"]=limit;
        tableEditMap["range_key_cursor_mark"]= range_key_cursor_mark;

        $.ajax({
            cache: false,
            type: "POST",
//	    		url: "/otscfgsvr/api/record/" + modal_table_name + "?" + queryUrl,
//                url: "/otscfgsvr/api/record/" + modal_table_name + "/query",
            url:"wls_1_recordInfo.json",
            dataType: "json",
            data: JSON.stringify(tableEditMap),
            timeout: 30000,
            success: function(results, msg){
                if (results["errcode"] == 0) {
                    var recordCol_num=0;
                    for (var i=0; i<results["records"].length; i++){
                        var recordColNum=0;
                        for(var j=0; j<record_table_primary_keys.length; j++){
                            if(modal_PKCols[j] == results["records"][i][record_table_primary_keys[j]]){
                                recordColNum++;
                            }
                            else{
                                break;
                            }
                        }
                        if(recordColNum==record_table_primary_keys.length){
                            recordToEdit = results["records"][i];
                            break;
                        }
                    }
                    for(var item in recordToEdit){
                        RColEditTable_data[recordCol_num] = {"RColEditName":item,"RColEditValue":recordToEdit[item]};
                        recordCol_num++;
                    }
//                        $('#RColEditTable').editable({
//                                type: 'text',
//                                url: '/post',
//                        });
//                        //ajax emulation
//                        $.mockjax({
//                            url: '/post',
//                            responseTime: 200
//                        });
                    $('#RColEditTable').bootstrapTable('destroy').bootstrapTable({
                        data: RColEditTable_data,
                        classes: 'table',
                        undefinedText: '',
                        striped: true,
                        editable:true,//开启编辑模式
                        clickToSelect: true,
                        cache : false,
                        columns: [
                            {
                                field:"RColEditName",
                                title:"列名",
                                align:"center",
                            },
                            {
                                field:"RColEditValue",
                                title:"列值",
                                align:"center",
//                                    editable: true,
                                editable:{
                                    type: 'text',
//                                        validate: function (v) {
//                                            if (isNaN(v)) return '必须是数字';
//                                            var status = parseInt(v);
//                                            if (status <= 0 || status>2) return '必须1或者2';
//
//                                        }
                                }
                            }
                        ],

                    });

                }
                else {
                    $("#recordUpdateStatus").append("获取记录失败！错误：" + results["errcode"]);
                }
            },
            error: function (msg){
                var errmsg = "获取记录失败！错误: " + getStructMsg(msg);
                $("#recordUpdateStatus").append(errmsg);
            }
        });

    }

    function recordUpdate(){
        debugger
        var recordUpdateMap = {};
        var recordUpdate_data={};
        var field_record;
        $("#recordUpdateStatus").html("");
        var RColEdit_table = $("#RColEditTable").bootstrapTable('getData');
        for(var i=0; i <RColEdit_table.length;i++){
            field_record = RColEdit_table[i]["RColEditName"];
            recordUpdate_data[field_record]= RColEdit_table[i]["RColEditValue"];
        }

        recordUpdateMap["records"] = [recordUpdate_data];
        $.ajax({
            type:"PUT",
            url:"/otscfgsvr/api/record/" + modal_table_name,
//                url:"wls_1_recordInfo.json",
            data:JSON.stringify(recordUpdateMap),
            dataType:"json",
            contentType: "application/json",
            timeout:30000,
            success:function(results){
                $("#recordUpdateStatus").append("更新记录").append(errorInfo(results["errcode"]));
                getRecordList();
            },
            error: function(msg){
                var errmsg = "更新记录失败！错误: " + getStructMsg(msg);
                $("#recordUpdateStatus").append(errmsg);
            }
        });
    }

</script>