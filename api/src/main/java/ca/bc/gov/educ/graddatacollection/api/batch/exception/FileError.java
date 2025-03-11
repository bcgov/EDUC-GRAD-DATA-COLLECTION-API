package ca.bc.gov.educ.graddatacollection.api.batch.exception;

import lombok.Getter;

/**
 * The enum File error.
 *
 * @author OM
 */
public enum FileError {
  /**
   * Upload file did not contain any content.
   */
  EMPTY_FILE("The DEM data file contains no records."),

  /**
   * The Invalid transaction code student details.
   */
  INVALID_TRANSACTION_CODE_STUDENT_DETAILS_DEM("Invalid transaction code on Detail record $? for student with Local ID $?. Must be one of \"D02\" or \"E02\"."),

  INVALID_TRANSACTION_CODE_STUDENT_DETAILS_CRS("Invalid transaction code on Detail record $? for student with Local ID $?. Must be one of \"D08\" or \"E08\"."),

  INVALID_TRANSACTION_CODE_STUDENT_DETAILS_XAM("Invalid transaction code on Detail record $? for student with Local ID $?. Must be one of \"E06\" or \"D06\"."),

  /**
   * The filetype ended in the wrong extension and may be the wrong filetype.
   */
  INVALID_FILE_EXTENSION("File extension invalid. Files must be of type \".dem\" or \".crs\" or \".xam\"."),

  NO_FILE_EXTENSION("No file extension provided. Files must be of type \".dem\" or \".crs\" or \".xam\"."),

  CONFLICT_FILE_ALREADY_IN_FLIGHT("File is already being processed for this school. School ministry code is: $?"),

  /**
   * No record for the provided school ID was found.
   */
  INVALID_SCHOOL("Unable to find a school record for school ministry code $?"),

  INVALID_FILENAME("File not processed due to invalid filename. Must be the school ministry code"),

  /**
   * The mincode on the uploaded document does not match the collection record.
   */
  MINCODE_MISMATCH("The school codes in your file do not match your school's code. Please ensure that all school codes in the file correspond to your school code."),

  /**
   * Invalid row length file error.
   * This will be thrown when any row in the given file is longer or shorter than expected.
   */
  INVALID_ROW_LENGTH("$?"),

  DUPLICATE_PEN_IN_DEM_FILE("The same PEN $? is appearing more than once in the DEM file on lines $?"),

  BLANK_PEN_IN_DEM_FILE("The PEN field is blank for one or more records in the DEM file on lines $?"),

  BLANK_PEN_IN_XAM_FILE("The PEN field is blank for one or more records in the XAM file on lines $?"),

  BLANK_PEN_IN_CRS_FILE("The PEN field is blank for one or more records in the CRS file on lines $?"),

  FILE_NOT_ALLOWED("File type not allowed"),
  COURSE_FILE_SESSION_ERROR(".CRS file must have at least 1 record with a current or future course session"),
  SCHOOL_IS_CLOSED("Invalid school provided - school is closed."),
  INVALID_SCHOOL_FOR_UPLOAD("This school is not eligible for graduation records and achievement data collection"),
  /**
   * School is opening.
   */
  SCHOOL_IS_OPENING("Invalid school provided - school is not yet open."),
  INVALID_SCHOOL_DATES("Invalid school dates - this was not expected."),
  SCHOOL_OUTSIDE_OF_DISTRICT("The school referenced in the uploaded file does not belong to district."),
  MISSING_MINCODE("No school ministry code found in file."),
  GENERIC_ERROR_MESSAGE("Unexpected failure during file processing.");

  /**
   * The Message.
   */
  @Getter
  private final String message;

  /**
   * Instantiates a new File error.
   *
   * @param message the message
   */
  FileError(final String message) {
    this.message = message;
  }
}
