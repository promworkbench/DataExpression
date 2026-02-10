package org.processmining.datapetrinets.expression;

import java.util.Map;

import org.processmining.datapetrinets.exception.EvaluatorException;

abstract public class AbstractGuardExpression implements GuardExpression {

	@Override
	public final boolean isTrue(VariableProvider variableProvider) throws EvaluatorException {
		return isTrue(variableProvider, FunctionProvider.EMPTY_PROVIDER);
	}

	@Override
	public final boolean isTrue(VariableProvider variableProvider, FunctionProvider functionProvider)
			throws EvaluatorException {
		Object result = evaluate(variableProvider, functionProvider);
		if (result instanceof Boolean) {
			return (Boolean) result;
		} else {
			throw new IllegalArgumentException("Expression does not evaluate to a Boolean!");
		}
	}

	@Override
	public final boolean isTrue(Map<String, Object> valueMap) throws EvaluatorException {
		return isTrue(GuardExpression.Factory.mapVariableProvider(valueMap));
	}

	@Override
	public final boolean isTrue() throws EvaluatorException {
		if (getNormalVariables().isEmpty() && getPrimeVariables().isEmpty()) {
			return isTrue(VariableProvider.EMPTY_PROVIDER);
		} else {
			// cannot be true, variables are missing
			return false;
		}
	}

	@Override
	public final Object evaluate(VariableProvider variableProvider) throws EvaluatorException {
		return evaluate(variableProvider, FunctionProvider.EMPTY_PROVIDER);
	}

	@Override
	public final Object evaluate(Map<String, Object> valueMap) throws EvaluatorException {
		return evaluate(GuardExpression.Factory.mapVariableProvider(valueMap));
	}

	@Override
	public final boolean isFalse(VariableProvider variableProvider) throws EvaluatorException {
		return !isTrue(variableProvider);
	}

	@Override
	public final boolean isFalse(VariableProvider variableProvider, FunctionProvider functionProvider)
			throws EvaluatorException {
		return !isTrue(variableProvider, functionProvider);
	}

	@Override
	public final boolean isFalse(Map<String, Object> valueMap) throws EvaluatorException {
		return !isTrue(valueMap);
	}

	@Override
	public final boolean isFalse() throws EvaluatorException {
		if (getNormalVariables().isEmpty() && getPrimeVariables().isEmpty()) {
			return isFalse(VariableProvider.EMPTY_PROVIDER);
		} else {
			// cannot be false, variables are missing
			return false;
		}
	}

}