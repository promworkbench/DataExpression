package org.processmining.datapetrinets.exception;

public class BooleanValueRequired extends EvaluatorException {

	private static final long serialVersionUID = -2018752011972934896L;

	public BooleanValueRequired() {
	}

	public BooleanValueRequired(String message) {
		super(message);
	}

	public BooleanValueRequired(Throwable cause) {
		super(cause);
	}

	public BooleanValueRequired(String message, Throwable cause) {
		super(message, cause);
	}

}
