package org.processmining.datapetrinets.expression;

import java.util.HashSet;
import java.util.Set;

import org.processmining.datapetrinets.expression.syntax.ExprFunction;
import org.processmining.datapetrinets.expression.syntax.ExprLitBoolean;
import org.processmining.datapetrinets.expression.syntax.ExprLitDouble;
import org.processmining.datapetrinets.expression.syntax.ExprLitInteger;
import org.processmining.datapetrinets.expression.syntax.ExprLitString;
import org.processmining.datapetrinets.expression.syntax.ExprRoot;
import org.processmining.datapetrinets.expression.syntax.ExpressionParserDefaultVisitor;
import org.processmining.datapetrinets.expression.syntax.ExpressionVisitorException;
import org.processmining.datapetrinets.expression.syntax.SimpleNode;

public final class LiteralValueCollector {

	private static final class LiteralCollectingVisitor<T> extends ExpressionParserDefaultVisitor {

		private Set<T> literalSet;
		private Class<T> requestedType;

		public LiteralCollectingVisitor(Class<T> type, Set<T> literalSet) {
			this.requestedType = type;
			this.literalSet = literalSet;
		}

		@SuppressWarnings("unchecked")
		public Object visit(ExprLitBoolean node, Object data) {
			if (!(node.jjtGetParent() instanceof ExprFunction)) {
				if (requestedType == Boolean.class) {
					literalSet.add((T) node.jjtGetValue());
				}
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		public Object visit(ExprLitString node, Object data) {
			if (!(node.jjtGetParent() instanceof ExprFunction)) {
				if (requestedType == String.class) {
					literalSet.add((T) unquote((String) node.jjtGetValue()));
				}
			}
			return null;
		}

		private static String unquote(String literal) {
			return literal.substring(1, literal.length() - 1);
		}

		@SuppressWarnings("unchecked")
		public Object visit(ExprLitDouble node, Object data) {
			if (!(node.jjtGetParent() instanceof ExprFunction)) {
				if (requestedType == Double.class) {
					literalSet.add((T) node.jjtGetValue());
				}
			}
			return null;
		}

		@SuppressWarnings("unchecked")
		public Object visit(ExprLitInteger node, Object data) {
			if (!(node.jjtGetParent() instanceof ExprFunction)) {
				if (requestedType == Integer.class) {
					literalSet.add((T) node.jjtGetValue());
				}
			}
			return null;
		}

		public Object visit(ExprRoot node, Object data) throws ExpressionVisitorException {
			if (node.jjtGetNumChildren() > 1) {
				throw new IllegalArgumentException("Invalid expression " + Printer.printCanonical(node)
						+ " should not have been parsed! Top level element is only allowed to have one child.");
			}
			return node.jjtGetChild(0).jjtAccept(this, data);
		}

		public Object visit(SimpleNode node, Object data) {
			throw new IllegalStateException("No unamed nodes allowed!");
		}

	}

	private LiteralValueCollector() {
		super();
	}

	public static <T> Set<T> collectAll(ExprRoot expression, Class<T> type) {
		Set<T> variableSet = new HashSet<T>();
		try {
			new LiteralCollectingVisitor<T>(type, variableSet).visit(expression, null);
		} catch (ExpressionVisitorException e) {
			throw new RuntimeException("Exception while trying to print expression!", e);
		}
		return variableSet;
	}

}
