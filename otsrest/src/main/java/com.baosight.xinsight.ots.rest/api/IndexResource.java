package com.baosight.xinsight.ots.rest.api;

import com.alibaba.fastjson.JSONObject;
import com.baosight.xinsight.model.PermissionCheckUserInfo;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.ots.rest.constant.ErrorMode;
import com.baosight.xinsight.ots.rest.service.IndexService;
import com.baosight.xinsight.ots.rest.util.PermissionUtil;

import org.apache.log4j.Logger;

import java.io.IOException;

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
@Path("/index")
public class IndexResource extends RestBase {
    private static final Logger LOG = Logger.getLogger(IndexResource.class);

    @Context
    UriInfo uriInfo;
    @Context
    HttpServletRequest request;


    /**
     * 创建索引，目前只处理HBase索引。
     * @param tableName
     * @param indexName
     * @return
     */
    @POST
    @Path("/{tablename}/{indexname}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.APPLICATION_JSON)
    public Response post(@PathParam("tablename") String tableName,
                         @PathParam("indexname") String indexName) {

        //todo lyh 校验表名
        //todo lyh 校验索引名

        //get userInfo
        PermissionCheckUserInfo userInfo = new PermissionCheckUserInfo();
        userInfo = PermissionUtil.getUserInfoModel(userInfo, request);

        //get body
        JSONObject postBody;
        try {
            postBody = readJsonToMap();
        } catch (OtsException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorMode(e.getErrorCode(), e.getMessage())).build();
        }

        //create index
        try {
            return Response.status(Response.Status.CREATED).type(MediaType.APPLICATION_JSON)
                        .entity(IndexService.createIndex(userInfo, tableName, indexName, postBody)).build();
        } catch (OtsException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }

}
