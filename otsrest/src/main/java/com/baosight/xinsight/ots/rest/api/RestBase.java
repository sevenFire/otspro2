package com.baosight.xinsight.ots.rest.api;

import com.baosight.xinsight.ots.OtsErrorCode;
import com.baosight.xinsight.ots.exception.OtsException;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Context;


/**
 * @author liyuhui
 * @date 2018/12/17
 * @description
 */
public class RestBase {
    private static final Logger logger = Logger.getLogger(RestBase.class);
    @Context
    HttpServletRequest request;

    @Context
    HttpServletResponse response;


    /**
     * Get the spring application context.
     *
     * @return the applicationContext
     */

    protected JsonNode readJsonToMap() throws OtsException {
        try {
            request.setCharacterEncoding("utf-8");
        } catch (UnsupportedEncodingException e) {
            throw new OtsException(OtsErrorCode.INCORRECT_ENCODING, e);
        }


        String data = "";
        if (request.getParameter("params") != null) {
            data = request.getParameter("params");
        } else {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
                String line = null;
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }

                data = sb.toString();
                data = data.trim();
                if (!sb.toString().startsWith("{")) {
                    data = "{" + data;
                }
                if (!sb.toString().endsWith("}")) {
                    data += "}";
                }
            } catch (IOException e) {
                logger.warn(e.getMessage());
                throw new OtsException(OtsErrorCode.INCORRECT_CONTENT, e);
            }finally {
                if (br != null){
                    try {
                        br.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }


        try {
        	ObjectMapper mapper = new ObjectMapper();
        	return mapper.readTree(data); 
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            throw new OtsException(OtsErrorCode.PARSE_JSONSTRING_ERROR, ex);
        }
    }
}
