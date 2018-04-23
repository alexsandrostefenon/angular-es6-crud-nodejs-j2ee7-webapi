package org.domain.crud.entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the barcode database table.
 * 
 */
@Entity
@NamedQuery(name="Barcode.findAll", query="SELECT b FROM Barcode b")
public class Barcode implements Serializable {
	private static final long serialVersionUID = 1L;
	private String barcode;
	private String manufacturer;
	private Integer product;

	public Barcode() {
	}


	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public String getBarcode() {
		return this.barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}


	public String getManufacturer() {
		return this.manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}


	public Integer getProduct() {
		return this.product;
	}

	public void setProduct(Integer product) {
		this.product = product;
	}

}