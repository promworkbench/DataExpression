package org.processmining.datapetrinets.expression;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

import javax.lang.model.SourceVersion;

import org.processmining.datapetrinets.exception.EvaluatorException;
import org.processmining.datapetrinets.expression.syntax.ExprAnd;
import org.processmining.datapetrinets.expression.syntax.ExprOr;
import org.processmining.datapetrinets.expression.syntax.ExprRoot;
import org.processmining.datapetrinets.expression.syntax.ExpressionParserTreeConstants;
import org.processmining.datapetrinets.expression.syntax.ExpressionParserVisitor;
import org.processmining.datapetrinets.expression.syntax.ExpressionVisitorException;
import org.processmining.datapetrinets.expression.syntax.Node;
import org.processmining.datapetrinets.expression.syntax.ParseException;

/**
 * A guard expression defined over some set of variables. Please use the
 * {@link Factory} methods to obtain an instance. You may use the
 * {@link Operation} methods to compose expressions. The syntax for guard
 * expressions is defined in 'expression.jjt' in the package
 * {@link org.processmining.datapetrinets.expression.syntax.}.
 * <p>
 * Please note, this class was previously part of the DataPetriNets package.
 * That is why it uses the package name 'datapetrinets'.
 * 
 * @author F. Mannhardt
 *
 */
public interface GuardExpression {

	/**
	 * Standard way of creating {@link GuardExpression} instances
	 */
	public static final class Factory {

		private static final GuardExpression FALSE_INSTANCE;
		private static final GuardExpression TRUE_INSTANCE;

		static {
			try {
				TRUE_INSTANCE = newInstance("true");
			} catch (ParseException e) {
				throw new RuntimeException("Failed to parse 'true'!", e);
			}
			try {
				FALSE_INSTANCE = newInstance("false");
			} catch (ParseException e) {
				throw new RuntimeException("Failed to parse 'false'!", e);
			}
		}

		private Factory() {
		}

		private static final char DEFAULT_REPLACEMENT_CHAR = '_';

		public static GuardExpression newInstance(String expression) throws ParseException {
			return new GuardExpressionImpl(expression);
		}

		public static GuardExpression newInstance(ExprRoot expression) {
			return new GuardExpressionImpl(expression);
		}

		public static GuardExpression trueInstance() {
			return TRUE_INSTANCE;
		}

		public static GuardExpression falseInstance() {
			return FALSE_INSTANCE;

		}

		public static boolean isValidVariableIdentifier(String id) {
			return SourceVersion.isName(id) || SourceVersion.isKeyword(id);
		}

		public static String transformToVariableIdentifier(String id) {
			StringBuilder sb = new StringBuilder();
			if (id.isEmpty()) {
				throw new IllegalArgumentException(
						"Cannot transform an empty String into a valid variable identifier!");
			}
			int utfChar = id.codePointAt(0);
			for (int i = 0; i < id.length(); i += Character.charCount(utfChar)) {
				utfChar = id.codePointAt(i);
				if (Character.isJavaIdentifierPart(utfChar)) {
					sb.appendCodePoint(utfChar);
				} else {
					String s = new String(Character.toChars(utfChar));
					try {
						sb.append(URLEncoder.encode(s, "utf-8").replace('%', '$'));
					} catch (UnsupportedEncodingException e) {
						sb.append(DEFAULT_REPLACEMENT_CHAR);
					}
				}
			}
			if (!Character.isJavaIdentifierStart(sb.codePointAt(0))) {
				// If the start character is not valid, then insert a valid start character beforehand
				sb.insert(0, DEFAULT_REPLACEMENT_CHAR);
			}
			return sb.toString();
		}

		public static VariableProvider mapVariableProvider(Map<String, Object> valueMap) {
			return new VariableProvider.DefaultVariableProvider(valueMap);
		}

	}

	/**
	 * Operations that mutate, combine, etc. {@link GuardExpression} instances
	 */
	public static final class Operation {

		private Operation() {
		}

		/**
		 * Combines both {@link GuardExpression}s with logical AND
		 * 
		 * @param lhs
		 *            non-null {@link GuardExpression}
		 * @param rhs
		 *            non-null {@link GuardExpression}
		 * @return lhs && rhs
		 */
		public static GuardExpression and(GuardExpression lhs, GuardExpression rhs) {
			if (lhs == null) {
				throw new NullPointerException("LHS is NULL");
			}
			if (rhs == null) {
				throw new NullPointerException("RHS is NULL");
			}
			if (lhs instanceof GuardExpressionImpl && rhs instanceof GuardExpressionImpl) {
				GuardExpressionImpl lhsGuard = (GuardExpressionImpl) lhs;
				GuardExpressionImpl rhsGuard = (GuardExpressionImpl) rhs;
				return combineWithoutParsing(lhsGuard, rhsGuard, new ExprAnd(ExpressionParserTreeConstants.JJTAND));
			} else {
				try {
					return new GuardExpressionImpl(
							"(" + lhs.toCanonicalString() + ")&&(" + rhs.toCanonicalString() + ")");
				} catch (ParseException e) {
					throw new RuntimeException("Could not combine two expression!", e);
				}
			}
		}

		/**
		 * Combines both {@link GuardExpression}s with logical OR
		 * 
		 * @param lhs
		 *            non-null {@link GuardExpression}
		 * @param rhs
		 *            non-null {@link GuardExpression}
		 * @return lhs || rhs
		 */
		public static GuardExpression or(GuardExpression lhs, GuardExpression rhs) {
			if (lhs == null) {
				throw new NullPointerException("LHS is NULL");
			}
			if (rhs == null) {
				throw new NullPointerException("RHS is NULL");
			}
			if (lhs instanceof GuardExpressionImpl && rhs instanceof GuardExpressionImpl) {
				GuardExpressionImpl lhsGuard = (GuardExpressionImpl) lhs;
				GuardExpressionImpl rhsGuard = (GuardExpressionImpl) rhs;
				return combineWithoutParsing(lhsGuard, rhsGuard, new ExprOr(ExpressionParserTreeConstants.JJTOR));
			} else {
				try {
					return new GuardExpressionImpl(
							"(" + lhs.toCanonicalString() + ")||(" + rhs.toCanonicalString() + ")");
				} catch (ParseException e) {
					throw new RuntimeException("Could not combine two expression!", e);
				}
			}
		}
		
		/**
		 * Negates {@link GuardExpression}
		 * 
		 * @param guard
		 *            non-null {@link GuardExpression}
		 * @return !(lhs)
		 */		
		public static GuardExpression not(GuardExpression guard) {
			if (guard== null) {
				throw new NullPointerException("Expression is NULL");
			}			
			try {
				return new GuardExpressionImpl("!(" + guard.toCanonicalString() + ")");
			} catch (ParseException e) {
				throw new RuntimeException("Could not negate expression!", e);
			}
		}		

		private static GuardExpression combineWithoutParsing(GuardExpressionImpl lhs, GuardExpressionImpl rhs,
				Node booleanOp) {
			Node lhsChild = lhs.getExpression().jjtGetChild(0);
			lhsChild.jjtSetParent(booleanOp);
			booleanOp.jjtAddChild(lhsChild, 0);
			Node rhsChild = rhs.getExpression().jjtGetChild(0);
			rhsChild.jjtSetParent(booleanOp);
			booleanOp.jjtAddChild(rhsChild, 1);

			ExprRoot newRoot = new ExprRoot(ExpressionParserTreeConstants.JJTROOT);
			booleanOp.jjtSetParent(newRoot);
			newRoot.jjtAddChild(booleanOp, 0);

			return new GuardExpressionImpl(newRoot);
		}

	}

	Object visit(ExpressionParserVisitor visitor, Object data) throws ExpressionVisitorException;

	Object evaluate(VariableProvider variableProvider, FunctionProvider functionProvider) throws EvaluatorException;

	Object evaluate(VariableProvider variableProvider) throws EvaluatorException;

	Object evaluate(Map<String, Object> valueMap) throws EvaluatorException;

	boolean isTrue(VariableProvider variableProvider, FunctionProvider functionProvider) throws EvaluatorException;

	boolean isTrue(VariableProvider variableProvider) throws EvaluatorException;

	boolean isTrue(Map<String, Object> valueMap) throws EvaluatorException;

	/**
	 * @return whether the expression is always true, returns false in case
	 *         variables are used
	 * @throws EvaluatorException
	 *             never
	 */
	boolean isTrue() throws EvaluatorException;

	boolean isFalse(VariableProvider variableProvider, FunctionProvider functionProvider) throws EvaluatorException;

	boolean isFalse(VariableProvider variableProvider) throws EvaluatorException;

	boolean isFalse(Map<String, Object> valueMap) throws EvaluatorException;

	/**
	 * @return whether the expression is always false, returns false in case
	 *         variables are used
	 * @throws EvaluatorException
	 *             never
	 */
	boolean isFalse() throws EvaluatorException;

	/**
	 * @return the list of normal (non-prime) variables
	 */
	Set<String> getNormalVariables();

	/**
	 * Returns the list of prime variable identifiers (x' is the prime version
	 * of x). The variable identifiers are returned without the prime symbol (x
	 * instead of x' would be returned).
	 * 
	 * @return the list of prime variables
	 */
	Set<String> getPrimeVariables();

	<T> Set<T> getLiteralValues(Class<T> type);

	String toCanonicalString();

	String toPrettyString(int spaces);

	String toTreeLikeString(int indent);

}
