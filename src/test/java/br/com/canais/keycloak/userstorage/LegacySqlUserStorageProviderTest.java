package br.com.canais.keycloak.userstorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.credential.PasswordCredentialModel;

import java.lang.reflect.Proxy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacySqlUserStorageProviderTest {

    private static final String STORED_PASSWORD_HASH = "hash-da-senha";

    private LegacySqlUserStorageProvider provider;
    private LegacyUserAdapter user;

    @BeforeEach
    void setUp() {
        ComponentModel storageProviderModel = new ComponentModel();
        storageProviderModel.setId("legacy-sql-user-storage");

        provider = new LegacySqlUserStorageProvider(sessionProxy(), storageProviderModel);
        user = new LegacyUserAdapter(
                sessionProxy(),
                null,
                storageProviderModel,
                new UserEntity("123", "usuario@teste.com", STORED_PASSWORD_HASH));
    }

    @Test
    void deveAceitarSenhaQuandoOHashInformadoForIgualAoArmazenado() {
        assertTrue(provider.isValid(
                null,
                user,
                credentialInput(PasswordCredentialModel.TYPE, STORED_PASSWORD_HASH)));
    }

    @Test
    void deveRejeitarSenhaQuandoOHashInformadoForDiferente() {
        assertFalse(provider.isValid(
                null,
                user,
                credentialInput(PasswordCredentialModel.TYPE, "hash-incorreto")));
    }

    @Test
    void deveRejeitarCredencialDeTipoNaoSuportado() {
        assertFalse(provider.isValid(
                null,
                user,
                credentialInput("otp", STORED_PASSWORD_HASH)));
    }

    @Test
    void deveRejeitarRespostaNula() {
        assertFalse(provider.isValid(
                null,
                user,
                credentialInput(PasswordCredentialModel.TYPE, null)));
    }

    private CredentialInput credentialInput(String type, String challengeResponse) {
        return new CredentialInput() {
            @Override
            public String getCredentialId() {
                return null;
            }

            @Override
            public String getType() {
                return type;
            }

            @Override
            public String getChallengeResponse() {
                return challengeResponse;
            }
        };
    }

    private KeycloakSession sessionProxy() {
        return (KeycloakSession) Proxy.newProxyInstance(
                KeycloakSession.class.getClassLoader(),
                new Class<?>[]{KeycloakSession.class},
                (proxy, method, args) -> null);
    }
}
