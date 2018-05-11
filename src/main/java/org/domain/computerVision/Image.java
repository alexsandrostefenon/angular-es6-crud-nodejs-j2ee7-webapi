package org.domain.computerVision;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.domain.commom.ByteArrayUtils;
import org.domain.commom.LoggerText;
import org.domain.commom.RefInt;

public class Image {
	public static final int CHANNEL_RED = 2;
	public static final int CHANNEL_GREEN = 1;
	public static final int CHANNEL_BLUE = 0;
	public static final int MIN_ALLOCATED_SIZE = 10*10;
//	public static final int MAX_VALUE_INT = 127;
	public static final int SEARCH_DIRECTION_TOP_TO_BOTTOM = 0;
	public static final int SEARCH_DIRECTION_BOTTOM_TO_TOP = 1;
	public static final int SEARCH_DIRECTION_LEFT_TO_RIGHT = 2;
	public static final int SEARCH_DIRECTION_RIGHT_TO_LEFT = 3;

	public static final int OPER_ADD = 0;
	public static final int OPER_SUB = 1;
	public static final int OPER_MUL = 2;
	public static final int OPER_DIV = 3;
	public static final int OPER_SUB_INV = 4;
	public static final int OPER_SQRT = 5;
	public static final int OPER_DIFF_ABS = 6;
	
	private static LoggerText logger = null;
	
	protected int width;
	protected int height;
	protected int numChannels;
	private int allocatedSize = 0;
	protected byte[] bufferByte;
	protected int[] bufferInt;
	private int bitsByChannel;
	private int maxValueByChannel;
	
/*	
	private static boolean loggerState = false;
	private static void log(String message) throws IOException {
		if (Image.logger != null && Image.loggerState == true) {
			Image.logger.log(message);
		}
	}
	
	private static void setLoggerState(boolean state) {
		Image.loggerState = state;
	}
*/	

	public static double myRound(double val) {
		return Math.ceil(val-0.5);
	}
	
	public static void setLogger(LoggerText logger) {
		if (Image.logger != null) {
			Image.logger = null;
		}
		
		Image.logger = logger;
	}

	public int getMaxValueByChannel() {
		return maxValueByChannel;
	}
	
	public void reset(Image other) {
		reset(other.width, other.height, other.numChannels);
	}
	
	public void reset(int width, int height, int numChannels) {
		int newSize = width * height * numChannels;
		
		if (newSize == 0) {
			newSize = MIN_ALLOCATED_SIZE;
		}
		
		if (newSize > this.allocatedSize) {
			if (this.bitsByChannel < 8) {
				this.bufferByte = null;
				this.bufferByte = new byte[newSize];
			} else {
				this.bufferInt = null;
				this.bufferInt = new int[newSize];
			}
			// DEBUG
	    	System.gc();
			this.allocatedSize = newSize;
		}
		
		this.width = width;
		this.height = height;
		this.numChannels = numChannels;
		clear();
	}

	public void aquire(int width, int height, int numChannels, int rowStride, byte[] data) {
		this.reset(width, height, numChannels);
		Pixel pixel = new Pixel(this);
		
		for (int row = 0, posIn = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				for (int c = 0; c < numChannels; c++) {
					pixel.values[c] = ByteArrayUtils.convertToUnsignedValue(data[posIn++], this.bitsByChannel);
				}
				
				setPixel(col, row, pixel.values);
			}
		}
	}
	
	public void aquire(int width, int height, int numChannels, int rowStride, int[] argb, int offset) {
		this.reset(width, height, numChannels);
		int[] pixel = new int[this.numChannels];
		
		for (int row = 0; row < height; row++) {
			int posIn = row * rowStride;
			
			for (int col = 0; col < width; col++) {
				int val = argb[offset+posIn++];
				int b = val & 0x000000ff;
				val >>= 8;
				int g = val & 0x000000ff;
				val >>= 8;
				int r = val & 0x000000ff;
				val >>= 8;
				int a = val & 0x000000ff;
				
				if (this.bitsByChannel < 8) {
					int shift = 8 - this.bitsByChannel;
					b >>= shift;
					g >>= shift;
					r >>= shift;
					a >>= shift;
				}

				if (this.numChannels == 3) {
					pixel[0] = r;
					pixel[1] = g;
					pixel[2] = b;
				} else if (this.numChannels == 1) {
					pixel[0] = b;
				} else if (this.numChannels == 4) {
					pixel[0] = a;
					pixel[1] = r;
					pixel[2] = g;
					pixel[3] = b;
				}
				
				this.setPixel(col, row, pixel);
			}
		}
	}
	
	private void initialize(int width, int height, int numChannels, int bitsByChannel) {
		this.bitsByChannel = bitsByChannel;
		
		if (this.bitsByChannel < 8) {
			this.maxValueByChannel = (1 << this.bitsByChannel) - 1;
		} else {
			this.maxValueByChannel = 255;
		}
		
		this.reset(width, height, numChannels);
	}
	
	public Image(int width, int height, int numChannels) {
		initialize(width, height, numChannels, 7);
	}

	public Image(int width, int height, int numChannels, int bitsByChannel) {
		initialize(width, height, numChannels, bitsByChannel);
	}
	
	public Image(Image other, boolean copyData) throws Exception {
		initialize(other.width, other.height, other.numChannels, other.bitsByChannel);
		
		if (copyData == true) {
			Image.copy(other, null, this, null);
		}
	}
	
	public Image(File file) throws IOException {
		this.reset(0, 0, 0);
		ImageFormat.loadBMP(this, file);
	}

	public Image(String filename) throws IOException {
		this.reset(0, 0, 0);
		ImageFormat.loadBMP(this, filename);
	}

	public void assign(Image other) throws Exception {
		reset(other);
		Image.copy(other, null, this, null);
	}
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public int getNumChannels() {
		return numChannels;
	}

	public Rect getRect() {
		Rect rect = new Rect(this.width, this.height);
		return rect;
	}
	
	public byte[] getPixels() {
		return this.bufferByte;
	}
// Operações sob um pixel específico endereçado por col e row	
	public void addToPixel(int col, int row, int[] pixel) {
		int pos = (row * this.width + col) * this.numChannels;
		
		if (this.bufferByte != null) {
			for (int c = 0; c < this.numChannels; c++, pos++) {
				pixel[c] += bufferByte[pos];
			}
		} else if (this.bufferInt != null) {
			for (int c = 0; c < this.numChannels; c++, pos++) {
				pixel[c] += bufferInt[pos];
			}
		}
	}

	public void getPixel(int col, int row, int[] pixel) {
		int pos = (row * width + col) * numChannels;
		
		if (this.bufferByte != null) {
			for (int c = 0; c < this.numChannels; c++, pos++) {
				pixel[c] = bufferByte[pos];
			}
		} else if (this.bufferInt != null) {
			for (int c = 0; c < this.numChannels; c++, pos++) {
				pixel[c] = bufferInt[pos];
			}
		}
	}

	public void getPixel(int col, int row, RefInt pixel) {
		if (this.numChannels == 1) {
			if (this.bufferByte != null) {
				pixel.value = this.bufferByte[row * this.width + col];
			} else if (this.bufferInt != null) {
				pixel.value = this.bufferInt[row * this.width + col];
			}
		} else {
			int sum = 0;
			int pos = (row * this.width + col) * this.numChannels;
			
			for (int c = 0; c < this.numChannels; c++, pos++) {
				if (this.bufferByte != null) {
					sum += this.bufferByte[pos];
				} else if (this.bufferInt != null) {
					sum += this.bufferInt[pos];
				}
			}
			
			pixel.value = sum / this.numChannels;
		}
	}

	public void setPixel(int col, int row, int[] pixel) {
		int pos = (row * this.width + col) * this.numChannels;
		
		for (int c = 0; c < this.numChannels; c++, pos++) {
			if (this.bufferByte != null) {
				this.bufferByte[pos] = (byte) pixel[c];
			} else if (this.bufferInt != null) {
				this.bufferInt[pos] = pixel[c];
			}
		}
	}

	public void setPixel(int col, int row, int value) {
		if (this.numChannels == 1) {
			if (this.bufferByte != null) {
				this.bufferByte[row * this.width + col] = (byte) value;
			} else if (this.bufferInt != null) {
				this.bufferInt[row * this.width + col] = value;
			}
		} else {
			int pos = (row * this.width + col) * this.numChannels;
			
			for (int c = 0; c < this.numChannels; c++, pos++) {
				if (this.bufferByte != null) {
					this.bufferByte[pos] = (byte) value;
				} else if (this.bufferInt != null) {
					this.bufferInt[pos] = value;
				}
			}
		}
	}
	
	public int getBright(int col, int row) {
		int pos = (this.width * row + col) * this.numChannels;
		int value = 0;
		
		for (int c = 0; c < this.numChannels; c++) {
			if (this.bufferByte != null) {
				value += this.bufferByte[pos++];
			} else {
				value += this.bufferInt[pos++];
			}
		}
		
		value /= this.numChannels;
		return value;
	}
// final de Operações sob um pixel específico endereçado por col e row
	public void clear() {
		int size = width * height * numChannels;
		
		if (bufferByte != null) {
			for (int i = 0; i < size; i++) {
				bufferByte[i] = 0;
			}
		}
		
		if (bufferInt != null) {
			for (int i = 0; i < size; i++) {
				bufferInt[i] = 0;
			}
		}
	}

	public void dispose() {
		this.bufferByte = null;
	}

	public boolean isEqual(int[] pixel, int[] other) {
		boolean ret = true;
		
		for (int c = 0; c < numChannels; c++) {
			if (pixel[c] != other[c]) {
				ret = false;
				break;
			}
		}
		
		return ret;
	}

	public boolean isInRange(int[] pixel, int[] other, int tolerance) {
		boolean ret = true;
		
		for (int c = 0; c < numChannels; c++) {
			int diff = pixel[c] - other[c];
			
			if (Math.abs(diff) > tolerance) {
				ret = false;
				break;
			}
		}
		
		return ret;
	}

	public void accumulate(int[] accumulate, int[] pixel) {
		for (int c = 0; c < numChannels; c++)
			accumulate[c] += pixel[c];
	}

	public void divide(int[] pixelOut, int[] pixelIn, int divisor) {
		for (int c = 0; c < numChannels; c++)
			pixelOut[c] = pixelIn[c]/divisor;
	}

	public void pixelMerge(int col, int row, int[] pixelOther, double opacity) {
		if (pixelOther.length != this.numChannels) {
			return;
		}
		
		int[] pixel = new int[this.numChannels];
		double transparency = 1.0 - opacity;
		getPixel(col, row, pixel);
		
		for (int c = 0; c < this.numChannels; c++) {
			pixel[c] = (int) (pixel[c] * opacity + pixelOther[c] * transparency);
		}
		
		setPixel(col, row, pixel);
	}

	public void drawRectangle(Rect rect, int[] color, double opacity, int lineWidth) {
		int top = rect.y;
		int bottom = rect.getBottom();
		int left = rect.x;
		int right = rect.getRight();
		// varre o topo e o fundo
		for (int col = left; col < right; col++) {
			for (int i = 0; i < lineWidth; i++) {
				pixelMerge(col, top+i, color, opacity);
				pixelMerge(col, bottom-1-i, color, opacity);
			}
		}		
		// varre a esquerda e a direita
		top += lineWidth;
		bottom -= lineWidth;
		
		for (int row = top; row < bottom; row++) {
			for (int i = 0; i < lineWidth; i++) {
				pixelMerge(left+i, row, color, opacity);
				pixelMerge(right-1-i, row, color, opacity);
			}
		}
	}
	
	public static void drawDiff(Image imgRef, Rect roiRef, Image imgOther, Rect roiOther, int[] colorTolerance, int[] color, double opacity) throws Exception {
		if (imgOther.numChannels != imgRef.numChannels)
			throw new Exception("imagens de dimensões diferentes");
		
		if (roiOther.width != roiRef.width || roiOther.height != roiRef.height)
			throw new Exception("regiões de dimensões diferentes");
		
		int numChannels = imgOther.getNumChannels();
		// parâmetros da imagem de entrada.
		int topOther = roiOther.y;
		int leftOther = roiOther.x;
		int[] pixelOther = new int[numChannels];
		// parâmetros da imagem de saída.
		int topRef = roiRef.y;
		int bottomRef = topRef + roiRef.height;
		int leftRef = roiRef.x;
		int rightRef = leftRef + roiRef.width;
		int[] pixelRef = new int[numChannels];
		// varre a região de interesse
		for (int rowRef = topRef, rowOther = topOther; rowRef < bottomRef; rowRef++, rowOther++) {
			for (int colRef = leftRef, colOther = leftOther; colRef < rightRef; colRef++, colOther++) {
				imgRef.getPixel(colRef, rowRef, pixelRef);
				imgOther.getPixel(colOther, rowOther, pixelOther);
				
				for (int c = 0; c < numChannels; c++) {
					int diff = pixelRef[c] - pixelOther[c];
					
					if (diff < 0) {
						diff *= -1;
					}
					
					if (diff > colorTolerance[c]) {
						imgOther.pixelMerge(colOther, rowOther, color, opacity);
						// passa para o próximo pixel
						break;
					}
				}
			}
		}
	}
	// slow
	public static void copySoftScaled(Image imgIn, Rect rectIn, Image imgOut, Rect rectOut) throws Exception {
		if (imgIn.numChannels != imgOut.numChannels) {
			throw new Exception("diferent color space not suported");
		}
		// parâmetros de entrada.
		int topIn = rectIn.y;
		int leftIn = rectIn.x;
		int numChannels = imgIn.numChannels;
		// parâmetros de saída.
		int widthOut = imgOut.width;
		int topOut = rectOut.y;
		int bottomOut = rectOut.getBottom();
		int leftOut = rectOut.x;
		int rightOut = rectOut.getRight();
		// fatores de conversão
		double dx = (double)rectIn.width/(double)rectOut.width;
		double dy = (double)rectIn.height/(double)rectOut.height;
		double x;
		double y = topIn;
		double[] mean = new double[numChannels];
		// varre cada pixel da saída, verificando quem deve ser a entrada.
		for (int rowOut = topOut; rowOut < bottomOut; rowOut++, y += dy) {
			int posOut = (rowOut * widthOut + leftOut) * numChannels;
			x = leftIn;
			
			for (int colOut = leftOut; colOut < rightOut; colOut++, x += dx) {
				imgIn.mean(x, y, dx, dy, mean, null);
				
				for (int c = 0; c < numChannels; c++, posOut++) {
					if (imgOut.bufferByte != null) {
						imgOut.bufferByte[posOut] = (byte)mean[c];
					} else {
						imgOut.bufferInt[posOut] = (int) mean[c];
					}
				}
			}
		}
	}
	// fast
	public static void copyHardScaled(Image imgIn, Rect rectIn, Image imgOut, Rect rectOut) throws Exception {
		if (imgIn.numChannels != imgOut.numChannels) {
			throw new Exception("diferent color space not suported");
		}
		// parâmetros de entrada.
		int topIn = rectIn.y;
		int leftIn = rectIn.x;
		int numChannels = imgIn.numChannels;
		int widthIn = imgIn.width;
		// parâmetros de saída.
		int widthOut = imgOut.width;
		int topOut = rectOut.y;
		int bottomOut = rectOut.getBottom();
		int leftOut = rectOut.x;
		int rightOut = rectOut.getRight();
		// fatores de conversão
		double dx = (double)rectIn.width/(double)rectOut.width;
		double dy = (double)rectIn.height/(double)rectOut.height;
		double x;
		double y = topIn;
		// varre cada pixel da saída, verificando quem deve ser a entrada.
		for (int rowOut = topOut; rowOut < bottomOut; rowOut++, y += dy) {
			int posOut = (rowOut * widthOut + leftOut) * numChannels;
			x = leftIn;
			
			for (int colOut = leftOut; colOut < rightOut; colOut++, x += dx) {
				int posIn = ((int)y * widthIn + (int)x) * numChannels;
				
				for (int c = 0; c < numChannels; c++, posOut++, posIn++) {
					imgOut.bufferByte[posOut] = imgIn.bufferByte[posIn];
				}
			}
		}
	}
	// fast
	public static void reduce(Image imgOut, Rect rectOut, Image imgIn, Rect rectIn, int dx, int dy) throws Exception {
		if (rectIn == null) {
			rectIn = imgIn.getRect();
		}
		
		if (rectOut == null) {
			rectOut = imgOut.getRect();
		}
		
		if (imgIn.numChannels != imgOut.numChannels) {
			throw new Exception("diferent color space not suported");
		}
		// parâmetros de entrada.
		int numChannels = imgIn.numChannels;
		// ajuste das dimensões de saída
		{
			rectOut.x = 0;
			rectOut.y = 0;
			int widthOut = rectIn.width / dx;
			
			if (rectIn.width % dx != 0) {
				widthOut--;
				rectIn.width = widthOut * dx;
			}
	
			rectOut.width = widthOut;
			// ajuste da altura
			int heightOut = rectIn.height / dy;
			
			if (rectIn.height % dy != 0) {
				heightOut--;
				rectIn.height = heightOut * dy;
			}
			
			rectOut.height = heightOut;
			imgOut.reset(widthOut, heightOut, numChannels);
		}
		// parâmetros de saída.
		int topOut = rectOut.y;
		int bottomOut = rectOut.getBottom();
		int leftOut = rectOut.x;
		int rightOut = rectOut.getRight();
		// fatores de conversão
		int[] pixel = new int[numChannels];
		Rect roi = new Rect(rectIn);
		roi.width = dx;
		roi.height = dy;
		// varre cada pixel da saída, verificando quem deve ser a entrada.
		for (int rowOut = topOut; rowOut < bottomOut; rowOut++, roi.y += dy) {
			roi.x = 0;
			
			for (int colOut = leftOut; colOut < rightOut; colOut++, roi.x += dx) {
				imgIn.mean(roi, pixel);
//				pixel[0] = (roi.x % 20 == 0) ? 255 : 0; 
				imgOut.setPixel(colOut, rowOut, pixel);
			}
		}
	}
	// cópia sem escalonamento
	public static void copy(Image imgIn, Rect rectIn, Image imgOut, Rect rectOut) throws Exception {
		if (rectIn == null) {
			rectIn = imgIn.getRect();
		}
		
		if (rectOut == null) {
			rectOut = imgOut.getRect();
		}
		
		if (rectIn.width != rectOut.width || rectIn.height != rectOut.height) {
			throw new Exception("diferent scales not suported");
		}
		// parâmetros de entrada.
		int numChannelsIn = imgIn.numChannels;
		int widthIn = imgIn.width;
		int topIn = rectIn.y;
		int leftIn = rectIn.x;
		// parâmetros de saída.
		int numChannelsOut = imgOut.numChannels;
		int widthOut = imgOut.width;
		int topOut = rectOut.y;
		int bottomOut = rectOut.getBottom();
		int leftOut = rectOut.x;
		int rightOut = rectOut.getRight();
		int[] pixelIn = new int[numChannelsIn];
		// sem conversão  de cor
		if (numChannelsOut == numChannelsIn) {
			for (int rowOut = topOut, rowIn = topIn; rowOut < bottomOut; rowOut++, rowIn++) {
				for (int colOut = leftOut, colIn = leftIn; colOut < rightOut; colOut++, colIn++) {
					imgIn.getPixel(colIn, rowIn, pixelIn);
					imgOut.setPixel(colOut, rowOut, pixelIn);
				}
			}
		// convert to gray
		} else if (numChannelsOut == 1) {
			for (int rowOut = topOut, rowIn = topIn; rowOut < bottomOut; rowOut++, rowIn++) {
				int posOut = rowOut * widthOut + leftOut;
				int posIn = (rowIn * widthIn + leftIn) * numChannelsIn;
				
				for (int colOut = leftOut; colOut < rightOut; colOut++, posOut++) {
					int sum = 0;
					
					for (int c = 0; c < numChannelsIn; c++, posIn++) {
						if (imgIn.bufferByte != null) {
							sum += imgIn.bufferByte[posIn];
						} else {
							sum += imgIn.bufferInt[posIn];
						}
					}
					
					if (imgOut.bufferByte != null) {
						imgOut.bufferByte[posOut] = (byte)(sum / numChannelsIn);
					} else {
						imgOut.bufferInt[posOut] = sum / numChannelsIn;
					}
				}
			}
		// convert from gray
		} else if (numChannelsIn == 1) {
			for (int rowOut = topOut, rowIn = topIn; rowOut < bottomOut; rowOut++, rowIn++) {
				int posOut = (rowOut * widthOut + leftOut) * numChannelsOut;
				int posIn = rowIn * widthIn + leftIn;
				
				for (int colOut = leftOut; colOut < rightOut; colOut++, posIn++) {
					for (int c = 0; c < numChannelsOut; c++, posOut++) {
						imgOut.bufferByte[posOut] = imgIn.bufferByte[posIn];
					}
				}
			}
		}
	}
	
	public static void threshold(Image imgIn, Rect roiIn, Image imgOut, Rect roiOut, int valMax, int valUp, int valDown) throws Exception {
		if (imgIn.getWidth() != imgOut.getWidth() || imgIn.getHeight() != imgOut.getHeight()) {
			throw new Exception("imagens de dimensões diferentes");
		}
		
		if (roiIn.width != roiOut.width || roiIn.height != roiOut.height) {
			throw new Exception("regiões de dimensões diferentes");
		}
		// parâmetros da imagem de entrada.
		int numChannelsIn = imgIn.getNumChannels();
		int imgWidthIn = imgIn.getWidth();
		int topIn = roiIn.y;
		int leftIn = roiIn.x;
		// parâmetros da imagem de saída.
		int numChannelsOut = imgOut.getNumChannels();
		int imgWidthOut = imgOut.getWidth();
		int topOut = roiOut.y;
		int bottomOut = topOut + roiOut.height;
		int leftOut = roiOut.x;
		int rightOut = leftOut + roiOut.width;
		
		if (numChannelsOut == 1) {
			if (numChannelsIn == 1) {
				for (int rowOut = topOut, rowIn = topIn; rowOut < bottomOut; rowOut++, rowIn++) {
					int posIn = rowIn * imgWidthIn + leftIn;
					int posOut = rowOut * imgWidthOut + leftOut;
					
					for (int colOut = leftOut; colOut < rightOut; colOut++, posIn++, posOut++) {
						if (imgIn.bufferByte[posIn] > valMax) {
							imgOut.bufferByte[posOut] = (byte) valUp;
						} else {
							imgOut.bufferByte[posOut] = (byte) valDown;
						}
					}
				}
			} else {
				valMax *= numChannelsIn;
				
				for (int rowOut = topOut, rowIn = topIn; rowOut < bottomOut; rowOut++, rowIn++) {
					int posIn = (rowIn * imgWidthIn + leftIn) * numChannelsIn;
					int posOut = rowOut * imgWidthOut + leftOut;
					for (int colOut = leftOut; colOut < rightOut; colOut++, posOut++) {
						int sum = 0;
						
						for (int c = 0; c < numChannelsIn; c++, posIn++) {
							sum += imgIn.bufferByte[posIn];
						}
						
						if (sum > valMax) {
							imgOut.bufferByte[posOut] = (byte) valUp;
						} else {
							imgOut.bufferByte[posOut] = (byte) valDown;
						}
					}
				}
			}
		} else {
			throw new Exception("conversão não definida.");
		}
/*		
		String debugDir = "/tmp/";
		
		if (debugDir  != null) {
			imgOut.saveBMP(debugDir + "imgThreshold.bmp");
		}
*/
	}
	
	public void mean(Rect roi, double[] mean, double[] sigma) {
		int roiSize = roi.width * roi.height; 
		double sum = 0;
		double sum2 = 0;
		int top = roi.y;
		int bottom = roi.getBottom();
		int left = roi.x;
		int right = roi.getRight();
		double val;
		
		for (int row = top; row < bottom; row++) {
			int pos = (row * width + left) * numChannels;
			
			for (int col = left; col < right; col++) {
				val = 0.0;
				
				for (int c = 0; c < numChannels; c++, pos++) {
					val += bufferByte[pos];
				}
				
				val /= this.numChannels;
				sum += val;
				sum2 += val * val;
			}
		}
		
		double _mean = sum / (double)roiSize;
		mean[0] = _mean;
		
		if (sigma != null) {
		    double mean2 = sum2 / (double)roiSize;
		    double var = mean2-_mean*_mean;
		    double _sigma = Math.sqrt(var);
		    sigma[0] = _sigma;
		}
	}

	public void mean(Rect roi, int[] pixelMean) {
		for (int c = 0; c < numChannels; c++) {
			pixelMean[c] = 0;
		}
		
		int top = roi.y;
		int bottom = roi.getBottom();
		int left = roi.x;
		int right = roi.getRight();
		
		for (int row = top; row < bottom; row++) {
			for (int col = left; col < right; col++) {
				this.addToPixel(col, row, pixelMean);
			}
		}
		
		int roiSize = roi.width * roi.height;
		
		for (int c = 0; c < numChannels; c++) {
			pixelMean[c] /= roiSize;
		}
	}
	/**
	 * Observação : esta operação está fortemente ligada a operação de escalonamento com redução, em que se
	 * fatia a imagem de entrada em retângulos com dimenções X por Y, onde X e Y são obtidos da razão
	 * entre roiIn e roiOut, onde cada ponto da imagem de saída, é definido pela média do respectivo
	 * retângulo da entrada. Assim a corespondência entre os pontos da imagens de saída e os retângulos da
	 * entrada é de alinhamento em top e left, deve-se atentar que a tendência é presupor que o retângulo
	 * da entrada é o centro ao redor do ponto da saída, que não é o caso.
	 * 
	 * @param x
	 * @param y
	 */
	public void mean(double x, double y, double width, double height, double[] mean, double[] sigma) {
		double[] sum = new double[this.numChannels]; 
		double[] sum2 = new double[this.numChannels]; 
		int left = (int)x;
		int top = (int)y;
		int right = (int)(x+width);
		int bottom = (int)(y+height);
		double weight, weightRow, val;
		int pos, c;
		double weightRight = x - Math.floor(x);
		double weightLeft = 1.0 - weightRight;
		double weightBottom = y - Math.floor(y);
		double weightTop = 1.0 - weightBottom;
		double weightSum = 0.0;
		
		for (c = 0; c < this.numChannels; c++) {
			sum[c] = 0.0;
			sum2[c] = 0.0;
		}
/*
  		try {
			Image.log(String.format("x = %.2f - y = %.2f - w = %.2f - h = %.2f", x, y, width, height));
		} catch (IOException e) {
		}

  		try {
			Image.log(String.format("weightLeft = %.2f - weightRight = %.2f - weightTop = %.2f - weightBottom = %.2f", weightLeft, weightRight, weightTop, weightBottom));
		} catch (IOException e) {
		}
*/		
		
		for (int row = top; row <= bottom; row++) {
			pos = (row * this.width + left) * this.numChannels;
			
			if (row >= 0 && row < this.height) {
				weightRow = 1.0;
				
				if (row == top) {
					weightRow *= weightTop; 
				} else if (row == bottom) {
					weightRow *= weightBottom; 
				}
				
				for (int col = left; col <= right; col++, pos += this.numChannels) {
					if (col >= 0 && col < this.width) {
						weight = weightRow;
						
						if (col == left) {
							weight *= weightLeft; 
						} else if (col == right) {
							weight *= weightRight; 
						}
						
						weightSum += weight;
							
						for (c = 0; c < this.numChannels; c++) {
							if (this.bufferByte != null) {
								val = weight * this.bufferByte[pos+c];
							} else {
								val = weight * this.bufferInt[pos+c];
							}
/*							
							// TODO
					  		try {
								Image.log(String.format("row = %d - col = %d - pixel = %d - val = %.2f - weight = %.2f", row, col, this.pixels[pos+c], val, weight));
							} catch (IOException e) {
							}
*/							
							sum[c] += val;
							sum2[c] += val * val;
						}
					}
				}
			}
		}
		
		for (c = 0; c < this.numChannels; c++) {
			double _mean = 0.0;
			
			if (weightSum > 0.0) {
				_mean = sum[c] / weightSum;
			}
			
			mean[c] = _mean;
			
			if (sigma != null) {
			    double mean2 = 0.0;
			    
				if (weightSum > 0.0) {
					mean2 = sum2[c] / weightSum;
				}
				
			    double var = mean2-_mean*_mean;
			    double _sigma = Math.sqrt(var);
			    sigma[c] = _sigma;
			}
		}
/*		
		// TODO
  		try {
			Image.log(String.format("mean = %.2f - weightSum = %.2f\n", mean[0], weightSum));
		} catch (IOException e) {
		}
		*/
	}

	/**
	 * Fatia a imagem de entrada em retângulos com dimenções X por Y, onde X e Y são obtidos da razão
	 * entre roiIn e roiOut.
	 * Varre cada ponto das imagens de saída, setando o valor do pixel com a média e o
	 * desvio padrão do respectivo retângulo da entrada.
	 * 
	 * Observação : a corespondência entre os pontos da imagens de saída e os retângulos da entrada
	 * é de alinhamento em top e left, deve-se atentar que a tendência é presupor que o retângulo
	 * da entrada é o centro ao redor do ponto da saída, que não é o caso. 
	 * 
	 * @param img
	 * @param roiIn
	 * @param imgMean
	 * @param imgSigma
	 * @param roiOut
	 * @throws Exception
	 */
	static void mean(Image img, Rect roiIn, Image imgMean, Image imgSigma, Rect roiOut) throws Exception {
		if (img == null || roiIn == null || imgMean == null || imgSigma == null || roiOut == null) {
			throw new Exception("parâmetro nulo");
		}
		
		if (imgMean.width != imgSigma.width || imgMean.height != imgSigma.height || imgMean.numChannels != imgSigma.numChannels) {
			throw new Exception("imagens de saída tem dimensões diferentes");
		}
		
		if (img.numChannels != imgMean.numChannels || roiIn.width < roiOut.width || roiIn.height < roiOut.height) {
			throw new Exception("regiões de entrada e saída tem dimensões incompatíveis");
		}
		
//		long tIni = System.currentTimeMillis();
		
		// parâmetros da imagem de entrada.
		int numChannels = img.numChannels;
		int topIn = roiIn.y;
		int leftIn = roiIn.x;
		// parâmetros das imagens de saída.
		int imgWidthOut = imgMean.width;
		int topOut = roiOut.y;
		int bottomOut = topOut + roiOut.height;
		int leftOut = roiOut.x;
		int rightOut = leftOut + roiOut.width;
		// parâmetros de conversão.
		double dx = (double)roiIn.width/(double)roiOut.width;
		double dy = (double)roiIn.height/(double)roiOut.height;
		double rowIn = topIn;
		double[] mean = new double[numChannels]; 
		double[] sigma = new double[numChannels]; 
		// varre cada ponto da saída, verificando quem deve ser a entrada.
		for (int rowOut = topOut; rowOut < bottomOut; rowOut++, rowIn += dy) {
			int posOut = (rowOut * imgWidthOut + leftOut) * numChannels;
			double colIn = leftIn;
			
			for (int colOut = leftOut; colOut < rightOut; colOut++, colIn += dx) {
				img.mean(colIn, rowIn, dx, dy, mean, sigma);
				
				for (int c = 0; c < numChannels; c++, posOut++) {
					imgMean.bufferByte[posOut] = (byte)Math.round(mean[c]);
					imgSigma.bufferByte[posOut] = (byte)Math.round(sigma[c]);
				}
			}
		}
		
//		long tEnd = System.currentTimeMillis();
//		log(String.format("Image.mean : tempo de processamento  = %s ms\n", tEnd-tIni));
	}
	
	public static void erase(Image imgIn, Rect roiIn, Image imgOut, Rect roiOut, byte lowBrightRange, byte hightBrightRange, byte[] fillColor) throws Exception {
		if (imgIn.getWidth() != imgOut.getWidth() || imgIn.getHeight() != imgOut.getHeight() || imgIn.numChannels != imgOut.numChannels)
			throw new Exception("imagens de dimensões diferentes");
		
		if (roiIn.width != roiOut.width || roiIn.height != roiOut.height)
			throw new Exception("regiões de dimensões diferentes");
		
		int numChannels = imgIn.getNumChannels();
		// parâmetros da imagem de entrada.
		int imgWidthIn = imgIn.getWidth();
		int topIn = roiIn.y;
		int leftIn = roiIn.x;
		// parâmetros da imagem de saída.
		int imgWidthOut = imgOut.getWidth();
		int topOut = roiOut.y;
		int bottomOut = topOut + roiOut.height;
		int leftOut = roiOut.x;
		int rightOut = leftOut + roiOut.width;
		// ajuste no range.
		int _lowBrightRange = lowBrightRange * numChannels;
		int _hightBrightRange = hightBrightRange * numChannels;
		
		for (int rowOut = topOut, rowIn = topIn; rowOut < bottomOut; rowOut++, rowIn++) {
			int posIn = (rowIn * imgWidthIn + leftIn) * numChannels;
			int posOut = (rowOut * imgWidthOut + leftOut) * numChannels;
			
			for (int colOut = leftOut; colOut < rightOut; colOut++, posIn += numChannels) {
				int sum = 0;
				
				for (int c = 0; c < numChannels; c++) {
					sum += imgIn.bufferByte[posIn+c];
				}
				if (sum >= _lowBrightRange && sum <= _hightBrightRange) {
					for (int c = 0; c < numChannels; c++, posOut++) {
						imgOut.bufferByte[posOut] = fillColor[c];
					}
				} else {
					for (int c = 0; c < numChannels; c++, posOut++) {
						imgOut.bufferByte[posOut] = imgIn.bufferByte[posIn+c];
					}
				}
			}
		}
	}
	
	public static void clamp(Image imgIn, Rect roiIn, Image imgOut, Rect roiOut, int[] limDown, int[] limUp) throws IllegalArgumentException {
		if (imgIn.getWidth() != imgOut.getWidth() || imgIn.getHeight() != imgOut.getHeight() || imgIn.numChannels != imgOut.numChannels)
			throw new IllegalArgumentException("imagens de dimensões diferentes");
		
		if (roiIn.width != roiOut.width || roiIn.height != roiOut.height)
			throw new IllegalArgumentException("regiões de dimensões diferentes");
		
//    	long tIni = System.currentTimeMillis();
		int numChannels = imgIn.getNumChannels();
		// ajuste no range.
		double range;
		double[] gain = new double[numChannels];
		int maxValByChannel = imgOut.getMaxValueByChannel();
		
		for (int c = 0; c < numChannels; c++) {
			range = limUp[c] - limDown[c];
			
			if (range <= 0.0) {
				throw new IllegalArgumentException("os limites produzem range inválido");
			}
			
			gain[c] = (double) maxValByChannel / range;
		}
		
		// parâmetros da imagem de entrada.
		int imgWidthIn = imgIn.getWidth();
		int topIn = roiIn.y;
		int leftIn = roiIn.x;
		// parâmetros da imagem de saída.
		int imgWidthOut = imgOut.getWidth();
		int topOut = roiOut.y;
		int bottomOut = topOut + roiOut.height;
		int leftOut = roiOut.x;
		int rightOut = leftOut + roiOut.width;
		
		for (int rowOut = topOut, rowIn = topIn; rowOut < bottomOut; rowOut++, rowIn++) {
			int posIn = (rowIn * imgWidthIn + leftIn) * numChannels;
			int posOut = (rowOut * imgWidthOut + leftOut) * numChannels;
			
			for (int colOut = leftOut; colOut < rightOut; colOut++) {
				for (int c = 0; c < numChannels; c++) {
					double val = imgIn.bufferByte[posIn++];
					// ajusta o offset
					val -= limDown[c];
					// aplica o ganho
					val *= gain[c];
					// verifica a saturação (que teóricamente só pode ocorrer para o limite inferior)
					if (val < 0.0) {
						val = 0.0;
					} else if (val > maxValByChannel) {
						val = maxValByChannel;
					} else {
						val = Image.myRound(val);
					}
					
					imgOut.bufferByte[posOut++] = (byte) val;
				}
			}
		}
		
//    	long tEnd = System.currentTimeMillis();
    	
//    	if (debugDir != null) {
//			imgOut.saveBMP(debugDir + "imgClamp.bmp");
//	        log("clamp concluído  em : " + (tEnd - tIni) + "ms");
//    	}
	}

	public static void contrast(Image imgIn, Rect roiIn, Image imgMean, Image imgSigma, Rect roiOut, int vizSize) throws Exception {
		if (imgIn == null || roiIn == null || imgMean == null || roiOut == null) {
			throw new IllegalArgumentException("parâmetro nulo");
		}
		
		if (imgIn.width != imgMean.width || imgIn.height != imgMean.height || imgIn.numChannels != imgMean.numChannels) {
			throw new IllegalArgumentException("imagens de dimensões diferentes");
		}
		
		if (roiIn.width != roiOut.width || roiIn.height != roiOut.height) {
			throw new IllegalArgumentException("regiões de dimensões diferentes");
		}
		
//		long tIni = System.currentTimeMillis();
		
		int numChannels = imgIn.numChannels;
		int width = imgIn.width;
		int height = imgIn.height;
		// parâmetros da imagem de saída.
		int topOut = roiOut.y;
		int bottomOut = topOut + height;
		int leftOut = roiOut.x;
		int rightOut = leftOut + width;
		// gera as imagem das médias e desvios padrões;
		int widthSmall = width / vizSize;
		int heightSmall = height / vizSize;
		Rect roiSmall = new Rect(widthSmall, heightSmall);
		Image imgMeanSmall = new Image(roiSmall.width, roiSmall.height, numChannels);
		Image imgSigmaSmall = new Image(roiSmall.width, roiSmall.height, numChannels);
		mean(imgIn, roiIn, imgMeanSmall, imgSigmaSmall, roiSmall);
		// TODO
//		imgMeanSmall.saveBMP("/tmp/imgMeanSmall.bmp");
//		imgSigmaSmall.saveBMP("/tmp/imgSigmaSmall.bmp");
//		Image.copy(imgMeanSmall, imgMeanSmall.getRect(), imgMean, imgMean.getRect());
//		Image.copy(imgSigmaSmall, imgSigmaSmall.getRect(), imgSigma, imgSigma.getRect());
//		imgMean.saveBMP("/tmp/imgMean1.bmp");
//		imgSigma.saveBMP("/tmp/imgSigma1.bmp");
		// parâmetros de conversão de escala entre a imgIn/imgOut e a imgMean/imgSigma.
		double scaleX = (double)widthSmall/(double)width;
		double scaleY = (double)heightSmall/(double)height;
		double[] mean = new double[numChannels];
		double[] sigma = new double[numChannels];
		double rowSmall = 0.0;
		int posOut, c, colOut;
		// varre cada ponto da saída, verificando quem deve ser a entrada
		for (int rowOut = topOut; rowOut < bottomOut; rowOut++, rowSmall += scaleY) {
			posOut = (rowOut * width + leftOut) * numChannels;
			double colSmall = 0.0;
			
			for (colOut = leftOut; colOut < rightOut; colOut++, colSmall += scaleX) {
//				Image.setLoggerState(true);
				
				imgMeanSmall.mean(colSmall-0.5, rowSmall-0.5, 1.0, 1.0, mean, null);
				
//				Image.setLoggerState(false);
				
				imgSigmaSmall.mean(colSmall-0.5, rowSmall-0.5, 1.0, 1.0, sigma, null);
				
				for (c = 0; c < numChannels; c++, posOut++) {
					imgMean.bufferByte[posOut] = (byte)Math.round(mean[c]);
					imgSigma.bufferByte[posOut] = (byte)Math.round(sigma[c]);
				}
			}
		}
		
//		long tEnd = System.currentTimeMillis();
//		log(String.format("Image.contrast : tempo de processamento  = %s ms\n", tEnd-tIni));
	}

	public double getDensity(double x, double y, double width, double height, int color) {
		if (this.numChannels != 1) {
			return 0.0;
		}
		
		double sum = 0.0;
		int left = (int)x;
		int top = (int)y;
		int right = (int)(x+width);
		int bottom = (int)(y+height);
		double weight, weightRow;
		int pos;
		double weightRight = x - Math.floor(x);
		double weightLeft = 1.0 - weightRight;
		double weightBottom = y - Math.floor(y);
		double weightTop = 1.0 - weightBottom;
		double weightSum = 0.0;

		for (int row = top; row < bottom; row++) {
			pos = (row * this.width + left) * this.numChannels;
			
			if (row >= 0 && row < this.height) {
				weightRow = 1.0;
				
				if (row == top) {
					weightRow *= weightTop; 
				} else if (row == bottom) {
					weightRow *= weightBottom; 
				}
				
				for (int col = left; col < right; col++, pos++) {
					if (col >= 0 && col < this.width) {
						weight = weightRow;
						
						if (col == left) {
							weight *= weightLeft; 
						} else if (col == right) {
							weight *= weightRight; 
						}
						
						weightSum += weight;
						
						if (this.bufferByte[pos] == color) {
							sum += weight;
						}
					}
				}
			}
		}
		
		return sum/weightSum;
	}
	
	public int[] getARGB(int[] argb) {
		int numPixels = this.width * this.height;
		
		if (argb == null || argb.length < numPixels) {
			argb = new int[numPixels];
		}
		
		int[] pixel = new int[this.numChannels];
		
		for (int row = 0, posOut = 0; row < this.height; row++) {
			for (int col = 0; col < this.width; col++) {
				this.getPixel(col, row, pixel);
				
				if (this.numChannels == 1) {
					argb[posOut++] = 0xff000000 | pixel[0] << 16 | pixel[0] << 8 | pixel[0];
				} else {
					int value = 0;
					
					for (int c = 0; c < this.numChannels; c++) {
						value <<= 8;
						value |= pixel[c];
					}
					
					argb[posOut++] = 0xff000000 | value;
				}
			}
		}
		
		return argb;
	}
	
	public void getLimitsRowBright(Rect roi, RefInt refMinBrightMean, RefInt refMaxBrightMean, RefInt rowOfMin, RefInt rowOfMax) {
		int top = roi.y;
		int bottom = roi.getBottom();
		int left = roi.x;
		int right = roi.getRight();
		int minBright = this.maxValueByChannel * roi.width * numChannels;
		int maxBright = -1;
		
		for (int row = top; row < bottom; row++) {
			int pos = (row * width + left) * numChannels;
			int lineBright = 0;
			
			if (this.bufferByte != null) {
				for (int col = left; col < right; col++) {
					for (int c = 0; c < numChannels; c++, pos++) {
						lineBright += bufferByte[pos];
					}
				}
			}
			
			if (this.bufferInt != null) {
				for (int col = left; col < right; col++) {
					for (int c = 0; c < numChannels; c++, pos++) {
						lineBright += bufferInt[pos];
					}
				}
			}
			
			if (lineBright < minBright) {
				minBright = lineBright;
				rowOfMin.value = row;
			}
			
			if (lineBright > maxBright) {
				maxBright = lineBright;
				rowOfMax.value = row;
			}
		}
		
		refMinBrightMean.value = minBright / (roi.width * numChannels);
		refMaxBrightMean.value = maxBright / (roi.width * numChannels);
	}

	public int getFirstInBrightRange(Rect roi, RefInt minBrightMean, RefInt maxBrightMean, int searchDirection, boolean updateRefs) {
		int ret = -1;
		int top = roi.y;
		int bottom = roi.getBottom();
		int left = roi.x;
		int right = roi.getRight();
		int minBright;
		int maxBright;
		int bright = 0;
		
		if (searchDirection == Image.SEARCH_DIRECTION_LEFT_TO_RIGHT || searchDirection == Image.SEARCH_DIRECTION_RIGHT_TO_LEFT) {
			minBright = minBrightMean.value * roi.height * numChannels;
			maxBright = maxBrightMean.value * roi.height * numChannels;
		} else {
			minBright = minBrightMean.value * roi.width * numChannels;
			maxBright = maxBrightMean.value * roi.width * numChannels;
		}
		
		if (searchDirection == Image.SEARCH_DIRECTION_TOP_TO_BOTTOM) {
			for (int row = top; row < bottom; row++) {
				int pos = (row * width + left) * numChannels;
				bright = 0;
				
				for (int col = left; col < right; col++) {
					for (int c = 0; c < numChannels; c++, pos++) {
						bright += bufferByte[pos];
					}
				}
				
				if (bright >= minBright && bright <= maxBright) {
					ret = row;
					break;
				}
			}
		} else if (searchDirection == Image.SEARCH_DIRECTION_BOTTOM_TO_TOP) {
			for (int row = bottom-1; row >= top; row--) {
				int pos = (row * width + left) * numChannels;
				bright = 0;
				
				for (int col = left; col < right; col++) {
					for (int c = 0; c < numChannels; c++, pos++) {
						bright += bufferByte[pos];
					}
				}
				
				if (bright >= minBright && bright <= maxBright) {
					ret = row;
					break;
				}
			}
		} else if (searchDirection == Image.SEARCH_DIRECTION_LEFT_TO_RIGHT) {
			for (int col = left; col < right; col++) {
				bright = 0;
				
				for (int row = top; row < bottom; row++) {
					int pos = (row * width + col) * numChannels;
					
					for (int c = 0; c < numChannels; c++, pos++) {
						bright += bufferByte[pos];
					}
				}
				
				if (bright >= minBright && bright <= maxBright) {
					ret = col;
					break;
				}
			}
		} else if (searchDirection == Image.SEARCH_DIRECTION_RIGHT_TO_LEFT) {
			for (int col = right-1; col >= left; col--) {
				bright = 0;
				
				for (int row = top; row < bottom; row++) {
					int pos = (row * width + col) * numChannels;
					
					for (int c = 0; c < numChannels; c++, pos++) {
						bright += bufferByte[pos];
					}
				}
				
				if (bright >= minBright && bright <= maxBright) {
					ret = col;
					break;
				}
			}
		}
		
		if (updateRefs) {
			if (searchDirection == Image.SEARCH_DIRECTION_LEFT_TO_RIGHT || searchDirection == Image.SEARCH_DIRECTION_RIGHT_TO_LEFT) {
				minBrightMean.value = bright / (roi.height * numChannels);
				maxBrightMean.value = bright / (roi.height * numChannels);
			} else {
				minBrightMean.value = bright / (roi.width * numChannels);
				maxBrightMean.value = bright / (roi.width * numChannels);
			}
		}
		
		return ret;
	}
	
	public static int getDiffCount(Image imgOut, Rect roiOut, Image imgIn, Rect roiIn, int[] colorTolerance) throws Exception {
		int count = 0;
		
		if (imgIn.numChannels != imgOut.numChannels)
			throw new Exception("imagens de dimensões diferentes");
		
		if (roiIn.width != roiOut.width || roiIn.height != roiOut.height)
			throw new Exception("regiões de dimensões diferentes");
		
		int numChannels = imgIn.getNumChannels();
		// parâmetros da imagem de entrada.
		int topIn = roiIn.y;
		int leftIn = roiIn.x;
		int[] pixelIn = new int[numChannels];
		// parâmetros da imagem de saída.
		int topOut = roiOut.y;
		int bottomOut = topOut + roiOut.height;
		int leftOut = roiOut.x;
		int rightOut = leftOut + roiOut.width;
		int[] pixelOut = new int[numChannels];
		// varre a região de interesse
		for (int rowOut = topOut, rowIn = topIn; rowOut < bottomOut; rowOut++, rowIn++) {
			for (int colOut = leftOut, colIn = leftIn; colOut < rightOut; colOut++, colIn++) {
				imgIn.getPixel(colIn, rowIn, pixelIn);
				imgOut.getPixel(colOut, rowOut, pixelOut);
				
				for (int c = 0; c < numChannels; c++) {
					int diff = pixelOut[c] - pixelIn[c];
					
					if (diff < 0) {
						diff *= -1;
					}
					
					if (diff > colorTolerance[c]) {
						count++;
						// passa para o próximo pixel
						break;
					}
				}
			}
		}
		
		return count;
	}

	public static List<Rect> getDiffRegions(Image imgRef, Rect roiRef, Image imgOther, Rect roiOther, int[] colorTolerance, int distToleranceX, int distToleranceY) throws Exception {
		// TODO : fazer teste de performance, devido a elevado uso de Rect rect = Rect.findInner(list, colRef, rowRef, distToleranceX, distToleranceY)
		List<Rect> list = new ArrayList<Rect>(1024);
		
		if (imgOther.numChannels != imgRef.numChannels)
			throw new Exception("imagens de dimensões diferentes");
		
		if (roiOther.width != roiRef.width || roiOther.height != roiRef.height)
			throw new Exception("regiões de dimensões diferentes");
		
		int numChannels = imgOther.getNumChannels();
		// parâmetros da imagem de entrada.
		int topOther = roiOther.y;
		int leftOther = roiOther.x;
		int[] pixelOther = new int[numChannels];
		// parâmetros da imagem de saída.
		int topRef = roiRef.y;
		int bottomRef = topRef + roiRef.height;
		int leftRef = roiRef.x;
		int rightRef = leftRef + roiRef.width;
		int[] pixelRef = new int[numChannels];
		// varre a região de interesse
		for (int rowRef = topRef, rowOther = topOther; rowRef < bottomRef; rowRef++, rowOther++) {
			for (int colRef = leftRef, colOther = leftOther; colRef < rightRef; colRef++, colOther++) {
				imgRef.getPixel(colRef, rowRef, pixelRef);
				imgOther.getPixel(colOther, rowOther, pixelOther);
				
				for (int c = 0; c < numChannels; c++) {
					int diff = pixelRef[c] - pixelOther[c];
					
					if (diff < 0) {
						diff *= -1;
					}
					
					if (diff > colorTolerance[c]) {
						// verifica se acha este ponto em alguma região cadastrada
						Rect rect = Rect.findInner(list, colRef, rowRef, distToleranceX, distToleranceY);
						
						if (rect != null) {
							// DEBUG :
//							System.out.printf("Aproveitando pontos %d,%d em %s\n", colRef, rowRef, rect.toString());
							Rect.union(rect, colRef, rowRef);
							// sempre que distTolerance > 0, após a união, tem change de haver junções  
							Rect.joinInners(list, distToleranceX, distToleranceY);
						} else {
							rect = new Rect(colRef, rowRef, 1, 1);
							list.add(rect);
							// sempre que adicionar uma região nova, verifica se existem junções
							Rect.joinInners(list, distToleranceX, distToleranceY);
						}
						// passa para o próximo pixel
						break;
					}
				}
			}
		}
 
		return list;
	}
	
	public void setLineData(int row, byte[] bufferLine) {
	    int lineWidth = width * numChannels;
    	int posRow = row * lineWidth;
    	int posOut = posRow;
    	int posIn = 0;
    	
    	for (int col = 0; col < width; col++) {
    		for (int c = 0; c < numChannels; c++) {
		    	if (this.bitsByChannel < 8) {
					int val = ByteArrayUtils.convertToUnsignedValue(bufferLine[posIn++], this.bitsByChannel);
    				this.bufferByte[posOut++] = (byte) val;
		    	} else {
					int val = ByteArrayUtils.convertToUnsignedValue(bufferLine[posIn++], 8);
		    		this.bufferInt[posOut++] = val;
		    	}
    		}
    	}
	}

	public int getLine(int row, int offset, byte[] buffer) {
    	int posIn = row * this.width * this.numChannels;
    	
    	if (this.bitsByChannel < 8) {
        	for (int col = 0; col < this.width; col++) {
        		for (int c = 0; c < this.numChannels; c++) {
    				buffer[offset++] = ByteArrayUtils.convertToUnsignedByte(this.bufferByte[posIn++], this.bitsByChannel);
        		}
        	}
    	} else {
        	for (int col = 0; col < this.width; col++) {
        		for (int c = 0; c < this.numChannels; c++) {
    				buffer[offset++] = ByteArrayUtils.convertToUnsignedByte(this.bufferInt[posIn++], 8);
        		}
        	}
    	}
    	
    	return offset;
	}

	public static void replaceColor(Image imgOut, Image imgIn, Rect rect, int valOut, int valIn) {
		int left = rect.x;
		int right = rect.getRight();
		int top = rect.y;
		int bottom = rect.getBottom();
		RefInt _valIn = new RefInt();
		
		for (int row = top; row < bottom; row++) {
			for (int col = left; col < right; col++) {
				imgIn.getPixel(col, row, _valIn);
				
				if (_valIn.value == valIn) {
					imgOut.setPixel(col, row, valOut);
				}
			}
		}
	}
// Operações básicas de adição, subtração, multiplacação, divisão, quadrado, raiz, etc..., que envolvem outra imagem
	private static void oper(Image imgOut, int posOut, Image imgIn, int posIn, int oper) {
		if (imgOut.bufferByte != null && imgIn.bufferByte != null) {
			if (oper == OPER_ADD) {
				imgOut.bufferByte[posOut] += imgIn.bufferByte[posIn];
			} else if (oper == OPER_SUB) {
				imgOut.bufferByte[posOut] -= imgIn.bufferByte[posIn];
			} else if (oper == OPER_MUL) {
				imgOut.bufferByte[posOut] *= imgIn.bufferByte[posIn];
			} else if (oper == OPER_DIV) {
				imgOut.bufferByte[posOut] /= imgIn.bufferByte[posIn];
			} else if (oper == OPER_DIFF_ABS) {
				imgOut.bufferByte[posOut] -= imgIn.bufferByte[posIn];
				
				if (imgOut.bufferByte[posOut] < 0) {
					imgOut.bufferByte[posOut] *= -1;
				}
			}
		} else {
			if (oper == OPER_ADD) {
				imgOut.bufferInt[posOut] += imgIn.bufferInt[posIn];
			} else if (oper == OPER_SUB) {
				imgOut.bufferInt[posOut] -= imgIn.bufferInt[posIn];
			} else if (oper == OPER_MUL) {
				imgOut.bufferInt[posOut] *= imgIn.bufferInt[posIn];
			} else if (oper == OPER_DIV) {
				imgOut.bufferInt[posOut] /= imgIn.bufferInt[posIn];
			} else if (oper == OPER_DIFF_ABS) {
				imgOut.bufferInt[posOut] -= imgIn.bufferInt[posIn];
				
				if (imgOut.bufferInt[posOut] < 0) {
					imgOut.bufferInt[posOut] *= -1;
				}
			}
		}
	}
	
	public static void oper(Image imgOut, Rect rectOut, Image imgIn, Rect rectIn, int oper) throws Exception {
		if (imgOut.width != imgIn.width) {
			throw new Exception("Don't match width");
		}
		
		if (imgOut.height != imgIn.height) {
			throw new Exception("Don't match height");
		}
		
		if (imgOut.numChannels != imgIn.numChannels) {
			throw new Exception("Don't match channels");
		}
		
		if (imgOut.bitsByChannel != imgIn.bitsByChannel) {
			throw new Exception("Don't match bitsByChannel");
		}
		// parâmetros de entrada.
		int widthIn = imgIn.width;
		int topIn = rectIn.y;
		int leftIn = rectIn.x;
		// parâmetros de saída.
		int numChannels = imgOut.numChannels;
		int widthOut = imgOut.width;
		int topOut = rectOut.y;
		int bottomOut = rectOut.getBottom();
		int leftOut = rectOut.x;
		int rightOut = rectOut.getRight();
		// sem conversão  de cor
		for (int rowOut = topOut, rowIn = topIn; rowOut < bottomOut; rowOut++, rowIn++) {
			int posOut = (rowOut * widthOut + leftOut) * numChannels;
			int posIn = (rowIn * widthIn + leftIn) * numChannels;
			
			for (int colOut = leftOut; colOut < rightOut; colOut++) {
				for (int c = 0; c < numChannels; c++, posIn++, posOut++) {
					oper(imgOut, posOut, imgIn, posIn, oper);
				}
			}
		}
	}
// Operações básicas de complemento, adição, subtração, multiplacação, divisão, quadrado, raiz, etc..., que não envolvem outra imagem
	public static void oper(Image imgOut, Rect rectOut, int value, int oper) {
		int left = rectOut.x;
		int right = rectOut.getRight();
		int top = rectOut.y;
		int bottom = rectOut.getBottom();
		
		for (int row = top; row < bottom; row++) {
			int posOut = (row * imgOut.width + left) * imgOut.numChannels;
			
			for (int col = left; col < right; col++) {
				for (int c = 0; c < imgOut.numChannels; c++, posOut++) {
					if (imgOut.bufferByte != null) {
						if (oper == OPER_ADD) {
							imgOut.bufferByte[posOut] += value;
						} else if (oper == OPER_SUB) {
							imgOut.bufferByte[posOut] -= value;
						} else if (oper == OPER_MUL) {
							imgOut.bufferByte[posOut] *= value;
						} else if (oper == OPER_DIV) {
							imgOut.bufferByte[posOut] /= value;
						} else if (oper == OPER_SUB_INV) {
							imgOut.bufferByte[posOut] = (byte) (value - imgOut.bufferByte[posOut]);
						} else if (oper == OPER_SQRT) {
							imgOut.bufferByte[posOut] = (byte) Math.round(Math.sqrt(imgOut.bufferByte[posOut]));
						}
					} else {
						if (oper == OPER_ADD) {
							imgOut.bufferInt[posOut] += value;
						} else if (oper == OPER_SUB) {
							imgOut.bufferInt[posOut] -= value;
						} else if (oper == OPER_MUL) {
							imgOut.bufferInt[posOut] *= value;
						} else if (oper == OPER_DIV) {
							imgOut.bufferInt[posOut] /= value;
						} else if (oper == OPER_SUB_INV) {
							imgOut.bufferInt[posOut] = value - imgOut.bufferInt[posOut];
						} else if (oper == OPER_SQRT) {
							imgOut.bufferInt[posOut] = (int) Math.round(Math.sqrt(imgOut.bufferInt[posOut]));
						}
					}
				}
			}
		}
	}

	public void AddAsciiHex(StringBuffer buffer) {
		Pixel pixel = new Pixel(this);
		
		for (int row = 0; row < height; row++) {
			for (int col = 0; col < width; col++) {
				getPixel(col, row, pixel.values);
				
				for (int c = 0; c < this.numChannels; c++) {
					ByteArrayUtils.AddAsciiHexFromUnsignedByte(buffer, pixel.values[c]);
				}
			}
		}
	}
	
}
