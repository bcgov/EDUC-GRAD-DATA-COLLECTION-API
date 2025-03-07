package ca.bc.gov.educ.graddatacollection.api.rules.course;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

public enum CourseValidationRulesDependencyMatrix {
    ENTRY202("V202", new String[]{CourseStudentValidationIssueTypeCode.DEM_DATA_MISSING.getCode()}),
    ENTRY203("V203", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode()}),
    ENTRY204("V204", new String[]{CourseStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getCode(), CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID.getCode(), CourseStudentValidationIssueTypeCode.COURSE_YEAR_INVALID.getMessage()}),
    ENTRY205("V205", new String[]{CourseStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getCode(), CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID.getCode(), CourseStudentValidationIssueTypeCode.COURSE_YEAR_INVALID.getMessage()}),
    ENTRY206("V206", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode()}),
    ENTRY207("V207", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode()}),
    ENTRY208("V208", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode()}),
    ENTRY209("V209", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode()}),
    ENTRY210("V210", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID.getCode(), CourseStudentValidationIssueTypeCode.COURSE_YEAR_INVALID.getCode()}),
    ENTRY211("V211", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID.getCode(), CourseStudentValidationIssueTypeCode.COURSE_YEAR_INVALID.getCode()}),
    ENTRY212("V212", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), CourseStudentValidationIssueTypeCode.COURSE_MONTH_INVALID.getCode(), CourseStudentValidationIssueTypeCode.COURSE_YEAR_INVALID.getCode()}),
    ENTRY213("V213", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), CourseStudentValidationIssueTypeCode.COURSE_SESSION_INVALID.getCode()}),
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
    ENTRY226("V226", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode()}),
    ENTRY227("V227", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode()}),
    ENTRY228("V228", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode()}),
    ENTRY229("V229", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), CourseStudentValidationIssueTypeCode.INVALID_FINE_ARTS_APPLIED_SKILLS_CODE.getCode()}),
    ENTRY230("V230", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), CourseStudentValidationIssueTypeCode.INVALID_FINE_ARTS_APPLIED_SKILLS_CODE.getCode()}),
    ENTRY231("V231", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode()}),
    ENTRY232("V232", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode()}),
    ENTRY233("V233", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode()}),
    ENTRY234("V234", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), CourseStudentValidationIssueTypeCode.COURSE_NOT_INDEPENDENT_DIRECTED_STUDIES.getCode()}),
    ENTRY235("V235", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), CourseStudentValidationIssueTypeCode.RELATED_COURSE_RELATED_LEVEL_INVALID.getCode()}),
    ENTRY236("V236", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode()}),
    ENTRY237("V237", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode()}),
    ENTRY238("V238", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_INVALID.getCode()}),
    ENTRY239("V239", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), CourseStudentValidationIssueTypeCode.COURSE_STATUS_INVALID.getCode()}),
    ENTRY240("V240", new String[]{CourseStudentValidationIssueTypeCode.DEM_ISSUE.getCode(), CourseStudentValidationIssueTypeCode.FINAL_LETTER_GRADE_INVALID.getCode()}),
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
