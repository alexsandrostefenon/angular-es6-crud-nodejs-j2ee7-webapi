package org.domain.iso8583router.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "iso8583router_message_adapter")
public class ISO8583RouterMessageAdapterConf implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5585520573142916221L;
	
	@Id
	private String name;
	private String parent = "iso8583default";
	
	@NotNull
	@Column(name="adapter_class")
	private String adapterClass = "org.domain.financial.messages.MessageAdapterISO8583";

	private Boolean compress = false;
	
	@Column(name="tag_prefix")
	private String tagPrefix = null;
	
	@Transient
	private List<ISO8583RouterMessageAdapterConfItem> items = null;
	
	public ISO8583RouterMessageAdapterConf(String name, String parent) {
		super();
		this.name = name;
		this.parent = parent;
	}
	
	public ISO8583RouterMessageAdapterConf() {
		// TODO Auto-generated constructor stub
	}
	
	public List<ISO8583RouterMessageAdapterConfItem> getItems() {
		return items;
	}
	
	public void setItems(List<ISO8583RouterMessageAdapterConfItem> items) {
		this.items = items;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getParent() {
		return parent;
	}
	public void setParent(String parent) {
		this.parent = parent;
	}
	public String getAdapterClass() {
		return adapterClass;
	}
	public void setAdapterClass(String adapter) {
		this.adapterClass = adapter;
	}
	public String getTagPrefix() {
		return tagPrefix;
	}
	public void setTagPrefix(String tagPrefix) {
		this.tagPrefix = tagPrefix;
	}
	public Boolean getCompress() {
		return compress;
	}
	public void setCompress(Boolean compress) {
		this.compress = compress;
	}

}
