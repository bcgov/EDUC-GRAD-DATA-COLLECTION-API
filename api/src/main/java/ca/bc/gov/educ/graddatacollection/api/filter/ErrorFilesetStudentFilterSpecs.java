package ca.bc.gov.educ.graddatacollection.api.filter;

import ca.bc.gov.educ.graddatacollection.api.model.v1.ErrorFilesetStudentEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class ErrorFilesetStudentFilterSpecs extends BaseFilterSpecs<ErrorFilesetStudentEntity> {

  public ErrorFilesetStudentFilterSpecs(FilterSpecifications<ErrorFilesetStudentEntity, ChronoLocalDate> dateFilterSpecifications, FilterSpecifications<ErrorFilesetStudentEntity, ChronoLocalDateTime<?>> dateTimeFilterSpecifications, FilterSpecifications<ErrorFilesetStudentEntity, Integer> integerFilterSpecifications, FilterSpecifications<ErrorFilesetStudentEntity, String> stringFilterSpecifications, FilterSpecifications<ErrorFilesetStudentEntity, Long> longFilterSpecifications, FilterSpecifications<ErrorFilesetStudentEntity, UUID> uuidFilterSpecifications, FilterSpecifications<ErrorFilesetStudentEntity, Boolean> booleanFilterSpecifications, Converters converters) {
    super(dateFilterSpecifications, dateTimeFilterSpecifications, integerFilterSpecifications, stringFilterSpecifications, longFilterSpecifications, uuidFilterSpecifications, booleanFilterSpecifications, converters);
  }
}
