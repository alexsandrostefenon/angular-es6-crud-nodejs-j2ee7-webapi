package org.domain.computerVision;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.domain.commom.ByteArrayUtils;
import org.domain.commom.LoggerText;
import org.domain.commom.RefDouble;
import org.domain.computerVision.Blobs.Blob;

public class ImageClassificator implements FilenameFilter {
	private class FilterDirectories implements FilenameFilter {
		public boolean accept(File dir, String name) {
			boolean ret = false;
			File candidate = new File(dir + File.separator + name);
			if (candidate.isDirectory() == true)
				ret = true;
			candidate = null;
			return ret;
		}
	}
	
	private int width;
	private int height;
	// se true, ignora o recognize de simbolos abaixo da probabilidade de certeza das amostras que geraram os pesos.
	private boolean checkMinSure;
	private String[] symbols;
	private Image[] imgRefMean;
	private Image[] imgRefDesviation;
	// menor valor encontrado nas imagens que geraram os pesos de cada símbolo.
	private double[] minSymbolSures;
	// maior valor encontrado nas imagens dos outros grupos de símbolos.
	private double[] maxConflictSures;
	private double[] responses;
	// está sendo utilizada somente no método Recognize (blobs).
	private Image imgOcr;
	// imagem auxiliar para o método messure
	private Image imgDiff;
	// formato de arquivamento das imagens.
	private String imgFormat;
	private LoggerText logger;
	private int numChannels;
	
	public void setCheckMinSure(boolean value) {
		this.checkMinSure = value;
	}
		
	public void dispose() {
		this.symbols = null;
		this.imgRefMean = null;
		this.imgRefDesviation = null;
		this.responses = null;
		this.minSymbolSures = null;
		this.maxConflictSures = null;
		if (imgOcr != null) {
			imgOcr.dispose ();
			imgOcr = null;
		}
	}
	
	public void saveWeights(String filename) throws IOException {
		int imgSize = this.width * this.height * this.numChannels;
		// depois é só salvar o arquivo de pesos.
		StringBuffer buffer = new StringBuffer (symbols.length*imgSize*2+10240);
		// começa adicionando uma linha para os dígitos, uma coluna para cada dígito.
		for (int i = 0; i < symbols.length; i++) {
			buffer.append (symbols[i].charAt(0));
			if (i == symbols.length-1)
				buffer.append ("\n");
			else
				buffer.append ("\t");
		}
		
		buffer.append (this.width);
		buffer.append ("\n");
		buffer.append (this.height);
		buffer.append ("\n");
		buffer.append (this.numChannels);
		buffer.append ("\n");
		// depois adiciona as informações de limite de certeza para cada dígito.
		if (minSymbolSures != null) {
			for (int i = 0; i < symbols.length; i++) {
				buffer.append (minSymbolSures[i]);
				if (i == symbols.length-1)
					buffer.append ("\n");
				else
					buffer.append ("\t");
			}
		}
		// depois adiciona as informações de limite de conflito de certeza para cada dígito.
		if (maxConflictSures != null) {
			for (int i = 0; i < symbols.length; i++) {
				buffer.append (maxConflictSures[i]);
				if (i == symbols.length-1)
					buffer.append ("\n");
				else
					buffer.append ("\t");
			}
		}
		// adiciona os pesos para cada dígito.
		for (int digit = 0; digit < symbols.length; digit++) {
			this.imgRefMean[digit].AddAsciiHex(buffer);
			buffer.append ("\n");
			this.imgRefDesviation[digit].AddAsciiHex(buffer);
			buffer.append ("\n");
		}

		FileWriter file = new FileWriter(filename);
		file.write(buffer.toString());
		file.close();
		file = null;
		buffer = null;
	}
/*
	public void exportWeightsToSource(String filename) throws IOException {
		File file = new File(filename);
		String nameAndExtension = file.getName();
		String name = nameAndExtension.substring(0, nameAndExtension.indexOf('.'));
		file = null;
		StringBuilder buffer = new StringBuilder (symbols.length*width*height*10);
		Formatter formatter = new Formatter(buffer, Locale.US);
		// começa adicionando uma linha para os dígitos.
		buffer.append ("char symbols_" + name + "[] = {");
		for (int i = 0; i < symbols.length; i++) {
			buffer.append(symbols[i].charAt(0));
			if (i < symbols.length-1)
				buffer.append (',');
		}
		buffer.append ("};\n\n");
		// depois adiciona as mínimas certezas.
		if (minSymbolSures != null) {
			buffer.append ("char minSymbolSures_" + name + "[] = {");
			for (int i = 0; i < symbols.length; i++) {
				formatter.format("'%0'", minSymbolSures[i]);
				if (i < symbols.length-1)
					buffer.append (',');
			}
			buffer.append ("};\n\n");
		}
		// depois adiciona os limiares de conflitos com símbolos de outros grupos.
		if (maxConflictSures != null) {
			buffer.append("char maxConflictSures_" + name + "[] = {");
			for (int i = 0; i < symbols.length; i++) {
				formatter.format("'%0'", maxConflictSures[i]);
				if (i < symbols.length-1)
					buffer.append (',');
			}
			buffer.append ("};\n\n");
		}
		// depois adiciona os pesos.
		int size = weights.length;
		buffer.append("double weights_" + name + "[] = {");
		for (int digit = 0, pos = 0; digit < symbols.length; digit++) {
			for (int row = 0; row < height; row++) {
				for (int col = 0; col < width; col++, pos++) {
					formatter.format("'%0'", weights[pos]);
					if (pos < size-1)
						buffer.append(',');
				}
				buffer.append("\n");
			}
		}
		buffer.append ("};\n\n");
		FileWriter writer = new FileWriter(filename);
		writer.write(buffer.toString ());
		writer.close();
		writer = null;
		buffer = null;
	}
*/	
	/// <summary>Gera os pesos para um determinado padrão</summary>
	/// <param name="digit">Índice do modelo</param>
	/// <param name="path">Pasta raíz das imagens dos modelos</param>
	/// <param name="mask">
	/// Imagem da máscara do fundo comum a todos os modelos.
	/// Os pixeis sob esta máscara não são considerados<see cref="RGB32Image"/>
	/// </param>
	private void generateWeights(int model, String path, List<Image> list) throws Exception {
		Image imgMean = new Image (this.imgOcr, false);
		// imagens auxiliares para gerar o disvio padrão
		Image imgMeanOfSquare = new Image (this.imgOcr, false);
		Image imgMeanOfSquareAux = new Image (this.imgOcr, false);
		Rect rect = imgMean.getRect();
		File dir = new File(path + File.separator + symbols[model]);
		// carrega todas as imagens que vão compor a média
		if (dir.exists() == true) {
			String[] files = dir.list(this);
			Image imgLoad = new Image (this.imgOcr, false);
			
			for (int i = 0; i < files.length; i++) {
				String filename = dir.getPath() + File.separator + files[i];
				ImageFormat.loadBMP(imgLoad, filename);
				Image img = new Image (this.imgOcr, false);
				Image.copy(imgLoad, rect, img, rect);
				list.add(img);
			}
		}
		// calcula a soma, para depois gerar a média
		for (int i = 0; i < list.size(); i++) {
			Image img = list.get(i);
			Image.oper(imgMean, rect, img, rect, Image.OPER_ADD);
			imgMeanOfSquareAux.assign(img);
			// eleva ao quadrado
			Image.oper(imgMeanOfSquareAux, rect, imgMeanOfSquareAux, rect, Image.OPER_MUL);
//			System.out.printf("1 - %d - imgMeanOfSquareAux(0, 9) = %d\n", i, imgMeanOfSquareAux.getBright(0, 9));
			Image.oper(imgMeanOfSquare, rect, imgMeanOfSquareAux, rect, Image.OPER_ADD);
//			System.out.printf("2 - %d - imgMeanOfSquare(0, 9) = %d\n", i, imgMeanOfSquare.getBright(0, 9));
		}
		// a média é a soma das imagens, dividido pela quantidade de imagens
		Image.oper(imgMean, rect, list.size(), Image.OPER_DIV);
		this.imgRefMean[model] = imgMean;
		// a variância é a média dos quadrados menos o quadrado da média
		Image.oper(imgMeanOfSquare, rect, list.size(), Image.OPER_DIV);
//		System.out.printf("3 - imgMeanOfSquare(0, 9) = %d\n", imgMeanOfSquare.getBright(0, 9));
		// quadrado da média
		Image imgSquareOfMean = new Image (imgMean, true);
		Image.oper(imgSquareOfMean, rect, imgSquareOfMean, rect, Image.OPER_MUL);
//		System.out.printf("4 - imgSquareOfMean(0, 9) = %d\n", imgSquareOfMean.getBright(0, 9));
		// a variância é a média dos quadrados menos o quadrado da média
	    // double var = mean2-_mean*_mean;
		Image imgMeanDesviation = new Image (imgMeanOfSquare, true);
		Image.oper(imgMeanDesviation, rect, imgSquareOfMean, rect, Image.OPER_SUB);
//		System.out.printf("5 - imgMeanDesviation(0, 9) = %d\n", imgMeanDesviation.getBright(0, 9));
		// o desvio padrão é a raiz quadrada da variância
	    // double desviation = Math.sqrt(var);
		Image.oper(imgMeanDesviation, rect, 0, Image.OPER_SQRT);
		this.imgRefDesviation[model] = imgMeanDesviation;
//		System.out.printf("imgMean.getBright(0, 9) = %d\n", imgMean.getBright(0, 9));
//		System.out.printf("imgMeanDesviation(0, 9) = %d\n", imgMeanDesviation.getBright(0, 9));
		// DEBUG
//*
		try {
			String filename1 = String.format("/tmp/imgMean_%s.bmp", this.symbols[model]);
			ImageFormat.saveBMP(this.imgRefMean[model], filename1);
			String filename2 = String.format("/tmp/imgDesviation_%s.bmp", this.symbols[model]);
			ImageFormat.saveBMP(this.imgRefDesviation[model], filename2);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//*/				
		// double prob = 255 - desviation
//		Image.oper(imgMeanWeight, rect, imgMeanWeight.getMaxValueByChannel(), Image.OPER_SUB_INV);
		// calculo do menor valor no próprio conjunto de elementos.
		minSymbolSures[model] = 1.0;
		
		for (int i = 0; i < list.size(); i++) {
			Image img = list.get(i);
			double weight = messure(img, imgMean, imgMeanDesviation, this.imgDiff);
			
			if (weight < minSymbolSures[model]) {
				minSymbolSures[model] = weight;
			}
		}
	}
	
	/// <summary>
	/// se a quantidade percuntual de ocorrências for maior que threshould, então é fundo comum,
	/// e pinta na máscara.
	/// </summary>
/*
	Image generateMask(String path, double threshould) throws IOException {
		int imgSize = width * height;
		// utiliza as informações de média e desvio padrão para a decisão de ser máscara.
		int[] numFG = new int[imgSize];
		int[] numBG = new int[imgSize];
		for (int pos = 0; pos < imgSize; pos++) {
			numFG[pos] = 0;
			numBG[pos] = 0;
		}
		int numFiles = 0;
		for (int digit = 0; digit < symbols.length; digit++) {
			File dir = new File(path + File.separator + symbols[digit]);
			if (dir.exists() == true) {
				String[] files = dir.list(this);
				for (int i = 0; i < files.length; i++) {
					String filename = files[i];
					numFiles ++;
					Image img = new Image (filename);
					if (img.getWidth() == this.width && img.getHeight() == this.height) {
						for (int pos = 0; pos < imgSize; pos++) {
							if (img.bright(pos) == 0.0)
								numBG[pos] ++;
							else
								numFG[pos] ++;
						}
					}
					img.dispose ();
					img = null;
				}
				files = null;
			}
			dir = null;
		}
		Image mask = new Image (this.width, this.height, 1);
		// toma a decisão de quem é mascara e quem não é.
		int max = (int) (threshould * numFiles);
		try {
			for (int pos = 0; pos < imgSize; pos++) {
				if (numFG[pos] >= max || numBG[pos] >= max)
					mask.setPixel(pos, (byte)0xff);
				else
					mask.setPixel(pos, (byte)0x00);
			}
		} catch (Exception exc) {
			// o tratamento de exceção é obrigatório, pois o método setPixel() alerta incompatilidade com o número de canais.
		}
		numFG = null;
		numBG = null;
		return mask;
	}
*/
	private boolean isConflict(int model, String name, String filename, RefDouble conflictWeight, RefDouble bestCorrectWeight, Image img) throws IOException {
		boolean ret = false;
		
		if (img == null) {
			img = new Image(filename);
		}
		
		char correctChar = name.charAt(0); 
		recognize(img, null);
		conflictWeight.value = this.responses[model];
		bestCorrectWeight.value = -1.0;
		
		for (int i = 0; i < this.symbols.length; i++) {
			char ch = this.symbols[i].charAt(0);
			
			if (ch == correctChar) {
				double weight = this.responses[i];
				
				if (weight > bestCorrectWeight.value) {
					bestCorrectWeight.value = weight;
				}
			}
		}
		
		if (conflictWeight.value > bestCorrectWeight.value) {
			ret = true;
		}
		
		return ret;
	}
	
	private void generateWeights(String path, boolean moveConflicts) throws Exception {
		List<List<Image>> lists = new ArrayList<List<Image>>(this.symbols.length);
		// calcula os pesos para todos os digitos.
		for (int model = 0; model < symbols.length; model++) {
			List<Image> list = new ArrayList<Image>(1024);
			lists.add(list);
			System.out.print("Gerando pesos para " + symbols[model] + "\n");
			generateWeights(model, path, list);
			System.out.printf("minSymbolSures[%s] = %s\n", symbols[model], this.minSymbolSures[model]);
		}
		// verifica os limites de conflito com os outros dígitos de outros grupos.
		StringBuffer buffer = new StringBuffer (1024);
		buffer.append("symbol\tconflict file\tconflictWeight\tbestCorrectWeight\tmaxConflictSures[model]\tminSymbolSures[model]\n");
		
		for (int model = 0; model < symbols.length; model++) {
			maxConflictSures[model] = -1.0;
			
			for (int other = 0; other < symbols.length; other++) {
				File dir = new File(path + File.separator + symbols[other]);
				// não testa contra o próprio grupo.
				if (dir.exists() == true && symbols[model].charAt(0) != symbols[other].charAt(0)) {
					List<Image> list = lists.get(other);
					System.out.printf("Testando conflitos de %s contra %s\n", symbols[model], symbols[other]);
					String[] files = dir.list(this);
					
					for (int i = 0; i < files.length; i++) {
						Image img = list.get(i);
						String filename = dir.getPath() + File.separator + files[i];
						RefDouble conflictWeight = new RefDouble();
						RefDouble bestCorrectWeight = new RefDouble();
						// verifica se é um conflito real, isto é, se este modelo (digit) reconhece melhor que os próprios modelos (other)
						if (isConflict(model, files[i], filename, conflictWeight, bestCorrectWeight, img)) {
							if (conflictWeight.value > this.maxConflictSures[model]) {
								this.maxConflictSures[model] = conflictWeight.value;
							}
							
							buffer.append(String.format("%s\t%s\t%f\t%f\t%f\t%f\n", symbols[model], files[i], conflictWeight.value, bestCorrectWeight.value, this.maxConflictSures[model], minSymbolSures[model]));
							System.out.print((String.format("%s\t%s\t%f\t%f\t%f\t%f\n", symbols[model], files[i], conflictWeight.value, bestCorrectWeight.value, this.maxConflictSures[model], minSymbolSures[model])));
						}
					}
					
					files = null;
				}
			}
		}

		RefDouble weight = new RefDouble();
		FileOutputStream fosConflicts = new FileOutputStream(path + File.separator + "conflicts.tsv");
		fosConflicts.write(buffer.toString().getBytes());
		fosConflicts.close();
		fosConflicts = null;
		buffer.setLength(0);
		// move os dígitos conflituosos para uma subpasta.
		if (moveConflicts == true) {
			for (int model = 0; model < symbols.length; model++) {
				List<Image> list = lists.get(model);
				System.out.print("Movendo arquivos de baixo acerto para " + symbols[model]);
				File dir = new File(path + File.separator + symbols[model]);
				File dirMove = new File(path +File.separator + symbols[model] + "_conflicts");
				
				if (dirMove.exists() == false) {
					dirMove.mkdir();
				}
				
				String[] files = dir.list(this);
				
				for (int i = 0; i < files.length; i++) {
					Image img = list.get(i);
					
					if (img == null) {
						String filename = dir.getPath() + File.separator + files[i];
						img = new Image (filename);
					}
					
					weight.value = messure(img, this.imgRefMean[model], this.imgRefDesviation[model], this.imgDiff);
					
					if (weight.value <= maxConflictSures[model]) {
						// move para a pasta unclassified
						String filename = dir.getPath() + File.separator + files[i];
						File file = new File(filename);
						File fileDest = new File(dirMove.getAbsolutePath() + File.separator + files[i]);
						file.renameTo(fileDest);
						buffer.append(String.format("%s\t%s\t%f\t%f\n", symbols[model], files[i], weight, maxConflictSures[model]));
					}
				}
				
				files = null;
			}
		}

		FileOutputStream fosWarnings = new FileOutputStream(path + File.separator + "moved_list.tsv");
		fosWarnings.write(buffer.toString().getBytes());
		fosWarnings.close();
		fosWarnings = null;
		buffer = null;
	}
	
	private double messure(Image img, Image imgMean, Image imgDesviation, Image imgDiffOfMean) {
		try {
			imgDiffOfMean.assign(imgMean);
			Rect rect = imgMean.getRect();
			// calcula a diferença da média
			Image.oper(imgDiffOfMean, rect, img, rect, Image.OPER_DIFF_ABS);
			// calcula a proximidade da média
//			Image.oper(imgAux, rect, imgAux.getMaxValueByChannel(), Image.OPER_SUB_INV);
			// pondera com a informação do desvio padrão
//			Image.oper(imgDiffOfMean, rect, imgDesviation, rect, Image.OPER_MUL);
			Image.oper(imgDiffOfMean, rect, imgDiffOfMean.getMaxValueByChannel(), Image.OPER_MUL);
			// normaliza de 0 à 255
//			Image.oper(imgAux, rect, imgAux.getMaxValueByChannel(), Image.OPER_DIV);
			Pixel pixelMean = new Pixel(imgDiffOfMean);
			imgDiffOfMean.mean(rect, pixelMean.values);
			// normaliza de 0.0 à 1.0
			double maxValByChannel2 = imgDiffOfMean.getMaxValueByChannel();
			maxValByChannel2 *= maxValByChannel2;
			double ret = maxValByChannel2 - (double) pixelMean.getBright();
			ret /= maxValByChannel2;
			
			if (ret < 0.0 || ret > 1.0) {
				System.out.println("erro");
			}
			
			return ret;
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			return 0.0;
		}
	}
	
	public char recognize(Image img, RefDouble weight) {
		double bestWeight = -1.0;
		char ret = '?';
		// retorna a melhor resposta
		for (int model = 0; model < symbols.length; model++) {
			this.responses[model]  = messure(img, this.imgRefMean[model], this.imgRefDesviation[model], this.imgDiff);
			
			if (this.responses[model] > bestWeight) {
				if (this.checkMinSure == false || this.responses[model] >= maxConflictSures[model]) {
					bestWeight = this.responses[model];
					String str = this.symbols[model];
					ret = str.charAt(0);
				}
			}
		}
		
		if (weight != null) {
			weight.value = bestWeight;
		}
		
		return ret;
	}

	public char recognize(Image img, String candidates, RefDouble weight) {
		// retorna a melhor resposta
		weight.value = -1.0;
		char ret = '?';
		for (int candidate = 0; candidate < candidates.length(); candidate++) {
			for (int model = 0; model < symbols.length; model++) {
				if (candidates.charAt(candidate) == symbols[model].charAt(0)) {
					responses[model]  = messure (img, this.imgRefMean[model], this.imgRefDesviation[model], this.imgDiff);
					
					if (responses[model] > weight.value) {
						if (checkMinSure == false || responses[model] >= maxConflictSures[model]) {
							weight.value = responses[model];
							String str = symbols[model];
							ret = str.charAt(0);
						}
					}
				}
			}
		}
		return ret;
	}

	public char recognize(Blob blob, String candidates, RefDouble weight) {
		blob.makeSample (imgOcr);
		return recognize (imgOcr, candidates, weight);
	}

	public String recognize(Blobs blobs) {
		StringBuffer buffer = new StringBuffer(blobs.size());
		RefDouble weight = new RefDouble();
		for (int i = 0; i < blobs.size(); i++) {
			blobs.getBlob(i).makeSample(imgOcr);
			buffer.append(recognize(imgOcr, weight));
		}
		return buffer.toString ();
	}

	public String recognizeLines(ArrayList<ArrayList<Blob>> lines) {
		StringBuilder buffer = new StringBuilder(1024);
		RefDouble weight = new RefDouble();
		
		for (int j = 0; j < lines.size(); j++) {
			ArrayList<Blob> line = lines.get(j);
			
			for (int i = 0; i < line.size(); i++) {
				Blob blob = (Blob) line.get(i);
				blob.makeSample(imgOcr);
				char ch = recognize(imgOcr, weight);
				// DEBUG
/*
				try {
					String filename = String.format("/tmp/ch_%s-line_%d-col_%d.bmp", ch, j, i);
					ImageFormat.saveBMP(imgOcr, filename);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
*/				
				buffer.append(ch);
			}
			
			if (j < lines.size()-1) {
				buffer.append ('\n');
			}
		}
		
		String ret = buffer.toString ();
		buffer = null;
		return ret;
	}

	private void initialize(int width, int height, int numChannels, String[] symbols, Image[] weights, Image[] desv) {
		this.logger = null;
		this.imgFormat = "bmp";
		this.checkMinSure = false;
		this.width = width;
		this.height = height;
		this.numChannels = numChannels;
		this.imgOcr = new Image (this.width, this.height, this.numChannels, 32);
		this.imgDiff = new Image(this.width, this.height, this.numChannels, 32);
		this.minSymbolSures = null;
		this.maxConflictSures = null;
		this.symbols = symbols;
		this.imgRefMean = weights;
		this.imgRefDesviation = desv;
		this.responses = new double[symbols.length];
	}
/*	
	public ImageClassificator(String[] symbols, int width, int height, double[] weights) {
		initialize(width, height, symbols, weights);
	}
*/
	public ImageClassificator(String path, int width, int height, int numChannels, boolean splitConflicts) throws Exception {
		File dir = new File(path);
		FilterDirectories filterDirectories = new FilterDirectories();
		// deixa somente o nome dos subdiretórios (não o path completo).
		this.symbols = dir.list(filterDirectories);
		filterDirectories = null;
		java.util.Arrays.sort(this.symbols);
		this.imgRefMean = new Image[symbols.length];
		this.imgRefDesviation = new Image[symbols.length];
		initialize(width, height, numChannels, symbols, imgRefMean, imgRefDesviation);
		this.minSymbolSures = new double[symbols.length];
		this.maxConflictSures = new double[symbols.length];
		generateWeights(path, splitConflicts);
	}
	
	public ImageClassificator(String filename, LoggerText logger) throws IOException {
		RandomAccessFile file = new RandomAccessFile(filename, "r");
		this.symbols = file.readLine().split("\t");
		this.width = Integer.parseInt(file.readLine());
		this.height = Integer.parseInt(file.readLine());
		this.numChannels = Integer.parseInt(file.readLine());
		this.imgRefMean = new Image[symbols.length];
		this.imgRefDesviation = new Image[symbols.length];
		initialize(width, height, numChannels, symbols, imgRefMean, imgRefDesviation);
		this.logger = logger;
		this.minSymbolSures = new double[symbols.length];
		this.maxConflictSures = new double[symbols.length];
		String[] cols = file.readLine().split("\t");
		
		for (int digit = 0; digit < symbols.length; digit++) {
			this.minSymbolSures[digit] = Double.parseDouble(cols[digit]);
		}
		
		cols = file.readLine().split ("\t");
		
		for (int digit = 0; digit < symbols.length; digit++) {
			this.maxConflictSures[digit] = Double.parseDouble(cols[digit]);
		}
				
		for (int model = 0; model < symbols.length; model++) {
			String asciiHex = file.readLine();
			byte[] data = new byte[this.width * this.height * this.numChannels]; 
			ByteArrayUtils.AsciiHexToBinary(data, asciiHex);
			Image imgRef = new Image(width, height, numChannels, 32);
			this.imgRefMean[model] = imgRef;
			imgRef.aquire(width, height, numChannels, width, data);
			//
			asciiHex = file.readLine();
			ByteArrayUtils.AsciiHexToBinary(data, asciiHex);
			Image imgDesv = new Image(width, height, numChannels, 32);
			this.imgRefDesviation[model] = imgDesv;
			imgDesv.aquire(width, height, numChannels, width, data);
//			// DEBUG
//			try {
//				ImageFormat.saveBMP(imgRef, "/tmp/imgRef.bmp");
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}
		
		file.close();
	}
		
	private void log(String string) {
		if (this.logger != null) {
//			this.logger.log(string);
		}
	}

	public boolean accept(File arg0, String arg1) {
		boolean ret = false;
		if (arg1.indexOf(this.imgFormat) >= 0) {
			ret = true;
		}
		return ret;
	}
}
