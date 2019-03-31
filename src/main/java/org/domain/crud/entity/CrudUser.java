package org.domain.crud.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;

@IdClass(CrudGroupOwnerNamePK.class)
@Entity
@Table(name = "crud_user")
public class CrudUser {

	/**
	 *
	 */
	@Id
	@Column(name="crud_group_owner")
	private Integer crudGroupOwner;
	@Id
	private String name;
	private String password;
	private String roles;
	@Column(length=10240)
	private String menu;

//	@Column(name = "routes_character_varying", columnDefinition = "character varying")@Convert(converter=org.domain.crud.admin.JsonConverter.class)
//	@Column(name = "routes_jsonb", columnDefinition = "jsonb")@Convert(converter=org.domain.crud.admin.JsonPGobjectConverter.class)
//	private JsonStructure routes;
	@Column(length=10240)
	private String routes;
	@Column(name = "show_system_menu")
	private Boolean showSystemMenu;
	private String path;
	private String ip;
	private String authctoken;

	public CrudUser() {
	}

	public Integer getCrudGroupOwner() {
		return crudGroupOwner;
	}

	public void setCrudGroupOwner(Integer crudGroupOwner) {
		this.crudGroupOwner = crudGroupOwner;
	}

	public String getRoles() {
		return roles;
	}

	public void setRoles(String roles) {
		this.roles = roles;
	}

	public String getMenu() {
		return menu;
	}

	public void setMenu(String menu) {
		this.menu = menu;
	}

	public Boolean getShowSystemMenu() {
		return showSystemMenu;
	}

	public void setShowSystemMenu(Boolean showSystemMenu) {
		this.showSystemMenu = showSystemMenu;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getAuthctoken() {
		return authctoken;
	}

	public void setAuthctoken(String authctoken) {
		this.authctoken = authctoken;
	}

	public String getRoutes() {
		return routes;
	}

	public void setRoutes(String routes) {
		this.routes = routes;
	}


}
