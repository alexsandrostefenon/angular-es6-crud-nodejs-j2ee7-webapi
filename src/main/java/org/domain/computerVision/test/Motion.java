package org.domain.computerVision.test;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.domain.computerVision.Image;

public class Motion {
	public void captureScreen() throws Exception {
		   Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		   Rectangle rect = new Rectangle(screenSize);
		   Robot robot = new Robot();
		   BufferedImage image = robot.createScreenCapture(rect);
		   Image img = new Image(rect.width, rect.height, 3);
		   // image.getRGB(x, y);
	}
	
	private void saveImage() throws IOException {
		int width = 640;
		int height = 480;
		String fileName = "/tmp/tmp.png";
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		ImageIO.write(image, "png", new File(fileName));
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
