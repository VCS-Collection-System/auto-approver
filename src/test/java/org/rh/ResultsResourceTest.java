package org.rh;

import static io.restassured.RestAssured.given;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;



import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.rh.client.PamService;
import org.rh.client.model.PamSignalPayload;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.response.Response;


@QuarkusTest
@QuarkusTestResource(KeycloakRealmResourceManager.class)
@TestProfile(UnitTestProfile.class)
public class ResultsResourceTest {

    @Inject
    @InjectMock
    @RestClient
    PamService mockPamService;

    private static String requestBody = "{\n" +
        "   \"userId\": \"42\",\n" +
        "   \"confidenceScore\": 85,\n" +
        "   \"hardFail\": false," +
        "   \"report\": \"data:image/pdf;base64,mock report data\"\n" +
        "}";

    @Test
    public void TestResultsEndpointHappyPath() {
        
        Mockito.when(mockPamService.submitResults(any(PamSignalPayload.class),anyString(),anyString())).thenReturn(javax.ws.rs.core.Response.ok().build());

        Response response = given()
                .header("Content-type", "application/json")
                .header("API-Key","mock-api-key")
                .and()
                .body(requestBody)
            .when().post("/results")
            .then()
                .extract().response();

        Assertions.assertEquals(200, response.statusCode());

    }

    @Test
    public void TestResultsEndpointPamFailure() {
        
        Mockito.when(mockPamService.submitResults(any(PamSignalPayload.class),anyString(),anyString())).thenReturn(javax.ws.rs.core.Response.status(400).build());

        Response response = given()
                .header("Content-type", "application/json")
                .header("API-Key","mock-api-key")
                .and()
                .body(requestBody)
            .when().post("/results")
            .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());

    }

    @Test
    public void TestResultsEndpointApiKeyMissing() {
        
        Mockito.when(mockPamService.submitResults(any(PamSignalPayload.class),anyString(),anyString())).thenReturn(javax.ws.rs.core.Response.ok().build());

        Response response = given()
                .header("Content-type", "application/json")
                .and()
                .body(requestBody)
            .when().post("/results")
            .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals("API-Key header must be set", response.getBody().asString());

    }

    @Test
    public void TestResultsEndpointApiKeyWrong() {
        
        Mockito.when(mockPamService.submitResults(any(PamSignalPayload.class),anyString(),anyString())).thenReturn(javax.ws.rs.core.Response.ok().build());

        Response response = given()
                .header("Content-type", "application/json")
                .header("API-Key","wrong-api-key")
                .and()
                .body(requestBody)
            .when().post("/results")
            .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals("API-Key header value is invalid", response.getBody().asString());

    }

    @Test
    public void TestResultsEndpointHandleCWECallingPam() {
        
        Mockito.when(mockPamService.submitResults(any(PamSignalPayload.class),anyString(),anyString())).thenThrow(new ClientWebApplicationException());

        Response response = given()
                .header("Content-type", "application/json")
                .header("API-Key","mock-api-key")
                .and()
                .body(requestBody)
            .when().post("/results")
            .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
        Assertions.assertEquals("HTTP 500 Internal Server Error", response.getBody().asString());

    }



}
