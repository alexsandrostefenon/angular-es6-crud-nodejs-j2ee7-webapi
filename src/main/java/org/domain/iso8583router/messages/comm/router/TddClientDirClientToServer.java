package org.domain.iso8583router.messages.comm.router;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.domain.commom.Logger;
import org.domain.iso8583router.messages.Message;
import org.domain.iso8583router.messages.comm.Connector;

// Precisa de apenas uma thread para tratar todos os meios de captura
class TddClientDirClientToServer implements Runnable {
	private Connector manager;
	private String[] fieldsCompareIgnore;
	private ArrayList<Message> list;
	private boolean cancelThread;
	private SimpleDateFormat simpleDateFormatYYMMDD = new SimpleDateFormat("yyMMdd");
	
	public void cancel() {
		this.cancelThread = true;
	}
	
	public void add(Message message) {
		this.list.add(message);
	}

	public void clear() {
		this.list.clear();
	}
	
	private void adjustSystemDateResponse(Message messageRef, Message message) {
		Calendar dt = Calendar.getInstance();
		String dateToday = String.format("%02d%02d%02d", dt.get(Calendar.YEAR)-2000, dt.get(Calendar.MONTH)+1, dt.get(Calendar.DAY_OF_MONTH));
		String lastOkDate = message.getLastOkDate();
		String lastOkDateRef = messageRef.getLastOkDate();
		
		if (dateToday.equals(lastOkDate) && lastOkDateRef != null && lastOkDateRef.equals(lastOkDate) == false) {
			try {
				this.simpleDateFormatYYMMDD.parse(lastOkDateRef);
			} catch (Exception e) {
				this.manager.log(Logger.LOG_LEVEL_ERROR, "adjustSystemDateResponse", String.format("invalid date format : %s", lastOkDateRef), messageRef);
				return;
			}
			
			messageRef.setLastOkDate(dateToday);
		}
	}
	
	private void commRelease(Message messagePrev, int index) {
		if (messagePrev != null) {
			Integer poolCommId = messagePrev.getPoolCommId();
			
			if (poolCommId != null) {
				boolean closeComm = true;
				
				if (index < this.list.size()) {
					Message message = this.list.get(index);
					String uniqueCaptureNsu = message.getUniqueCaptureNsu();
					String uniqueCaptureNsuPrev = messagePrev.getUniqueCaptureNsu();

					if (uniqueCaptureNsu.equals(uniqueCaptureNsuPrev)) {
						message.setPoolCommId(poolCommId);
						closeComm = false;
					}
				}
				
				if (closeComm) {
//					this.manager.commRelease(messagePrev);
				}
			}
		}
	}

	private boolean setReplyEspected(Message message, int indexNext) {
		boolean replyEspected = false;
		String msgType = message.getMsgType();
		
		if (msgType == null || msgType.equals("0202") == false) {
			if ("0202".equals(message.getMsgType()) == false && indexNext < this.list.size()) {
				Message messageNext = this.list.get(indexNext);
				String uniqueCaptureNsu = message.getUniqueCaptureNsu();
				String uniqueCaptureNsuNext = messageNext.getUniqueCaptureNsu();

				if (uniqueCaptureNsuNext.equals(uniqueCaptureNsu)) {
					replyEspected = true;
					message.setReplyEspected("1");
				}
			}
		}
		
		return replyEspected;
	}

	public void run() {
		int activeIndex = 0;
		this.cancelThread = false;
		Message messageOut = null;
		
		try {
			while (this.cancelThread == false && activeIndex < this.list.size()) {
				commRelease(messageOut, activeIndex);
				messageOut = this.list.get(activeIndex++);
				String moduleName = messageOut.getModule();
				messageOut.setModuleOut(moduleName);
				messageOut.setModuleIn(moduleName);
				boolean replyEspected = setReplyEspected(messageOut, activeIndex);
				this.manager.log(Logger.LOG_LEVEL_TRACE, "TddClientToServer.run", String.format("enviando [%s/%s - %s]", activeIndex, this.list.size(), moduleName), messageOut);
				Message messageIn = new Message();
				this.manager.commOut(messageOut, messageIn);
				this.manager.log(Logger.LOG_LEVEL_TRACE, "TddClientToServer.run", String.format("recebido [%s]", moduleName), messageOut);

				if (replyEspected) {
					Message messageInRef = this.list.get(activeIndex++);
					adjustSystemDateResponse(messageInRef, messageIn);
					String fieldName = Message.compareIgnore(messageInRef, messageIn, fieldsCompareIgnore); 

					if (fieldName != null) {
						String dataReceived = messageIn.getFieldData(fieldName);
						String dataEspected = messageInRef.getFieldData(fieldName);
						this.manager.log(Logger.LOG_LEVEL_ERROR, "ES.TddClient.execute", String.format("diference in %s - [%s]", fieldName, moduleName), messageInRef);
						this.manager.log(Logger.LOG_LEVEL_ERROR, "ES.TddClient.execute", String.format("received [%04d - %s]", dataReceived != null ? dataReceived.length() : 0, dataReceived), messageIn);
						this.manager.log(Logger.LOG_LEVEL_ERROR, "ES.TddClient.execute", String.format("espected [%04d - %s]", dataEspected != null ? dataEspected.length() : 0, dataEspected), messageInRef);
						// Aborta o teste
						this.cancelThread = true;
					}
				} else {
					Thread.sleep(2000);
				}
			}
			
			this.manager.log(Logger.LOG_LEVEL_TRACE, "TddClientToServer.run", "commRelease", messageOut);
			commRelease(messageOut, this.list.size());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public TddClientDirClientToServer(Connector manager, String[] fieldsCompareIgnore) {
		this.manager = manager;
		this.fieldsCompareIgnore = fieldsCompareIgnore;
		this.list = new ArrayList<Message>(1024);
	}

	public void setFieldsCompareIgnore(String[] fieldsCompareIgnore) {
		this.fieldsCompareIgnore = fieldsCompareIgnore;
	}
}
