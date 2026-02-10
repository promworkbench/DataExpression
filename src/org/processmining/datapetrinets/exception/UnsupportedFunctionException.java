package org.processmining.datapetrinets.exception;

public class UnsupportedFunctionException extends EvaluatorException {

	private static final long serialVersionUID = -3542349715769903942L;

	public UnsupportedFunctionException() {
		super();
	}

	public UnsupportedFunctionException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedFunctionException(String message) {
		super(message);
	}

	public UnsupportedFunctionException(Throwable cause) {
		super(cause);
	}

}
