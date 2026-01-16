package ca.bc.gov.educ.graddatacollection.api.filter;

import ca.bc.gov.educ.graddatacollection.api.model.v1.FinalErrorFilesetStudentEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class FinalErrorFilesetStudentFilterSpecs extends BaseFilterSpecs<FinalErrorFilesetStudentEntity> {

  public FinalErrorFilesetStudentFilterSpecs(FilterSpecifications<FinalErrorFilesetStudentEntity, ChronoLocalDate> dateFilterSpecifications, FilterSpecifications<FinalErrorFilesetStudentEntity, ChronoLocalDateTime<?>> dateTimeFilterSpecifications, FilterSpecifications<FinalErrorFilesetStudentEntity, Integer> integerFilterSpecifications, FilterSpecifications<FinalErrorFilesetStudentEntity, String> stringFilterSpecifications, FilterSpecifications<FinalErrorFilesetStudentEntity, Long> longFilterSpecifications, FilterSpecifications<FinalErrorFilesetStudentEntity, UUID> uuidFilterSpecifications, FilterSpecifications<FinalErrorFilesetStudentEntity, Boolean> booleanFilterSpecifications, Converters converters) {
    super(dateFilterSpecifications, dateTimeFilterSpecifications, integerFilterSpecifications, stringFilterSpecifications, longFilterSpecifications, uuidFilterSpecifications, booleanFilterSpecifications, converters);
  }
}
