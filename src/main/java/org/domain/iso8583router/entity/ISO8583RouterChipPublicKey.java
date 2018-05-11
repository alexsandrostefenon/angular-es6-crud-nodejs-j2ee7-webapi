package org.domain.iso8583router.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@Entity
@Table(name = "iso8583router_chip_public_key")
public class ISO8583RouterChipPublicKey implements java.io.Serializable {
	private static final long serialVersionUID = 7948763868785118303L;

	@Column(name = "registered_application_provider_identifier")
	@Size(min = 10, max = 10)
	String registeredApplicationProviderIdentifier;

	@Column(name = "public_key_index_tag_9f22")
	@Size(min = 2, max = 2)
	String publicKeyIndexTag9F22;
	// pode ser 1 ou 3
	@Column(name = "exp_size")
	@Min(value = 1)
	@Max(value = 3)
	Integer expSize;

	@Column(name = "public_key_check_exponent_tag_9f2e")
	@Size(min = 6, max = 6)
	String publicKeyExponentTag9F2E;

	@Column(length = 496, name = "public_key_modulus_tag_9f2d")
	String publicKeyModulusTag9F2D;

	@Id
	@Column(name = "public_key_check_sum")
	@Size(min = 40, max = 40)
	String publicKeyCheckSum;

	@Column(name = "hash_status")
	private Integer hashStatus;

	public ISO8583RouterChipPublicKey() {
		this.publicKeyIndexTag9F22 = "00";
		this.expSize = 0;
		this.publicKeyExponentTag9F2E = "000000";
		this.publicKeyModulusTag9F2D = "";
		this.publicKeyCheckSum = "";
	}

	public String getRegisteredApplicationProviderIdentifier() {
		return registeredApplicationProviderIdentifier;
	}

	public void setRegisteredApplicationProviderIdentifier(
			String registeredApplicationProviderIdentifier) {
		this.registeredApplicationProviderIdentifier = registeredApplicationProviderIdentifier;
	}

	public String getPublicKeyIndexTag9F22() {
		return publicKeyIndexTag9F22;
	}

	public void setPublicKeyIndexTag9F22(String publicKeyIndexTag9F22) {
		this.publicKeyIndexTag9F22 = publicKeyIndexTag9F22;
	}

	public Integer getExpSize() {
		return expSize;
	}

	public void setExpSize(Integer expSize) {
		this.expSize = expSize;
	}

	public String getPublicKeyExponentTag9F2E() {
		return publicKeyExponentTag9F2E;
	}

	public void setPublicKeyExponentTag9F2E(String publicKeyExponentTag9F2E) {
		this.publicKeyExponentTag9F2E = publicKeyExponentTag9F2E;
	}

	public String getPublicKeyModulusTag9F2D() {
		return publicKeyModulusTag9F2D;
	}

	public void setPublicKeyModulusTag9F2D(String publicKeyModulusTag9F2D) {
		this.publicKeyModulusTag9F2D = publicKeyModulusTag9F2D;
	}

	public String getPublicKeyCheckSum() {
		return publicKeyCheckSum;
	}

	public void setPublicKeyCheckSum(String publicKeyCheckSum) {
		this.publicKeyCheckSum = publicKeyCheckSum;
	}

	public Integer getHashStatus() {
		return hashStatus;
	}

	public void setHashStatus(Integer hashStatus) {
		this.hashStatus = hashStatus;
	}

}
