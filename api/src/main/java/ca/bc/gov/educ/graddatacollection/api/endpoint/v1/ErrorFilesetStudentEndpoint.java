package ca.bc.gov.educ.graddatacollection.api.endpoint.v1;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.URL;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ErrorFilesetStudent;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.concurrent.CompletableFuture;

@RequestMapping(URL.BASE_URL_FILESET)
public interface ErrorFilesetStudentEndpoint {

    @GetMapping(URL.PAGINATED)
    @PreAuthorize("hasAuthority('SCOPE_READ_FILESET_STUDENT_ERROR')")
    @Transactional(readOnly = true)
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
    CompletableFuture<Page<ErrorFilesetStudent>> findAll(@RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
                                                                @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                                @RequestParam(name = "sort", defaultValue = "") String sortCriteriaJson,
                                                                @RequestParam(name = "searchCriteriaList", required = false) String searchCriteriaListJson);

}
