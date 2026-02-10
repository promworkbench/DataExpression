package org.processmining.datapetrinets.expression;

import java.util.Map;

import org.processmining.datapetrinets.exception.VariableNotFoundException;

public interface VariableProvider {

	public class DefaultVariableProvider implements VariableProvider {

		private final Map<String, Object> valueMap;

		public DefaultVariableProvider(Map<String, Object> valueMap) {
			this.valueMap = valueMap;
		}

		public Object getValue(String variableName) throws VariableNotFoundException {
			Object value = valueMap.get(variableName);
			if (value == null) {
				throw new VariableNotFoundException(
						String.format("Variable %s is not found in map %s", variableName, valueMap));
			} else {
				return value;
			}
		}

	}

	static VariableProvider EMPTY_PROVIDER = new VariableProvider() {

		public Object getValue(String variableName) throws VariableNotFoundException {
			return new VariableNotFoundException("No variable values known!");
		}
	};

	Object getValue(String variableName) throws VariableNotFoundException;

}