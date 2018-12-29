package com.baosight.xinsight.ots.rest.api;

import com.baosight.xinsight.model.PermissionCheckUserInfo;
import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.exception.OtsException;
import com.baosight.xinsight.ots.rest.constant.ErrorMode;
import com.baosight.xinsight.ots.rest.constant.RestConstants;
import com.baosight.xinsight.ots.rest.model.index.request.IndexCreateBody;
import com.baosight.xinsight.ots.rest.model.index.response.IndexInfoListResponseBody;
import com.baosight.xinsight.ots.rest.model.index.response.IndexNamesResponseBody;
import com.baosight.xinsight.ots.rest.model.index.same.IndexInfoBody;
import com.baosight.xinsight.ots.rest.service.IndexService;
import com.baosight.xinsight.ots.rest.util.PermissionUtil;
import com.baosight.xinsight.ots.rest.util.RegexUtil;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
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
                         @PathParam("indexname") String indexName,
                         String body) {

        if (tableName.equals(RestConstants.Query_all_tables)) {
            LOG.error(Response.Status.FORBIDDEN.name() + ":" + tableName + " is not a valid table object.");
            return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorMode(OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST, Response.Status.FORBIDDEN.name() + ":" + tableName + " is not a valid table object.")).build();
        }

        try{
            if (!RegexUtil.isValidTableName(tableName)) {
                LOG.error(Response.Status.FORBIDDEN.name() + ": tablename '" + tableName + "' contains illegal char.");
                throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST, Response.Status.FORBIDDEN.name() + ": tablename '" + tableName + "' contains illegal char.");
            }

            if (indexName.equals(RestConstants.Query_all_indexes)) {
                LOG.error(Response.Status.FORBIDDEN.name() + ":" + indexName + " is not a valid index object.");
                return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON)
                        .entity(new ErrorMode(OtsErrorCode.EC_OTS_STORAGE_INDEX_NO_EXIST, Response.Status.FORBIDDEN.name() + ":" + indexName + " is not a valid index object.")).build();
            }

            if (!RegexUtil.isValidIndexName(indexName)) {
                LOG.error(Response.Status.FORBIDDEN.name() + ": indexname '" + indexName + "' contain illegal char.");
                throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_INDEX_NO_EXIST, Response.Status.FORBIDDEN.name() + ": indexname '" + indexName + "' contain illegal char.");
            }

            LOG.debug("Post:" + tableName + "/" + indexName + "\nContent:\n" + body);

            //get body
            IndexCreateBody postBody = IndexCreateBody.toClass(body);

            //get userInfo
            PermissionCheckUserInfo userInfo = new PermissionCheckUserInfo();
            userInfo = PermissionUtil.getUserInfoModel(userInfo, request);

            return Response.status(Response.Status.CREATED).type(MediaType.APPLICATION_JSON)
                    .entity(IndexService.createIndex(userInfo, tableName, indexName, postBody)).build();
        }catch (OtsException e) {
            e.printStackTrace();
            return Response.status(e.getErrorCode() == OtsErrorCode.EC_OTS_PERMISSION_NO_PERMISSION_FAULT?Response.Status.FORBIDDEN : Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(e.getErrorCode(), e.getMessage())).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(500L, e.getMessage())).build();
        }
    }

    /**
     * 查询单个索引详细信息
     * @param tableName
     * @param indexName
     * @return
     */
    @GET
    @Path("/{tablename}/{indexname}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("tablename") String tableName, @PathParam("indexname") String indexName) {
        if (tableName.equals(RestConstants.Query_all_tables.toString())) {
            LOG.error(Response.Status.FORBIDDEN.name() + ":" + tableName + " is not a valid table object.");
            return Response.status(Response.Status.FORBIDDEN).type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorMode(OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST, Response.Status.FORBIDDEN.name() + ":" + tableName + " is not a valid table object.")).build();
        }

        try {
            //校验表名
            if (!RegexUtil.isValidTableName(tableName)) {
                LOG.error(Response.Status.FORBIDDEN.name() + ": tablename '" + tableName + "' contains illegal char.");
                throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_TABLE_NOTEXIST, Response.Status.FORBIDDEN.name() + ": tablename '" + tableName + "' contains illegal char.");
            }
            LOG.debug("Get:" + tableName + "/" + indexName);

            //获取用户信息
            PermissionCheckUserInfo userInfo = new PermissionCheckUserInfo();
            userInfo = PermissionUtil.getUserInfoModel(userInfo, request);

            if (indexName.equals(RestConstants.Query_all_indexes.toString())) {//查询所有索引名
                IndexNamesResponseBody responseBody = IndexService.getIndexNameList(userInfo, tableName);
                return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(responseBody).build();
            } else if (indexName.equals(RestConstants.Query_all_indexes_info.toString())) {//查询所有索引信息
                IndexInfoListResponseBody responseBody = IndexService.getIndexInfoList(userInfo, tableName);
                return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(responseBody).build();
            } else {//查询单个索引

                //校验索引名
                if (!RegexUtil.isValidIndexName(indexName)) {
                    LOG.error(Response.Status.FORBIDDEN.name() + ": indexname '" + indexName + "' contain illegal char.");
                    throw new OtsException(OtsErrorCode.EC_OTS_STORAGE_INDEX_NO_EXIST, Response.Status.FORBIDDEN.name() + ": indexname '" + indexName + "' contain illegal char.");
                }

                IndexInfoBody responseBody = IndexService.getIndexInfo(userInfo, tableName, indexName);
                return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(responseBody).build();
            }

        } catch (OtsException e) {
            e.printStackTrace();
            return Response.status(e.getErrorCode() == OtsErrorCode.EC_OTS_PERMISSION_NO_PERMISSION_FAULT ? Response.Status.FORBIDDEN : Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(e.getErrorCode(), e.getMessage())).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(500L, e.getMessage())).build();
        }
    }



}
