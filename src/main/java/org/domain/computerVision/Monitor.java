package org.domain.computerVision;

import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;

public class Monitor {
	public static final int RECORD_DESKTOP_TEXT = 0;
	public static final int RECORD_CAMERA_MOTION = 1;
	SimpleDateFormat sdf;
	private Image imgBackGround;
	private Image imgBackGroundResize;
	private Image imgDiff;
	int[] colorTolerance = new int[] {0, 0, 0};
	private int[] argb;
	private String activeFoldPath;
	private int activeFileIndex;
	private File[] files;
	private int activeFactorChange;
	private Rect activeDiffRegion;
	String appVersion1 = "dm1";
	String appVersion2 = "dm2";
	public static String version = "dm2";
	private String storagePath;
	private String activeFileName;
//	boolean isBackGroundEmpty = true;
	private int monitorModel;
	private final Blobs blobs = new Blobs(1920, 1080, 3);
	private int[] colorBg = new int[] {0x7f, 0x7f, 0x7f};
	
	public Monitor(int monitorModel) {
		this.monitorModel = monitorModel;
    	this.sdf = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		this.imgBackGround = new Image(640, 480, 3, 8);
		this.imgBackGroundResize = new Image(640, 480, 3, 8);
		this.imgDiff = new Image(640, 480, 3, 8);
		this.activeDiffRegion = new Rect();
	}

	private int getImage(File file, Image imgNewBackGround, Rect region, boolean drawBorder, boolean drawDiff, boolean updateBackGround) {
		int factorChange = 0;
		// yyyy_MM_dd_HH_mm_ss_%04d_%04d_%04d_%04d_%03d", x, y, width, height, factorChange
		// 2013_07_13_22_02_16_0593_0007_0593_0007_000.png				
		String fileName = file.getName();
		
		try {
			BufferedImage bufferedImageIn = ImageIO.read(file);
			this.argb = ImageFormat.loadBufferedImage(this.imgDiff, bufferedImageIn, this.argb);
			
//			if (this.isBackGroundEmpty == true) {
//				this.imgBackGround.assign(this.imgDiff);
//				this.isBackGroundEmpty = false;
//			}
			
			Rect roi = this.imgDiff.getRect();
			// faz o merge com a imagem de fundo
			String strX = fileName.substring(20, 24);
			String strY = fileName.substring(25, 29);
			roi.x = Integer.parseInt(strX);
			roi.y = Integer.parseInt(strY);
			Rect rectNewBackGround = this.imgBackGround.getRect();
			Rect.union(rectNewBackGround, roi, rectNewBackGround);
			adjustBackGroundSize(rectNewBackGround);
			imgNewBackGround.assign(this.imgBackGround);
			Image.copy(imgDiff, null, imgNewBackGround, roi);
			// DEBUG
//			saveImage(this.imgBackGround, roi, "/tmp/" + File.separator + getActiveFileName());
//			saveImage(imgNewBackGround, roi, "/tmp/" + File.separator + fileName);
			// marca a região de diferença
			int[] color = new int[] {127, 0, 0};
			
			if (drawBorder == true) {
				imgNewBackGround.drawRectangle(roi, color, 0.50, 8);
			}

			if (drawDiff == true) {
				Image.drawDiff(imgBackGround, roi, imgNewBackGround, roi, this.colorTolerance, color, 0.50);
			}

			if (updateBackGround) {
				Image.copy(imgDiff, null, imgBackGround, roi);
			}
			
			if (region != null) {
				region.assign(roi);
			}

			if (fileName.length() == 47) {
				String strFactorChange = fileName.substring(40, 43);
				factorChange = Integer.parseInt(strFactorChange);
			}
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(fileName);
			e.printStackTrace();
		}
		
		return factorChange;
	}
		
	public String convertOldVersion(String foldPath) {
		try {
			if (foldPath.indexOf(this.appVersion2) >= 0) {
				return foldPath;
			}
			
			if (foldPath.indexOf(this.appVersion1) < 0) {
				return foldPath;
			}
			
			File[] files = getFiles(foldPath);
			Image imgNewBackGround = new Image(640, 480, 3, 32);
			
			for (File file : files) {
				getImage(file, imgNewBackGround, null, false, false, false);
				String dateTime = file.getName().substring(0, 19);
				pushFrame(dateTime, imgNewBackGround);
			}
			
			foldPath = foldPath.replace(appVersion1, appVersion2);
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		return foldPath;
	}
	
	private File[] getFiles(String foldPath) {
		File[] files = null;
		File dir = new File(foldPath);
		
		if (dir.isDirectory()) {
			files = dir.listFiles();
			Arrays.sort(files);
		}
		
		return files;
	}
	
	// rc == 0 : não tem mais imagens
	// rc == 1 : tem imagem do mesmo frame
	// rc == 2 : tem imagem do próximo frame
	public int getImage(String foldPath, Image imgOut, boolean forward, boolean drawBorder, boolean drawDiff) {
		int rc = 0;
		
		if (foldPath.equals(this.activeFoldPath) == false) {
			this.activeFoldPath = foldPath;
			this.activeFileIndex = 0;
			this.files = getFiles(foldPath);
		}
		
		if (this.files == null || this.files.length == 0) {
			return rc;
		}
		
		File file = null;
		
		if (forward == true && this.activeFileIndex < this.files.length) {
			file = this.files[this.activeFileIndex++];
		} else if (forward == false && this.activeFileIndex > 0) {
			file = this.files[this.activeFileIndex--];
		}
		
		if (file != null) {
			this.activeFactorChange = getImage(file, imgOut, this.activeDiffRegion, drawBorder, drawDiff, true);
			this.activeFileName = file.getName();
			
			if (this.activeFileIndex >= this.files.length) {
				rc = 0;
			} else {
				String str = this.files[this.activeFileIndex].getName().substring(0, 19);
				
				if (file.getName().startsWith(str) == true) {
					rc = 1;
				} else {
					rc = 2;
				}
			}
		}
		
		return rc;
	}
	
	private String getFileName(String foldPath, String prefix, String suffix, Rect rect, int factorChange) {
		File dir = new File(foldPath);
		
		if (dir.exists() == false) {
			if (dir.mkdirs() == false) {
				return null;
			}
		}
		
    	StringBuffer filename = new StringBuffer(256);
    	filename.append(dir.getAbsolutePath());
    	filename.append(File.separator);
    	filename.append(prefix);
    	filename.append(String.format("_%04d_%04d_%04d_%04d_%03d", rect.x, rect.y, rect.getRight()-1, rect.getBottom()-1, factorChange));
    	filename.append(".");
    	filename.append(suffix);
    	return filename.toString();
	}

	private void saveImage(Image img, Rect rect, String filename) throws Exception {
		imgDiff.reset(rect.width, rect.height, img.getNumChannels());
		Image.copy(img, rect, this.imgDiff, null);
		this.argb = this.imgDiff.getARGB(this.argb);
		BufferedImage bitmapOut = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_ARGB);
		bitmapOut.setRGB(0, 0, rect.width, rect.height, argb, 0, rect.width);
		
		if (filename != null) {
			ImageIO.write(bitmapOut, "png", new File(filename));
		}
		
		bitmapOut = null;
		System.gc();
	}
	
	private void saveImage(String foldPath, String prefix, Image imgNewBackGround, Rect rect) throws Exception {
		int factorChange = 100 * Image.getDiffCount(imgNewBackGround, rect, this.imgBackGround, rect, colorTolerance);
		factorChange /= rect.width * rect.height;
		String filename = getFileName(foldPath, prefix, "png", rect, factorChange);
		saveImage(imgNewBackGround, rect, filename);
	}
	// caso houver mudança no tamho da tela, faz os ajustes
	private void adjustBackGroundSize(Rect rectNewBackGround) throws Exception {
		// a primeira coisa é verificar se deve redimensionar a imagem de fundo
		Rect rectBackGround = imgBackGround.getRect();
		Rect rectResize = new Rect(rectNewBackGround);
		// verifica se ficou mais larga
		boolean enlarge = Rect.union(rectBackGround, rectNewBackGround, rectResize);
		boolean reduce = false;
		// verifica se reduziu
		if (enlarge == false) {
			reduce = Rect.intersection(rectBackGround, rectNewBackGround, rectResize);
		}
		
		if (enlarge == true || reduce == true) {
			this.imgBackGroundResize.reset(rectResize.width, rectResize.height, this.imgBackGround.getNumChannels());
			
			if (reduce == true) {
				rectBackGround.assign(rectResize);
			}
			
			Image.copy(imgBackGround, rectBackGround, imgBackGroundResize, rectBackGround);
			this.imgBackGround.assign(imgBackGroundResize);
		}
	}
	
	public void pushFrame(String dateTime, Image img) {
		try {
			if (this.monitorModel == Monitor.RECORD_DESKTOP_TEXT) {
				// localiza todos os objetos, com tolerância 0 para diferença de tonalidade, assim vai conseguir diferenciar as
				// imagens, pois elas vão estar fragmentadas em pontos infimos, bastando tingilos de branco.
//				this.blobs.reset(img.getWidth(), img.getHeight(), img.getNumChannels());
//				Rect roi = img.getRect();
//				this.blobs.scan(img, roi, colorBg, 1, 1);
//				this.blobs.removeSmall(5, true);
//				Image.copy(this.blobs.img, roi, img, roi);
			}
			
			if (dateTime == null) {
				dateTime = sdf.format(new Date());
			}
			
			Rect rect = img.getRect();
			adjustBackGroundSize(rect);
			int distanceToleranceX = 100;
			int distanceToleranceY = 100;
			List<Rect> list = Image.getDiffRegions(imgBackGround, rect, img, rect, colorTolerance, distanceToleranceX, distanceToleranceY);
			
			boolean skip = false;
			
			if (this.monitorModel == Monitor.RECORD_DESKTOP_TEXT) {
				if (list != null && list.size() == 1) {
					Rect rectDiff = list.get(0);
					
					if (rectDiff.width < 15) {
						skip = true;
					}
				}
			}
			
	    	String foldPath = this.storagePath + File.separator + this.appVersion2 + File.separator + dateTime.substring(0, 13);		
			
			for (Rect rectDiff : list) {
				saveImage(foldPath, dateTime, img, rectDiff);
			}
			// agora que localizou e calculou todas as diferenças, atualiza a imagem de fundo (imgBackGround)
			this.imgBackGround.assign(img);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}

	public int getActiveFactorChange() {
		return this.activeFactorChange;
	}

	public Rect getActiveDiffRegion() {
		return this.activeDiffRegion;
	}
	
	public String getActiveFileName() {
		return this.activeFileName;
	}

	public String getAppVersion() {
		return this.appVersion2;
	}

	public void setStoragePath(String str) {
		this.storagePath = str;
	}


	public int getActiveFileIndex() {
		return this.activeFileIndex;
	}

	public int getNumFiles() {
		return this.files.length;
	}

}
