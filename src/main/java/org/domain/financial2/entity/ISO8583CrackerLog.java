package org.domain.financial2.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.Size;

@Entity
@Table(name = "iso8583_cracker_log", schema = "public")
public class ISO8583CrackerLog implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6552305915855815264L;
	@Id
	@Column(name = "id", unique = true, nullable = false)
	private Integer id;
	@Column(name = "log_level")
	private String logLevel;
	@Column(name = "header", length = 512)
	@Size(max = 512)
	private String header;
	@Column(name = "module", length = 128)
	@Size(max = 128)
	private String module;
	@Column(name = "payload_type")
	private String payloadType;
	private String root;
	@Column(name = "message", length = 40960)
	@Size(max = 40960)
	private String message;
	@Column(name = "line_original", length = 40960)
	@Size(max = 40960)
	private String lineOriginal;
	@Column(name = "line_remind", length = 40960)
	@Size(max = 40960)
	private String lineRemind;
	
	@Override
	public String toString() {
		return String.format("%30s - %30s - %30s - %30s - %30s - %30s - %30s - %30s\n",
														this.logLevel, this.header, this.module, this.payloadType, this.root,
														message.substring(0, message.length() > 30 ? 30 : message.length()),
														lineOriginal.substring(0, message.length() > 30 ? 30 : message.length()),
														lineRemind.substring(0, message.length() > 30 ? 30 : message.length()));
	}

	public ISO8583CrackerLog() {
	}

	public ISO8583CrackerLog(int id) {
		this.id = id;
	}

	public ISO8583CrackerLog(int id, String header, String lineoriginal, String lineremind,
			String loglevel, String message, String module, String payloadtype,
			String root) {
		this.id = id;
		this.header = header;
		this.lineOriginal = lineoriginal;
		this.lineRemind = lineremind;
		this.logLevel = loglevel;
		this.message = message;
		this.module = module;
		this.payloadType = payloadtype;
		this.root = root;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getHeader() {
		return this.header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getLineoriginal() {
		return this.lineOriginal;
	}

	public void setLineoriginal(String lineoriginal) {
		this.lineOriginal = lineoriginal;
	}

	public String getLineremind() {
		return this.lineRemind;
	}

	public void setLineremind(String lineremind) {
		this.lineRemind = lineremind;
	}

	public String getLoglevel() {
		return this.logLevel;
	}

	public void setLoglevel(String loglevel) {
		this.logLevel = loglevel;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getModule() {
		return this.module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	public String getPayloadtype() {
		return this.payloadType;
	}

	public void setPayloadtype(String payloadtype) {
		this.payloadType = payloadtype;
	}

	public String getRoot() {
		return this.root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

}
