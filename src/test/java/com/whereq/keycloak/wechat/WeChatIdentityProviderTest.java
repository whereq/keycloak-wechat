package com.whereq.keycloak.wechat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.core.Response;
import org.junit.*;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.keycloak.broker.oidc.OAuth2IdentityProviderConfig;
import org.keycloak.broker.provider.AuthenticationRequest;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.broker.provider.IdentityProvider;
import org.keycloak.broker.provider.util.IdentityBrokerState;
import com.whereq.keycloak.wechat.mock.MockedAuthenticationSessionModel;
import com.whereq.keycloak.wechat.mock.MockedHttpRequest;

import java.util.Map;
import java.util.UUID;

import com.whereq.keycloak.wechat.mock.MockedKeycloakSession;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.CoreMatchers.containsString;

@RunWith(PowerMockRunner.class)
@PrepareForTest({UUID.class, WeChatIdentityProvider.class})
public class WeChatIdentityProviderTest {
    WeChatIdentityProvider weChatIdentityProvider;

    @Before
    public void before() {
        UUID uuid = PowerMockito.mock(UUID.class);
        Mockito.when(uuid.toString()).thenReturn("ccec3eea-fd08-4ca2-b83a-2921228f2480");

        PowerMockito.mockStatic(UUID.class);
        PowerMockito.when(UUID.randomUUID()).thenReturn(uuid);

        OAuth2IdentityProviderConfig config = new OAuth2IdentityProviderConfig();
        config.setClientId("clientId");
        weChatIdentityProvider = new WeChatIdentityProvider(null, config);
    }

    @Test
    public void performLoginThrowsIfHttpRequestIsNull() {
        try {
            AuthenticationRequest request = new AuthenticationRequest(null, null, null, null, null, null, null);

            weChatIdentityProvider.performLogin(request);
        } catch (RuntimeException ex) {
            Assert.assertThat(ex.toString(), containsString("org.keycloak.broker.provider.IdentityBrokerException: Could not create authentication request because java.lang.NullPointerException"));
        }
    }

    @Test
    public void pcGoesToQRConnect() {
        IdentityBrokerState state = IdentityBrokerState.decoded("state", "clientId", "clientId", "tabId");
        var authSession = new MockedAuthenticationSessionModel();

        org.keycloak.http.HttpRequest httpRequest = new MockedHttpRequest();
        AuthenticationRequest request = new AuthenticationRequest(new MockedKeycloakSession(httpRequest), null, authSession, httpRequest, null, state, "https" +
                "://redirect.to.customized/url");

        var res = weChatIdentityProvider.performLogin(request);

        Assert.assertEquals("303 redirect", Response.Status.SEE_OTHER.getStatusCode(), res.getStatus());
        Assert.assertEquals("pc goes to customized login url", "https://open.weixin.qq" +
                ".com/connect/qrconnect?scope=snsapi_login&state=state.tabId" +
                ".clientId&appid=clientId&redirect_uri=https%3A%2F%2Fredirect.to" +
                ".customized%2Furl&nonce=ccec3eea-fd08-4ca2-b83a-2921228f2480", res.getLocation().toString());
    }

    @Test
    public void pcGoesToCustomizedURLIfPresent() {
        var config = new WeChatIdentityProviderConfig();
        config.setClientId("clientId");
        config.setClientId2(WeChatIdentityProvider.WECHAT_MP_APP_ID);
        config.setCustomizedLoginUrlForPc("https://another.url/path");

        Assert.assertEquals("set config get config", "https://another.url/path", config.getCustomizedLoginUrlForPc());

        weChatIdentityProvider = new WeChatIdentityProvider(null, config);

        IdentityBrokerState state = IdentityBrokerState.decoded("state", "clientId", "clientId", "tabId");
        var authSession = new MockedAuthenticationSessionModel();

        org.keycloak.http.HttpRequest httpRequest = new MockedHttpRequest();
        AuthenticationRequest request = new AuthenticationRequest(new MockedKeycloakSession(httpRequest), null, authSession, httpRequest, null, state,
                "https" +
                        "://redirect.to.customized/url");

        var res = weChatIdentityProvider.performLogin(request);

        Assert.assertEquals("303 redirect", Response.Status.SEE_OTHER.getStatusCode(), res.getStatus());
        Assert.assertTrue("pc goes to customized login url", res.getLocation().toString().startsWith("https://another.url/path"));
    }

    @org.junit.jupiter.api.Test
    void getFederatedIdentityForWMP() throws JsonProcessingException {
        var mockSessionKey = "n1HE228Kq\\/i3HRlz\\/K71Aw==";
        var mockUserInfo = Map.of("session_key", mockSessionKey);

        var mockAccessToken = "54_XzDD7MVKpVBX5m-VtsjAE9tyImVxUSKE2VgOzEBDemngNCAVwFfPr3RNusGjcBrZl2CPyQoONP4kqUI24Wl1KYZO-ZC2emmLR1bZfUPoH2FXd5iz780ZTOhb3lkDjK8zS0n31JdhXPwtPaqVDKHeAAAMTQ";
        var expectedContextData = Map.of(IdentityProvider.FEDERATED_ACCESS_TOKEN, mockAccessToken, "UserInfo", mockUserInfo);

        final String sessionKeyResponse = "{\"session_key\":\"n1HE228Kq\\/i3HRlz\\/K71Aw==\",\"openid\":\"odrHN4p1UMWRdQfMK4xm9dtQXvf8\",\"unionid\":\"oLLUdsyyVLcjdxFXiOV2pZYuOdR0\"}";
        var expectedJsonProfile = new ObjectMapper().readTree(sessionKeyResponse);

        var config = new WeChatIdentityProviderConfig();
        config.setWmpClientId("123456");

        var sut = new WeChatIdentityProvider(null, config);

        var expectedUser = new BrokeredIdentityContext(sut.getJsonProperty(expectedJsonProfile, "unionid"));
        expectedUser.setUsername(sut.getJsonProperty(expectedJsonProfile, "openid"));
        expectedUser.setEmail("null");

        var res = sut.getFederatedIdentity(sessionKeyResponse, WeChatLoginType.FROM_WECHAT_MINI_PROGRAM, "{\"access_token\":\"" + mockAccessToken + "\",\"expires_in\":7200}");
        var contextData = res.getContextData();
        Assertions.assertNotNull(contextData);
        Assertions.assertEquals(expectedContextData.get(IdentityProvider.FEDERATED_ACCESS_TOKEN), contextData.get(IdentityProvider.FEDERATED_ACCESS_TOKEN));
        Assertions.assertEquals(expectedUser.toString(), res.toString());
    }
}
