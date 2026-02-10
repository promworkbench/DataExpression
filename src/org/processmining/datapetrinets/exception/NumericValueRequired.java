package org.processmining.datapetrinets.exception;

public class NumericValueRequired extends EvaluatorException {

	private static final long serialVersionUID = 6372508187571062616L;

	public NumericValueRequired() {
		super();
	}

	public NumericValueRequired(String message, Throwable cause) {
		super(message, cause);
	}

	public NumericValueRequired(String message) {
		super(message);
	}

	public NumericValueRequired(Throwable cause) {
		super(cause);
	}
	
}
