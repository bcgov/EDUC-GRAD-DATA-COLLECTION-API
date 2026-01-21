package ca.bc.gov.educ.graddatacollection.api.filter;

import ca.bc.gov.educ.graddatacollection.api.model.v1.FinalIncomingFilesetEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class FinalIncomingFilesetFilterSpecs extends BaseFilterSpecs<FinalIncomingFilesetEntity> {

  public FinalIncomingFilesetFilterSpecs(FilterSpecifications<FinalIncomingFilesetEntity, ChronoLocalDate> dateFilterSpecifications, FilterSpecifications<FinalIncomingFilesetEntity, ChronoLocalDateTime<?>> dateTimeFilterSpecifications, FilterSpecifications<FinalIncomingFilesetEntity, Integer> integerFilterSpecifications, FilterSpecifications<FinalIncomingFilesetEntity, String> stringFilterSpecifications, FilterSpecifications<FinalIncomingFilesetEntity, Long> longFilterSpecifications, FilterSpecifications<FinalIncomingFilesetEntity, UUID> uuidFilterSpecifications, FilterSpecifications<FinalIncomingFilesetEntity, Boolean> booleanFilterSpecifications, Converters converters) {
    super(dateFilterSpecifications, dateTimeFilterSpecifications, integerFilterSpecifications, stringFilterSpecifications, longFilterSpecifications, uuidFilterSpecifications, booleanFilterSpecifications, converters);
  }
}
