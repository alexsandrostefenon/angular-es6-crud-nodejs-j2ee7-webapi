package org.domain.computerVision;
//*
import java.io.File;
/*/
/*/
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.domain.commom.Logger;
import org.domain.commom.RefInt;

public class Blobs {
	class BlobBuildAux {
		public int[] cols;
		public int[] rows;
		public int[] pixelRef;
		public int[] pixel;
		private int[] accumulate;
		private int numChannels;
		
		public BlobBuildAux(Image img) {
			int maxNumPixels = img.getWidth() * img.getHeight();
			this.numChannels = img.getNumChannels();
			this.cols = new int[maxNumPixels];
			this.rows = new int[maxNumPixels];
			this.pixelRef = new int[this.numChannels];
			this.pixel = new int[this.numChannels];
			this.accumulate = new int[this.numChannels];
		}

		public int[] getCleanAccumulate() {
			for (int c = 0; c < numChannels; c++)
				this.accumulate[c] = 0;
			return this.accumulate;
		}

		public void dispose() {
			this.cols = null;
			this.rows = null;
			this.pixelRef = null;
			this.pixel = null;
			this.accumulate = null;
			this.numChannels = 0;
		}
	}
	
	public class Blob {
		// parâmetros que deram início a detecção do blob.
		private int[] colorBg;
		private int pixelTolerance;
		private int meanTolerance;
		// referência para a imagem original em Blobs.
		private Image img;
		// referência para a imgMap em Blobs.
		private Image imgMap;
		private Image imgMapAux;
		// final dos parâmetros que deram início a detecção do blob.
		
		// Cada blob tem um id que o mapeia na imgMap, isto é necessário para
		// não precisar ter cópias internas das imagens.
		private int id;
		private Rect rect;
		private int filledArea;
		private Point filledCenter;
		private int[] filledMeanPixel;
		
		public int[] getColorBg() {
			return colorBg;
		}

		public int getPixelTolerance() {
			return pixelTolerance;
		}

		public int getMeanTolerance() {
			return meanTolerance;
		}

		public Rect getRect() {
			return rect;
		}

		public int getFilledArea() {
			return filledArea;
		}
		
		public int[] getFilledMeanPixel() {
			return filledMeanPixel;
		}

		// apaga seus pixeis na imagem original.
		public void erase() {
			int left = rect.x;
			int right = left + rect.width;
			int bottom = rect.y + rect.height;
			RefInt val = new RefInt();
			
			for (int row = rect.y; row < bottom; row++) {
				for (int col = left; col < right; col++) {
					imgMap.getPixel(col, row, val);
					
					if (val.value == id) {
						img.setPixel(col, row, colorBg);
					}
				}
			}
		}
		
		public void dispose() {
			if (img == null || imgMap == null || imgMapAux == null) {
				return;
			}
			
			int left = rect.x;
			int right = rect.x + rect.width;
			int bottom = rect.y + rect.height;
			RefInt val = new RefInt();
			
			for (int row = rect.y; row < bottom; row++) {
				for (int col = left; col < right; col++) {
					imgMap.getPixel(col, row, val);
					
					if (val.value == id) {
						imgMap.setPixel(col, row, 0);
					}
				}
			}
			
			img = null;
			imgMap = null;
			imgMapAux = null;
		}
		
		// para fins de melhor desenpenho no arredondamento,
		// as dimensões da imgOut devem ser menores que a metade das dimensões do blob.
		public void makeSample(Image imgOut) {
			// copiar a imagem do blob para uma imagem temporária, depois aplicar o escalonamento
			int width = this.imgMap.width;
			int height = this.imgMap.height;
			this.imgMapAux.reset(width, height, 1);
			int maxValue = this.imgMapAux.getMaxValueByChannel();
			Image.replaceColor(this.imgMapAux, this.imgMap, this.rect, maxValue, this.id);
			
			try {
				Image.copySoftScaled(this.imgMapAux, this.rect, imgOut, imgOut.getRect());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void union(Blob other) {
			int numChannels = img.getNumChannels();
			int newFilledArea = this.filledArea + other.filledArea;
			
			if (newFilledArea != 0) {
				double factor = (double) this.filledArea / (double) newFilledArea;
				double otherFactor = (double) other.filledArea / (double) newFilledArea;
				
				for (int i = 0; i < numChannels; i++) {
					this.filledMeanPixel[i] = (byte) ((this.filledMeanPixel[i] * factor) + (other.filledMeanPixel[i] * otherFactor));
				}
			}
			
			this.filledArea = newFilledArea;
			Rect.union(this.rect, other.rect, this.rect);
			// muda na imgMap de other.id para this.id.
			int other_id = other.id;
			int left = other.rect.x;
			int right = other.rect.x + other.rect.width;
			int bottom = other.rect.y + other.rect.height;
			RefInt val = new RefInt();
			
			for (int row = other.rect.y; row < bottom; row++) {
				for (int col = left; col < right; col++) {
					this.imgMap.getPixel(col, row, val);
					
					if (val.value == other_id) {
						this.imgMap.setPixel(col, row, id);
					}
				}
			}
		}
		
		public int distanceX (Blob other) {
			return Rect.distanceX (this.rect, other.rect);
		}
		
		public int distanceY (Blob other) {
			return Rect.distanceY (this.rect, other.rect);
		}
		
		// os parâmetros x_ini e x_end são relativos ao left do blob.
		public int MinConnection (int x_ini, int x_end) {
			// os parâmetros x_ini e x_end são relativos ao left do blob.
			x_ini += rect.x;
			x_end += rect.x;
			int x_min = x_ini;
			double min = 0;
			int top = rect.y;
			int bottom = rect.getBottom();
			RefInt val = new RefInt();
			
			for (int x = x_ini; x <= x_end; x++) {
				// @todo : ao invés de comparar pela soma de pixeis, pode ser melhor
				// comparar pela distância do pixel mais alto ao mais baixo, ou pelo menor
				// valor das duas formas.
				int sum = 0;
				
				for (int row = top; row < bottom; row++) {
					this.imgMap.getPixel(x, row, val);
					
					if (val.value == id) {
						img.getPixel(x, row, val);
						sum += val.value;
					}
				}
				
				if (sum < min) {
					min = sum;
					x_min = x;
				}
			}
			
			return x_min - rect.x;
		}

		public void Clamp (Rect roi) {
			// TODO : não basta somente redimensionar o retângulo, é preciso apagar os pixeis das regiões excluídas no mapa,
			// melhor ainda se o procedimento fosse apagar o blob original e reescanea-lo na nova região.
			this.rect = roi;
		}

		// implementada em 23/05/2010 para debugar o escaneamento do blob.
		public void saveMap (String filename) throws Exception {
			Image imgOut = new Image(img.getWidth(), img.getHeight(), 3);
			int maxValByChannel = imgOut.getMaxValueByChannel();
			int[] colorFg = {maxValByChannel, maxValByChannel, maxValByChannel};
			int[] colorBg = {0, 0, 0};
			int row, col;
			int imgWidth = this.imgMap.getWidth();
			int imgHeight = this.imgMap.getHeight();
			RefInt val = new RefInt();
			
			for (row = 0; row < imgHeight; row++) {
				for (col = 0; col < imgWidth; col++) {
					this.imgMap.getPixel(col, row, val);
					
					if (val.value == this.id) {
						imgOut.setPixel(col, row, colorFg);
					} else {
						imgOut.setPixel(col, row, colorBg);
					}
				}
			}
			
			ImageFormat.saveBMP(imgOut, filename);
			imgOut = null;
		}
		
		// mapa de movimento para a pesquisa de pontos conectados, deve priorizar a direita e o centro..
		final int[] dx = {+0, +1, +1, -1, -1, -1, 0, +1};
		final int[] dy = {+1, +1, +0, +1, 0, -1, -1, -1};
		
		/// <summary>
		/// Copia este pixel e todos os outros conectados a ele e todos os pixels conectados a eles,
		/// processando recursivamente até pegar todos os pixeis interligados.
		/// Os vizinhos não preferenciais, são adicionados à lista de candidatos para serem inspecionados no próximo loop.
		/// </summary>
		/// <param name="id">código que será atribuído a este blob e que o diferenciará dos demais</param>
		/// <param name="col">coordenada x da semente de pesquisa</param>
		/// <param name="row">coordenada y da semente de pesquisa</param>
		/// <param name="img">imagem para escanear<see cref="Image"/></param>
		/// <param name="map">mapa de blobs onde será marcado cada pixel deste blob, deverá ter as mesmas dimensões
		/// da img<see cref="Image"/></param>
		/// <param name="roi">região de interesse para fazer a pesquisa<see cref="Rectangle"/></param>
		/// <param name="colorBg">cor de fundo, todas as partes da imagem nesta cor serão desconsideradas</param>
		/// <param name="pixelTolerance">tolerância de comparação entre um pixel e seus vizinhos</param>
		/// <param name="meanTolerance">tolerância de comparação entre a cor média do blob um pixel na sua
		/// fronteira que seja candidato a pertencer ao blob</param>
		/// <param name="aux">classe com as variaveis temporárias para otimização do garbage colector, devem ser criada por quem
		/// instancia este construtor, com isto o garbage colector só vai atuar depois do escaneamento de todos os blobs</param>
		public Blob (int id, int col, int row, Image img, Image imgMap, Image imgMapAux, Rect roi,
	                        int[] colorBg, int pixelTolerance, int meanTolerance, BlobBuildAux aux)
		{
			int[] cols = aux.cols;
			int[] rows = aux.rows;
			int[] pixelRef = aux.pixelRef;
			int[] pixel = aux.pixel;
			int[] accumulate = aux.getCleanAccumulate();
		    img.accumulate(accumulate, pixel);

			this.id = id;
			this.img = img;
			this.imgMap = imgMap;
			this.imgMapAux = imgMapAux;
			this.colorBg = colorBg;
			this.pixelTolerance = pixelTolerance;
			this.meanTolerance = meanTolerance;
			this.filledArea = 1;
			this.filledMeanPixel = new int[img.numChannels];
			img.getPixel(col, row, this.filledMeanPixel);
			this.filledCenter = new Point(col, row);
			RefInt auxId = new RefInt();
			
			int left = col;
			int right = col;
			int top = row;
			int bottom = row;
			
			int minLeft = roi.x;
			int maxRight = roi.getRight()-1;
			int minTop = roi.y;
			int maxBottom = roi.getBottom()-1;
			
			this.imgMap.setPixel(col, row, id);
		    
		    rows[0] = row;
		    cols[0] = col;
			int numPointsReminder = 1;

			// fica em loop até que a lista de vizinhos seja esvaziada.
			while (numPointsReminder > 0) {
				// pega o primeiro ponto para examinar.
				int refCol = cols[numPointsReminder-1];
				int refRow = rows[numPointsReminder-1];
				numPointsReminder--;
				img.getPixel(refCol, refRow, pixelRef);
		
				for (int i = 0; i < dx.length && i < dy.length; i++) {
					// mapeia o próximo vizinho para teste.
					col = refCol + dx[i];
					row = refRow + dy[i];
					
					if (col < minLeft || col > maxRight || row < minTop || row > maxBottom) {
						continue;
					}
					
					img.getPixel(col, row, pixel);
					// se é fundo, então pula para o próximo.
					if (img.isEqual(pixel, colorBg) == true) {
						continue;
					}
					// se este vizinho já pertence a algum blob, então pula para o próximo.
					this.imgMap.getPixel(col, row, auxId);
					if (auxId.value != 0) {
						continue;
					}
					// checa se pertence a este blob.
					if (img.isInRange(pixelRef, pixel, pixelTolerance) == true || img.isInRange(this.filledMeanPixel, pixel, meanTolerance) == true) {
						this.imgMap.setPixel(col, row, id);
						// não posso me esquecer de coloca-lo na lista para examinar também os seus vizinhos mais tarde.
						cols[numPointsReminder] = col;
						rows[numPointsReminder] = row;
						numPointsReminder++;
						// atualiza os limites do blob.
						if (col < left) {
							left = col;
						} else if (col > right) {
							right = col;
						}
						
						if (row < top) {
							top = row;
						} else if  (row > bottom) {
							bottom = row;
						}
						// atualiza as demais características do blob.
						this.filledArea++;
						this.filledCenter.x += col;
						this.filledCenter.y += row;
					    img.accumulate(accumulate, pixel);
					    img.divide(this.filledMeanPixel, accumulate, this.filledArea);
					}
				}
			}
			
			// atualiza as dimensões do blob
			this.rect = new Rect(left, top, (right-left)+1, (bottom-top)+1);
			this.filledCenter.x /= this.filledArea;
			this.filledCenter.y /= this.filledArea;
		}
	}

	public class BlobComparer implements Comparator<Blob> {
		
		public static final int SORT_BY_LeftAndWidthAndHeight = 0;
		public static final int SORT_BY_TopAndHeight = 1;
		public static final int SORT_BY_FilledAreaSmallToBig = 2;
		public static final int SORT_BY_FilledAreaBigToSmall = 3;
		
		private int type;
		
		public int getType() {
			return type;
		}

		public void setType(int type) {
			this.type = type;
		}

		public int compare(Blob o1, Blob o2) {
			Blob blob = (Blob) o1;
			Blob other = (Blob) o2;
			Rect blobRect = blob.getRect(); 
			Rect otherRect = other.getRect(); 
			int blobFilledArea = blob.getFilledArea();
			int otherFilledArea = other.getFilledArea();
			if (type == SORT_BY_LeftAndWidthAndHeight) {
				if (blobRect.x < otherRect.x)
					return -1;
				if (blobRect.x > otherRect.x)
					return +1;
				if (blobRect.width > otherRect.width)
					return -1;
				if (blobRect.width < otherRect.width)
					return +1;
				if (blobRect.height > otherRect.height)
					return -1;
				if (blobRect.height < otherRect.height)
					return +1;
			} else if (type == SORT_BY_TopAndHeight) {
				if (blobRect.y < otherRect.y)
					return -1;
				if (blobRect.y > otherRect.y)
					return +1;
				if (blobRect.height > otherRect.height)
					return -1;
				if (blobRect.height < otherRect.height)
					return +1;
			} else if (type == SORT_BY_FilledAreaSmallToBig) {
				if (blobFilledArea > otherFilledArea)
					return +1;
				if (blobFilledArea < otherFilledArea)
					return -1;
			} else if (type == SORT_BY_FilledAreaBigToSmall) {
				if (blobFilledArea < otherFilledArea)
					return +1;
				if (blobFilledArea > otherFilledArea)
					return -1;
			}
			return 0;
		}
	}
	
	// Cópia interna da imagem original.
	Image img;
	private int imgWidth;
	private int imgHeight;
	private int imgNumChannels;
	// Mapa de blobs, contém ítem é o indices do blob a que o pixel correspondente pertence.
	private Image imgMap;
	private Image imgMapAux;
	private BlobComparer comparer;
	// lista de blobs detectados.
	private ArrayList<Blob> blobs;
	private Object debugDir;
	private Logger logger;
	
	public void setLogger(Logger logger) {
		this.logger = logger;
	}
	
	@SuppressWarnings("unused")
	private void log(String msg) {
		if (this.logger != null) {
//			this.logger.log(msg);
		}
	}
	
	public void saveMapColor(Image imgOut) {
		imgOut.clear ();
		int m = imgOut.getMaxValueByChannel();
		int[] vals = {m/5, 2*m/5, 3*m/5, 4*m/5, m};
		int numVals = vals.length;
		int numColors = numVals * numVals * numVals;
		int[] colorsRed = new int[numColors];
		int[] colorsGreen = new int[numColors];
		int[] colorsBlue = new int[numColors];
		
		for (int red = 0, index = 0; red < numVals; red++) {
			for (int green = 0; green < numVals; green++) {
				for (int blue = 0; blue < numVals; blue++, index++) {
					colorsRed[index] = vals[red];
					colorsGreen[index] = vals[green];
					colorsBlue[index] = vals[blue];
				}
			}
		}
		
		int[] pixel = new int[3];
		int row, col;
		int imgWidth = this.imgMap.getWidth();
		int imgHeight = this.imgMap.getHeight();
		RefInt refId = new RefInt();
		
		for (row = 0; row < imgHeight; row++) {
			for (col = 0; col < imgWidth; col++) {
				imgMap.getPixel(col, row, refId);
				
				if (refId.value == 0) {
					pixel[Image.CHANNEL_RED] = 0;
					pixel[Image.CHANNEL_GREEN] = 0;
					pixel[Image.CHANNEL_BLUE] = 0;
				} else {
					int cor = refId.value % numColors;
					pixel[Image.CHANNEL_RED] = colorsRed[cor];
					pixel[Image.CHANNEL_GREEN] = colorsGreen[cor];
					pixel[Image.CHANNEL_BLUE] = colorsBlue[cor];
				}
				
				imgOut.setPixel(col, row, pixel);
			}
		}
	}
	
	public void saveMap(String filename) throws Exception {
		Image imgOut = new Image(img.getWidth(), img.getHeight(), 3);
		saveMapColor(imgOut);
		ImageFormat.saveBMP(imgOut, filename);
		imgOut = null;
	}
	
	public void drawRects(Image img) {
		int[] color = new int[] {127, 127, 127};
		
		for (int i = 0; i < blobs.size(); i++) {
			Rect rect = new Rect(blobs.get(i).getRect());
			rect.grow(1, 1, 0, 0, img.getWidth(), img.getHeight());
			img.drawRectangle(rect, color, 0.75, 1);
			rect = null;
		}
	}

	public void saveSamples(ArrayList<ArrayList<Blob>> lines, Image imgAux, String dirBase, String symbols) throws Exception {
		for (int i = 0, index = 0; i < lines.size(); i++) {
			ArrayList<Blob> line = lines.get(i);
			
			for (int j = 0; j < line.size() && index < symbols.length(); j++, index++) {
				Blob blob = line.get(j);
				blob.makeSample(imgAux);
				char symbol = symbols.charAt(index);
				File dir = new File(dirBase +File.separator + symbol);
				
				if (dir.exists() == false) {
					dir.mkdirs();
				}
				
				String filename = String.format("%s%s%c_%d_%s.bmp", dir, File.separator, symbol, index, symbols);
				ImageFormat.saveBMP(imgAux, filename);
				dir = null;
				filename = null;
			}
		}
	}

	// remove todos os blobs, mas sem apaga-los na imagem original, isto é útil quando se quer rodar um novo scan
	// com novas tolerâncias, mas descartando regiões que foram eliminados pelas chamadas aos métodos Remove ().
	public void clear () {
		if (blobs != null) {
			for (int i = 0; i < blobs.size(); i++) {
				Blob blob = (Blob) blobs.get(i);
				if (blob != null) {
					blob.dispose ();
					blobs.set(i, null);
				}
			}
			blobs.clear ();
		}
	}
	
	public void dispose () {
		if (img != null) {
			img.dispose ();
			img = null;
		}

		if (imgMap != null) {
			imgMap.dispose ();
			imgMap = null;
		}
		
		if (imgMapAux != null) {
			imgMapAux.dispose ();
			imgMapAux = null;
		}
		
		comparer = null;
		// limpa a lista de blobs.
		clear ();
		blobs = null;
	}
	
	public Blob getBlob(int index) {
		return (Blob) blobs.get(index);
	}
	
	public int size() {
		return blobs.size();
	}
	
	public void sort(int type) {
		comparer.setType(type);
		Collections.sort(blobs, comparer);
	}

	public void remove(int index, boolean erase) {
		Blob blob = (Blob) blobs.get(index);
		if (erase == true)
			blob.erase();
		blob.dispose();
		blob = null;
		blobs.remove(index);
	}
	
	public void remove(Blob blob) {
		int index = blobs.indexOf(blob);
		remove(index, true);
	}
	
	public void unionNears(int maxDistanceX, int maxDistanceY, int maxWidth, int maxHeight) {
		Rect rect = new Rect();
		
		for (int i = 0; i < blobs.size()-1; i++) {
			Blob blob = (Blob) blobs.get(i);
			
			for (int j = i+1; j < blobs.size(); j++) {
				Blob other = (Blob) blobs.get(j);
				// verifica se consegue cumprir as condições no eixo X e no eixo Y.
				if (blob.distanceX (other) <= maxDistanceX && blob.distanceY (other) <= maxDistanceY) {
					// verifica se o retângulo resultante não extrapola o máximo permitido.
					Rect.union(blob.getRect(), other.getRect(), rect);
					
					if (rect.width <= maxWidth && rect.height <= maxHeight) {
                    	blob.union(other);
						remove(j, false);
						
						if (blobs.size() > 0)
							j--;
					}
				}
			}
		}
		
		rect = null;
		
		if (this.debugDir != null) {
			try {
				saveMap(this.debugDir + "union_nears.bmp");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private int[] clone(int[] source) {
		int[] ret = new int[source.length];
		
		for (int i = 0; i < source.length; i++) {
			ret[i] = source[i];
		}
		
		return ret;
	}
	
	// Divide objetos mais largos que maxWidth, porém o objeto dividido não pode ser menor que minWidth.
	public void splitLarges(int maxWidth, int minWidth) {
	    for (int i = 0; i < blobs.size(); i++) {
			Blob blob = (Blob) blobs.get(i);
			Rect rect = blob.getRect();
	        if (rect.width > maxWidth) {
	            // acha o ponto de menor conexão entre o final do caracter mais fino
	            // e o final do caracter mais largo.
				// x_ini e x_end são relativos ao left do blob.
	            int x_ini = minWidth;
	            int x_end = maxWidth;
				int maxRight = rect.width - minWidth;
	            if (x_end > maxRight)
	                x_end = maxRight;
	            if (x_end > x_ini) {
					int x = blob.MinConnection (x_ini, x_end);
					int[] colorBg = clone(blob.getColorBg());
					int pixelTolerance = blob.getPixelTolerance();
					int meanTolerance = blob.getMeanTolerance();
					// deleta o blob original, mas sem apagar na imagem.
					remove (i, false);
					if (blobs.size() > 0)
						i--;
					blob = null;
					// primeiramente reescaneia a parte da esquerda, adicionando os blobs encontrados.
					Rect rectLeft = new Rect(rect);
					rectLeft.width = x;
					scan(rectLeft, colorBg, pixelTolerance, meanTolerance);
					// depois reescaneia a parte da direita, adicionando os blobs encontrados.
					Rect rectRight = new Rect(rect);
					rectRight.x += x;
					rectRight.width -= x;
					scan(rectRight, colorBg, pixelTolerance, meanTolerance);
	            }
	        }
	    }
		
		if (this.debugDir != null) {
			try {
				saveMap(this.debugDir + "split_larges.bmp");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	// minSplit deve ser menor que maxSplit, mas só pode ser zero se os caracteres não se dividem.
	public void union(int minBlobs, int maxBlobs, int minWidth, int maxWidth, int minSplit, int maxSplit, int maxHeight) {
		// ordena da esquerda para a direita.
		sort (BlobComparer.SORT_BY_LeftAndWidthAndHeight);
		Rect rect = new Rect();
		boolean found;
		// vai juntando da esquerda para a direita :
		// -> se menor que minWidth e menos separados que maxSplit,
		// ou
		// -> se maior que minWidth e menor que maxWidth e menos separado que minSplit.
		// -> se menor que maxHeight
		do {
			found = false;
			for (int i = 0; i < blobs.size()-1; i++) {
				Blob blob = (Blob) blobs.get(i);
				for (int j = i+1; j < blobs.size(); j++) {
					Blob other = (Blob) blobs.get(j);
	                // calcula a largura e a separação do candidato a novo objeto.
	                int obj_width = other.getRect().getRight() - blob.getRect().x;
	                int obj_split = other.getRect().x - blob.getRect().getRight();
					Rect.union(blob.getRect(), other.getRect(), rect);
	                // se a separação for maior que a máxima largura possível para um caracter,
	                // então para de tentar unir no objeto 'i'.
	                if (obj_split > maxWidth)
	                    break;
	                if (obj_width > maxWidth)
	                    continue;
	                if (rect.height > 1.35 * maxHeight)
	                    continue;
	                // une se
	                //  mais estreito que o caracter mais fino
	                //  e menos separado que a maior separação
	                // une se
	                //  a separação for menor que a menor separação
	                //  possível entre caracteres.
	                if ((obj_width < minWidth && obj_split < maxSplit) || (obj_split < minSplit)) {
	                    blob.union (other);
						remove (j, false);
						if (blobs.size() > 0)
							j--;
	                    found = true;
	                }
				}
			}
		} while (found == true);
		rect = null;
		// TODO : inserir código para separar os blobs maiores se sobrou menos que deveria.
	}
	
	// une blobs colados próximos cuja cor média esteja na tolerância.
	public void union(int colorTolerance, int maxGapX, int maxGapY) {
		boolean join;
//		int numJoins = 0;
		do {
			join = false;
			// varre todos contra todos, procurando vizinhos próximos
			for (int i = 0; i < blobs.size()-1; i++) {
				Blob blob = (Blob) blobs.get(i);
				for (int j = i+1; j < blobs.size(); j++) {
					Blob other = (Blob) blobs.get(j);
					// verifica se há compatibilidade.
					if (blob.distanceX (other) <= maxGapX && blob.distanceY (other) <= maxGapY &&
						 img.isInRange (blob.getFilledMeanPixel(), other.getFilledMeanPixel(), colorTolerance) == true)
					{
						blob.union (other);
						remove (j, false);
						
						if (blobs.size() > 0)
							j--;
		
						join = true;
//						numJoins++;
					}
				}
			}
		}
		while (join == true);
	}
	
	// une blobs colados próximos cuja cor média esteja na tolerância.
	public void union(int colorTolerance) {
		// TODO : varre o mapa de blobs, procurando os que pode unir.
	}
	
	// remove os objetos dentro da cor mais a tolerância.
	public void	removeIn(int[] color, int tolerance) {
		for (int i = 0; i < blobs.size(); i++) {
			Blob blob = (Blob) blobs.get(i);
			
			if (img.isInRange (color, blob.getFilledMeanPixel(), tolerance) == true) {
				remove (i, true);
				
				if (blobs.size() > 0)
					i--;
			}
		}
	}

	// remove os objetos fora da cor mais a tolerância.
	public void	removeOut(int[] color, int tolerance) {
		for (int i = 0; i < blobs.size(); i++) {
			Blob blob = (Blob) blobs.get(i);
			
			if (img.isInRange (color, blob.getFilledMeanPixel(), tolerance) == false) {
				remove (i, true);
				
				if (blobs.size() > 0)
					i--;
			}
		}
	}

	public void removeSmall(int size, boolean erase) {
		for (int i = 0; i < blobs.size(); i++) {
			Blob blob = (Blob) blobs.get(i);
			if (blob.getRect().width * blob.getRect().height <= size) {
				remove (i, erase);
				if (blobs.size() > 0)
					i--;
			}
		}
		
		if (this.debugDir != null) {
			try {
				saveMap(this.debugDir + "remove_small.bmp");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void removeSmall(int minWidth, int minHeight, boolean both) {
		if (both == true) {
			for (int i = 0; i < blobs.size(); i++) {
				Blob blob = (Blob) blobs.get(i);
				if (blob.getRect().width < minWidth && blob.getRect().height < minHeight) {
					remove (i, true);
					if (blobs.size() > 0)
						i--;
				}
			}
		} else {
			for (int i = 0; i < blobs.size(); i++) {
				Blob blob = (Blob) blobs.get(i);
				if (blob.getRect().width < minWidth || blob.getRect().height < minHeight) {
					remove (i, true);
					if (blobs.size() > 0)
						i--;
				}
			}
		}
	}
	
	public void removeBig(int size) {
		for (int i = 0; i < blobs.size(); i++)
		{
			Blob blob = (Blob) blobs.get(i);
			if (blob.getRect().width * blob.getRect().height >= size)
			{
				remove (i, true);
				if (blobs.size() > 0)
					i--;
			}
		}
	}
	
	public void removeBig(int maxWidth, int maxHeight, boolean both) {
		if (both == true)
		{
			for (int i = 0; i < blobs.size(); i++)
			{
				Blob blob = (Blob) blobs.get(i);
				if (blob.getRect().width > maxWidth && blob.getRect().height > maxHeight)
				{
					remove (i, true);
					if (blobs.size() > 0)
						i--;
				}
			}
		}
		else
		{
			for (int i = 0; i < blobs.size(); i++)
			{
				Blob blob = (Blob) blobs.get(i);
				if (blob.getRect().width > maxWidth || blob.getRect().height > maxHeight)
				{
					remove (i, true);
					if (blobs.size() > 0)
						i--;
				}
			}
		}
		
		if (this.debugDir != null) {
			try {
				saveMap(this.debugDir + "remove_big.bmp");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	// remove objetos isolados por maxDistX e maxNearY.
	// remove objetos isolados que são menores que o menor objeto permitido.
	// remove objetos sem pelo menos minCount vizinhos dentro de regionWidth e regionHeight.
	public void removeGroupIsolated(int colorTolerance,
                            							int minWidth, int minHeight, int maxNearX, int maxNearY,
                            							int regionWidth, int regionHeight, int minCount, int minSumSize) {
		Rect rect = new Rect(); 
		int numRemove;
		// 2010_03_06 - fica em loop enquanto tiver removido alguém, pois a remoção de um ítem pode reverter a situação
		// de um ítem anterior que foi aprovado, por exemplo, a última chamda deste método na imagem NJD9603.
		do {
			numRemove = 0;
			// varre todos contra todos.
			for (int i = 0; i < blobs.size(); i++) {
				Blob blob = (Blob) blobs.get(i);
				
				int count = 1;
				int sumSize = blob.getFilledArea();
				int minDistX = regionWidth;
				int minDistY = regionHeight;
				int minDist = regionWidth + regionHeight;
				
				for (int j = 0; j < blobs.size(); j++) {
					if (i != j) {
						Blob other = (Blob) blobs.get(j);
						Rect.union(blob.getRect(), other.getRect(), rect);
						// verifica se consegue cumprir as condições no eixo X e no eixo Y.
						if (
						    	rect.width <= regionWidth && rect.height <= regionHeight &&
								img.isInRange (blob.getFilledMeanPixel(), other.getFilledMeanPixel(), colorTolerance) == true
						    )
						{
							count++;
							sumSize += other.getFilledArea();
							
							int distX = blob.distanceX (other);
							int distY = blob.distanceY (other);
							
							if (distX < 0)
								distX = 0;
							
							if (distY < 0)
								distY = 0;
							
							int dist = (int) Math.sqrt (distX * distX + distY * distY);
							
							if (dist < minDist)
								minDist = dist;
						
							if (distX < minDistX)
								minDistX = distX;
						
							if (distY < minDistY)
								minDistY = distY;
						}
					}
				}
				// verifica se obedece as condições individuais.
				// minDist não pode ser maior que zero quando o blob é menor que mínimo permitido,
				// esta restrição foi implementada para o leitor de placas.
				if ((blob.getRect().width < minWidth || blob.getRect().height < minHeight) && minDist > 0)	{
					remove (i, true);
					if (blobs.size() > 0)
						i--;
					numRemove++;
				// verifica se obedece as condições de grupo.
				} else if (minDistX > maxNearX || minDistY > maxNearY || count < minCount || sumSize < minSumSize)	{
					remove (i, true);
					if (blobs.size() > 0)
						i--;
					numRemove++;
				}
			}
		} while (numRemove != 0);
		
		rect = null;
	}
	
	/// 2010_03_06 - remove os blobs quem tem razão de filledArea muito alta
	public void removeBlocks(double threshouldFilled) {
		for (int i = 0; i < blobs.size(); i++) {
			Blob blob = (Blob) blobs.get(i);
			
			double factor = (double) blob.getFilledArea() / (double) (blob.getRect().width * blob.getRect().height);
			
			if (factor >= threshouldFilled) {
				remove (i, true);
				
				if (blobs.size() > 0) {
					i--;
				}
			}
		}
	}
	
	public ArrayList<ArrayList<Blob>> getLine() {
		ArrayList<ArrayList<Blob>> lines = new ArrayList<ArrayList<Blob>>(256);
		ArrayList<Blob> line = new ArrayList<Blob>(256);
		lines.add(line);
		sort(BlobComparer.SORT_BY_LeftAndWidthAndHeight);
		
		for (Blob blob : blobs) {
			line.add(blob);
		}
		
		return lines;
	}
	// para separar por linhas,
	// primeiro vou ordenar por top e altura,
	// à medida que varro a lista, vou colocando os blobs que se encaixam nos anteriores,
	// até que ocorra o primeiro que não se encaixe.
	// após montar uma linha, ordeno seus blobs da esquerda para a direita.
	public ArrayList<ArrayList<Blob>> getMultiline(int maxLineHeight) {
		ArrayList<ArrayList<Blob>> lines = new ArrayList<ArrayList<Blob>>(256);
		sort(BlobComparer.SORT_BY_TopAndHeight);
		BlobComparer comparer = new BlobComparer();
		comparer.setType(BlobComparer.SORT_BY_LeftAndWidthAndHeight);
		int i = 0;
		// continua tentando criar linhas até varrer todos os blobs.
		while (i < blobs.size()) {
			// cada vez que chega aqui, é nova linha.
			ArrayList<Blob> line = new ArrayList<Blob>(256);
			lines.add(line);
			Blob blob = (Blob)blobs.get(i);
			line.add(blob);
			int refTop = blob.getRect().y;
			int refBottom = blob.getRect().getBottom();
			i++;
			while (i < blobs.size()) {
				blob = (Blob)blobs.get(i);
				int top = blob.getRect().y;
				int bottom = blob.getRect().getBottom();
				if (top > refBottom || bottom - refTop > maxLineHeight) {
					// passa para a próxima linha
					break;
				}
				line.add(blob);
				i++;
			}
			Collections.sort(line, comparer);
		}
		comparer = null;
		return lines;
	}

	public Rect getRegion() {
		Rect rect = this.img.getRect();
		int x = rect.getRight();
		int y = rect.getBottom();
		int right = 0;
		int botton = 0;
		
		for (int i = 0; i < blobs.size(); i++) {
			Blob blob = (Blob) blobs.get(i);
			Rect _rect = blob.rect;
			int _right = _rect.getRight();
			int _botton = _rect.getBottom();
			
			if (_rect.x < x) {
				x = _rect.x;
			}
			
			if (_rect.y < y) {
				y = _rect.y;
			}
			
			if (_right > right) {
				right = _right;
			}
			
			if (_botton > botton) {
				botton = _botton;
			}
		}
		
		rect = new Rect(x, y, right-x, botton-y);
		return rect;
	}
	
	public int maxHeight() {
		int max = 0;
		
		for (int i = 0; i < blobs.size(); i++) {
			Blob blob = (Blob) blobs.get(i);
			if (blob.getRect().height > max)
				max = blob.getRect().height;
		}
		
		return max;
	}

	public void meanColor(int[] pixelOut) {
		int count = blobs.size();
		int[] accumulate = new int[imgNumChannels];
		for (int c = 0; c < imgNumChannels; c++) {
			accumulate[c] = 0;
		}
		for (int i = 0; i < count; i++) {
			img.accumulate(accumulate, blobs.get(i).getFilledMeanPixel()); 
		}
		if (count != 0) {
			img.divide(pixelOut, accumulate, count);
		} else {
			img.divide(pixelOut, accumulate, 1);
		}
	}

	public double meanWidth() {
		int count = blobs.size();
		if (count <= 0)
			return 0;
		int sum = 0;
		for (int i = 0; i < count; i++) {
			Blob blob = (Blob) blobs.get(i);
			sum += blob.getRect().width;
		}
		return (double)sum/ (double)count;
	}

	public double meanHeight() {
		int count = blobs.size();
		if (count <= 0)
			return 0;
		int sum = 0;
		for (int i = 0; i < count; i++) {
			Blob blob = (Blob) blobs.get(i);
			sum += blob.getRect().height;
		}
		return (double)sum/ (double)count;
	}

	// Este método pode ser utilizado a qualquer momento para adicionar novos blobs na lista.
	// Ele é principalmente utilizado no método Split, onde é feita nova varredura sobre os objetos resultantes.
	public void scan(Rect roi, int[] colorBg, int pixelTolerance, int meanTolerance) {
		BlobBuildAux aux = new BlobBuildAux(this.img);
		RefInt refId = new RefInt();
		int left = roi.x;
		
		for (int row = roi.y; row < roi.getBottom(); row++) {
			for (int col = left ; col < roi.getRight(); col++) {
				img.getPixel(col, row, aux.pixel);
				imgMap.getPixel(col, row, refId);
				// basta que o pixel não tenha sido mapeado e que não seja fundo.
				if (refId.value == 0 && img.isEqual(aux.pixel, colorBg) == false) {
					// o próprio blob trata de setar os seus pixeis no mapa de blobs a medida que escanei seus pixeis,
					// isto vai evitar que um mesmo pixel seja mapeado em dois ou mais blobs.
					Blob blob = new Blob (blobs.size()+1, col, row, img, imgMap, this.imgMapAux, roi, colorBg, pixelTolerance, meanTolerance, aux);
					blobs.add (blob);
				}
			}
		}
		
		aux.dispose();
		aux = null;
	}
	
	// Este método é utilizado para limpar a lista de blobs pré-existentes e inserir os novos blobs detectados.
	// Ele é principalmente utilizado para a primeira varredura na imagem,
	public void scan(Image imgIn, Rect roi, int[] colorBg, int pixelTolerance, int meanTolerance) throws Exception {
		if (imgIn == null || imgIn.getWidth() != imgWidth || imgIn.getHeight() != imgHeight) {
			throw new Exception ("imgIn : Invalid image size");
		}
		// a primeira coisa é copiar a imagem original para um buffer interno.
		this.img.clear();
		Image.copy(imgIn, roi, this.img, roi);
		// limpar a lista de blobs.
		clear ();
		// e limpar a map.
		this.imgMap.clear();
		this.imgMapAux.clear();
 		scan(roi, colorBg, pixelTolerance, meanTolerance);
		
		if (this.debugDir != null) {
			saveMap(this.debugDir + "scan.bmp");
		}
	}
	
	public void reset(int imgWidth, int imgHeight, int imgNumChannels) {
		this.img.reset(imgWidth, imgHeight, imgNumChannels);
		this.imgMap.reset(imgWidth, imgHeight, 1);
		this.imgMapAux.reset(imgWidth, imgHeight, 1);
		this.imgWidth = imgWidth;
		this.imgHeight = imgHeight;
		this.imgNumChannels = imgNumChannels;
		// TODO : refazer a lista de blobs
	}
	
	public Blobs(int imgWidth, int imgHeight, int imgNumChannels) {
		this.imgWidth = imgWidth;
		this.imgHeight = imgHeight;
		this.imgNumChannels = imgNumChannels;
		this.imgMap = new Image(imgWidth, imgHeight, 1, 32);
		this.imgMapAux = new Image(imgWidth, imgHeight, 1);
		this.img = new Image(imgWidth, imgHeight, imgNumChannels);
		this.blobs = new ArrayList<Blob>(imgWidth * imgHeight);
		this.comparer = new BlobComparer();
	}
}
