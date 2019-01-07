<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<%@ page language="java" import="com.baosight.xinsight.ots.cfgsvr.bean.AuthBean" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="zh-CN">
<head>
	<meta charset="UTF-8"/>
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
	<meta name="viewport" content="width=device-width, initial-scale=1.0">
	<title>表详细页--OTS 配置中心</title>
	<link rel="shortcut icon" href="jsp/images/icon.ico"/>
	<link href="jsp/bootstrap-3.2.0-dist/css/bootstrap.min.css" rel="stylesheet" type="text/css">
	<link href="jsp/css/bootstrap-table.css" rel="stylesheet" type="text/css">
	<link href="jsp/css/style.css" rel="stylesheet" type="text/css">
	<link href="jsp/css/content-style.css" rel="stylesheet" type="text/css">
	<%--添加文件支持下拉框多选--%>
	<%--<link href="jsp/css/multiple-select.css" rel="stylesheet" type="text/css"/>--%>
	<%--<link href="jsp/css/bootstrap-table.min.css" rel="stylesheet" type="text/css"/>--%>
	<%--添加文件支持表格可编辑--%>
	<link href="jsp/css/bootstrap-editable.css" rel="stylesheet" type="text/css"/>

<%--<script type="text/javascript" src="jsp/js/jquery-1.9.1.min.js"></script>--%>
	<script type="text/javascript" src="jsp/js/jquery-1.8.2.min.js"></script>
	<script type="text/javascript" src="jsp/js/menu-anim.js"></script>
	<script type="text/javascript" src="jsp/bootstrap-3.2.0-dist/js/bootstrap.min.js"></script>
	<%--<script type="text/javascript" src="jsp/bootstrap-3.2.0-dist/js/bootstrap.js"></script>--%>//若加了，批量刪除无法下拉
	<script type="text/javascript" src="jsp/js/bootstrap-table.js"></script>
	<script type="text/javascript" src="jsp/My97DatePicker/WdatePicker.js"></script>
	<script type = "text/javascript" src="jsp/js/exceptionDic.js"></script>
	<script type="text/javascript" src="jsp/js/init-js.js"></script>
	<script type="text/javascript" src="jsp/js/common.js"></script>
	<%--添加文件支持下拉框多选--%>
	<%--<script type="text/javascript" src="jsp/js/multiple-select.js"></script>--%>
	<script type="text/javascript" src="jsp/js/bootstrap-table.min.js"></script>
	<%--添加文件支持表格可编辑--%>
	<script type="text/javascript" src="jsp/js/bootstrap-table-editable.js"></script>
	<script type="text/javascript" src="jsp/js/bootstrap-editable.js"></script>


	<script type="text/javascript">
        $(function(){
            showActivePage("table");
        });
	</script>
</head>

<body class="body_fix">
<div id="header">
	<jsp:include page="header.jsp" />
</div>
<div class="content" >
	<%
		String tenantId = "-1"; //means tenant id is empty
		String token = "";
		String tableId = "-1";  //means not need table id
		String tenant = "";
		String username = "";
		String userId = "-1";  //
		if(session.getAttribute("tenant")!=null)
		{
			token = session.getAttribute("token").toString();
			tenant = session.getAttribute("tenant").toString();
			username = session.getAttribute("username").toString();
			tenantId = session.getAttribute("tenantId").toString();
			userId = session.getAttribute("userId").toString();
			tableId = request.getParameter("id");
		}
	%>
	<!---content--->
	<div class="row" >
		<div class="row" style="height:40px;">
			<div class="pageicon">|</div>
			<div style="margin-left:5px;margin-top:5px;">
				<ul class="breadcrumb pagetitle" style="background-color:transparent;margin-bottom:0px;padding-top:0px;padding-left:5px;">
					<li><a href="table.jsp" >OTS-表的管理</a></li>
					<li><a href="#" id="tablename"></a></li>
				</ul>
			</div>
		</div>
		<div class="row" style="margin-top:0px;">
			<div id="menuTop" class="col-md-9" style="min-height:35px;width:100%">
				<div id="menuType" class="menuType">
					<div style="float:left" id="dataInfo" class="contentMenu-sel" onclick="clickData();">表数据</div>
					<div style="float:left" id="columnInfo" class="contentMenu-col" onclick="clickColumnInfo();">列信息</div>
					<div style="float:left" id="indexInfo" class="contentMenu" onclick="clickIndex();">索引</div>
				</div>
			</div>
		</div>
		<hr style="height: 1px; background: #aaa; margin-left:70px;margin-right:70px;margin-top:0px;margin-bottom:10px;" />
		<div id="pageGroup">
			<div  id="dataPage">
				<div  id="PKColQueryDiv" style="margin-left:70px;margin-right:70px;overflow-y:auto;height:200px;">
					<table id="PKColQueryTable" style="height:100px;table-layout:fixed; word-wrap:break-word;border-collapse:collapse;">
						<thead>
						<tr>
							<th class="col-md-3" class="PKColQueryConName" data-field="PKColQueryName" data-align="center" >主键列名&nbsp;&nbsp;<span class="glyphicon glyphicon-question-sign" title="注：查询条件按创建表时的主键列的顺序填写，即只有当前一个主键列的查询条件填写完成后，才会显示下一个主键列。当选择精确查询时，每个列值必须完整。"></span></th>
							<th class="col-md-6" data-field="PKColQueryCondition" data-align="center" data-formatter="PKColQueryConditionFormatter" data-events="operateEvents">查询条件&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</th>
						</tr>
						</thead>
					</table>
				</div>

				<div id="PKQueryButton" style="margin-left:900px;width:45%;">
					<input name="" type="button" class="btn2"  style=" width:50px;margin:auto" value="查询" onclick='recordQuery();'>
				</div>
				<div class="r_text14" id="PKQ_infoStatus" data-name="hr_status" style=" float: left;margin-top:10px;margin-left:20px;width:90%;text-align:center;" ></div>

				<div class="row" style="margin-left:70px;margin-right:70px;margin-top:10px;">
					<jsp:useBean id="test" class="com.baosight.xinsight.ots.cfgsvr.bean.AuthBean" />
					<jsp:setProperty name="test" property="token" value = "<%=token %>" />
					<jsp:setProperty name="test" property="tenant" value = "<%=tenant %>" />
					<jsp:setProperty name="test" property="username" value = "<%=username %>" />
					<jsp:setProperty name="test" property="tenantId" value = "<%=tenantId %>" />
					<jsp:setProperty name="test" property="userId" value = "<%=userId %>" />
					<jsp:setProperty name="test" property="serviceName" value = "OTS" />
					<jsp:setProperty name="test" property="tableId" value = "<%=tableId %>" />

					<div class="table-controls">
						<div class="pull-right" style="" >
							<%--<c:if test="${test.hasRecordWritePerm}">--%>
							<input name="" type="button" class="btn" style="width:80px;" value="添加记录" onclick='clickAddRecord();' data-toggle="modal"  data-target="#recordCreate">
							<input name="" type="button" class="btn" style="width:80px;" value="清空记录" onclick='clickClearRecord();'>
							<span class="dropdown">
		      					  <button type="button" class="btn dropdown-toggle" data-toggle="dropdown">批量删除<span class="caret"></span></button>
			      				  <ul class="dropdown-menu" role="menu">
									  <li><a href="#" class="recordMultiDelete" onclick='clickDeleteMultiRecords();' data-method="remove">删除已选行</a></li>
									  <li><a href="#" onclick='clickConditionDelete();' data-toggle="modal"  data-target="#conditionDelete">按条件删除</a></li>
								  </ul>
							   </span>
							<%--</c:if>--%>
							<input name="" type="button" class="btn" style="width:100px;" value="编辑显示列" onclick='clickEditDisplayColumns();' data-toggle="modal"  data-target="#propertyEdit">
						</div>
					</div>

					<div id="table">
						<table id="tableDetailRecord" style="table-layout:fixed; word-wrap:break-word;border-collapse:collapse">
							<thead>
							<tr>
								<th data-field="state" data-checkbox="true"></th>
								<th data-field="" data-align="center"></th>
								<th data-field="" data-align="center"></th>
								<th data-field="" data-align="center"></th>
								<th data-field="" data-align="center"></th>
								<th data-field="" data-align="center"></th>
								<%--<th data-field="" data-align="center"></th>--%>
								<th data-class="col-operate-record" data-field="operate" data-align="center" data-formatter="recordOperateFormatter" data-events="operateEvents">操作</th>
							</tr>
							</thead>
						</table>
					</div>
				</div>
			</div>

			<div class="row" id="columnPage" style="margin-left:70px;margin-right:70px;display: none" >
				<div>
					<table id="tableColumn" style="table-layout:fixed; word-wrap:break-word;border-collapse:collapse">
						<thead>
						<tr>
							<%--<th class="" data-field="state" data-checkbox="true"></th>--%>
							<th data-field="col_name" data-align="center">列名</th>
							<th data-field="col_type" data-align="center">类型</th>
							<th data-field="ifpk" data-align="center">是否主键</th>
							<th data-field="pk_seq" data-align="center">主键排序</th>
						</tr>
						</thead>
					</table>
				</div>
			</div>

			<div class="row" id="indexPage" style="margin-left:70px;display: none" >
				<div class="row" style="margin-top:5px;">
					<span>索引名：&nbsp;&nbsp;&nbsp;<input name="" id="indexQueryName" type="text" value="">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</span>
					<span><input name="" type="button" class="btn2" style="width:50px" value="查询" onclick='indexQuery();'></span>
				</div>
				<div class="row" style="margin-right:70px;margin-top:5px;">
					<div style="text-align:right;color:#428BCA;">注：索引操作需要处理时间稍长，操作间隔建议不要过频！</div>
					<%--<c:if test="${test.hasRecordWritePerm}">--%>
					<div class="table-controls">
						<div class="pull-right">
							<input name="" type="button" class="btn" style="width:80px" value="新建索引" onclick='clickCreateIndex();' data-toggle="modal"  data-target="#indexCreate">
							<input name="" type="button" class="btn" style="width:80px" value="批量删除" onclick='clickDeleteMultiIndex();' data-method="remove">
						</div>
					</div>
					<%--</c:if>--%>
					<table id="tableDetailIndex" style="table-layout:fixed; word-wrap:break-word;border-collapse:collapse">
						<thead>
						<tr>
							<th data-field="state" data-checkbox="true"></th>
							<th data-field="index_name" data-align="center" data-formatter="indexNameFormatter">索引名</th>
							<th data-field="index_type" data-align="center" >类型</th>
							<%--<th data-field="index_type" data-align="center" data-formatter="indexTypeFormatter">类型</th>--%>
							<%--<th data-field="pattern" data-align="center" data-formatter="indexPatternFormatter">模式</th>--%>
							<th data-field="create_time" data-align="center">创建时间</th>
							<th data-field="modify_time" data-align="center">最后修改时间</th>
							<th class="col-operate-index" data-field="operate" data-align="center" data-formatter="indexOperateFormatter" data-events="operateEvents">操作</th>
						</tr>
						</thead>
					</table>
				</div>
			</div>

		</div>
	</div>
</div>
<!---dialog: record--->
<div class="modal text-center" id="recordCreate" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-dialog" style="display: inline-block; width: auto;">
		<div class="modal-content"></div>
	</div>
</div>
<div class="modal text-center" id="recordEdit" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-dialog" style="display: inline-block; width: auto;">
		<div class="modal-content"></div>
	</div>
</div>
<%--<div class="modal text-center" id="strategyDelete" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">--%>
<%--<div class="modal-dialog" style="display: inline-block; width: auto;">--%>
<%--<div class="modal-content"></div>--%>
<%--</div>--%>
<%--</div>--%>
<div class="modal text-center" id="conditionDelete" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-dialog" style="display: inline-block; width: auto;">
		<div class="modal-content"></div>
	</div>
</div>
<!---dialog: property--->
<div class="modal text-center" id="propertyEdit" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-dialog" style="display: inline-block; width: auto;">
		<div class="modal-content"></div>
	</div>
</div>
<!---dialog: index--->
<div class="modal text-center" id="indexCreate" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-dialog" style="display: inline-block; width: auto;">
		<div class="modal-content"></div>
	</div>
</div>
<div class="modal text-center" id="indexEdit" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
	<div class="modal-dialog" style="display: inline-block; width: auto;">
		<div class="modal-content"></div>
	</div>
</div>
<div>
	<jsp:include page="footer.jsp" />
</div>
<div>
	<jsp:include page="errorTips.jsp" />
</div>
</body>

<script type="text/javascript">
    var tableDetailRecordNum = 100;		    //每次查询默认返回记录数
    var table_detail_row_index = 0;
    var total_record_num = 0;				//已经返回记录数
    var table_detail_records = [];
    var table_detail_indexes = [];
    var args_tablename = getUrlParam("tablename");
    //    	var args_pktype = getUrlParam("pktype");
    //    	var args_range = getUrlParam("range");
    var args_id = getUrlParam("id");
    var display_name = getUrlParam("display");
    var table_detail_show_columns_num = 6;
    var table_detail_show_columns = [];
    var table_detail_query_columns = [];
    var table_detail_column_property = {"field":"", "title":"", "align":"center"};
    var table_detail_record_query_condition={};
    var modal_table_name;
    var modal_index_name;
    var modal_index_info = {};
    //    var modal_hash_key;
    //    var modal_range_key;
    var modal_PKCols=[];
    var timeoutId;
    var record_table_primary_keys = [];
    var table_PK_data=[];
    var record_table_columns = [];
    var editRowCheck=0;
    var record_limit=100;//查询记录时，limit默认值为100，最大值10000

    function recordAlertMsg(msg){
        errorAlertMsg(msg);
    }


    //按查询条件删除记录
    //todo
    function recordConditionConfirmAlert(msg, operate, param) {
        confirmAlertMsg(msg, 1, operate, param);

    }

    function recordConfirmAlert(msg, operate, param) {
        confirmAlertMsg(msg, 1, operate, param);
    }

    function indexConfirmAlert(msg, operate, param) {
        confirmAlertMsg(msg, 2, operate, param);
    }



    //主键查询条件的选择
    function PKColQueryConditionFormatter(value, row, index) {
        debugger
		return [
			'<div name="" id="PKQConditionDiv' + index + '">',
				'<div name="" id="PKStartDiv' + index + '" style="float:left;margin-left: 50px">',
					'<input class="" name="" id="tableDetailRangeStartKey' + index + '" type="text"  style="width:120px;text-align:left;margin-left:0px;" value="" placeholder="起始键 " >',
				 '</div>',
				'<div name="" id="PKRangeDiv' + index + '" style="float:left">' ,
					'<span  class="r_text1" name="" id="tableDetailRangeSplit' + index + '"></span>----',
					'<input class="" name="" id="tableDetailRangeEndKey' + index + '" type="text"  style="width:120px;margin-left: 0px;" value="" placeholder="终止键 " >',
				'</div>',
				'<div name="" id="PKCheckDiv' + index + '" style="margin-left:20px;float:left;margin-top:5px">' ,
					'<input class="CheckBoxStateAcc" type="checkbox" name="" id="CheckBoxStateAcc' + index + '" style="" onclick="">精确查询',
				'</div>',
			'</div>',
		].join('');
    }



    $(function(){
        debugger
        //todo 2018.12.19增加复选键来简化页面
        $('#PKColQueryTable').bootstrapTable('destroy').bootstrapTable({
            data: [],
//            data: wls_PKQ_datas,
            classes: 'table',
            striped: true,
            formatNoMatches: function () {
                return '';
            },
        });
		if (args_tablename == null ) {
			recordAlertMsg("非法的URL，请跳转至“表的管理”页面，点击表名，进入“表详细页”页面。");
			//window.location = "table.jsp";
		}
        debugger
        $("#tablename").text(getStructName(args_tablename) + "-表数据");
        document.getElementById("tablename").title = args_tablename;
        if (display_name == "index") {
            clickIndex();
        }

        for (var i=0; i<table_detail_show_columns_num; i++) {
            table_detail_show_columns[i] = table_detail_column_property;
        }
        table_detail_show_columns[0] = {"field":"state", "checkbox":true};

        loadInitRecord_PK();

        $('#tableDetailRecord').bootstrapTable({
            data: [],
            classes: 'table',
            striped: true,
            formatNoMatches: function () {
                return '';
            },
            columns: table_detail_show_columns
        });
        recordInit();
        $('#tableDetailRecord').on('page-change.bs.table', function (e, size, number){
            var $table = $('#tableDetailRecord').bootstrapTable();
            var totalPages = $table.bootstrapTable('getOptions').totalPages;
            if(totalPages == size){
                total_record_num = $table.bootstrapTable('getOptions').totalRows;
                tableDetailRecordUpdate();
            }
        });
    });

    function loadInitRecord_PK(){
        var url = '/otscfgsvr/api/table/';
        url += args_tablename;


        $.ajax({
            type: "GET",
            url: url,
//            url:"wls_colInfo_tableInfo.json" ,
            dataType: "json",
            timeout: 10000,
            async:false,
            success: function(results, msg){
                var errorcode = results["errcode"];
                if (errorcode == 0) {
                    record_table_columns = results["table_columns"];
                    record_table_primary_keys = results["primary_key"];
                }
                else {
                    var errmsg = "获取表 " + args_tablename + " 详细信息失败。";
                    recordAlertMsg(errmsg);
                }
            },
            complete: function() {
                var column_name;
                for (var i = 0; i < record_table_primary_keys.length; i++) {
                    column_name = record_table_primary_keys[i];
                    table_PK_data[i] = {"PKColQueryName": column_name};//动态加载主键列
                }
                $('#PKColQueryTable').bootstrapTable('destroy').bootstrapTable({
                    data: table_PK_data,
                    classes: 'table',
                    undefinedText: '',
                    striped: true,
                });
                for(var i=1;i<record_table_primary_keys.length;i++){
                    $('#PKColQueryTable').bootstrapTable('hideRow', {index:i});
                }
            },
            error: function(msg){
//                record_table_return_column = [];
                if(msg["status"] != 404) {
                    var errmsg = "获取表" + getStructName(args_tablename) + " 显示列信息错误: " + getStructMsg(msg);
                    tableAlertMsg(errmsg);
                }
            }
        });

    }

    function recordInit(){
        var col_num=0;
        table_detail_record_query_condition["return_columns"]=record_table_primary_keys;
        $.ajax({
            cache: false,
            type: "POST",
            url: "/otscfgsvr/api/table/" + args_tablename + "/query",
//            url:"wls_1_recordInfo.json",
            data: JSON.stringify({"return_columns": record_table_primary_keys}),
            dataType: "json",
            timeout:30000,
//            async:false,
            success:function(results, msg) {
                if (results["errcode"] == 0) {
                    var obj_record = results["records"][0];
                    for(var field_ in obj_record){
                        table_detail_query_columns[col_num]=field_;
                        col_num++;
                    }
                    getTableDetailShowColumns();
                    getRecordList();
                }
                else {
                    recordAlertMsg("获取表 " + args_tablename + " 显示列信息失败！错误: " + results["errcode"]);
                }
            },
            error: function(msg){
                if(msg["status"] != 404) {
                    var errmsg = "获取表" + getStructName(args_tablename) + " 显示列信息错误: " + getStructMsg(msg);
                    recordAlertMsg(errmsg);
                }
                getRecordList();
            }
        });
    }

    function clickData()
    {
        $("#tablename").text(getStructName(args_tablename) + "-数据");
        document.getElementById("tablename").title = args_tablename;
        $("#index_name").css("display", "none");
        menuClick(0,3);

        $('#tableDetailRecord').bootstrapTable({
            data: [],
            classes: 'table',
            striped: true,
            formatNoMatches: function () {
                return '';
            },
        });

        $("#tableDetailHashKey").attr("value", "");
        $("#tableDetailRangePrefixKey").attr("value", "");
        $("#tableDetailRangeStartKey").attr("value", "");
        $("#tableDetailRangeEndKey").attr("value", "");

        recordQuery();
    }

    function clickColumnInfo()
    {
//        debugger
        $("#tablename").text(getStructName(args_tablename) + "-列信息");
        document.getElementById("tablename").title = args_tablename;
        $("#index_name").css("display", "none");
//            $("#index_name").css("display", "inline-block");
        menuClick(1,3);

        $('#tableColumn').bootstrapTable({
            data: [],
            classes: 'table',
            striped: true,
            formatNoMatches: function () {
                return '';
            },
        });
//        $('#tableColumn').on('pre-body.bs.table', function (data, rows) {
//            clearTimeout(timeoutId);
//            indexGetStatus(rows);
//        });
//        indexInit();

//-------------------获取列信息-----------------------------------------
        var url = '/otscfgsvr/api/table/';
        url += args_tablename;
        var table_column_records = [];
        var table_primary_keys = [];
        var if_PK=0;

        $.ajax({
            type: "GET",
            url: url,
//            url:"wls_colInfo_tableInfo.json" ,
            dataType: "json",
            timeout: 10000,
            success: function(results, msg){
                var errorcode = results["errcode"];
                if (errorcode == 0) {
                    table_column_records = results["table_columns"];
                    table_primary_keys = results["primary_key"];
                }
                else {
                    var errmsg = "获取表 " + args_tablename + " 详细信息失败。";
                    recordAlertMsg(errmsg);
                }
            },
            complete: function() {
                var table_column_data = new Array();
                var column_name;
                for (var i = 0; i < table_column_records.length; i++) {
                    column_name = table_column_records[i]["col_name"];
                    if ($.inArray(column_name, table_primary_keys) > -1) {
                        if_PK = 1;
                    } else {
                        if_PK = 0;
                    }
                    table_column_data[i] = {
                        "col_name": column_name,
                        "col_type": table_column_records[i]["col_type"],
                        "ifpk": if_PK ? "是" : "否",
                        "pk_seq": if_PK ? (table_primary_keys.lastIndexOf(column_name) + 1) : "",
                    };
                }
                $('#tableColumn').bootstrapTable('destroy').bootstrapTable({
                    data: table_column_data,
                    classes: 'table',
                    undefinedText: '',
                    striped: true,
                });
            },
            error: function(msg){
                table_column_records = [];
                if(msg["status"] != 404) {
                    var errmsg = "获取表" + getStructName(args_tablename) + " 显示列信息错误: " + getStructMsg(msg);
                    tableAlertMsg(errmsg);
                }
            }
        });
    }


    function clickIndex()
    {
        $("#tablename").text(getStructName(args_tablename) + "-索引");
        document.getElementById("tablename").title = args_tablename;
        $("#index_name").css("display", "inline-block");
        menuClick(2,3);
        $('#tableDetailIndex').bootstrapTable({
            data: [],
            classes: 'table',
            striped: true,
            formatNoMatches: function () {
                return '';
            },
        });
        $('#tableDetailIndex').on('pre-body.bs.table', function (data, rows) {
            clearTimeout(timeoutId);
//            indexGetStatus(rows);
        });
        indexInit();
    }

    function recordOperateFormatter(value, row, index) {
        return [
            <%--'<c:if test="${test.hasRecordWritePerm}">',--%>
            '<input name="" type="button" class="recordEdit btn3" style="width:50px;" value="编辑" data-toggle="modal"  data-target="#recordEdit">',
            '&nbsp;&nbsp;&nbsp;&nbsp;',

            '<input name="" type="button" class="recordDelete btn3" style="width:50px;" value="删除" data-method="remove">',
            <%--'</c:if>'--%>
        ].join('');
    }

    window.operateEvents = {
        'click .recordEdit': function (e, value, row, index) {
            clickEditRecord(row);
        },
        'click .recordDelete': function (e, value, row, index) {
            clickDeleteRecord(row);
        },
        'click .indexRebuild': function (e, value, row, index) {
            clickRebuildIndex(row, index);
        },
        'click .indexEdit': function (e, value, row, index) {
            clickIndexEdit(row, index);
        },
        'click .indexClear': function (e, value, row, index) {
            clickClearIndex(row, index);
        },
        'click .indexDelete': function (e, value, row, index) {
            clickDeleteIndex(row);
        },
        'click .CheckBoxStateAcc': function (e, value, row, index) {
            clickCheckStateAcc(index);
        },
    };


    function clickCheckStateAcc(editIndex) {
        //todo 当点击当前复选框时，当前的“----”和“终止键”隐藏，"起始键"变为精确查询，并出现下一行。
        debugger
        var text_start={};
        var text_end;
        $("#PKQ_infoStatus").html("");
        var isChecked_now = $('#CheckBoxStateAcc' + editIndex).prop('checked');//判断当前复选框是否被选中
        if(isChecked_now){
            for(var i = 0;i < editIndex;i++) {
                var isChecked_before = $('#CheckBoxStateAcc' + i).prop('checked');
                if(!isChecked_before){
                    $("#PKQ_infoStatus").html("请保证之前的主键列都是精确查询。");
                    $('#CheckBoxStateAcc' + editIndex).attr("checked", false);//若之前主键列没有被勾选，则本主键列无法勾选。
                    return;
                }
                else{
                    text_start[i] = $('#tableDetailRangeStartKey'+i).val();
                    if(text_start[i]==""){
                        $("#PKQ_infoStatus").html("请填写上一个主键列“精确查询”的条件。");
                        $('#CheckBoxStateAcc' + editIndex).attr("checked", false);//若之前主键列没有被勾选，则本主键列无法勾选。
                        return;
					}
				}
            }
            text_start[editIndex] = $('#tableDetailRangeStartKey'+editIndex).val();
            editRowCheck=editIndex+1;
			$('#PKColQueryTable').bootstrapTable('showRow', {index:(editRowCheck)});
			for(var i=0;i<=editIndex;i++){
                $('#PKRangeDiv'+i).hide();
                $('#CheckBoxStateAcc' + i).attr("checked", true);
                $('#tableDetailRangeStartKey'+i).attr("value",text_start[i]);
			}
        }else{
            if(editIndex ==  (record_table_primary_keys.length-1)){
                $('#PKRangeDiv'+editIndex).show();
                $('#CheckBoxStateAcc' + editIndex).attr("checked", false);
            }else {
                var isChecked_after = $('#CheckBoxStateAcc' + (editIndex + 1)).prop('checked');
                if ((!isChecked_after) && ($('#tableDetailRangeStartKey' + (editIndex + 1)).val() == "") && ($('#tableDetailRangeEndKey' + (editIndex + 1)).val() == "")) {
                    for (var i = 0; i <= editIndex; i++) {
                        text_start[i] = $('#tableDetailRangeStartKey' + i).val();
                    }
					$('#PKColQueryTable').bootstrapTable('hideRow', {index: (editIndex + 1)});
					for (var i = 0; i < editIndex; i++) {
						$('#PKRangeDiv' + i).hide();
						$('#CheckBoxStateAcc' + i).attr("checked", true);
						$('#tableDetailRangeStartKey' + i).attr("value", text_start[i]);
					}
                    $('#tableDetailRangeStartKey' + editIndex).attr("value", text_start[editIndex]);
            	}else{
                    $("#PKQ_infoStatus").html("若想以此主键列为范围查询，则请确保此主键列为查询的最后一个主键列。");
                    $('#CheckBoxStateAcc' + editIndex).attr("checked", true);
                    return;
				}
            }
        }
    }

    function getRecordList(){
        debugger
        var queryUrl ="/otscfgsvr/api/table/" + args_tablename + "/query";
//        var queryUrl = "wls_1_recordInfo.json";

        $.ajax({
            cache: false,
            type: "POST",
            url: queryUrl,
            data:JSON.stringify(table_detail_record_query_condition),
            dataType: "json",
            timeout:30000,
            success:function(results, msg){
                if (results["errcode"] == 0) {
                    table_detail_records = results["records"];
                    for (var i=0; i<table_detail_records.length; i++){
                        $.each(table_detail_records[i],function(j){
                            table_detail_records[i][j] = htmlEscape(table_detail_records[i][j]);
                        });
                    }
                }
                else {
                    recordAlertMsg("记录查询失败！错误: " + results["errcode"]);
                }
            },
            complete: function() {
                var $table = $('#tableDetailRecord').bootstrapTable();
                var pagesize = $table.bootstrapTable('getOptions').pageSize;
                $('#tableDetailRecord').bootstrapTable('destroy').bootstrapTable({
                    data: table_detail_records,
                    classes: 'table',
                    undefinedText: '',
                    striped: true,
                    pagination: true,
                    pageSize: pagesize,
                    pageList: [5, 10, 15],
                    formatRecordsPerPage: function (pageNumber) {
                        return sprintf('每页显示 %s 条记录', pageNumber);
                    },
                    formatShowingRows: function (pageFrom, pageTo, totalRows) {
                        return sprintf('第  %s 到 %s 条记录', pageFrom, pageTo);
                    },
                    columns: table_detail_show_columns
                });
            },
            error: function(msg){
                recordAlertMsg("记录查询失败！错误: " + getStructMsg(msg));
            }
        });
    }

    function tableDetailRecordUpdate(){
//        table_detail_record_query_condition["offset"] = total_record_num;
//        var queryUrl = "/otscfgsvr/api/record/" + args_tablename + "?";
//        queryUrl += "query_from=0&limit=" + tableDetailRecordNum + "&offset=" + table_detail_record_query_condition["offset"];
//        if (table_detail_record_query_condition["hash_key"]) {
//            queryUrl += "&hash_key=" + table_detail_record_query_condition["hash_key"];
//        }

//        if (table_detail_query_columns.length != 0 && table_detail_query_columns[0] != "") {
//            queryUrl += "&columns=" + table_detail_query_columns.join(",");
//        }
        //todo 添加查询条件
        var PKCondition=[];
        var text_PKCon={};
        var num_PKCon=0;
        for(var i=0;i<record_table_primary_keys.length;i++){
            text_PKCon[i]=$('#tableDetailRangeStartKey' + i).val();
            if(text_PKCon[i]=="undefined" ){
                num_PKCon=i;
                break;
            }
            if(($('#CheckBoxStateAcc' + i).prop('checked')) && $('#tableDetailRangeStartKey' + i).val()==""){
                num_PKCon=i;
                break;
            }
            if((!$('#CheckBoxStateAcc' + i).prop('checked')) && ($('#tableDetailRangeStartKey' + i).val()=="") && ($('#tableDetailRangeEndKey' + i).val()=="")){
                num_PKCon=i;
                break;
            }
        }
        for(var j=0;j<num_PKCon;j++){
            if(j==num_PKCon-1){
                if(!$('#CheckBoxStateAcc' + num_PKCon).prop('checked')){
                    PKCondition[j]={"col_name":record_table_primary_keys[j],"col_start":$('#tableDetailRangeStartKey' + j).val(),"col_end":$('#tableDetailRangeEndKey' + j).val()};
                }
                else{
                    PKCondition[j]={"col_name":record_table_primary_keys[j],"col_value":$('#tableDetailRangeStartKey' + j).val()};
                }
            }else {
                PKCondition[j]={"col_name":record_table_primary_keys[j],"col_value":$('#tableDetailRangeStartKey' + j).val()};
            }
        }
        table_detail_record_query_condition["primary_key"]=PKCondition;
        table_detail_record_query_condition["limit"]=record_limit;
        table_detail_record_query_condition["offset"] = total_record_num;

        var queryUrl = "/otscfgsvr/api/record/" + args_tablename + "/query";
        $.ajax({
            cache: false,
            type: "POST",
            url: queryUrl,
            dataType: "json",
            timeout:30000,
            data:JSON.stringify(table_detail_record_query_condition),
            success:function(results, msg){
                if (results["errcode"] == 0) {
                    table_detail_records = results["records"];
                    for (var i=0; i<table_detail_records.length; i++){
                        $.each(table_detail_records[i],function(j){
                            table_detail_records[i][j] = htmlEscape(table_detail_records[i][j]);
                        });
                    }

                    $('#tableDetailRecord').bootstrapTable('append', table_detail_records);
                }
                else {
                    recordAlertMsg("记录查询失败！错误: " + results["errcode"]);
                }
            },
            error: function(msg){
                recordAlertMsg("记录查询失败！错误: " + getStructMsg(msg));
            }
        });

    }

    function recordQuery(){
// todo 校验起始键tableDetailRangeStartKey是否都有值，最后一个如果是范围查询，则起始键或终止键是否其中一个有值
        var text_null=$('#tableDetailRangeStartKey0').val();
        if(text_null==""){
            $("#PKQ_infoStatus").html("请填写完整的查询条件。");
            return;
		}
//    //todo 添加查询条件
		var PKCondition=[];
        var text_PKCon={};
        var num_PKCon=0;
        for(var i=0;i<record_table_primary_keys.length;i++){
            text_PKCon[i]=$('#tableDetailRangeStartKey' + i).val();
            if(text_PKCon[i]=="undefined" ){
                num_PKCon=i;
                break;
			}
			if(($('#CheckBoxStateAcc' + i).prop('checked')) && $('#tableDetailRangeStartKey' + i).val()==""){
                num_PKCon=i;
                break;
			}
			if((!$('#CheckBoxStateAcc' + i).prop('checked')) && ($('#tableDetailRangeStartKey' + i).val()=="") && ($('#tableDetailRangeEndKey' + i).val()=="")){
                num_PKCon=i;
                break;
            }
        }
		for(var j=0;j<num_PKCon;j++){
//            text_PKCon[j]=$('#tableDetailRangeStartKey' + j).val();
			if(j==num_PKCon-1){
			    if(!$('#CheckBoxStateAcc' + num_PKCon).prop('checked')){
                    PKCondition[j]={"col_name":record_table_primary_keys[j],"col_start":$('#tableDetailRangeStartKey' + j).val(),"col_end":$('#tableDetailRangeEndKey' + j).val()};
				}
				else{
                    PKCondition[j]={"col_name":record_table_primary_keys[j],"col_value":$('#tableDetailRangeStartKey' + j).val()};
				}
			}else {
                PKCondition[j]={"col_name":record_table_primary_keys[j],"col_value":$('#tableDetailRangeStartKey' + j).val()};
			}
		}
    	table_detail_record_query_condition["primary_key"]=PKCondition;
        table_detail_record_query_condition["limit"]=record_limit;
        getRecordList();
    }

    function clickDeleteRecord(editRow) {
        debugger
        var confirmText = "确定删除本条记录吗";
        confirmText += " ?";
        recordConfirmAlert(confirmText, 0, editRow);
    }

    function recordDelete(editRow) {
        debugger
		var PKDelete=[];
        for(var i=0;i<record_table_primary_keys.length;i++){
            PKDelete[i]={"col_name":record_table_primary_keys[i],"col_value":editRow[record_table_primary_keys[i]]};
		}
        table_detail_record_query_condition["primary_key"]=PKDelete;
        $.ajax({
            type:"DELETE",
            url:"/otscfgsvr/api/record/" + args_tablename,
            timeout:30000,
            data:JSON.stringify(table_detail_record_query_condition),
            success:function(results, msg){
                if(results["errcode"] == 0)
                    getRecordList();
                recordAlertMsg("删除记录" + errorInfo(results["errcode"]));
                return;
            },
            error: function(msg){
                var errmsg = "删除记录失败！错误: " + getStructMsg(msg);
                recordAlertMsg(errmsg);
            }
        });
    }

    function clickDeleteMultiRecords() {
        var $table = $('#tableDetailRecord').bootstrapTable();
        var selects = $table.bootstrapTable('getSelections');
        if (selects.length == 0){
            recordAlertMsg("请选中至少一条记录。");
            return;
        }
        var msg = '确定删除选定的记录？';
        recordConfirmAlert(msg, 1, selects);
    }

    function recordMultiDelete(selects){
        $.map(selects, function (row) {
            $.ajax({
                async: false,
                type:"DELETE",
                url:"/otscfgsvr/api/record/" + args_tablename,
                timeout:30000,
                data:JSON.stringify(table_detail_record_query_condition),
                success:function(results, msg){
                    recordAlertMsg("删除记录" + errorInfo(results["errcode"]));
                    if(results["errcode"] == 0){
                        getRecordList();
                    }
                    return;
                },
                error: function(msg){
                    var errmsg = "删除记录失败！错误: " + getStructMsg(msg);
                    recordAlertMsg(errmsg);
                    return false;
                }
            });
        });

    }

    function clickClearRecord() {
        var msg = '确定清空表内所有记录？';
        recordConfirmAlert(msg, 2, null);
    }

    function recordClear() {
        $.ajax({
            type:"DELETE",
            url:"/otscfgsvr/api/record/" + args_tablename + "/truncate",
            timeout:30000,
            success:function(results, msg){
                if (results.errcode != 0) {
                    recordAlertMsg("清空记录" + errorInfo(results["errcode"]));
                    return;
                }
                $table = $('#tableDetailRecord').bootstrapTable('destroy').bootstrapTable({
                    data: [],
                    classes: 'table',
                    striped: true,
                    formatNoMatches: function () {
                        return '';
                    },
                    columns: table_detail_show_columns
                });
                recordAlertMsg("清空记录成功！");
            },
            error: function(msg){
                var errmsg = "清空记录失败！错误: " + getStructMsg(msg);
                recordAlertMsg(errmsg);
            }
        });
    }

    function indexInit(){
        $("#indexQueryName").attr("value", "");
        $.ajax({
            cache: false,
            type: "GET",
            url: "/otscfgsvr/api/index/" + args_tablename + "/_all_indexes_info",
            dataType: "json",
            timeout: 30000,
            success: function(results, msg){
                if (results["errcode"] == 0) {
                    var index_info_list = results["index_info_list"];
                    table_detail_indexes = index_info_list;
                }
                else {
                    recordAlertMsg("查询所有索引信息" + errorInfo(results["errcode"]));
                }
            },
            complete: function()
            {
                var $table = $('#tableDetailIndex').bootstrapTable();
                var pagesize = $table.bootstrapTable('getOptions').pageSize;
                $('#tableDetailIndex').bootstrapTable('destroy').bootstrapTable({
                    data: table_detail_indexes,
                    classes: 'table',
                    undefinedText: '',
                    striped: true,
                    pagination: true,
                    pageSize: pagesize,
                    pageList: [5, 10, 15],
                    formatRecordsPerPage: function (pageNumber) {
                        return sprintf('每页显示 %s 条记录', pageNumber);
                    },
                    formatShowingRows: function (pageFrom, pageTo, totalRows) {
                        return sprintf('第  %s 到 %s 条记录', pageFrom, pageTo);
                    },
                });
            },
            error: function(msg){
                var errmsg = "查询所有索引信息失败！错误: " + getStructMsg(msg);
                recordAlertMsg(errmsg);
            }
        });
    }

    function indexNameFormatter(value, row) {
//        return '<a style="cursor:pointer;" onclick=\'goIndexView("' + value + '","' + row["type"] + '","'  + row["table_id"] + '");\'>' + value + '</a>';
        return '<a style="cursor:pointer;" onclick=\'goIndexView("' + value + '","' + row["index_type"] + '","'  + row["table_id"] + '");\'>' + value + '</a>';
    }

//    function indexTypeFormatter(value) {
//        if (value == 1) {
//            return "Hbase";
//        }
//        else {
//            return "ElasticSearch";
//        }
//    }

    //		function indexPatternFormatter(value) {
    //			switch(value) {
    //				case 0:
    //					return "离线";
    //				case 1:
    //					return "实时";
    //				default:
    //					return "-";
    //			}
    //		}

    function indexOperateFormatter(value, row, index) {
        return [
            '<div class="progress progress-striped" id="indexProgress' + index + '" style="margin-bottom:5px;"><div class="progress-bar" id="indexProgressBar' + index + '" role="progressbar" style="width:' + row["progress"] + '"></div></div>',
            '<div id="indexButton' + index + '" style="display:block">',
//            '<div id="indexState' + index + '" class="r_text10" style="display:none">编译失败！</div>',
            <%--'<c:if test="${test.hasRecordWritePerm}">',--%>
            '<input name="" type="button" class="indexClear btn3" style="width:50px;" value="清空" data-method="updateRow">',
            '&nbsp;&nbsp;&nbsp;&nbsp;',
            '<input name="" type="button" class="indexRebuild btn3" style="width:50px;" value="重建" data-method="updateRow">',
            '&nbsp;&nbsp;&nbsp;&nbsp;',
            '<input name="" type="button" class="indexEdit btn3" style="width:50px;" value="编辑" data-toggle="modal"  data-target="#indexEdit">',
            '&nbsp;&nbsp;&nbsp;&nbsp;',
            '<input name="" type="button" class="indexDelete btn3" style="width:50px;" value="删除"></div>',
            <%--'</c:if>'--%>
        ].join('') ;
    }

//    function indexGetStatus(editRows) {
//        var progressId = "";
//        var progressBarId = "";
//        var progress = "";
//        for (var i=0; i<editRows.length; i++){
//            progressId = "indexProgress" + i;
//            progressBarId = "indexProgressBar" + i;
//            progress = editRows[i]["progress"];
//            if (progress == null || (parseFloat(progress, 10) >= 0 && parseFloat(progress, 10) < 100.0)) {
//                var query_condition = "index_type=" + editRows[i]["type"] + "&table_id=" + editRows[i]["table_id"] + "&index_id=" + editRows[i]["index_id"];
//                $.ajax({
//                    cache: false,
//                    async: false,
//                    type: "GET",
//                    url: "/otscfgsvr/api/index/status/" + args_tablename + "/" + editRows[i]["index_name"] + "?" + query_condition,
//                    dataType: "json",
//                    timeout: 20000,
//                    success: function(results, msg){
//                        editRows[i]["progress"] = results["progress"];
//                        progress = editRows[i]["progress"];
//                        if (document.getElementById(progressId)) {
//                            if (parseFloat(progress, 10) >= 0 && parseFloat(progress, 10) < 100.0) {
//                                document.getElementById("indexButton" + i).style.display = "none";
//                                document.getElementById(progressId).style.display = "block";
//                                document.getElementById(progressBarId).style.width = editRows[i]["progress"] + "%";
//                            }
//                            else {
//                                document.getElementById(progressId).style.display = "none";
//                                document.getElementById("indexButton" + i).style.display = "block";
//                            }
//                        }
//                    },
//                    error: function(msg){
//                        editRows[i]["progress"] = -1;
//                        if (document.getElementById(progressId)) {
//                            document.getElementById(progressId).style.display = "none";
//                        }
//                        if (document.getElementById("indexState" + i)) {
//                            document.getElementById("indexButton" + i).style.display = "block";
//                            document.getElementById("indexState" + i).style.display = "block";
//                        }
//                    }
//                });
//            }
//        }
//        timeoutId = setTimeout(function(){
//            var flag = 0;
//            for (var i=0; i<editRows.length; i++){
//                progress = editRows[i]["progress"];
//                progressId = "indexProgress" + i;
//                if (parseFloat(progress, 10) >= 0 && parseFloat(progress, 10) < 100.0) {
//                    flag = 1;
//                }
//                else if (document.getElementById(progressId)) {
//                    document.getElementById(progressId).style.display = "none";
//                    document.getElementById("indexButton" + i).style.display = "block";
//                }
//                if(parseInt(progress,10) < 0) {
//                    if (document.getElementById("indexState" + i)) {
//                        document.getElementById("indexState" + i).style.display = "block";
//                    }
//                }
//            }
//            if (flag == 1) {
//                indexGetStatus(editRows);
//            }
//        }, 2000);
//    }

    function indexQuery() {
        var indexname = $("#indexQueryName").val().trim();
        if(!indexname){
            indexInit();
        }
        else{
            $.ajax({
                cache: false,
                type: "GET",
                url: "/otscfgsvr/api/index/" + args_tablename + "/" + indexname + "?query_from=0",
                dataType: "json",
                timeout: 20000,
                success: function(results, msg){
                    if (results["errcode"] == 0) {
                        table_detail_indexes = [];
                        table_detail_indexes.push(results);
                        $('#tableDetailIndex').bootstrapTable('destroy').bootstrapTable({
                            data: table_detail_indexes,
                            classes: 'table',
                        });
                    }
                    else {
                        recordAlertMsg("查询索引信息" + errorInfo(results["errcode"]) );
                    }

                },
                error: function(msg){
                    var errmsg = "查询索引 " + getStructName(indexname) + " 信息失败！错误: " + getStructMsg(msg);
                    recordAlertMsg(errmsg);
                }
            });
        }
    }

    function clickIndexEdit(editRow, rowIndex) {
        debugger
        modal_table_name = args_tablename;
        modal_index_name = editRow["index_name"];
        modal_index_info = editRow;
        table_detail_row_index = rowIndex;
        $('#indexEdit').on("show.bs.modal", function () {
            $(this).removeData("bs.modal");
        });
        $("#indexEdit").modal({
            backdrop: "static",
            show: false,
            remote: "index_edit.jsp"
        });
    }

    function clickClearIndex(editRow, rowIndex) {
        var param = {"row":editRow, "index": rowIndex};
        var msg = '确定清空索引 ' + getStructName(editRow["index_name"]) + ' 的记录？';
        indexConfirmAlert(msg, 2, param);
    }

    function indexClear(editRow, rowIndex) {
        $.ajax({
            type:"PUT",
            url:"/otscfgsvr/api/index/" + args_tablename + "/"+ editRow["index_name"],
            data: "{\"truncate\":true}",
            dataType: "json",
            contentType: "application/json",
            timeout:30000,
            success:function(results, msg) {
                if (results.errcode != 0)
                {
                    recordAlertMsg("清空索引" + editRow["index_name"] + errorInfo(results["errcode"]));
                }
                else
                {
                    recordAlertMsg("清空索引" + editRow["index_name"] + " 成功！");
                    $.ajax({
                        cache: false,
                        type:"GET",
                        url:"/otscfgsvr/api/index/" + args_tablename + "/" + editRow["index_name"] + "?query_from=0",
                        dataType: "json",
                        timeout:10000,
                        success:function(results, msg) {
                            if(results["errcode"]==0){
                                var $table = $("#tableDetailIndex").bootstrapTable();
                                $table.bootstrapTable('updateRow', {
                                    index: rowIndex,
                                    row: {
                                        modify_time: results['modify_time'],
                                    }
                                });
                            }else{
                                recordAlertMsg(results["errinfo"]);
                            }

                        },
                        error: function(msg){
                            var errmsg = "获取索引 " + getStructName(editRow["index_name"]) + " 信息错误: " + getStructMsg(msg);
                            recordAlertMsg(errmsg);
                        }
                    });
                }
            },
            error: function(msg){
                var errmsg = "清空索引 " + getStructName(editRow["index_name"]) + " 失败！错误: " + getStructMsg(msg);
                recordAlertMsg(errmsg);
            }
        });
    }

    function clickDeleteIndex(editRow) {
        var msg = '确定删除索引 ' + editRow["index_name"] + '？';
        indexConfirmAlert(msg, 0, editRow);
    }

    function indexDelete(editRow) {
        debugger
        document.body.style.cursor = "wait";
        $.ajax({
            type:"DELETE",
            url:"/otscfgsvr/api/index/" + args_tablename + "/" + editRow["index_name"],
            timeout:100000,
            success:function(results, msg){
                if(results["errcode"] == 0){
                    var dataArray = [];
                    var valuesArray = [];
                    dataArray[0] = editRow;
                    valuesArray[0] = editRow["index_name"];
                    $table = $('#tableDetailIndex').bootstrapTable({
                        data: dataArray
                    });
                    $table.bootstrapTable('remove', {
                        field: 'index_name',
                        values: valuesArray
                    });
                    recordAlertMsg("删除索引" + editRow["index_name"] + " 任务提交成功！");
                } else{
                    recordAlertMsg("删除索引" + editRow["index_name"] + " 任务提交"  + errorInfo(results["errcode"]));
                }

            },
            complete : function() {
                document.body.style.cursor = "default";
            },
            error: function(msg){
                var errmsg = "删除索引 " + getStructName(editRow["index_name"]) + " 任务提交失败！错误: " + getStructMsg(msg);
                recordAlertMsg(errmsg);
            }
        });
    }

    function clickDeleteMultiIndex() {
        var $table = $('#tableDetailIndex').bootstrapTable();
        var selects = $table.bootstrapTable('getSelections');
        if (selects.length == 0){
            recordAlertMsg("请选中至少一条索引。");
            return;
        }
        var msg = '确定删除选定的索引？';
        indexConfirmAlert(msg, 1, selects);
    }

    function indexMultiDelete(selects){
        $.map(selects, function (row) {
            var indexname = row["index_name"];
            document.body.style.cursor = "wait";
            $.ajax({
                type:"DELETE",
                url:"/otscfgsvr/api/index/" + args_tablename + "/" + indexname,
                timeout:100000,
                success:function(results, msg){
                    if (results.errcode != 0) {
                        recordAlertMsg("重建索引" + indexname + "任务提交" + errorInfo( results["errcode"]));
                    }else{
                        var dataArray = [];
                        var valuesArray = [];
                        dataArray[0] = row;
                        valuesArray[0] = indexname;
                        $table = $('#tableDetailIndex').bootstrapTable({
                            data: dataArray
                        });
                        $table.bootstrapTable('remove', {
                            field: 'index_name',
                            values: valuesArray
                        });
                    }
                },
                complete : function() {
                    document.body.style.cursor = "default";
                },
                error: function(msg){
                    var errmsg = "删除索引 " + getStructName(indexname) + " 任务提交失败！错误: " + getStructMsg(msg);
                    recordAlertMsg(errmsg);
                }
            });
        });
    }

    function clickRebuildIndex(editRow, rowIndex) {
        var param = {"row":editRow, "index": rowIndex};
        var msg = '确定重建索引 ' + getStructName(editRow["index_name"]) + '？';
        indexConfirmAlert(msg, 3, param);
    }

    function indexRebuild(editRow, rowIndex){
        var indexname = editRow["index_name"];
        $.ajax({
            type:"PUT",
            url:"/otscfgsvr/api/index/" + args_tablename + "/"+ indexname,
            data:"{\"rebuild\":true}",
            dataType: "json",
            contentType: "application/json",
            timeout:30000,
            success:function(results, msg) {
                if (results.errcode != 0) {
                    recordAlertMsg("重建索引" + indexname + " 任务提交 " +errorInfo( results["errcode"]) );
                }
                else {
                    recordAlertMsg("重建索引" + indexname + " 任务提交成功！");
                    $.ajax({
                        cache: false,
                        type:"GET",
                        url:"/otscfgsvr/api/index/" + args_tablename + "/" + indexname + "?query_from=0",
                        dataType: "json",
                        timeout:10000,
                        success:function(results, msg) {
                            var $table = $("#tableDetailIndex").bootstrapTable();
                            $table.bootstrapTable('updateRow', {
                                index: rowIndex,
                                row: {
                                    modify_time: results['modify_time'],
                                }
                            });
                            editRow["progress"] = 0;
                        },
                        error: function(msg){
                            var errmsg = "获取索引 " + getStructName(indexname) + " 信息失败！错误: " + getStructMsg(msg);
                            recordAlertMsg(errmsg);
                        }
                    });
                }
            },
            error: function(msg){
                var errmsg = "重建索引" + getStructName(indexname) + " 任务提交失败！错误: " + getStructMsg(msg);
                recordAlertMsg(errmsg);
            }
        });
    }

    function goIndexView(indexname, idxtype, tableid){
//        var url = "index_view.jsp?tablename=" + args_tablename + "&indexname=" + indexname + "&pktype=" + args_pktype +"&id=" + tableid;
        var url = "index_view.jsp?tablename=" + args_tablename + "&indexname=" + indexname +"&id=" + tableid;

        url += "&idxtype=" + idxtype;
        window.location=url;
    }

    function clickCreateIndex() {
        modal_table_name = args_tablename;
        $('#indexCreate').on("show.bs.modal", function () {
            $(this).removeData("bs.modal");
        });
        $("#indexCreate").modal({
            backdrop: "static",
            show: false,
            remote: "index_create.jsp"
        });
    }

    function clickAddRecord() {
        modal_table_name = args_tablename;
        $('#recordCreate').on("show.bs.modal", function () {
            $(this).removeData("bs.modal");
        });
        $("#recordCreate").modal({
            backdrop: "static",
            show: false,
            remote: "record_create.jsp"
        });
    }

    function clickEditDisplayColumns() {
        modal_table_name = args_tablename;
        $('#propertyEdit').on("show.bs.modal", function () {
            $(this).removeData("bs.modal");
        });
        $("#propertyEdit").modal({
            backdrop: "static",
            show: false,
            remote: "property_edit.jsp"
        });
    }

    function clickEditRecord(editRow) {
        debugger
        modal_table_name = args_tablename;
//        table_detail_records
        for(var i=0; i <record_table_primary_keys.length;i++){
            modal_PKCols[i]=htmlUnescape(editRow[record_table_primary_keys[i]]);
        }
//        modal_hash_key = htmlUnescape(editRow["hash_key"]);
//        if (args_pktype == 1) {
//            modal_range_key = htmlUnescape(editRow["range_key"]);
//        }
        $('#recordEdit').on("show.bs.modal", function () {
            $(this).removeData("bs.modal");
        });
        $("#recordEdit").modal({
            backdrop: "static",
            show: false,
            remote: "record_edit.jsp"
        });
    }

    //    function clickStrategyDelete() {
    //        modal_table_name = args_tablename;
    //        $('#strategyDelete').on("show.bs.modal", function () {
    //            $(this).removeData("bs.modal");
    //        });
    //        $("#strategyDelete").modal({
    //            backdrop: "static",
    //            show: false,
    //            remote: "record_delete.jsp"
    //        });
    //    }

    function clickConditionDelete() {
        var msg = '确定按选定的查询条件删除记录？';
        recordConditionConfirmAlert(msg, 1, null);
    }

    //    function textFocus(type){
    //        if (type == 0){
    //            $("#AccurateKeyRadio").attr("checked", true);
    ////            $('#tc_tableColumnType' + index_radio).attr("checked", true);
    //        }
    //        else if (type == 1){
    //            $("#rangeStartEndKeyRadio").attr("checked", true);
    ////            $('#rangeStartEndKeyRadio'+ index_radio).attr("checked", true);
    //        }
    //    }
    //
    function getTableDetailShowColumns() {
//        debugger
        var propertyShowNum = 1;

        for (var i=0; i<table_detail_query_columns.length; i++) {
            var columnMap = {"align":"center"};
            columnMap["field"] = table_detail_query_columns[i];
            columnMap["title"] = htmlEscape(table_detail_query_columns[i]);
            table_detail_show_columns[propertyShowNum] = columnMap;
            propertyShowNum++;
        }
        for (var i=propertyShowNum; i<table_detail_show_columns_num; i++) {
            table_detail_show_columns[i] = table_detail_column_property;
        }
    }
</script>
</html>
