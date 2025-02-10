package ca.bc.gov.educ.graddatacollection.api.rules.course;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

public enum CourseValidationRulesDependencyMatrix {
    ENTRY202("V202", new String[]{CourseStudentValidationIssueTypeCode.DEM_DATA_MISSING.getCode()}),
    ENTRY203("V203", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode()}),
    ENTRY206("V206", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode()}),
    ENTRY208("V208", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode()}),
    ENTRY209("V209", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode()}),
    ENTRY210("V210", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID.getCode(), CourseStudentValidationIssueTypeCode.COURSE_YEAR_INVALID.getCode()}),
    ENTRY211("V211", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID.getCode(), CourseStudentValidationIssueTypeCode.COURSE_YEAR_INVALID.getCode()}),
    ENTRY212("V212", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID.getCode(), CourseStudentValidationIssueTypeCode.COURSE_YEAR_INVALID.getCode()}),
    ENTRY214("V214", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode()}),
    ENTRY215("V215", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode()}),
    ENTRY216("V216", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), CourseStudentValidationIssueTypeCode.INTERIM_PCT_INVALID.getCode(), CourseStudentValidationIssueTypeCode.INTERIM_LETTER_GRADE_INVALID.getCode()}),
    ENTRY217("V217", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode()}),
    ENTRY218("V218", new String[]{CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_OR_PERCENT_NOT_BLANK.getCode()}),
    ENTRY219("V219", new String[]{CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_OR_PERCENT_NOT_BLANK.getCode()}),
    ENTRY220("V220", new String[]{CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_OR_PERCENT_NOT_BLANK.getCode()}),
    ENTRY221("V221", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), CourseStudentValidationIssueTypeCode.FINAL_PCT_INVALID.getCode(), CourseStudentValidationIssueTypeCode.FINAL_PCT_NOT_BLANK.getCode(), CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_INVALID.getCode()}),
    ENTRY222("V222", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_INVALID.getCode()}),
    ENTRY223("V223", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_INVALID.getCode()}),
    ENTRY224("V224", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), CourseStudentValidationIssueTypeCode.FINAL_PCT_INVALID.getCode(), CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_INVALID.getCode()}),
    ENTRY225("V225", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_INVALID.getCode()}),
    ENTRY227("V227", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode()}),
    ENTRY228("V228", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode()}),
    ENTRY231("V231", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode()}),
    ENTRY237("V237", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode()}),
    ;

    @Getter
    private final String ruleID;
    @Getter
    private final String[] baseRuleErrorCode;
    CourseValidationRulesDependencyMatrix(String ruleID, String[] baseRuleErrorCode) {
        this.ruleID = ruleID;
        this.baseRuleErrorCode = baseRuleErrorCode;
    }

    public static Optional<CourseValidationRulesDependencyMatrix> findByValue(String ruleID) {
        return Arrays.stream(values()).filter(code -> code.ruleID.equalsIgnoreCase(ruleID)).findFirst();
    }
}
