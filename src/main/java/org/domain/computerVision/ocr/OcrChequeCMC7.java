package org.domain.computerVision.ocr;

import org.domain.commom.Module;
import org.domain.commom.ModuleInfo;
import org.domain.commom.RefInt;
import org.domain.computerVision.Image;
import org.domain.computerVision.Rect;

// imagem de referência está em 200dpi ou seja 1470 x 616
public class OcrChequeCMC7 extends OcrText implements Module {
	// o tamanho de referência de um cheque é 1470 x 616
	double scaleFactor = 1.0;
	int cmc7height = 25;
	int cmc7regionWidth = 940;
	
	public Rect getRoi(Image img) {
		Rect roi = img.getRect();
		// vou calcular a altura do cheque, partindo do princípio que o usuário alinhou o topo e os lados.
		this.scaleFactor = (double) roi.width / 1470.0;
		this.cmc7height = (int) (25.0 * this.scaleFactor);
		this.cmc7regionWidth = (int) (965.0 * this.scaleFactor);
		int minLeft = (int) (50.0 * this.scaleFactor);
		int maxWidth = (int) ((1080.0 - 50.0) * this.scaleFactor);
		int minTop = (int) (520.0 * this.scaleFactor);
		int maxHeight = (int) ((580.0 - 520.0) * this.scaleFactor);
		// região do CMC7
		roi.x = minLeft;
		roi.y = minTop;
		roi.width = maxWidth;
		roi.height = maxHeight;
		// localiza a linha mais escura
		RefInt refMinBrightMean = new RefInt();
		RefInt refMaxBrightMean = new RefInt();
		RefInt rowOfMinBright = new RefInt();
		RefInt rowOfMaxBright = new RefInt();
		// detecta a linha mais ecura, que deve estar próxima ao centro horizontal do CMC7
		img.getLimitsRowBright(roi, refMinBrightMean, refMaxBrightMean, rowOfMinBright, rowOfMaxBright);
		roi.height = this.cmc7height;
		int rangeBright = refMaxBrightMean.value - refMinBrightMean.value;
		rangeBright *= 60;
		rangeBright /= 100;
		refMinBrightMean.value += rangeBright;
		// encontra o top
		roi.y = rowOfMinBright.value - this.cmc7height;
		int top = img.getFirstInBrightRange(roi, refMinBrightMean, refMaxBrightMean, Image.SEARCH_DIRECTION_BOTTOM_TO_TOP, false);
		// encontra o bottom
		roi.y += this.cmc7height;
		int bottom = img.getFirstInBrightRange(roi, refMinBrightMean, refMaxBrightMean, Image.SEARCH_DIRECTION_TOP_TO_BOTTOM, false);
		// ajusta o top e o bottom
		
		if (top > 5) {
			roi.y = top - 5;
		} else {
			roi.y -= this.cmc7height;
		}
		
		if (bottom > 0) {
			roi.setBottom(bottom + 5);
		} else {
			roi.height = 2 * this.cmc7height;
		}
		// localiza o left
		refMaxBrightMean.value = refMinBrightMean.value;
		refMinBrightMean.value = 0;
		int col = img.getFirstInBrightRange(roi, refMinBrightMean, refMaxBrightMean, Image.SEARCH_DIRECTION_LEFT_TO_RIGHT, false);
		// ajusta o left e o right
		if (col > -1) {
			roi.x = col - 5;
			roi.width = this.cmc7regionWidth;
		} else {
			System.out.println("Não encontrou o left.");
		}
		
		return roi;
	}
	
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
		this.blobs.reset(imgWidth, imgHeight, img.getNumChannels());
		clearLines();
		this.colorBg[0] = 0;
		this.colorBg[1] = 0;
		this.colorBg[2] = 0;
		this.blobs.scan(img, roi, colorBg, 0, 0);
		int maxCharWidth = (int) (25.0 * this.scaleFactor);
		int minCharWidth = (int) (15.0 * this.scaleFactor);
		int maxCharHeight = (int) (30.0 * this.scaleFactor);
		int maxGapX = (int) (4.0 * this.scaleFactor);
		int maxGapY = (int) (15.0 * this.scaleFactor);
		this.blobs.removeBig(2*maxCharWidth, maxCharHeight, false);
 		this.blobs.splitLarges(maxCharWidth, minCharWidth);
		this.blobs.removeSmall(1*2, true);
		this.blobs.unionNears(maxGapX, maxGapY, maxCharWidth, maxCharHeight);
		this.blobs.unionNears(maxGapX, maxGapY, maxCharWidth, maxCharHeight);
		this.imgBlobs.reset(imgWidth, imgHeight, 3);
		this.blobs.saveMapColor(this.imgBlobs);
		this.blobs.drawRects(this.imgBlobs);
		debugImg(this.imgBlobs, "imgBlobs.bmp");
//		debugBlobs("debugBlobs.bmp");
		this.lines = blobs.getLine();
	}

	@Override
	public void getInfo(ModuleInfo info) {
		info.family = "OCR";
		info.name = "OcrChequeCMC7";
		info.version = 1;
		info.dependenciesFamily = new String[] {};
		info.dependencies = new String[] {};
	}
}
