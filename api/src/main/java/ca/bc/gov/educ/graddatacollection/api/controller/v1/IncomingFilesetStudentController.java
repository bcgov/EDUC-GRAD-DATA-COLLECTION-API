package ca.bc.gov.educ.graddatacollection.api.controller.v1;

import ca.bc.gov.educ.graddatacollection.api.endpoint.v1.IncomingFilesetEndpoint;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.IncomingFilesetExtendedMapper;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.IncomingFilesetMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.AssessmentStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.DemographicStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.service.v1.*;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.IncomingFileset;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.IncomingFilesetExtended;
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

    private final DemographicStudentService demographicStudentService;

    private final AssessmentStudentService assessmentStudentService;

    private final CourseStudentService courseStudentService;

    private static final IncomingFilesetMapper mapper = IncomingFilesetMapper.mapper;

    private static final IncomingFilesetExtendedMapper extendedMapper  = IncomingFilesetExtendedMapper.mapper;

    public IncomingFilesetStudentController(IncomingFilesetSearchService incomingFilesetSearchService, DemographicStudentService demographicStudentService, AssessmentStudentService assessmentStudentService, CourseStudentService courseStudentService) {
        this.incomingFilesetSearchService = incomingFilesetSearchService;
        this.demographicStudentService = demographicStudentService;
        this.assessmentStudentService = assessmentStudentService;
        this.courseStudentService = courseStudentService;
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
    public IncomingFilesetExtended getStudentFileset(String pen, UUID incomingFilesetID, UUID schoolID, UUID districtID) {
        DemographicStudentEntity demStud = this.demographicStudentService.getDemStudent(pen, incomingFilesetID, schoolID, districtID);

        UUID resolvedFilesetID = incomingFilesetID != null ? incomingFilesetID : demStud.getIncomingFileset().getIncomingFilesetID();

        List<AssessmentStudentEntity> xamStuds = this.assessmentStudentService.getXamStudents(pen, resolvedFilesetID, schoolID, districtID);
        List<CourseStudentEntity> crsStuds = this.courseStudentService.getCrsStudents(pen, resolvedFilesetID, schoolID, districtID);

        return extendedMapper.toStructure(pen, resolvedFilesetID, demStud, crsStuds, xamStuds);
    }
}
