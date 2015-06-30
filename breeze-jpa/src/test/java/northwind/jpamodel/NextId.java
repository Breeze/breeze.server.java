package northwind.jpamodel;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class NextId {
    private String name;
    private long nextId1;

    @Id
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getNextId1() {
		return nextId1;
	}
	public void setNextId1(long nextId1) {
		this.nextId1 = nextId1;
	}

}
