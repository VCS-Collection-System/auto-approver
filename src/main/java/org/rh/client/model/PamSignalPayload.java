package org.rh.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PamSignalPayload {
    
    @JsonProperty("com.redhat.vcs.model.VaxProofAutomaticApprovalResponse")
    public PamModelWrapper wrapper;
}
