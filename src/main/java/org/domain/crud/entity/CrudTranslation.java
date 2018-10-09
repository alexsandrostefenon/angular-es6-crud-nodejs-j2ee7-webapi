package org.domain.crud.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
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
	@Id
	@SequenceGenerator(name="crud_translation_id_seq", sequenceName="crud_translation_id_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="crud_translation_id_seq")
	@Column(columnDefinition="serial")
	private Integer id;
	@Column(nullable = false)
	private String name;
	@Column(nullable = false)
	private String locale;
	private String translation;

	public CrudTranslation() {
	}

	public CrudTranslation(int id) {
		this.id = id;
	}

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

}
