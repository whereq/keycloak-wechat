package com.whereq.keycloak.wechat;

import jakarta.ws.rs.core.Response;
import org.keycloak.services.managers.ClientSessionCode;
import org.keycloak.sessions.AuthenticationSessionModel;

public class ParsedCodeContext {
    public ClientSessionCode<AuthenticationSessionModel> clientSessionCode;
    public Response response;

    public static ParsedCodeContext clientSessionCode(ClientSessionCode<AuthenticationSessionModel> clientSessionCode) {
        ParsedCodeContext ctx = new ParsedCodeContext();
        ctx.clientSessionCode = clientSessionCode;
        return ctx;
    }

    public static ParsedCodeContext response(Response response) {
        ParsedCodeContext ctx = new ParsedCodeContext();
        ctx.response = response;
        return ctx;
    }
}
