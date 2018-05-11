package org.domain.iso8583router.messages.comm;

import java.io.IOException;

import org.domain.iso8583router.messages.Message;

public class CommAdapterPayload implements CommAdapter {
	
	public void send(Comm comm, Message message, byte[] payload) throws Exception {
		// EMULADOR
		if (message.getTimeStamp() != null && message.getTimeExec() != null) {
			Integer timeSleep = message.getTimeExec();
			long timeExec = System.currentTimeMillis() - message.getTimeStamp();
			
			if (timeSleep > timeExec) {
				Thread.sleep(timeSleep - timeExec);
			}
		}

		comm.os.write(payload);
	}

	public int receive(Comm comm, Message message, byte[] payload) throws Exception {
		int rc = -1;
		int readen = comm.is.read(payload, 0, payload.length);
		
		if (readen > 0) {
			rc = readen;
		} else {
			throw new IOException("CommAdapterPayload.read : Invalid size received");
		}
		
		return rc;
	}

	public void setup(String paramsSend, String paramsReceive) {
	}

}
