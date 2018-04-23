package org.domain.crud.entity;

import java.io.Serializable;
import javax.persistence.*;


/**
 * The persistent class for the nfe_st_ipi_enquadramento database table.
 * 
 */
@Entity
@Table(name="nfe_st_ipi_enquadramento")
@NamedQuery(name="NfeStIpiEnquadramento.findAll", query="SELECT n FROM NfeStIpiEnquadramento n")
public class NfeStIpiEnquadramento implements Serializable {
	private static final long serialVersionUID = 1L;
	private Integer id;
	private String name;
	@Column(name="ipi_operacao")
	private Integer ipiOperacao;

	public NfeStIpiEnquadramento() {
	}


	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}


	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}


	public Integer getIpiOperacao() {
		return this.ipiOperacao;
	}

	public void setIpiOperacao(Integer nfeStIpiOperacao) {
		this.ipiOperacao = nfeStIpiOperacao;
	}

}