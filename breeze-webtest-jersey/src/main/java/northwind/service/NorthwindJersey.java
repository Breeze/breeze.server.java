package northwind.service;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import northwind.model.Customer;

import com.breeze.jersey.BreezeEntityService;
import com.breeze.query.EntityQuery;

@Path("northwind")
@Consumes("application/json")
@Produces("application/json; charset=UTF-8")
public class NorthwindJersey extends BreezeEntityService {

    /** Create instance using the injected ServletContext */
    public NorthwindJersey(@Context ServletContext ctx) {
        super(ctx);
    }

    @Override
    @GET
    @Path("CustomersInBrazil")
    public String customersInBrazil(@Context UriInfo uriInfo) {
        EntityQuery entityQuery = extractEntityQuery(uriInfo);
        entityQuery = entityQuery.where("{ country: 'Brazil' }").take(5);
        return executeQuery(Customer.class, entityQuery);
    }

}
