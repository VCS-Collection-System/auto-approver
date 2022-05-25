package org.rh;

import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.util.HashMap;
import java.util.Map;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.Options.ChunkedEncodingPolicy;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

public class KeycloakRealmResourceManager implements QuarkusTestResourceLifecycleManager {
    private WireMockServer server;

    @Override
    public Map<String, String> start() {

        server = new WireMockServer(wireMockConfig().dynamicPort().useChunkedTransferEncoding(ChunkedEncodingPolicy.NEVER));
        server.start();

        server.stubFor(WireMock.post("/tokens")
                .withRequestBody(matching("grant_type=password&username=alice&password=alice"))
                .willReturn(WireMock
                        .aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                "{\"access_token\":\"access_token_1\", \"expires_in\":4, \"refresh_token\":\"refresh_token_1\"}")));
        server.stubFor(WireMock.post("/tokens")
                .withRequestBody(matching("grant_type=refresh_token&refresh_token=refresh_token_1"))
                .willReturn(WireMock
                        .aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(
                                "{\"access_token\":\"access_token_2\", \"expires_in\":4, \"refresh_token\":\"refresh_token_1\"}")));


        Map<String, String> conf = new HashMap<>();
        conf.put("keycloak.url", server.baseUrl());
        return conf;
    }

    @Override
    public synchronized void stop() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }
}
