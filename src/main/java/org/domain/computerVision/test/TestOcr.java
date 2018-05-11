package org.domain.computerVision.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.domain.commom.Logger;
import org.domain.commom.ModuleManager;
import org.domain.computerVision.ImageClassificator;
import org.domain.computerVision.ocr.Ocr;

public class TestOcr {
	public static final int CMD_UNKNOW = -1;
	public static final int CMD_MANAGER_START = 0;
	public static final int CMD_MANAGER_STOP = 1;
	public static final int CMD_MANAGER_SHUTDOWN = 2;
	public static final int CMD_OCR_EXECUTE = 3;
	public static final int CMD_OCR_THRESHOLD = 4;
	public static final int CMD_OCR_GENERATE_DIGITS = 5;
	public static final int CMD_OCR_GENERATE_WEIGHTS = 6;
	public static final int CMD_OCR_RECOGNIZE = 7;
	private ModuleManager manager;
	private String workDir;
	public Ocr ocrParam;
    
	private void loadImage(String filename) throws IOException {
		// TODO : converter para BMP
		File file = new File(filename);
	    FileInputStream strm = new FileInputStream(file);
    	byte[] bufferBMP = new byte[(int) file.length()];
	    strm.read(bufferBMP, 0, (int) file.length());
	    strm.close();
	    strm = null;
	    file = null;
		this.ocrParam.imgs.add(bufferBMP);
	}

	private void splitDigits(String plugin, File[] files) throws Exception {
		setModule(plugin);
		ocrParam.cmd = Ocr.CMD_GENERATE_DIGITS;
		
		for (File file : files) {
			String filename = file.getName();
			int end = filename.lastIndexOf("_");
			
			if (end < 0) {
				end = filename.lastIndexOf(".");
			}
			
			if (end >= 0) {
				ocrParam.symbols = filename.substring(0, end);
			} else {
				ocrParam.symbols = filename;
			}
			
			ocrParam.pathDigits = file.getParent() + File.separator + "symbols";
			loadImage(file.getAbsolutePath());
			this.manager.execute(plugin, ocrParam);
		}
	}

	private void recognize(String plugin, File[] files) throws IOException {
		setModule(plugin);
		int numCorrect = 0;
		int numAll = 0;
		String[] real = new String[files.length];
		String[] responses = new String[files.length];
		ocrParam.cmd = Ocr.CMD_RECOGNIZE;
		long tIni = System.currentTimeMillis();
		
		for (int i = 0; i < files.length; i++) {
			this.ocrParam.imgs.clear();
			File file = files[i];
			String ans = null;
			StringBuilder buffer = new StringBuilder(file.getName());
			int end = buffer.indexOf("_");
			
			if (end < 0) {
				end = buffer.lastIndexOf(".");
			}
			
			if (end > 0) {
				buffer.setLength(end);
			}
			
			String ansCorrect = buffer.toString();
			
			try {
				try {
					loadImage(file.getAbsolutePath());
					this.manager.execute(plugin, ocrParam);
					ans = ocrParam.text;
				} catch (Exception e) {
					ans = null;
					e.printStackTrace();
				}
				
				if (ocrParam.caseSensitive == false) {
					ans = ans.toUpperCase();
					ansCorrect = ansCorrect.toUpperCase();
				}
				
				real[i] = ansCorrect;
				responses[i] = ans;
			} catch (IllegalArgumentException ex) {
				System.err.printf("Erro de parâmetros ao processar imagem, mensagem :\n" + ex.getMessage());
				continue;
			} catch (Exception ex) {
				System.err.printf("Erro desconhecido ao processar imagem, mensagem :\n" + ex.getMessage());
				continue;
			}
			
			if (ans.equals(ansCorrect)) {
				numCorrect++;
			}
			
			numAll++;
			String msg = String.format ("%03d/%03d - %05.1f - Answer = %s", numCorrect, numAll, (double)((numCorrect * 100) / numAll), ans);
			this.manager.log(Logger.LOG_LEVEL_INFO, "recognize", msg, ocrParam);
		}
		
		long time = System.currentTimeMillis() - tIni;
		String msg = String.format ("rate = %f images per secound", (double)(1000*numAll)/ (double)time);
		this.manager.log(Logger.LOG_LEVEL_INFO, "recognize", msg, ocrParam);
		report(real, responses);
		real = null;
		responses = null;
	}

	private void recognize(String plugin, byte[] bufferBMP) throws IOException {
		setModule(plugin);
		ocrParam.cmd = Ocr.CMD_RECOGNIZE;
		this.ocrParam.imgs.clear();
		this.ocrParam.imgs.add(bufferBMP);
		long tIni = System.currentTimeMillis();
		
		try {
			try {
				this.manager.execute(plugin, ocrParam);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (IllegalArgumentException ex) {
			System.err.printf("Erro de parâmetros ao processar imagem, mensagem :\n" + ex.getMessage());
		} catch (Exception ex) {
			System.err.printf("Erro desconhecido ao processar imagem, mensagem :\n" + ex.getMessage());
		}
		
		long time = System.currentTimeMillis() - tIni;
		String msg = String.format ("rate = %f images per secound", (double)(1000)/ (double)time);
//		log(msg);
	}

	private void report(String[] reals, String[] responses) throws IOException {
		// monta um vetor com todas as letras ocorridas
		int[][] matriz = new int[256][256];
		
		for (int i = 0; i < reals.length; i++) {
			String real = reals[i];
			String response = responses[i];
			
			for (int j = 0; j < real.length() && j < response.length(); j++) {
				int col = real.charAt(j);
				int row = response.charAt(j);
				matriz[col][row]++;
			}
		}
		
		int[] digits = new int[256];
		int numDigits = 0;
		
		for (int col = 0; col < 256; col++) {
			for (int row = 0; row < 256; row++) {
				if (matriz[col][row] != 0) {
					digits[numDigits++] = col;
					break;
				}
			}
		}
		
		FileOutputStream file = new FileOutputStream(this.workDir + File.separator + "matriz.tsv");
		// imprime o cabeçalho
		for (int i = 0; i < numDigits; i++) {
			file.write('\t');
			file.write(digits[i]);
		}
		
		file.write('\n');
		
		for (int row = 0, i = 0; row < 256; row++) {
			if (row == digits[i]) {
				i++;
				file.write(row);
				file.write('\t');
				
				for (int col = 0, j = 0; col < 256; col++) {
					if (col == digits[j]) {
						j++;
						String str = Integer.toString(matriz[col][row]);
						file.write(str.getBytes());
						file.write('\t');
					}
				}
				
				file.write('\n');
			}
		}
		
		file.close();
		file = null;
	}

	private void generateWeights(String plugin) throws Exception {
		setModule(plugin);
		// faz o backup do último arquivo de pesos utilizado.
		File file = new File(ocrParam.classifierPath);
		
		if (file.exists() == true) {
			File backup = new File(file.getName() + "_" + System.currentTimeMillis() + ".tsv");
			file.renameTo(backup);
		}
		
		String symbolsDir = this.workDir + File.separator + plugin + File.separator + "symbols";
		ImageClassificator classificator = new ImageClassificator (symbolsDir, ocrParam.generateDigitWidth, ocrParam.generateDigitHeight, ocrParam.generateDigitChannels, /*0.0,*/ false);
		classificator.saveWeights(ocrParam.classifierPath);
		classificator.dispose ();
		classificator = null;
	}

	private void setModule(String plugin) throws IOException {
		// verfica se existe o arquivo com os padrões do classificador
		this.ocrParam.classifierPath = this.workDir + File.separator + plugin + ".tsv";
//          this.logger = new Logger(workDir + "log_" + System.currentTimeMillis() + ".txt");
		this.ocrParam.logname = this.workDir + File.separator + plugin + ".txt"; 
	}
	
	private void convertToBMP(File[] files) {
/*		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			String filename = file.getAbsolutePath();
			
			if (filename.endsWith(".bmp") == false && filename.endsWith(".BMP") == false) {
				BufferedImage img = null;
				
				try {
				    img = ImageIO.read(file);
					filename = filename.substring(0, filename.lastIndexOf('.')) + ".bmp";
					file = new File(filename);
				    ImageIO.write(img, "bmp", file);
					files[i] = file;
				} catch (IOException e) {
					files[i] = null;
				}
			}
		}*/
	}
	
	public void execute(int cmd, String[] plugins, File[] files, byte[] bufferBMP, String dataPath) {
		this.workDir = dataPath;
		convertToBMP(files);
		this.ocrParam.imgs.clear();
		
		try {
			if (cmd == CMD_OCR_EXECUTE) {
				for (String plugin : plugins) {
					this.manager.execute(plugin, null);
				}
			} else if (cmd == CMD_OCR_THRESHOLD) {
				this.ocrParam.cmd = Ocr.CMD_THRESHOLD;
				
				for (String plugin : plugins) {
					for (File file : files) {
						setModule(plugin);
						loadImage(file.getAbsolutePath());
						this.manager.execute(plugin, this.ocrParam);
					}
				}
			} else if (cmd == CMD_OCR_GENERATE_DIGITS) {
				for (String plugin : plugins) {
					splitDigits(plugin, files);
				}
			} else if (cmd == CMD_OCR_GENERATE_WEIGHTS) {
				for (String plugin : plugins) {
					generateWeights(plugin);
				}
			} else if (cmd == CMD_OCR_RECOGNIZE) {
				for (String plugin : plugins) {
					if (files != null) {
						recognize(plugin, files);
					} else if (bufferBMP != null) {
						recognize(plugin, bufferBMP);
					}
				}
			}
		} catch (Exception exc) {
			exc.printStackTrace();
//			log(exc.getMessage());
		}
	}

	public TestOcr(ModuleManager manager) {
		this.manager = manager;
		this.ocrParam = new Ocr();
		this.ocrParam.generateDigitWidth = 16;
		this.ocrParam.generateDigitHeight = 20;
		this.ocrParam.generateDigitChannels = 1;
		this.ocrParam.imgs = new ArrayList<byte[]>();
	}

}
