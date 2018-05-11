package org.domain.crud.entity;

import javax.json.JsonObject;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "crud_service")
@XmlRootElement
public class CrudService implements java.io.Serializable {
	// path, isOnLine, fields, filterFields, objDefault
	// service.name, service.isOnLine, service.fields, service.filterFields, service.objDefault

	/**
	 *
	 */
	private static final long serialVersionUID = -1919519756231188092L;

	@Id
	@SequenceGenerator(name="crud_service_id_seq", sequenceName="crud_service_id_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="crud_service_id_seq")
	@Column(columnDefinition="serial")
	private Integer id;

	@Column(unique=true)
	private String name;// path

	private String menu;

	private String template;

	@Column(length=10240)
	private String fields;

	@Column(length=10240,name="filter_fields")
	private String filterFields;

	@Column(length=512,name="order_by")
	private String orderBy;

	@Column(name="is_on_line")
	private Boolean isOnLine;

	@Column(name = "save_and_exit")
	private Boolean saveAndExit = true;

	private String title;

	@Transient
	private JsonObject jsonFields;
	
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public CrudService() {
	}

	public CrudService(int id) {
		this.id = id;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getFields() {
		return fields;
	}

	public void setFields(String fields) {
		this.fields = fields;
	}

	public String getFilterFields() {
		return filterFields;
	}

	public void setFilterFields(String filterFields) {
		this.filterFields = filterFields;
	}

	public Boolean getIsOnLine() {
		return isOnLine;
	}

	public void setIsOnLine(Boolean isOnLine) {
		this.isOnLine = isOnLine;
	}

	public String getMenu() {
		return menu;
	}

	public void setMenu(String menu) {
		this.menu = menu;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Boolean getSaveAndExit() {
		return saveAndExit;
	}

	public void setSaveAndExit(Boolean saveAndExit) {
		this.saveAndExit = saveAndExit;
	}

	public String getTemplate() {
		return template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public void setJsonFields(JsonObject jsonFields) {
		this.jsonFields = jsonFields;
	}

}
