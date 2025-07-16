package ca.bc.gov.educ.graddatacollection.api.rules.assessment;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum AssessmentValidationRulesDependencyMatrix {
    ENTRY1("V02", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_DATA_MISSING.getCode()}),
    ENTRY2("V03", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_DATA_MISSING.getCode(), AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode()}),
    ENTRY13("V14", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_DATA_MISSING.getCode(), AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode()}),
    ENTRY14("V15", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_DATA_MISSING.getCode(), AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode()}),
    ENTRY15("V16", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_DATA_MISSING.getCode(), AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode()}),
    ENTRY16("V17", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_DATA_MISSING.getCode(), AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode()}),
    ENTRY17("V18", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_DATA_MISSING.getCode(),AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode()}),
    ENTRY18("V19", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_DATA_MISSING.getCode(),AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getCode()}),
    ENTRY19("V20", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_DATA_MISSING.getCode(), AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getCode()}),
    ENTRY20("V21", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_DATA_MISSING.getCode(), AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getCode()}),
    ENTRY21("V22", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_DATA_MISSING.getCode(), AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode()}),


    ;

    private final String ruleID;
    private final String[] baseRuleErrorCode;
    AssessmentValidationRulesDependencyMatrix(String ruleID, String[] baseRuleErrorCode) {
        this.ruleID = ruleID;
        this.baseRuleErrorCode = baseRuleErrorCode;
    }

    public static Optional<AssessmentValidationRulesDependencyMatrix> findByValue(String ruleID) {
        return Arrays.stream(values()).filter(code -> code.ruleID.equalsIgnoreCase(ruleID)).findFirst();
    }
}
