package org.domain.financial2.messages.comm;

import java.io.IOException;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import org.domain.commom.RefInt;
import org.domain.iso8583router.messages.Message;
import org.domain.iso8583router.messages.comm.Comm;
import org.domain.iso8583router.messages.comm.CommAdapter;

public class CommAdapterSizePayloadCrypt implements CommAdapter {
	private Cipher crypt;
	private Cipher decrypt;
	
	public CommAdapterSizePayloadCrypt() {
		try {
			// {0x85, 0x02, 0x1f, 0x5b, 0xdc, 0x67, 0x9b, 0x13};
			byte[] keyBin = new byte[] { (byte) 0x85, 0x02, 0x1f, 0x5b, (byte) 0xdc, 0x67, (byte) 0x9b, 0x13 };
			KeySpec ks = new DESKeySpec(keyBin);
			SecretKeyFactory kf = SecretKeyFactory.getInstance("DES");
			SecretKey key = kf.generateSecret(ks);
			String algorithm = "DES/ECB/NoPadding";
			this.crypt = Cipher.getInstance(algorithm);
			this.crypt.init(Cipher.ENCRYPT_MODE, key);
			this.decrypt = Cipher.getInstance(algorithm);
			this.decrypt.init(Cipher.DECRYPT_MODE, key);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
	}
	
	public void send(Comm comm, Message message, byte[] payload) throws Exception {
		int size = payload.length;
		int cryptSize = size;
		int fillSize = 0;
		// 8 por conta do toFill 0x00 da criptografia
		if ((size % 8) != 0) {
			cryptSize = ((size / 8) + 1) * 8;
			fillSize = cryptSize - size; 
		}
		
		int offset = 0;
		byte[] buffer = new byte[cryptSize+4];// 4 por conta do maior cabecalho possivel
		
		if (comm.conf.getSizeAscii()) {
			String strSize = String.format("%04d", cryptSize);
			offset = Comm.pack(buffer, offset, strSize.length(), strSize.getBytes());
		} else {
			offset = Comm.pack(buffer, offset, 2, comm.conf.getEndianType(), cryptSize);
		}
		// criptografa o payload
		byte[] aux = new byte[cryptSize];
		System.arraycopy(payload, 0, aux, 0, size);
		offset += crypt.doFinal(aux, 0, cryptSize, buffer, offset);
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
		
		if (size.value > 0 && size.value < payload.length) {
			int readen = comm.is.read(comm.bufferReceive, 0, size.value);
			
			if (readen != size.value) {
				throw new IOException("CommAdapterSizePayload.read : Invalid size len received");
			}
			
			rc = decrypt.doFinal(comm.bufferReceive, 0, readen, payload, 0);
		}

		return rc;
	}

	public void setup(String paramsSend, String paramsReceive) {
	}

}
