package org.rh.rest.model;

import java.time.LocalDate;
import java.util.List;

public class AutoApprovalRequest {
    public String correlationId;
    public String firstName;
    public String lastName;
    public String altFirstName;
    public String altLastName;
    public LocalDate dob;
    public List<Proof> proofs; 
}
