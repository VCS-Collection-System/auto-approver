package org.rh.rest;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.rh.client.PamService;
import org.rh.client.model.PamModelWrapper;
import org.rh.client.model.PamSignalPayload;
import org.rh.rest.model.AutoApprovalResult;

import io.quarkus.logging.Log;
import io.quarkus.oidc.client.Tokens;

// This endpoint is intended for the Eagle Eye service to leverage
// to POST results back into our system
@Path("/results")
public class ResultsResource {
    
    @ConfigProperty(name="approval-service.api-key")
    String validApiKey;

    @ConfigProperty(name="pam/mp-rest/url")
    String pamContainerUrl;
    
    @ConfigProperty(name="pam.signal")
    String signal;

    @Inject
    Tokens tokens;

    // Inject our PAM client
    @RestClient
    @Inject
    PamService pam;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Counted(name = "eagleEyeResults", description = "How many results have been posted to the auto-approval service.")
    @Timed(name = "eagleEyeResultsTimer", description = "A measure of how long it takes to submit a result.", unit = MetricUnits.MILLISECONDS)
    public Response results(AutoApprovalResult req, @HeaderParam("API-Key") String apiKey) {

        if (apiKey == null) {
            Log.error("API-Key header not set");
            return Response.serverError().entity("API-Key header must be set").build();
        }
        if (!apiKey.equals(validApiKey))
        {
            Log.error("API-Key header value is invalid");
            return Response.serverError().entity("API-Key header value is invalid").build();
        }

        Log.info("Received results from Eagle Eye:");
        Log.info("correlationId: " + req.userId);
        Log.info("confidenceScore: "+req.confidenceScore);
        
        PamSignalPayload payload = new PamSignalPayload();
        payload.wrapper = new PamModelWrapper();
        payload.wrapper.confidenceScore = req.confidenceScore;
        payload.wrapper.report = req.report;

        Log.debug("payload created");
        Log.info("Calling PAM");

        try {
            Log.debug("PAM Conatiner Url: " + pamContainerUrl);
            Response response = pam.submitResults(payload, req.userId, signal);

            if(response.getStatus() == 200)
            {
                Log.info("PAM call succeeded");
                return Response.ok("Success", MediaType.TEXT_PLAIN).build();
            }
            Log.error("PAM call failed");
            return Response.serverError().entity(response.getEntity()).build();

        } catch (ClientWebApplicationException e) {
            
            // gracefully handle exceptions
            Log.error("PAM call failed: " + e.getMessage());
            return Response.serverError().entity(e.getMessage()).build();
        }              
    }
}
