package com.breezejs.testutil;



import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.SessionFactory;

import com.breezejs.Metadata;
import com.breezejs.MetadataWrapper;
import com.breezejs.hib.MetadataBuilder;
import com.breezejs.hib.StaticConfigurator;
import com.breezejs.metadata.DataType;
import com.breezejs.metadata.IEntityType;
import com.breezejs.metadata.IMetadata;
import com.breezejs.query.AndOrPredicate;
import com.breezejs.query.AnyAllPredicate;
import com.breezejs.query.BinaryPredicate;
import com.breezejs.query.Expression;
import com.breezejs.query.FnExpression;
import com.breezejs.query.LitExpression;
import com.breezejs.query.Operator;
import com.breezejs.query.Predicate;
import com.breezejs.query.PropExpression;
import com.breezejs.query.UnaryPredicate;
import com.breezejs.util.JsonGson;

import junit.framework.TestCase;

// TODO: need nested property tests

public class PredicateTest extends TestCase {
	private IMetadata _metadataWrapper;
	protected void setUp() throws Exception {
		super.setUp();
		SessionFactory sf = StaticConfigurator.getSessionFactory();
		
		MetadataBuilder mb = new MetadataBuilder(sf);
		
		Metadata metadata = mb.buildMetadata();
		_metadataWrapper = new MetadataWrapper(metadata);
	}
	
	public void testBinaryPredNull() {
		 String pJson = "{ shipName: null }";
		 Map map = JsonGson.fromJson(pJson);
		 Predicate pred = Predicate.predicateFromMap(map);
		 assertTrue(pred != null);
		 assertTrue(pred instanceof BinaryPredicate);
		 BinaryPredicate bpred = (BinaryPredicate) pred;
		 assertTrue(bpred.getOperator() == Operator.Equals);
		 assertTrue(bpred.getExpr1Source().equals("shipName"));
		 assertTrue(bpred.getExpr2Source() == null);
		 
		 IEntityType et = _metadataWrapper.getEntityTypeForResourceName("Orders");
		 pred.validate(et);
		 PropExpression expr1 = (PropExpression) bpred.getExpr1();
		 assertTrue(expr1.getPropertyPath().equals("shipName"));
		 LitExpression expr2 = (LitExpression) bpred.getExpr2();
		 assertTrue(expr2.getDataType() == DataType.String);
		 assertTrue(expr2.getValue() == null);
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
		 
		 IEntityType et = _metadataWrapper.getEntityTypeForResourceName("Orders");
		 pred.validate(et);
		 PropExpression expr1 = (PropExpression) bpred.getExpr1();
		 assertTrue(expr1.getPropertyPath().equals("freight"));
		 LitExpression expr2 = (LitExpression) bpred.getExpr2();
		 assertTrue(expr2.getDataType() == DataType.Decimal);
		 assertTrue(expr2.getValue().equals(BigDecimal.valueOf(100.0)));
		 
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
		 
		 IEntityType et = _metadataWrapper.getEntityTypeForResourceName("Employees");
		 pred.validate(et);
		 PropExpression expr1 = (PropExpression) bpred.getExpr1();
		 assertTrue(expr1.getPropertyPath().equals("lastName"));
		 LitExpression expr2 = (LitExpression) bpred.getExpr2();
		 assertTrue(expr2.getDataType() == DataType.String);
		 assertTrue(expr2.getValue().equals("S"));
	}
	
	public void testBinaryPredBoolean() {
    	// TODO: can't validate this because 'discontinued' property is no longer on order.
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
	
	public void testBinaryPredStringQuote() {
		 String pJson = "{ 'companyName': { 'contains':  \"'\" } }";
		 Map map = JsonGson.fromJson(pJson);
		 Predicate pred = Predicate.predicateFromMap(map);
		 assertTrue(pred != null);
		 assertTrue(pred instanceof BinaryPredicate);
		 BinaryPredicate bpred = (BinaryPredicate) pred;
		 assertTrue(bpred.getOperator() == Operator.Contains);
		 assertTrue(bpred.getExpr1Source().equals("companyName"));
		 assertTrue(bpred.getExpr2Source().equals("'"));
		 
		 IEntityType et = _metadataWrapper.getEntityTypeForResourceName("Customers");
		 pred.validate(et);
		 PropExpression expr1 = (PropExpression) bpred.getExpr1();
		 assertTrue(expr1.getPropertyPath().equals("companyName"));
		 LitExpression expr2 = (LitExpression) bpred.getExpr2();
		 assertTrue(expr2.getDataType() == DataType.String);
		 assertTrue(expr2.getValue().equals("'"));
	}
	
	public void testBinaryExplicitDate() {
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
		 
		 IEntityType et = _metadataWrapper.getEntityTypeForResourceName("Orders");
		 pred.validate(et);
		 PropExpression expr1 = (PropExpression) bpred.getExpr1();
		 assertTrue(expr1.getPropertyPath().equals("shippedDate"));
		 LitExpression expr2 = (LitExpression) bpred.getExpr2();
		 assertTrue(expr2.getDataType() == DataType.DateTime);
		 assertTrue(expr2.getValue().equals(new Date(115,1,9))); // wierd rules: yy - 1900, mm (0-11), dd (1-31)
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
		 
		 IEntityType et = _metadataWrapper.getEntityTypeForResourceName("Employees");
		 pred.validate(et);
		 PropExpression expr1 = (PropExpression) bpred.getExpr1();
		 assertTrue(expr1.getPropertyPath().equals("lastName"));
		 LitExpression expr2 = (LitExpression) bpred.getExpr2();
		 assertTrue(expr2.getDataType() == DataType.String);
		 assertTrue(expr2.getValue().equals("firstName"));
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
		 BinaryPredicate bpred1 = (BinaryPredicate) pred1;
		 assertTrue(bpred1.getOperator() == Operator.GreaterThan);
		 assertTrue(bpred1.getExpr1Source().equals("freight"));
		 assertTrue(bpred1.getExpr2Source().equals(100.0));
		 
		 Predicate pred2 = preds.get(1);
		 assertTrue(pred2 instanceof BinaryPredicate);
		 BinaryPredicate bpred2 = (BinaryPredicate) pred2;
		 assertTrue(bpred2.getOperator() == Operator.StartsWith);
		 assertTrue(bpred2.getExpr1Source().equals("shipCity"));
		 assertTrue(bpred2.getExpr2Source().equals("S"));
		 
		 IEntityType et = _metadataWrapper.getEntityTypeForResourceName("Orders");
		 pred.validate(et);
		 
		 PropExpression expr1 = (PropExpression) bpred1.getExpr1();
		 assertTrue(expr1.getPropertyPath().equals("freight"));
		 LitExpression expr2 = (LitExpression) bpred1.getExpr2();
		 assertTrue(expr2.getDataType() == DataType.Decimal);
		 assertTrue(expr2.getValue().equals(BigDecimal.valueOf(100.0)));
		 
		 PropExpression expr1b = (PropExpression) bpred2.getExpr1();
		 assertTrue(expr1b.getPropertyPath().equals("shipCity"));
		 LitExpression expr2b = (LitExpression) bpred2.getExpr2();
		 assertTrue(expr2b.getDataType() == DataType.String);
		 assertTrue(expr2b.getValue().equals("S"));

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
		 
		 IEntityType et = _metadataWrapper.getEntityTypeForResourceName("Orders");
		 pred.validate(et);
		 
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
		 
		 IEntityType et = _metadataWrapper.getEntityTypeForResourceName("Orders");
		 pred.validate(et);
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
		 
		 IEntityType et = _metadataWrapper.getEntityTypeForResourceName("Orders");
		 pred.validate(et);
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
		 
		 IEntityType et = _metadataWrapper.getEntityTypeForResourceName("Customers");
		 pred.validate(et);
		 PropExpression propExpr = (PropExpression) aapred.getExpr();
		 assertTrue(propExpr.getPropertyPath().equals("orders"));
		 assertTrue(propExpr.getProperty().getName().equals("orders"));
		 
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
	
	public void testBinaryPredFn() {
		 String pJson = "{ 'toLower(\"shipName\")': 'abc' }";
		 Map map = JsonGson.fromJson(pJson);
		 Predicate pred = Predicate.predicateFromMap(map);
		 assertTrue(pred != null);
		 assertTrue(pred instanceof BinaryPredicate);
		 BinaryPredicate bpred = (BinaryPredicate) pred;
		 assertTrue(bpred.getOperator() == Operator.Equals);
		 assertTrue(bpred.getExpr1Source().equals("toLower(\"shipName\")"));
		 assertTrue(bpred.getExpr2Source().equals("abc"));
		 
		 IEntityType et = _metadataWrapper.getEntityTypeForResourceName("Orders");
		 pred.validate(et);
		 FnExpression expr1 = (FnExpression) bpred.getExpr1();
		 assertTrue(expr1.getFnName().equals("toLower"));
		 List<Expression> argExprs = expr1.getExpressions();
		 PropExpression argExpr1 = (PropExpression) argExprs.get(0);
		 assertTrue(argExpr1.getDataType() == DataType.String);
		 assertTrue(argExpr1.getPropertyPath().equals("shipName"));
	}
	             
	
}
