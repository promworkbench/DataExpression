package org.processmining.datapetrinets.expression.syntax;

public class ExpressionVisitorException extends Exception {

	private static final long serialVersionUID = -1016843302334049812L;

	public ExpressionVisitorException() {
	}

	public ExpressionVisitorException(String message) {
		super(message);
	}

	public ExpressionVisitorException(Throwable cause) {
		super(cause);
	}

	public ExpressionVisitorException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExpressionVisitorException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
