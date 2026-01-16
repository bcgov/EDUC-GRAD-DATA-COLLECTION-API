package ca.bc.gov.educ.graddatacollection.api.service.v1;

import ca.bc.gov.educ.graddatacollection.api.exception.GradDataCollectionAPIRuntimeException;
import ca.bc.gov.educ.graddatacollection.api.filter.FinalErrorFilesetStudentFilterSpecs;
import ca.bc.gov.educ.graddatacollection.api.model.v1.ErrorFilesetStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.FinalErrorFilesetStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.repository.v1.FinalErrorFilesetStudentPaginationRepository;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.Search;
import ca.bc.gov.educ.graddatacollection.api.util.RequestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

@Service
@Slf4j
@RequiredArgsConstructor
public class FinalErrorFilesetStudentSearchService extends BaseSearchService {
  @Getter
  private final FinalErrorFilesetStudentFilterSpecs finalErrorFilesetStudentFilterSpecs;

  private final FinalErrorFilesetStudentPaginationRepository finalErrorFilesetStudentPaginationRepository;

  private final Executor paginatedQueryExecutor = new EnhancedQueueExecutor.Builder()
    .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("async-pagination-query-executor-%d").build())
    .setCorePoolSize(2).setMaximumPoolSize(10).setKeepAliveTime(Duration.ofSeconds(60)).build();

  @Transactional(propagation = Propagation.SUPPORTS)
  public CompletableFuture<Page<FinalErrorFilesetStudentEntity>> findAll(Specification<FinalErrorFilesetStudentEntity> studentSpecs, final Integer pageNumber, final Integer pageSize, final List<Sort.Order> sorts) {
    log.trace("In find all query: {}", studentSpecs);
    return CompletableFuture.supplyAsync(() -> {
      Pageable paging = PageRequest.of(pageNumber, pageSize, Sort.by(sorts));
      try {
        log.trace("Running paginated query: {}", studentSpecs);
        var results = this.finalErrorFilesetStudentPaginationRepository.findAll(studentSpecs, paging);
        log.trace("Paginated query returned with results: {}", results);
        return results;
      } catch (final Throwable ex) {
        log.error("Failure querying for paginated SDC school students: {}", ex.getMessage());
        throw new CompletionException(ex);
      }
    }, paginatedQueryExecutor);

  }

  public Specification<FinalErrorFilesetStudentEntity> setSpecificationAndSortCriteria(String sortCriteriaJson, String searchCriteriaListJson, ObjectMapper objectMapper, List<Sort.Order> sorts) {
    Specification<FinalErrorFilesetStudentEntity> schoolSpecs = null;
    try {
      RequestUtil.getSortCriteria(sortCriteriaJson, objectMapper, sorts);
      if (StringUtils.isNotBlank(searchCriteriaListJson)) {
        List<Search> searches = objectMapper.readValue(searchCriteriaListJson, new TypeReference<>() {
        });
        int i = 0;
        for (var search : searches) {
          schoolSpecs = getSpecifications(schoolSpecs, i, search, finalErrorFilesetStudentFilterSpecs);
          i++;
        }
      }
    } catch (JsonProcessingException e) {
      throw new GradDataCollectionAPIRuntimeException(e.getMessage());
    }
    return schoolSpecs;
  }
}
