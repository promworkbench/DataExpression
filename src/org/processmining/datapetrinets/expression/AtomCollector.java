package org.processmining.datapetrinets.expression;

import org.processmining.datapetrinets.expression.syntax.ExprAtLeast;
import org.processmining.datapetrinets.expression.syntax.ExprAtMost;
import org.processmining.datapetrinets.expression.syntax.ExprEqual;
import org.processmining.datapetrinets.expression.syntax.ExprGreaterThan;
import org.processmining.datapetrinets.expression.syntax.ExprLessThan;
import org.processmining.datapetrinets.expression.syntax.ExprNotEqual;
import org.processmining.datapetrinets.expression.syntax.ExprRoot;
import org.processmining.datapetrinets.expression.syntax.ExpressionParserDefaultVisitor;
import org.processmining.datapetrinets.expression.syntax.ExpressionVisitorException;
import org.processmining.datapetrinets.expression.syntax.SimpleNode;

public final class AtomCollector {
	
	private static final class AtomCount {
		private int count;
		
		public void inc() {
			count++;
		}

		public int getCount() {
			return count;
		}
	}

	private static final class ComparisonAtomCollectingVisitor extends ExpressionParserDefaultVisitor {

		public Object visit(ExprEqual node, Object data) throws ExpressionVisitorException {
			((AtomCount)data).inc();
			return data;
		}

		public Object visit(ExprNotEqual node, Object data) throws ExpressionVisitorException {
			((AtomCount)data).inc();
			return data;
		}

		public Object visit(ExprLessThan node, Object data) throws ExpressionVisitorException {
			((AtomCount)data).inc();
			return data;
		}

		public Object visit(ExprAtMost node, Object data) throws ExpressionVisitorException {
			((AtomCount)data).inc();
			return data;
		}

		public Object visit(ExprGreaterThan node, Object data) throws ExpressionVisitorException {
			((AtomCount)data).inc();
			return data;
		}

		public Object visit(ExprAtLeast node, Object data) throws ExpressionVisitorException {
			((AtomCount)data).inc();
			return data;
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
	
	private static final ComparisonAtomCollectingVisitor ATOM_VISITOR = new ComparisonAtomCollectingVisitor();

	private AtomCollector() {
		super();
	}
	
	public static int countComparisonAtoms(GuardExpression expression) {
		try {
			AtomCount atomCount = new AtomCount();
			expression.visit(ATOM_VISITOR, atomCount);
			return atomCount.getCount();
		} catch (ExpressionVisitorException e) {
			throw new RuntimeException("Exception while trying to print expression!", e);
		}
	}

}
