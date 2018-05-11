package org.domain.computerVision.ocr;

import org.domain.commom.Module;
import org.domain.commom.ModuleInfo;
import org.domain.computerVision.Image;
import org.domain.computerVision.Rect;

public class OcrChequeClientInfo extends OcrText implements Module {
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
		blobs.scan(img, roi, colorBg, 0, 0);
		debugBlobs("blobs.bmp");
		int maxCharWidth = 25;
		int minCharWidth = 15;
		int maxCharHeight = 30;
		// era 3, mas juntou as letras PF do cheque ABELARDO .. CPF, que tem tamanho de 19 e separação de 1
		int maxGapX = 0;
		int maxGapY = 15;
 		blobs.removeBig(2*maxCharWidth, maxCharHeight, false);
		debugBlobs("blobs_after_remove_big.bmp");
		blobs.splitLarges(maxCharWidth, (int) minCharWidth);
		debugBlobs("blobs_after_split_larges.bmp");
		blobs.removeSmall(1*2, true);
		debugBlobs("blobs_after_remove_small.bmp");
		blobs.unionNears(maxGapX, maxGapY, maxCharWidth, maxCharHeight);
		debugBlobs("blobs_after_union_nears.bmp");
		blobs.drawRects(this.imgBlobs);
		lines = blobs.getMultiline(maxCharHeight);
	}

	@Override
	public void getInfo(ModuleInfo info) {
		info.family = "OCR";
		info.name = "OcrChequeClientInfo";
		info.version = 1;
		info.dependenciesFamily = new String[] {};
		info.dependencies = new String[] {};
	}
}
