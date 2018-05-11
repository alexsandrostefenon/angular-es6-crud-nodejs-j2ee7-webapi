package org.domain.computerVision;

public class Pixel {
	int numChannels;
	int maxValue;
	public int[] values;

	public Pixel(Image img) {
		this.numChannels = img.getNumChannels();
		this.maxValue = img.getMaxValueByChannel();
		this.values = new int[img.getNumChannels()];
	}

	public int getBrightPercent() {
		int val = getBright();
		val *= 100;
		val /= maxValue;
		return val;
	}

	public double getBrightRelative() {
		double val = 0.0;
		
		for (int c = 0; c < numChannels; c++) {
			val += this.values[c];
		}
		
		val /= this.numChannels * maxValue;
		return val;
	}

	public int getBright() {
		int val = 0;
		
		for (int c = 0; c < numChannels; c++) {
			val += this.values[c];
		}
		
		val /= this.numChannels;
		return val;
	}
	
	
}
