package ca.bc.gov.educ.graddatacollection.api.exception;

/**
 * The type Pen reg api runtime exception.
 */
public class GradDataCollectionAPIRuntimeException extends RuntimeException {

  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = 5241655513745148898L;

  /**
   * Instantiates a new Pen reg api runtime exception.
   *
   * @param message the message
   */
  public GradDataCollectionAPIRuntimeException(String message) {
		super(message);
	}

  public GradDataCollectionAPIRuntimeException(Throwable exception) {
    super(exception);
  }

}
