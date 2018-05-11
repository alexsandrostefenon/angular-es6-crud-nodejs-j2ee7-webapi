package org.domain.iso8583router.messages.comm;
import org.domain.iso8583router.messages.Message;

public interface CommAdapter {
	public void send(Comm comm, Message message, byte[] payload) throws Exception;
	
	public int receive(Comm comm, Message message, byte[] payload) throws Exception;

	public void setup(String paramsSend, String paramsReceive);
			
}
