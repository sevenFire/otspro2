package com.lyh.sample.rest.util;

import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;

/**
 * @author liyuhui
 * @date 2018/12/25
 * @description
 */
public class JsonUtil {
    private static final String DEFAULT_ENCODING = "UTF-8";

    private JsonUtil() {}

    public static <T> String toJsonString(T t) {
        return toJsonString(t, t.getClass());
    }

    public static <T> String toJsonString(T t, Type generic) {
        JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(baos);
            provider.writeTo(t, t.getClass(), generic, new Annotation[0], MediaType.APPLICATION_JSON_TYPE, null, bos);
            return baos.toString(DEFAULT_ENCODING);
        } catch (IOException e) {
            throw new RuntimeException("Serialize to JSON failed.", e);
        }
    }

    /**
     * JAXBAnnotation and JacksonAnnotation supported.
     *
     * @param is InputStream
     * @param type Class type. Generic type supported.
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T readJsonFromStream(InputStream is, Type genericType) {
        JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        try {
            return (T) provider.readFrom(Object.class, genericType, new Annotation[0], MediaType.APPLICATION_JSON_TYPE, null, is);
        } catch (Exception e) {
            throw new RuntimeException("Deserialize from JSON failed.", e);
        }
    }
}
