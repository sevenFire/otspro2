package com.baosight.xinsight.ots.rest.api;

import com.baosight.xinsight.model.PermissionCheckUserInfo;
import com.baosight.xinsight.ots.rest.util.PermissionUtil;

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
@Path("/record")
public class RecordResource {
    private static final Logger LOG = Logger.getLogger(RecordResource.class);

    @Context
    UriInfo uriInfo;
    @Context
    HttpServletRequest request;



    @POST
    @Path("/{tablename}")
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces(MediaType.APPLICATION_JSON)
    public Response post(@PathParam("tablename") String tableName, String body) {
        //todo lyh 对表名的校验

        PermissionCheckUserInfo userInfo = new PermissionCheckUserInfo();
        userInfo = PermissionUtil.getUserInfoModel(userInfo, request);

        if (body.length() > 1000) {
            LOG.debug("Post:" + tableName + "\n Part Content:\n" + body.substring(0, 999));
        } else {
            LOG.debug("Post:" + tableName + "\nContent:\n" + body);
        }

//        RecordListModel model = RecordListModel.toClass(body);





        return null;
    }

}
