package org.domain.erp.entity;

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
	private String number;
	private String manufacturer;
	private Integer product;

	public Barcode() {
	}


	@Id
	public String getNumber() {
		return this.number;
	}

	public void setNumber(String number) {
		this.number = number;
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