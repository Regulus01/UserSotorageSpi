package br.com.canais.keycloak.userstorage;

import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.storage.UserStorageProviderFactory;
import org.keycloak.provider.ProviderConfigProperty;

import java.util.List;

public final class LegacySqlUserStorageProviderFactory
        implements UserStorageProviderFactory<LegacySqlUserStorageProvider> {

    public static final String ID = "legacy-sql-user-storage";
    private static final List<ProviderConfigProperty> CONFIG_PROPERTIES = List.of(
            property("jdbcUrl", "JDBC URL", "SQL Server JDBC URL.", true),
            property("jdbcUser", "Database user", "SQL Server username.", true),
            property("jdbcPassword", "Database password", "SQL Server password.", true)
    );

    private static ProviderConfigProperty property(String name, String label, String helpText,
                                                    boolean secret) {
        ProviderConfigProperty property = new ProviderConfigProperty();
        property.setName(name);
        property.setLabel(label);
        property.setHelpText(helpText);
        property.setType(ProviderConfigProperty.STRING_TYPE);
        property.setSecret(secret);
        property.setRequired(true);
        return property;
    }

    @Override
    public LegacySqlUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new LegacySqlUserStorageProvider(session, model);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getHelpText() {
        return "Autentica usuarios no SQL Server legado.";
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return CONFIG_PROPERTIES;
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }
}
