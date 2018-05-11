package org.domain.financial2.messages.comm;

import java.io.IOException;

import org.domain.commom.ByteArrayUtils;
import org.domain.commom.RefInt;
import org.domain.iso8583router.messages.Message;
import org.domain.iso8583router.messages.comm.Comm;
import org.domain.iso8583router.messages.comm.CommAdapter;

public class CommAdapterSizeTpduPayloadHex implements CommAdapter {
	private byte[] tpduHeader = new byte[] { 0x60, 0x00, 0x00, 0x00, 0x00 };
	
	public void send(Comm comm, Message message, byte[] payload) throws Exception {
		int size = payload.length;
		int offset = 0;
		byte[] buffer = new byte[size+4];
		
		if (comm.conf.getSizeAscii()) {
			String strSize = String.format("%04d", size);
			offset = Comm.pack(buffer, offset, strSize.length(), strSize.getBytes());
		} else {
			offset = Comm.pack(buffer, offset, 2, comm.conf.getEndianType(), size);
		}
		
		offset = ByteArrayUtils.pack(buffer, offset, tpduHeader.length, tpduHeader);

		int payloadLength = payload.length;
		byte[] aux = new byte[(payloadLength / 2) + (payloadLength % 2)];
		String str = new String(payload);
		org.domain.commom.ByteArrayUtils.AsciiHexToBinary(aux, str);
		payload = aux;
		
		offset = ByteArrayUtils.pack(buffer, offset, payload.length, payload);
		// EMULADOR
		if (message.getTimeStamp() != null && message.getTimeExec() != null) {
			Integer timeSleep = message.getTimeExec();
			long timeExec = System.currentTimeMillis() - message.getTimeStamp();
			
			if (timeSleep > timeExec) {
				Thread.sleep(timeSleep - timeExec);
			}
		}

		comm.os.write(buffer, 0, offset);
	}

	public int receive(Comm comm, Message message, byte[] payload) throws Exception {
		int rc = -1;
		RefInt size = new RefInt();
		
		if (comm.conf.getSizeAscii()) {
			int readen = comm.is.read(payload, 0, 4);
			
			if (readen != 4) {
				throw new IOException("CommAdapterSizePayload.read : Invalid size len received");
			}
			
			String strSize = new String(payload, 0, 4, "ISO-8859-1");
			size.value = Integer.parseInt(strSize);
		} else {
			Comm.unpack(comm.is, payload, 0, 2, comm.conf.getEndianType(), size);
		}
		// l� o cabe�alho TPDU
		comm.is.read(payload, 0, tpduHeader.length);
		
		if (size.value > 0 && size.value < payload.length) {
			int readen = comm.is.read(payload, 0, size.value);
			
			if (readen != size.value) {
				throw new IOException("CommAdapterSizePayload.read : Invalid size len received");
			}
			
			ByteArrayUtils.unCompress(payload, 0, readen);
			rc = readen * 2;
		}

		return rc;
	}

	public void setup(String paramsSend, String paramsReceive) {
	}

}
