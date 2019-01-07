<%@ page language="java" contentType="text/html; charset=UTF-8"  pageEncoding="UTF-8"%>
<div class="modal-header" style="background-color:#f5f5f5;">
	<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
	<h4 class="modal-title" id="myModalLabel" style="text-align:left;font-weight:bold;">表的编辑</h4>
</div>

<div class="modal-body">
	<div style="height:360px;width:650px;">
		<div class="row" style="height:40px;margin-left:130px;"></div>
		<div class="row" id="rowTableInfoTc" style="margin-left:70px;height:180px;">
			<div  class="row" style="margin-top:0px;">
				<div class="r_text" style="width:100px;padding-top:3px;">*表名：</div>
				<div class="r_box" style='width:370px;'>
					<input name='te_tableName' id='te_tableName' type='text' class='' style='width:350px;height:25px;' value='' disabled="disabled">
					<span class="glyphicon glyphicon-exclamation-sign" title="注意：一旦创建表名不可更改！"></span>
				</div>
				<div class="r_text14" id="te_noTableName" style="padding-top:10px;width:45px;display:none;">必填！</div>
			</div>

			<div  class="row" style="margin-top:0px;">
				<div class="r_text" style="width:100px;padding-top:3px;">描述信息：</div>
				<div class="r_box">
					<textarea id="te_description" class="textbox1" style="width:350px;height:80px;max-height:160px;resize:none;" onKeypress="checkEnter(event)" ></textarea>
				</div>
			</div>

		</div>
		<div class="row" style="margin-left:50px;margin-top:60px;height:40px;">
			<div class="r_text14" id="tableUpdateStatus" data-name="te_status" style="margin-left:10px;width:550px;text-align:center"></div>
		</div>
		<div  class="row" style="margin-top:0px;margin-left:0px;">
			<div class="r_box" style="width:100%;text-align:center">
				<input id="te_cancel1" name="" type="button" class="btn2" style="width:80px" value="取消" data-dismiss="modal">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				<input id="btn_editTable" name="" type="button" class="btn2" style="width:60px" value="更新" onclick='tableUpdate();'>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			</div>
		</div>
	</div>
</div>
<script type="text/javascript">

    $(function(){
//	 	    debugger;
        tableUpdateWindow(modal_table_name);
    });

    function tableUpdateWindow(tableName) {
        $("#tableUpdateStatus").html("");
        $("#te_tableName").attr("value", tableName);

        //todo 描述信息录入
        $("#te_description").attr("value", "");
        for (var i=0; i<all_table_info.length; i++){
            if(all_table_info[i]["table_name"] == tableName){

                $("#te_description").attr("value", all_table_info[i]["table_desc"]);

            }
        }
    }

    function tableUpdate(){
        debugger
        $("#tableUpdateStatus").html("");
        if($("#te_description").val().length > 256)
        {
            $("#tableUpdateStatus").html("");
            $("#tableUpdateStatus").append("表描述长度不可以超过256个字符。");
            return;
        }

        //var desc = htmlEscape($("#description2").val());
        var desc = $("#te_description").val();
//            alert("更新表");
//            return;
        $.ajax({
            type: "PUT",
            url: "/otscfgsvr/api/table/" + $("#te_tableName").val(),
            data: JSON.stringify({"table_desc": desc}),
            dataType: "json",
            contentType: "application/json",
            timeout: 10000,
            success: function (results, msg) {
                $("#tableUpdateStatus").append("更新表").append(errorInfo(results["errcode"]));
                if(results["errcode"] ==0){
                    var $table = $("#tableManage").bootstrapTable();
                    $table.bootstrapTable('updateRow', {
                        index: table_row_index,
                        row: {
                            table_desc: desc
                        }
                    });
                    getTableInfo();
                }
            },
            error: function(msg){
                var errmsg = "更新表错误: " + getStructMsg(msg);
                $("#tableUpdateStatus").append(errmsg);
            }
        });
    }
</script>

