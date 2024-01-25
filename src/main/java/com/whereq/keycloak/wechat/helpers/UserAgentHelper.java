package com.whereq.keycloak.wechat.helpers;

import com.whereq.keycloak.wechat.WeChatIdentityProvider;

public class UserAgentHelper {
    public static boolean isWechatBrowser(String ua) {
        return ua.indexOf(WeChatIdentityProvider.WECHATFLAG) > 0;
    }
}
