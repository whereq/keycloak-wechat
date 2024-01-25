package com.whereq.keycloak.wechat;

public class UserAgentHelper {
    public static boolean isWechatBrowser(String ua) {
        return ua.contains(WeChatIdentityProvider.WECHATFLAG);
    }
}
