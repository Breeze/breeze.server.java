package northwind.jpamodel;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Version;

@Entity
public class Product {
    private int productID;
    private String productName;
    private Integer supplierID;
    private Integer categoryID;
    private String quantityPerUnit;
    private BigDecimal unitPrice;
    private Short unitsInStock;
    private Short unitsOnOrder;
    private Short reorderLevel;
    private boolean isDiscontinued;
    private Date discontinuedDate;
    private int rowVersion;

    private Category category;
    private Supplier supplier;

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	public int getProductID() {
		return productID;
	}
	public void setProductID(int productID) {
		this.productID = productID;
	}
    @Column(length=40, nullable=false)
	public String getProductName() {
		return productName;
	}
	public void setProductName(String productName) {
		this.productName = productName;
	}
    @Column(insertable=false, updatable=false)
	public Integer getSupplierID() {
		return supplierID;
	}
	public void setSupplierID(Integer supplierID) {
		this.supplierID = supplierID;
	}
    @Column(insertable=false, updatable=false)
	public Integer getCategoryID() {
		return categoryID;
	}
	public void setCategoryID(Integer categoryID) {
		this.categoryID = categoryID;
	}
    @Column(length=20)
	public String getQuantityPerUnit() {
		return quantityPerUnit;
	}
	public void setQuantityPerUnit(String quantityPerUnit) {
		this.quantityPerUnit = quantityPerUnit;
	}
	public BigDecimal getUnitPrice() {
		return unitPrice;
	}
	public void setUnitPrice(BigDecimal unitPrice) {
		this.unitPrice = unitPrice;
	}
	public Short getUnitsInStock() {
		return unitsInStock;
	}
	public void setUnitsInStock(Short unitsInStock) {
		this.unitsInStock = unitsInStock;
	}
	public Short getUnitsOnOrder() {
		return unitsOnOrder;
	}
	public void setUnitsOnOrder(Short unitsOnOrder) {
		this.unitsOnOrder = unitsOnOrder;
	}
	public Short getReorderLevel() {
		return reorderLevel;
	}
	public void setReorderLevel(Short reorderLevel) {
		this.reorderLevel = reorderLevel;
	}
	public boolean getIsDiscontinued() {
	    return isDiscontinued;
	}
	public void setIsDiscontinued(boolean discontinued) {
	    this.isDiscontinued = discontinued;
	}
	public Date getDiscontinuedDate() {
        return discontinuedDate;
    }
    public void setDiscontinuedDate(Date discontinuedDate) {
        this.discontinuedDate = discontinuedDate;
    }

    @Version
	public int getRowVersion() {
		return rowVersion;
	}
	public void setRowVersion(int rowVersion) {
		this.rowVersion = rowVersion;
	}
	@ManyToOne
	@JoinColumn(name="categoryID")
	public Category getCategory() {
		return category;
	}
	public void setCategory(Category category) {
		this.category = category;
	}
	@ManyToOne
    @JoinColumn(name="supplierID")
	public Supplier getSupplier() {
		return supplier;
	}
	public void setSupplier(Supplier supplier) {
		this.supplier = supplier;
	}

}
