package org.domain.crud.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="crud_group")
public class CrudGroup {
	@Id
	@SequenceGenerator(name="crud_group_id_seq", sequenceName="crud_group_id_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="crud_group_id_seq")
	@Column(columnDefinition="serial")
	private Integer id;
	@Column(name = "name", length = 32, unique = true, nullable = false)
	private String name;

	public CrudGroup() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
