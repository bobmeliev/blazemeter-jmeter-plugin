package com.blazemeter.jmeter.upload;

import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Vitali
 * Date: 7/15/13
 * Time: 15:25
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Sample {
    public Long na;
    public Long by;
    public String de;
    public String dt;
    public Long t;
    public Long ec;
    public Long ng;
    public String hn;
    public Long it;
    public String lb;
    public Long lt;
    public String rc;
    public String rm;
    public Long sc;
    public Boolean s;
    public String tn;
    public Long ts;

    @JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
    public String url;
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
    public ArrayList<Sample> ch = new ArrayList<>();
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
    public ArrayList<AssertionResult> as = new ArrayList<>();
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_EMPTY)
    public String ck;//cookies

    @Override
    public String toString() {
        return "Sample{" +
                "na=" + na +
                ", by=" + by +
                ", de='" + de + '\'' +
                ", dt='" + dt + '\'' +
                ", t=" + t +
                ", ec=" + ec +
                ", ng=" + ng +
                ", hn='" + hn + '\'' +
                ", it=" + it +
                ", lb='" + lb + '\'' +
                ", lt=" + lt +
                ", rc='" + rc + '\'' +
                ", rm='" + rm + '\'' +
                ", sc=" + sc +
                ", s=" + s +
                ", tn='" + tn + '\'' +
                ", ts=" + ts +
                ", url='" + url + '\'' +
                ", as=" + as.toString() +
                ", ch=" + ch.toString() +
                ", ck='" + ck + '\'' +
                ", rq='" + rq + '\'' +
                ", m='" + m + '\'' +
                ", qs='" + qs + '\'' +
                ", rd='" + rd + '\'' +
                ", rf='" + rf + '\'' +
                ", rh='" + rh + '\'' +
                ", tg='" + tg + '\'' +
                '}';
    }

    public String rq;// self._get_request_headers(elem),
    public String m;//method elem.findtext('method', ''),
    public String qs;// elem.findtext('queryString', ''),
    public String rd;//': elem.findtext('responseData', ''),
    public String rf;//': elem.findtext('responseFile', ''),
    public String rh;//': self._get_response_headers(elem),
    public String tg;//': elem.tag,

    public Sample() {

    }
}
