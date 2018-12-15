package com.baosight.xinsight.ots.rest.model.metrics.response;

import com.baosight.xinsight.ots.rest.model.table.operate.TableColumnsBody;
import com.baosight.xinsight.utils.JsonUtil;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.ArrayList;
import java.util.List;

/**
 * @author liyuhui
 * @date 2018/12/15
 * @description
 */
public class MetricsInfoListBody {
    @JsonProperty(value="errcode")
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    private Long errcode;

    @JsonProperty(value="metric_info_list")
    @JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
    private List<MetricsInfoBody> metricsInfoBodyList = new ArrayList<>();

    @JsonIgnore
    @Override
    public String toString() {
        return JsonUtil.toJsonString(this);
    }

    public MetricsInfoListBody() {
    }

    public MetricsInfoListBody(Long errcode, List<MetricsInfoBody> metricsInfoBodyList) {
        this.errcode = errcode;
        this.metricsInfoBodyList = metricsInfoBodyList;
    }

    public MetricsInfoListBody(List<MetricsInfoBody> metricsInfoBodyList) {
        this.metricsInfoBodyList = metricsInfoBodyList;
    }

    public Long getErrcode() {
        return errcode;
    }

    public void setErrcode(Long errcode) {
        this.errcode = errcode;
    }

    public List<MetricsInfoBody> getMetricsInfoBodyList() {
        return metricsInfoBodyList;
    }

    public void setMetricsInfoBodyList(List<MetricsInfoBody> metricsInfoBodyList) {
        this.metricsInfoBodyList = metricsInfoBodyList;
    }
}
