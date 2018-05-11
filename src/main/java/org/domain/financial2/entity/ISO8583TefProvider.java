package org.domain.financial2.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "iso8583_tef_provider")
public class ISO8583TefProvider implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3375340551982846830L;

	@Id
	@Column(name = "id", unique = true, nullable = false)
	private Integer id;

	private Integer flags;
	
	private String text;

	@Column(name = "auth_call_name")
	private String authCallName;

	@Column(name = "crypt_key_reference")
	private String cryptKeyReference;

	@Column(name = "master_key")
	private String masterKey;

	public ISO8583TefProvider() {
	}

	public ISO8583TefProvider(int id) {
		this.id = id;
	}

	public ISO8583TefProvider(int id, Integer flags, String text) {
		this.id = id;
		this.flags = flags;
		this.text = text;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getFlags() {
		return this.flags;
	}

	public void setFlags(Integer flags) {
		this.flags = flags;
	}

	public String getText() {
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getAuthCallName() {
		return authCallName;
	}

	public void setAuthCallName(String authCallName) {
		this.authCallName = authCallName;
	}

	public String getCryptKeyReference() {
		return cryptKeyReference;
	}

	public void setCryptKeyReference(String cryptKeyReference) {
		this.cryptKeyReference = cryptKeyReference;
	}

	public String getMasterKey() {
		return masterKey;
	}

	public void setMasterKey(String masterKey) {
		this.masterKey = masterKey;
	}

}
