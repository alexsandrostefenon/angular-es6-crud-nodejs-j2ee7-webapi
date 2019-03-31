package org.domain.crud.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@IdClass(CrudGroupUserPK.class)
@Entity
@Table(name = "crud_group_user", uniqueConstraints = @UniqueConstraint(columnNames = {"crud_user", "crud_group"}))
public class CrudGroupUser {
	@Id @Column(name="crud_user")
	private String crudUser;
	@Id @Column(name="crud_group")
	private Integer crudGroup;
	
	public String getCrudUser() {
		return crudUser;
	}
	public void setCrudUser(String crudUser) {
		this.crudUser = crudUser;
	}
	public Integer getCrudGroup() {
		return crudGroup;
	}
	public void setCrudGroup(Integer crudGroup) {
		this.crudGroup = crudGroup;
	}

}
