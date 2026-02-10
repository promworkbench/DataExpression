package org.processmining.datapetrinets.expression;

import org.processmining.datapetrinets.exception.UnsupportedFunctionException;


public interface FunctionProvider {
	
	public static final FunctionProvider EMPTY_PROVIDER = new FunctionProvider() {

		public Object calculate(String function, Object[] parameters) throws UnsupportedFunctionException {
			throw new UnsupportedFunctionException();
		}

	};
	
	public static final FunctionProvider BASIC_MATH = new FunctionProviderBasicMath();
	
	Object calculate(String function, Object[] parameters) throws UnsupportedFunctionException;
	
}