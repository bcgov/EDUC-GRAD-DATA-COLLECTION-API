package ca.bc.gov.educ.graddatacollection.api.constants.v1;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Getter
public enum GradRequirementYearCodes {
    YEAR_1950("1950"),
    YEAR_1986("1986"),
    YEAR_1996("1996"),
    YEAR_2004("2004"),
    YEAR_2018("2018"),
    YEAR_2023("2023"),
    SCCP("SCCP");

    @Getter
    private final String code;
    GradRequirementYearCodes(String code) {
        this.code = code;
    }

    public static Optional<GradRequirementYearCodes> findByValue(String value) {
        return Arrays.stream(values()).filter(e -> Arrays.asList(e.code).contains(value)).findFirst();
    }

    public static List<String> getAdultGraduationProgramYearCodes() {
        List<String> codes = new ArrayList<>();
        codes.add(YEAR_1950.getCode());
        return codes;
    }

    public static List<String> getOffshoreSchoolNotAllowedCodes() {
        List<String> codes = getAdultGraduationProgramYearCodes();
        codes.add(SCCP.getCode());
        return codes;
    }


    public static List<String> getNonAdultGraduationProgramYearCodes() {
        List<String> codes = new ArrayList<>();
        codes.add(YEAR_1986.getCode());
        codes.add(YEAR_1996.getCode());
        codes.add(YEAR_2004.getCode());
        codes.add(YEAR_2018.getCode());
        codes.add(YEAR_2023.getCode());
        codes.add(SCCP.getCode());
        return codes;
    }
}