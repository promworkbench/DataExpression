package org.processmining.datapetrinets.expression;

import java.lang.ref.SoftReference;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import org.processmining.datapetrinets.exception.BooleanValueRequired;
import org.processmining.datapetrinets.exception.EvaluatorException;
import org.processmining.datapetrinets.exception.NumericValueRequired;
import org.processmining.datapetrinets.expression.syntax.ExprAnd;
import org.processmining.datapetrinets.expression.syntax.ExprAtLeast;
import org.processmining.datapetrinets.expression.syntax.ExprAtMost;
import org.processmining.datapetrinets.expression.syntax.ExprDiv;
import org.processmining.datapetrinets.expression.syntax.ExprEqual;
import org.processmining.datapetrinets.expression.syntax.ExprFunction;
import org.processmining.datapetrinets.expression.syntax.ExprGreaterThan;
import org.processmining.datapetrinets.expression.syntax.ExprLessThan;
import org.processmining.datapetrinets.expression.syntax.ExprLitBoolean;
import org.processmining.datapetrinets.expression.syntax.ExprLitDouble;
import org.processmining.datapetrinets.expression.syntax.ExprLitInteger;
import org.processmining.datapetrinets.expression.syntax.ExprLitNull;
import org.processmining.datapetrinets.expression.syntax.ExprLitString;
import org.processmining.datapetrinets.expression.syntax.ExprMinus;
import org.processmining.datapetrinets.expression.syntax.ExprMult;
import org.processmining.datapetrinets.expression.syntax.ExprNegation;
import org.processmining.datapetrinets.expression.syntax.ExprNot;
import org.processmining.datapetrinets.expression.syntax.ExprNotEqual;
import org.processmining.datapetrinets.expression.syntax.ExprOr;
import org.processmining.datapetrinets.expression.syntax.ExprPlus;
import org.processmining.datapetrinets.expression.syntax.ExprRoot;
import org.processmining.datapetrinets.expression.syntax.ExprVariable;
import org.processmining.datapetrinets.expression.syntax.ExpressionParserVisitor;
import org.processmining.datapetrinets.expression.syntax.ExpressionVisitorException;
import org.processmining.datapetrinets.expression.syntax.Node;
import org.processmining.datapetrinets.expression.syntax.SimpleNode;

import com.google.common.math.DoubleMath;

/**
 * Class with static methods related to evaluating an {@link GuardExpression}.
 * 
 * @author F. Mannhardt
 * 
 */
public final class Evaluator {

	public interface Provider {

		VariableProvider getVariableProvider();

		FunctionProvider getFunctionProvider();

	}

	private interface BinaryOperation {

		Object evaluate(Object lhs, Object rhs);

	}

	public static final double SOLVING_PRECISION = 0.000001d;

	public static final String OLD_DATE_FORMAT = "EEE MMM dd kk:mm:ss zzz yyyy";
	public static final String STANDARD_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

	private static final ThreadLocal<SoftReference<DateFormat>> THREAD_LOCAL_OLD_DF = new ThreadLocal<>();
	private static final ThreadLocal<SoftReference<DateFormat>> THREAD_LOCAL_STANDARD_DF = new ThreadLocal<>();

	private static DateFormat getOldDateFormat() {
		SoftReference<DateFormat> softReference = THREAD_LOCAL_OLD_DF.get();
		if (softReference != null) {
			DateFormat dateFormat = softReference.get();
			if (dateFormat != null) {
				return dateFormat;
			}
		}
		DateFormat result = new SimpleDateFormat(OLD_DATE_FORMAT, Locale.US);
		softReference = new SoftReference<>(result);
		THREAD_LOCAL_OLD_DF.set(softReference);
		return result;
	}

	private static DateFormat getStandardDateFormat() {
		SoftReference<DateFormat> softReference = THREAD_LOCAL_STANDARD_DF.get();
		if (softReference != null) {
			DateFormat dateFormat = softReference.get();
			if (dateFormat != null) {
				return dateFormat;
			}
		}
		DateFormat result = new SimpleDateFormat(STANDARD_DATE_FORMAT, Locale.US);
		softReference = new SoftReference<>(result);
		THREAD_LOCAL_STANDARD_DF.set(softReference);
		return result;
	}

	public static class Visitor implements ExpressionParserVisitor {

		private static final BinaryOperation atLeast = new BinaryOperation() {

			public Object evaluate(Object lhs, Object rhs) {
				try {
					int x = compareObjects(lhs, rhs);
					return x == 0 || x > 0;
				} catch (NumericValueRequired e) {
					throw new NumericValueRequired(
							String.format(
									"Unable to determine binary expression: < %s %s %s >. Operation not defined for non-numeric values!",
									lhs, toString(), rhs), e);
				}
			}

			@Override
			public String toString() {
				return ">=";
			}

		};

		private static final BinaryOperation atMost = new BinaryOperation() {

			public Object evaluate(Object lhs, Object rhs) {
				try {
					int x = compareObjects(lhs, rhs);
					return x == 0 || x < 0;
				} catch (NumericValueRequired e) {
					throw new NumericValueRequired(
							String.format(
									"Unable to determine binary expression: < %s %s %s >. Operation not defined for non-numeric values!",
									lhs, toString(), rhs), e);
				}
			}

			@Override
			public String toString() {
				return "<=";
			}

		};

		private static final BinaryOperation lessThan = new BinaryOperation() {

			public Object evaluate(Object lhs, Object rhs) {
				try {
					int x = compareObjects(lhs, rhs);
					return x < 0;
				} catch (NumericValueRequired e) {
					throw new NumericValueRequired(
							String.format(
									"Unable to determine binary expression: < %s %s %s >. Operation not defined for non-numeric values!",
									lhs, toString(), rhs), e);
				}
			}

			@Override
			public String toString() {
				return "<";
			}

		};

		private static final BinaryOperation greaterThan = new BinaryOperation() {

			public Object evaluate(Object lhs, Object rhs) {
				try {
					int x = compareObjects(lhs, rhs);
					return x > 0;
				} catch (NumericValueRequired e) {
					throw new NumericValueRequired(
							String.format(
									"Unable to determine binary expression: < %s %s %s >. Operation not defined for non-numeric values!",
									lhs, toString(), rhs), e);
				}
			}

			@Override
			public String toString() {
				return ">";
			}

		};

		private static final BinaryOperation equal = new BinaryOperation() {

			public Object evaluate(Object lhs, Object rhs) {
				if (bothNumeric(lhs, rhs)) {
					return DoubleMath.fuzzyEquals(((Number) lhs).doubleValue(), ((Number) rhs).doubleValue(),
							Evaluator.SOLVING_PRECISION);
				} else {
					// NULL safe equals from Java 7
					return Objects.equals(lhs, rhs);
				}
			}

			@Override
			public String toString() {
				return "==";
			}

		};

		private static final BinaryOperation div = new BinaryOperation() {

			public Object evaluate(Object lhs, Object rhs) {
				if (bothNumeric(lhs, rhs)) {
					return ((Number) lhs).doubleValue() / ((Number) rhs).doubleValue();
				} else {
					throw new NumericValueRequired(
							String.format(
									"Unable to determine binary expression: < %s %s %s >. Operation not defined for non-numeric values!",
									lhs, toString(), rhs));
				}
			}

			@Override
			public String toString() {
				return "/";
			}

		};

		private static final BinaryOperation mult = new BinaryOperation() {

			public Object evaluate(Object lhs, Object rhs) {
				if (bothNumeric(lhs, rhs)) {
					return ((Number) lhs).doubleValue() * ((Number) rhs).doubleValue();
				} else {
					throw new NumericValueRequired(
							String.format(
									"Unable to determine binary expression: < %s %s %s >. Operation not defined for non-numeric values!",
									lhs, toString(), rhs));
				}				
			}

			@Override
			public String toString() {
				return "*";
			}

		};

		private static final BinaryOperation plus = new BinaryOperation() {

			public Object evaluate(Object lhs, Object rhs) {
				if (bothNumeric(lhs, rhs)) {
					return ((Number) lhs).doubleValue() + ((Number) rhs).doubleValue();
				} else {
					throw new NumericValueRequired(
							String.format(
									"Unable to determine binary expression: < %s %s %s >. Operation not defined for non-numeric values!",
									lhs, toString(), rhs));
				}
			}

			@Override
			public String toString() {
				return "+";
			}

		};

		private static final BinaryOperation minus = new BinaryOperation() {

			public Object evaluate(Object lhs, Object rhs) {
				if (bothNumeric(lhs, rhs)) {
					return ((Number) lhs).doubleValue() - ((Number) rhs).doubleValue();
				} else {
					throw new NumericValueRequired(
							String.format(
									"Unable to determine binary expression: < %s %s %s >. Operation not defined for non-numeric values!",
									lhs, toString(), rhs));
				}
			}

			@Override
			public String toString() {
				return "-";
			}

		};

		protected static int compareObjects(Object lhs, Object rhs) {
			int x;
			if (bothNumeric(lhs, rhs)) {
				x = DoubleMath.fuzzyCompare(((Number) lhs).doubleValue(), ((Number) rhs).doubleValue(),
						Evaluator.SOLVING_PRECISION);
			} else if (bothString(lhs, rhs)) {
				x = ((String) lhs).compareTo(((String) rhs));
			} else {
				throw new NumericValueRequired();
			}
			return x;
		}

		protected static boolean bothString(Object lhs, Object rhs) {
			return lhs instanceof String && rhs instanceof String;
		}

		protected static boolean bothNumeric(Object lhs, Object rhs) {
			return lhs instanceof Number && rhs instanceof Number;
		}

		protected static Object tryParseDateToMillis(Object obj) {
			if (obj instanceof Date) {
				return ((Date) obj).getTime();
			}
			if (obj instanceof Number) {
				// Comparison of a number (unix timestamp) with a date 
				return obj;
			}
			if (obj instanceof String) {
				ParsePosition pos = new ParsePosition(0);
				Date date = getStandardDateFormat().parse((String) obj, pos);
				if (date != null) {
					return date.getTime();
				} else {
					pos.setIndex(0);
					date = getOldDateFormat().parse((String) obj, pos);
					if (date != null) {
						return date.getTime();
					}
				}
			}
			// Fallback, we could not parse the date
			return obj;
		}

		private static Object calcBinaryExpression(Object lhs, Object rhs, BinaryOperation op)
				throws EvaluatorException {

			// Handle dates
			if (lhs instanceof Date || rhs instanceof Date) {
				lhs = tryParseDateToMillis(lhs);
				rhs = tryParseDateToMillis(rhs);
			}

			return op.evaluate(lhs, rhs);
		}

		public Object visit(ExprLitNull node, Object data) {
			return null;
		}

		public Object visit(ExprLitBoolean node, Object data) {
			return Boolean.valueOf((String) node.jjtGetValue());
		}

		public Object visit(ExprLitString node, Object data) {
			return unquote((String) node.jjtGetValue());
		}

		private static String unquote(String literal) {
			return literal.substring(1, literal.length() - 1);
		}

		public Object visit(ExprLitDouble node, Object data) {
			return Double.parseDouble((String) node.jjtGetValue());
		}

		public Object visit(ExprLitInteger node, Object data) {
			return Long.parseLong((String) node.jjtGetValue());
		}

		public Object visit(ExprVariable node, Object data) throws ExpressionVisitorException {
			Provider provider = (Provider) data;
			return provider.getVariableProvider().getValue((String) node.jjtGetValue());
		}

		public Object visit(ExprNot node, Object data) throws ExpressionVisitorException {
			Object arg = node.jjtGetChild(0).jjtAccept(this, data);
			if (arg instanceof Boolean) {
				return !((Boolean) arg);
			} else {
				throw new EvaluatorException("'!' is not defined for non-boolean values!");
			}
		}

		public Object visit(ExprNegation node, Object data) throws ExpressionVisitorException {
			Object arg = node.jjtGetChild(0).jjtAccept(this, data);
			if (arg instanceof Number) {
				return Double.valueOf(-((Number) arg).doubleValue());
			} else {
				throw new EvaluatorException("Negation is not defined for non-numeric values!");
			}
		}

		public Object visit(ExprDiv node, Object data) throws ExpressionVisitorException {
			Object lhs = node.jjtGetChild(0).jjtAccept(this, data);
			Object rhs = node.jjtGetChild(1).jjtAccept(this, data);
			return calcBinaryExpression(lhs, rhs, div);
		}

		public Object visit(ExprMult node, Object data) throws ExpressionVisitorException {
			Object lhs = node.jjtGetChild(0).jjtAccept(this, data);
			Object rhs = node.jjtGetChild(1).jjtAccept(this, data);
			return calcBinaryExpression(lhs, rhs, mult);
		}

		public Object visit(ExprMinus node, Object data) throws ExpressionVisitorException {
			Object lhs = node.jjtGetChild(0).jjtAccept(this, data);
			Object rhs = node.jjtGetChild(1).jjtAccept(this, data);
			return calcBinaryExpression(lhs, rhs, minus);
		}

		public Object visit(ExprPlus node, Object data) throws ExpressionVisitorException {
			Object lhs = node.jjtGetChild(0).jjtAccept(this, data);
			Object rhs = node.jjtGetChild(1).jjtAccept(this, data);
			return calcBinaryExpression(lhs, rhs, plus);
		}

		public Object visit(ExprAtLeast node, Object data) throws ExpressionVisitorException {
			Object lhs = node.jjtGetChild(0).jjtAccept(this, data);
			Object rhs = node.jjtGetChild(1).jjtAccept(this, data);
			return calcBinaryExpression(lhs, rhs, atLeast);
		}

		public Object visit(ExprGreaterThan node, Object data) throws ExpressionVisitorException {
			Object lhs = node.jjtGetChild(0).jjtAccept(this, data);
			Object rhs = node.jjtGetChild(1).jjtAccept(this, data);
			return calcBinaryExpression(lhs, rhs, greaterThan);
		}

		public Object visit(ExprAtMost node, Object data) throws ExpressionVisitorException {
			Object lhs = node.jjtGetChild(0).jjtAccept(this, data);
			Object rhs = node.jjtGetChild(1).jjtAccept(this, data);
			return calcBinaryExpression(lhs, rhs, atMost);
		}

		public Object visit(ExprLessThan node, Object data) throws ExpressionVisitorException {
			Object lhs = node.jjtGetChild(0).jjtAccept(this, data);
			Object rhs = node.jjtGetChild(1).jjtAccept(this, data);
			return calcBinaryExpression(lhs, rhs, lessThan);
		}

		public Object visit(ExprNotEqual node, Object data) throws ExpressionVisitorException {
			Object lhs = node.jjtGetChild(0).jjtAccept(this, data);
			Object rhs = node.jjtGetChild(1).jjtAccept(this, data);
			return !((Boolean) equal.evaluate(lhs, rhs));
		}

		public Object visit(ExprEqual node, Object data) throws ExpressionVisitorException {
			Object lhs = node.jjtGetChild(0).jjtAccept(this, data);
			Object rhs = node.jjtGetChild(1).jjtAccept(this, data);
			return equal.evaluate(lhs, rhs);
		}

		public Object visit(ExprAnd node, Object data) throws ExpressionVisitorException {
			Object lhs = node.jjtGetChild(0).jjtAccept(this, data);

			if (lhs instanceof Boolean && !(Boolean) lhs) {
				// Short circuit AND
				return Boolean.FALSE;
			} else {
				Object rhs = node.jjtGetChild(1).jjtAccept(this, data);

				if (!(lhs instanceof Boolean && rhs instanceof Boolean)) {
					throw new BooleanValueRequired(
							String.format(
									"Unable to determine < %s || %s >. Operation not defined for non-boolean values!",
									lhs, rhs));
				}

				return (Boolean) lhs && (Boolean) rhs;

			}
		}

		public Object visit(ExprOr node, Object data) throws ExpressionVisitorException {
			Object lhs = node.jjtGetChild(0).jjtAccept(this, data);

			if (lhs instanceof Boolean && (Boolean) lhs) {
				// Short circuit OR
				return Boolean.TRUE;
			} else {
				Object rhs = node.jjtGetChild(1).jjtAccept(this, data);

				if (!(lhs instanceof Boolean && rhs instanceof Boolean)) {
					throw new BooleanValueRequired(
							String.format(
									"Unable to determine < %s || %s >. Operation not defined for non-boolean values!",
									lhs, rhs));
				}

				return (Boolean) lhs || (Boolean) rhs;

			}
		}

		public Object visit(ExprRoot node, Object data) throws ExpressionVisitorException {
			if (node.jjtGetNumChildren() > 1) {
				throw new ExpressionVisitorException("Invalid expression " + Printer.printCanonical(node)
						+ " should not have been parsed! Top level element is only allowed to have one child.");
			}
			return node.jjtGetChild(0).jjtAccept(this, data);
		}

		public Object visit(SimpleNode node, Object data) throws ExpressionVisitorException {
			throw new ExpressionVisitorException("No unamed nodes allowed!");
		}

		public Object visit(ExprFunction node, Object data) throws ExpressionVisitorException {
			Provider provider = (Provider) data;
			Object[] params = new Object[node.jjtGetNumChildren()];
			for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
				Node child = node.jjtGetChild(i);
				if (child instanceof ExprVariable) {
					// Use the identifier instead of resolving the variable
					params[i] = ((ExprVariable) child).jjtGetValue();
				} else {
					params[i] = child.jjtAccept(this, data);
				}
			}
			return provider.getFunctionProvider().calculate((String) node.jjtGetValue(), params);
		}

	}

	private static final Visitor VISITOR = new Visitor();

	private Evaluator() {
		super();
	}

	public static Object evaluate(GuardExpression expression, final VariableProvider variableProvider,
			final FunctionProvider functionProvider) throws EvaluatorException {
		try {
			return expression.visit(VISITOR, new Provider() {

				public VariableProvider getVariableProvider() {
					return variableProvider;
				}

				public FunctionProvider getFunctionProvider() {
					return functionProvider;
				}
			});
		} catch (ExpressionVisitorException | EvaluatorException e) {
			// Add context information
			throw new EvaluatorException("Error trying to evaluate expression " + expression.toCanonicalString(), e);
		}
	}

}
