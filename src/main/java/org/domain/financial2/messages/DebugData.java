package org.domain.financial2.messages;

import java.util.HashMap;

import org.domain.commom.ByteArrayUtils;
import org.domain.commom.RefInt;
import org.domain.commom.Utils;
import org.domain.iso8583router.messages.Message;

public class DebugData {
	private static HashMap<String, String> mapQuestionsHCT = new HashMap<String, String>(25);
	private static HashMap<String, String> mapTefBit59 = new HashMap<String, String>(25);
	
	private static String crackPosReceipt(String value) {
		value = value.replace("<?",     "\n\t--------------MENSAGEM PARA IMPRESSAO -------\n\t");
		value = value.replace("<R<-", "<-\n\t--------------REIMPRESSAO HABILITADA --------\n\t");
		value = value.replace("00<-",   "\n\t--------------0 VIA PARA O COMERCIO ---------\n\t");
		value = value.replace("01<-",   "\n\t--------------1 VIA PARA O COMERCIO ---------\n\t");
		value = value.replace("02<-",   "\n\t--------------2 VIAS PARA O COMERCIO --------\n\t");
		value = value.replace("<+00",   "\n\t--------------0 VIA PARA O CLIENTE ----------\n\t");
		value = value.replace("<+01",   "\n\t--------------1 VIA PARA O CLIENTE ----------\n\t");
		value = value.replace("</",     "\n\t");
		value = value.replace("<~",     "\n\t--------------VIA COMERCIO-------------------\n\t");
		value = value.replace("<=",     "\n\t--------------VIA CLIENTE -------------------\n\t");
		value = value.replace("<!",     "\n\t--------------FINAL DA IMPRESSAO ------------");
		value = value.replace("&",      "\n\t--------------FINAL DA MENSAGEM -------------");
		value = value.replace("<_",     "\n\t--------------CORTAR AQUI -------------------\n\t");
		return value;
	}
	
	private static String crackPosTag9034(String value) {
		String names[] = {
				"\tCLI_TABELA       ",
				"\tCLI_TAMANHO      ",
				"\tCLI_CODIGO       ",
				"\tCLI_BIN          ",
				"\tCLI_DATA         ",
				"\tCLI_QTDE_PROD    ",
				"\t\tCLI_COD_PRODUTO  ",
				"\t\tCLI_QTDE_PERG    ",
				"\t\t\tCLI_COD_PERGUNTAS"};
		HashMap<String, String> mapProducts = new HashMap<String, String>(25);
		mapProducts.put("1000", "Compra");
		mapProducts.put("1001", "Consulta Saldo");
		int sizes[] = {2, 4, 12, 8, 6, 2, 4, 2, 4};
		StringBuffer buffer = new StringBuffer(1999);
		String str = null;
		int offset = 0;
		
		try {
			for (int i = 0; i < 6; i++) {
				int size = sizes[i];
				str = value.substring(offset, offset + size);
				offset += size;
				buffer.append(names[i] + " : " + str + "\n");
			}
			
			if (str != null) {
				int countProducts = Integer.parseInt(str);
				int size;
				
				for (int i = 0; i < countProducts; i++) {
					// CLI_COD_PRODUTO
					size = sizes[6];
					str = value.substring(offset, offset + size);
					offset += size;
					buffer.append(String.format("%s : %s -> %s\n", names[6], str, mapProducts.get(str)));
					// CLI_QTDE_PERG
					size = sizes[7];
					str = value.substring(offset, offset + size);
					offset += size;
					buffer.append(names[7] + " : " + str + "\n");
					// CLI_COD_PERGUNTAS
					int countQuestions = Integer.parseInt(str);
					
					for (int j = 0; j < countQuestions; j++) {
						size = sizes[8];
						str = value.substring(offset, offset + size);
						offset += size;
						buffer.append(String.format("%s : %s -> %s\n", names[8], str, mapQuestionsHCT.get(str)));
					}
				}
				
				if (value.length() > offset) {
					buffer.append("ERROR : value.length() > offset\n");
				}
			}
		} catch (Exception e) {
			buffer.append("ERROR : " + e.getMessage() + "\n");
		}
		
		value = value + "\n" + buffer.toString();
		return value;
	}

	private static String crackDinamicQuestions(String data) {
		// 0 -> fixed, 1 -> L, 2 -> LL, 3 -> LLL, 4 -> LLLL
		int[] sizesTypes   = {  0,   0,   0,   2,   0,   0,   0,   0,   0,   2,   3};
		int[] maxSizes     = {  2,   3,   4,  21,   2,   2,   2,   3,   4,  20, 999};
		int[] charsBySizes = {  1,   1,   1,   1,   1,   1,   1,   1,   1,   4,   1};
//		char[] dataTypes   = {'N', 'N', 'N', 'A', 'N', 'N', 'N', 'N', 'N', 'N', 'N'};
		String names[] = {
				"\tPERG_TABELA        ",
				"\tPERG_TAMANHO       ",
				"\tPERG_CODIGO        ",
				"\tPERG_TEXTO_POS     ",
				"\tPERG_MASCARA       ",
				"\tPERG_RESP_MIN      ",
				"\tPERG_RESP_MAX      ",
				"\tPERG_RESP_CAMPO    ",
				"\tPERG_AGRUPADOR     ",
				"\tPERG_COD_PERGUNTAS ",
				"\tPERG_PARAMETROS    ",
				};
		StringBuffer buffer = new StringBuffer(1999);
		String str = null;
		RefInt offset = new RefInt();
		
		try {
			int cnt = 0;
			
			while (offset.value < data.length()) {
				buffer.append(String.format("\n\tPergunta %d :\n", ++cnt));
				
				for (int i = 0; i < names.length; i++) {
					int sizeType = sizesTypes[i];
					int size = maxSizes[i];
					
					if (sizeType > 0) {
						size = extractInt(data, offset, sizeType);
						
						if (size > maxSizes[i]) {
							throw new Exception("Invalid size");
						}
					}
					
					if (i == 9) {
						buffer.append("\tPerguntas :\n");
						
						for (int j = 0; j < size; j++) {
							str = extractStr(data, offset, 4);
							buffer.append(String.format("\t\t%s -> %s\n", str, mapQuestionsHCT.get(str)));
						}
					} else {
						size *= charsBySizes[i];
						str = extractStr(data, offset, size);
						buffer.append(names[i] + " : " + str + "\n");
					}
				}
			}
		} catch (Exception e) {
			buffer.append("ERROR : " + e.getMessage() + "\n");
		}
		
		data = buffer.toString();
		return data;
	}
	
	private static String extractStr(String data, RefInt offset, int size) {
		String ret = data.substring(offset.value, offset.value + size);
		offset.value += size;
		return ret;		
	}
	
	private static int extractInt(String data, RefInt offset, int size) {
		int value = Integer.parseInt(extractStr(data, offset, size));
		return value;
	}
	
	private static void checkValue(StringBuffer buffer, String str, String[] strings) {
		int index = Utils.findInList(strings, str);
		
		if (index < 0) {
			buffer.append(String.format("\nERRO -> Valor fora do esperado (%s), possibilidades ", str));
			
			for (String string : strings) {
				buffer.append(" - ");
				buffer.append(string);
			}
			
			buffer.append("\n");
		}
	}

	private static void printTable(StringBuffer buffer, String data, RefInt offset, String[] labels, int[] fieldSizes, int numRows, int numCols) {
		String[][] fields = new String[numRows][numCols];
		
		for (int row = 0; row < numRows; row++) {
			for (int col = 0; col < numCols; col++) {
				fields[row][col] = extractStr(data, offset, fieldSizes[col]);
			}
		}
		
		int[] colsSize  = new int[numCols];
		
		for (int col = 0; col < numCols; col++) {
			colsSize[col] = labels[col].length() + 1;
			
			if (colsSize[col] < fieldSizes[col] + 2) {
				colsSize[col] = fieldSizes[col] + 2;
			}
		}
		
		buffer.append("\n\t");
		
		for (int col = 0; col < numCols; col++) {
			buffer.append(labels[col]);
			int diff = colsSize[col] - labels[col].length();
			
			while (diff-- > 0) {
				buffer.append(' ');
			}
			
			buffer.append(" | ");
		}
		
		for (int row = 0; row < numRows; row++) {
			buffer.append("\n\t");
			
			for (int col = 0; col < numCols; col++) {
				int diff = colsSize[col] - fieldSizes[col];
				
				while (diff-- > 0) {
					buffer.append(' ');
				}
				
				buffer.append(fields[row][col]);
				buffer.append(" | ");
			}
		}
		
		buffer.append("\n");
	}
	
	private static String printVector(String data, String labels[], int sizes[], char types[]) {
		if (labels == null) {
			return data;
		}
		
		int offset = 0;
		StringBuffer buffer = new StringBuffer(1024);
		buffer.append("\n\n");
		
		for (int i = 0; i < labels.length; i++) {
			int size = sizes[i];
			
			if (offset + size > data.length()) {
				break;
			}
			
			char type = types[i];
			buffer.append(String.format("\t%03d) %s - %03d - %s :\n\t", i+1, type, size, labels[i]));
			
			if (type == 'N') {
				buffer.append(data.subSequence(offset, offset + size));
			} else if (type == 'B') {
				for (int j = 0; j < size; j++) {
					ByteArrayUtils.AddAsciiHexFromUnsignedByte(buffer, data.charAt(offset + j));
				}
			}
			
			buffer.append("\n");
			offset += size;
		}
		
		return buffer.toString();
	}
	
	private static String crackSpTransConfigParameters(String data, RefInt offset, int numCols) {
		StringBuffer buffer = new StringBuffer(1024);
		buffer.append("\n\tCartoes que permitem recarga (multipla e temporal)\n");
		int numRows = extractInt(data, offset, 3);
		String[] labels = new String[] {"Tipo Cartao", "Grupo Cartao", "Tipo Carga", "Valor", "Desconto", "Cotas", "Limite Dias", "Sigla"};
		int[] fieldSizes = new int[] {3, 1, 2, 7, 5, 2, 2, 5};
		printTable(buffer, data, offset, labels, fieldSizes, numRows, numCols);
		data = buffer.toString();
		return data;
	}

	private static String crackSpTransConfigParameters(String version, String data) {
		StringBuffer buffer = new StringBuffer(1024);
		RefInt offset = new RefInt();
		buffer.append(version);
		buffer.append("\n\tCartoes que permitem recarga de VC, VT e VE:\n");
		int numRows = extractInt(data, offset, 3);
		String[] labels = new String[] {"Tipo Cartao", "Grupo Cartao"};
		int[] fieldSizes = new int[] {3, 1};
		int numCols = 2;
		printTable(buffer, data, offset, labels, fieldSizes, numRows, numCols);
		numCols = version.equals("002") ? 5 : 8;
		buffer.append(crackSpTransConfigParameters(data, offset, numCols));
		data = buffer.toString();
		return data;
	}
	
	private static String crackSpTransRecharge(Message message, String data, String bit) {
		String labels[] = null;
		char types[] = null;
		int sizes[] = null;
		
		if (bit.equals("063")) {
			if (data.startsWith("001")) {
				labels = new String[] {
						"Versao",
						"Numero logico do cartao (vide dados de emissao)",
						"Tipo do cartao (vide dados de emissao)",
						"Grupo do cartao (vide mensagem de consulta 0205/0215)",
						"Tipo de carga a ser feita no cartao",
						"Dados de Emissao do Cartao",
						"Dados de Ultima Transacao",
						"Dados de Recarga (4 carteiras, 16 bytes cada)",
						"Dados de revalidacao do Cartao",
						"Valor da Carga, formato LITTLE-ENDIAN, (0 = indica solicitacao de recarga pre-paga por lista, ou recarga multipla)",
						"Numero fisico do cartao",
						"Dados gerais (preencher com zeros)"
				};
				
				sizes = new int[] {3, 10, 3, 1, 2, 16, 58, 64, 28, 4, 4, 6};
				types = new char[] {'N','N','N','N','N','B','B','B','B','B','B','B'};
			} else if (data.startsWith("002")) {
				labels = new String[] {
						"Versao",
						"Numero logico do cartao (vide dados de emissao)",
						"Tipo do cartao (vide dados de emissao)",
						"Grupo do cartao (vide mensagem de consulta 0205/0215)",
						"Tipo de carga a ser feita no cartao",
						"Dados de Emissao do Cartao",
						"Dados de Ultima Transacao. ( VT e VC )",
						"Dados de Recarga ( VT e VC ) (4 carteiras, 16 bytes cada)",
						"Dados de revalidacao do Cartao",
						"Dados de Ultima Transacao Estudante ( VE )",
						"Dados de Recarga Estudante ( VE ); (2 carteiras, 16 bytes cada carteira, na seguinte ordem: 3A e 3B)",
						"Dados de Ultima Transacao Temporal ( T1 e T2 )",
						"Dados de Recarga Temporal ( T1 e T2 ) (2 carteiras, 16 bytes cada carteira, na seguinte ordem: T1 e T2)",
						"Valor da Carga, formato LITTLE-ENDIAN, (0 = indica solicitacao de recarga pre-paga por lista, ou recarga multipla)",
						"Quantidade de Cota para recarga temporal",
						"Numero fisico do cartao",
						"Dados gerais (preencher com zeros)"
				};
				
				sizes = new int[] {3, 10, 3, 1, 2, 16, 58, 64, 28, 58, 32, 58, 32, 4, 5, 4, 6};
				types = new char[] {'N','N','N','N','N','B','B','B','B','B','B','B','B','B','N','B','B'};
			}
		} else if (bit.equals("062")) {
			if (data.length() != 148) {
				labels = new String[] {
						"Dados de Ultima Transacao - modificados",
						"Dados de Recarga - carteira modificada",
						"Valor da Carga, formato LITTLE-ENDIAN",
						"Carteira carregada",
						"Chave B",
						"Indicador de movimentacao de carteiras",
						"Valor do desconto em centavos",
						"Valor da carga",
						"Valor residual da carga",
						"Codigo de barras"
				};
				
				sizes = new int[] {58,16,4,1,6,1,2,7,7,44};
				types = new char[] {'B','B','B','B','B','B','B','N','N','N'};
			} else {
				labels = new String[] {
						"Dados de Ultima Transacao - modificados",
						"Dados de Recarga - carteira modificada",
						"Valor da Carga, formato LITTLE-ENDIAN",
						"Carteira carregada",
						"Chave B",
						"Indicador de movimentacao de carteiras",
						"Valor do desconto em centavos",
						"Valor da carga",
						"Valor residual da carga",
						"Tipo da carga",
						"Codigo de barras"
				};
				
				sizes = new int[] {58,16,4,1,6,1,2,7,7,2,44};
				types = new char[] {'B','B','B','B','B','B','B','N','N','N','N'};
			}
		}
		
		return printVector(data, labels, sizes, types); 
	}
	
	private static String crackTefBit59(String data) {
		StringBuffer buffer = new StringBuffer(1024);
		RefInt offset = new RefInt();
		buffer.append(data);
		buffer.append("\n\t3.1.11. Bit 59 - Dados Adicionais:\n");
		
		try {
			while (offset.value < data.length()) {
				String tag = extractStr(data, offset, 4);
				int size = extractInt(data, offset, 3);
				String value = extractStr(data, offset, size);
				buffer.append(String.format("\t%s - %s [%03d] : '%s'\n", tag, mapTefBit59.get(tag), size, value));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		data = buffer.toString();
		return data;
	}
	
	private static String crackTabelasInicializacaoTef(String value) {
		String names[] = {
				"\tIdentificador da Tabela         ",
				"\tTamanho do registro             ",
				"\tVers�o da Tabela                ",
				"\tHelp Desk Telephone             ",
				"\tTipo de Criptografia            ",
				"\tWorking Key                     ",
				"\t�ndice de Master Key            ",
				"\tConfigura��o do Terminal Byte 1 ",
				"\tConfigura��o do Terminal Byte 2 ",
				"\tConfigura��o do Terminal Byte 3 ",
				"\tCabe�alho do EC linha 1         ",
				"\tCabe�alho do EC linha 2         ",
				"\tCabe�alho do EC linha 3         ",
				"\tCNPJ do Estabelecimento         ",
				"\tLimite de Troco                 ",
				"\tNome da Rede Adquirente         ",
				"\tDTE Number 1                    ",
				"\tDTE Number 2                    ",
				"\tResponse Time Out               ",
				"\tTransaction Category Code       ",
				"\tMerchant Category Code          ",
				"\tTerminal Category Code          ",
				"\tC�digo do Pa�s                  ",
				"\tC�digo da Moeda                 ",
		};
		HashMap<String, String> mapProducts = new HashMap<String, String>(25);
		mapProducts.put("1000", "Compra");
		mapProducts.put("1001", "Consulta Saldo");
		int sizes[] = {2, 3, 7, 12, 2, 32 , 2, 2, 2, 2, 40, 40, 40, 20, 12, 20, 16, 16, 2, 1, 4, 3, 4, 4};
		StringBuffer buffer = new StringBuffer(1999);
		buffer.append("\n\t3.3. Tabelas de inicializa��o:\n");
		String str = null;
		int offset = 0;
		
		try {
			for (int i = 0; i < names.length; i++) {
				int size = sizes[i];
				str = value.substring(offset, offset + size);
				offset += size;
				buffer.append(names[i] + " : " + str + "\n");
			}
		} catch (Exception e) {
			buffer.append("ERROR : " + e.getMessage() + "\n");
		}
		
		value = value + "\n" + buffer.toString();
		return value;
	}

	private static String extractText(StringBuffer buffer, String strIni, String strEnd, boolean isIncludeStrIni, boolean isIncludeStrEnd, boolean isEraseStrIni, boolean isEraseStrEnd) {
		if (buffer == null || strIni == null || strEnd == null) {
			return null;
		}
		
		int posIniSel = buffer.indexOf(strIni);
		String str = null;
		
		if (posIniSel >= 0) {
			int posIniErase = posIniSel;
			int posEndSel = buffer.indexOf(strEnd, posIniSel + strIni.length());
			int posEndErase = posEndSel;
			
			if (posEndSel <= 0) {
				posEndErase = posEndSel = buffer.length();
				isEraseStrEnd = false;
			} else {
				if (isIncludeStrEnd) {
					posEndSel += strEnd.length();
				}
			}
			
			if (isIncludeStrIni == false) {
				posIniSel += strIni.length();
			}
			
			str = buffer.substring(posIniSel, posEndSel);
			
			if (isEraseStrIni == false) {
				posIniErase += strIni.length();
			}
			
			if (isEraseStrEnd) {
				posEndErase += strEnd.length(); 
			}
			
			buffer.delete(posIniErase, posEndErase);
		}
		
		return str;
	}
	
	static String crack(Message message, String name, String value) {
		if (mapQuestionsHCT.size() == 0) {
			mapQuestionsHCT.put("0100", "QUILOMETRAGEM");
			mapQuestionsHCT.put("0101", "LITRAGEM");
			mapQuestionsHCT.put("0102", "VALOR DO COMBUSTIVEL");
			mapQuestionsHCT.put("0103", "LITROS DE OLEO");
			mapQuestionsHCT.put("0104", "VALOR DO OLEO");
			mapQuestionsHCT.put("0105", "CODIGO MANUTENCAO");
			mapQuestionsHCT.put("0106", "VALOR MANUTENCAO");
			mapQuestionsHCT.put("0107", "MATRICULA");
			mapQuestionsHCT.put("0108", "NR. FROTA");
			mapQuestionsHCT.put("0109", "PLACA DO VEICULO");
			mapQuestionsHCT.put("0110", "LETRAS DA PLACA");
			mapQuestionsHCT.put("0111", "TIPO DE COMBUSTIVEL");
			mapQuestionsHCT.put("0112", "SENHA");
			mapQuestionsHCT.put("0113", "VALOR TOTAL");
			mapQuestionsHCT.put("0114", "REG EMPREGADO");
			mapQuestionsHCT.put("0115", "ORDEM DE SERVICO");
		}
		
		if (mapTefBit59.size() == 0) {
			mapTefBit59.put("0061", "Numero da Versao Especificacao Getnet");
			mapTefBit59.put("0062", "Versao do Aplicativo Client");
			mapTefBit59.put("0063", "Versao da Biblioteca do PinPad");
			mapTefBit59.put("0064", "Versao da Aplicacao Getnet no PinPad");
			mapTefBit59.put("0065", "Versao do Modulo TEF Getnet");
			mapTefBit59.put("0066", "Versao do Aplicativo Servidor TEF");
			mapTefBit59.put("0067", "Nome Fabricante Software TEF");
			mapTefBit59.put("0068", "CNPJ do Estabelecimento");
			mapTefBit59.put("0069", "Tipo de Criptografia Utilizada na Senha");
			mapTefBit59.put("0070", "Indice da Chave de Criptografia de PIN Utilizada");
		}	
		
		String msgType = message.getMsgType();
		String codeProcess = message.getCodeProcess();
		String codeResponse = message.getCodeResponse();
		boolean escapeBinaryData = true;
		
		if (name == null || msgType == null || codeProcess == null) {
			return ByteArrayUtils.escapeBinaryData(value, true, '?');
		}
		
		if (name.equals("data") && value != null) {
			StringBuffer buffer = new StringBuffer(value);
			String tag9006 = DebugData.extractText(buffer, "#9006@", "#", false, false, true, false); 
			String tag9007 = DebugData.extractText(buffer, "#9007@", "#", false, false, true, false); 
			String tag9034 = DebugData.extractText(buffer, "#9034@", "#", false, false, true, false);
			String receipt = DebugData.extractText(buffer, "<?", "<!", true, true, true, true);

			if (tag9006 != null) {
				buffer.append(String.format("\n\tComprovante :\n\t%s\n", crackPosReceipt(tag9006)));
				escapeBinaryData = false;
			}
			
			if (tag9007 != null) {
				buffer.append(String.format("\n\tMensagem para display :\n\t%s\n", tag9007));
				escapeBinaryData = false;
			}
			
			if (tag9034 != null) {
				buffer.append(String.format("\n\tTabela de fluxo de clientes :\n\t%s\n", crackPosTag9034(tag9034)));
				escapeBinaryData = false;
			}
			
			if (receipt != null) {
				buffer.append(String.format("\n\tComprovante :\n\t%s\n", crackPosReceipt(receipt)));
				escapeBinaryData = false;
			}
			
			if (codeResponse != null && codeResponse.equals("7") && value.startsWith("02")) {
				buffer.append(String.format("\n\tTabela de Perguntas :\n%s\n", crackDinamicQuestions(value)));
				escapeBinaryData = false;
			}
			
			value = buffer.toString();
		}
		
		if (msgType.equals("0215") && codeProcess.equals("913574") && name.equals("auth_0215_XXXXXX_063")) {
			String version = value;
			value = message.getData();
			
			if (version == null) {
			} else if (version.equals("002")) {
				value = crackSpTransConfigParameters(version, value);
				escapeBinaryData = false;
			} else if (version.equals("003")) {
				value = crackSpTransConfigParameters(version, value);
				escapeBinaryData = false;
			}
		} else if (msgType.equals("0215") && codeProcess.equals("913574") && name.equals("msgTextComplement")) {
			RefInt offset = new RefInt();
			value = crackSpTransConfigParameters(value, offset, 8);
			escapeBinaryData = false;
		} else if (msgType.equals("0200") && codeProcess.equals("913574") && name.equals("data")) {
			value = crackSpTransRecharge(message, value, "063");
			escapeBinaryData = false;
		} else if (msgType.equals("0210") && codeProcess.equals("913574") && name.equals("auth_0210_913574_063")) {
			value = crackSpTransRecharge(message, value, "063");
			escapeBinaryData = false;
		} else if (msgType.equals("0210") && codeProcess.equals("913574") && name.equals("data")) {
			value = crackSpTransRecharge(message, value, "062");
			escapeBinaryData = false;
		} else if (name.equals("transportData")) {
			value = crackTefBit59(value);
			escapeBinaryData = false;
		} else if (msgType.equals("0810") && codeProcess.equals("920000") && name.equals("data") && value.startsWith("01")) {
			value = crackTabelasInicializacaoTef(value);
			escapeBinaryData = false;
		}
		
		if (escapeBinaryData) {
			try {
				value = ByteArrayUtils.escapeBinaryData(value, false, '?');
			} catch (Exception e) {
				e.printStackTrace();
				value = null;
			}
		}
		
		return value;
	}
}
