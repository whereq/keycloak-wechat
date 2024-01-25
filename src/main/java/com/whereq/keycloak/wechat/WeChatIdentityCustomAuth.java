package com.whereq.keycloak.wechat;

import org.keycloak.broker.oidc.AbstractOAuth2IdentityProvider;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.util.SimpleHttp;
import org.keycloak.broker.social.SocialIdentityProvider;
import org.keycloak.models.KeycloakSession;

import java.io.IOException;

import static com.whereq.keycloak.wechat.UserAgentHelper.isWechatBrowser;

public class WeChatIdentityCustomAuth extends AbstractOAuth2IdentityProvider<OAuth2IdentityProviderConfig>
        implements SocialIdentityProvider<OAuth2IdentityProviderConfig> {

    private final WeChatIdentityProvider weChatIdentityProvider;
    public String accessToken;

    public WeChatIdentityCustomAuth(KeycloakSession session, OAuth2IdentityProviderConfig config, WeChatIdentityProvider weChatIdentityProvider) {
        super(session, config);
        this.weChatIdentityProvider = weChatIdentityProvider;
    }

    // TODO: cache mechanism
    public String getAccessToken(WeChatLoginType wechatLoginType) throws IOException {
        logger.info("getAccessToken with " + wechatLoginType);

        var clientId = this.getConfig().getClientId();
        var clientSecret = this.getConfig().getClientSecret();

        try {
            String ua = session.getContext().getRequestHeaders().getHeaderString("user-agent").toLowerCase();
            logger.info("ua = " + ua);

            if (!isWechatBrowser(ua) || WeChatLoginType.FROM_PC_QR_CODE_SCANNING.equals(wechatLoginType)) {
                logger.info("not wechat browser or from pc qr code scanning");

                clientId = this.getConfig().getConfig().get(WeChatIdentityProvider.WECHAT_MP_APP_ID);
                clientSecret = this.getConfig().getConfig().get(WeChatIdentityProvider.WECHAT_MP_APP_SECRET);
            }
        } catch (Exception ex) {
            logger.error("获取 user-agent 失败");
            logger.error(ex);
        }

        logger.info(String.format("getAccessToken by %s%n%s%n", clientId, clientSecret));
        var res =
                SimpleHttp.doGet(String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential" +
                                "&appid=%s&secret=%s", clientId, clientSecret),
                        this.session).asString();

        logger.info(String.format("res is %s%n", res));
        var accessToken = this.extractTokenFromResponse(res, "access_token");
//        var expiresIn = this.extractTokenFromResponse(res, "expires_in");

        this.accessToken = accessToken;
        return accessToken;
    }

    @Override
    protected String getDefaultScopes() {
        return null;
    }

    public BrokeredIdentityContext auth(String openid, WeChatLoginType wechatLoginType) throws IOException {
        var accessToken = getAccessToken(wechatLoginType);

        var profile = SimpleHttp.doGet(String.format("https://api.weixin.qq.com/cgi-bin/user/info?access_token=%s&openid" +
                "=%s&lang=zh_CN", accessToken, openid), this.session).asJson();

        System.out.println("profile is " + profile);

        var context = this.weChatIdentityProvider.extractIdentityFromProfile(null, profile);
        context.getContextData().put(FEDERATED_ACCESS_TOKEN, accessToken);

        return context;
    }
}
