package org.rh.client.model;

import java.util.List;

public class EagleEyeProofsRequest {
    
    public String userId;
    public String legalFirstName;
    public String legalLastName;
    public String altFirstName;
    public String altLastName;
    public String dob; // Date in mm/dd/yyyy format
    public List<EagleEyeProof> proofs; 
}
