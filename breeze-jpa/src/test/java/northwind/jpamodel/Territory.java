package northwind.jpamodel;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

@Entity
public class Territory {
    private int territoryID;
    private String territoryDescription;
    private int regionID;
    private int rowVersion;

    private Set<EmployeeTerritory> employeeTerritories;
    private Region region;
    private Set<Employee> employees;

    @Id
	public int getTerritoryID() {
		return territoryID;
	}
	public void setTerritoryID(int territoryID) {
		this.territoryID = territoryID;
	}
	public String getTerritoryDescription() {
		return territoryDescription;
	}
	public void setTerritoryDescription(String territoryDescription) {
		this.territoryDescription = territoryDescription;
	}
    @Column(insertable=false, updatable=false)
	public int getRegionID() {
		return regionID;
	}
	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}
	public int getRowVersion() {
		return rowVersion;
	}
	public void setRowVersion(int rowVersion) {
		this.rowVersion = rowVersion;
	}
	@OneToMany(mappedBy="territory")
	public Set<EmployeeTerritory> getEmployeeTerritories() {
		return employeeTerritories;
	}
	public void setEmployeeTerritories(Set<EmployeeTerritory> employeeTerritories) {
		this.employeeTerritories = employeeTerritories;
	}
    @ManyToOne
    @JoinColumn(name="regionID")
	public Region getRegion() {
		return region;
	}
	public void setRegion(Region region) {
		this.region = region;
	}
	@ManyToMany
	public Set<Employee> getEmployees() {
		return employees;
	}
	public void setEmployees(Set<Employee> employees) {
		this.employees = employees;
	}

}
