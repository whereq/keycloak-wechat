package com.whereq.keycloak.wechat.egress.wechat.mp.models;

public class AccessTokenRequestBody {
    public String grant_type;
    public String appid;
    public String secret;

    public AccessTokenRequestBody(String grant_type, String appid, String secret) {
        this.grant_type = grant_type;
        this.appid = appid;
        this.secret = secret;
    }
}
