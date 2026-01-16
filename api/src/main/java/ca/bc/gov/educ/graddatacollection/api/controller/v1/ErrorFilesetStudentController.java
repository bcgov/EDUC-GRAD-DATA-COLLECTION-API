package ca.bc.gov.educ.graddatacollection.api.controller.v1;

import ca.bc.gov.educ.graddatacollection.api.constants.v1.CustomSearchType;
import ca.bc.gov.educ.graddatacollection.api.constants.v1.ValidationFieldCode;
import ca.bc.gov.educ.graddatacollection.api.endpoint.v1.ErrorFilesetStudentEndpoint;
import ca.bc.gov.educ.graddatacollection.api.exception.GradDataCollectionAPIRuntimeException;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.ErrorFilesetStudentMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.FinalErrorFilesetStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.service.v1.FinalErrorFilesetStudentSearchService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.ErrorFilesetStudent;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.Search;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SearchCriteria;
import ca.bc.gov.educ.graddatacollection.api.util.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    private final FinalErrorFilesetStudentSearchService errorFilesetStudentSearchService;

    private static final ErrorFilesetStudentMapper mapper = ErrorFilesetStudentMapper.mapper;

    public ErrorFilesetStudentController(FinalErrorFilesetStudentSearchService errorFilesetStudentSearchService) {
        this.errorFilesetStudentSearchService = errorFilesetStudentSearchService;
    }

    @Override
    public CompletableFuture<Page<ErrorFilesetStudent>> findAll(Integer pageNumber, Integer pageSize, String sortCriteriaJson, String searchCriteriaListJson) {
        final List<Sort.Order> sorts = new ArrayList<>();
        List<String> mapFilter = new ArrayList<>();
        Specification<FinalErrorFilesetStudentEntity> studentSpecs = errorFilesetStudentSearchService
                .setSpecificationAndSortCriteria(
                        sortCriteriaJson,
                        searchCriteriaListJson,
                        JsonUtil.mapper,
                        sorts
                );
        try {
            if (StringUtils.isNotBlank(searchCriteriaListJson)) {
                List<Search> searches = JsonUtil.mapper.readValue(searchCriteriaListJson, new TypeReference<>() {
                });

                List<Search> customSearch = searches.stream().filter(search -> search.getSearchCriteriaList().stream().anyMatch(cri ->
                        cri.getValue().equalsIgnoreCase(CustomSearchType.DEMERROR.getCode())
                                || cri.getValue().equalsIgnoreCase(CustomSearchType.CRSERROR.getCode())
                                || cri.getValue().equalsIgnoreCase(CustomSearchType.XAMERROR.getCode())
                                || cri.getValue().equalsIgnoreCase(CustomSearchType.ERROR.getCode())
                                || cri.getValue().equalsIgnoreCase(CustomSearchType.WARNING.getCode())
                                || ValidationFieldCode.findByCode(cri.getValue()).isPresent())

                ).toList();
                if (!customSearch.isEmpty()) {
                    mapFilter.addAll(customSearch.stream().map(Search::getSearchCriteriaList).flatMap(searchCriteria -> searchCriteria.stream().map(SearchCriteria::getValue).distinct()).toList());
                }
            }
            return this.errorFilesetStudentSearchService
                        .findAll(studentSpecs, pageNumber, pageSize, sorts)
                        .thenApplyAsync(student -> student.map(stu -> mapper.toStructureWithFilter(stu, mapFilter)));


        } catch (JsonProcessingException e) {
            throw new GradDataCollectionAPIRuntimeException(e.getMessage());
        }
    }
}
