package northwind.jpamodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

@Entity
public class EmployeeTerritory {
    private int id;
    private int employeeID;
    private int territoryID;
    private int rowVersion;

    private Employee employee;
    private Territory territory;

    @Id
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
    @Column(insertable=false, updatable=false)
	public int getEmployeeID() {
		return employeeID;
	}
	public void setEmployeeID(int employeeID) {
		this.employeeID = employeeID;
	}
    @Column(insertable=false, updatable=false)
	public int getTerritoryID() {
		return territoryID;
	}
	public void setTerritoryID(int territoryID) {
		this.territoryID = territoryID;
	}
	public int getRowVersion() {
		return rowVersion;
	}
	public void setRowVersion(int rowVersion) {
		this.rowVersion = rowVersion;
	}
	@ManyToOne
	@JoinColumn(name="employeeID")
	public Employee getEmployee() {
		return employee;
	}
	public void setEmployee(Employee employee) {
		this.employee = employee;
	}
    @ManyToOne
    @JoinColumn(name="territoryId")
	public Territory getTerritory() {
		return territory;
	}
	public void setTerritory(Territory territory) {
		this.territory = territory;
	}

}
