package com.baosight.xinsight.ots.rest.api;

import com.baosight.xinsight.model.PermissionCheckUserInfo;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.ots.rest.constant.ErrorMode;
import com.baosight.xinsight.ots.rest.constant.RestConstants;
import com.baosight.xinsight.ots.rest.model.table.operate.TableCreateBody;
import com.baosight.xinsight.ots.rest.model.table.operate.TableUpdateBody;
import com.baosight.xinsight.ots.rest.service.TableService;
import com.baosight.xinsight.ots.rest.util.PermissionUtil;
import com.baosight.xinsight.ots.rest.util.RegexUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
@Path("/table")
public class TableResource {
    private static final Logger LOG = Logger.getLogger(TableResource.class);

    @Context
    UriInfo uriInfo;
    @Context
    HttpServletRequest request;


    /**
     * 创建表
     * @param tableName
     * @param body
     * @return
     */
    @POST
    @Path("/{tablename}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.APPLICATION_JSON)
    public Response postTable(@PathParam("tablename") String tableName, String body) {
        try{
            //todo lyh 校验表名合法性
            if (!RegexUtil.isValidTableName(tableName)) {
                LOG.error(Response.Status.FORBIDDEN.name() + ": tablename '" + tableName + "' contains illegal char.");
                throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST,
                        Response.Status.FORBIDDEN.name() + ": tablename '" + tableName + "' contains illegal char.");
            }
            //获得userInfo
            PermissionCheckUserInfo userInfo = new PermissionCheckUserInfo();
            userInfo = PermissionUtil.getUserInfoModel(userInfo, request);
            LOG.debug("Post:" + tableName + "\nContent:\n" + body);

            //生成body对应的model
            TableCreateBody tableCreateBody = TableCreateBody.toClass(body);

            //创建表，包含小表和大表（如果大表不存在）。
            TableService.createTable(userInfo, tableName, tableCreateBody);

        }catch (OtsException e) {
            e.printStackTrace();
            LOG.error(ExceptionUtils.getFullStackTrace(e));
            return Response.status(e.getErrorCode() == OtsErrorCode.EC_OTS_PERMISSION_NO_PERMISSION_FAULT?Response.Status.FORBIDDEN : Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(e.getErrorCode(), e.getMessage())).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(500L, e.getMessage())).build();
        }

        // successful
        return Response.status(Response.Status.CREATED).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(0L)).build();
    }

    /**
     * 获取表的详细信息， 注意权限的筛选
     * @param tableName
     * @return
     */
    @GET
    @Path("/{tablename}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTableInfo(@PathParam("tablename") String tableName) {
        //获得userInfo
        PermissionCheckUserInfo userInfo = new PermissionCheckUserInfo();
        userInfo = PermissionUtil.getUserInfoModel(userInfo, request);
        LOG.debug("GET:" + tableName);

        //获取表信息。若不存在则提示错误：不存在该表。
        try {
            //获取所有表的详细信息
            if (tableName.equals(RestConstants.Query_all_tables_info)) {
                String limit = StringUtils.trim(uriInfo.getQueryParameters().getFirst(RestConstants.Query_limit));
                String offset = StringUtils.trim(uriInfo.getQueryParameters().getFirst(RestConstants.Query_offset));

                return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON)
                        .entity(TableService.getAllTableInfoList(userInfo, TableService.dealWithLimit(limit), TableService.dealWithOffset(offset))).build();
            } else {
                //todo lyh 错误码和校验规则
                if (!RegexUtil.isValidTableName(tableName)) {
                    LOG.error(Response.Status.FORBIDDEN.name() + ":" + tableName + " is not a valid table name.");
                    return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON).entity(
                            new ErrorMode(OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST, Response.Status.FORBIDDEN.name() + ":" + tableName + " is not a valid table name.")).build();
                }

                return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON)
                        .entity(TableService.getTableInfo(userInfo,tableName).toString()).build();

            }
        }catch (OtsException e) {
            e.printStackTrace();
            return Response.status(e.getErrorCode() == OtsErrorCode.EC_OTS_PERMISSION_NO_PERMISSION_FAULT?Response.Status.FORBIDDEN : Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(e.getErrorCode(), e.getMessage())).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(500L, e.getMessage())).build();
        }
    }

    /**
     * 查询满足模糊条件的表
     * @return 表名的列表
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTableNameList(@QueryParam(value="name") String name,
                                 @QueryParam(value="limit") String limit,
                                 @QueryParam(value="offset") String offset) {
        PermissionCheckUserInfo userInfo = new PermissionCheckUserInfo();
        userInfo = PermissionUtil.getUserInfoModel(userInfo, request);
        LOG.debug("Get table list, user:" + userInfo.getUserName() + "@" + userInfo.getTenantName());

        if (StringUtils.isBlank(name)){
            return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON).entity(
                    new ErrorMode(OtsErrorCode.EC_OTS_STORAGE_NULL_PARAMETER, Response.Status.FORBIDDEN.name() + ":" + "parameter 'name' is needed.")).build();
        }

        try{
            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(
                    TableService.getTableNameList(userInfo,name,TableService.dealWithLimit(limit),TableService.dealWithOffset(offset),true)).build();
        } catch (OtsException e) {
            e.printStackTrace();
            return Response.status(e.getErrorCode() == OtsErrorCode.EC_OTS_PERMISSION_NO_PERMISSION_FAULT?Response.Status.FORBIDDEN : Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(e.getErrorCode(), e.getMessage())).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(500L, e.getMessage())).build();
        }

    }

    /**
     * 查询所有表
     * @return 表名的列表
     */
    @GET
    @Path("/_all_tables")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTableNameList() {
        PermissionCheckUserInfo userInfo = new PermissionCheckUserInfo();
        userInfo = PermissionUtil.getUserInfoModel(userInfo, request);
        LOG.debug("Get table list, user:" + userInfo.getUserName() + "@" + userInfo.getTenantName());

        String limit = StringUtils.trim(uriInfo.getQueryParameters().getFirst(RestConstants.Query_limit));
        String offset = StringUtils.trim(uriInfo.getQueryParameters().getFirst(RestConstants.Query_offset));

        try{
             return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(
                TableService.getAllTableNameList(userInfo,TableService.dealWithLimit(limit),TableService.dealWithOffset(offset))).build();
        } catch (OtsException e) {
            e.printStackTrace();
            return Response.status(e.getErrorCode() == OtsErrorCode.EC_OTS_PERMISSION_NO_PERMISSION_FAULT?Response.Status.FORBIDDEN : Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(e.getErrorCode(), e.getMessage())).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(500L, e.getMessage())).build();
        }
    }


    @PUT
    @Path("/{tablename}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.APPLICATION_JSON)
    public Response putTable(@PathParam("tablename") String tableName, String body) {
        //todo lyh 校验表名

        PermissionCheckUserInfo userInfo = new PermissionCheckUserInfo();
        userInfo = PermissionUtil.getUserInfoModel(userInfo, request);
        LOG.debug("PUT table, user:" + userInfo.getUserName() + "@" + userInfo.getTenantName());

        try {
            //生成body对应的model
            TableUpdateBody tableUpdateBody = TableUpdateBody.toClass(body);
            //修改表
            TableService.updateTable(userInfo, tableName, tableUpdateBody);
        } catch (OtsException e) {
            e.printStackTrace();
            LOG.error(ExceptionUtils.getFullStackTrace(e));
            return Response.status(e.getErrorCode() == OtsErrorCode.EC_OTS_PERMISSION_NO_PERMISSION_FAULT ? Response.Status.FORBIDDEN :Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(e.getErrorCode(), e.getMessage())).build();
        }catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(500L, e.getMessage())).build();
        }

        // successful
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(0L)).build();
    }

    @DELETE
    @Path("/{tablename}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTable(@PathParam("tablename") String tablename) {
        //todo lyh 校验表名

        PermissionCheckUserInfo userInfo = new PermissionCheckUserInfo();
        userInfo = PermissionUtil.getUserInfoModel(userInfo, request);
        LOG.debug("DELETE table, user:" + userInfo.getUserName() + "@" + userInfo.getTenantName());


        try{
            TableService.deleteTable(userInfo,tablename);
        } catch (OtsException e) {
            e.printStackTrace();
            return Response.status(e.getErrorCode() == OtsErrorCode.EC_OTS_PERMISSION_NO_PERMISSION_FAULT?Response.Status.FORBIDDEN : Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(e.getErrorCode(), e.getMessage())).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(500L, e.getMessage())).build();
        }
        // successful
        return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(0L)).build();

    }


}
