package de.adorsys.ledgers.token.exchange;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resource.RealmResourceProviderFactory;

/**
 * @author Lorent Lempereur
 */
public class ConfigurableTokenResourceProviderFactory implements RealmResourceProviderFactory {

    private static final Logger LOG = Logger.getLogger(ConfigurableTokenResourceProviderFactory.class);

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        ConfigurationTokenResourceConfiguration configuration = ConfigurationTokenResourceConfiguration.readFromEnvironment();
        LOG.infof("Keycloak-ConfigurableToken is configured with: %s", configuration);
        return new ConfigurableTokenResourceProvider(session, configuration);
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

    @Override
    public String getId() {
        return ConfigurableTokenResourceProvider.ID;
    }
}
