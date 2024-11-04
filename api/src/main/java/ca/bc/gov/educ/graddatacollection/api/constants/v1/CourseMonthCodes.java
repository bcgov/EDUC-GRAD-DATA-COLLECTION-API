package ca.bc.gov.educ.graddatacollection.api.constants.v1;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum CourseMonthCodes {
    MONTH_01("01"),
    MONTH_02("02"),
    MONTH_03("03"),
    MONTH_04("04"),
    MONTH_05("05"),
    MONTH_06("06"),
    MONTH_07("07"),
    MONTH_08("08"),
    MONTH_09("09"),
    MONTH_10("10"),
    MONTH_11("11"),
    MONTH_12("12");

    private final String code;
    CourseMonthCodes(String code) { this.code = code; }

    public static Optional<CourseMonthCodes> findByValue(String value) {
        return Arrays.stream(values()).filter(e -> Arrays.asList(e.code).contains(value)).findFirst();
    }
}
