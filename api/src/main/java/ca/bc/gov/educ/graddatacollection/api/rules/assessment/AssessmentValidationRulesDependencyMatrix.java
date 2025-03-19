package ca.bc.gov.educ.graddatacollection.api.rules.assessment;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum AssessmentValidationRulesDependencyMatrix {
    ENTRY1("V304", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode()}),
    ENTRY2("V318", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), AssessmentStudentValidationIssueTypeCode.DEM_DATA_MISSING.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode()}),
    ENTRY3("V319", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode()}),
    ENTRY4("V320", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_DATA_MISSING.getCode()}),
    ENTRY5("V321", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode()}),
    ENTRY6("V322", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), AssessmentStudentValidationIssueTypeCode.DEM_DATA_MISSING.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getCode()}),
    ENTRY7("V302", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode()}),
    ENTRY8("V303", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode()}),
    ENTRY9("V306", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode()}),
    ENTRY10("V307", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode()}),
    ENTRY11("V308", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode()}),
    ENTRY12("V309", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode()}),
    ENTRY13("V310", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode()}),
    ENTRY14("V311", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode()}),
    ENTRY15("V312", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode()}),
    ENTRY16("V314", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode()}),
    ENTRY17("V315", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode()}),
    ENTRY18("V316", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode()}),
    ENTRY19("V317", new String[]{AssessmentStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID_MONTH.getCode(), AssessmentStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode()}),
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
