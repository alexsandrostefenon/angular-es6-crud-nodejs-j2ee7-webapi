package org.domain.iso8583router.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.domain.commom.Utils;
import org.domain.iso8583router.messages.comm.Comm;

@Entity
@Table(name = "iso8583router_comm", schema = "public")
public class ISO8583RouterCommConf implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6552305915855815264L;

	private String session; // capture (tef,pos,atm (CAIXA24H, SaquePague, etc...)), acquier (in,out (CIELO, REDECARD)), emissor (provider (MASTERCARD, VISA, VIVO, TIM, OI, CLARO, etc...))
	@Id
	private String name; // TEF, HSM, ETH_V2
	private Boolean enabled = true; // false, true
	// Server or Client
	private Boolean listen = false; // CLIENT = 0, SERVER = 1
	private String ip = "127.0.0.1";
	private Integer port = 3001; // 1100, 7000, 24000
	// if Persistent, don't close connection
	// PERMANENT, TEMPORARY
	private Boolean permanent = true; // TEMPORARY
	@Column(name="size_ascii")
	private Boolean sizeAscii = true;
	private String adapter = "org.domain.financial.messages.comm.CommAdapterSizePayload"; // org.domain.financial.messages.comm.CommAdapterPayload, org.domain.financial.messages.comm.CommAdapterSizePayload
	// número máximo de conexões que podem aguardar na fila para ser capturadas pelo servidor
	private Integer backlog = 50;
	// Client to Server, Server to Client, Bidirecional
	private Utils.CommRequestDirection direction = Utils.CommRequestDirection.CLIENT_TO_SERVER; // CLIENT_TO_SERVER = 0, SERVER_TO_CLIENT = 1, BIDIRECIONAL = 2
	// Comm.ENDIAN_BIG ou Comm.ENDIAN_LITTLE
	@Column(name="endian_type")
	private Comm.BinaryEndian endianType = Comm.BinaryEndian.UNKNOW; // BIG
	// número máximo de seções simultâneas que o servidor pode processar
	@Column(name="max_opened_connections")
	private Integer maxOpenedConnections = 100; // n�mro m�ximo de conex�es : 1, 100, 1000
	@Column(name="message_adapter")
	private String messageAdapter = "ISO8583default";

	@Override
	public String toString() {
		return "CommConf [name=" + name + ", enabled=" + enabled + ", listen=" + listen + ", ip=" + ip + ", port="
				+ port + ", permanent=" + permanent + ", direction=" + direction + ", adapter=" + adapter
				+ ", messageConf=" + messageAdapter + "]";
	}

	public ISO8583RouterCommConf() {
	}

	public ISO8583RouterCommConf(String name, Integer port, Boolean permanent, Utils.CommRequestDirection direction, String adapter, String messageConf, Boolean sizeAscii) {
		this.name = name;
		this.port = port;
		this.permanent = permanent;
		this.direction = direction;
		this.adapter = adapter;
		this.messageAdapter = messageConf;
		this.sizeAscii = sizeAscii;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getEnabled() {
		return this.enabled != null ? this.enabled : false;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Boolean getListen() {
		return this.listen != null ? this.listen : false;
	}

	public void setListen(Boolean listen) {
		this.listen = listen;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Boolean getPermanent() {
		return this.permanent != null ? this.permanent : false;
	}

	public void setPermanent(Boolean permanent) {
		this.permanent = permanent;
	}

	public Utils.CommRequestDirection getDirection() {
		return this.direction != null ? this.direction : Utils.CommRequestDirection.CLIENT_TO_SERVER;
	}

	public void setDirection(Utils.CommRequestDirection direction) {
		this.direction = direction;
	}

	public Integer getMaxOpenedConnections() {
		return maxOpenedConnections;
	}

	public void setMaxOpenedConnections(Integer maxOpenedConnections) {
		this.maxOpenedConnections = maxOpenedConnections;
	}

	public String getAdapter() {
		return adapter;
	}

	public void setAdapter(String adapter) {
		this.adapter = adapter;
	}

	public String getMessageAdapter() {
		return messageAdapter;
	}

	public void setMessageAdapter(String messageAdapter) {
		this.messageAdapter = messageAdapter;
	}

	public Comm.BinaryEndian getEndianType() {
		return endianType;
	}

	public void setEndianType(Comm.BinaryEndian endianType) {
		this.endianType = endianType;
	}
	
	public Boolean getSizeAscii() {
		return sizeAscii;
	}

	public void setSizeAscii(Boolean sizeAscii) {
		this.sizeAscii = sizeAscii;
	}

	public Integer getBacklog() {
		return backlog;
	}

	public void setBacklog(Integer backlog) {
		this.backlog = backlog;
	}

	public String getSession() {
		return session;
	}

	public void setSession(String session) {
		this.session = session;
	}

}
