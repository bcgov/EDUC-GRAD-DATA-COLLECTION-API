package ca.bc.gov.educ.graddatacollection.api.filter;

import ca.bc.gov.educ.graddatacollection.api.model.v1.IncomingFilesetEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.UUID;

@Service
@Slf4j
public class IncomingFilesetFilterSpecs extends BaseFilterSpecs<IncomingFilesetEntity> {

  public IncomingFilesetFilterSpecs(FilterSpecifications<IncomingFilesetEntity, ChronoLocalDate> dateFilterSpecifications, FilterSpecifications<IncomingFilesetEntity, ChronoLocalDateTime<?>> dateTimeFilterSpecifications, FilterSpecifications<IncomingFilesetEntity, Integer> integerFilterSpecifications, FilterSpecifications<IncomingFilesetEntity, String> stringFilterSpecifications, FilterSpecifications<IncomingFilesetEntity, Long> longFilterSpecifications, FilterSpecifications<IncomingFilesetEntity, UUID> uuidFilterSpecifications, FilterSpecifications<IncomingFilesetEntity, Boolean> booleanFilterSpecifications, Converters converters) {
    super(dateFilterSpecifications, dateTimeFilterSpecifications, integerFilterSpecifications, stringFilterSpecifications, longFilterSpecifications, uuidFilterSpecifications, booleanFilterSpecifications, converters);
  }
}
