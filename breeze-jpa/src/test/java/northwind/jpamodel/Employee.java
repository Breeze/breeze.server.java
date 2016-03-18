package northwind.jpamodel;
import java.util.Set;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Version;

@Entity
public class Employee {
    private Integer employeeID;
    private String lastName;
    private String firstName;
    private String title;
    private String titleOfCourtesy;
    private Date birthDate;
    private Date hireDate;
    private String address;
    private String city;
    private String region;
    private String postalCode;
    private String country;
    private String homePhone;
    private String extension;
    private byte[] photo;
    private String notes;
    private String photoPath;
    private Integer reportsToEmployeeID;
    private int rowVersion;
    private String fullName;

    private Set<Employee> directReports;
    private Employee manager;
    private Set<EmployeeTerritory> employeeTerritories;
    private Set<Order> orders;
//    private Set<Territory> territories;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	public Integer getEmployeeID() {
		return employeeID;
	}
	public void setEmployeeID(Integer employeeID) {
		this.employeeID = employeeID;
	}
    @Column(length=30, nullable=false)
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
    @Column(length=30, nullable=false)
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
    @Column(length=30)
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
    @Column(length=25)
	public String getTitleOfCourtesy() {
		return titleOfCourtesy;
	}
	public void setTitleOfCourtesy(String titleOfCourtesy) {
		this.titleOfCourtesy = titleOfCourtesy;
	}
	public Date getBirthDate() {
		return birthDate;
	}
	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}
	public Date getHireDate() {
		return hireDate;
	}
	public void setHireDate(Date hireDate) {
		this.hireDate = hireDate;
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
	public String getHomePhone() {
		return homePhone;
	}
	public void setHomePhone(String homePhone) {
		this.homePhone = homePhone;
	}
    @Column(length=4)
	public String getExtension() {
		return extension;
	}
	public void setExtension(String extension) {
		this.extension = extension;
	}
	public byte[] getPhoto() {
		return photo;
	}
	public void setPhoto(byte[] photo) {
		this.photo = photo;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
    @Column(length=255)
	public String getPhotoPath() {
		return photoPath;
	}
	public void setPhotoPath(String photoPath) {
		this.photoPath = photoPath;
	}
	@Column(insertable=false, updatable=false)
	public Integer getReportsToEmployeeID() {
		return reportsToEmployeeID;
	}
	public void setReportsToEmployeeID(Integer reportsToEmployeeID) {
		this.reportsToEmployeeID = reportsToEmployeeID;
	}
    @Version
	public int getRowVersion() {
		return rowVersion;
	}
	public void setRowVersion(int rowVersion) {
		this.rowVersion = rowVersion;
	}
    @Column(nullable=false)
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
    @OneToMany(mappedBy="manager")
	public Set<Employee> getDirectReports() {
		return directReports;
	}
	public void setDirectReports(Set<Employee> directReports) {
		this.directReports = directReports;
	}
    @ManyToOne
    @JoinColumn(name="reportsToEmployeeID")
	public Employee getManager() {
		return manager;
	}
	public void setManager(Employee manager) {
		this.manager = manager;
	}
    @OneToMany(mappedBy="employee")
	public Set<EmployeeTerritory> getEmployeeTerritories() {
		return employeeTerritories;
	}
	public void setEmployeeTerritories(Set<EmployeeTerritory> employeeTerritories) {
		this.employeeTerritories = employeeTerritories;
	}
	@OneToMany(mappedBy="employee")
	public Set<Order> getOrders() {
		return orders;
	}
	public void setOrders(Set<Order> orders) {
		this.orders = orders;
	}
//    @ManyToMany
//	public Set<Territory> getTerritories() {
//		return territories;
//	}
//	public void setTerritories(Set<Territory> territories) {
//		this.territories = territories;
//	}

}
