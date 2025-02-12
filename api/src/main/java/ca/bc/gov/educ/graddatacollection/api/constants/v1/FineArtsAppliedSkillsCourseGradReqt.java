package ca.bc.gov.educ.graddatacollection.api.constants.v1;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The enum Fine Arts and Applied Skills Course Graduation Requirement Code.
 */
public enum FineArtsAppliedSkillsCourseGradReqt {

    A("A"),
    B("B"),
    F("F");

    /**
     * The Code.
     */
    @Getter
    private final String code;

    /**
     * Instantiates a new FineArtsAppliedSkillsCourseGradReqt codes.
     *
     * @param code the code
     */
    FineArtsAppliedSkillsCourseGradReqt(final String code) {
        this.code = code;
    }

    public static List<String> getCodes() {
        return Arrays.stream(FineArtsAppliedSkillsCourseGradReqt.values()).map(FineArtsAppliedSkillsCourseGradReqt::getCode).collect(Collectors.toList());
    }
}
