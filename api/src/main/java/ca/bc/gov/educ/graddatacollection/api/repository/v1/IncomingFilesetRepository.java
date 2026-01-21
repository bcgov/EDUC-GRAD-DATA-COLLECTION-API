package ca.bc.gov.educ.graddatacollection.api.repository.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.SchoolSubmissionCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface IncomingFilesetRepository extends JpaRepository<IncomingFilesetEntity, UUID>, JpaSpecificationExecutor<IncomingFilesetEntity> {
    Optional<IncomingFilesetEntity> findBySchoolIDAndFilesetStatusCode(UUID schoolID, String statusCode);

    Optional<IncomingFilesetEntity> findBySchoolIDAndFilesetStatusCodeAndDemFileNameIsNotNullAndXamFileNameIsNotNullAndCrsFileNameIsNotNull(UUID schoolID, String statusCode);
    
    @Transactional
    @Modifying
    @Query("DELETE FROM IncomingFilesetEntity WHERE incomingFilesetID = :incomingFilesetID")
    void deleteByIncomingFilesetID(UUID incomingFilesetID);

    @Query(value = """
    SELECT inFileset.school_id as schoolID,
    COUNT(inFileset.incoming_fileset_id) as submissionCount
    FROM incoming_fileset inFileset
    WHERE inFileset.fileset_status_code = 'COMPLETED'
    AND inFileset.create_date >= GREATEST(:reportingStartDate, (CURRENT_TIMESTAMP - INTERVAL '30' day))
    AND inFileset.reporting_period_id = :reportingPeriodID
    GROUP BY inFileset.school_id
    """, nativeQuery = true)
    List<SchoolSubmissionCount> findSchoolSubmissionsInLast30Days(UUID reportingPeriodID, LocalDateTime reportingStartDate);

    @Query(value = """
    SELECT inFileset.schoolID as schoolID,
    COUNT(inFileset.incomingFilesetID) as submissionCount,
    MAX(inFileset.createDate) as lastSubmissionDate
    FROM IncomingFilesetEntity inFileset
    WHERE inFileset.filesetStatusCode = 'COMPLETED'
    AND inFileset.createDate >= :summerStartDate
    AND inFileset.createDate <= :summerEndDate
    AND inFileset.reportingPeriod.reportingPeriodID = :reportingPeriodID
    GROUP BY inFileset.schoolID
    """)
    List<SchoolSubmissionCount> findSchoolSubmissionsInSummerReportingPeriod(UUID reportingPeriodID, LocalDateTime summerStartDate, LocalDateTime summerEndDate);

    @Query(value = """
    SELECT inFileset.schoolID as schoolID,
    COUNT(inFileset.incomingFilesetID) as submissionCount,
    MAX(inFileset.createDate) as lastSubmissionDate
    FROM IncomingFilesetEntity inFileset
    WHERE inFileset.filesetStatusCode = 'COMPLETED'
    AND inFileset.createDate >= :schoolStartDate
    AND inFileset.createDate <= :schoolEndDate
    AND inFileset.reportingPeriod.reportingPeriodID = :reportingPeriodID
    GROUP BY inFileset.schoolID
    """)
    List<SchoolSubmissionCount> findSchoolSubmissionsInSchoolReportingPeriod(UUID reportingPeriodID, LocalDateTime schoolStartDate, LocalDateTime schoolEndDate);

    @Query(value = """
    SELECT COUNT(*)
    FROM IncomingFilesetEntity inFileset
    WHERE inFileset.filesetStatusCode != 'COMPLETED'
    AND inFileset.updateDate <= :updateDate
    AND inFileset.demFileName is not null
    AND inFileset.crsFileName is not null
    AND inFileset.xamFileName is not null
    AND ((select count(ds2) from DemographicStudentEntity ds2 where ds2.studentStatusCode = 'LOADED' and ds2.incomingFileset.incomingFilesetID = inFileset.incomingFilesetID) > 0
    OR (select count(cs2) from CourseStudentEntity cs2 where cs2.studentStatusCode = 'LOADED' and cs2.incomingFileset.incomingFilesetID = inFileset.incomingFilesetID) > 0
    OR (select count(as2) from AssessmentStudentEntity as2 where as2.studentStatusCode = 'LOADED' and as2.incomingFileset.incomingFilesetID = inFileset.incomingFilesetID) > 0)
    """)
    long findPositionInQueueByUpdateDate(LocalDateTime updateDate);

    @Modifying
    @Query(value = """
        INSERT INTO FINAL_INCOMING_FILESET (
            INCOMING_FILESET_ID, SCHOOL_ID, DISTRICT_ID, DEM_FILE_NAME, DEM_FILE_DATE_UPLOADED,
            XAM_FILE_NAME, XAM_FILE_DATE_UPLOADED, CRS_FILE_NAME, CRS_FILE_DATE_UPLOADED,
            FILESET_STATUS_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE, CSV_FILE_NAME,
            REPORTING_PERIOD_ID
        )
        SELECT 
            :filesetId, SCHOOL_ID, DISTRICT_ID, DEM_FILE_NAME, DEM_FILE_DATE_UPLOADED,
            XAM_FILE_NAME, XAM_FILE_DATE_UPLOADED, CRS_FILE_NAME, CRS_FILE_DATE_UPLOADED,
            :filesetStatus, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE, CSV_FILE_NAME,
            REPORTING_PERIOD_ID
        FROM INCOMING_FILESET
        WHERE INCOMING_FILESET_ID = :filesetId
        """, nativeQuery = true)
    int copyFilesetParent(
            @Param("filesetId") UUID filesetId,
            @Param("filesetStatus") String filesetStatus);

    @Modifying
    @Query(value = """
        INSERT INTO FINAL_DEMOGRAPHIC_STUDENT (
            DEMOGRAPHIC_STUDENT_ID, INCOMING_FILESET_ID, STUDENT_STATUS_CODE, TRANSACTION_ID, VENDOR_ID,
            LOCAL_ID, PEN, LAST_NAME, MIDDLE_NAME, FIRST_NAME, ADDRESS1, ADDRESS2, CITY, PROVINCIAL_CODE,
            COUNTRY_CODE, POSTAL_CODE, BIRTHDATE, GENDER, CITIZENSHIP, GRADE, PROGRAM_CODE_1, PROGRAM_CODE_2,
            PROGRAM_CODE_3, PROGRAM_CODE_4, PROGRAM_CODE_5, program_cadre_flag, grad_requirement_year,
            school_certificate_completion_date, STUDENT_STATUS, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE
        )
        SELECT 
            gen_random_uuid(),
            :filesetId,
            STUDENT_STATUS_CODE, TRANSACTION_ID, VENDOR_ID,
            LOCAL_ID, PEN, LAST_NAME, MIDDLE_NAME, FIRST_NAME, ADDRESS1, ADDRESS2, CITY, PROVINCIAL_CODE,
            COUNTRY_CODE, POSTAL_CODE, BIRTHDATE, GENDER, CITIZENSHIP, GRADE, PROGRAM_CODE_1, PROGRAM_CODE_2,
            PROGRAM_CODE_3, PROGRAM_CODE_4, PROGRAM_CODE_5, program_cadre_flag, grad_requirement_year,
            school_certificate_completion_date, STUDENT_STATUS, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE
        FROM DEMOGRAPHIC_STUDENT
        WHERE INCOMING_FILESET_ID = :filesetId
        """, nativeQuery = true)
    int copyDemographicStudents(
            @Param("filesetId") UUID filesetId);

    @Modifying
    @Query(value = """
        INSERT INTO FINAL_DEMOGRAPHIC_STUDENT_VALIDATION_ISSUE (
            DEMOGRAPHIC_STUDENT_VALIDATION_ISSUE_ID, DEMOGRAPHIC_STUDENT_ID, VALIDATION_ISSUE_SEVERITY_CODE,
            VALIDATION_ISSUE_CODE, VALIDATION_ISSUE_FIELD_CODE, VALIDATION_ISSUE_DESCRIPTION,
            CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE
        )
        SELECT 
            gen_random_uuid(),
            fds.DEMOGRAPHIC_STUDENT_ID, VALIDATION_ISSUE_SEVERITY_CODE,
            VALIDATION_ISSUE_CODE, VALIDATION_ISSUE_FIELD_CODE, VALIDATION_ISSUE_DESCRIPTION,
            CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE
        FROM DEMOGRAPHIC_STUDENT_VALIDATION_ISSUE dvi
        INNER JOIN DEMOGRAPHIC_STUDENT ds ON dvi.DEMOGRAPHIC_STUDENT_ID = ds.DEMOGRAPHIC_STUDENT_ID
        INNER JOIN FINAL_DEMOGRAPHIC_STUDENT fds ON ds.DEMOGRAPHIC_STUDENT_ID = fds.DEMOGRAPHIC_STUDENT_ID
        WHERE ds.INCOMING_FILESET_ID = :filesetId
        """, nativeQuery = true)
    int copyDemographicValidationIssues(
            @Param("filesetId") UUID filesetId);

    @Modifying
    @Query(value = """
        INSERT INTO FINAL_COURSE_STUDENT (
            COURSE_STUDENT_ID, INCOMING_FILESET_ID, STUDENT_STATUS_CODE, TRANSACTION_ID, LOCAL_ID, VENDOR_ID,
            PEN, COURSE_CODE, COURSE_LEVEL, COURSE_YEAR, COURSE_MONTH, INTERIM_PERCENTAGE, INTERIM_GRADE,
            FINAL_PERCENTAGE, FINAL_GRADE, COURSE_STATUS, LAST_NAME, NUMBER_OF_CREDITS, RELATED_COURSE,
            RELATED_LEVEL, COURSE_DESCRIPTION, COURSE_TYPE, COURSE_GRADUATION_REQUIREMENT,
            CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE
        )
        SELECT 
            gen_random_uuid(),
            :filesetId,
            STUDENT_STATUS_CODE, TRANSACTION_ID, LOCAL_ID, VENDOR_ID,
            PEN, COURSE_CODE, COURSE_LEVEL, COURSE_YEAR, COURSE_MONTH, INTERIM_PERCENTAGE, INTERIM_GRADE,
            FINAL_PERCENTAGE, FINAL_GRADE, COURSE_STATUS, LAST_NAME, NUMBER_OF_CREDITS, RELATED_COURSE,
            RELATED_LEVEL, COURSE_DESCRIPTION, COURSE_TYPE, COURSE_GRADUATION_REQUIREMENT,
            CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE
        FROM COURSE_STUDENT
        WHERE INCOMING_FILESET_ID = :filesetId
        """, nativeQuery = true)
    int copyCourseStudents(
            @Param("filesetId") UUID filesetId);

    @Modifying
    @Query(value = """
        INSERT INTO FINAL_COURSE_STUDENT_VALIDATION_ISSUE (
            COURSE_STUDENT_VALIDATION_ISSUE_ID, COURSE_STUDENT_ID, VALIDATION_ISSUE_SEVERITY_CODE,
            VALIDATION_ISSUE_CODE, VALIDATION_ISSUE_FIELD_CODE, VALIDATION_ISSUE_DESCRIPTION,
            CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE
        )
        SELECT 
            gen_random_uuid(),
            fcs.COURSE_STUDENT_ID, VALIDATION_ISSUE_SEVERITY_CODE,
            VALIDATION_ISSUE_CODE, VALIDATION_ISSUE_FIELD_CODE, VALIDATION_ISSUE_DESCRIPTION,
            CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE
        FROM COURSE_STUDENT_VALIDATION_ISSUE cvi
        INNER JOIN COURSE_STUDENT cs ON cvi.COURSE_STUDENT_ID = cs.COURSE_STUDENT_ID
        INNER JOIN FINAL_COURSE_STUDENT fcs ON cs.COURSE_STUDENT_ID = fcs.COURSE_STUDENT_ID
        WHERE cs.INCOMING_FILESET_ID = :filesetId
        """, nativeQuery = true)
    int copyCourseValidationIssues(
            @Param("filesetId") UUID filesetId);

    @Modifying
    @Query(value = """
        INSERT INTO FINAL_ASSESSMENT_STUDENT (
            ASSESSMENT_STUDENT_ID, INCOMING_FILESET_ID, ASSESSMENT_ID, STUDENT_STATUS_CODE, TRANSACTION_ID,
            VENDOR_ID, LOCAL_ID, PEN, COURSE_CODE, COURSE_YEAR, COURSE_MONTH, IS_ELECTRONIC_EXAM,
            LOCAL_COURSE_ID, PROVINCIAL_SPECIAL_CASE, COURSE_STATUS, LAST_NAME, COURSE_LEVEL, INTERIM_LETTER_GRADE,
            INTERIM_SCHOOL_PERCENT, FINAL_SCHOOL_PERCENT, EXAM_PERCENT, FINAL_PERCENT, FINAL_LETTER_GRADE,
            NUM_CREDITS, CRSE_TYPE, TO_WRITE_FLAG, EXAM_SCHOOL_ID,
            CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE
        )
        SELECT 
            gen_random_uuid(),
            :filesetId,
            ASSESSMENT_ID, STUDENT_STATUS_CODE, TRANSACTION_ID,
            VENDOR_ID, LOCAL_ID, PEN, COURSE_CODE, COURSE_YEAR, COURSE_MONTH, IS_ELECTRONIC_EXAM,
            LOCAL_COURSE_ID, PROVINCIAL_SPECIAL_CASE, COURSE_STATUS, LAST_NAME, COURSE_LEVEL, INTERIM_LETTER_GRADE,
            INTERIM_SCHOOL_PERCENT, FINAL_SCHOOL_PERCENT, EXAM_PERCENT, FINAL_PERCENT, FINAL_LETTER_GRADE,
            NUM_CREDITS, CRSE_TYPE, TO_WRITE_FLAG, EXAM_SCHOOL_ID,
            CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE
        FROM ASSESSMENT_STUDENT
        WHERE INCOMING_FILESET_ID = :filesetId
        """, nativeQuery = true)
    int copyAssessmentStudents(
            @Param("filesetId") UUID filesetId);

    @Modifying
    @Query(value = """
        INSERT INTO FINAL_ASSESSMENT_STUDENT_VALIDATION_ISSUE (
            ASSESSMENT_STUDENT_VALIDATION_ISSUE_ID, ASSESSMENT_STUDENT_ID, VALIDATION_ISSUE_SEVERITY_CODE,
            VALIDATION_ISSUE_CODE, VALIDATION_ISSUE_FIELD_CODE, VALIDATION_ISSUE_DESCRIPTION,
            CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE
        )
        SELECT 
            gen_random_uuid(),
            fas.ASSESSMENT_STUDENT_ID, VALIDATION_ISSUE_SEVERITY_CODE,
            VALIDATION_ISSUE_CODE, VALIDATION_ISSUE_FIELD_CODE, VALIDATION_ISSUE_DESCRIPTION,
            CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE
        FROM ASSESSMENT_STUDENT_VALIDATION_ISSUE avi
        INNER JOIN ASSESSMENT_STUDENT asst ON avi.ASSESSMENT_STUDENT_ID = asst.ASSESSMENT_STUDENT_ID
        INNER JOIN FINAL_ASSESSMENT_STUDENT fas ON asst.ASSESSMENT_STUDENT_ID = fas.ASSESSMENT_STUDENT_ID
        WHERE asst.INCOMING_FILESET_ID = :filesetId
        """, nativeQuery = true)
    int copyAssessmentValidationIssues(
            @Param("filesetId") UUID filesetId);

    @Modifying
    @Query(value = """
        INSERT INTO FINAL_ERROR_FILESET_STUDENT (
            ERROR_FILESET_STUDENT_ID, INCOMING_FILESET_ID, PEN, LOCAL_ID, LAST_NAME, FIRST_NAME, BIRTHDATE,
            CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE
        )
        SELECT 
            gen_random_uuid(),
            :filesetId,
            PEN, LOCAL_ID, LAST_NAME, FIRST_NAME, BIRTHDATE,
            CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE
        FROM ERROR_FILESET_STUDENT
        WHERE INCOMING_FILESET_ID = :filesetId
        """, nativeQuery = true)
    int copyErrorFilesetStudents(
            @Param("filesetId") UUID filesetId);

    @Modifying
    @Query(value = """
        UPDATE INCOMING_FILESET 
        SET FILESET_STATUS_CODE = :filesetStatus,
            UPDATE_DATE = NOW()
        WHERE INCOMING_FILESET_ID = :filesetId
        """, nativeQuery = true)
    int markStagedFilesetComplete(
            @Param("filesetId") UUID filesetId,
            @Param("filesetStatus") String filesetStatus);
    
}
