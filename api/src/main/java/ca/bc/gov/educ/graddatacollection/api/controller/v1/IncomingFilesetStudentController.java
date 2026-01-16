package ca.bc.gov.educ.graddatacollection.api.controller.v1;

import ca.bc.gov.educ.graddatacollection.api.endpoint.v1.IncomingFilesetEndpoint;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.IncomingFilesetMapper;
import ca.bc.gov.educ.graddatacollection.api.mappers.v1.IncomingFilesetStudentMapper;
import ca.bc.gov.educ.graddatacollection.api.model.v1.*;
import ca.bc.gov.educ.graddatacollection.api.service.v1.*;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.IncomingFileset;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.IncomingFilesetStudent;
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

    private final FinalIncomingFilesetSearchService finalIncomingFilesetSearchService;

    private final IncomingFilesetSearchService incomingFilesetSearchService;

    private final DemographicStudentService demographicStudentService;

    private final AssessmentStudentService assessmentStudentService;

    private final CourseStudentService courseStudentService;

    private static final IncomingFilesetMapper mapper = IncomingFilesetMapper.mapper;

    private static final IncomingFilesetStudentMapper extendedMapper  = IncomingFilesetStudentMapper.mapper;

    public IncomingFilesetStudentController(FinalIncomingFilesetSearchService finalIncomingFilesetSearchService, IncomingFilesetSearchService incomingFilesetSearchService, DemographicStudentService demographicStudentService, AssessmentStudentService assessmentStudentService, CourseStudentService courseStudentService) {
        this.finalIncomingFilesetSearchService = finalIncomingFilesetSearchService;
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
                .thenApplyAsync(fileset -> fileset.map(mapper::toStructure).map(file -> {
                    long pos = incomingFilesetSearchService.getCounts(file);
                    file.setPositionInQueue(String.valueOf(pos));
                    return file;
                }));
    }

    @Override
    public CompletableFuture<Page<IncomingFileset>> findAllFinal(Integer pageNumber, Integer pageSize, String sortCriteriaJson, String searchCriteriaListJson) {
        final List<Sort.Order> sorts = new ArrayList<>();
        Specification<FinalIncomingFilesetEntity> studentSpecs = finalIncomingFilesetSearchService
                .setSpecificationAndSortCriteria(
                        sortCriteriaJson,
                        searchCriteriaListJson,
                        JsonUtil.mapper,
                        sorts
                );
        return this.finalIncomingFilesetSearchService
                .findAll(studentSpecs, pageNumber, pageSize, sorts)
                .thenApplyAsync(fileset -> fileset.map(mapper::toStructure).map(file -> {
                    long pos = finalIncomingFilesetSearchService.getCounts(file);
                    file.setPositionInQueue(String.valueOf(pos));
                    return file;
                }));
    }

    @Override
    public IncomingFilesetStudent getFilesetStudent(String pen, UUID incomingFilesetID, UUID schoolID) {
        FinalDemographicStudentEntity demStud = this.demographicStudentService.getDemStudent(pen, incomingFilesetID, schoolID);

        UUID resolvedFilesetID = incomingFilesetID != null ? incomingFilesetID : demStud.getIncomingFileset().getIncomingFilesetID();

        List<FinalAssessmentStudentEntity> xamStuds = this.assessmentStudentService.getXamStudents(pen, resolvedFilesetID, schoolID);
        List<FinalCourseStudentEntity> crsStuds = this.courseStudentService.getCrsStudents(pen, resolvedFilesetID, schoolID);

        return extendedMapper.toStructure(pen, resolvedFilesetID, demStud, crsStuds, xamStuds);
    }
}
