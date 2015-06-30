package northwind.jpamodel;

import java.util.Date;

import javax.persistence.Entity;

@Entity
public class DiscontinuedProduct extends Product {
    private Date discontinuedDate;

	public Date getDiscontinuedDate() {
		return discontinuedDate;
	}
	public void setDiscontinuedDate(Date discontinuedDate) {
		this.discontinuedDate = discontinuedDate;
	}

}
