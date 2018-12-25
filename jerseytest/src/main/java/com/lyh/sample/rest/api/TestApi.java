package com.lyh.sample.rest.api;

import com.lyh.sample.rest.model.TableColumnsBody;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author liyuhui
 * @date 2018/12/25
 * @description
 */
@Path("/test")
public class TestApi {
    @GET
    @Path("/hello")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getHello() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_JSON)
                .entity(0).build();

    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() {
        String columns = "[{\"col_name\":\"col1\"}]";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<TableColumnsBody> tableColumnsBodies = objectMapper.readValue(columns, new TypeReference<List<TableColumnsBody>>() {
            });
            System.out.println(tableColumnsBodies);
            return Response.status(Response.Status.OK).type(MediaType.APPLICATION_JSON).entity(tableColumnsBodies).build();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        String columns = "[{\"col_name\":\"col1\"}]";
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<TableColumnsBody> tableColumnsBodies = objectMapper.readValue(columns, new TypeReference<List<TableColumnsBody>>() {
            });
            System.out.println(tableColumnsBodies);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
