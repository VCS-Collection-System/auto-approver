package org.rh;

import java.util.HashMap;
import java.util.Map;

import io.quarkus.test.junit.QuarkusTestProfile;

public class UnitTestProfile implements QuarkusTestProfile{
    
    @Override
    public Map<String, String> getConfigOverrides() {
        
        Map<String, String> conf = new HashMap<>();
        conf.put("approval-service.api-key","mock-api-key");
        conf.put("pam/mp-rest/url","https://localhost");
        conf.put("eagle-eye/mp-rest/url","https://localhost");
        conf.put("eagle-eye-auth/mp-rest/url","https://localhost");
        conf.put("pam.signal","EESignal");
        conf.put("quarkus.oidc-client.auth-server-url","${keycloak.url}");
        conf.put("quarkus.oidc-client.discovery-enabled","false");
        conf.put("quarkus.oidc-client.token-path","/tokens");
        conf.put("quarkus.oidc-client.client-id","quarkus-service-app");
        conf.put("quarkus.oidc-client.client-id","quarkus-service-app");
        conf.put("quarkus.oidc-client.credentials.secret","secret");
        conf.put("quarkus.oidc-client.grant.type","password");
        conf.put("quarkus.oidc-client.grant-options.password.username","alice");
        conf.put("quarkus.oidc-client.grant-options.password.password","alice");

        return conf;
    }
}
