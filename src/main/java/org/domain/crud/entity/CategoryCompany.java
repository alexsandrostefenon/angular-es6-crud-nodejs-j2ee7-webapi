package org.domain.crud.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@IdClass(CompanyIdPK.class)
@Entity
@Table(name = "category_company", uniqueConstraints = @UniqueConstraint(columnNames = {"company", "category"}))
public class CategoryCompany {
	@Id private Integer company;
	@Id
	@SequenceGenerator(name="category_company_id_seq", sequenceName="category_company_id_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="category_company_id_seq")
	@Column(columnDefinition="serial")
	private Integer id;
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
