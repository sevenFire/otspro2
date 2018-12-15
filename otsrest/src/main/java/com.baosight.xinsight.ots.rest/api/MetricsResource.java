package com.baosight.xinsight.ots.rest.api;

import com.baosight.xinsight.common.CommonConstants;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.ots.rest.constant.ErrorMode;
import com.baosight.xinsight.ots.rest.model.metrics.response.MetricsInfoBody;
import com.baosight.xinsight.ots.rest.model.metrics.response.MetricsInfoListBody;
import com.baosight.xinsight.ots.rest.service.MetricsService;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
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
@Path("/metrics")
public class MetricsResource {
    private static final Logger LOG = Logger.getLogger(MetricsResource.class);

    @Context
    UriInfo uriInfo;
    @Context
    HttpServletRequest request;


    /**
     * 查询单表的监控信息
     */
    @GET
    @Path("/{tablename}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMetricsInfoByTableName(@PathParam("tablename") String tableName) {
        //todo lyh 表名校验

        long tenantId = Long.parseLong(request.getAttribute(CommonConstants.SESSION_TENANTID_KEY).toString());

        try {
            //校验namespace是否存在
            if (!MetricsService.isNamespaceExist(tenantId)) {
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON)
                        .entity(new ErrorMode((long) OtsErrorCode.EC_RDS_FAILED_QUERY_TENANT, Response.Status.NOT_FOUND.name() + ": tenantId '" + tenantId + "' is not exist.")).build();
            } else {//查询监控信息
                MetricsInfoBody metricsInfoBody = MetricsService.getMetricsInfoByTableName(tenantId, tableName);
                metricsInfoBody.setErrcode(0L);

                return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON)
                        .entity(metricsInfoBody).build();
            }
        } catch (OtsException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(e.getErrorCode(), e.getMessage())).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(500L, e.getMessage())).build();
        }
    }


    /**
     * 查询某租户下所有表的监控信息
     * 以tenantId作为namespace。
     * 并且同一个租户下所有用户共用一张HBase大表，表名为1:ots_tenantId。
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMetricsInfoByNamespace() {

        long tenantId = Long.parseLong(request.getAttribute(CommonConstants.SESSION_TENANTID_KEY).toString());

        try{
            if (!MetricsService.isNamespaceExist(tenantId)) {
                return Response.status(Response.Status.NOT_FOUND).type(MediaType.APPLICATION_JSON)
                        .entity(new ErrorMode((long) OtsErrorCode.EC_RDS_FAILED_QUERY_TENANT, Response.Status.NOT_FOUND.name() + ": tenantid '" + tenantId + "' is not exist.")).build();
            }
            else {
                MetricsInfoListBody metricsInfoListBody = MetricsService.getMetricsInfoByNamespace(tenantId);
                metricsInfoListBody.setErrcode(0L);

                return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON)
                        .entity(metricsInfoListBody).build();
            }
        } catch (OtsException e) {
            e.printStackTrace();
            return Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(e.getErrorCode(), e.getMessage())).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(500L, e.getMessage())).build();
        }
    }
}
