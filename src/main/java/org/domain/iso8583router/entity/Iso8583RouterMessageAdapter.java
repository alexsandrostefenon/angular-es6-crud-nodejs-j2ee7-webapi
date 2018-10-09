package org.domain.iso8583router.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "iso8583_router_message_adapter")
public class Iso8583RouterMessageAdapter implements java.io.Serializable {
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
	private List<Iso8583RouterMessageAdapterItem> items = null;
	
	public Iso8583RouterMessageAdapter(String name, String parent) {
		super();
		this.name = name;
		this.parent = parent;
	}
	
	public Iso8583RouterMessageAdapter() {
		// TODO Auto-generated constructor stub
	}
	
	public List<Iso8583RouterMessageAdapterItem> getItems() {
		return items;
	}
	
	public void setItems(List<Iso8583RouterMessageAdapterItem> items) {
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
