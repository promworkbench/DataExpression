package org.processmining.datapetrinets.expression;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.processmining.datapetrinets.expression.syntax.ExprFunction;
import org.processmining.datapetrinets.expression.syntax.ExprRoot;
import org.processmining.datapetrinets.expression.syntax.ExprVariable;
import org.processmining.datapetrinets.expression.syntax.ExpressionParserDefaultVisitor;
import org.processmining.datapetrinets.expression.syntax.ExpressionVisitorException;
import org.processmining.datapetrinets.expression.syntax.SimpleNode;

import com.google.common.collect.ImmutableSet;

public final class VariableCollector {
	
	private static final class VariableCollectingVisitor extends ExpressionParserDefaultVisitor {
		
		@SuppressWarnings("unchecked")
		public Object visit(ExprVariable node, Object data) {
			if (!(node.jjtGetParent() instanceof ExprFunction)) {
				((Collection<String>) data).add((String)node.jjtGetValue());
			}
			return null;
		}
		
		public Object visit(ExprRoot node, Object data) throws ExpressionVisitorException {
			if (node.jjtGetNumChildren() > 1) {
				throw new IllegalArgumentException("Invalid expression " + Printer.printCanonical(node)
						+ " should not have been parsed! Top level element is only allowed to have one child.");
			}
			return node.childrenAccept(this, data);
		}

		public Object visit(SimpleNode node, Object data) {
			throw new IllegalStateException("No unamed nodes allowed!");
		}

	}
	
	private static final VariableCollectingVisitor VARIABLE_VISITOR = new VariableCollectingVisitor();

	private VariableCollector() {
		super();
	}
	
	public static Set<String> collectAll(ExprRoot expression) {
		Set<String> variableSet = new HashSet<String>();
		try {
			VARIABLE_VISITOR.visit(expression, variableSet);
		} catch (ExpressionVisitorException e) {
			throw new RuntimeException("Exception while trying to print expression!", e);
		}
		return variableSet;
	}
	
	public static Set<String> collectPrimesOnly(ExprRoot expression) {
		Set<String> variables = collectAll(expression);
		Set<String> primeVariables = new HashSet<>();
		for (Iterator<String> iterator = variables.iterator(); iterator.hasNext();) {
			String varName = iterator.next();
			if (isPrimeVar(varName)) {
				iterator.remove();
				primeVariables.add(stripPrime(varName));
			}
		}		
		return ImmutableSet.copyOf(primeVariables);
	}

	public static Set<String> collectNormalOnly(ExprRoot expression) {
		Set<String> variables = collectAll(expression);
		for (Iterator<String> iterator = variables.iterator(); iterator.hasNext();) {
			String varName = iterator.next();
			if (isPrimeVar(varName)) {
				iterator.remove();
			}
		}		
		return ImmutableSet.copyOf(variables);
	}
	
	private static String stripPrime(String varName) {
		return varName.substring(0, varName.length() - 1);
	}
	
	private static boolean isPrimeVar(String varName) {
		return varName.charAt(varName.length() - 1) == '\'';
	}

}
