package northwind.jpamodel;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Version;

@Entity
public class InternationalOrder {
    private int orderID;
    private String customsDescription;
    private BigDecimal exciseTax;
    private int rowVersion;

    private Order order;

    @Id
	public int getOrderID() {
		return orderID;
	}

	public void setOrderID(int orderID) {
		this.orderID = orderID;
	}

    @Column(length=100, nullable=false)
	public String getCustomsDescription() {
		return customsDescription;
	}

	public void setCustomsDescription(String customsDescription) {
		this.customsDescription = customsDescription;
	}

    @Column(nullable=false)
	public BigDecimal getExciseTax() {
		return exciseTax;
	}

	public void setExciseTax(BigDecimal exciseTax) {
		this.exciseTax = exciseTax;
	}

    @Version
	public int getRowVersion() {
		return rowVersion;
	}

	public void setRowVersion(int rowVersion) {
		this.rowVersion = rowVersion;
	}

	@OneToOne(optional=false)
	@JoinColumn(name="orderID", insertable=false, updatable=false)
	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

}
