package org.domain.iso8583router.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

import org.domain.commom.Utils.DataAlign;
import org.domain.commom.Utils;

@Entity
@IdClass(value = Iso8583RouterMessageAdapterItemPK.class)
@Table(name = "iso8583_router_message_adapter_item", schema = "public")
public class Iso8583RouterMessageAdapterItem implements java.io.Serializable {
	private static final long serialVersionUID = -2253404418736276959L;
	@Id
	@Column(name="message_adapter") @NotNull
	private String messageAdapter = "iso8583default"; // iso8583default
	
	@Id
	@Column(name="root_pattern") @NotNull
	private String rootPattern = "\\d\\d\\d\\d";
	
	@Id
	@NotNull
	private String tag;
	
	@Column(name="order_index")
	private Integer orderIndex;
	
	@Column(name="field_name") 
	private String fieldName;
	
	@Column(name="data_type") @NotNull
	private Integer dataType = Utils.DATA_TYPE_DECIMAL | Utils.DATA_TYPE_HEX | Utils.DATA_TYPE_MASK | Utils.DATA_TYPE_ALPHA | Utils.DATA_TYPE_SPECIAL;
	private DataAlign alignment = DataAlign.ZERO_LEFT;

	@Column(name="size_header") @Min(0) @NotNull	
	private Integer sizeHeader = 0;
	@Column(name="min_length") @NotNull
	private Integer minLength = 1;
	@Column(name="max_length") @NotNull
	private Integer maxLength = 2 * 999;
	
	public Iso8583RouterMessageAdapterItem() {
	}
	
	public String getMessageAdapter() {
		return messageAdapter;
	}

	public void setMessageAdapter(String messageAdapter) {
		this.messageAdapter = messageAdapter;
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

	public DataAlign getAlignment() {
		return alignment;
	}

	public void setAlignment(DataAlign alignment) {
		this.alignment = alignment;
	}

	public Integer getOrderIndex() {
		return orderIndex;
	}

	public void setOrderIndex(Integer orderIndex) {
		this.orderIndex = orderIndex;
	}

}
