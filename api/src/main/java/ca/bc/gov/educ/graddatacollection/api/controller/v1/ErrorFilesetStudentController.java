package ca.bc.gov.educ.graddatacollection.api.controller.v1;

import ca.bc.gov.educ.graddatacollection.api.endpoint.v1.ErrorFilesetStudentEndpoint;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.ErrorFilesetStudentMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.ErrorFilesetStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.service.v1.ErrorFilesetStudentSearchService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ErrorFilesetStudent;
import ca.bc.gov.educ.graddatacollection.api.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
public class ErrorFilesetStudentController implements ErrorFilesetStudentEndpoint {

    private final ErrorFilesetStudentSearchService errorFilesetStudentSearchService;

    private static final ErrorFilesetStudentMapper mapper = ErrorFilesetStudentMapper.mapper;

    public ErrorFilesetStudentController(ErrorFilesetStudentSearchService errorFilesetStudentSearchService) {
        this.errorFilesetStudentSearchService = errorFilesetStudentSearchService;
    }

    @Override
    public CompletableFuture<Page<ErrorFilesetStudent>> findAll(Integer pageNumber, Integer pageSize, String sortCriteriaJson, String searchCriteriaListJson) {
        final List<Sort.Order> sorts = new ArrayList<>();
        Specification<ErrorFilesetStudentEntity> studentSpecs = errorFilesetStudentSearchService
                .setSpecificationAndSortCriteria(
                        sortCriteriaJson,
                        searchCriteriaListJson,
                        JsonUtil.mapper,
                        sorts
                );
        return this.errorFilesetStudentSearchService
                .findAll(studentSpecs, pageNumber, pageSize, sorts)
                .thenApplyAsync(sdcSchoolStudentEntities -> sdcSchoolStudentEntities.map(mapper::toStructure));
    }
}
