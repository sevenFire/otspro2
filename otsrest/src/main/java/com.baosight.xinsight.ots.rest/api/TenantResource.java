package com.baosight.xinsight.ots.rest.api;

import com.baosight.xinsight.auth.AuthManager;
import com.baosight.xinsight.common.CommonConstants;
import com.baosight.xinsight.ots.rest.constant.ErrorMode;
import com.baosight.xinsight.ots.rest.constant.RestErrorCode;
import com.baosight.xinsight.ots.rest.model.token.response.TokenQueryModel;
import com.baosight.xinsight.ots.rest.util.ConfigUtil;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

/**
 * @author liyuhui
 * @date 2018/12/14
 * @description
 */
@Path("/")
public class TenantResource {
    private static final Logger LOG = Logger.getLogger(TenantResource.class);

    @Context
    UriInfo uriInfo;
    @Context
    HttpServletRequest request;

    /**
     * 通过用户名、密码的basic认证来获取token
     * @return
     */
    @GET
    @Path("/token")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() {
        try {
            String username = request.getAttribute(CommonConstants.SESSION_USERNAME_KEY).toString();
            String tenant = request.getAttribute(CommonConstants.SESSION_TENANT_KEY).toString();
            String password = request.getAttribute(CommonConstants.IN_SESSION_PASSWORD_KEY).toString();

            //获取token
            String token = AuthManager.login(ConfigUtil.getInstance().getAuthServerAddr(), tenant, username, password);

            //转换成json
            TokenQueryModel model = new TokenQueryModel();
            model.setToken(token);
            if (token == null) {
                model.setErrcode(RestErrorCode.EC_OTS_REST_QUERY_TOKEN);
            } else {
                model.setErrcode(0L);
            }

            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(model).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(500L, e.getMessage())).build();
        }
    }
}
