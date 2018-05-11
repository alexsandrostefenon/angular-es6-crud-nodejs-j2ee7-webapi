package org.domain.computerVision.ocr;

import java.util.ArrayList;

import org.domain.commom.Module;
import org.domain.commom.ModuleInfo;
import org.domain.commom.RefDouble;
import org.domain.computerVision.Image;
import org.domain.computerVision.Rect;
import org.domain.computerVision.Blobs.Blob;
import org.domain.computerVision.Blobs.BlobComparer;

public class OcrLP extends OcrText implements Module {

	private void extractBlobs(Image img, int initialTolerance, int maxTolerance, int colorScale) throws Exception {
		int imgWidth = img.getWidth();
		int imgHeight = img.getHeight();
		this.imgBlobs.reset(imgWidth, imgHeight, img.getNumChannels());
		this.blobs.reset(imgWidth, imgHeight, img.getNumChannels());

		// sempre tem que resetar a lista de blobs.
		if (lines != null) {
			for (int i = 0; i < lines.size(); i++) {
				lines.get(i).clear();
			}
			
			lines.clear();
		}
		
		double factor = 1.0;//1.4
		this.minNumDigits = 7;
		this.maxNumDigits = 7;
		this.minCharWidth = (int) (6*factor);//5
		this.maxCharWidth = (int) (25*factor);//25
		this.minCharHeight = (int) (17*factor);//19
		this.maxCharHeight = (int) (35*factor);
		// vou admitir alguma inclinação
		this.maxLineHeight = (int) (40*factor);
		this.minDigitsSplit = (int) (2*factor);
		this.maxDigitsSplit = (int) (19*factor);//15//19
//		this.roi = new Rectangle (0, 100, 704, 480-100);
//		this.roi = new Rectangle (0, 200, 704, 480-200);
		double boardFactor = 1.0;//1.4
		int colorTolerance = initialTolerance;
		int minSumSize = this.minNumDigits * this.minCharHeight * this.minCharWidth;
		int regionWidth = (int) (200.0 * boardFactor);
		Rect roi = getRoi(img);
		// 1º, procura blobs com cor semelhante.
		this.blobs.setLogger(this.manager);
		this.blobs.scan(img, roi, colorBg, colorTolerance, colorScale * colorTolerance);
		// une blobs colados, de cor semelhante, até ter a quantidade mínima de candidatos válidos.
		int loop = 0;
		int step2 = 0;
		
		do {
			debugBlobs(String.format("%03d_%03d_scan_1_%03d.bmp", loop, step2++, colorTolerance));
			
			if (colorTolerance > initialTolerance) {
				// 1º, une os blobs muito parecidos, mas com limiar escolhido de tal forma que não possa unir
				// fundo com dígito ou dígito com fundo.
				blobs.clear();
//				blobs.scan(null, roi, colorBg, colorTolerance, colorScale * colorTolerance);
				blobs.scan(roi, colorBg, colorTolerance, colorScale * colorTolerance);
			}
			// 2º, remove blobs que são muito grandes para ser dígitos.
			blobs.removeBig(maxCharWidth, maxCharHeight, false);
			debugBlobs(String.format("%03d_%03d_removeBig_1_%03d.bmp", loop, step2++, colorTolerance));
			/// 3º, remove blobs que mesmo unidos aos seus vizinhos, não tem dimensões suficientes para virarem dígito.
			blobs.clear();
//			blobs.scan(null, roi, colorBg, maxTolerance, colorScale * maxTolerance);
			blobs.scan(roi, colorBg, maxTolerance, colorScale * maxTolerance);
			debugBlobs(String.format("%03d_%03d_scan_2_%03d.bmp", loop, step2++, colorTolerance));
			blobs.removeSmall(minCharWidth, minCharHeight, false);
			debugBlobs(String.format("%03d_%03d_removeSmall_1_%03d.bmp", loop, step2++, colorTolerance));
			/// 4º,
			/// remove objetos isolados que são menores que o menor objeto permitido.
			/// remove blobs que mesmo unidos aos seus vizinhos, não tem chance de virarem um grupo válido de dígitos.
			/// 1.25, pois vou aceitar até meio caracter de deslocamento vertical entre o dígito mais alto e o mais baixo,
			/// ou seja, a máxima diferença de altura entre as pontas, ex. imagem ALG 2853.
			blobs.clear();
//			blobs.scan(null, roi, colorBg, colorTolerance, colorScale * colorTolerance);
			blobs.scan(roi, colorBg, colorTolerance, colorScale * colorTolerance);
			debugBlobs(String.format("%03d_%03d_scan_3_%03d.bmp", loop, step2++, colorTolerance));
			blobs.removeGroupIsolated(48, minCharWidth, minCharHeight, maxDigitsSplit, maxCharHeight, regionWidth, (int) (1.25 * blobs.maxHeight()), minNumDigits, minSumSize);
			debugBlobs(String.format("%03d_%03d_removeGroup_1_%03d_%09d.bmp", loop, step2++, colorTolerance, blobs.size()));
			loop++;
			colorTolerance++;
		} while (blobs.size() > minNumDigits && colorTolerance <= maxTolerance);
		/// 5º, remove algum resíduo que pode ter ficado para trás.
		blobs.removeSmall(minCharWidth, minCharHeight, false);
		debugBlobs(String.format("%03d_%03d_removeSmall_2_%03d.bmp", loop, step2++, colorTolerance));
		/// 2010_03_06
		/// 6º, remove os retângulos muito sólidos, ex.: ADG 1942.
		if (blobs.size() > maxNumDigits) {
			for (int i = 0; i < blobs.size(); i++) {
				Blob blob = blobs.getBlob(i);
				int width = blob.getRect().width;
				int height = blob.getRect().height;
				
				if (width > 2 * minCharWidth) {
					double factorFilled = (double) blob.getFilledArea() / (double) (width * height);
					
					if (factorFilled > 0.85) {
						blobs.remove(i, true);
						
						if (blobs.size() > 0) {
							i--;
						}
					}
				}
			}
		}
		/// 7º,
		/// remove objetos isolados que são menores que o menor objeto permitido.
		/// remove blobs que mesmo unidos aos seus vizinhos, não tem chance de virarem um grupo válido de dígitos.
		/// 1.25, pois vou aceitar até meio caracter de deslocamento vertical entre o dígito mais alto e o mais baixo,
		/// ou seja, a máxima diferença de altura entre as pontas, ex. imagem ALG 2853.
		blobs.removeGroupIsolated(48/2, minCharWidth, minCharHeight, maxDigitsSplit, maxCharHeight, regionWidth, (int) (1.25 * blobs.maxHeight()), minNumDigits, minSumSize);
		debugBlobs(String.format("%03d_%03d_removeGroup_2_%03d.bmp", loop, step2++, colorTolerance));
		/// 2010_03_06
		/// 8º, remove os blobs de tamanho fora da média, por exemplo, o primeiro blob do estágio final da imagem HRO0276.
		if (blobs.size() > maxNumDigits) {
			int meanHeight = (int) blobs.meanHeight();
			int minHeight = (int) (0.8 * meanHeight);
			int maxHeight = (int) (1.2 * meanHeight);
			blobs.removeBig(maxCharWidth, maxHeight, false);
			blobs.removeSmall(minCharWidth, minHeight, false);
		}
		// TODO : separar os blobs em linhas e remover todos os blobs de linhas que tenham mais ou menos que 7 ítens.
		ArrayList<ArrayList<Blob>> list = blobs.getMultiline(maxLineHeight);
		
		if (list.size() > 1) {
			// varre cada linha
			for (int i = 0; i < list.size(); i++) {
				ArrayList<Blob> line = list.get(i);
				// checa se tem pelo menos sete blobs.
				if (line.size() < 7 || line.size() > 7) {
					for (int j = 0; j < line.size(); j++) {
						blobs.remove(line.get(j));
					}
				}
			}
		}
		
		list.clear ();
		list = null;
		blobs.sort(BlobComparer.SORT_BY_LeftAndWidthAndHeight);
		/// 2010_03_07
		/// 9º, truncar o top e o bottom dos blobs para o valor da média de altura (height) de todos os dígitos (ex. MMI3850).
//		blobs.adjustTopBottom(0.2, 0.0);
		//calcula a média de largura das letras.
		@SuppressWarnings("unused")
		int meanWidthLetter = maxCharWidth;
		
		if (blobs.size() >= 3) {
			meanWidthLetter = 0;
			int count = 0;
			
			for (int i = 0; i < 3; i++) {
				int width = blobs.getBlob(i).getRect().width;
				
				if (width > 2 * minCharWidth) {
					meanWidthLetter += width;
					count++;
				}
			}
			
			if (count != 0) {
				meanWidthLetter /= count;
			}
		}
		//calcula a média de largura dos números.
		@SuppressWarnings("unused")
		int meanWidthNumber = maxCharWidth;
		
		if (blobs.size() >= maxNumDigits) {
			meanWidthNumber = 0;
			int count = 0;
			
			for (int i = 3; i < maxNumDigits; i++) {
				int width = blobs.getBlob(i).getRect().width;
				
				if (width > 2 * minCharWidth) {
					meanWidthNumber += width;
					count++;
				}
			}
			
			if (count != 0) {
				meanWidthNumber /= count;
			}
		}
		
		this.blobs.saveMapColor(this.imgBlobs);
	}
	
	private String recognize(RefDouble minSure) {
		//parte para o reconhecimento de cada blob, e já verifica a mínima certeza.			
		String ret = "";
		minSure.value = 1.0;
		org.domain.commom.RefDouble weight = new org.domain.commom.RefDouble();
		
		for (int i = 0; i < 3 && i < blobs.size(); i++) {
			Blob blob = blobs.getBlob(i);
			ret += classificator.recognize(blob, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", weight);
//			double factor = 1.0 - (double) Math.Abs (blob.Rect.Width - meanWidthLetter) / (double) maxCharWidth;
//			weight *= factor;
				
			if (weight.value < minSure.value) {
				minSure.value = weight.value;
			}
		}
		
		for (int i = 3; i < blobs.size() && i < maxNumDigits; i++) {
			Blob blob = blobs.getBlob(i);
			ret += classificator.recognize(blob, "0123456789", weight);
//			double factor = 1.0 - (double) Math.Abs (blob.Rect.Width - meanWidthNumber) / (double) maxCharWidth;
//			weight *= factor;
				
			if (weight.value < minSure.value) {
				minSure.value = weight.value;
			}
		}
		
		if (ret.length() == 0) {
			minSure.value = 0.0;
		}
		
		return ret;
	}

	public String recognize(Image imgIn) throws Exception {
		RefDouble sure = new RefDouble();
		String bestAnswer = "";
		double bestMinSure = -1.0;
		@SuppressWarnings("unused")
		int bestColorTolerance = 0;
//		int colorTolerance = 10 / 2;
//		int colorTolerance = 8 / 2;
		int colorTolerance = 4;
		int numSections = 1;//3;
		int width = imgIn.getWidth();
		int height = imgIn.getHeight()/numSections;
		int numChannels = imgIn.getNumChannels();
		Image imgOut = new Image(width, height, numChannels);
		Rect roi = imgIn.getRect();
		Rect rectIn = new Rect(width, height);
		Rect rectOut = new Rect(width, height);
		
		if (numChannels > 1) {
			height -= this.maxCharHeight;
			numSections = imgIn.getHeight()/height + 1;
		}
		
//		for (int colorTolerance = 10; colorTolerance >= 7; colorTolerance--) {
		for (int section = 0; section < numSections; section++) {
			if (rectIn.getBottom() > roi.getBottom()) {
				rectIn.setBottom(roi.getBottom());
			}

			Image.copy(imgIn, rectIn, imgOut, rectOut);
//			extractBlobs(imgOut, colorTolerance, 12 / 2, 4 / 2);
			extractBlobs(imgOut, colorTolerance, 6, 1);
			String answer = recognize(sure);
			
			if (sure.value > bestMinSure) {
				bestMinSure = sure.value;
				bestAnswer = answer;
				bestColorTolerance = colorTolerance;
				// copia a seleção
				Rect rect = this.blobs.getRegion();
				this.imgThreshold.reset(rect.width, rect.height, this.imgBlobs.getNumChannels());
				Image.copy(imgBlobs, rect, imgThreshold, this.imgThreshold.getRect());

				if (bestMinSure > 0.25) {
					break;
				}
			}
			
			rectIn.y += height;
		}
		
		return bestAnswer;
	}

	/**
	 * Este método localiza os blobs diretamente nos dados de entrada (sem qualquer tratamento).
	 * Remove os blobs muito grandes e os muito claros.
	 * Une os blobs que sobraram.
	 * @param imgIn
	 * @throws Exception
	 */
	public void extractBlobs (Image img) throws Exception {
//		extractBlobs(img, 10 / 2, 12 / 2, 4 / 2);
//		extractBlobs(img, 8 / 2, 12 / 2, 4 / 2);
//		extractBlobs(img, 4, 6, 1);
		extractBlobs(img, 4, 6, 1);
		this.blobs.drawRects(this.imgBlobs);
		lines = this.blobs.getLine();
	}

	/**
	 * Separa o fundo dos objetos de frente, parte por premissa que o fundo é mais claro que os
	 * objetos relevantes.
	 * O algorítimo consiste em comparar cada pixel com a média e desvio padrão da vizinhança
	 * (amostra), estabelencendo como
	 * 
	 * limite_inferior = média - sigmasDown * desvio_padrão
	 * 
	 * limite_superior = média - sigmasUp * desvio_padrão
	 * 
	 * @param imgIn
	 * @param roiIn
	 * @param imgOut
	 * @param roiOut
	 * @param vizSize valor para a largura e a altura do retângulo da vizinhança (60 para cheques) 
	 * @throws Exception
	 */
	public static void clamp(Image imgIn, Rect roiIn, Image imgOut, Rect roiOut, int vizSize, int sigmaThreshold, double sigmasDown, double sigmasUp) throws Exception {
		if (imgIn == null || roiIn == null || imgOut == null || roiOut == null) {
			throw new IllegalArgumentException("parâmetro nulo");
		}
		
		int widthIn = imgIn.getWidth();
		int heightIn = imgIn.getHeight();
		int channels = imgIn.getNumChannels();
		int widthOut = imgOut.getWidth();
		int heightOut = imgOut.getHeight();
		int channelsOut = imgOut.getNumChannels();
		
		if (widthIn != widthOut || heightIn != heightOut || channels != channelsOut) {
			throw new IllegalArgumentException("imagens de dimensões diferentes");
		}
		
		if (roiIn.width != roiOut.width || roiIn.height != roiOut.height) {
			throw new IllegalArgumentException("regiões de dimensões diferentes");
		}
		
		// parâmetros da imagem de entrada.
		int topIn = roiIn.y;
		int leftIn = roiIn.x;
		// parâmetros da imagem de saída.
		int topOut = roiOut.y;
		int bottomOut = topOut + roiOut.height;
		int leftOut = roiOut.x;
		int rightOut = leftOut + roiOut.width;
		Image imgMean = new Image(widthIn, heightIn, channels);
		Image imgSigma = new Image(widthIn, heightIn, channels);
		Image.contrast(imgIn, roiIn, imgMean, imgSigma, roiIn, vizSize);
		
		int posIn, colOut, rowIn, rowOut;
		int[] pixel = new int[channels];
		int[] pixelMean = new int[channels];
		byte[] pixelsIn = imgIn.getPixels();
		byte[] pixelsMean = imgMean.getPixels();
		byte[] pixelsSigma = imgSigma.getPixels();
		// varre cada ponto da saída, verificando quais devem ser os limites para threshold.
		for (rowOut = topOut, rowIn = topIn; rowOut < bottomOut; rowOut++, rowIn++) {
			posIn = (rowIn * widthIn + leftIn) * channels;
			
			for (colOut = leftOut; colOut < rightOut; colOut++) {
				boolean doChange = true;
				
				for (int c = 0; c < channels; c++, posIn++) {
					pixel[c] = pixelsIn[posIn];
					pixelMean[c] = pixelsMean[posIn];
					double sigma = pixelsSigma[posIn];
					
					if (sigma > sigmaThreshold) {
						double mean = pixelsMean[posIn];
						double limDown = mean + sigmasDown * sigma;
						double limUp = mean + sigmasUp * sigma;
						
						int val = pixelsIn[posIn];
						
						if (val < limDown || val > limUp) {
							doChange = false;
						}
					}
				}
				
				if (doChange == true) {
					imgOut.setPixel(colOut, rowOut, pixelMean);
				} else {
					imgOut.setPixel(colOut, rowOut, pixel);
				}
			}
		}
	}

	public void threshold(Image img) throws Exception {
		Rect roiIn = getRoi(img);
		int width = roiIn.width;
		int height = roiIn.height;
		Rect roiOut = new Rect(width, height);
		int vizSize = 30;
		
		if (height < 2 * vizSize) {
			vizSize = height;
		}
		
		this.imgGray.reset(width, height, img.getNumChannels());
		this.imgClamp.reset(width, height, img.getNumChannels());
		this.imgThreshold.reset(width, height, img.getNumChannels());
		// aplica contraste local
		clamp(img, roiOut, this.imgThreshold, roiOut, vizSize, 15, -0.75, 0.75);
		debugImg(imgThreshold, "imgThreshold.bmp");
	}

	@Override
	public void getInfo(ModuleInfo info) {
		info.family = "OCR";
		info.name = "OcrLP";
		info.version = 1;
		info.dependenciesFamily = new String[] {};
		info.dependencies = new String[] {};
	}
}
