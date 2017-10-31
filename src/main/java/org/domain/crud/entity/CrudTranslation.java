package org.domain.crud.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "crud_translation")
@XmlRootElement
public class CrudTranslation implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1919519756231188092L;
	private Integer id;
	private String name;
	private String locale;
	private String translation;

	public CrudTranslation() {
	}

	public CrudTranslation(int id) {
		this.id = id;
	}

	@Column(nullable = false)
	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getTranslation() {
		return translation;
	}

	public void setTranslation(String translation) {
		this.translation = translation;
	}

	@Column(nullable = false)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Id
	@Column(name = "id", unique = true, nullable = false)
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

}
