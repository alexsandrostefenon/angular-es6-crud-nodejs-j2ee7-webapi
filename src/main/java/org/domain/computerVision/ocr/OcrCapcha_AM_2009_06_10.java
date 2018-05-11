package org.domain.computerVision.ocr;

import org.domain.commom.Module;
import org.domain.commom.ModuleInfo;
import org.domain.computerVision.Image;
import org.domain.computerVision.Rect;

public class OcrCapcha_AM_2009_06_10 extends OcrText implements Module {
	/**
	 * Este método localiza os blobs diretamente nos dados de entrada (sem qualquer tratamento).
	 * Remove os blobs muito grandes e os muito claros.
	 * Une os blobs que sobraram.
	 * @param imgIn
	 * @throws Exception
	 */
	
	public void extractBlobs (Image img) throws Exception {
		int imgWidth = img.getWidth();
		int imgHeight = img.getHeight();
		Rect roi = new Rect(imgWidth, imgHeight);
		this.imgBlobs.reset(imgWidth, imgHeight, img.getNumChannels());
		this.blobs.reset(imgWidth, imgHeight, img.getNumChannels());

		// sempre tem que resetar a lista de blobs.
		if (lines != null) {
			for (int i = 0; i < lines.size(); i++) {
				lines.get(i).clear();
			}
			
			lines.clear();
		}
		
		this.colorBg[0] = 0;
		this.colorBg[1] = 0;
		this.colorBg[2] = 0;
		int minCharWidth = 9;
		int maxCharWidth = 14;
		int maxCharHeight = 14;
		blobs.scan(img, roi, colorBg, 0, 0);
		// remove os que são maiores que dois digitos unidos lateralmente
 		blobs.removeBig(2*maxCharWidth, maxCharHeight, false);
		// remove o lixo
		blobs.removeSmall(1*2, true);
		// une os fragmentos internos dos dígitos
		blobs.unionNears(0, 1, maxCharWidth, maxCharHeight);
		// separa os digitos emendados lateralmente
		blobs.splitLarges(maxCharWidth, (int) minCharWidth);
		blobs.drawRects(this.imgBlobs);
		lines = blobs.getLine();
	}

	public void threshold(Image img) throws Exception {
		int width = img.getWidth();
		int height = img.getHeight();
		Rect roi = new Rect(width, height);
		int[] min = new int[] {142, 84, 0};
		int[] max = new int[] {255, 153, 125};
		this.imgThreshold.reset(width, height, 1);
		this.imgClamp.reset(width, height, img.getNumChannels());
		// descarta as cores fora da região de interesse
		Image.clamp(img, roi, this.imgClamp, roi, min, max);
        int threshold = (66 * img.getMaxValueByChannel())/100;
		Image.threshold(this.imgClamp, roi, this.imgThreshold, roi, threshold, 0, img.getMaxValueByChannel());
	}

	@Override
	public void getInfo(ModuleInfo info) {
		info.family = "OCR";
		info.name = "OcrCapcha_AM_2009_06_10";
		info.version = 1;
		info.dependenciesFamily = new String[] {};
		info.dependencies = new String[] {};
	}
}
