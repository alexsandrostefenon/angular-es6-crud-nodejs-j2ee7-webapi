package org.domain.financial2.messages.capture;

import java.io.FileOutputStream;
import java.util.List;

import javax.persistence.EntityManager;

import org.domain.commom.ByteArrayUtils;
import org.domain.financial2.entity.ISO8583Bin;

public class BinDinamicTables {

	private EntityManager entityManager;

	public BinDinamicTables(EntityManager entityManager, boolean isJTA) {
		this.entityManager = entityManager;
	}

	private void parseBin(int id, String data) {
		ISO8583Bin bin = new ISO8583Bin();
		int offset = 0;
		bin.setBin(ByteArrayUtils.parseInt(data, offset, 6));
		offset += 6;
		bin.setRange(ByteArrayUtils.parseInt(data, offset, 4));
		offset += 4;
		bin.setEmitter(data.substring(offset, offset+2));
		offset += 2;
		bin.setPanSize(ByteArrayUtils.parseInt(data, offset, 2));
		offset += 2;
		bin.setProvider(ByteArrayUtils.parseInt(data, offset, 3));
		offset += 3;
		bin.setTimeout(ByteArrayUtils.parseInt(data, offset, 3));
		offset += 3;
		offset++;
		bin.setProducts(ByteArrayUtils.hexToInt(data, offset, 4, '0', '0'));
		offset += 4;
		int posEnd = data.indexOf('*', offset);
		
		if (posEnd > 0) {
			bin.setQuestions(data.substring(offset, posEnd));
			offset = posEnd + 1;
			
			if (offset < data.length()) {
				bin.setProviderName(data.substring(posEnd)+1);
			}
		}
		
		this.entityManager.getTransaction().begin();
		
		try {
			this.entityManager.persist(bin);
		} catch (Exception e) {
			System.err.println(bin);
			e.printStackTrace();
		}
		
		try {
			this.entityManager.getTransaction().commit();
		} catch (Exception e) {
			System.err.println(bin);
			e.printStackTrace();
		}
		
		this.entityManager.clear();
	}

	void parse(String data) {
//		this.entityManager.getTransaction().begin();
		this.entityManager.createQuery("DELETE FROM Bin b").executeUpdate();			
//		this.entityManager.getTransaction().commit();
		String[] rows = data.split("@");
		
		for (int i = 0; i < rows.length; i++) {
			String row = rows[i];
			
			if (row.length() > 0) {
				parseBin(i+1, row);
			}
		}
		
		StringBuilder builder = new StringBuilder(100*1024);
		@SuppressWarnings("unchecked")
		List<ISO8583Bin> bins = this.entityManager.createQuery("from Bin b order by b.provider,b.bin,b.emitter").getResultList();
		
		for (ISO8583Bin binDinamic : bins) {
			builder.append(binDinamic.toString());
		}
		
		try {
			FileOutputStream fileOutputStream = new FileOutputStream("BinDinamic.txt");
			fileOutputStream.write(builder.toString().getBytes());
			fileOutputStream.close();
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}
