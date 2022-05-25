package org.rh.client;

import java.net.URI;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.rh.client.model.EagleEyeAuthRequest;
import org.rh.client.model.EagleEyeAuthResponse;
import org.rh.client.model.EagleEyeProofResponse;
import org.rh.client.model.EagleEyeProofsRequest;


import org.jboss.logging.Logger;

@RegisterRestClient(configKey="eagle-eye")
@ApplicationScoped
public interface EagleEyeService {
    
    // NOTE: The Eagle Eye auth endpoint requires a custom X-Tenant header to be passed along. Due to this
    // requirement we cannot use the @OidcClientFilter below. An issue was raised with the Quarkus team, and
    // they are adding support for custom headers in the near future (2.4.0.Final possibly, but if it misses
    // that release, then they are targetting 2.4.1.Final).
    // This whole getToken method can then be replaced by the @OidcClientFilter annotation plus some config
    // to add the custom header. Ex: quarkus.oidc-client.headers.X-Tenant="${eagle-eye.tenant-id}" 
    default String getToken(String headerName) {

        EagleEyeAuthService eeAuth = RestClientBuilder.newBuilder()
            .baseUri(URI.create(ConfigProvider.getConfig().getValue("eagle-eye-auth/mp-rest/url", String.class)))
            .build(EagleEyeAuthService.class);

        String grant_type = ConfigProvider.getConfig().getValue("eagle-eye-auth.grant.type", String.class);
        String client_id = ConfigProvider.getConfig().getValue("eagle-eye-auth.client-id", String.class);
        String client_secret = ConfigProvider.getConfig().getValue("eagle-eye-auth.credentials.secret", String.class);

        if ("Authorization".equals(headerName)) {
            EagleEyeAuthRequest authRequest = new EagleEyeAuthRequest();
            authRequest.client_id = client_id;
            authRequest.client_secret = client_secret;
            authRequest.grant_type = grant_type;

            Logger log = Logger.getLogger(EagleEyeService.class);

            log.info("Calling Auth Service ");

            EagleEyeAuthResponse authResponse = eeAuth.getToken(authRequest);

            // Uncomment if you need to see the Bearer token in the logs
            //log.info("Auth Token: Bearer " + authResponse.access_token);  // NOSONAR
            log.info("Auth Token obtained");

            return "Bearer " + authResponse.access_token;    
        }
        throw new UnsupportedOperationException("unknown header name");
    }

    // Client endpoint definition for POSTing to Eagle Eye /proofs endpoint
    // Must pass Authorization and X-Tenant headers
    // See above NOTE on why we are using a default method to calculate the Bearer token instead of
    // using @OidcClientFilter
    @POST
    @Path("/proofs")
    @ClientHeaderParam(name = "Authorization", value = "{getToken}")
    @ClientHeaderParam(name = "X-Tenant", value = "${eagle-eye.tenant-id}")
    EagleEyeProofResponse submitProof(EagleEyeProofsRequest req);
  
}
