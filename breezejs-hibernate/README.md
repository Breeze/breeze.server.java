# breezejs-hibernate

This project is a Java library that facilitates building [Breeze](http://www.breezejs.com/)-compatible backends using
[Hibernate](http://hibernate.org/orm/) and [JAX-RS (Jersey)](https://jersey.java.net/).  It is set up as a [Maven](http://maven.apache.org/) project, which builds a JAR that can then
be used as a library in a web application.

### Features:

- Generates [Breeze metadata](http://www.breezejs.com/documentation/metadata) from Hibernate mappings
- Parses (a subset of) [OData](http://www.odata.org/documentation/odata-version-3-0/url-conventions/) queries into [Criteria](http://docs.jboss.org/hibernate/core/3.6/javadocs/org/hibernate/Criteria.html) queries
- Executes queries using Hibernate Sessions
- Expands graphs of related entites using lazy loading
- Serializes query results to [JSON](http://www.json.org/java/), using [$id/$ref syntax for handling references](https://blogs.oracle.com/sundararajan/entry/a_convention_for_circular_reference)
- Handles saving Breeze payloads in Hibernate

To see these features in action, please see the [NorthBreeze sample](https://github.com/Breeze/breeze.js.samples/tree/master/java/NorthBreeze).

## Using the API

There are three main classes that you will use to do most of the work: QueryService, SaveService, and MetadataBuilder.

### QueryService

The QueryService class takes [OData](http://www.odata.org/documentation/odata-version-3-0/url-conventions/), [Criteria](http://docs.jboss.org/hibernate/orm/3.6/reference/en-US/html/querycriteria.html), and [HQL](http://docs.jboss.org/hibernate/orm/3.6/reference/en-US/html/queryhql.html) queries and returns the results as JSON.  It takes a [SessionFactory](http://docs.jboss.org/hibernate/core/3.6/javadocs/org/hibernate/SessionFactory.html) in the constructor, and creates a new [Session](http://docs.jboss.org/hibernate/core/3.6/javadocs/org/hibernate/Session.html) for each OData query that it executes.

##### OData

The QueryService builds Criteria queries from OData, so it needs the Class on which the query operates.  Example usages:

	// Get the first 5 Customers in Brazil
	QueryService qs = new QueryService(sessionFactory);
	String odataQuery = "?$top=5&$filter=Country eq 'Brazil'";
	String customerJson = qs.queryToJson(Customer.class, odataQuery);
	
Behind the scenes, that odataQuery string is parsed into an `OdataParameters` object, which is then converted into a Criteria query.  In a JAX-RS app, the `OdataParameters ` object can be automatically populated from the HTTP request:

	@GET
	@Path("Customers")
	public String getCustomers(@BeanParam OdataParameters odataParameters) {
		QueryService qs = new QueryService(this.sessionFactory);
		return queryService.queryToJson(Customer.class, odataParameters);
	}

The [@BeanParam](https://jax-rs-spec.java.net/nonav/2.0/apidocs/javax/ws/rs/BeanParam.html) annotation [tells JAX-RS](https://jersey.java.net/documentation/latest/jaxrs-resources.html#d0e1889) to create and populate the OdataParameters object from the HTTP request query string.

##### Criteria

The OData queries are turned into Criteria queries before execution.  You can also use a
Criteria query directly.  In this case, you create a Criteria from the session, and pass that to the queryToJson method:

	Session session = sessionFactory.openSession();
	Criteria crit = session.createCriteria(Customer.class);
	crit.add( Restrictions.like("companyName", "Alfred%") );
	crit.setMaxResults(50);
	QueryService qs = new QueryService(sessionFactory);
	String json = qs.queryToJson(crit, false, null);
	session.close();

Note that in this case, you open and close the session yourself.

##### OData and Criteria Together

In some scenarios, you may want to be able to allow the client to send OData queries,
but apply additional filters on the server.  Here's one way:

	@GET
	@Path("Orders")
	public String getOrders(@BeanParam OdataParameters odataParameters) {

		// Get the user's employee id (after they've logged in)
		int employeeId = getEmployeeIdFromHttpSession();

		Session session = sessionFactory.openSession();
		Criteria crit = session.createCriteria(Order.class);

		// User can only see Orders that match their employeeId
		crit.add( Restrictions.eq("employeeId", new Integer(employeeId));

    	// Apply OData parameters to the Criteria (skip, top, filter, etc.)
    	OdataCriteria.applyParameters(crit, odataParameters);

		// Execute the query, with optional inlineCount and expands
    	String json = queryToJson(crit, op.hasInlineCount(), op.expands());

		session.close();
		return json;
	}

Naturally you would apply appropriate exception handling in a real application.

##### InlineCount

The OData parameters `$inlinecount` and `$expand` are treated specially because of the way they affect the query.

The [$inlinecount](http://www.odata.org/documentation/odata-version-3-0/odata-version-3-0-core-protocol/#theinlinecountsystemqueryoption) parameter is used to get the total number of results that *would have been returned* if $skip and $top were not applied.  For server-side paging, $inlinecount allows you to determine the total number of available pages.

For $inlinecount, the Criteria query is essentially executed twice.  First `criteria.list()` is used to get the results.  Then the skip (first result), top (max results), and orderBy operations are removed from the Criteria, and a projection is applied to get the count of the rows.

**Without** `$inlinecount`, e.g. `/Customers?$top=2`, the JSON result is an array of objects:

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

**With** `$inlinecount`, e.g. `/Customers?$top=2&$inlinecount=allpages`, the list of results are wrapped in an outer QueryResult object, and the JSON becomes:

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

The OData [$expand](http://www.odata.org/documentation/odata-version-3-0/odata-version-3-0-core-protocol/#theexpandsystemqueryoption) parameter causes entities related to the root entity to be included in the result.  While conceptually related to a JOIN operation in SQL, the semantics are actually quite different.  In order to preserve the shape and relationships of the original entities, and get accurate row counts for paging, breezejs-hibernate does not use joins, but relies on [Hibernate Select fetching](http://docs.jboss.org/hibernate/orm/3.6/reference/en-US/html/performance.html#performance-fetching) (lazy loading).

When an OData query is turned into a Criteria, the $expands are kept separate. 
After the Criteria query is executed, the expands are processed by the HibernateExpander class, which performs `Hibernate.initialize()` on each of the associations.

Naturally, the disadvantage of select fetching is that it results in more queries. 
The performance impacts can be minimized by using [batch fetching](http://docs.jboss.org/hibernate/orm/3.6/reference/en-US/html/performance.html#performance-fetching-batch).  Consider setting the `default_batch_fetch_size` in your Hibernate configuration:

	<property name="default_batch_fetch_size">32</property>

You may also consider using a [second-level cache](http://docs.jboss.org/hibernate/orm/3.6/reference/en-US/html/performance.html#performance-cache).


### SaveService

The `SaveService` class is responsible for taking a JSON payload from the Breeze client, 
and saving it through Hibernate.  Under the hood, it relies on the `HibernateContext` 
class to do most of the work, which includes:

1. Converting from JSON to Java entities
1. Calling [beforeSaveEntities](http://www.breezejs.com/documentation/contextprovider) to allow pre-save processing
1. Re-establishing the relationships between entities, based on the foreign keys
1. Associating the entities to a Hibernate Session
1. Saving the entities in the session
1. Keeping track of the mapping between temporary (client-generated) keys and real (server-generated) keys.
1. Removing the relationships between entities, so they can be returned to the Breeze client.
1. Calling [afterSaveEntities](http://www.breezejs.com/documentation/contextprovider) to allow post-save processing
1. Converting the entities and key mappings to a SaveResult
1. Converting the SaveResult to JSON
1. Handling errors

Errors from the database or in other processing are returned as an HTTP 500 response.  Data validation errors or other application-supplied EntityErrors are returned as an HTTP 403 (Forbidden) response.

Use the SaveService to implement the `SaveChanges` endpoint, which the Breeze client
expects to use to save changes:

	@POST
	@Path("SaveChanges")
	public Response saveChanges(String saveBundle) {
		SaveService ss = new SaveService(this.sessionFactory, this.metadata);
		return saveService.saveChanges(saveBundle);
	}

Note that you can have multiple such endpoints, to support [named saves](http://www.breezejs.com/documentation/saving-changes).
	
### MetadataBuilder

The Breeze client requires metadata about the domain model in order to entities manage entities.  The SaveService also requires metadata to convert foreign keys into navigation properties for related entities.  Metadata for both of these purposes is provided by the `MetadataBuilder` class.

The MetadataBuilder uses Hibernate's Metadata API to get information about entity mappings
and relationships.  This information should be consistent whether mapping is done using .hbm.xml files, annotations, or programatically.  

The MetadataBuilder requires the Hibernate SessionFactory and the Configuration.  Depending upon the Hibernate version, the Configuration may be accessible from the SessionFactory itself; then you can use the constructor

	public MetadataBuilder(SessionFactory sessionFactory)

Otherwise, you will need to provide the Configuration:

	public MetadataBuilder(SessionFactory sessionFactory, Configuration configuration)

If you're using [Spring Framework](http://projects.spring.io/spring-framework/), you may need to follow [this advice](http://stackoverflow.com/questions/2736100/how-can-i-get-the-hibernate-configuration-object-from-spring) to get the Configuration.

Building the Metadata is a relatively expensive operation, and metadata doesn't change during the run time of the app.  The result should be cached and used for all subsequent requests.

### JSON Serialization

Currently, breezejs-hibernate performs JSON serialization using an adapted version of [A. Sundararajan's wrapper](https://blogs.oracle.com/sundararajan/entry/a_convention_for_circular_reference) of 
[Douglas Crockford's JSON library](http://www.json.org/java/).  This is because
it needs support for for handing references (circular and otherwise) that are compatible with Breeze (and thus with [Json.NET](http://james.newtonking.com/json/help/index.html?topic=html/PreserveObjectReferences.htm)).

We would like to use a better-supported JSON library, but we haven't found one for Java that supports references in the same manner (and without annotating all of our model 
classes).  Let us know if you find one.
  

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

#### OData Grammar

Currently, only a subset of the [OData protocol specification](http://www.odata.org/documentation/odata-version-3-0/odata-version-3-0-core-protocol/) is handled.

- **$orderby**: supported
- **$top**: supported
- **$skip**: supported
- **$inlinecount**: supported
- **$filter**: supports comparisons (eq, ne, gt, ge, lt, le) but not logicals (and, or, not), arithmetic (add, sub, mul, div, mod), grouping, or functions (substring, endswith, etc.)
- **$select**: not supported
- **$format**: not supported, always returns JSON
- **$links**: not supported
- **$count**: not supported



## Building the Library

The project uses [Maven](http://maven.apache.org/) as its build tool and dependency manager, so you'll need to install Maven.

The dependencies are defined in the `pom.xml` file.  Maven reads this file to determine what other libraries it needs to download
before it can start the build.  Hibernate and Jersey each have their own dependencies, so expect Maven to install many jars before 
actually compiling.  Type 

	mvn package

to compile the sources and build `breezejs-hibernate-{version}.jar`.  

##### Dependency Jars

When you deploy the jar, you will need to deploy all the of the 
Hibernate and Jersey dependencies along with it.  To get all the jars in one handy directory, use

	mvn assembly:single

This will create the directory `breezejs-hibernate/target/breezejs-hibernate-0.1a-lib/lib` which contains all the necessary jars.  You might want to do this so that you can include the jars in the build path for your IDE.

##### JAR With Dependencies

If needed, you can create a single jar that has all of the dependencies included.  To do this, edit the pom.xml file.  **Comment out** the line

	<descriptor>src/main/assembly/lib.xml</descriptor>
	
and **uncomment** the line

	<descriptorRef>jar-with-dependencies</descriptorRef>
	
Then use 

	mvn assembly:single
	
and it will create `breezejs-hibernate/target/breezejs-hibernate-0.1a-jar-with-dependencies.jar` which has all the classes from all the 
jars discussed above.

## Using Eclipse or Other IDE

In order to edit and build using an IDE, you will first need to use Maven to get the jars of all the dependencies, as described under "Dependencies" above.  Then you 
can include the jars in the classpath of the project in your IDE.

For Eclipse, the `.classpath` and `.project` files are provided in the breezejs-hibernate 
directory.  After performing the `mvn assembly:single` command described above, you can open Eclipse and use "import project" to use the existing project definition.

