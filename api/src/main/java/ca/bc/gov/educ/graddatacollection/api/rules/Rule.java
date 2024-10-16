package ca.bc.gov.educ.graddatacollection.api.rules;

import java.util.List;

public interface Rule<U, T> {
  boolean shouldExecute(U u, List<T> list);

  List<T> executeValidation(U u);
}
