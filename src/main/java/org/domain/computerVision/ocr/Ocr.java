package org.domain.computerVision.ocr;

import java.util.ArrayList;

public class Ocr {
	public static final int CMD_THRESHOLD = 0;
	public static final int CMD_GENERATE_DIGITS = 1;
	public static final int CMD_RECOGNIZE = 3;
	
	public int cmd;
	public int generateDigitWidth;
	public int generateDigitHeight;
	public int generateDigitChannels;
	public String classifierPath;
	public String symbols;
	public String pathDigits;
	public String pathDebug;
	public String text;
	public boolean caseSensitive;
	public ArrayList<byte[]> imgs;
	public String logname; 
}
