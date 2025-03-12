package ca.bc.gov.educ.graddatacollection.api.constants.v1;

public final class URL {

  private URL(){
  }

  public static final String BASE_URL = "/api/v1/grad-data-collection";
  public static final String BASE_URL_ERROR_FILESET = BASE_URL + "/error-fileset";
  public static final String BASE_URL_FILESET = BASE_URL + "/fileset";
  public static final String PAGINATED = "/paginated";
  public static final String GET_STUDENT = "/get-student";
  public static final String VALIDATION_ISSUE_TYPE_CODES = "/validation-issue-type-codes";
  public static final String VALIDATION_FIELD_CODES = "/validation-field-codes";
  public static final String BASE_URL_REPORT_GENERATION = BASE_URL + "/reportGeneration";

}
