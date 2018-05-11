package org.domain.iso8583router.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "iso8583router_log")
public class ISO8583RouterLog implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4308430862241707265L;
	@Id
	@Column(name="time_id", columnDefinition="bpchar(19)", length=19)
	private String timeId;
	@Column(name="transaction_id")
	private Integer transactionId;
	@NotNull
	@Column(name="log_level")
	private String loglevel;
	@NotNull
	private String modules;
	private String root;
	@NotNull
	private String header;
	@NotNull
	private String message;
	private String transaction;
	
	@Override
	public String toString() {
		return String.format("%s - %10s - %10d - %20s - %10s - %s - %s", timeId, loglevel, transactionId, header, root, modules, message, transaction);
	}
	
	public ISO8583RouterLog() {
		// TODO Auto-generated constructor stub
	}

	public ISO8583RouterLog(String timeStamp, String loglevel, Integer transactionId, String header, String root, String modules, String message, String transaction) {
		this.timeId = timeStamp;
		this.loglevel = loglevel;
		this.transactionId = transactionId;
		this.header = header;
		this.root = root;
		this.modules = modules;
		this.message = message;
		this.transaction = transaction;
	}

	public String getTimeId() {
		return timeId;
	}

	public void setTimeId(String timeId) {
		this.timeId = timeId;
	}

	public String getLoglevel() {
		return loglevel;
	}

	public void setLoglevel(String loglevel) {
		this.loglevel = loglevel;
	}

	public Integer getTransactionId() {
		return transactionId;
	}

	public void setTransactionId(Integer transactionId) {
		this.transactionId = transactionId;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getRoot() {
		return root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	public String getModules() {
		return modules;
	}

	public void setModules(String modules) {
		this.modules = modules;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getTransaction() {
		return transaction;
	}

	public void setTransaction(String transaction) {
		this.transaction = transaction;
	}
	
}
