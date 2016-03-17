package northwind.jpamodel;

import java.util.Collection;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Version;

@Entity
public class Customer {

	private String customerID;
	private String customerID_OLD;
	private String companyName;
	private String contactName;
	private String contactTitle;
	private String address;
	private String city;
	private String region;
	private String postalCode;
	private String country;
	private String phone;
	private String fax;
	private Integer rowVersion;

	private Collection<Order> orders;

	@Id
    @GeneratedValue(strategy=GenerationType.AUTO)
	public String getCustomerID() {
		return customerID;
	}

	public void setCustomerID(String customerID) {
		this.customerID = customerID;
	}

    @Column(length=5)
	public String getCustomerID_OLD() {
		return customerID_OLD;
	}

	public void setCustomerID_OLD(String customerID_OLD) {
		this.customerID_OLD = customerID_OLD;
	}

    @Column(length=40, nullable=false)
	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

    @Column(length=30)
	public String getContactName() {
		return contactName;
	}

	public void setContactName(String contactName) {
		this.contactName = contactName;
	}

    @Column(length=30)
	public String getContactTitle() {
		return contactTitle;
	}

	public void setContactTitle(String contactTitle) {
		this.contactTitle = contactTitle;
	}

    @Column(length=60)
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

    @Column(length=15)
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

    @Column(length=15)
	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

    @Column(length=10)
	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

    @Column(length=15)
	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

    @Column(length=24)
	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

    @Column(length=24)
	public String getFax() {
		return fax;
	}

	public void setFax(String fax) {
		this.fax = fax;
	}

    @Version
	public Integer getRowVersion() {
		return rowVersion;
	}

	public void setRowVersion(Integer rowVersion) {
		this.rowVersion = rowVersion;
	}

	@OneToMany(mappedBy="customer")	
	public Collection<Order> getOrders() {
		return orders;
	}

	public void setOrders(Collection<Order> orders) {
		this.orders = orders;
	}

}
