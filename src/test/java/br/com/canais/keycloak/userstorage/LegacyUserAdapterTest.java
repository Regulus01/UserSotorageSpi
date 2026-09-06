package br.com.canais.keycloak.userstorage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.component.ComponentModel;
import org.keycloak.storage.ReadOnlyException;
import org.keycloak.storage.StorageId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LegacyUserAdapterTest {

    private static final String PROVIDER_ID = "legacy-sql-user-storage";
    private static final String EXTERNAL_ID = "123";
    private static final String EMAIL = "usuario@teste.com";
    private static final String PASSWORD = "senha";

    private LegacyUserAdapter adapter;

    @BeforeEach
    void setUp() {
        ComponentModel storageProviderModel = new ComponentModel();
        storageProviderModel.setId(PROVIDER_ID);

        adapter = new LegacyUserAdapter(
                null,
                null,
                storageProviderModel,
                new UserEntity(EXTERNAL_ID, EMAIL, PASSWORD));
    }

    @Test
    void deveRetornarOIdKeycloakCompostoPeloProviderEIdExterno() {
        assertEquals(EXTERNAL_ID, StorageId.externalId(adapter.getId()));
    }

    @Test
    void deveRetornarOsDadosDoUsuarioLegado() {
        assertEquals(EMAIL, adapter.getUsername());
        assertEquals(EMAIL, adapter.getEmail());
        assertEquals(PASSWORD, adapter.getPasswordHash());
    }

    @Test
    void deveConsiderarOEmailVerificado() {
        assertTrue(adapter.isEmailVerified());
    }

    @Test
    void naoDevePermitirAlterarOUsuario() {
        assertThrows(ReadOnlyException.class, () -> adapter.setUsername("outro@teste.com"));
    }
}
