package org.rh.rest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.rh.client.EagleEyeService;
import org.rh.client.model.EagleEyeProofResponse;
import org.rh.client.model.EagleEyeProofsRequest;
import org.rh.rest.model.AutoApprovalRequest;
import org.rh.util.ApprovalRequestMapper;

import io.quarkus.logging.Log;

// This /proofs endpoint will be invoked by PAM to intiate a call out to Eagle Eye 
@Path("/proofs")
@ApplicationScoped
public class ProofsResource {
    static final String IMAGE_REGEX = "(?<=^|(.*))(\n)(?: *)\"image\"( *):( *)\"data:(.+?)\n(?:.*?)";
    static final String IMAGE_REDACTED = "\n\"image\" : <redacted>\n";
    
    @ConfigProperty(name="eagle-eye/mp-rest/url")
    String eagleEyeHost;
    
    // Inject our Eagle Eye client
    @RestClient
    @Inject
    EagleEyeService ee;

    // Inject our MapStruct object mapper
    @Inject
    ApprovalRequestMapper mapper;

    // Endpoint definition for /proofs
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Counted(name = "autoApprovalRequests", description = "How many auto approval checks have been requested.")
    @Timed(name = "autoApprovalRequestsTimer", description = "A measure of how long it takes to submit an auto approval request.", unit = MetricUnits.MILLISECONDS)
    public Response proofs(AutoApprovalRequest req) {
        
        Log.info("Outgoing Request to Eagle Eye: correlationId: " + req.correlationId);

        try {

            // Map incoming request object to the Eagle Eye request object and submit the request via our client
            EagleEyeProofsRequest eeReq = mapper.toEagleEyeProofsRequest(req);

            ObjectMapper om = new ObjectMapper();
            String json = om.writerWithDefaultPrettyPrinter().writeValueAsString(eeReq);
            json = removeImageBase64(json);
            Log.debug("\n"+json);

            Log.info("Submitting proof");
            EagleEyeProofResponse res = ee.submitProof(eeReq);

            if (!res.success) {
                // gracefully handle errors
                return Response.serverError().entity(res).build();
            }
            
            // 200 OK - and include any message from Eagle Eye
            return Response.ok(res.message).build();

        } catch (ClientWebApplicationException | JsonProcessingException e) {
            Log.info("Exception handling");
            // gracefully handle exceptions
            Log.info(e.getMessage());            
            Log.info(e.toString());

            return Response.serverError().entity(e.getMessage()).build();
        } 
    }

    /**
     * Strip out the base64-encoded image data for readability.
     * @param json
     * @return
     */
    String removeImageBase64(String json) {
        Pattern pattern = Pattern.compile(IMAGE_REGEX, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
    	Matcher matcher = pattern.matcher(json);
        String result = matcher.replaceAll(IMAGE_REDACTED);

        return result;
    }
}