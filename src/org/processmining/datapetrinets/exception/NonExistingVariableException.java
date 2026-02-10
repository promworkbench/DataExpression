package org.processmining.datapetrinets.exception;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;

public class NonExistingVariableException extends Exception {

	private static final long serialVersionUID = -2761643758833503382L;
	
	private final ImmutableSet<String> missingVariables;

	public NonExistingVariableException(ImmutableSet<String> missingVariables) {
		super("Missing variables "+ Joiner.on(",").join(missingVariables));
		this.missingVariables = missingVariables;		
	}

	public ImmutableSet<String> getMissingVariables() {
		return missingVariables;
	}

}
