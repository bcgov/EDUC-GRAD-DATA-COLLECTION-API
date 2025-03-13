package ca.bc.gov.educ.graddatacollection.api.controller.v1;

import ca.bc.gov.educ.graddatacollection.api.endpoint.v1.IncomingFilesetEndpoint;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.IncomingFilesetMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.service.v1.IncomingFilesetSearchService;
import ca.bc.gov.educ.graddatacollection.api.service.v1.IncomingFilesetService;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.IncomingFileset;
import ca.bc.gov.educ.graddatacollection.api.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RestController
@Slf4j
public class IncomingFilesetStudentController implements IncomingFilesetEndpoint {

    private final IncomingFilesetSearchService incomingFilesetSearchService;

    private final IncomingFilesetService incomingFilesetService;

    private static final IncomingFilesetMapper mapper = IncomingFilesetMapper.mapper;

    public IncomingFilesetStudentController(IncomingFilesetSearchService incomingFilesetSearchService, IncomingFilesetService incomingFilesetService) {
        this.incomingFilesetSearchService = incomingFilesetSearchService;
        this.incomingFilesetService = incomingFilesetService;
    }

    @Override
    public CompletableFuture<Page<IncomingFileset>> findAll(Integer pageNumber, Integer pageSize, String sortCriteriaJson, String searchCriteriaListJson) {
        final List<Sort.Order> sorts = new ArrayList<>();
        Specification<IncomingFilesetEntity> studentSpecs = incomingFilesetSearchService
                .setSpecificationAndSortCriteria(
                        sortCriteriaJson,
                        searchCriteriaListJson,
                        JsonUtil.mapper,
                        sorts
                );
        return this.incomingFilesetSearchService
                .findAll(studentSpecs, pageNumber, pageSize, sorts)
                .thenApplyAsync(fileset -> fileset.map(mapper::toStructure));
    }

    @Override
    public IncomingFileset getIncomingFileset(String pen, UUID incomingFilesetID, UUID schoolID, UUID districtID) {
        IncomingFilesetEntity incomingFilesetEntity = this.incomingFilesetService.getErrorFilesetStudent(pen, incomingFilesetID, schoolID, districtID);
        log.debug("getIncomingFileset: ={}", incomingFilesetEntity);
        return mapper.toStructure(incomingFilesetEntity);
    }
}
