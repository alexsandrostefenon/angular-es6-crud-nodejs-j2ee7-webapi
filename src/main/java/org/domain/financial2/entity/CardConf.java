package org.domain.financial2.entity;

import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.persistence.EntityManager;

import org.domain.commom.ByteArrayUtils;
import org.domain.commom.Utils;
import org.domain.iso8583router.entity.ISO8583RouterTransaction;

public class CardConf {
	private ISO8583Bin bin;
	private ISO8583TefProduct product;
	private ISO8583TefProvider provider;
	
	public ISO8583Bin getBin() {
		return bin;
	}

	public void setBin(ISO8583Bin bin) {
		this.bin = bin;
	}

	public ISO8583TefProduct getProduct() {
		return product;
	}

	public void setProduct(ISO8583TefProduct product) {
		this.product = product;
	}

	public ISO8583TefProvider getProvider() {
		return provider;
	}

	public void setProvider(ISO8583TefProvider provider) {
		this.provider = provider;
	}

	private static String getSqlBin(EntityManager em, String binAndEmitter) {
		String sql = null;
		
		if (binAndEmitter != null && binAndEmitter.length() >= 8) {
			if (Utils.isDecimal(binAndEmitter, 0, 8)) {
				String bin = binAndEmitter.substring(0, 6);
				String emitter = binAndEmitter.substring(6, 8);
				// 1 - verifica TefBin + emissor esta cadastrado no banco
				sql = String.format("from Bin b where b.bin <= %s and %s <= b.range and b.emitter = '%s'", bin, bin, emitter);
				List<ISO8583Bin> list = em.createQuery(sql, ISO8583Bin.class).getResultList();
				
				if (list == null || list.size() == 0) {
					// 2 - se nao achou com emissor, tenta sem
					sql = String.format("from Bin b where b.bin <= %s and %s <= b.range and b.emitter = '##'", bin, bin);
					list = em.createQuery(sql, ISO8583Bin.class).getResultList();
				}
				
				if (list == null || list.size() == 0) {
					sql = null;
					System.err.println("Bin nao cadastrado : " + bin);
				} else {
					System.out.println("Configuracoes do Bin : " + list);
				}
			} else {
				System.err.println("Trilha II com bin e emissor invalido : " + binAndEmitter);
			}
		} else {
			System.err.println("Trilha II de tamanho invalido : " + binAndEmitter);
		}
		
		return sql;
	}
	
	private static List<ISO8583Bin> getListBin(EntityManager em, String binAndEmitter) {
		List<ISO8583Bin> list = null;
		String sql = getSqlBin(em, binAndEmitter);
		
		if (sql != null) {
			list = em.createQuery(sql, ISO8583Bin.class).getResultList();
		}
		
		return list;
	}
	
	public static List<ISO8583TefProductWithoutCard> getProductsWithoutCard(EntityManager em) {
		String sql = String.format("from TefProductWithoutCard p");
		List<ISO8583TefProductWithoutCard> TefProductWithoutCard =  em.createQuery(sql, ISO8583TefProductWithoutCard.class).getResultList();
		System.out.println(TefProductWithoutCard);
		return TefProductWithoutCard;
	}
	
	// Devolve a lista dos produtos habilitados para o bin + emissor
	public static List<ISO8583TefProduct> getProducts(EntityManager em, String binAndEmitter) {
		String sql = getSqlBin(em, binAndEmitter);
		List<ISO8583TefProduct> tefProducts = null;
		
		if (sql != null) {
			// 3 - Carrega os produtos
			// select * from Tef_Product p where p.id in (select b.Product from Tef_Bin b where b.val_min <= 605663 and 605663 <= b.val_max and b.emitter = '##')
			sql = String.format("from TefProduct p where p.id in (select b.product %s)", sql);
			tefProducts =  em.createQuery(sql, ISO8583TefProduct.class).getResultList();
			System.out.println(tefProducts);
		}
		
		return tefProducts;
	}
	
	private static List<ISO8583Bin> filterBin(List<ISO8583Bin> bins, int productId) {
		List<ISO8583Bin> ret = new ArrayList<ISO8583Bin>(bins.size());
		
		for (ISO8583Bin bin : bins) {
			if (bin.getProduct() == productId) {
				ret.add(bin);
			}
		}
		
		return ret;
	}
	
	public static ISO8583Bin getBin(EntityManager em, String binAndEmitter, int productId) {
		ISO8583Bin bin = null;
		List<ISO8583Bin> list = getListBin(em, binAndEmitter);
		list = filterBin(list, productId);
		
		if (list != null && list.size() == 1) {
			bin = list.get(0);
		}
		
		return bin;
	}
	
	private static List<ISO8583TefFlow> loadTefFlows(EntityManager em, String strTefFlows) {
		List<ISO8583TefFlow> tefFlows = em.createQuery("from TefFlow f", ISO8583TefFlow.class).getResultList();
		List<ISO8583TefFlow> list = new ArrayList<ISO8583TefFlow>(tefFlows.size());
		
		if (strTefFlows != null) {
			int offsetTefFlows = 0;
			
			while (offsetTefFlows < strTefFlows.length()) {
				String strTefFlow = strTefFlows.substring(offsetTefFlows, offsetTefFlows+3);
				offsetTefFlows += 3;
				int tefFlowId = Integer.parseInt(strTefFlow);
				ISO8583TefFlow tefFlow = org.domain.financial2.entity.ISO8583Bin.getFlow(tefFlows, tefFlowId);
				list.add(tefFlow);
			}
		}
		
		return list;
	}
	
	public static List<ISO8583TefFlow> getTefFlows(EntityManager em, int binId, int productId) {
		List<ISO8583TefFlow> list = null;
		String sql = String.format("from TefProduct p where p.id = %d", productId);
		ISO8583TefProduct tefProduct = em.createQuery(sql, ISO8583TefProduct.class).getSingleResult();
		sql = String.format("from Bin b where b.id = %d", binId);
		ISO8583Bin bin = em.createQuery(sql, ISO8583Bin.class).getSingleResult();
		
		if (bin != null && tefProduct != null) {
			String tefFlowsByBin = bin.getFlows(); 
			String tefFlowsByTefProduct = tefProduct.getFlows();
			
			if (tefFlowsByBin == null || tefFlowsByTefProduct == null) {
				String tefFlows = tefFlowsByBin;
				
				if (tefFlows == null) {
					tefFlows = tefFlowsByTefProduct;
				}

				list = loadTefFlows(em, tefFlows);
			} else {
				System.err.println("Encontrada mais de uma configuracao de Fluxos para a sessao");
			}
		} else {
			System.err.println("Produto nao encontrado");
		}

		return list;
	}

	public static String extractPan(ISO8583RouterTransaction message) {
		String pan = message.getPan();
		
		if (pan == null) {
			pan = message.getTrackIi();
			
			if (pan != null) {
	    	if (pan.length() > 19) {
	    		pan = pan.substring(0, 19);
	    	}
	    	
	    	int pos = pan.indexOf('=');
	    	
	    	if (pos > 0) {
	    		pan = pan.substring(0, pos);
	    	}
			}
		}
		
		return pan;
	}
	
	public static CardConf getTefConf(EntityManager em, ISO8583RouterTransaction message) {
		String codeProcess = message.getCodeProcess();
		
		if (codeProcess == null || Utils.isDecimal(codeProcess, 0, codeProcess.length()) == false) {
			return null;
		}
		
		int code = Integer.parseInt(codeProcess);
		String pan = CardConf.extractPan(message);
		List<ISO8583TefProduct> products = CardConf.getProducts(em, pan);
		CardConf conf = null;
		
		if (products != null) {
			for (ISO8583TefProduct tefProduct : products) {
				int _code = tefProduct.getCodeProcess();
				
				if (_code == code) {
					Integer productId = tefProduct.getId();
					conf = new CardConf();
					conf.bin = CardConf.getBin(em, pan, productId);
					conf.product = tefProduct;
					String sql = String.format("from TefProvider p where p.id = %d", conf.bin.getProvider());
					conf.provider = (ISO8583TefProvider) em.createQuery(sql).getSingleResult();
					break;
				}
			}
		}
		
		return conf;
	}
	
	public static String getLastDigitsPanBeforeDv(String pan, boolean padding) {
		String lastDigitsPanBeforeDv;
		int pos = pan.indexOf('=');
		
		if (pos >= (12+1)) {
			lastDigitsPanBeforeDv = pan.substring(pos-(12+1), pos-1);
		} else if (pan.length() >= (12+1)) {
			pos = pan.length();
			lastDigitsPanBeforeDv = pan.substring(pos-(12+1), pos-1);
		} else {
			lastDigitsPanBeforeDv = "000000000000";
		}
		
		if (padding == true) {
			lastDigitsPanBeforeDv = "0000" + lastDigitsPanBeforeDv;
		}

		return lastDigitsPanBeforeDv;
	}
	
	private static String decryptDes(String masterKey, String dataInHex) {
		String ret = null;
		
		try {
			byte[] dataIn = new byte[dataInHex.length()/2];
			org.domain.commom.ByteArrayUtils.AsciiHexToBinary(dataIn, dataInHex);
			byte[] masterKeyBin = new byte[8];
			org.domain.commom.ByteArrayUtils.AsciiHexToBinary(masterKeyBin, masterKey);
			KeySpec ks = new DESKeySpec(masterKeyBin);
			SecretKeyFactory kf = SecretKeyFactory.getInstance("DES");
			SecretKey key = kf.generateSecret(ks);
			String algorithm = "DES/ECB/NoPadding";
			Cipher cryptDesProvider = Cipher.getInstance(algorithm);
			cryptDesProvider.init(Cipher.DECRYPT_MODE, key);
			byte[] dataOut = cryptDesProvider.doFinal(dataIn);
			ret = ByteArrayUtils.getHexStr(dataOut, dataOut.length);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return ret;
	}
	
	public static void openPassword(ISO8583RouterTransaction message, String masterKey) {
		// bit 052
		String pin = message.getPassword();
		// bit 002, 045
		String pan = CardConf.extractPan(message);
		String pinOpenedHex = decryptDes(masterKey, pin);
		String lastDigitsPanBeforeDv = CardConf.getLastDigitsPanBeforeDv(pan, true);
		String xorHexAscii = ByteArrayUtils.xorHexAscii(pinOpenedHex, lastDigitsPanBeforeDv);
		System.out.println("masterKey              = " + masterKey);
		System.out.println("pan                    = " + pan);
		System.out.println("pinOpenedHex           = " + pinOpenedHex);
		System.out.println("lastDigitsPanBeforeDv  = " + lastDigitsPanBeforeDv);
		System.out.println("xorHexAscii            = " + xorHexAscii);
		String password = null;
		
		if (xorHexAscii != null && xorHexAscii.length() == 16) {
			password = xorHexAscii.substring(2, 6);
		}
		
		message.setPassword(password);
	}

}
