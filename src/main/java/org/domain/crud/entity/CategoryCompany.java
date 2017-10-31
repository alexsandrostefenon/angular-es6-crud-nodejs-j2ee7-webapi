package org.domain.crud.entity;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@IdClass(CompanyIdPK.class)
@Entity
@Table(name = "category_company", uniqueConstraints = @UniqueConstraint(columnNames = {"company", "category"}))
public class CategoryCompany {
	@Id private Integer company;
	@Id private Integer id;
	private Integer category;

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getCompany() {
		return company;
	}

	public void setCompany(Integer company) {
		this.company = company;
	}

	public Integer getCategory() {
		return category;
	}

	public void setCategory(Integer category) {
		this.category = category;
	}

}
