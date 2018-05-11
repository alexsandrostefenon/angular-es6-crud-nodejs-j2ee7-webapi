/**
 * 
 */
package org.domain.computerVision.ocr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.domain.commom.Logger;
import org.domain.commom.Module;
import org.domain.commom.ModuleInfo;
import org.domain.commom.ModuleManager;
import org.domain.computerVision.Blobs;
import org.domain.computerVision.Image;
import org.domain.computerVision.ImageClassificator;
import org.domain.computerVision.ImageFormat;
import org.domain.computerVision.Rect;
import org.domain.computerVision.Blobs.Blob;

/**
 * @author alexsandro
 *
 */
public class OcrText implements Module {
	int minCharWidth;
	int maxCharWidth;
	int minCharHeight;
	int maxCharHeight;
	int maxLineHeight;
	int maxGapX;
	int maxGapY;
	int minNumDigits;
	int maxNumDigits;
	int minDigitsSplit;
	int maxDigitsSplit;
	static int MAX_SCREEN_WIDTH = 800;
	
	protected ModuleManager manager;
	protected int[] colorBg;
//	private Rect roi;
	protected ImageClassificator classificator;
	protected Blobs blobs;
	protected Image imgIn, imgGray, imgClamp, imgThreshold, imgBlobs, imgSymbol, imgAuxDisplay;
	protected ArrayList<ArrayList<Blob>> lines;
	protected String debugDir;
	private String classifierPath;
	private byte[] bufferImg; 
	
	public void initialize () {
		this.colorBg = new int[3];
		this.colorBg[0] = 0;
		this.colorBg[1] = 0;
		this.colorBg[2] = 0;
		this.classificator = null;
		this.imgSymbol = null;
		int maxImgWidth = 640;
		int maxImgHeight = 30;
		int bitsByChannel = 7;
		this.imgIn = new Image(maxImgWidth, maxImgHeight, 3);
		this.imgGray = new Image(maxImgWidth, maxImgHeight, 1);
		this.imgClamp = new Image(maxImgWidth, maxImgHeight, 1);
		this.imgThreshold = new Image(maxImgWidth, maxImgHeight, 1);
		this.imgBlobs = new Image(maxImgWidth, maxImgHeight, 3);
		this.imgSymbol = new Image(maxImgWidth, maxImgHeight, 1);
		this.imgAuxDisplay = new Image(maxImgWidth, maxImgHeight, 3);
		this.blobs = new Blobs(maxImgWidth, maxImgHeight, 1);
		this.bufferImg = new byte[maxImgWidth*maxImgHeight*3+40+1024];
//		this.roi = new Rect(0, 0, 640, 480);
	}

	void debugBlobs(String name) throws Exception {
		if (this.debugDir != null) {
			this.blobs.saveMap(this.debugDir + File.separator + name);
		}
	}

	void debugImg(Image img, String name) throws Exception {
		if (this.debugDir != null) {
			ImageFormat.saveBMP(img, this.debugDir + File.separator + name);
		}
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
	public static void clamp(Image imgIn, Rect roiIn, Image imgOut, Rect roiOut, int vizSize, double sigmasDown, double sigmasUp) throws Exception {
		if (imgIn == null || roiIn == null || imgOut == null || roiOut == null) {
			throw new IllegalArgumentException("parâmetro nulo");
		}
		
		int widthIn = imgIn.getWidth();
		int heightIn = imgIn.getHeight();
		int channelsIn = imgIn.getNumChannels();
		int widthOut = imgOut.getWidth();
		int heightOut = imgOut.getHeight();
		int channelsOut = imgOut.getNumChannels();
		
		if (widthIn != widthOut || heightIn != heightOut || channelsIn != channelsOut) {
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
		Image imgMean = new Image(widthIn, heightIn, channelsIn);
		Image imgSigma = new Image(widthIn, heightIn, channelsIn);
		Image.contrast(imgIn, roiIn, imgMean, imgSigma, roiIn, vizSize);
		
		int posIn, colOut, rowIn, rowOut;
		int maxValByChannel = imgOut.getMaxValueByChannel();
		int[] colorBg = {maxValByChannel, maxValByChannel, maxValByChannel};
		int[] pixelSigma = {0, 0, 0};
		int[] pixelIn = {0, 0, 0};
		int[] pixelOut = {0, 0, 0};
		double[] globalMean = {0.0, 0.0, 0.0};
		double[] globalSigma = {0.0, 0.0, 0.0};
		imgIn.mean(imgIn.getRect(), globalMean, globalSigma);
		byte[] pixelsIn = imgIn.getPixels();
		byte[] pixelsMean = imgMean.getPixels();
		byte[] pixelsSigma = imgSigma.getPixels();
		// varre cada ponto da saída, verificando quais devem ser os limites para threshold.
		for (rowOut = topOut, rowIn = topIn; rowOut < bottomOut; rowOut++, rowIn++) {
			posIn = (rowIn * widthIn + leftIn) * channelsIn;
			
			for (colOut = leftOut; colOut < rightOut; colOut++) {
				for (int c = 0; c < channelsIn; c++, posIn++) {
					pixelIn[c] = pixelsIn[posIn];
					pixelSigma[c] = pixelsSigma[posIn];
					double mean = pixelsMean[posIn];
					double sigma = pixelsSigma[posIn];
					double limDown = mean + sigmasDown * sigma;
					double limUp = mean + sigmasUp * sigma;
					double range = limUp - limDown;
					double gain = 1.0;
					
					if (range > 0.0) {
						gain = (double) maxValByChannel / range;
					}
					
					double val = pixelsIn[posIn];
					// ajusta o offset
					val -= limDown;
					// aplica o ganho
					val *= gain;
					// verifica a saturaÃ§Ã£o (que teÃ³ricamente sÃ³ pode ocorrer para o limit inferior)
					if (val < 0.0) {
						val = 0.0;
					} else if (val > maxValByChannel) {
						val = maxValByChannel;
					} else {
						val = Math.round(val);
					}
					
					pixelOut[c] = (int)val;
				}
				
				boolean forceBg = true;
				
				for (int c = 0; c < channelsIn; c++) {
					if (pixelIn[c] < 0.7 * globalMean[c] || pixelSigma[c] > 50) {
						forceBg = false;
						break;
					}
				}
				
				if (forceBg == true) {
					imgOut.setPixel(colOut, rowOut, colorBg);
				} else {
					imgOut.setPixel(colOut, rowOut, pixelOut);
				}
			}
		}
	}

	public Rect getRoi(Image img) {
		Rect roi = img.getRect();
		return roi;
	}
	
	/**
	 * Liamiariza imgIn, joga o resultado em tons de cinza na imgOut.
	 * @param imgIn
	 * @param imgOut
	 * @throws Exception
	 */
	public void threshold(Image img) throws Exception {
		Rect roiIn = getRoi(img);
		int width = roiIn.width;
		int height = roiIn.height;
		Rect roiOut = new Rect(width, height);
		int vizSize = 20;
		
		if (vizSize > height/2) {
			vizSize = height/2;
		}
		
		this.imgGray.reset(width, height, 1);
		this.imgClamp.reset(width, height, 1);
		this.imgThreshold.reset(width, height, 1);
		//  converte para cinza
		Image.copy(img, roiIn, this.imgGray, roiOut);
		debugImg(this.imgGray, "imgGray.bmp");
		// aplica contraste local
		clamp(this.imgGray, roiOut, this.imgClamp, roiOut, vizSize, -1.5, -0.75);
		debugImg(this.imgClamp, "imgClamp.bmp");
		int maxValueByChannel = img.getMaxValueByChannel();
		Image.threshold(this.imgClamp, roiOut, this.imgThreshold, roiOut, maxValueByChannel/2, 0, maxValueByChannel);
		debugImg(this.imgThreshold, "imgThreshold.bmp");
	}
	
	public void clearLines() {
		// sempre tem que resetar a lista de blobs.
		if (lines != null) {
			for (int i = 0; i < lines.size(); i++) {
				lines.get(i).clear();
			}
			
			lines.clear();
		}
	}
	
	/**
	 * Este método localiza os blobs diretamente nos dados de entrada (sem qualquer tratamento).
	 * Remove os blobs muito grandes e os muito claros.
	 * Une os blobs que sobraram.
	 * @param imgIn
	 * @throws Exception
	 */
	public void extractBlobs(Image img) throws Exception {
		Rect roi = getRoi(img);
		int width = roi.width;
		int height = roi.height;
		this.blobs.reset(width, height, img.getNumChannels());
		clearLines();
		this.colorBg[0] = 0;
		this.colorBg[1] = 0;
		this.colorBg[2] = 0;
		blobs.scan(img, roi, colorBg, 0, 0);
		int maxCharWidth = 15;
		int maxCharHeight = 15;
		int maxGapX = 1;
		int maxGapY = 1;
		blobs.removeSmall(3*3, true);
		blobs.unionNears(maxGapX, maxGapY, maxCharWidth, maxCharHeight);
		this.imgBlobs.reset(width, height, 3);
		blobs.drawRects(this.imgBlobs);
		debugImg(this.imgBlobs, "imgBlobs.bmp");
		debugBlobs("debugBlobs.bmp");
		lines = blobs.getMultiline(maxCharHeight);
	}
	
	/**
	 * @param imgIn é a imagem de referência.
	 * @param widthOut é a largura da imagem dos dígitos.
	 * @param heightOut é a altura da imagem dos dígitos. 
	 * @throws Exception 
	 */
	public void generateDigits(Image img, int width, int height, int numChannels, String symbols, String pathOut) throws Exception {
		threshold(img);
		extractBlobs(this.imgThreshold);
		this.imgSymbol.reset(width, height, numChannels);
		blobs.saveSamples(lines, this.imgSymbol, pathOut, symbols);
	}

	public String recognize(Image img) throws Exception {
		threshold(img);
		extractBlobs(imgThreshold);
		String ret = classificator.recognizeLines(this.lines);
		return ret;
	}

	public void setDebugPath(String debugPath) {
		if (debugPath != null) {
			File dir = new File(debugPath);
			
			if (dir.exists() && dir.isDirectory()) {
				this.debugDir = debugPath;
			}
		}
	}

	@Override
	public void getInfo(ModuleInfo info) {
		info.family = "OCR";
		info.name = "OcrText";
		info.version = 1;
		info.dependenciesFamily = new String[] {};
		info.dependencies = new String[] {};
	}
	
	@Override
	public void enable(ModuleManager manager) {
		this.manager = manager;
		initialize();
	}
	
	@Override
	public void start() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void stop() {
		// TODO Auto-generated method stub
	}
	
	public byte[] getBMP(Image img) throws Exception {
		Rect rect = img.getRect();
		
		if (rect.width > MAX_SCREEN_WIDTH) {
			try {
				double scale = (double)rect.width / (double)MAX_SCREEN_WIDTH;
				rect.width = MAX_SCREEN_WIDTH;
				double height = (double)rect.height;
				height /= scale;
				rect.height = (int) height;
				this.imgAuxDisplay.reset(rect.width, rect.height, img.getNumChannels());
				Image.copyHardScaled(img, img.getRect(), this.imgAuxDisplay, rect);
				img = this.imgAuxDisplay;
			} catch (Exception e) {
				e.printStackTrace();
//				log(e.getMessage());
			}
		}
		
		int imgSize = ImageFormat.getBmpSize(img.getWidth(), img.getHeight(), img.getNumChannels());
		
		if (imgSize > this.bufferImg.length) {
			this.bufferImg = null;
			this.bufferImg = new byte[imgSize];
		}
		
		byte[] ret = null;
		int size = ImageFormat.exportBMP(img, this.bufferImg);
		
		if (size == imgSize) {
			ret = new byte[size];
			System.arraycopy(this.bufferImg, 0, ret, 0, size);
		} else {
			throw new Exception("Error in OcrText.getBMP");
		}
		
		return ret;
	}
	
	@Override
	public long execute(Object data) {
		Ocr ocrParam = (Ocr) data;
		setDebugPath(ocrParam.pathDebug);
		
		if (ocrParam.classifierPath != null && ocrParam.classifierPath.equals(this.classifierPath) == false) {
			this.classifierPath = ocrParam.classifierPath;
			
			try {
				this.manager.log(Logger.LOG_LEVEL_TRACE, "OcrText.execute", "classificator ini", ocrParam);
				this.classificator = new ImageClassificator(ocrParam.classifierPath, null);
				this.manager.log(Logger.LOG_LEVEL_TRACE, "OcrText.execute", "classificator end", ocrParam);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
//				log(e.getMessage());
				this.classificator = null;
			}
		}
		
		try {
			// TODO : lêr a imagem para processar
			if (ocrParam.imgs.size() > 0) {
				// TODO : quando for camera, ler somente o ROI
				ImageFormat.loadBMP(this.imgIn, ocrParam.imgs.get(0));
			}
			
			ocrParam.imgs.clear();
			
			if (ocrParam.cmd == Ocr.CMD_THRESHOLD) {
				threshold(this.imgIn);
				ocrParam.imgs.add(getBMP(this.imgIn));
				ocrParam.imgs.add(getBMP(this.imgClamp));
				ocrParam.imgs.add(getBMP(this.imgThreshold));
			} else if (ocrParam.cmd == Ocr.CMD_GENERATE_DIGITS) {
				generateDigits(this.imgIn, ocrParam.generateDigitWidth, ocrParam.generateDigitHeight, ocrParam.generateDigitChannels, ocrParam.symbols, ocrParam.pathDigits);
				ocrParam.imgs.add(getBMP(this.imgIn));
				ocrParam.imgs.add(getBMP(this.imgClamp));
				ocrParam.imgs.add(getBMP(this.imgThreshold));
			} else if (ocrParam.cmd == Ocr.CMD_RECOGNIZE) {
				ocrParam.text = recognize(this.imgIn);
				ocrParam.imgs.add(getBMP(this.imgIn));
				ocrParam.imgs.add(getBMP(this.imgThreshold));
				ocrParam.imgs.add(getBMP(this.imgBlobs));
			}
		} catch (Exception e) {
			e.printStackTrace();
//			log(e.getMessage());
		}
		
		return 0;
	}
	
	@Override
	public void config(String section, String value) {
		// TODO Auto-generated method stub
	}
}
