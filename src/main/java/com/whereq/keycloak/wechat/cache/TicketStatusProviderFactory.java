package com.whereq.keycloak.wechat.cache;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.storage.UserStorageProviderFactory;

public class TicketStatusProviderFactory implements UserStorageProviderFactory<TicketStatusProvider> {
    @Override
    public TicketStatusProvider create(KeycloakSession keycloakSession, ComponentModel componentModel) {
        return new TicketStatusProvider(keycloakSession, componentModel);
    }

    @Override
    public String getId() {
        return "TicketStatusProvider";
    }
}
