package org.processmining.datapetrinets.expression;

import org.processmining.datapetrinets.exception.UnsupportedFunctionException;

public class FunctionProviderBasicMath implements FunctionProvider {

	public Object calculate(String function, Object[] parameters) throws UnsupportedFunctionException {
		switch (function) {
			case "min" :
				double min = Double.MAX_VALUE;
				for (Object obj : parameters) {
					if (obj instanceof Number) {
						min = Math.min(min, ((Number) obj).doubleValue());
					} else {
						throw new UnsupportedFunctionException("Only defined for numeric values " + function);
					}
				}
				return new Double(min);

			case "max" :
				double max = -Double.MAX_VALUE;
				for (Object obj : parameters) {
					if (obj instanceof Number) {
						max = Math.max(max, ((Number) obj).doubleValue());
					} else {
						throw new UnsupportedFunctionException("Only defined for numeric values " + function);
					}
				}
				return new Double(max);

			case "abs" :
				if (parameters.length != 1 || !(parameters[0] instanceof Number)) {
					throw new UnsupportedFunctionException("Unary function for numeric values " + function);
				}
				return Math.abs(((Number) parameters[0]).doubleValue());

			default :
				throw new UnsupportedFunctionException("Unknown function " + function);
		}
	}

}