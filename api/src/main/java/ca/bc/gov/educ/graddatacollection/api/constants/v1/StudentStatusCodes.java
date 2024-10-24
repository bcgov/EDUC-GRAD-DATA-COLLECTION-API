package ca.bc.gov.educ.graddatacollection.api.constants.v1;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Getter
public enum StudentStatusCodes {
    M("M");

    @Getter
    private final String code;
    StudentStatusCodes(String code) {
        this.code = code;
    }

    public static Optional<StudentStatusCodes> findByValue(String value) {
        return Arrays.stream(values()).filter(e -> Arrays.asList(e.code).contains(value)).findFirst();
    }

    public static List<String> getStudentStatusCodeM() {
        List<String> codes = new ArrayList<>();
        codes.add(M.getCode());
        return codes;
    }
}
