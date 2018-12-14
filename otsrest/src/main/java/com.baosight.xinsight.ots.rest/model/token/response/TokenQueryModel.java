package com.baosight.xinsight.ots.rest.model.token.response;

import java.io.Serializable;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
/**
 * @author liyuhui
 * @date 2018/12/14
 * @description
 */
public class TokenQueryModel implements Serializable {

    @JsonIgnore
    private static final long serialVersionUID = 1L;

    @JsonProperty(value="token")
    private String token;

    @JsonProperty(value="errcode")
    private Long errcode;

    /**
     * 默认构造器
     */
    public TokenQueryModel() {}

    /**
     * 带参构造器
     * @param token
     * @param errcode
     */
    public TokenQueryModel(String token, Long errcode) {
        super();
        this.token = token;
        this.errcode = errcode;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getErrcode() {
        return errcode;
    }

    public void setErrcode(Long errcode) {
        this.errcode = errcode;
    }
}
