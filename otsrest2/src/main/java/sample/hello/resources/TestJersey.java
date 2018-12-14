package sample.hello.resources;

import com.baosight.xinsight.ots.OtsErrorCode;


import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import sample.hello.constants.ErrorMode;
import sample.hello.exception.OtsException;
import sample.hello.model.TableCreateBody;

/**
 * @author liyuhui
 * @date {DATE}
 * @description
 */

@Path("/hello")
public class TestJersey {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String sayHello() {
        return "Hello Jersey";
    }

    @Path("/json")
    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    public Map sayHelloJson() {
        Map<String, String> map = new HashMap<>();
        map.put("abc", "def");
        return map;
    }

    @Path("/jsonp")
    @POST
    @Consumes({MediaType.TEXT_PLAIN, MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON })
    public Response sayHelloJsonp(String body) {
        TableCreateBody bodyModel;

        try {
            bodyModel = TableCreateBody.toClass(body);
        } catch (OtsException e) {
            e.printStackTrace();
            return Response.status(e.getErrorCode() == OtsErrorCode.EC_OTS_PERMISSION_NO_PERMISSION_FAULT ? Response.Status.FORBIDDEN :Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(e.getErrorCode(), e.getMessage())).build();
        }
        return Response.status(Response.Status.CREATED).type(MediaType.APPLICATION_JSON).entity(new ErrorMode(0L)).build();
    }
}
