package northwind.jpamodel;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

@Entity
public class Region {
    private int regionID;
    private String regionDescription;
    private int rowVersion;

    private Set<Territory> territories;

    @Id
    @TableGenerator(name="TABLE_GEN", table="NextId", pkColumnName="Name",
    valueColumnName="NextId", pkColumnValue="GLOBAL")
    @GeneratedValue(strategy=GenerationType.TABLE, generator="TABLE_GEN")	
    public int getRegionID() {
		return regionID;
	}

	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}
	@Column(length=50, nullable=false)
	public String getRegionDescription() {
		return regionDescription;
	}

	public void setRegionDescription(String regionDescription) {
		this.regionDescription = regionDescription;
	}

    @Version
	public int getRowVersion() {
		return rowVersion;
	}

	public void setRowVersion(int rowVersion) {
		this.rowVersion = rowVersion;
	}
	@OneToMany(mappedBy="region")
	public Set<Territory> getTerritories() {
		return territories;
	}

	public void setTerritories(Set<Territory> territories) {
		this.territories = territories;
	}

}
