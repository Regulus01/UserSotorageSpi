package br.com.canais.keycloak.userstorage;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.UserModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.RoleModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.keycloak.models.RealmModel;
import org.jboss.logging.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

public final class LegacySqlUserStorageProvider
        implements UserStorageProvider, UserLookupProvider, UserQueryProvider,
        CredentialInputValidator {

    private static final Logger LOG = Logger.getLogger(LegacySqlUserStorageProvider.class);

    private static final String FIND_BY_EMAIL = """
            SELECT u.Usu_Id, u.Usu_Email, u.Usu_Password
            FROM authentication.Usuario u
            WHERE u.Usu_Email = ?
            """;
    private static final String FIND_BY_ID = """
            SELECT u.Usu_Id, u.Usu_Email, u.Usu_Password
            FROM authentication.Usuario u
            WHERE u.Usu_Id = ?
            """;

    private final KeycloakSession session;
    private final ComponentModel model;

    public LegacySqlUserStorageProvider(KeycloakSession session, ComponentModel model) {
        this.session = Objects.requireNonNull(session);
        this.model = Objects.requireNonNull(model);
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        LOG.infof("Consultando usuário legado por username: %s", username);
        return findUser(realm, FIND_BY_EMAIL, username);
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        LOG.infof("Consultando usuário legado por e-mail: %s", email);
        return findUser(realm, FIND_BY_EMAIL, email);
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        String externalId = StorageId.externalId(id);
        LOG.infof("Consultando usuário legado por ID: keycloakId=%s, externalId=%s", id, externalId);
        return findUser(realm, FIND_BY_ID, externalId);
    }

    private UserModel findUser(RealmModel realm, String sql, String value) {
        LOG.infof("Executando consulta de usuário legado com parâmetro: %s", value);
        try (Connection connection = openConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, value);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    LOG.infof("Nenhum usuário legado encontrado para: %s", value);
                    return null;
                }
                UserEntity entity = new UserEntity(
                        result.getString("Usu_Id"),
                        result.getString("Usu_Email"),
                        result.getString("Usu_Password"));
                LOG.infof("Usuário legado encontrado: externalId=%s, email=%s",
                        entity.externalId(), entity.email());
                return new LegacyUserAdapter(
                        session,
                        realm,
                        model,
                        entity);
            }
        } catch (SQLException exception) {
            LOG.infof(exception, "Falha na consulta do usuário legado para: %s", value);
            throw new IllegalStateException("Falha ao consultar authentication.Usuario.", exception);
        }
    }

    private Connection openConnection() throws SQLException {
        String jdbcUrl = requiredConfig("jdbcUrl");
        String jdbcUser = requiredConfig("jdbcUser");
        LOG.infof("Abrindo conexão com o banco legado: url=%s, user=%s", jdbcUrl, jdbcUser);
        Connection connection = DriverManager.getConnection(
                jdbcUrl,
                jdbcUser,
                requiredConfig("jdbcPassword"));
        LOG.info("Conexão com o banco legado aberta.");
        return connection;
    }

    private String requiredConfig(String name) {
        String value = model.get(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Configuração obrigatória ausente: " + name);
        }
        return value;
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        return supportsCredentialType(credentialType)
                && user instanceof LegacyUserAdapter;
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        if (!supportsCredentialType(input.getType())
                || !(user instanceof LegacyUserAdapter adapter)
                || input.getChallengeResponse() == null) {
            LOG.info("Validação de credencial rejeitada por tipo, usuário ou resposta inválida.");
            return false;
        }

        boolean valid = input.getChallengeResponse().equals(adapter.getPasswordHash());
        LOG.infof("Validação de senha do usuário legado: externalId=%s, resultado=%s",
                adapter.getId(), valid);
        return valid;
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String, String> params,
                                                  Integer firstResult, Integer maxResults) {
        return Stream.empty();
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm,
                                                                  String attrName,
                                                                  String attrValue) {
        return Stream.empty();
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group,
                                                    Integer firstResult, Integer maxResults) {
        return Stream.empty();
    }

    @Override
    public Stream<UserModel> getRoleMembersStream(RealmModel realm, RoleModel role,
                                                   Integer firstResult, Integer maxResults) {
        return Stream.empty();
    }

    @Override
    public int getUsersCount(RealmModel realm) {
        return 0;
    }

    @Override
    public void preRemove(RealmModel realm) {
    }

    @Override
    public void preRemove(RealmModel realm, GroupModel group) {
    }

    @Override
    public void preRemove(RealmModel realm, RoleModel role) {
    }

    @Override
    public void close() {
    }
}
