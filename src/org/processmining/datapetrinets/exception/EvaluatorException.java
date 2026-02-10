package org.processmining.datapetrinets.exception;


public class EvaluatorException extends RuntimeException {

	private static final long serialVersionUID = 1608376231344658194L;

	public EvaluatorException() {
	}

	public EvaluatorException(String message) {
		super(message);
	}

	public EvaluatorException(Throwable cause) {
		super(cause);
	}

	public EvaluatorException(String message, Throwable cause) {
		super(message, cause);
	}

}
