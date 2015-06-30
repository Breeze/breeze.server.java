package northwind.jpamodel;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Region {
    private int regionID;
    private String regionDescription;
    private int rowVersion;

    private Set<Territory> territories;

    @Id
	public int getRegionID() {
		return regionID;
	}

	public void setRegionID(int regionID) {
		this.regionID = regionID;
	}

	public String getRegionDescription() {
		return regionDescription;
	}

	public void setRegionDescription(String regionDescription) {
		this.regionDescription = regionDescription;
	}

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
