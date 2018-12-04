package org.domain.iso8583router.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.domain.commom.Utils.DataAlign;
import org.domain.commom.Utils;

@Entity
@Table(name = "iso8583_router_message_adapter_item", schema = "public")
public class Iso8583RouterMessageAdapterItem implements java.io.Serializable {
	private static final long serialVersionUID = -2253404418736276959L;
	@Id
	private Integer id;
	@Column(name="message_adapter") @NotNull
	private String messageAdapter = "iso8583default"; // iso8583default
	private DataAlign alignment = DataAlign.ZERO_LEFT;
	@Column(name="data_type") @NotNull
	private Integer dataType = Utils.DATA_TYPE_DECIMAL | Utils.DATA_TYPE_ALPHA | Utils.DATA_TYPE_SPECIAL;
	@Column(name="data_format")
	private String dataFormat;
	@Column(name="min_length") @NotNull
	private Integer minLength = 1;
	@Column(name="max_length") @NotNull
	private Integer maxLength = 2 * 999;
	@Column(name="field_name") 
	private String fieldName;
	@NotNull
	private String tag;
	@Column(name="size_header") @Min(0) @NotNull	
	private Integer sizeHeader = 0;
	@Column(name="root_pattern") @NotNull
	private String rootPattern = "\\d\\d\\d\\d";
	
	public Iso8583RouterMessageAdapterItem(Integer id, String fieldName, String tag, Integer minLength, Integer maxLength,
			Integer sizeHeader, String rootPattern, String messageAdapterConfName, Integer dataType) {
		this.id = id;
		this.fieldName = fieldName;
		this.tag = tag;
		this.minLength = minLength;
		this.maxLength = maxLength;
		this.sizeHeader = sizeHeader;
		this.rootPattern = rootPattern;
		this.messageAdapter = messageAdapterConfName;
		this.dataType = dataType;
	}

	public Iso8583RouterMessageAdapterItem() {
	}
	
	public String getMessageAdapterConfName() {
		return messageAdapter;
	}

	public void setMessageAdapterConfName(String messageAdapterConfName) {
		this.messageAdapter = messageAdapterConfName;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getRootPattern() {
		return rootPattern;
	}

	public void setRootPattern(String rootPattern) {
		this.rootPattern = rootPattern;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Integer getMinLength() {
		return minLength;
	}

	public void setMinLength(Integer minLength) {
		this.minLength = minLength;
	}

	public Integer getMaxLength() {
		return maxLength;
	}

	public void setMaxLength(Integer maxLength) {
		this.maxLength = maxLength;
	}

	public Integer getSizeHeader() {
		return sizeHeader;
	}

	public void setSizeHeader(Integer sizeHeader) {
		this.sizeHeader = sizeHeader;
	}

	public Integer getDataType() {
		return dataType;
	}

	public void setDataType(Integer dataType) {
		this.dataType = dataType;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getDataFormat() {
		return dataFormat;
	}

	public void setDataFormat(String dataFormat) {
		this.dataFormat = dataFormat;
	}

	public DataAlign getAlignment() {
		return alignment;
	}

	public void setAlignment(DataAlign alignment) {
		this.alignment = alignment;
	}

}
