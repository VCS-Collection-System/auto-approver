package org.rh.client;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.eclipse.microprofile.rest.client.annotation.ClientHeaderParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.rh.client.model.EagleEyeAuthRequest;
import org.rh.client.model.EagleEyeAuthResponse;


@RegisterRestClient(configKey="eagle-eye-auth")
@ApplicationScoped
public interface EagleEyeAuthService {

    // Client endpoint definition for the POST to the /token endpoint
    // Must pass X-Tenant header
    // This whole interface can go away once we switch to using the @OidcClientFitler (see NOTE in EagleEyeService.java) 
    @POST
    @Path("/token")
    @ClientHeaderParam(name = "X-Tenant", value = "${eagle-eye.tenant-id}")
    EagleEyeAuthResponse getToken(EagleEyeAuthRequest req);
}
