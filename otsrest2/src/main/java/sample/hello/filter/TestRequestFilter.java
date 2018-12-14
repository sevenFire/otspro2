package sample.hello.filter;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

/**
 * @author liyuhui
 * @date {DATE}
 * @description
 */
public class TestRequestFilter implements ContainerRequestFilter {

    @Context
    private HttpServletRequest servletRequest;

    @Override
    public ContainerRequest filter(ContainerRequest containerRequest) {
//        System.out.println("TestRequestFilter Request:" + containerRequest);
//        if(!servletRequest.getRequestURL().toString().endsWith("/login") && !servletRequest.getMethod().equals("OPTIONS")){
//            Response response = Response.ok(new ErrorJSON(99, "error")).status(401).type(MediaType.APPLICATION_JSON).build();
//            throw new WebApplicationException(response); // Throw new UnAuthorized
//        }
        return containerRequest;
    }
}
