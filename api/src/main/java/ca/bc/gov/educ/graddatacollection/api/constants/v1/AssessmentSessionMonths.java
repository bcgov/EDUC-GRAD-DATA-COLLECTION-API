package ca.bc.gov.educ.graddatacollection.api.constants.v1;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum AssessmentSessionMonths {
    JANUARY("01"),
    APRIL("04"),
    JUNE("06"),
    NOVEMBER("11");

    @Getter
    private final String code;
    AssessmentSessionMonths(String code) {
        this.code = code;
    }
    public static Optional<AssessmentSessionMonths> findByValue(String value) {
        return Arrays.stream(values()).filter(e -> Arrays.asList(e.code).contains(value)).findFirst();
    }
}