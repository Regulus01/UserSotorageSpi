package br.com.canais.keycloak.userstorage;

import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapter;

public final class LegacyUserAdapter extends AbstractUserAdapter{

    private final UserEntity entity;

    public LegacyUserAdapter(KeycloakSession session, RealmModel realm, ComponentModel storageProviderModel, UserEntity entity) {
        super(session, realm, storageProviderModel);
        this.entity = entity;
        this.storageId = new StorageId(storageProviderModel.getId(), entity.externalId());
    }

    @Override
    public String getUsername() {
        return entity.email();
    }

    @Override
    public String getEmail() {
        return entity.email();
    }

    @Override
    public boolean isEmailVerified() {
        return true;
    }

    public String getPasswordHash() {
        return entity.password();
    }

    @Override
    public SubjectCredentialManager credentialManager() {
        return null;
    }
}
