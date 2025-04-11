package ca.bc.gov.educ.graddatacollection.api.exception;

import ca.bc.gov.educ.graddatacollection.api.exception.errors.ApiError;
import lombok.Getter;
import org.springframework.validation.FieldError;

import java.util.List;
import java.util.Map;

@Getter
public class ReportingPeriodValidationException extends RuntimeException {
    private final ApiError error;

    public ReportingPeriodValidationException(List<FieldError> fieldErrors) {
        super("Validation error");
        this.error = new ApiError(org.springframework.http.HttpStatus.BAD_REQUEST);
        this.error.setMessage("Validation error");
        this.error.addValidationErrors(fieldErrors);
    }

    public Map<String, Object> toSafeResponse() {
        return Map.of(
                "message", "Validation error in reporting period",
                "reportingPeriodValidationErrors", error.getSubErrors()
        );
    }
}
