package northwind.jpamodel;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Comment implements Serializable {
    private static final long serialVersionUID = 1L;
    private Date createdOn;
    private String comment1;
    private byte seqNum;

    @Id
	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
	    this.createdOn = createdOn;
	}

	public String getComment1() {
	    return comment1;
	}

	public void setComment1(String comment1) {
	    this.comment1 = comment1;
	}
	@Id
	public byte getSeqNum() {
	    return seqNum;
	}

	public void setSeqNum(byte seqNum) {
	    this.seqNum = seqNum;
	}

    public int hashCode()
    {
        if (createdOn == null) return super.hashCode(); //transient instance
        if (seqNum == 0) return super.hashCode(); //transient instance
        return createdOn.hashCode() ^ seqNum;

    }

    public boolean equals(Object obj)
    {
        if (obj == null) return false;
        if (!(obj instanceof Comment)) return false;
        Comment x = (Comment) obj;
        return (createdOn.equals(x.getCreatedOn())) && (seqNum == x.getSeqNum());
    }

}
