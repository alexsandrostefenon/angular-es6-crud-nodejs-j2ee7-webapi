package org.domain.financial2.messages.comm;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import org.domain.commom.BinaryEndian;
import org.domain.commom.ByteArrayUtils;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

public class Pinpad {
	static byte EOT = 0x04;
	static byte ACK = 0x06;
	static byte DC3 = 0x13;
	static byte NAK = 0x15;
	static byte SYN = 0x16;
	static byte ETB = 0x17;
	static byte CAN = 0x18;

	private CommPortIdentifier portId;
  private SerialPort serialPort;
  private OutputStream outputStream;
	private InputStream inputStream;
	private byte[] bufferAux = new byte[10*2048];
	private byte[] bufferSend = new byte[10*2048];
	private byte[] bufferReceive = new byte[10*2048];
	private byte[] bufferBlock = new byte[10*2048];
	
 	private static int calcCrc (byte[] pbData, int offset, int iLength)	{
		int CRC_MASK  = 0x1021; /* x^16 + x^12 + x^5 + x^0 */
		int wData, wCRC = 0;
		
		for (int j = 0; j < iLength; j++) {
			int val = (0xff & pbData[offset++]);
			wData = val << 8;
			
			for (int i = 0; i < 8; i++, wData <<= 1) {
				if (((wCRC ^ wData) & 0x00008000) != 0) {
					wCRC = ((wCRC << 1) ^ CRC_MASK);
				} else {
					wCRC <<= 1;
				}
			}
		}
		
		wCRC &= 0x0000ffff;
		return wCRC;
	}
	
	private static int buildPackage(byte[] bufferOut, byte[] bufferAux, byte[] cmd, byte[] data, int dataSize) {
		int offset = 0;
		offset = ByteArrayUtils.pack(bufferAux, offset, cmd.length, BinaryEndian.LITTLE, cmd);
		
		if (data != null) {
			offset = ByteArrayUtils.pack(bufferAux, offset, dataSize, BinaryEndian.LITTLE, data);
		}
		
		bufferAux[offset++] = Pinpad.ETB;
		int crc = calcCrc(bufferAux, 0, offset);
		// aplica os escapes
		bufferOut[0] = Pinpad.SYN;
		int size = 1;
		dataSize += cmd.length;
		
		for (int i = 0; i < dataSize; i++, size++) {
			byte val = bufferAux[i];
			
			if (val == Pinpad.DC3 || val == Pinpad.SYN || val == Pinpad.ETB) {
				bufferOut[size++] = Pinpad.DC3;
				val -= 0x10;
				val += 0x30;
			}
			
			bufferOut[size] = val;
		}
		
		bufferOut[size++] = Pinpad.ETB;
		size = ByteArrayUtils.pack(bufferOut, size, 2, BinaryEndian.BIG, crc);
		return size;
	}
	
	private static int crackPackage(byte[] bufferOut, byte[] data, int dataSize, byte[] cmd) {
		if (dataSize < 1) {
			System.out.println("buffer recebido inv�lido.");
			return -1;
		}
		
		if (dataSize < 10) { // SYN, PKTDATA [cmd(3) + stat(3) + ...] , ETB, CRC(2) 
			return -1;
		}
		
		if (data[0] != Pinpad.SYN || data[dataSize-3] != Pinpad.ETB) {
			System.out.println("protocolo inv�lido 1.");
			return -1;
		}
		
		if (data[1] == 'E' && data[2] == 'R' && data[3] == 'R') {
			System.out.println("resposta de ERRO recebido.");
		} else if (data[1] != cmd[0] || data[2] != cmd[1] || data[3] != cmd[2]) {
			System.out.println("protocolo inv�lido 2.");
			return -1;
		}
		
		byte crc1 = data[dataSize-2];
		byte crc2 = data[dataSize-1];
		int crc = crc1 << 8 | crc2;
		crc &= 0x0000ffff;
		dataSize -= 2;
		int pktDataSize = 0;
		
		for (int i = 1; i < dataSize; pktDataSize++, i++) {
			byte val = data[i];
			
			if (val == Pinpad.DC3) {
				val = data[++i];
				val -= 0x30;
				val += 0x10;
			}
			
			bufferOut[pktDataSize] = val;
		}
		
		int _crc = calcCrc(bufferOut, 0, pktDataSize);
		pktDataSize -= 2;
		int ret = -1;
		
		if (crc == _crc) {
			String str = new String(data, 4, 3);
			System.out.println("STAT = " + str);
			ret = Integer.parseInt(str);
			int j = 0;
			
			for (int i = 6; i < pktDataSize; i++) {
				byte val = bufferOut[i];
				bufferOut[j++] = val;
			}
		} else {
			System.out.println("CRC inv�lido.");
		}
		
		return ret;
	}

	public void close() {
    try {
			this.outputStream.close();
			this.inputStream.close();
			this.serialPort.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
  public Pinpad(String ttyName) {
    Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();
    this.portId = null;

    while (portList.hasMoreElements()) {
    	CommPortIdentifier _portId = (CommPortIdentifier) portList.nextElement();
      
      if (_portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				if (_portId.getName().equals(ttyName)) {
				   this.portId = _portId;
				}
      }
    }
    
    if (this.portId != null) {
      try {
        this.serialPort = (SerialPort) this.portId.open("pinpad", 2000);
	    } catch (PortInUseException e) {
	    		e.printStackTrace();
	    }
	    
	    try {
	    	this.outputStream = this.serialPort.getOutputStream();
	      this.inputStream = this.serialPort.getInputStream();
	    } catch (IOException e) {
	  		e.printStackTrace();
	    }
	    
	    try {
	    	this.serialPort.setSerialPortParams(19200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
	    } catch (UnsupportedCommOperationException e) {
	  		e.printStackTrace();
	    }
    }
    
  }
  
  public int getBlock(byte[] dataOut, int blockId) {
  	int ret = 0;
  	
  	for (int i = 0; i < blockId; i++) {
  		
  	}
  	
  	return ret;
  }
  
  public int comm(byte[] dataOut, String cmdStr, byte[] dataIn, int dataSize, int timeout) throws Exception {
  	int ret = 0;
  	byte[] cmd = cmdStr.getBytes();
  	
  	int sendSize = buildPackage(this.bufferSend, this.bufferAux, cmd, dataIn, dataSize);
		outputStream.write(this.bufferSend, 0, sendSize);
		// primeiro espera o ACK
    Thread.sleep(1000);
		int val = inputStream.read();
		
		if (val == Pinpad.ACK) {
  		System.out.println("ACK recebido, aguardando processamento ou o timeout.");
			int sizeRead;
			
			do {
		  	sizeRead = inputStream.read(this.bufferReceive);
		  	
		  	if (sizeRead > 0) {
		  		ret = crackPackage(dataOut, this.bufferReceive, sizeRead, cmd);
		  		break;
		  	}
		  	
		    Thread.sleep(1000);
		  	System.out.println("Aguardando Resposta.");
			} while (--timeout > 0);
			// se deu timeout, cancela o comando
			if (sizeRead <= 0 && timeout == 0) {
				outputStream.write(Pinpad.CAN);
				inputStream.read();
			}
		} else if (val == Pinpad.NAK) {
  		System.out.println("NAK recebido.");
  		ret = -1;
		} else {
  		System.out.println("primeiro byte da resposta inv�lido.");
  		ret = -1;
		}
  	
  	return ret;
  }
	
	public static void main(String[] args) {
  	byte[] dataOut = new byte[2048];

    try {
    	Pinpad pinpad = new Pinpad("COM6");
    	int stat;
//    	stat = pinpad.comm(dataOut, "OPN", null, 0, 1);
//    	stat = pinpad.comm(dataOut, "DSP03212345678901234567890123456789012", null, 0, 1);
    	stat = pinpad.comm(dataOut, "CKE003010", null, 0, 20); // pg. 65 ... TODO : utilizar CEX
    	// chip
//    	stat = pinpad.comm(dataOut, "CHP00501000", null, 0);
    	pinpad.close();
    } catch (Exception e) {
    	System.out.println(e.getMessage());
    }
  }

}
