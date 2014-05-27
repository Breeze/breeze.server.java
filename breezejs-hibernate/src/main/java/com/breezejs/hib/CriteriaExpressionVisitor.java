package com.breezejs.hib;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Stack;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.SimpleExpression;
import org.joda.time.LocalDateTime;
import org.odata4j.expression.*;
import org.odata4j.expression.OrderByExpression.Direction;

public class CriteriaExpressionVisitor {
	
	private Criteria crit;
	
//	private Stack<String> ops = new Stack<String>();
//	private Stack<String> props = new Stack<String>();
//	private Stack<Object> stack = new Stack<Object>();
	
	public CriteriaExpressionVisitor(Criteria crit) {
		this.crit = crit;
	}
	


	public void beforeDescend() {
		// TODO Auto-generated method stub

	}


	public void afterDescend() {
//		Object item = stack.pop();
//		if (item instanceof String) {
//			String op = (String) item;
//			String propertyName = (String) stack.pop();
//			Object rhs = stack.pop();
//			crit.add(new OperatorExpression(propertyName, rhs, op));
//		}
	}


	public void betweenDescend() {
		// TODO Auto-generated method stub

	}


	public void visit(String type) {
		// TODO Auto-generated method stub

	}


	public void visit(OrderByExpression expr) {
		String property = (String) visit(expr.getExpression());
		Order order = expr.getDirection() == Direction.ASCENDING ? Order.asc(property) : Order.desc(property);
		crit.addOrder(order);
	}
	
	public Object visit(CommonExpression ce) {
		return ce.toString();
	}
	
	public Criterion getCriterion(CommonExpression ce) {
		return null;
	}


	public void visit(AddExpression expr) {
		throw new RuntimeException("Not implemented: " + expr);
	}


	public LogicalExpression visit(AndExpression expr) {
		return Restrictions.and((Criterion) visit(expr.getLHS()), (Criterion) visit(expr.getRHS()));
	}
	
	public boolean visit(BooleanLiteral expr) {
		return expr.getValue();
	}


	public void visit(CastExpression expr) {
		// TODO Auto-generated method stub

	}


	public void visit(ConcatMethodCallExpression expr) {
		// TODO Auto-generated method stub

	}


	public Date visit(DateTimeLiteral expr) {
		return expr.getValue().toDateTime().toDate();
	}


	public Date visit(DateTimeOffsetLiteral expr) {
		return expr.getValue().toDate();
	}


	public BigDecimal visit(DecimalLiteral expr) {
		return expr.getValue();
	}


	public void visit(DivExpression expr) {
		// TODO Auto-generated method stub

	}


	public SimpleExpression visit(EndsWithMethodCallExpression expr) {
		return Restrictions.like((String) visit(expr.getTarget()), visit(expr.getValue()));
	}


	public String visit(EntitySimpleProperty expr) {
		return expr.getPropertyName();
	}


	public SimpleExpression visit(EqExpression expr) {
		return Restrictions.eq((String) visit(expr.getLHS()), visit(expr.getRHS()));
	}


	public SimpleExpression visit(GeExpression expr) {
		return Restrictions.ge((String) visit(expr.getLHS()), visit(expr.getRHS()));
	}


	public SimpleExpression visit(GtExpression expr) {
		return Restrictions.gt((String) visit(expr.getLHS()), visit(expr.getRHS()));
	}


	public String visit(GuidLiteral expr) {
		return expr.getValue().toString();
	}


	public byte[] visit(BinaryLiteral expr) {
		return expr.getValue();
	}


	public byte visit(ByteLiteral expr) {
		return expr.getValue().byteValue();
	}


	public byte visit(SByteLiteral expr) {
		return expr.getValue();
	}


	public void visit(IndexOfMethodCallExpression expr) {
		// TODO Auto-generated method stub

	}


	public float visit(SingleLiteral expr) {
		return expr.getValue();
	}


	public double visit(DoubleLiteral expr) {
		return expr.getValue();
	}


	public int visit(IntegralLiteral expr) {
		return expr.getValue();
	}


	public long visit(Int64Literal expr) {
		return expr.getValue();
	}


	public void visit(IsofExpression expr) {
		// TODO Auto-generated method stub

	}


	public SimpleExpression visit(LeExpression expr) {
		return Restrictions.le((String) visit(expr.getLHS()), visit(expr.getRHS()));
	}


	public void visit(LengthMethodCallExpression expr) {
		// TODO Auto-generated method stub

	}


	public SimpleExpression visit(LtExpression expr) {
		return Restrictions.lt((String) visit(expr.getLHS()), visit(expr.getRHS()));
	}


	public void visit(ModExpression expr) {
		// TODO Auto-generated method stub

	}


	public void visit(MulExpression expr) {
		// TODO Auto-generated method stub

	}


	public SimpleExpression visit(NeExpression expr) {
		return Restrictions.ne((String) visit(expr.getLHS()), visit(expr.getRHS()));
	}


	public Criterion visit(NegateExpression expr) {
		return Restrictions.not((Criterion) visit(expr.getExpression()));
	}


	public Criterion visit(NotExpression expr) {
		return Restrictions.not((Criterion) visit(expr.getExpression()));
	}


	public Object visit(NullLiteral expr) {
		return null;
	}


	public LogicalExpression visit(OrExpression expr) {
		return Restrictions.or((Criterion) visit(expr.getLHS()), (Criterion) visit(expr.getRHS()));
	}


	public void visit(ParenExpression expr) {
		// TODO Auto-generated method stub

	}


	public void visit(BoolParenExpression expr) {
		// TODO Auto-generated method stub

	}


	public void visit(ReplaceMethodCallExpression expr) {
		// TODO Auto-generated method stub

	}


	public SimpleExpression visit(StartsWithMethodCallExpression expr) {
		return Restrictions.like((String) visit(expr.getTarget()), visit(expr.getValue()));

	}


	public String visit(StringLiteral expr) {
		return expr.getValue();
	}


	public void visit(SubExpression expr) {
		// TODO Auto-generated method stub
	}


	public void visit(SubstringMethodCallExpression expr) {
		// TODO Auto-generated method stub

	}


	public void visit(SubstringOfMethodCallExpression expr) {
		// TODO Auto-generated method stub

	}


	public Date visit(TimeLiteral expr) {
		return expr.getValue().toDateTimeToday().toDate();

	}


	public void visit(ToLowerMethodCallExpression expr) {
		// TODO Auto-generated method stub

	}


	public void visit(ToUpperMethodCallExpression expr) {
		// TODO Auto-generated method stub

	}


	public void visit(TrimMethodCallExpression expr) {
		// TODO Auto-generated method stub

	}


	public void visit(YearMethodCallExpression expr) {
		// TODO Auto-generated method stub

	}


	public void visit(MonthMethodCallExpression expr) {
		// TODO Auto-generated method stub

	}


	public void visit(DayMethodCallExpression expr) {
		// TODO Auto-generated method stub

	}


	public void visit(HourMethodCallExpression expr) {
		// TODO Auto-generated method stub

	}


	public void visit(MinuteMethodCallExpression expr) {
		// TODO Auto-generated method stub

	}


	public void visit(SecondMethodCallExpression expr) {
		// TODO Auto-generated method stub

	}


	public void visit(RoundMethodCallExpression expr) {
		// TODO Auto-generated method stub

	}


	public void visit(FloorMethodCallExpression expr) {
		// TODO Auto-generated method stub

	}


	public void visit(CeilingMethodCallExpression expr) {
		// TODO Auto-generated method stub

	}


	public void visit(AggregateAnyFunction expr) {
		// TODO Auto-generated method stub

	}


	public void visit(AggregateAllFunction expr) {
		// TODO Auto-generated method stub

	}


}
