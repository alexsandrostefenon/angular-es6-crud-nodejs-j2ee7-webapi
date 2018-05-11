package org.domain.computerVision.test;

import java.io.File;

import org.domain.computerVision.Image;
import org.domain.computerVision.ImageFormat;
import org.domain.computerVision.Rect;

public class TestImageBmpGray {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String dirInString = "/home/alexsandro/workspace/JavaProjects/data/OcrChequeCMC7/";
		File dirIn = new File(dirInString);
		String dirOutString = "/tmp/";
		File dirOut = new File(dirOutString);
		
		if (dirIn.exists() && dirIn.isDirectory() && dirOut.exists() && dirOut.isDirectory()) {
			File[] files = dirIn.listFiles();
			
			if (files != null) {
				for (File file : files) {
					String filenameIn = file.getName();
					
					if (filenameIn.endsWith(".bmp")) {
						String filenameOut = filenameIn.substring(0, filenameIn.length()-4);
						Image imgIn = new Image(640, 480, 1);
						Image imgOut = new Image(640, 480, 1);
						
						try {
							ImageFormat.loadBMP(imgIn, dirInString + filenameIn);
							Rect rect = imgIn.getRect();
							imgOut.reset(rect.width, rect.height, 1);
							Image.copy(imgIn, rect, imgOut, rect);
							ImageFormat.saveBMP(imgOut, dirOutString + filenameOut);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
	
}
