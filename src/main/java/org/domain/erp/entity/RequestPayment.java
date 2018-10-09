package org.domain.erp.entity;

import java.math.BigDecimal;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.domain.crud.entity.CompanyIdPK;

@IdClass(CompanyIdPK.class)
@Entity
@Table(name = "request_payment", uniqueConstraints = @UniqueConstraint(columnNames = {"request", "number"}))
public class RequestPayment implements java.io.Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = -8921880901269127764L;
	@Id private Integer company;
	private Integer request;
	@Id
	@SequenceGenerator(name="request_payment_id_seq", sequenceName="request_payment_id_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="request_payment_id_seq")
	@Column(columnDefinition="serial")
	private Integer id;
	private Integer type;
	@Column(precision = 9, scale = 3)
	private BigDecimal value;
	private Integer account;
	@Column(length = 16)
	private String number;
	@Column(name = "due_date", length = 29)@Temporal(TemporalType.TIMESTAMP)
	private Date dueDate;
	@Column(name = "payday", length = 29)@Temporal(TemporalType.TIMESTAMP)
	private Date payday;
	@Column(precision = 9, scale = 3)
	private BigDecimal balance = new BigDecimal(0.0);

	public Integer getCompany() {
		return company;
	}

	public void setCompany(Integer company) {
		this.company = company;
	}

	public RequestPayment() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getAccount() {
		return this.account;
	}

	public void setAccount(Integer account) {
		this.account = account;
	}

	public Integer getRequest() {
		return this.request;
	}

	public void setRequest(Integer request) {
		this.request = request;
	}

	public String getNumber() {
		return this.number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public Integer getType() {
		return this.type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public BigDecimal getValue() {
		return this.value;
	}

	public void setValue(BigDecimal value) {
		this.value = value;
	}

	public Date getDueDate() {
		return this.dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public Date getPayday() {
		return this.payday;
	}

	public void setPayday(Date payday) {
		this.payday = payday;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

}
