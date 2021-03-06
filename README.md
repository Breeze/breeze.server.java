# breeze-hibernate

This project is a Java library that facilitates building [Breeze](http://breeze.github.io/)-compatible backends using
[Hibernate](http://hibernate.org/orm/).  It is set up as a [Maven](http://maven.apache.org/) project, which builds a JAR that can then
be used as a library in a web application. 

Documentation and API docs at: https://breeze.github.io/doc-java-hib/

Source at: [https://github.com/Breeze/breeze.server.java](https://github.com/Breeze/breeze.server.java) 


Note: There is a separate `breeze-webserver` library (discussed later) that references the `breeze-hibernate` library to provide an easy path to creating a Java servlet app to wrap the breeze-hibernate functionality.  

### Features:

- Generates [Breeze metadata](http://breeze.github.io/doc-js/metadata) from Hibernate mappings
- Parses breeze client EntityQuery instances encoded as json into [Criteria](http://docs.jboss.org/hibernate/core/3.6/javadocs/org/hibernate/Criteria.html) queries
- Executes these queries using Hibernate Sessions
- Expands graphs of related entites using lazy loading.
- Serializes query results to [JSON](http://www.json.org/java/), using [$id/$ref syntax for handling references](https://blogs.oracle.com/sundararajan/entry/a_convention_for_circular_reference)
- Handles saving Breeze payloads in Hibernate


## Using the API

There are three main classes that you will use to do most of the work: HibernateQueryProcessor, HibernateSaveProcessor, and HibernateMetadata.  Each of these is a subclass of the generic QueryProcessor, SaveProcessor and Metadata classes respectively.

### HibernateQueryProcessor ( implements QueryProcessor)

The HibernateQueryProcessor class takes a breezejs *EntityQuery*, encoded as json, and converts it into one or more Hibernate criteria queries,  then executes them and provides a method to serializes the results as JSON. 

The *HibernateQueryProcessor* constructor takes *Metadata* object created as a result of parsing the Hibernate mapping collection along with a [SessionFactory](http://docs.jboss.org/hibernate/core/3.6/javadocs/org/hibernate/SessionFactory.html). Queries may then be executed by passing in a class and a json query string ( or an actual EntityQuery) to the *executeQuery* method.  This in turn internally creates a new [Session](http://docs.jboss.org/hibernate/core/3.6/javadocs/org/hibernate/Session.html) for each query that it executes.

##### EntityQuery to Criteria

The HibernateQueryProcessor typically builds Criteria queries from jsonified *EntityQuery* instances.  Each query also needs the Class on which the query operates.  Example:

    // metadata is the metadata for the hibernate model being queried
    // sessionFactory is a Hibernate SessionFactory
    QueryProcessor qp = new HibernateQueryProcessor(metadata, sessionFactory);
	// First 5 customers in 'Brazil'
    // typically this json string will have come from the client web app. 
    String json = "{ where: { country: 'Brazil' }, take: 5 }";

    // and then we execute it.
    QueryResult qr = qp.executeQuery(Customer.class, json);
    Collection results = qr.getResults();
	String jsonResults = qr.toJson();

	
Behind the scenes, that json string is parsed into an *EntityQuery* object, which is then converted into a Criteria query, which is then executed.  In a Java servelet app, the *QueryResult* object can be converted to json via a 'toJson' call and returned from the HTTP request to the breeze client.

Alternatively the server side breeze EntityQuery can be constructed via the EntityQuery construction api. The construction api provides a more structured, strongly typed alternative for creating a query.

    // Customers with company names starting with 'A'
    Predicate newPred = new BinaryPredicate(Operator.StartsWith,
                "companyName", "A");
    EntityQuery eq = new EntityQuery().where(newPred);
    // an alternative to ...
    // EntityQuery eq = new
    // EntityQuery("{ companyName: { startsWith: 'A' }}");
    QueryResult qr = executeQuery(Customer.class, eq);
	String jsonResults = qr.toJson();        


##### Combining client query with additional server query restrictions

In some scenarios, you may want to be able to allow the client to send  queries, but apply additional filters on the server.  Here's one way:

    // assuming the 'json' var came in via a HttpServlet request.
     
    // Create an EntityQuery based on what the original query from the client
	EntityQuery eq = new EntityQuery(json);
    
    // now we add an additional where clause and a take clause    
    Predicate newPred = new BinaryPredicate(Operator.StartsWith,
                    "companyName", 'B');
    // create a new EntityQuery object
    eq = eq.where(newPred).take(10);
    QueryResult qr = qp.executeQuery(Customer.class, eq);
    Collection results = qr.getResults();
	String jsonResults = qr.toJson();
     
Naturally you would apply appropriate exception handling in a real application.

##### InlineCount

The breeze EntityQuery *setInlineCount* and *expand* capabilities are treated specially because of the way they affect the query.

The inlinecount capability is used to get the total number of results that **would have been returned** if *skip* and *top* were not applied.  For server-side paging, *inlinecount* allows you to determine the total number of available pages.

For inlinecount, the Criteria query is essentially executed twice.  First `criteria.list()` is used to get the results.  Then the skip (first result), top (max results), and orderBy operations are removed from the Criteria, and a projection is applied to get the count of the rows.

**Without** `inlinecount`, Ex: **{ take: 2 }** ; the JSON result is an array of objects:

	[
		{
			$id: "0",
			$type: "northwind.model.Customer",
			companyName: "Island Trading",
			country: "UK",
			customerID: "008C5552-1FDE-421F-BDBF-F1C66C612AFA",
		},
		{
			$id: "1",
			$type: "northwind.model.Customer",
			companyName: "HILARION-Abastos",
			country: "Venezuela",
			customerID: "01858F10-9870-4D0F-8903-95223B3524A0",
		}
	]

**With** `inlinecount`, Ex: **{ take: 2, inlineCount: true }** the list of results are wrapped in an outer  object, and the JSON becomes:

	{
		$id: "0",
		$type: "com.breezejs.QueryResult",
		InlineCount: 96,
		Results: [
			{
				$id: "1",
				$type: "northwind.model.Customer",
				companyName: "Island Trading",
				country: "UK",
				customerID: "008C5552-1FDE-421F-BDBF-F1C66C612AFA",
			},
			{
				$id: "2",
				$type: "northwind.model.Customer",
				companyName: "HILARION-Abastos",
				country: "Venezuela",
				customerID: "01858F10-9870-4D0F-8903-95223B3524A0",
			}
		]
	}

The Breeze client handles both result types correctly.

##### Expand

The *expand* capability (Ex: **{ take: 2, expand: 'orders' }** causes entities related to the root entity to be included in the result.  While conceptually related to a JOIN operation in SQL, the semantics are actually quite different.  In order to preserve the shape and relationships of the original entities, and get accurate row counts for paging, breezejs-hibernate does not use joins, but relies on [Hibernate Select fetching](http://docs.jboss.org/hibernate/orm/3.6/reference/en-US/html/performance.html#performance-fetching) (lazy loading).

When an Breeze query is turned into a Criteria, the expands are kept separate. 
After the Criteria query is executed, the expands are processed by the HibernateExpander class, which performs `Hibernate.initialize()` on each of the associations.

Naturally, the disadvantage of select fetching is that it results in more queries. 
The performance impacts can be minimized by using [batch fetching](http://docs.jboss.org/hibernate/orm/3.6/reference/en-US/html/performance.html#performance-fetching-batch).  Consider setting the `default_batch_fetch_size` in your Hibernate configuration:

	<property name="default_batch_fetch_size">32</property>

You may also consider using a [second-level cache](http://docs.jboss.org/hibernate/orm/3.6/reference/en-US/html/performance.html#performance-cache).


### HibernateSaveProcessor ( implements SaveProcessor)

The `HibernateSaveProcessor` class is responsible for taking a JSON payload from the Breeze client, 
and saving it through Hibernate.  Under the hood several steps occur in this proces.

1. Converting from JSON to Java entities
1. Re-establishing the relationships between entities, based on the foreign keys
1. Calling *SaveWorkState.beforeSaveEntities* to allow pre-save processing
1. Associating the entities to a Hibernate Session
2. Call *SaveWorkState.beforeCommit* to allow additional pre-save processing
1. Saving the entities in the session
1. Keeping track of the mapping between temporary (client-generated) keys and real (server-generated) keys.
1. Removing the relationships between entities, so they can be returned to the Breeze client.
1. Calling *SaveWorkState.afterSaveEntities* to allow post-save processing
1. Converting the entities and key mappings to a *SaveResult*
1. Converting the *SaveResult* to JSON
1. Handling errors

Errors from the database or in other processing are returned as an HTTP 500 response.  Data validation errors or other application-supplied EntityErrors are returned as an HTTP 403 (Forbidden) response.


#### Example
The HibernateSaveProcessor is used to implement any endpoint to a breezejs SaveChanges call. In the example below, we are assuming that the java servlet method has been called as a result of a breezeJs *saveChanges* call.  

	 public void saveChanges(HttpServletRequest request,
            HttpServletResponse response) {
        // extractSaveBundle is a method in the breeze-webservice lib
        // that will be described later.
        Map saveBundle = extractSaveBundle(request);
        SaveWorkState sws = new SaveWorkState(saveBundle);
        SaveProcessor processor = new HibernateSaveProcessor(metadata, sessionFactory);
        SaveResult sr = processor.saveChanges(saveWorkState);
        
        
        writeSaveResponse(response, sr);
    }
    
The *SaveWorkState* object mentioned above is a wrapper over the save data that is passed in from the client saveChanges call.  In addition, the *SaveWorkState* may be subclassed to add custom handling to the save process.  The SaveWorkState has a *beforeSaveEntity*, *beforeSaveEntities* and a *beforeCommit* method that will all be called during save processing.
These are discussed in more detail in the `breeze-webserver` library section below. 

### HibernateMetadata extends Metadata

The Breeze client requires metadata about the domain model in order to entities manage entities.  The HibernateSaveProcessor also requires metadata to convert foreign keys into navigation properties for related entities.  Metadata for both of these purposes is provided by the `Metadata` class and in particular its specialized  HibernateMetadata subclass.

The HibernateMetadata class uses Hibernate's Metadata API to get information about entity mappings
and relationships.  This information should be consistent whether mapping is done using .hbm.xml files, annotations, or programatically.  

The HibernateMetadata class requires the Hibernate SessionFactory and the Configuration.  Depending upon the Hibernate version, the Configuration may be accessible from the SessionFactory itself; then you can use the constructor

	public HibernateMetadata(SessionFactory sessionFactory)

Otherwise, you will need to provide the Configuration:

	public HibernateMetadata(SessionFactory sessionFactory, Configuration configuration)

Calling the *build* method then populates the new instance.

If you're using [Spring Framework](http://projects.spring.io/spring-framework/), you may need to follow [this advice](http://stackoverflow.com/questions/2736100/how-can-i-get-the-hibernate-configuration-object-from-spring) to get the Configuration.

Building the Metadata is a relatively expensive operation, and metadata doesn't change during the run time of the app.  The result should be cached and used for all subsequent requests.

### JSON Serialization

Currently, breeze-hibernate performs JSON serialization using the   [Google's GSON library](https://code.google.com/p/google-gson/ "Google's Gson library") library and several custom Gson TypeAdapters. These custom adapters are necessary to support handling circular references in a manner that is compatible with the default breezeJs configuration settings. (and thus with [Json.NET](http://james.newtonking.com/json/help/index.html?topic=html/PreserveObjectReferences.htm)) and to allow correct handling of Hibernate proxies.
  

## Caveats and Limitations
#### Foreign Keys Must Be Mapped

Unlike Hibernate itself, Breeze requires foreign keys that are mapped to object properties so Breeze can maintain the relationships on the client side. Here's an example, mapping a relationship from Order to Customer:

	<many-to-one name="Customer" column="`CustomerID`" class="Customer" />
	<property name="CustomerID" type="System.Guid" insert="false" update="false" />

The "Customer" property is mapped normally, while the "CustomerID" property is mapped with `insert="false"` and `update="false"`. This way, the CustomerID is exposed to Breeze, but Hibernate will perform inserts and updates using the ID of the Customer object itself.

##### Possible Fix?
Foreign keys are required on the client for Breeze to work. They are also required to 
re-connect the entities on the server during the SaveChanges processing.  However, we
should be able to generate the keys automatically, without having to map them in the 
model.  Our plan is to:

1. Create necessary foreign key properties in the metadata where they don't exist in the real model. These would be marked as "synthetic" somehow ('$' prefix, special property, etc).
2. During the JSON serialization process, populate the synthetic foreign keys from the related entities or Hibernate proxies.
3. During the JSON deserialization process (when saving), carry the synthetic foreign key information along with the entity, so it can be used to re-establish relationships or create Hibernate proxies.

Note that this is closely tied to the JSON serialization process.

#### Limitations

Currently, `breeze-hibernate` supports the entire spectrum breeze query and save capabilities with the exception of:

1. breeze server side functions i.e. The 'month' function in the following predicate:  
    > { where: { 'month(birthDate)': { gt: 3}}}

2. projections of collection properties. i.e. the 'orders' property below.
    > { where: { companyName: { startsWith: 'B'  } }, select: 'orders' }
    

# breeze-webserver

This project is a Java library that builds on top of breeze-hibernate by making it relatively easy to build a breeze backend webserver servlet app. 
 
This library has two primary classes

- AppContextListener - This is a very simple class that simply caches the Hibernate sessionFactory and makes a single Metadata call against the current Hibernate model and caches this as well. The BreezeControllerServlet base class makes use of both of these.
- BreezeControllerServlet - This is the primary class for this library. You will typically subclass this class and add a variety of NamedQuery and NamedSave methods for any nondefault queries and saves within your app.  You will also write any beforeSave and afterSave interception methods within your subclass.
 
### Subclassing BreezeControllerServlet

A simple example of subclassing the BreezeControllerServlet is shown below, along with an example of how to add save interception methods.

    public class NorthwindTestServlet extends BreezeControllerServlet {

      @Override
      public SaveWorkState createSaveWorkState(Map saveBundle) {
        return new SaveWorkState(saveBundle) {
           /* all of the overriden methods below have access to all of the  SaveWorkState data and methods. These methods provide a simple 
           means to find/add/remove/modify any entities involved in the save pipeline - see the api documentation for more detail 
           */  

           @Override
           public boolean beforeSaveEntity(EntityInfo entityInfo) {
             // custom code here -
           }

           @Override
           public void beforeSaveEntities() {
             // custom code here -
           }

           @Override   
           public void afterSaveEntities() {
    	     // custom code here -
           }

           // other overriden SaveWorkState methods here ...
        }     
     }
           
### Queries and 'Named Queries'

The BreezeControllerServlet will automatically route and handle any servlet requests that are not *Metadata* or *SaveChanges* operations as queries. 

If there is a public method whose name matches the resource name in the servlet request on your subclass of the BreezeControllerServlet then this method will be called. This is referred to as a 'Named Query'.  If a matching method cannot be found, the BreezeControllerServlet will consider this a 'Standard Query' and will attempt to interpret the resource name and map it to a query against one of the EntityTypes in your Hibernate domain model. It uses the Metadata's resourceName/EntityType mapping for this purpose. The remainder of the incoming url will be interpreted as a json serialized EntityQuery.  'Standard' queries are nice because you will not need to do any additional work to handle them in your subclass.

#### Example of a 'Named Query'.

The 'Named' query below would be called from a breeze client to return a list of customers the 'expand' to include the Orders associated with these customers will be added on the server. 

Javascript client: 

    var q = EntityQuery.from("CustomersAndOrders").where("companyName", "startsWith", "P");
    myEntityManager.executeQuery(q).then(...);

Java Servlet:

    public class NorthwindTestServlet extends BreezeControllerServlet {

      public void CustomersAndOrders(HttpServletRequest request,
            HttpServletResponse response) {
        // extractEntityQuery is a built in method on the BreezeControllerServlet class 
        // that converts the url string in the HttpRequest into an EntityQuery instance.
        EntityQuery eq = this.extractEntityQuery(request);
        // create a new EntityQuery object
        eq = eq.expand("orders");
        QueryResult qr = executeQuery(Customer.class, eq);
        // writeQueryResponse is a built-in method of the BreezeControllerServlet class.
        this.writeQueryResponse(response, qr);
      }
    }

### Saves and 'Named Saves'

he BreezeControllerServlet will automatically route and handle any servlet requests that the result of client *SaveChanges* operations, i.e. any javascript operations that look like

Javascript client:

    myEntityManager.saveChanges(...).then(...);

In other words, if you are doing a standard breezeJs save operation you will not need to do anything further than simply instantiate an instance of your BreezeControllerServlet and these saves will be performed without any further intervention on your part.

However, if you want more control over the save process, either to validate the data being saved or to possibly add/modify/remove some the data being saved then you can create a 'Named Save' interception point.  

This involves simply naming a method in your servlet subclass with the same name used as a 'resourceName' in the client js saveChanges call.

In the example below, we perform a standard save plus the addition of an additional comment record added for each save operation performed.

Javascript client:

    var so = new SaveOptions({ resourceName: "SaveWithComment", tag: "some additional info" });
    em.saveChanges(null, so).then(...)

Java Servlet:  

    public void SaveWithComment(HttpServletRequest request,
            HttpServletResponse response) {
      Map saveBundle = extractSaveBundle(request);
      SaveWorkState sws = new SaveWorkState(saveBundle) {
          @Override
          // public void beforeSaveEntities() {
          public void beforeCommit(Object context) {
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

  
