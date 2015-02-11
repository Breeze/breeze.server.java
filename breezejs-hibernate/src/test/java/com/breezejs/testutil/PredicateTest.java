package com.breezejs.testutil;



import java.util.List;
import java.util.Map;

import com.breezejs.query.AndOrPredicate;
import com.breezejs.query.AnyAllPredicate;
import com.breezejs.query.BinaryPredicate;
import com.breezejs.query.Operator;
import com.breezejs.query.Predicate;
import com.breezejs.query.UnaryPredicate;
import com.breezejs.util.JsonGson;

import junit.framework.TestCase;

public class PredicateTest extends TestCase {
	protected void setUp() throws Exception {
		super.setUp();
		
	}
	
	public void testBinaryPredDouble() {
		 String pJson = "{ freight: { '>' : 100}}";
		 Map map = JsonGson.fromJson(pJson);
		 Predicate pred = Predicate.predicateFromMap(map);
		 assertTrue(pred != null);
		 assertTrue(pred instanceof BinaryPredicate);
		 BinaryPredicate bpred = (BinaryPredicate) pred;
		 assertTrue(bpred.getOperator() == Operator.GreaterThan);
		 assertTrue(bpred.getExpr1Source().equals("freight"));
		 assertTrue(bpred.getExpr2Source().equals(100.0));
	}
	
	public void testBinaryPredString() {
		 String pJson = "{ lastName: { 'startsWith' : 'S'}}";
		 Map map = JsonGson.fromJson(pJson);
		 Predicate pred = Predicate.predicateFromMap(map);
		 assertTrue(pred != null);
		 assertTrue(pred instanceof BinaryPredicate);
		 BinaryPredicate bpred = (BinaryPredicate) pred;
		 assertTrue(bpred.getOperator() == Operator.StartsWith);
		 assertTrue(bpred.getExpr1Source().equals("lastName"));
		 assertTrue(bpred.getExpr2Source().equals("S"));
	}
	
	public void testBinaryPredBoolean() {
		 String pJson = "{ discontinued: true }";
		 Map map = JsonGson.fromJson(pJson);
		 Predicate pred = Predicate.predicateFromMap(map);
		 assertTrue(pred != null);
		 assertTrue(pred instanceof BinaryPredicate);
		 BinaryPredicate bpred = (BinaryPredicate) pred;
		 assertTrue(bpred.getOperator() == Operator.Equals);
		 assertTrue(bpred.getExpr1Source().equals("discontinued"));
		 assertTrue(bpred.getExpr2Source().equals(true));
	}
	
	public void testBinaryExplicit() {
		 String pJson = "{ shippedDate: { value: '2015-02-09T00:00:00', dataType: 'DateTime' }}";
		 Map map = JsonGson.fromJson(pJson);
		 Predicate pred = Predicate.predicateFromMap(map);
		 assertTrue(pred != null);
		 assertTrue(pred instanceof BinaryPredicate);
		 BinaryPredicate bpred = (BinaryPredicate) pred;
		 assertTrue(bpred.getOperator() == Operator.Equals);
		 assertTrue(bpred.getExpr1Source().equals("shippedDate"));
		 assertTrue(bpred.getExpr2Source() instanceof Map);
		 Map expr2Source = (Map) bpred.getExpr2Source();
		 assertTrue(expr2Source.get("dataType").equals("DateTime"));
	}
	
	public void testBinaryExplicit2() {
		 String pJson = "{ 'lastName': { 'startsWith': { value: 'firstName' } } }";
		 Map map = JsonGson.fromJson(pJson);
		 Predicate pred = Predicate.predicateFromMap(map);
		 assertTrue(pred != null);
		 assertTrue(pred instanceof BinaryPredicate);
		 BinaryPredicate bpred = (BinaryPredicate) pred;
		 assertTrue(bpred.getOperator() == Operator.StartsWith);
		 assertTrue(bpred.getExpr1Source().equals("lastName"));
		 assertTrue(bpred.getExpr2Source() instanceof Map);
		 Map expr2Source = (Map) bpred.getExpr2Source();
		 assertTrue(expr2Source.get("value").equals("firstName"));
	}
	
	
	public void testExplicitAnd() {
		 String pJson = "{ and: [ { freight: { gt: 100} }, { shipCity: { startsWith: 'S'} } ] }";
		 Map map = JsonGson.fromJson(pJson);
		 Predicate pred = Predicate.predicateFromMap(map);
		 assertTrue(pred != null);
		 assertTrue(pred instanceof AndOrPredicate);
		 AndOrPredicate aopred = (AndOrPredicate) pred;
		 assertTrue(aopred.getOperator() == Operator.And);
		 assertTrue(aopred.getPredicates().size() == 2);
		 List<Predicate> preds = aopred.getPredicates();
		 
		 Predicate pred1 = preds.get(0);
		 assertTrue(pred1 instanceof BinaryPredicate);
		 BinaryPredicate bpred = (BinaryPredicate) pred1;
		 assertTrue(bpred.getOperator() == Operator.GreaterThan);
		 assertTrue(bpred.getExpr1Source().equals("freight"));
		 assertTrue(bpred.getExpr2Source().equals(100.0));
		 
		 Predicate pred2 = preds.get(1);
		 assertTrue(pred2 instanceof BinaryPredicate);
		 bpred = (BinaryPredicate) pred2;
		 assertTrue(bpred.getOperator() == Operator.StartsWith);
		 assertTrue(bpred.getExpr1Source().equals("shipCity"));
		 assertTrue(bpred.getExpr2Source().equals("S"));
	}
	
	
	
	
	public void testImplicitAnd() {
		 String pJson = "{ freight: { '>' : 100, 'lt': 200 }}";
		 Map map = JsonGson.fromJson(pJson);
		 Predicate pred = Predicate.predicateFromMap(map);
		 assertTrue(pred != null);
		 assertTrue(pred instanceof AndOrPredicate);
		 AndOrPredicate aopred = (AndOrPredicate) pred;
		 assertTrue(aopred.getOperator() == Operator.And);
		 assertTrue(aopred.getPredicates().size() == 2);
		 List<Predicate> preds = aopred.getPredicates();
		 
		 Predicate pred1 = preds.get(0);
		 assertTrue(pred1 instanceof BinaryPredicate);
		 BinaryPredicate bpred = (BinaryPredicate) pred1;
		 assertTrue(bpred.getOperator() == Operator.GreaterThan);
		 assertTrue(bpred.getExpr1Source().equals("freight"));
		 assertTrue(bpred.getExpr2Source().equals(100.0));
		 
		 Predicate pred2 = preds.get(1);
		 assertTrue(pred2 instanceof BinaryPredicate);
		 bpred = (BinaryPredicate) pred2;
		 assertTrue(bpred.getOperator() == Operator.LessThan);
		 assertTrue(bpred.getExpr1Source().equals("freight"));
		 assertTrue(bpred.getExpr2Source().equals(200.0));
	}
	
	public void testImplicitAnd3Way() {
		 String pJson = "{ freight: { '>': 100}, rowVersion: { lt: 10}, shippedDate: '2015-02-09T00:00:00' }";
			      
		 Map map = JsonGson.fromJson(pJson);
		 Predicate pred = Predicate.predicateFromMap(map);
		 assertTrue(pred != null);
		 assertTrue(pred instanceof AndOrPredicate);
		 AndOrPredicate aopred = (AndOrPredicate) pred;
		 assertTrue(aopred.getOperator() == Operator.And);
		 assertTrue(aopred.getPredicates().size() == 3);
		 List<Predicate> preds = aopred.getPredicates();
		 
		 Predicate pred1 = preds.get(0);
		 assertTrue(pred1 instanceof BinaryPredicate);
		 BinaryPredicate bpred = (BinaryPredicate) pred1;
		 assertTrue(bpred.getOperator() == Operator.GreaterThan);
		 assertTrue(bpred.getExpr1Source().equals("freight"));
		 assertTrue(bpred.getExpr2Source().equals(100.0));
		 
		 Predicate pred2 = preds.get(1);
		 assertTrue(pred2 instanceof BinaryPredicate);
		 bpred = (BinaryPredicate) pred2;
		 assertTrue(bpred.getOperator() == Operator.LessThan);
		 assertTrue(bpred.getExpr1Source().equals("rowVersion"));
		 assertTrue(bpred.getExpr2Source().equals(10.0));
		 
		 Predicate pred3 = preds.get(2);
		 assertTrue(pred3 instanceof BinaryPredicate);
		 bpred = (BinaryPredicate) pred3;
		 assertTrue(bpred.getOperator() == Operator.Equals);
		 assertTrue(bpred.getExpr1Source().equals("shippedDate"));
		 assertTrue(bpred.getExpr2Source().equals("2015-02-09T00:00:00"));
	}
	
	public void testNot() {
		 String pJson = "{ not: { freight: { gt:  100}}}";
		 Map map = JsonGson.fromJson(pJson);
		 Predicate pred = Predicate.predicateFromMap(map);
		 assertTrue(pred != null);
		 assertTrue(pred instanceof UnaryPredicate);
		 UnaryPredicate upred = (UnaryPredicate) pred;
		 assertTrue(upred.getOperator() == Operator.Not);
		 
		 Predicate basePred = upred.getPredicate();
		 assertTrue(basePred instanceof BinaryPredicate);
		 BinaryPredicate bpred = (BinaryPredicate) basePred;
		 assertTrue(bpred.getOperator() == Operator.GreaterThan);
		 assertTrue(bpred.getExpr1Source().equals("freight"));
		 assertTrue(bpred.getExpr2Source().equals(100.0));
		 
		 
	}
	
	public void testAny() {
		 String pJson = "{ orders: { any: {freight: { '>': 950 } } } }";
		 Map map = JsonGson.fromJson(pJson);
		 Predicate pred = Predicate.predicateFromMap(map);
		 assertTrue(pred != null);
		 assertTrue(pred instanceof AnyAllPredicate);
		 AnyAllPredicate aapred = (AnyAllPredicate) pred;
		 assertTrue(aapred.getOperator() == Operator.Any);
		 
		 assertTrue(aapred.getExprSource().equals("orders"));
		 
		 Predicate basePred = aapred.getPredicate();
		 assertTrue(basePred instanceof BinaryPredicate);
		 BinaryPredicate bpred = (BinaryPredicate) basePred;
		 assertTrue(bpred.getOperator() == Operator.GreaterThan);
		 assertTrue(bpred.getExpr1Source().equals("freight"));
		 assertTrue(bpred.getExpr2Source().equals(950.0));
		 
	}
	
	public void testAndWithAll() {
		 String pJson = "{ and: [ { companyName: { contains: 'ar' } }, { orders: { all: { freight: 10 } } } ] }";

		 Map map = JsonGson.fromJson(pJson);
		 Predicate pred = Predicate.predicateFromMap(map);
		 assertTrue(pred != null);
		 assertTrue(pred instanceof AndOrPredicate);
		 AndOrPredicate aopred = (AndOrPredicate) pred;
		 assertTrue(aopred.getOperator() == Operator.And);
		 assertTrue(aopred.getPredicates().size() == 2);
		 List<Predicate> preds = aopred.getPredicates();
		 
		 Predicate pred1 = preds.get(0);
		 assertTrue(pred1 instanceof BinaryPredicate);
		 BinaryPredicate bpred = (BinaryPredicate) pred1;
		 assertTrue(bpred.getOperator() == Operator.Contains);
		 assertTrue(bpred.getExpr1Source().equals("companyName"));
		 assertTrue(bpred.getExpr2Source().equals("ar"));
		 
		 Predicate pred2 = preds.get(1);
		 assertTrue(pred2 instanceof AnyAllPredicate);
		 AnyAllPredicate aapred = (AnyAllPredicate) pred2;
		 assertTrue(aapred.getOperator() == Operator.All);
		 
		 assertTrue(aapred.getExprSource().equals("orders"));
		 
		 Predicate basePred = aapred.getPredicate();
		 assertTrue(basePred instanceof BinaryPredicate);
		 bpred = (BinaryPredicate) basePred;
		 assertTrue(bpred.getOperator() == Operator.Equals);
		 assertTrue(bpred.getExpr1Source().equals("freight"));
		 assertTrue(bpred.getExpr2Source().equals(10.0));
		 
	}
	
		             
	             
	
}
