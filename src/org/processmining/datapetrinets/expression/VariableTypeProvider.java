package org.processmining.datapetrinets.expression;

public interface VariableTypeProvider {
	
	Class<?> getType(String variableName);

}
