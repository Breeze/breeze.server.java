package northwind.jpamodel;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class NextId {
    private String name;
    private long nextId;

    @Id
    @Column(length=50)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getNextId() {
		return nextId;
	}
	public void setNextId(long nextId) {
		this.nextId = nextId;
	}

}
