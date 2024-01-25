package com.whereq.keycloak.wechat.helpers;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WMPHelperTest {

    @Test
    void createStateForWMP() {
        assertEquals("wmp.tab.client", WMPHelper.createStateForWMP("client", "tab"));
    }
}
