package org.rh.util;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.rh.client.model.EagleEyeProof;
import org.rh.client.model.EagleEyeProofsRequest;
import org.rh.client.model.EagleEyeVaccination;
import org.rh.rest.model.AutoApprovalRequest;
import org.rh.rest.model.Proof;
import org.rh.rest.model.Vaccination;

@Mapper(componentModel = "cdi")
public interface ApprovalRequestMapper {
    @Mapping(target = "userId", source = "correlationId")
    @Mapping(target = "legalFirstName", source = "firstName")
    @Mapping(target = "legalLastName", source = "lastName")
    @Mapping(source = "dob", target = "dob", dateFormat = "MM/dd/yyyy")
    EagleEyeProofsRequest toEagleEyeProofsRequest(AutoApprovalRequest req);

    EagleEyeProof toEagleEyeProof(Proof p);

    @Mapping(source = "inoculationDate", target = "inoculationDate", dateFormat = "MM/dd/yyyy")
    EagleEyeVaccination toEagleEyeVaccination(Vaccination v);
}
