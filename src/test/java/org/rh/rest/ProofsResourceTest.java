package org.rh.rest;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import javax.inject.Inject;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.rh.KeycloakRealmResourceManager;
import org.rh.UnitTestProfile;
import org.rh.client.EagleEyeAuthService;
import org.rh.client.EagleEyeService;
import org.rh.client.model.EagleEyeAuthRequest;
import org.rh.client.model.EagleEyeAuthResponse;
import org.rh.client.model.EagleEyeProofResponse;
import org.rh.client.model.EagleEyeProofsRequest;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.response.Response;


@QuarkusTest
@QuarkusTestResource(KeycloakRealmResourceManager.class)
@TestProfile(UnitTestProfile.class)
public class ProofsResourceTest {

    @Inject
    @InjectMock
    @RestClient
    EagleEyeService mockEEService;

    @Inject
    @InjectMock
    @RestClient
    EagleEyeAuthService mockEEAuthService;

    private static String requestBody = "{\n" +
        "   \"correlationId\": \"42\",\n" +
        "   \"firstName\": \"Bob\",\n" +
        "   \"lastName\": \"Smith\",\n" +
        "   \"altFirstName\": null,\n" +
        "   \"altLastName\": null,\n" +
        "   \"dob\":\"1971-12-24\",\n" +
        "   \"proofs\":[\n" +
        "   {\n" +
        "       \"type\": \"cdc\",\n" +
        "       \"vaccinations\": [\n" +
        "           {\n" +
        "               \"vaccineType\": \"pfizer\",\n" +
        "               \"inoculationDate\": \"2021-02-04\",\n" +
        "               \"lotNumber\": \"ER8227\"\n" +
        "           },\n" +
        "           {\n" +
        "               \"vaccineType\": \"pfizer\",\n" +
        "               \"inoculationDate\": \"2021-04-12\",\n" +
        "               \"lotNumber\": \"EW0458\"\n" +
        "           }\n" +
        "       ],\n" +
        "   \"image\": \"data:image/jpeg;base64,mock image data\"\n" +
        "   }]}\n";


    @Test
    public void TestProofsEndpointHappyPath() {
        EagleEyeAuthResponse mockAuthResponse = mock(EagleEyeAuthResponse.class);
        EagleEyeProofResponse mockProofResponse = mock(EagleEyeProofResponse.class);
        mockProofResponse.success = true;
        mockProofResponse.message = "mock successful response";
        Mockito.when(mockEEAuthService.getToken(any(EagleEyeAuthRequest.class))).thenReturn(mockAuthResponse);
        Mockito.when(mockEEService.submitProof(any(EagleEyeProofsRequest.class))).thenReturn(mockProofResponse);

        

        Response response = given()
            .header("Content-type", "application/json")
            .and()
            .body(requestBody)
            .when().post("/proofs")
            .then()
                .extract().response();

        Assertions.assertEquals(200, response.statusCode());
    
    }

    @Test
    public void TestProofsEndpointHandleEEError() {
        EagleEyeAuthResponse mockAuthResponse = mock(EagleEyeAuthResponse.class);
        EagleEyeProofResponse mockProofResponse = mock(EagleEyeProofResponse.class);
        mockProofResponse.success = false;
        mockProofResponse.message = "mock unsuccessful response";
        Mockito.when(mockEEAuthService.getToken(any(EagleEyeAuthRequest.class))).thenReturn(mockAuthResponse);
        Mockito.when(mockEEService.submitProof(any(EagleEyeProofsRequest.class))).thenReturn(mockProofResponse);

        

        Response response = given()
            .header("Content-type", "application/json")
            .and()
            .body(requestBody)
            .when().post("/proofs")
            .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    
    }

    @Test
    public void TestProofsEndpointHandleCWEFromEE() {
        EagleEyeAuthResponse mockAuthResponse = mock(EagleEyeAuthResponse.class);
        EagleEyeProofResponse mockProofResponse = mock(EagleEyeProofResponse.class);
        mockProofResponse.success = false;
        mockProofResponse.message = "mock unsuccessful response";
        Mockito.when(mockEEAuthService.getToken(any(EagleEyeAuthRequest.class))).thenReturn(mockAuthResponse);
        Mockito.when(mockEEService.submitProof(any(EagleEyeProofsRequest.class))).thenThrow(new ClientWebApplicationException());

        Response response = given()
            .header("Content-type", "application/json")
            .and()
            .body(requestBody)
            .when().post("/proofs")
            .then()
                .extract().response();

        Assertions.assertEquals(500, response.statusCode());
    
    }

    /**
     * Just confirms that we parse out the base64-encoded image data for readability.
     */
    @Test
    public void testRemoveImageBase64() {
        ProofsResource testObject = new ProofsResource();
        String json = testObject.removeImageBase64(requestBody);

        Assertions.assertTrue(json.contains("<redacted>"));
    }
}