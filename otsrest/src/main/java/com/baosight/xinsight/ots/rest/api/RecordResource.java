package com.baosight.xinsight.ots.rest.api;

import com.baosight.xinsight.model.PermissionCheckUserInfo;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.constants.ParamConstant;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.ots.rest.constant.ErrorMode;
import com.baosight.xinsight.ots.rest.constant.RestConstants;
import com.baosight.xinsight.ots.rest.model.record.request.RecordAllQueryBody;
import com.baosight.xinsight.ots.rest.model.record.request.RecordInfoListRequestBody;
import com.baosight.xinsight.ots.rest.model.record.request.RecordQueryBody;
import com.baosight.xinsight.ots.rest.model.record.request.RecordQueryListBody;
import com.baosight.xinsight.ots.rest.model.record.response.RecordInfoListResponseBody;
import com.baosight.xinsight.ots.rest.service.RecordService;
import com.baosight.xinsight.ots.rest.util.ParamUtil;
import com.baosight.xinsight.ots.rest.util.PermissionUtil;
import com.baosight.xinsight.ots.rest.util.RegexUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * @author liyuhui
 * @date 2018/12/13
 * @description
    *  @Path 定义的是class级别的路径,体现在URI中指代资源路径.
    *  @Context用于注入上下文对象,例如ServletContext, Request, Response, UriInfo.
    *  @GET 表示该方法处理HTTP的GET请求.
    *  @Path 这里是定义method级别的路径,体现在URI中指代资源子路径,可以带有{}模板匹配参数.
    *  @Produces 指定返回的协议数据类型,可以定义为一个集合。
    *  @PathParam 注入url中传递的参数.
 *
 */
@Path("/record")
public class RecordResource extends RestBase{
    private static final Logger LOG = Logger.getLogger(RecordResource.class);

    @Context
    UriInfo uriInfo;
    @Context
    HttpServletRequest request;


    /**
     * 新增记录
     * @param tableName
     * @param body
     * @return
     */
    @POST
    @Path("/{tablename}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.APPLICATION_JSON)
    public Response postRecord(@PathParam("tablename") String tableName, String body) {
        //todo lyh 对表名的校验
        if (tableName.equals(RestConstants.Query_all_tables)) {
            LOG.error(Response.Status.FORBIDDEN.name() + ":" + tableName + " is not a valid table object.");
            return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON).entity(
                    new ErrorMode(OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST, Response.Status.FORBIDDEN.name() + ":" + tableName + " is not a valid table object.")).build();
        }

        //get userInfo
        PermissionCheckUserInfo userInfo = new PermissionCheckUserInfo();
        userInfo = PermissionUtil.getUserInfoModel(userInfo, request);

        //post record
        try {
            if (!RegexUtil.isValidTableName(tableName)) {
                LOG.error(Response.Status.FORBIDDEN.name() + ": tablename '" + tableName + "' contains illegal char.");
                throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST, Response.Status.FORBIDDEN.name() + ": tablename '" + tableName + "' contains illegal char.");
            }

            if (body.length() > 1000) {
                LOG.debug("Post:" + tableName + "\n Part Content:\n" + body.substring(0, 999));
            } else {
                LOG.debug("Post:" + tableName + "\nContent:\n" + body);
            }
            RecordInfoListRequestBody recordInfoListRequestBody = RecordInfoListRequestBody.toClass(body);

            return Response.status(Response.Status.CREATED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(RecordService.insertRecords(userInfo, tableName, recordInfoListRequestBody)).build();
        }catch (OtsException e) {
            e.printStackTrace();
            return Response.status(e.getErrorCode() == OtsErrorCode.EC_OTS_PERMISSION_NO_PERMISSION_FAULT?Response.Status.FORBIDDEN : Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(e.getErrorCode(), e.getMessage())).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(500L, e.getMessage())).build();
        }

    }


    /**
     * 修改记录
     * @param tableName
     * @param body
     * @return
     */
    @PUT
    @Path("/{tablename}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.APPLICATION_JSON)
    public Response putRecord(@PathParam("tablename") String tableName, String body) {
        //todo lyh 对表名的校验

        //get userInfo
        PermissionCheckUserInfo userInfo = new PermissionCheckUserInfo();
        userInfo = PermissionUtil.getUserInfoModel(userInfo, request);

        //put record
        try {
            if (!RegexUtil.isValidTableName(tableName)) {
                LOG.error(Response.Status.FORBIDDEN.name() + ": tablename '" + tableName + "' contains illegal char.");
                throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST, Response.Status.FORBIDDEN.name() + ": tablename '" + tableName + "' contains illegal char.");
            }

            if (body.length() > 1000) {
                LOG.debug("Post:" + tableName + "\n Part Content:\n" + body.substring(0, 999));
            } else {
                LOG.debug("Post:" + tableName + "\nContent:\n" + body);
            }
            RecordInfoListRequestBody recordInfoListRequestBody = RecordInfoListRequestBody.toClass(body);


            return Response.status(Response.Status.CREATED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(RecordService.updateRecords(userInfo, tableName, recordInfoListRequestBody)).build();
        }catch (OtsException e) {
            e.printStackTrace();
            return Response.status(e.getErrorCode() == OtsErrorCode.EC_OTS_PERMISSION_NO_PERMISSION_FAULT?Response.Status.FORBIDDEN : Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(e.getErrorCode(), e.getMessage())).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(500L, e.getMessage())).build();
        }
    }

    /**
     * 查询记录
     * @param tableName
     * @param body
     * @return
     */
    @POST
    @Path("/{tablename}/query")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRecordsByPrimaryKey(@PathParam("tablename") String tableName, String body) {
        if (tableName.equals(RestConstants.Query_all_tables)) {
            LOG.error(Response.Status.FORBIDDEN.name() + ":" + tableName + " is not a valid table object.");
            return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON).entity(
                    new ErrorMode(OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST, Response.Status.FORBIDDEN.name() + ":" + tableName + " is not a valid table object.")).build();
        }

        //get userInfo
        PermissionCheckUserInfo userInfo = new PermissionCheckUserInfo();
        userInfo = PermissionUtil.getUserInfoModel(userInfo, request);

        //get record
        try {
            if (!RegexUtil.isValidTableName(tableName)) {
                LOG.error(Response.Status.FORBIDDEN.name() + ": tablename '" + tableName + "' contains illegal char.");
                throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST, Response.Status.FORBIDDEN.name() + ": tablename '" + tableName + "' contains illegal char.");
            }

            RecordQueryBody recordQueryBody = RecordQueryBody.toClass(body);
            RecordInfoListResponseBody recordInfoListResponseBody = RecordService.getRecordsByPrimaryKey(userInfo, tableName, recordQueryBody);

            return Response.status(Response.Status.CREATED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(recordInfoListResponseBody).build();
        }catch (OtsException e) {
            e.printStackTrace();
            return Response.status(e.getErrorCode() == OtsErrorCode.EC_OTS_PERMISSION_NO_PERMISSION_FAULT?Response.Status.FORBIDDEN : Response.Status.BAD_REQUEST)
                    .type(MediaType.APPLICATION_JSON).entity(new ErrorMode(e.getErrorCode(), e.getMessage())).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(500L, e.getMessage())).build();
        }

    }

    /**
     * 查询记录
     * @param tableName
     * @param body
     * @return
     */
    @POST
    @Path("/{tablename}/keys")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRecordsByPrimaryKeys(@PathParam("tablename") String tableName, String body){
        if (tableName.equals(RestConstants.Query_all_tables)) {
            LOG.error(Response.Status.FORBIDDEN.name() + ":" + tableName + " is not a valid table object.");
            return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON).entity(
                    new ErrorMode(OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST, Response.Status.FORBIDDEN.name() + ":" + tableName + " is not a valid table object.")).build();
        }

        //get userInfo
        PermissionCheckUserInfo userInfo = new PermissionCheckUserInfo();
        userInfo = PermissionUtil.getUserInfoModel(userInfo, request);

        //get records
        try {
            if (!RegexUtil.isValidTableName(tableName)) {
                LOG.error(Response.Status.FORBIDDEN.name() + ": tablename '" + tableName + "' contains illegal char.");
                throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST, Response.Status.FORBIDDEN.name() + ": tablename '" + tableName + "' contains illegal char.");
            }

            RecordQueryListBody recordQueryListBody = RecordQueryListBody.toClass(body);
            RecordInfoListResponseBody recordInfoListResponseBody = RecordService.getRecordsByPrimaryKeys(userInfo, tableName, recordQueryListBody);

            return Response.status(Response.Status.CREATED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(recordInfoListResponseBody).build();
        }catch (OtsException e) {
            e.printStackTrace();
            return Response.status(e.getErrorCode() == OtsErrorCode.EC_OTS_PERMISSION_NO_PERMISSION_FAULT?Response.Status.FORBIDDEN : Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(e.getErrorCode(), e.getMessage())).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(500L, e.getMessage())).build();
        }


    }

    /**
     * 查询所有记录
     * @param tableName
     * @param body
     * @return
     */
    @GET
    @Path("/{tablename}/_all_records")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRecords(@PathParam("tablename") String tableName){
        if (tableName.equals(RestConstants.Query_all_tables)) {
            LOG.error(Response.Status.FORBIDDEN.name() + ":" + tableName + " is not a valid table object.");
            return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON).entity(
                    new ErrorMode(OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST, Response.Status.FORBIDDEN.name() + ":" + tableName + " is not a valid table object.")).build();
        }

        //get userInfo
        PermissionCheckUserInfo userInfo = new PermissionCheckUserInfo();
        userInfo = PermissionUtil.getUserInfoModel(userInfo, request);

        //get records
        try {
            if (!RegexUtil.isValidTableName(tableName)) {
                LOG.error(Response.Status.FORBIDDEN.name() + ": tablename '" + tableName + "' contains illegal char.");
                throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST, Response.Status.FORBIDDEN.name() + ": tablename '" + tableName + "' contains illegal char.");
            }


            //处理参数
            String limit = StringUtils.trim(uriInfo.getQueryParameters().getFirst(RestConstants.Query_limit));
            String cursorMark = StringUtils.trim(uriInfo.getQueryParameters().getFirst(ParamConstant.KEY_CURSOR_MARK));
            String returnColumnsString = uriInfo.getQueryParameters().getFirst(ParamConstant.KEY_RETURN_COLUMNS);

            RecordAllQueryBody recordAllQueryBody = new RecordAllQueryBody(returnColumnsString, ParamUtil.dealWithLimit(limit),ParamUtil.dealWithCursorMark(cursorMark));

            RecordInfoListResponseBody recordInfoListResponseBody = RecordService.getAllRecords(userInfo, tableName, recordAllQueryBody);

            return Response.status(Response.Status.CREATED)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(recordInfoListResponseBody).build();
        }catch (OtsException e) {
            e.printStackTrace();
            return Response.status(e.getErrorCode() == OtsErrorCode.EC_OTS_PERMISSION_NO_PERMISSION_FAULT?Response.Status.FORBIDDEN : Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(e.getErrorCode(), e.getMessage())).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(500L, e.getMessage())).build();
        }


    }


    /**
     * 删除满足条件的记录
     * @param tableName
     * @return
     */
    @DELETE
    @Path("/{tablename}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteRecords(@PathParam("tablename") String tableName, String body){
        if (tableName.equals(RestConstants.Query_all_tables)) {
            LOG.error(Response.Status.FORBIDDEN.name() + ":" + tableName + " is not a valid table object.");
            return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON).entity(
                    new ErrorMode(OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST, Response.Status.FORBIDDEN.name() + ":" + tableName + " is not a valid table object.")).build();
        }

        try {
            if (!RegexUtil.isValidTableName(tableName)) {
                LOG.error(Response.Status.FORBIDDEN.name() + ": tablename '" + tableName + "' contains illegal char.");
                throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST, Response.Status.FORBIDDEN.name() + ": tablename '" + tableName + "' contains illegal char.");
            }

            PermissionCheckUserInfo userInfo = new PermissionCheckUserInfo();
            userInfo = PermissionUtil.getUserInfoModel(userInfo, request);
            LOG.debug("Delete: " + tableName + "\n");

            RecordQueryBody recordQueryBody = RecordQueryBody.toClass(body);

            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON)
                    .entity(RecordService.deleteRecordsByPrimaryKey(userInfo, tableName,recordQueryBody)).build();

        } catch (OtsException e) {
            e.printStackTrace();
            return Response.status(e.getErrorCode() == OtsErrorCode.EC_OTS_PERMISSION_NO_PERMISSION_FAULT?Response.Status.FORBIDDEN : Response.Status.BAD_REQUEST).entity(new ErrorMode(e.getErrorCode(), e.getMessage())).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(500L, e.getMessage())).build();
        }
    }

    /**
     * 清空记录
     * @param tableName
     * @return
     */
    @DELETE
    @Path("/{tablename}/truncate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteAllRecords(@PathParam("tablename") String tableName){
        if (tableName.equals(RestConstants.Query_all_tables)) {
            LOG.error(Response.Status.FORBIDDEN.name() + ":" + tableName + " is not a valid table object.");
            return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON).entity(
                    new ErrorMode(OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST, Response.Status.FORBIDDEN.name() + ":" + tableName + " is not a valid table object.")).build();
        }

        try {
            if (!RegexUtil.isValidTableName(tableName)) {
                LOG.error(Response.Status.FORBIDDEN.name() + ": tablename '" + tableName + "' contains illegal char.");
                throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST, Response.Status.FORBIDDEN.name() + ": tablename '" + tableName + "' contains illegal char.");
            }

            PermissionCheckUserInfo userInfo = new PermissionCheckUserInfo();
            userInfo = PermissionUtil.getUserInfoModel(userInfo, request);
            LOG.debug("Delete: truncate " + tableName + "\n");

            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON)
                    .entity(RecordService.deleteAllRecords(userInfo, tableName)).build();

        } catch (OtsException e) {
            e.printStackTrace();
            return Response.status(e.getErrorCode() == OtsErrorCode.EC_OTS_PERMISSION_NO_PERMISSION_FAULT?Response.Status.FORBIDDEN : Response.Status.BAD_REQUEST).entity(new ErrorMode(e.getErrorCode(), e.getMessage())).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(500L, e.getMessage())).build();
        }


    }

}
