package northwind.service;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import northwind.model.Customer;
import northwind.model.Order;

import com.breeze.webtest.AppContextListener;
import com.breezejs.metadata.Metadata;
import com.breezejs.query.QueryResult;
import com.breezejs.save.SaveResult;
import com.breezejs.util.JsonGson;
import com.breezejs.hib.MetadataBuilder;
import com.breezejs.hib.QueryService;
import com.breezejs.hib.SaveService;

/**
 * BreezeTests JAX-RS service returning JSON.
 * @author Steve
 * @see https://jersey.java.net/documentation/latest/jaxrs-resources.html
 */
@Path("breezetests")
@Consumes("application/json")
@Produces("application/json; charset=UTF-8")
public class BreezeTests {
	
	private QueryService queryService;
	private SaveService saveService;
	private Metadata metadata;
	private static String metadataJson; 
	
	/** Create instance using the injected ServletContext */
	public BreezeTests(@Context ServletContext ctx) {
		this ((SessionFactory) ctx.getAttribute(AppContextListener.SESSIONFACTORY),
				(Metadata) ctx.getAttribute(AppContextListener.METADATA));
	}

	/** Create instance using provided sessionFactory and metadata.  This is private, just for testing */
	private BreezeTests(SessionFactory sessionFactory, Metadata metadata) {
		System.out.println("BreezeTests: sessionFactory=" + sessionFactory + ", metadata=" + metadata);
    	this.queryService = new QueryService(sessionFactory);
    	this.saveService = new SaveService(sessionFactory, metadata);
    	this.metadata = metadata;
	}
	
	@GET
	@Path("Metadata")
	public String getMetadata() {
		if (metadataJson == null) {
			metadataJson = JsonGson.toJson(this.metadata, false);
		}
		return metadataJson;
	}
	
	@POST
	@Path("SaveChanges")
	public Response saveChanges(String saveBundle) {
		SaveResult result = saveService.saveChanges(saveBundle);
		return toResponse(result);
	}
	
	Response toResponse(SaveResult result) {
		String json = JsonGson.toJson(result);
		Response response;
		if (result.hasErrors()) {
			response = Response.status(Response.Status.FORBIDDEN).entity(json).build(); 
		} else {
			response = Response.ok(json).build();
		}
		return response;
	}
		
	@GET
	@Path("Customers")
	public String getCustomers(@Context HttpServletRequest request) {
		QueryResult result = queryService.executeQuery(Customer.class, request.getQueryString());
		return result.toJson();
	}

	@GET
	@Path("Orders")
	public String getOrders(@Context HttpServletRequest request) {
		QueryResult result = queryService.executeQuery(Order.class, request.getQueryString());
		return result.toJson();
	}	  
	  
	/**
	 * Just for testing
	 * @param args
	 */
	public static void main(String[] args)
	{
		 // configures settings from hibernate.cfg.xml
		Configuration configuration = new Configuration();
		SessionFactory sessionFactory = configuration.configure().buildSessionFactory();
		
		// builds metadata from the Hibernate mappings
		MetadataBuilder metaGen = new MetadataBuilder(sessionFactory, configuration);
		Metadata metadata = metaGen.buildMetadata();
		
		BreezeTests nb = new BreezeTests(sessionFactory, metadata);
		String meta = nb.getMetadata();
		System.out.println(meta);
		System.exit(0);
	}
}
