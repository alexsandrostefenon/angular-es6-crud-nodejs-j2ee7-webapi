package org.domain.iso8583router.entity;

import java.io.Serializable;
import javax.persistence.*;
import javax.validation.constraints.Size;

import java.lang.Long;
import java.util.HashMap;

import org.domain.commom.ByteArrayUtils;

@Entity
@Table(name = "iso8583_router_transaction")
@NamedQuery(name="Iso8583RouterTransaction.findAll", query="SELECT r FROM Iso8583RouterTransaction r")
public class Iso8583RouterTransaction implements Serializable {
/**
	 * 
	 */
	private static final long serialVersionUID = -1995674890443105853L;
	//	this.data = ByteArrayUtils.escapeBinaryData(other.getData(), false, '?');
//	this.dataComplement = ByteArrayUtils.escapeBinaryData(other.getDataComplement(), false, '?');
	private Integer id;
	private String authNsu;
	private String captureEc;
	private String captureEquipamentType;
	private String captureNsu;
	private String captureProtocol;
	private String captureTablesVersionsIn;
	private String captureTablesVersionsOut;
	private String captureType;
	private Integer cardExpiration;
	private String channelConn;
	private String codeProcess;
	private String codeResponse;
	private String connDirection;
	private String connId;
	private String countryCode;
	private String data;
	private String dataComplement;
	private String dateLocal;
	private String dateTimeGmt;
	private String emvData;
	private String emvPanSequence;
	private String equipamentId;
	private Integer financialDate;
	private String hourLocal;
	private String lastOkDate;
	private String lastOkNsu;
	private String merchantType;
	private String module;
	private String moduleIn;
	private String moduleOut;
	private String msgType;
	private String numPayments;
	private String pan;
	private String password;
	private String providerEc;
	private String providerId;
	private String providerName;
	private String providerNsu;
	private String replyEspected;
	private String root;
	private String route;
	private Boolean sendResponse;
	private String sequenceIndex;
	private String systemDateTime;
	private String terminalSerialNumber;
	private Integer timeExec;
	private Integer timeout;
	private Long timeStamp;
	private Long timeStampOn;
	private Long timeStampOff;
	private String trackI;
	private String trackIi;
	private Long transactionId;
	private String transactionValue;
	private String transportData;
	private String uniqueCaptureNsu;
	// dinamic
	protected HashMap<String, String> dinamicFields = new HashMap<String, String>(128);

	public Iso8583RouterTransaction() {
		// TODO Auto-generated constructor stub
	}

	public Iso8583RouterTransaction(Iso8583RouterTransaction other) {
		this.id = other.id;
		this.authNsu = other.authNsu;
		this.captureEc = other.captureEc;
		this.captureEquipamentType = other.captureEquipamentType;
		this.captureNsu = other.captureNsu;
		this.captureProtocol = other.captureProtocol;
		this.captureTablesVersionsIn = other.captureTablesVersionsIn;
		this.captureTablesVersionsOut = other.captureTablesVersionsOut;
		this.captureType = other.captureType;
		this.cardExpiration = other.cardExpiration;
		this.channelConn = other.channelConn;
		this.codeProcess = other.codeProcess;
		this.codeResponse = other.codeResponse;
		this.connDirection = other.connDirection;
		this.connId = other.connId;
		this.countryCode = other.countryCode;
		this.data = other.data;
		this.dataComplement = other.dataComplement;
		this.dateLocal = other.dateLocal;
		this.dateTimeGmt = other.dateTimeGmt;
		this.emvData = other.emvData;
		this.emvPanSequence = other.emvPanSequence;
		this.equipamentId = other.equipamentId;
		this.financialDate = other.financialDate;
		this.hourLocal = other.hourLocal;
		this.lastOkDate = other.lastOkDate;
		this.lastOkNsu = other.lastOkNsu;
		this.merchantType = other.merchantType;
		this.module = other.module;
		this.moduleIn = other.moduleIn;
		this.moduleOut = other.moduleOut;
		this.msgType = other.msgType;
		this.numPayments = other.numPayments;
		this.pan = other.pan;
		this.password = other.password;
		this.providerEc = other.providerEc;
		this.providerId = other.providerId;
		this.providerName = other.providerName;
		this.providerNsu = other.providerNsu;
		this.replyEspected = other.replyEspected;
		this.root = other.root;
		this.route = other.route;
		this.sendResponse = other.sendResponse;
		this.sequenceIndex = other.sequenceIndex;
		this.systemDateTime = other.systemDateTime;
		this.terminalSerialNumber = other.terminalSerialNumber;
		this.timeExec = other.timeExec;
		this.timeout = other.timeout;
		this.timeStamp = other.timeStamp;
		this.timeStampOn = other.getTimeStampOn();
		this.timeStampOff = other.getTimeStampOff();
		this.trackI = other.trackI;
		this.trackIi = other.trackIi;
		this.transactionId = other.transactionId;
		this.transactionValue = other.transactionValue;
		this.transportData = other.transportData;
		this.uniqueCaptureNsu = other.uniqueCaptureNsu;
		this.dinamicFields = new HashMap<String, String>(other.dinamicFields);
	}

	@Id
	@Column(name = "id", unique = true, nullable = false)
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Column(name = "auth_nsu", length = 20)
	@Size(max = 20)
	public String getAuthNsu() {
		return this.authNsu;
	}

	public void setAuthNsu(String authNsu) {
		this.authNsu = authNsu;
	}

	@Column(name = "capture_ec", length = 15)
	@Size(max = 15)
	public String getCaptureEc() {
		return this.captureEc;
	}

	public void setCaptureEc(String captureEc) {
		this.captureEc = captureEc;
	}

	@Column(name = "capture_equipament_type", length = 999)
	@Size(max = 999)
	public String getCaptureEquipamentType() {
		return this.captureEquipamentType;
	}

	public void setCaptureEquipamentType(String captureEquipamentType) {
		this.captureEquipamentType = captureEquipamentType;
	}

	@Column(name = "capture_nsu")
	public String getCaptureNsu() {
		return this.captureNsu;
	}

	public void setCaptureNsu(String captureNsu) {
		this.captureNsu = captureNsu;
	}

	@Column(name = "capture_protocol", length = 64)
	@Size(max = 64)
	public String getCaptureProtocol() {
		return this.captureProtocol;
	}

	public void setCaptureProtocol(String captureProtocol) {
		this.captureProtocol = captureProtocol;
	}

	@Column(name = "capture_tables_versions_in", length = 64)
	@Size(max = 64)
	public String getCaptureTablesVersionsIn() {
		return this.captureTablesVersionsIn;
	}

	public void setCaptureTablesVersionsIn(String captureTablesVersions) {
		this.captureTablesVersionsIn = captureTablesVersions;
	}

	@Column(name = "capture_tables_versions_out", length = 64)
	@Size(max = 64)
	public String getCaptureTablesVersionsOut() {
		return this.captureTablesVersionsOut;
	}

	public void setCaptureTablesVersionsOut(String captureTablesVersions) {
		this.captureTablesVersionsOut = captureTablesVersions;
	}

	@Column(name = "capture_type")
	public String getCaptureType() {
		return this.captureType;
	}

	public void setCaptureType(String captureType) {
		this.captureType = captureType;
	}

	@Column(name = "card_expiration")
	public Integer getCardExpiration() {
		return this.cardExpiration;
	}

	public void setCardExpiration(Integer cardExpiration) {
		this.cardExpiration = cardExpiration;
	}

	@Column(name = "channel_conn")
	public String getChannelConn() {
		return this.channelConn;
	}

	public void setChannelConn(String channelConn) {
		this.channelConn = channelConn;
	}

	@Column(name = "code_process", length = 6)
	@Size(max = 6)
	public String getCodeProcess() {
		return this.codeProcess;
	}

	public void setCodeProcess(String codeProcess) {
		this.codeProcess = codeProcess;
	}

	@Column(name = "code_response", length = 3)
	@Size(max = 3)
	public String getCodeResponse() {
		return this.codeResponse;
	}

	public void setCodeResponse(String codeResponse) {
		this.codeResponse = codeResponse;
	}

	@Column(name = "conn_direction", length = 4)
	@Size(max = 4)
	public String getConnDirection() {
		return this.connDirection;
	}

	public void setConnDirection(String connDirection) {
		this.connDirection = connDirection;
	}

	@Column(name = "conn_id", length = 64)
	@Size(max = 64)
	public String getConnId() {
		return this.connId;
	}

	public void setConnId(String connId) {
		this.connId = connId;
	}

	@Column(name = "country_code")
	public String getCountryCode() {
		return this.countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	@Column(name = "data", length = 5994)
	@Size(max = 5994)
	public String getData() {
		return this.data;
	}

	public void setData(String data) {
		this.data = data;
	}

	@Column(name = "data_complement", length = 5994)
	@Size(max = 5994)
	public String getDataComplement() {
		return this.dataComplement;
	}

	public void setDataComplement(String dataComplement) {
		this.dataComplement = dataComplement;
	}

	@Column(name = "date_local")
	public String getDateLocal() {
		return this.dateLocal;
	}

	public void setDateLocal(String dateLocal) {
		this.dateLocal = dateLocal;
	}

	@Column(name = "date_time_gmt")
	public String getDateTimeGmt() {
		return this.dateTimeGmt;
	}

	public void setDateTimeGmt(String dateTimeGmt) {
		this.dateTimeGmt = dateTimeGmt;
	}

	@Column(name = "emv_data", length = 999)
	@Size(max = 999)
	public String getEmvData() {
		return this.emvData;
	}

	public void setEmvData(String emvData) {
		this.emvData = emvData;
	}

	@Column(name = "emv_pan_sequence", length = 999)
	@Size(max = 999)
	public String getEmvPanSequence() {
		return this.emvPanSequence;
	}

	public void setEmvPanSequence(String emvPanSequence) {
		this.emvPanSequence = emvPanSequence;
	}

	@Column(name = "equipament_id", length = 8)
	@Size(max = 8)
	public String getEquipamentId() {
		return this.equipamentId;
	}

	public void setEquipamentId(String equipamentId) {
		this.equipamentId = equipamentId;
	}

	@Column(name = "financial_date")
	public Integer getFinancialDate() {
		return this.financialDate;
	}

	public void setFinancialDate(Integer financialDate) {
		this.financialDate = financialDate;
	}

	@Column(name = "hour_local")
	public String getHourLocal() {
		return this.hourLocal;
	}

	public void setHourLocal(String hourLocal) {
		this.hourLocal = hourLocal;
	}

	@Column(name = "last_ok_date")
	public String getLastOkDate() {
		return this.lastOkDate;
	}

	public void setLastOkDate(String lastOkDate) {
		this.lastOkDate = lastOkDate;
	}

	@Column(name = "last_ok_nsu", length = 20)
	@Size(max = 20)
	public String getLastOkNsu() {
		return this.lastOkNsu;
	}

	public void setLastOkNsu(String lastOkNsu) {
		this.lastOkNsu = lastOkNsu;
	}

	@Column(name = "merchant_type", length = 4)
	@Size(max = 4)
	public String getMerchantType() {
		return this.merchantType;
	}

	public void setMerchantType(String merchantType) {
		this.merchantType = merchantType;
	}

	@Column(name = "module", length = 64)
	@Size(max = 64)
	public String getModule() {
		return this.module;
	}

	public void setModule(String module) {
		this.module = module;
	}

	@Column(name = "module_in", length = 64)
	@Size(max = 64)
	public String getModuleIn() {
		return this.moduleIn;
	}

	public void setModuleIn(String moduleIn) {
		this.moduleIn = moduleIn;
	}

	@Column(name = "module_out", length = 64)
	@Size(max = 64)
	public String getModuleOut() {
		return this.moduleOut;
	}

	public void setModuleOut(String moduleOut) {
		this.moduleOut = moduleOut;
	}

	@Column(name = "msg_type", length = 64)
	@Size(max = 64)
	public String getMsgType() {
		return this.msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}

	@Column(name = "num_payments", length = 2)
	@Size(max = 2)
	public String getNumPayments() {
		return this.numPayments;
	}

	public void setNumPayments(String numPayments) {
		this.numPayments = numPayments;
	}

	@Column(name = "pan", length = 32)
	@Size(max = 32)
	public String getPan() {
		return this.pan;
	}

	public void setPan(String pan) {
		this.pan = pan;
	}

	@Column(name = "password", length = 64)
	@Size(max = 64)
	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Column(name = "provider_ec", length = 20)
	@Size(max = 20)
	public String getProviderEc() {
		return this.providerEc;
	}

	public void setProviderEc(String providerEc) {
		this.providerEc = providerEc;
	}

	@Column(name = "provider_id")
	public String getProviderId() {
		return this.providerId;
	}

	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}

	@Column(name = "provider_name", length = 64)
	@Size(max = 64)
	public String getProviderName() {
		return this.providerName;
	}

	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}

	@Column(name = "provider_nsu", length = 12)
	@Size(max = 12)
	public String getProviderNsu() {
		return this.providerNsu;
	}

	public void setProviderNsu(String providerNsu) {
		this.providerNsu = providerNsu;
	}

	@Column(name = "reply_espected", length = 1)
	@Size(max = 1)
	public String getReplyEspected() {
		return this.replyEspected;
	}

	public void setReplyEspected(String replyEspected) {
		this.replyEspected = replyEspected;
	}

	@Column(name = "root", length = 64)
	@Size(max = 64)
	public String getRoot() {
		return this.root;
	}

	public void setRoot(String root) {
		this.root = root;
	}

	@Column(name = "route", length = 4096)
	@Size(max = 4096)
	public String getRoute() {
		return this.route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	@Column(name = "send_response")
	public Boolean getSendResponse() {
		return this.sendResponse;
	}

	public void setSendResponse(Boolean sendResponse) {
		this.sendResponse = sendResponse;
	}

	@Column(name = "sequence_index", length = 3)
	@Size(max = 3)
	public String getSequenceIndex() {
		return this.sequenceIndex;
	}

	public void setSequenceIndex(String sequenceIndex) {
		this.sequenceIndex = sequenceIndex;
	}

	@Column(name = "system_date_time", length = 14)
	@Size(max = 14)
	public String getSystemDateTime() {
		return this.systemDateTime;
	}

	public void setSystemDateTime(String systemDateTime) {
		this.systemDateTime = systemDateTime;
	}

	@Column(name = "terminal_serial_number", length = 64)
	@Size(max = 64)
	public String getTerminalSerialNumber() {
		return this.terminalSerialNumber;
	}

	public void setTerminalSerialNumber(String terminalSerialNumber) {
		this.terminalSerialNumber = terminalSerialNumber;
	}

	@Column(name = "time_exec")
	public Integer getTimeExec() {
		return this.timeExec;
	}

	public void setTimeExec(Integer timeExec) {
		this.timeExec = timeExec;
	}

	@Column(name = "timeout")
	public Integer getTimeout() {
		return this.timeout;
	}

	public void setTimeout(Integer timeout) {
		this.timeout = timeout;
	}

	@Column(name = "time_stamp")
	public Long getTimeStamp() {
		return this.timeStamp;
	}

	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}

	@Column(name = "time_stamp_on")
	public Long getTimeStampOn() {
		return this.timeStampOn;
	}

	public void setTimeStampOn(Long timeStampOn) {
		this.timeStampOn = timeStampOn;
	}

	@Column(name = "time_stamp_off")
	public Long getTimeStampOff() {
		return this.timeStampOff;
	}

	public void setTimeStampOff(Long timeStampOff) {
		this.timeStampOff = timeStampOff;
	}

	@Column(name = "track_i", length = 160)
	@Size(max = 160)
	public String getTrackI() {
		return this.trackI;
	}

	public void setTrackI(String trackI) {
		this.trackI = trackI;
	}

	@Column(name = "track_ii", length = 104)
	@Size(max = 104)
	public String getTrackIi() {
		return this.trackIi;
	}

	public void setTrackIi(String trackIi) {
		this.trackIi = trackIi;
	}

	@Column(name = "transaction_id")
	public Long getTransactionId() {
		return this.transactionId;
	}

	public void setTransactionId(Long transactionId) {
		this.transactionId = transactionId;
	}

	@Column(name = "transaction_value")
	public String getTransactionValue() {
		return this.transactionValue;
	}

	public void setTransactionValue(String transactionValue) {
		this.transactionValue = transactionValue;
	}

	@Column(name = "transport_data", length = 999)
	@Size(max = 999)
	public String getTransportData() {
		return this.transportData;
	}

	public void setTransportData(String transportData) {
		this.transportData = transportData;
	}

	@Column(name = "unique_capture_nsu", length = 64)
	@Size(max = 64)
	public String getUniqueCaptureNsu() {
		return this.uniqueCaptureNsu;
	}

	public void setUniqueCaptureNsu(String uniqueCaptureNsu) {
		this.uniqueCaptureNsu = uniqueCaptureNsu;
	}
	// dinamic	
	@Column(name = "dinamic_fields", length = 5994)
	@Size(max = 5994)
	public String getDinamicFields() {
		StringBuffer buffer = new StringBuffer(this.dinamicFields.size() * 64);
		
		for (String key : this.dinamicFields.keySet()) {
			String value = this.dinamicFields.get(key);
			
			if (value != null && value.length() > 0) {
				buffer.append(key);
				buffer.append("=");
				value = ByteArrayUtils.escapeBinaryData(value, false, '?');
				buffer.append(value);
				buffer.append("|");
			}
		}
		
		if (buffer.length() > 0) {
			buffer.setLength(buffer.length()-1);
		}
		
		return buffer.toString();
	}

	public void setDinamicFields(String dinamicFields) {
		this.dinamicFields.clear();
		String[] pairs = dinamicFields.split("\\|");
		
		for (String pair : pairs) {
			String[] cols = pair.split("=");
			
			if (cols.length == 2) {
				this.dinamicFields.put(cols[0], cols[1]);
			}
		}
	}

}
