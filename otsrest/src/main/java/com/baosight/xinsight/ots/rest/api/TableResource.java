package com.baosight.xinsight.ots.rest.api;

import com.baosight.xinsight.model.PermissionCheckUserInfo;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.ots.rest.common.ErrorMode;
import com.baosight.xinsight.ots.rest.service.TableService;
import com.baosight.xinsight.ots.rest.util.PermissionUtil;
import com.baosight.xinsight.ots.rest.util.RegexUtil;
import com.baosight.xinsight.ots.rest.vo.table.operate.TableCreateBodyVo;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
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
    public Response post(@PathParam("tablename") String tableName, String body) {
        //校验表名合法性
        //todo lyh 错误码和校验规则
        if (!RegexUtil.isValidTableName(tableName)) {
            LOG.error(Response.Status.FORBIDDEN.name() + ":" + tableName + " is not a valid table name.");
            return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON).entity(
                    new ErrorMode(OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST, Response.Status.FORBIDDEN.name() + ":" + tableName + " is not a valid table name.")).build();
        }

        //获得userInfo和body
        PermissionCheckUserInfo userInfo = new PermissionCheckUserInfo();
        userInfo = PermissionUtil.getUserInfoModel(userInfo, request);
        LOG.debug("Post:" + tableName + "\nContent:\n" + body);

        //生成body对应的model并校验参数合法性
        TableCreateBodyVo bodyModel;
        try {
            bodyModel = TableCreateBodyVo.toClass(body);
        } catch (OtsException e) {
            e.printStackTrace();
            LOG.error(ExceptionUtils.getFullStackTrace(e));
            return Response.status(e.getErrorCode() == OtsErrorCode.EC_OTS_PERMISSION_NO_PERMISSION_FAULT ? Response.Status.FORBIDDEN :Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(e.getErrorCode(), e.getMessage())).build();
        }

        //创建表
        try {
            TableService.createTable(userInfo, tableName, bodyModel);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(500L, e.getMessage())).build();
        }

        // successful
        return Response.status(Response.Status.CREATED).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(0L)).build();
    }
}
