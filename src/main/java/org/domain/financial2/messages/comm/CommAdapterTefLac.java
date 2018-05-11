package org.domain.financial2.messages.comm;

import java.io.IOException;

import org.domain.iso8583router.messages.Message;
import org.domain.iso8583router.messages.comm.Comm;
import org.domain.iso8583router.messages.comm.CommAdapter;
import org.domain.commom.BinaryEndian;
import org.domain.commom.ByteArrayUtils;
import org.domain.commom.RefInt;

public class CommAdapterTefLac implements CommAdapter {
	
	public void send(Comm comm, Message message, byte[] payload) throws Exception {
		String captureEc = message.getCaptureEc();
		
		if (captureEc == null || captureEc.length() != 15) {
			throw new IOException("CommAdapterTefLac.read : captureEc received");
		}
		
		int size = 1 + 15 + 10 + payload.length;
		int offset = 0;
		byte[] buffer = new byte[2 + size];//L0000000003487220000000001
		String header = "L" + captureEc + "0000000001";
		offset = ByteArrayUtils.pack(buffer, offset, 2, BinaryEndian.BIG, size);
		offset = ByteArrayUtils.pack(buffer, offset, header.length(), header.getBytes());
		offset = ByteArrayUtils.pack(buffer, offset, payload.length, payload);
		comm.os.write(buffer, 0, offset);
	}

	public int receive(Comm comm, Message message, byte[] payload) throws Exception {
		int rc = -1;
		RefInt size = new RefInt();
		
		synchronized (comm.is) {
			ByteArrayUtils.unpack(comm.is, payload, 0, 2, BinaryEndian.BIG, size);

			if (size.value <= 0) {
				throw new IOException(String.format("CommAdapterTefLac.read : negative or null size len received : %d", size.value));
			} else if (size.value > payload.length) {
				throw new IOException(String.format("CommAdapterTefLac.read : Invalid size len received, received %d, max %d", size.value, payload.length));
			} else {
				int readen = comm.is.read(payload, 0, size.value);
				
				if (readen != size.value) {
					throw new IOException(String.format("CommAdapterTefLac.read : Invalid size len received, received %d, espected %d", readen, size.value));
				}
				
				if (readen < 26) {
					throw new IOException("CommAdapterTefLac.read : Invalid header size");
				}
				
				for (int i = 26; i < readen; i++) {
					payload[i-26] = payload[i];
				}
				
				rc = size.value - 26;
			}
		}

		return rc;
	}

	public void setup(String paramsSend, String paramsReceive) {
	}

}
