package org.processmining.datapetrinets.exception;

public class VariableNotFoundException extends EvaluatorException {

	private static final long serialVersionUID = 6017940422103100221L;

	public VariableNotFoundException() {
		super();
	}

	public VariableNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}

	public VariableNotFoundException(String message) {
		super(message);
	}

	public VariableNotFoundException(Throwable cause) {
		super(cause);
	}

}
