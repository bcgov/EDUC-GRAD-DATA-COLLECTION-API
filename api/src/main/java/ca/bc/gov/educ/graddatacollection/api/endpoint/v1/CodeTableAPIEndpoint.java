package ca.bc.gov.educ.graddatacollection.api.endpoint.v1;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.URL;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ValidationIssueFieldCode;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ValidationIssueTypeCode;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@RequestMapping(URL.BASE_URL)
public interface CodeTableAPIEndpoint {

    @PreAuthorize("hasAuthority('SCOPE_READ_GRAD_COLLECTION_CODES')")
    @GetMapping(URL.VALIDATION_ISSUE_TYPE_CODES)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    @Transactional(readOnly = true)
    @Tag(name = "Validation Issue Codes", description = "Endpoints to get validation codes.")
    @Schema(name = "ValidationIssueTypeCode", implementation = ValidationIssueTypeCode.class)
    List<ValidationIssueTypeCode> getValidationIssueTypeCodes();

    @PreAuthorize("hasAuthority('SCOPE_READ_GRAD_COLLECTION_CODES')")
    @GetMapping(URL.VALIDATION_FIELD_CODES)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    @Transactional(readOnly = true)
    @Tag(name = "Validation Field Codes", description = "Endpoints to get validation codes.")
    @Schema(name = "ValidationIssueFieldCode", implementation = ValidationIssueFieldCode.class)
    List<ValidationIssueFieldCode> getValidationFieldCodes();

}
