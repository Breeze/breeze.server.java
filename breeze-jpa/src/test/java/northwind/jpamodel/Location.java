package northwind.jpamodel;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class Location {
    private String address;
    private String city;
    private String region;
    private String postalCode;
    private String country;

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

}
