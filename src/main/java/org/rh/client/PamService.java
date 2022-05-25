package org.rh.client;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import io.quarkus.oidc.client.reactive.filter.OidcClientRequestReactiveFilter;

import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.rh.client.model.PamSignalPayload;

@RegisterRestClient(configKey="pam")
@RegisterProvider(OidcClientRequestReactiveFilter.class)
public interface PamService {
    
    @POST   
    @Path("/processes/instances/{processInstanceId}/signal/{signalName}")    
    Response submitResults(PamSignalPayload payload, String processInstanceId, String signalName);

}
