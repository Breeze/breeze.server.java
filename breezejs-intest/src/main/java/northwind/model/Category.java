package northwind.model;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.Set;

import org.hibernate.Hibernate;

public class Category {
    private int categoryID;
    private String categoryName;
    private String description;
    private byte[] picture;
    private int rowVersion;

    private Set<Product> products;

	public int getCategoryID() {
		return categoryID;
	}

	public void setCategoryID(int categoryID) {
		this.categoryID = categoryID;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public byte[] getPicture() {
		return picture;
	}

	public void setPicture(byte[] picture) {
		this.picture = picture;
	}
	
//    /** Don't invoke this.  Used by Hibernate only. */ 
//	public void setPictureBlob(Blob picture) { 
//		int blobLength;
//		try {
//			blobLength = (int) picture.length();
//			byte[] blobAsBytes = picture.getBytes(1, blobLength);
//	        this.picture = blobAsBytes; 
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}  
//	}
//	  
//	/** Don't invoke this.  Used by Hibernate only. */ 
//    public Blob getPictureBlob() {  
//    	Hibernate.getLobCreator(sessionfactory.getCurrentSession()).createBlob();
//	} 

	public int getRowVersion() {
		return rowVersion;
	}

	public void setRowVersion(int rowVersion) {
		this.rowVersion = rowVersion;
	}

	public Set<Product> getProducts() {
		return products;
	}

	public void setProducts(Set<Product> products) {
		this.products = products;
	}

}
