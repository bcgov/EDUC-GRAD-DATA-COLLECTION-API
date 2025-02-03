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
  EMPTY_FILE("The uploaded file is empty."),

  /**
   * The Invalid transaction code student details.
   */
  INVALID_TRANSACTION_CODE_STUDENT_DETAILS("Invalid transaction code on Detail record $? for student with Local ID $?"),

  /**
   * The filetype ended in the wrong extension and may be the wrong filetype.
   */
  INVALID_FILE_EXTENSION("File extension invalid. Files must be of type \".dem\" or \".crs\" or \".xam\"."),

  NO_FILE_EXTENSION("No file extension provided. Files must be of type \".dem\" or \".crs\" or \".xam\"."),

  CONFLICT_FILE_ALREADY_IN_FLIGHT("File is already being processed for this school. Mincode is: $?"),

  /**
   * No record for the provided school ID was found.
   */
  INVALID_SCHOOL("Unable to find a school record for mincode $?"),

  /**
   * The mincode on the uploaded document does not match the collection record.
   */
  MINCODE_MISMATCH("The uploaded file is for another school. Please upload a file for $?"),

  /**
   * Invalid row length file error.
   * This will be thrown when any row in the given file is longer or shorter than expected.
   */
  INVALID_ROW_LENGTH("$?"),

  FILE_NOT_ALLOWED("File type not allowed"),
  COURSE_FILE_SESSION_ERROR(".CRS file must have at least 1 record with a current or future course session"),
  SCHOOL_IS_CLOSED("Invalid school provided - school is closed."),
  /**
   * School is opening.
   */
  SCHOOL_IS_OPENING("Invalid school provided - school is not yet open."),
  INVALID_SCHOOL_DATES("Invalid school dates - this was not expected."),
  SCHOOL_OUTSIDE_OF_DISTRICT("The school referenced in the uploaded file does not belong to district."),
  MISSING_MINCODE("No mincode found in file."),
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
