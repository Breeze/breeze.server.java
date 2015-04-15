package northwind.service;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import northwind.model.Category;
import northwind.model.Comment;
import northwind.model.Customer;
import northwind.model.Employee;
import northwind.model.Order;
import northwind.model.Product;
import northwind.model.Region;
import northwind.model.Supplier;

import com.breeze.save.EntityError;
import com.breeze.save.EntityErrorsException;
import com.breeze.save.EntityInfo;
import com.breeze.save.EntityState;
import com.breeze.save.KeyMapping;
import com.breeze.save.SaveResult;
import com.breeze.save.SaveWorkState;
import com.breeze.webserver.BreezeControllerServlet;
import com.breeze.metadata.MetadataHelper;
import com.breeze.query.AndOrPredicate;
import com.breeze.query.BinaryPredicate;
import com.breeze.query.EntityQuery;
import com.breeze.query.Operator;
import com.breeze.query.Predicate;
import com.breeze.query.QueryResult;

@SuppressWarnings( {"unused", "unchecked"})
public class NorthwindTestServlet extends BreezeControllerServlet {
    private static final long serialVersionUID = 1L;

    public void customersInBrazil(HttpServletRequest request,
            HttpServletResponse response) {
        String json = "{ where: { country: 'Brazil' }, take: 5 }";
        QueryResult qr = executeQuery("Customers", json);
        writeResponse(response, qr.toJson());
    }

    public void CustomerFirstOrDefault(HttpServletRequest request,
            HttpServletResponse response) {
        // should return empty array
        Predicate newPred = new BinaryPredicate(Operator.StartsWith,
                "companyName", "blah");
        EntityQuery eq = new EntityQuery().where(newPred);
        QueryResult qr = executeQuery(Customer.class, eq);
        writeQueryResponse(response, qr);
    };

    public void CustomersStartingWithA(HttpServletRequest request,
            HttpServletResponse response) {
        Predicate newPred = new BinaryPredicate(Operator.StartsWith,
                "companyName", "A");
        EntityQuery eq = new EntityQuery().where(newPred);
        // or ...
        // EntityQuery eq = new
        // EntityQuery("{ companyName: { startsWith: 'A' }}");
        QueryResult qr = executeQuery(Customer.class, eq);
        writeQueryResponse(response, qr);

    };

    public void CustomersStartingWith(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        String companyName = (String) eq.getParameters().get("companyName");
        if (companyName != null) {
            Predicate newPred = new BinaryPredicate(Operator.StartsWith,
                    "companyName", companyName);
            // create a new EntityQuery object
            eq = eq.where(newPred);
        } else {
            if (!eq.getParameters().containsKey("companyName")) {
                writeError(response, 404,
                        "'companyName' parameter should have been passed in");
                return;
            }
        }
        QueryResult qr = executeQuery(Customer.class, eq);
        writeQueryResponse(response, qr);
    }

    public void CustomersOrderedStartingWith(HttpServletRequest request,
            HttpServletResponse response) {
        // start with client query and add an additional filter.
        EntityQuery eq = this.extractEntityQuery(request);
        String companyName = (String) eq.getParameters().get("companyName");

        Predicate newPred = new BinaryPredicate(Operator.StartsWith,
                "companyName", companyName);

        eq = eq.where(newPred).orderBy("companyName");
        QueryResult qr = executeQuery(Customer.class, eq);
        writeQueryResponse(response, qr);
    }

    public void CustomersAndOrders(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        // create a new EntityQuery object
        eq = eq.expand("orders");
        QueryResult qr = executeQuery(Customer.class, eq);
        writeQueryResponse(response, qr);
    }

    public void CustomerWithScalarResult(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        // create a new EntityQuery object
        eq = eq.take(1);
        QueryResult qr = executeQuery(Customer.class, eq);
        writeQueryResponse(response, qr);
    }

    public void CustomersWithHttpError(HttpServletRequest request,
            HttpServletResponse response) {
        this.writeError(response, 404, "Unable to do something");
    }

    public void CustomersAsHRM(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        QueryResult qr = executeQuery(Customer.class, eq);
        writeQueryResponse(response, qr);
    }

    public class CustomerWithBigOrders {
        public Customer customer;
        public List<Order> bigOrders;

        public CustomerWithBigOrders(Customer customer, List<Order> bigOrders) {
            this.customer = customer;
            this.bigOrders = bigOrders;
        }
    }

    public void CustomersWithBigOrders(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        String json = "{ orders: { any: { freight: { gt: 100 } } } }";
        eq = eq.where(json).expand("orders");
        QueryResult result = executeQuery(Customer.class, eq);
        QueryResult qr = executeQuery(Customer.class, eq);
        List<CustomerWithBigOrders> customersWithBigOrders = new ArrayList<CustomerWithBigOrders>();
        for (Object o : qr.getResults()) {
            Customer c = (Customer) o;
            List<Order> bigOrders = new ArrayList<Order>();
            for (Order order : c.getOrders()) {
                if (order.getFreight().doubleValue() > 100.0) {
                    bigOrders.add(order);
                }
            }
            if (bigOrders.size() > 0) {
                CustomerWithBigOrders cwbo = new CustomerWithBigOrders(c,
                        bigOrders);
                customersWithBigOrders.add(cwbo);
            }
        }

        qr = new QueryResult(customersWithBigOrders);
        writeQueryResponse(response, qr);
    }

    public class CustomersAndProductsBundle {
        public List<Customer> customers;
        public List<Product> products;

        public CustomersAndProductsBundle(List<Customer> customers,
                List<Product> products) {
            this.customers = customers;
            this.products = products;
        }
    }

    public void CustomersAndProducts(HttpServletRequest request,
            HttpServletResponse response) {

        EntityQuery eq = new EntityQuery();
        QueryResult cresult = executeQuery(Customer.class, new EntityQuery());
        QueryResult presult = executeQuery(Product.class, new EntityQuery());
        List<CustomersAndProductsBundle> list = new ArrayList<CustomersAndProductsBundle>();
        list.add(new CustomersAndProductsBundle((cresult
                .getResults()), (presult.getResults())));

        QueryResult qr = new QueryResult(list);
        writeQueryResponse(response, qr);
    }

    // AltCustomers will not be in the resourceName/entityType map;
    public void AltCustomers(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        QueryResult qr = this.executeQuery(Customer.class, eq);
        writeQueryResponse(response, qr);
    }

    public void SearchCustomers(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        Map parameterMap = eq.getParameters();
        String companyName = (String) parameterMap.get("CompanyName");
        ArrayList contactNames = (ArrayList) parameterMap.get("ContactNames");
        String city = (String) parameterMap.get("City");
        boolean ok = companyName.length() > 0 && contactNames.size() > 1
                && city.length() > 0;
        if (!ok) {
            writeError(response, 400, "Unable to properly parse parameters");
        }
        // just testing that qbe actually made it in not attempted to write qbe
        // logic here
        // so just return first 3 customers.
        EntityQuery altQ = new EntityQuery().take(3);
        QueryResult qr = this.executeQuery(Customer.class, altQ);
        writeQueryResponse(response, qr);
    }

    public void SearchCustomers2(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        Map parameterMap = eq.getParameters();
        ArrayList qbeList = (ArrayList) parameterMap.get("qbeList");
        for (Object qbeItem : qbeList) {
            Map qbeMap = (Map) qbeItem;

            String companyName = (String) qbeMap.get("CompanyName");
            ArrayList contactNames = (ArrayList) qbeMap.get("ContactNames");
            String city = (String) qbeMap.get("City");
            boolean ok = companyName.length() > 0 && contactNames.size() > 0
                    && city.length() > 0;
            if (!ok) {
                writeError(response, 400, "Unable to properly parse parameters");
                return;
            }
        }
        // just testing that qbe actually made it in not attempted to write qbe
        // logic here
        // so just return first 3 customers.
        EntityQuery altQ = new EntityQuery().take(3);
        QueryResult qr = this.executeQuery(Customer.class, altQ);
        writeQueryResponse(response, qr);
    }

    public void OrdersCountForCustomer(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        Map parameterMap = eq.getParameters();
        String companyName = (String) parameterMap.get("companyName");
        // EntityQuery eq2 = new EntityQuery();
        eq = eq.where(new BinaryPredicate(Operator.StartsWith, "companyName",
                companyName));
        eq = eq.expand("orders");
        eq = eq.take(1);
        QueryResult qr = this.executeQuery(Customer.class, eq);
        List<Customer> custResults = qr.getResults();
        List<Integer> results = new ArrayList<Integer>();
        if (custResults.size() > 0) {
            Customer cust = (Customer) qr.getResults().get(0);
            results.add(cust.getOrders().size());
        } else {
            results.add(0);
        }

        qr = new QueryResult(results);
        writeQueryResponse(response, qr);
    }

    public void EnumerableEmployees(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        QueryResult qr = this.executeQuery(Employee.class, eq);
        writeQueryResponse(response, qr);
    }

    public void EmployeesMultipleParams(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        Map parameterMap = eq.getParameters();
        Integer empId = ((Double) parameterMap.get("employeeID")).intValue();
        String city = (String) parameterMap.get("city");
        Predicate pred1 = new BinaryPredicate(Operator.Equals, "employeeID",
                empId);
        Predicate pred2 = new BinaryPredicate(Operator.Equals, "city", city);
        Predicate pred = new AndOrPredicate(Operator.And, pred1, pred2);
        EntityQuery eq2 = new EntityQuery().where(pred);
        QueryResult qr = this.executeQuery(Employee.class, eq2);
        writeQueryResponse(response, qr);
    }

    public void CompanyNames(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = new EntityQuery().select("companyName");
        QueryResult qr = this.executeQuery(Customer.class, eq);
        writeQueryResponse(response, qr);
    }

    public void CompanyNamesAndIds(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        eq = eq.select("companyName", "customerID");
        QueryResult qr = this.executeQuery(Customer.class, eq);
        writeQueryResponse(response, qr);
    }

    public void CompanyNamesAndIdsAsDTO(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        eq = eq.select("companyName", "customerID");
        QueryResult qr = this.executeQuery(Customer.class, eq);
        writeQueryResponse(response, qr);
    }

    public void CompanyInfoAndOrders(HttpServletRequest request,
            HttpServletResponse response) {
        // TODO: right now this will fail because we don't know
        // how to make hibernate project collections.
        EntityQuery eq = this.extractEntityQuery(request);
        eq = eq.select("companyName", "customerID", "orders");
        QueryResult qr = this.executeQuery(Customer.class, eq);
        writeQueryResponse(response, qr);
    }

    public void OrdersAndCustomers(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        eq = eq.expand("customer");
        QueryResult qr = this.executeQuery(Order.class, eq);
        writeQueryResponse(response, qr);
    }

    public void SearchEmployees(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        Map parameterMap = eq.getParameters();
        ArrayList tmpEmpIds = (ArrayList) parameterMap.get("employeeIds");
        List<Integer> empIds = new ArrayList<Integer>();
        for (Object val : tmpEmpIds) {
            empIds.add(((Double) val).intValue());
        }
        eq = eq.where(new BinaryPredicate(Operator.In, "employeeID", empIds));

        QueryResult qr = this.executeQuery(Employee.class, eq);
        writeQueryResponse(response, qr);
    }

    public void EmployeesFilteredByCountryAndBirthdate(
            HttpServletRequest request, HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        Map parameterMap = eq.getParameters();
        String tmpBirthDate = (String) parameterMap.get("birthDate");
        try {

            Date birthDate = new SimpleDateFormat("mm/dd/yy")
                    .parse(tmpBirthDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(birthDate);
            tmpBirthDate = javax.xml.bind.DatatypeConverter.printDateTime(cal);

        } catch (Exception e) {
            // ok - this just means that we should allow the breeze predicate to
            // parse it.
        }
        // Date birthDate = (Date) DataType.coerceData(tmpBirthDate,
        // DataType.DateTime);
        String country = (String) parameterMap.get("country");
        Predicate pred1 = new BinaryPredicate(Operator.GreaterThanOrEqual,
                "birthDate", tmpBirthDate);
        Predicate pred2 = new BinaryPredicate(Operator.Equals, "country",
                country);
        Predicate pred = new AndOrPredicate(Operator.And, pred1, pred2);
        eq = eq.where(pred);
        QueryResult qr = this.executeQuery(Employee.class, eq);
        writeQueryResponse(response, qr);
    }

    // Saves

    public void SaveWithFreight(HttpServletRequest request,
            HttpServletResponse response) {
        Map saveBundle = extractSaveBundle(request);
        SaveWorkState sws = new SaveWorkState(saveBundle) {
            @Override
            public boolean beforeSaveEntity(EntityInfo entityInfo) {
                String tag = (String) this.getSaveOptions().tag;
                CheckFreight(entityInfo, tag);
                return true;
            }
        };

        SaveResult sr = saveChanges(sws);
        writeSaveResponse(response, sr);
    }
    
    public void SaveWithFreight2(HttpServletRequest request,
            HttpServletResponse response) {
        Map saveBundle = extractSaveBundle(request);
        SaveWorkState sws = new SaveWorkState(saveBundle) {
            @Override
            public void beforeSaveEntities()       
                    throws EntityErrorsException {
                String tag = (String) this.getSaveOptions().tag;
                List<EntityInfo> orderInfos = this.getEntityInfos(Order.class);
                if (orderInfos != null) {
                    for (EntityInfo entityInfo : orderInfos) {
                        CheckFreight(entityInfo, tag);
                    }
                }

            }
        };

        SaveResult sr = saveChanges(sws);
        writeSaveResponse(response, sr);
    }

    
    private void CheckFreight(EntityInfo entityInfo, String tag) {
        Order order = (Order) entityInfo.entity;
        if (tag.equals("freight update")) {
            order.setFreight(order.getFreight()
                    .add(new BigDecimal(1.0)));
        } else if (tag.equals("freight update-ov")) {
            order.setFreight(order.getFreight()
                    .add(new BigDecimal(1.0)));
            entityInfo.originalValuesMap.put("freight", null);
        } else if (tag.equals("freight update-force")) {
            order.setFreight(order.getFreight()
                    .add(new BigDecimal(1.0)));
            entityInfo.forceUpdate = true;
        }
    }

    public void SaveWithExit(HttpServletRequest request,
            HttpServletResponse response) {

        SaveResult sr = new SaveResult(new ArrayList<Object>(),
                new ArrayList<KeyMapping>());
        writeSaveResponse(response, sr);
    }

    public void SaveAndThrow(HttpServletRequest request,
            HttpServletResponse response) {

        SaveResult sr = new SaveResult(new ArrayList<Object>(),
                new ArrayList<KeyMapping>());
        writeSaveResponse(response, sr);
    }

    public void SaveWithEntityErrorsException(HttpServletRequest request,
            HttpServletResponse response) {

        Map saveBundle = extractSaveBundle(request);
        SaveWorkState sws = new SaveWorkState(saveBundle) {
            @Override
            public void beforeSaveEntities()  throws EntityErrorsException {
                List<EntityInfo> orderInfos = this.getEntityInfos(Order.class);
                if (orderInfos != null) {
                    List<EntityError> errors = new ArrayList<EntityError>();
                    for (EntityInfo ei : orderInfos) {
                        Object[] keyValues = new Object[] { ((Order) ei.entity)
                                .getOrderID() };
                        EntityError err = new EntityError("WrongMethod",
                                MetadataHelper.getEntityTypeName(Order.class),
                                keyValues, "orderID",
                                "Cannot save orders with the save method");
                        errors.add(err);
                    }
                    EntityErrorsException ex = new EntityErrorsException(
                            "test of custom exception message", errors);
                    throw ex;
                }
                
            }
        };

        SaveResult sr = saveChanges(sws);
        writeSaveResponse(response, sr);
    }

    public void SaveCheckUnmappedProperty(HttpServletRequest request,
            HttpServletResponse response) {

        Map saveBundle = extractSaveBundle(request);
        SaveWorkState sws = new SaveWorkState(saveBundle) {
            @Override
            public boolean beforeSaveEntity(EntityInfo entityInfo) {
                String unmappedValue = (String) entityInfo.unmappedValuesMap
                        .get("myUnmappedProperty");

                if (!unmappedValue.equals("anything22")) {
                    throw new RuntimeException(
                            "wrong value for unmapped property:  "
                                    + unmappedValue);
                }
                Customer cust = (Customer) entityInfo.entity;
                return false;
            }
        };

        SaveResult sr = saveChanges(sws);
        writeSaveResponse(response, sr);
    }
    
    public void SaveCheckUnmappedPropertySuppressed(HttpServletRequest request,
            HttpServletResponse response) {

        Map saveBundle = extractSaveBundle(request);
        SaveWorkState sws = new SaveWorkState(saveBundle) {
            @Override
            public boolean beforeSaveEntity(EntityInfo entityInfo) {
                Map map = entityInfo.unmappedValuesMap;
                if (map != null) {
                    throw new RuntimeException("unmapped properties should have been suppressed");
                }
                
                return false;
            }
        };

        SaveResult sr = saveChanges(sws);
        writeSaveResponse(response, sr);
    }

    public void SaveCheckUnmappedPropertySerialized(HttpServletRequest request,
            HttpServletResponse response) {

        Map saveBundle = extractSaveBundle(request);
        SaveWorkState sws = new SaveWorkState(saveBundle) {
            @Override
            public boolean beforeSaveEntity(EntityInfo entityInfo) {
                Map map = entityInfo.unmappedValuesMap;
                String unmappedValue = (String) map.get("myUnmappedProperty");

                if (!unmappedValue.equals("ANYTHING22")) {
                    throw new RuntimeException("wrong value for unmapped property:  " + unmappedValue);
                }

                Map anotherOne = (Map) map.get("anotherOne");
                
                List zs = (List) anotherOne.get("z");
                Map map2 = (Map) zs.get(5);
                Double fooValue = (Double) map2.get("foo");
                if (fooValue != 4.0) {
                    throw new RuntimeException("wrong value for 'anotherOne.z[5].foo'");
                }

                Double extra = (Double) anotherOne.get("extra");
                if (extra != 666.0) {
                    throw new RuntimeException("wrong value for 'anotherOne.extra'");
                }               

                Customer cust = (Customer) entityInfo.entity;
                if (!cust.getCompanyName().toUpperCase().equals(cust.getCompanyName())) {
                    throw new RuntimeException("Uppercasing of company name did not occur");
                }
                return false;
            }
        };

        SaveResult sr = saveChanges(sws);
        writeSaveResponse(response, sr);
    }


    public void SaveWithDbTransaction(HttpServletRequest request,
            HttpServletResponse response) {
        // same code as regular saveChanges
        Map saveBundle = extractSaveBundle(request);
        SaveWorkState sws = createSaveWorkState(saveBundle);
        SaveResult sr = saveChanges(sws);
        writeSaveResponse(response, sr);
    }
    
    public void SaveWithComment(HttpServletRequest request,
            HttpServletResponse response) {
        Map saveBundle = extractSaveBundle(request);
        SaveWorkState sws = new SaveWorkState(saveBundle) {
            @Override
            public void beforeSaveEntities() {
                Comment comment = new Comment();
                String tag = (String) this.getSaveOptions().tag;
                
                comment.setComment1((tag == null) ? "Generic comment" : tag);
                comment.setCreatedOn(new Date());
                comment.setSeqNum((byte) 1);
                this.addEntity(comment,  EntityState.Added);
                
            }
        };

        SaveResult sr = saveChanges(sws);
        writeSaveResponse(response, sr);
    }
    
    public void SaveCheckInitializer(HttpServletRequest request,
            HttpServletResponse response) {
        Map saveBundle = extractSaveBundle(request);
        SaveWorkState sws = new SaveWorkState(saveBundle) {
            @Override
            public void beforeSaveEntities() {
                // Create and add a new order.
                Order order = new Order();
                order.setOrderDate(new Date());
                this.addEntity(order, EntityState.Added);
            }
        };

        SaveResult sr = saveChanges(sws);
        writeSaveResponse(response, sr);
    }


    @Override
    public SaveWorkState createSaveWorkState(Map saveBundle) {
        SaveWorkState sws = new SaveWorkState(saveBundle) {
            @Override
            public boolean beforeSaveEntity(EntityInfo entityInfo) {
                if (entityInfo.entity instanceof Customer) {
                    Customer c = (Customer) entityInfo.entity;
                    if (c.getCompanyName().toLowerCase().equals("error")) {
                        Object[] key = new Object[] { c.getCustomerID() };
                        EntityError err = new EntityError("WrongMethod",
                                MetadataHelper.getEntityTypeName(Customer.class),
                                key, "companyName",
                                "This customer is not valid!");
                        throw new EntityErrorsException(err);
                    }
                }

                String tag = (String) this.getSaveOptions().tag;
                if (tag != null && tag.equals("addProdOnServer")) {
                    // // adds cannot be performed here yet... ( see
                    // beforeSaveEntities instead).
                    // Supplier supplier = (Supplier) entityInfo.entity;
                    // Product product = new Product();
                    // product.setProductName("Product added on server");
                    // supplier.getProducts().add(product);
                    // return true;
                }

                if (entityInfo.entity instanceof Region && entityInfo.entityState == EntityState.Added) {
                    Region region = (Region) entityInfo.entity;
                    // prohibit any additions of entities of type 'Region'
                    if (region.getRegionDescription().toLowerCase().startsWith("error")) {
                        return false;
                    } else {
                        return true;
                    }
                }
                return true;
            }

            @Override
            public void beforeSaveEntities() {
                String tag = (String) this.getSaveOptions().tag;
                if (tag == null) return;
                if (tag.equals("addProdOnServer")) {
                    List<EntityInfo> entityInfos = this.getEntityInfos(Supplier.class);
                    if (entityInfos.size() > 0) {
                        for (EntityInfo ei : entityInfos) {
                            Supplier supplier = (Supplier) ei.entity;
                            Product product = new Product();
                            product.setProductName("Product added on server");
                            product.setSupplier(supplier);
                            // can do this instead; both work
                            // product.setSupplierID(supplier.getSupplierID());
                            this.addEntity(product, EntityState.Added);
                        }
                    }
                } else if (tag.equals("CommentOrderShipAddress.Before")) {
                    List<EntityInfo> orderInfos = this.getEntityInfos(Order.class);
                    byte seqNum = 1;
                    for (EntityInfo oi : orderInfos) {
                        Order order = (Order) oi.entity;
                        Comment comment = new Comment();
                        comment.setComment1(order.getShipAddress());
                        comment.setSeqNum(seqNum++);
                        comment.setCreatedOn(new Date());
                        this.addEntity(comment, EntityState.Added);

                    }
                } else if (tag.equals("increaseProductPrice")) {
                    List<EntityInfo> categoryInfos = this.getEntityInfos(Category.class);
                    for (EntityInfo ci : categoryInfos) {
                        Category category = (Category) ci.entity;
                        // need to get all of the products associated with this category
                        // add up their price by $1.
                        // TODO: but category.getProducts (below) returns null
                        // so how do I force load the products.
                        Set<Product> products = category.getProducts();
                        for (Product p : products) {
                            BigDecimal unitPrice = p.getUnitPrice();
                            unitPrice = unitPrice.add(new BigDecimal(1.0));
                            p.setUnitPrice(unitPrice);
                            this.addEntity(p, EntityState.Modified);
                        }
                    }
                }
               
                
            }
            


            public void AfterSaveEntities() {
                // not yet implemented
            }
        };
        return sws;
    }
    // BeforeSaveEntities/AfterSaveEntities logic copied from .NET tests
    /*
    protected override Dictionary<Type, List<EntityInfo>> BeforeSaveEntities(Dictionary<Type, List<EntityInfo>> saveMap) {

      var tag = (string)SaveOptions.Tag;

      if (tag == "CommentOrderShipAddress.Before") {
        var orderInfos = saveMap[typeof(Order)];
        byte seq = 1;
        foreach (var info in orderInfos) {
          var order = (Order)info.Entity;
          AddComment(order.ShipAddress, seq++);
        }
      } else if (tag == "UpdateProduceShipAddress.Before") {
        var orderInfos = saveMap[typeof(Order)];
        var order = (Order)orderInfos[0].Entity;
        UpdateProduceDescription(order.ShipAddress);
      } else if (tag == "LookupEmployeeInSeparateContext.Before") {
        LookupEmployeeInSeparateContext(false);
      } else if (tag == "LookupEmployeeInSeparateContext.SameConnection.Before") {
        LookupEmployeeInSeparateContext(true);
      } else if (tag == "ValidationError.Before") {
        foreach (var type in saveMap.Keys) {
          var list = saveMap[type];
          foreach (var entityInfo in list) {
            var entity = entityInfo.Entity;
            var entityError = new EntityError() {
              EntityTypeName = type.Name,
              ErrorMessage = "Error message for " + type.Name,
              ErrorName = "Server-Side Validation",
            };
            if (entity is Order) {
              var order = (Order)entity;
              entityError.KeyValues = new object[] { order.OrderID };
              entityError.PropertyName = "OrderDate";
            }

          }
        }
      } else if (tag == "increaseProductPrice") {
        Dictionary<Type, List<EntityInfo>> saveMapAdditions = new Dictionary<Type, List<EntityInfo>>();
        foreach (var type in saveMap.Keys) {
          if (type == typeof(Category)) {
            foreach (var entityInfo in saveMap[type]) {
              if (entityInfo.EntityState == EntityState.Modified) {
                Category category = (entityInfo.Entity as Category);
                var products = this.Context.Products.Where(p => p.CategoryID == category.CategoryID);
                foreach (var product in products) {
                  if (!saveMapAdditions.ContainsKey(typeof(Product)))
                    saveMapAdditions[typeof(Product)] = new List<EntityInfo>();

                  var ei = this.CreateEntityInfo(product, EntityState.Modified);
                  ei.ForceUpdate = true;
                  var incr = (Convert.ToInt64(product.UnitPrice) % 2) == 0 ? 1 : -1;
                  product.UnitPrice += incr;
                  saveMapAdditions[typeof(Product)].Add(ei);
                }
              }
            }
          }
        }
        foreach (var type in saveMapAdditions.Keys) {
          if (!saveMap.ContainsKey(type)) {
            saveMap[type] = new List<EntityInfo>();
          }
          foreach (var enInfo in saveMapAdditions[type]) {
            saveMap[type].Add(enInfo);
          }
        }
      }
    
     */

}
