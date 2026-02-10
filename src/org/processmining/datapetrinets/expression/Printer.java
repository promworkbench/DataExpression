package org.processmining.datapetrinets.expression;

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
import org.processmining.datapetrinets.expression.syntax.SimpleNode;

/**
 * Class to create a readable String representation from an
 * {@link GuardExpression}.
 * 
 * @author F. Mannhardt
 * 
 */
public final class Printer {

	private static final class CanonicalPrinterVisitor implements ExpressionParserVisitor {

		private static StringBuilder sb(Object data) {
			return (StringBuilder) data;
		}

		public Object visit(ExprLitNull node, Object data) {
			sb(data).append("null");
			return null;
		}

		public Object visit(ExprLitBoolean node, Object data) {
			sb(data).append(node.jjtGetValue());
			return null;
		}

		public Object visit(ExprLitString node, Object data) {
			sb(data).append(node.jjtGetValue());
			return null;
		}

		public Object visit(ExprLitDouble node, Object data) {
			sb(data).append(node.jjtGetValue());
			return null;
		}

		public Object visit(ExprLitInteger node, Object data) {
			sb(data).append(node.jjtGetValue());
			return null;
		}

		public Object visit(ExprVariable node, Object data) {
			sb(data).append(node.jjtGetValue());
			return null;
		}

		public Object visit(ExprNot node, Object data) throws ExpressionVisitorException {
			sb(data).append("!(");
			node.jjtGetChild(0).jjtAccept(this, data);
			sb(data).append(')');
			return null;
		}

		public Object visit(ExprNegation node, Object data) throws ExpressionVisitorException {
			sb(data).append("-");
			node.jjtGetChild(0).jjtAccept(this, data);
			return null;
		}

		private void printBinaryOp(String symbol, SimpleNode node, Object data) throws ExpressionVisitorException {
			sb(data).append('(');
			node.jjtGetChild(0).jjtAccept(this, data);
			sb(data).append(symbol);
			node.jjtGetChild(1).jjtAccept(this, data);
			sb(data).append(')');
		}

		public Object visit(ExprDiv node, Object data) throws ExpressionVisitorException {
			printBinaryOp("/", node, data);
			return null;
		}

		public Object visit(ExprMult node, Object data) throws ExpressionVisitorException {
			printBinaryOp("*", node, data);
			return null;
		}

		public Object visit(ExprMinus node, Object data) throws ExpressionVisitorException {
			printBinaryOp("-", node, data);
			return null;
		}

		public Object visit(ExprPlus node, Object data) throws ExpressionVisitorException {
			printBinaryOp("+", node, data);
			return null;
		}

		public Object visit(ExprAtLeast node, Object data) throws ExpressionVisitorException {
			printBinaryOp(">=", node, data);
			return null;
		}

		public Object visit(ExprGreaterThan node, Object data) throws ExpressionVisitorException {
			printBinaryOp(">", node, data);
			return null;
		}

		public Object visit(ExprAtMost node, Object data) throws ExpressionVisitorException {
			printBinaryOp("<=", node, data);
			return null;
		}

		public Object visit(ExprLessThan node, Object data) throws ExpressionVisitorException {
			printBinaryOp("<", node, data);
			return null;
		}

		public Object visit(ExprNotEqual node, Object data) throws ExpressionVisitorException {
			printBinaryOp("!=", node, data);
			return null;
		}

		public Object visit(ExprEqual node, Object data) throws ExpressionVisitorException {
			printBinaryOp("==", node, data);
			return null;
		}

		public Object visit(ExprAnd node, Object data) throws ExpressionVisitorException {
			printBinaryOp("&&", node, data);
			return null;
		}

		public Object visit(ExprOr node, Object data) throws ExpressionVisitorException {
			printBinaryOp("||", node, data);
			return null;
		}

		public Object visit(ExprRoot node, Object data) throws ExpressionVisitorException {
			if (node.jjtGetNumChildren() > 1) {
				throw new IllegalArgumentException("Invalid expression " + node
						+ " should not have been parsed! Top level element is only allowed to have one child.");
			}
			node.jjtGetChild(0).jjtAccept(this, data);
			return null;
		}

		public Object visit(SimpleNode node, Object data) {
			throw new IllegalStateException("No unamed nodes allowed!");
		}

		public Object visit(ExprFunction node, Object data) throws ExpressionVisitorException {
			sb(data).append(node.jjtGetValue() + "(");
			for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
				node.jjtGetChild(i).jjtAccept(this, data);
				if (i != node.jjtGetNumChildren() - 1) {
					sb(data).append(',');
				}
			}
			sb(data).append(")");
			return null;
		}

	}

	private static final class PrettyPrinterVisitor implements ExpressionParserVisitor {

		private final int spaces;
		private final StringBuilder sb;

		public PrettyPrinterVisitor(int spaces) {
			this.spaces = spaces;
			this.sb = new StringBuilder();
		}

		public Object visit(ExprLitNull node, Object data) {
			sb.append("null");
			return null;
		}

		public Object visit(ExprLitBoolean node, Object data) {
			sb.append(node.jjtGetValue());
			return null;
		}

		public Object visit(ExprLitString node, Object data) {
			sb.append(node.jjtGetValue());
			return null;
		}

		public Object visit(ExprLitDouble node, Object data) {
			sb.append(node.jjtGetValue());
			return null;
		}

		public Object visit(ExprLitInteger node, Object data) {
			sb.append(node.jjtGetValue());
			return null;
		}

		public Object visit(ExprVariable node, Object data) {
			sb.append(node.jjtGetValue());
			return null;
		}

		public Object visit(ExprNot node, Object data) throws ExpressionVisitorException {
			sb.append("!(");
			node.jjtGetChild(0).jjtAccept(this, data);
			sb.append(')');
			return null;
		}

		public Object visit(ExprNegation node, Object data) throws ExpressionVisitorException {
			sb.append("-");
			node.jjtGetChild(0).jjtAccept(this, data);
			return null;
		}

		private void printBinaryOp(String symbol, SimpleNode node, Object data) throws ExpressionVisitorException {
			appendWhitespace(data, spaces);
			node.jjtGetChild(0).jjtAccept(this, data);
			appendWhitespace(data, spaces);
			sb.append(symbol);
			appendWhitespace(data, spaces);
			node.jjtGetChild(1).jjtAccept(this, data);
		}

		private void printBooleanOp(String symbol, SimpleNode node, Object data) throws ExpressionVisitorException {
			//appendWhitespace(data, spaces);
			sb.append('(');
			node.jjtGetChild(0).jjtAccept(this, data);
			appendWhitespace(data, spaces);
			sb.append(symbol);
			appendWhitespace(data, spaces);
			node.jjtGetChild(1).jjtAccept(this, data);
			sb.append(')');
		}

		public Object visit(ExprDiv node, Object data) throws ExpressionVisitorException {
			printBinaryOp(" / ", node, data);
			return null;
		}

		private void appendWhitespace(Object data, int level) {
			for (int i = 0; i < level; i++) {
				sb.append(' ');
			}
		}

		public Object visit(ExprMult node, Object data) throws ExpressionVisitorException {
			printBinaryOp(" * ", node, data);
			return null;
		}

		public Object visit(ExprMinus node, Object data) throws ExpressionVisitorException {
			printBinaryOp(" - ", node, data);
			return null;
		}

		public Object visit(ExprPlus node, Object data) throws ExpressionVisitorException {
			printBinaryOp(" + ", node, data);
			return null;
		}

		public Object visit(ExprAtLeast node, Object data) throws ExpressionVisitorException {
			printBinaryOp(" >= ", node, data);
			return null;
		}

		public Object visit(ExprGreaterThan node, Object data) throws ExpressionVisitorException {
			printBinaryOp(" > ", node, data);
			return null;
		}

		public Object visit(ExprAtMost node, Object data) throws ExpressionVisitorException {
			printBinaryOp(" <= ", node, data);
			return null;
		}

		public Object visit(ExprLessThan node, Object data) throws ExpressionVisitorException {
			printBinaryOp(" < ", node, data);
			return null;
		}

		public Object visit(ExprNotEqual node, Object data) throws ExpressionVisitorException {
			printBinaryOp(" != ", node, data);
			return null;
		}

		public Object visit(ExprEqual node, Object data) throws ExpressionVisitorException {
			printBinaryOp(" == ", node, data);
			return null;
		}

		public Object visit(ExprAnd node, Object data) throws ExpressionVisitorException {
			printBooleanOp("&&", node, data);
			return null;
		}

		public Object visit(ExprOr node, Object data) throws ExpressionVisitorException {
			printBooleanOp("||", node, data);
			return null;
		}

		public Object visit(ExprRoot node, Object data) throws ExpressionVisitorException {
			if (node.jjtGetNumChildren() > 1) {
				throw new IllegalArgumentException("Invalid expression " + node
						+ " should not have been parsed! Top level element is only allowed to have one child.");
			}
			return node.jjtGetChild(0).jjtAccept(this, data);
		}

		public Object visit(SimpleNode node, Object data) {
			throw new IllegalStateException("No unamed nodes allowed!");
		}

		public String getString() {
			return sb.toString();
		}

		public Object visit(ExprFunction node, Object data) throws ExpressionVisitorException {
			sb.append(node.jjtGetValue() + "(");
			for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
				node.jjtGetChild(i).jjtAccept(this, data);
				if (i != node.jjtGetNumChildren() - 1) {
					sb.append(',');
				}
			}
			sb.append(")");
			return null;
		}

	}

	private static final class TreePrinterVisitor implements ExpressionParserVisitor {

		private int indent;
		private final StringBuilder sb;

		public TreePrinterVisitor(int indent) {
			this.indent = indent;
			this.sb = new StringBuilder();
		}
		
		private static int in(Object data) {
			return (Integer) data;
		}
		
		private static void appendWhitespace(StringBuilder sb, Object data, int level) {
			for (int i = 0; i < level; i++) {
				sb.append(' ');
			}
		}

		public Object visit(ExprLitNull node, Object data) {
			sb.append("null");
			return null;
		}

		public Object visit(ExprLitBoolean node, Object data) {
			sb.append(node.jjtGetValue());
			return null;
		}

		public Object visit(ExprLitString node, Object data) {
			sb.append(node.jjtGetValue());
			return null;
		}

		public Object visit(ExprLitDouble node, Object data) {
			sb.append(node.jjtGetValue());
			return null;
		}

		public Object visit(ExprLitInteger node, Object data) {
			sb.append(node.jjtGetValue());
			return null;
		}

		public Object visit(ExprVariable node, Object data) {
			sb.append(node.jjtGetValue());
			return null;
		}

		public Object visit(ExprNot node, Object data) throws ExpressionVisitorException {
			sb.append("!(");
			node.jjtGetChild(0).jjtAccept(this, data);
			sb.append(')');
			return null;
		}

		public Object visit(ExprNegation node, Object data) throws ExpressionVisitorException {
			sb.append("-");
			node.jjtGetChild(0).jjtAccept(this, data);
			return null;
		}

		public Object visit(ExprDiv node, Object data) throws ExpressionVisitorException {
			appendWhitespace(sb, data, in(data));
			node.jjtGetChild(0).jjtAccept(this, data);
			sb.append(" / ");
			node.jjtGetChild(1).jjtAccept(this, data);
			return null;
		}

		public Object visit(ExprMult node, Object data) throws ExpressionVisitorException {
			appendWhitespace(sb, data, in(data));
			node.jjtGetChild(0).jjtAccept(this, data);
			sb.append(" * ");
			node.jjtGetChild(1).jjtAccept(this, data);
			return null;
		}

		public Object visit(ExprMinus node, Object data) throws ExpressionVisitorException {
			appendWhitespace(sb, data, in(data));
			node.jjtGetChild(0).jjtAccept(this, data);
			sb.append(" - ");
			node.jjtGetChild(1).jjtAccept(this, data);
			return null;
		}

		public Object visit(ExprPlus node, Object data) throws ExpressionVisitorException {
			appendWhitespace(sb, data, in(data));
			node.jjtGetChild(0).jjtAccept(this, data);
			sb.append(" + ");
			node.jjtGetChild(1).jjtAccept(this, data);
			return null;
		}

		public Object visit(ExprAtLeast node, Object data) throws ExpressionVisitorException {
			appendWhitespace(sb, data, in(data));
			node.jjtGetChild(0).jjtAccept(this, data);
			sb.append(" >= ");
			node.jjtGetChild(1).jjtAccept(this, data);
			return null;
		}

		public Object visit(ExprGreaterThan node, Object data) throws ExpressionVisitorException {
			appendWhitespace(sb, data, in(data));
			node.jjtGetChild(0).jjtAccept(this, data);
			sb.append(" > ");
			node.jjtGetChild(1).jjtAccept(this, data);
			return null;
		}

		public Object visit(ExprAtMost node, Object data) throws ExpressionVisitorException {
			appendWhitespace(sb, data, in(data));
			node.jjtGetChild(0).jjtAccept(this, data);
			sb.append(" <= ");
			node.jjtGetChild(1).jjtAccept(this, data);
			return null;
		}

		public Object visit(ExprLessThan node, Object data) throws ExpressionVisitorException {
			appendWhitespace(sb, data, in(data));
			node.jjtGetChild(0).jjtAccept(this, data);
			sb.append(" < ");
			node.jjtGetChild(1).jjtAccept(this, data);
			return null;
		}

		public Object visit(ExprNotEqual node, Object data) throws ExpressionVisitorException {
			appendWhitespace(sb, data, in(data));
			node.jjtGetChild(0).jjtAccept(this, data);
			sb.append(" != ");
			node.jjtGetChild(1).jjtAccept(this, data);
			return null;
		}

		public Object visit(ExprEqual node, Object data) throws ExpressionVisitorException {
			appendWhitespace(sb, data, in(data));
			node.jjtGetChild(0).jjtAccept(this, data);
			sb.append(" == ");
			node.jjtGetChild(1).jjtAccept(this, data);
			return null;
		}

		public Object visit(ExprAnd node, Object data) throws ExpressionVisitorException {
			appendWhitespace(sb, data, in(data));
			node.jjtGetChild(0).jjtAccept(this, in(data) + indent);
			sb.append('\n');
			appendWhitespace(sb, data, in(data));
			sb.append("&&");
			sb.append('\n');
			node.jjtGetChild(1).jjtAccept(this, in(data) + indent);			
			return null;
		}

		public Object visit(ExprOr node, Object data) throws ExpressionVisitorException {
			appendWhitespace(sb, data, in(data));
			node.jjtGetChild(0).jjtAccept(this, in(data) + indent);
			sb.append('\n');
			appendWhitespace(sb, data, in(data));
			sb.append("||");
			sb.append('\n');
			node.jjtGetChild(1).jjtAccept(this, in(data) + indent);		
			return null;
		}
		
		public Object visit(ExprFunction node, Object data) throws ExpressionVisitorException {
			sb.append(node.jjtGetValue() + "(");
			for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
				node.jjtGetChild(i).jjtAccept(this, data);
				if (i != node.jjtGetNumChildren() - 1) {
					sb.append(',');
				}
			}
			sb.append(")");
			return null;
		}

		public Object visit(ExprRoot node, Object data) throws ExpressionVisitorException {
			if (node.jjtGetNumChildren() > 1) {
				throw new IllegalArgumentException("Invalid expression " + node
						+ " should not have been parsed! Top level element is only allowed to have one child.");
			}
			return node.jjtGetChild(0).jjtAccept(this, data);
		}

		public Object visit(SimpleNode node, Object data) {
			throw new IllegalStateException("No unamed nodes allowed!");
		}

		public String getString() {
			return sb.toString();
		}

	}

	private static final CanonicalPrinterVisitor CANONICAL_VISITOR = new CanonicalPrinterVisitor();

	private Printer() {
		super();
	}

	public static String printCanonical(ExprRoot expression) {
		StringBuilder sb = new StringBuilder();
		try {
			CANONICAL_VISITOR.visit(expression, sb);
		} catch (ExpressionVisitorException e) {
			throw new RuntimeException("Exception while trying to print expression!", e);
		} 
		return sb.toString();
	}

	public static String printTree(ExprRoot expression, int indent) {
		TreePrinterVisitor treePrinterVisitor = new TreePrinterVisitor(indent);
		try {
			treePrinterVisitor.visit(expression, 0);
		} catch (ExpressionVisitorException e) {
			throw new RuntimeException("Exception while trying to print expression!", e);
		}
		return treePrinterVisitor.getString();
	}

	public static String printPretty(ExprRoot expression, int spaces) {
		PrettyPrinterVisitor prettyPrinterVisitor = new PrettyPrinterVisitor(spaces);
		try {
			prettyPrinterVisitor.visit(expression, null);
		} catch (ExpressionVisitorException e) {
			throw new RuntimeException("Exception while trying to print expression!", e);
		}
		return prettyPrinterVisitor.getString();
	}

}