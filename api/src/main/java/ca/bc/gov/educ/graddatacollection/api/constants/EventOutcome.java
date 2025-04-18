package ca.bc.gov.educ.graddatacollection.api.constants;

/**
 * The enum Event outcome.
 */
public enum EventOutcome {
  INITIATE_SUCCESS,
  SAGA_COMPLETED,
  READ_DEM_STUDENTS_FOR_PROCESSING_SUCCESS,
  READ_ASSESSMENT_STUDENTS_FOR_PROCESSING_SUCCESS,
  READ_COURSE_STUDENTS_FOR_PROCESSING_SUCCESS,
  VALIDATE_DEM_STUDENT_SUCCESS_WITH_NO_ERROR,
  VALIDATE_DEM_STUDENT_SUCCESS_WITH_ERROR,
  DEM_STUDENT_CREATED_IN_GRAD,
  VALIDATE_COURSE_STUDENT_SUCCESS_WITH_NO_ERROR,
  VALIDATE_COURSE_STUDENT_SUCCESS_WITH_ERROR,
  COURSE_STUDENT_CREATED_IN_GRAD,
  COURSE_STUDENT_STATUS_IN_COLLECTION_UPDATED,
  VALIDATE_ASSESSMENT_STUDENT_SUCCESS_WITH_NO_ERROR,
  VALIDATE_ASSESSMENT_STUDENT_SUCCESS_WITH_ERROR,
  ASSESSMENT_STUDENT_REGISTRATION_WRITTEN_IN_EAS,
  ASSESSMENT_STUDENT_NOT_WRITTEN_DUE_TO_DEM_FILE_ERROR,
  ASSESSMENT_STUDENT_REGISTRATION_ALREADY_EXISTS_IN_EAS,
  ASSESSMENT_STATUS_IN_COLLECTION_UPDATED,
  DEM_STUDENT_STATUS_IN_COLLECTION_UPDATED,
  STUDENT_ALREADY_EXIST,
  STUDENT_REGISTRATION_CREATED,
  STUDENT_NOT_FOUND
}
