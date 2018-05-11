package org.domain.computerVision;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import org.domain.commom.RefInt;

public class ImageFormat {

	private static int putWord(byte[] buffer, int offset, int val) {
		buffer[offset++] = (byte) (val & 0xff);
		val >>= 8;
		buffer[offset++] = (byte) (val & 0xff);
		return offset;
	}
	
	private static int putDWord(byte[] buffer, int offset, int val) {
		buffer[offset++] = (byte) (val & 0xff);
		val >>= 8;
		buffer[offset++] = (byte) (val & 0xff);
		val >>= 8;
		buffer[offset++] = (byte) (val & 0xff);
		val >>= 8;
		buffer[offset++] = (byte) (val & 0xff);
		return offset;
	}

	private static int getByte(byte[] data, RefInt offset) {
		int val = (0xff & data[offset.value]);
		offset.value++;
		return val;
	}
	
	private static int getWord(byte[] data, RefInt offset) {
		int b1 = getByte(data, offset);
		int b2 = getByte(data, offset);
		int val = b2 << 8 | b1;   
		return val;
	}
	
	private static int getDWord(byte[] data, RefInt offset) {
		int b1 = getByte(data, offset);
		int b2 = getByte(data, offset);
		int b3 = getByte(data, offset);
		int b4 = getByte(data, offset);
		int val = b4 << 24 | b3 << 16 | b2 << 8 | b1;   
		return val;
	}
/*	
	private static int getByte(InputStream strm) throws IOException {
		int val = strm.read();
		
		if (val < 0) {
			throw new IOException("final de arquivo");
		}
		
		return val;
	}
	
	private static int getWord(InputStream strm) throws IOException {
		int b1 = getByte(strm);
		int b2 = getByte(strm);
		int val = b2 << 8 | b1;   
		return val;
	}
	
	private static int getDWord(InputStream strm) throws IOException {
		int b1 = getByte(strm);
		int b2 = getByte(strm);
		int b3 = getByte(strm);
		int b4 = getByte(strm);
		int val = b4 << 24 | b3 << 16 | b2 << 8 | b1;   
		return val;
	}
*/
	private static void read(byte[] dataIn, RefInt offsetIn, byte[] dataOut) {
		int length = dataOut.length;
		System.arraycopy(dataIn, offsetIn.value, dataOut, 0, length);
		offsetIn.value += length;
	}
	
	private static void read(byte[] dataIn, RefInt offsetIn, byte[] dataOut, int length) {
		System.arraycopy(dataIn, offsetIn.value, dataOut, 0, length);
		offsetIn.value += length;
	}
		
	public static int getBmpWidthStep(int width, int numChannels) {
	    int widthStep = (width * numChannels + 3) & -4;
		return widthStep;
	}
	
	public static int getBmpSize(int width, int height,	int numChannels) {
	    int widthStep = getBmpWidthStep(width, numChannels);
	    int bitmapHeaderSize = 40;
	    int paletteSize = numChannels > 1 ? 0 : 1024;
	    int headerSize = 14 /* fileheader */ + bitmapHeaderSize + paletteSize;
	    int totalSize = widthStep * height + headerSize;
		return totalSize;
	}
	
	public static void loadBMP(Image img, byte[] bufferBMP) throws IOException {
	    int bufferSize = bufferBMP.length;
	    int bitmapFileHeaderSize = 14;
	    
	    if (bufferSize < bitmapFileHeaderSize) {
	    	String msg = "insuficient data size for valid header section (14 bytes) : have only " + bufferSize;
	    	System.out.println(msg);
	    	throw new IOException(msg);
	    }
	    
	    RefInt offset = new RefInt();
		// read signature 'BM'
		int B = getByte(bufferBMP, offset); // deve ser 0x42
		int M = getByte(bufferBMP, offset); // deve ser 0x4d
	    // testa as informações do header
	    if (B != 0x42 || M != 0x4d) {
	    	throw new IOException("invalid bitmap signature");
	    }
	    // write file header
	    int expectedSize = getDWord(bufferBMP, offset); // fileSize = fileStep*height + headerSize;
	    
	    if (bufferSize < expectedSize) {
	    	System.out.println("buffer size is lower than expected");
	    	throw new IOException("buffer size is lower than expected");
	    }
	    
	    getDWord(bufferBMP, offset); // deve ser 0
	    int headerSize = getDWord(bufferBMP, offset); // headerSize = bitmapFileHeaderSize + dibHeaderSize + paletteSize;
	    // check bitmap header
	    int dibHeaderSize = getDWord(bufferBMP, offset); // dibHeaderSize = 40 || dibHeaderSize = 108;
	    
	    if (bufferSize < bitmapFileHeaderSize + dibHeaderSize) {
	    	System.err.println("buffer size is lower than dibHeaderSize");
	    	throw new IOException("buffer size is lower than dibHeaderSize");
	    }
	    
	    int width = getDWord(bufferBMP, offset);
	    int height = getDWord(bufferBMP, offset);
	    getWord(bufferBMP, offset);
	    int numChannels = getWord(bufferBMP, offset) >> 3;
	    
	    int paletteSize = numChannels > 1 ? 0 : 1024;
	    // testa as informações do header
	    if (headerSize != bitmapFileHeaderSize + dibHeaderSize + paletteSize) {
	    	System.err.println("header size don't match with expected");
	    	throw new IOException("header size don't match with expected");
	    }
	    
	    getDWord(bufferBMP, offset); // deve ser 0 - BMP_RGB
	    getDWord(bufferBMP, offset); // deve ser 0
	    getDWord(bufferBMP, offset); // deve ser 0
	    getDWord(bufferBMP, offset); // deve ser 0
	    getDWord(bufferBMP, offset); // deve ser 0
	    getDWord(bufferBMP, offset); // deve ser 0
	    
	    // lê o restante do cabeçalho
	    int remaindHeaderSize = (bitmapFileHeaderSize + dibHeaderSize) - offset.value;
	    byte[] remaindHeader = new byte[remaindHeaderSize];
        read(bufferBMP, offset, remaindHeader);
	    
	    if (numChannels == 1) {
		    byte[] palette = new byte[4*256];
	        read(bufferBMP, offset, palette);
	        palette = null;
	    }
	    
	    int fileStep = (width*numChannels + 3) & -4;
	    // buffer auxiliar
	    byte[] bufferLine = new byte[fileStep];
	    int lineWidth = width * numChannels;
	    byte zeropad[] = {0x00, 0x00, 0x00, 0x00};
        img.reset(width, height, numChannels);
	    
	    for (int y = height - 1; y >= 0; y--) {
	        read(bufferBMP, offset, bufferLine, lineWidth);
	        
	        if (fileStep > width) {
	            read(bufferBMP, offset, zeropad, fileStep - lineWidth);
	        }
	        
	        img.setLineData(y, bufferLine);
	    }
	    
	    zeropad = null;
	}

	public static void loadBMP(Image img, File file) throws IOException {
	    if (file.exists() == false) {
	    	throw new IOException("file not exists");
	    }
	    
	    int fileSize = (int) file.length();

	    byte[] data = new byte[fileSize];
	    FileInputStream strm = new FileInputStream(file);
	    strm.read(data, 0, fileSize);
	    strm.close();
	    strm = null;
	    loadBMP(img, data);
	    data = null;
	}
	
	// executa as operações inversas do saveBMP
	public static void loadBMP(Image img, String filename) throws IOException	{
	    File file = new File(filename);
	    loadBMP(img, file);
	    file = null;
	}
	
	public static int convertArgbWithHeaderToBmp(int[] argbWithHeader,	byte[] bufferBMP) {
		int width = argbWithHeader[0];
		int height = argbWithHeader[1];
		int numChannels = argbWithHeader[2];
		int offset = exportBmpHeader(bufferBMP, width, height, numChannels);
		int lineStepBytes = getBmpWidthStep(width, numChannels);
	    int lineBytes = width * numChannels;
	    int diffLineBytes = lineStepBytes - lineBytes;
 	    byte zeropad[] = {0x00, 0x00, 0x00, 0x00};
	    
	    for (int y = height - 1; y >= 0; y--) {
	    	int posIn = y * width + 3 /*header*/;
	    	
	    	for (int col = 0; col < width; col++) {
				int val = argbWithHeader[posIn++];
				
	    		for (int c = 0; c < numChannels; c++) {
	    			byte low = (byte) (val & 0x000000ff);
					bufferBMP[offset++] = low;
					val >>= 8;
	    		}
	    	}
	        
	        if (diffLineBytes > 0) {
	            System.arraycopy(zeropad, 0, bufferBMP, offset, diffLineBytes);
	            offset += diffLineBytes;
	        }
	    }
	    
	    zeropad = null;
	    return offset;
	}
	
	private static int exportBmpHeader(byte[] buffer, int width, int height, int numChannels) {
	    int offset = 0;
	    int fileSize = getBmpSize(width, height, numChannels);
	    // buffer auxiliar
	    // write signature 'BM'
		buffer[offset++] = 0x42;
		buffer[offset++] = 0x4d;
	    // write file header
	    offset = putDWord(buffer, offset, fileSize); // file size
	    offset = putDWord(buffer, offset, 0);
	    
	    {
		    int bitmapHeaderSize = 40;
		    int paletteSize = numChannels > 1 ? 0 : 1024;
		    int headerSize = 14 /* fileheader */ + bitmapHeaderSize + paletteSize;
		    offset = putDWord(buffer, offset, headerSize);
		    // write bitmap header
		    offset = putDWord(buffer, offset, bitmapHeaderSize);
	    }
	    
	    offset = putDWord(buffer, offset, width);
	    offset = putDWord(buffer, offset, height);
	    offset = putWord(buffer, offset, 1);
	    offset = putWord(buffer, offset, numChannels << 3);
	    offset = putDWord(buffer, offset, 0/*BMP_RGB*/);
	    offset = putDWord(buffer, offset, 0);
	    offset = putDWord(buffer, offset, 0);
	    offset = putDWord(buffer, offset, 0);
	    offset = putDWord(buffer, offset, 0);
	    offset = putDWord(buffer, offset, 0);
	    
	    if (numChannels == 1) {
	        /*int length = 4*256;
	        
		    for (int i = 0; i < length; i += 4) {
		        int val = i ^ 0;
		        buffer[offset++] = buffer[offset++] = buffer[offset++] = (byte)val;
		        buffer[offset++] = 0;
		    }
		    */
		    for (int i = 1; i <= 256; i++) {
		        buffer[offset++] = buffer[offset++] = buffer[offset++] = (byte) (i & 0xff);
		        buffer[offset++] = 0;
		    }
	    }
	    
	    return offset;
	}

	public static int exportBMP(Image img, byte[] buffer) {
		int offset = exportBmpHeader(buffer, img.width, img.height, img.numChannels);
		int lineStepBytes = getBmpWidthStep(img.width, img.numChannels);
	    int lineBytes = img.width * img.numChannels;
	    int diffLineBytes = lineStepBytes - lineBytes;
 	    byte zeropad[] = {0x00, 0x00, 0x00, 0x00};
	    
	    for (int y = img.height - 1; y >= 0; y--) {
	    	offset = img.getLine(y, offset, buffer);
	        
	        if (diffLineBytes > 0) {
	            System.arraycopy(zeropad, 0, buffer, offset, diffLineBytes);
	            offset += diffLineBytes;
	        }
	    }
	    
	    zeropad = null;
	    return offset;
	}
	
	public static void saveBMP(Image img, String filename) throws Exception {
		int bmpSize = getBmpSize(img.width, img.height, img.numChannels);
		byte[] buffer = new byte[bmpSize];
		int size = exportBMP(img, buffer);
		FileOutputStream os = new FileOutputStream(filename);
		os.write(buffer, 0, size);
		os.close();
		os = null;
		buffer = null;
	}


	public static int[] loadBufferedImage(Image imgOut, BufferedImage bitmapIn, int[] bufferARGB) {
		int width = bitmapIn.getWidth();
		int height = bitmapIn.getHeight();
		int numChannels = 3;
		int numPixels = width * height;
		
		if (bufferARGB == null || bufferARGB.length < numPixels) {
			bufferARGB = new int[numPixels];
		}
		
		bitmapIn.getRGB(0 , 0, width, height, bufferARGB, 0, width);
		imgOut.aquire(width, height, numChannels, width, bufferARGB, 0);
		return bufferARGB;
	}

}
