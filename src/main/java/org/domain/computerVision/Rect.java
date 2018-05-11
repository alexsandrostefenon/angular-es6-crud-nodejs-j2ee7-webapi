/**
 * 
 */
package org.domain.computerVision;

import java.util.List;

/**
 * @author alexsandro
 * 
 */
public class Rect {
	public int x, y, width, height;
	
	public int getRight() {
		return x + width;
	}
	
	public int getBottom() {
		return y + height;
	}

	public void setRight(int right) {
		this.width = right - this.x; 
	}
	
	public void setBottom(int bottom) {
		this.height = bottom - this.y; 
	}
	
	public static int distanceX (Rect rect, Rect other)
	{
		int distance = -1;
		int right = rect.getRight();
		int otherRight = other.getRight();
		
		if (other.x >= right) {
			distance = other.x - (right - 1);
		} else if (otherRight - 1 <= rect.x) {
			distance = rect.x - (otherRight - 1);
		}
		
		return distance;
	}
	
	public static int distanceY (Rect rect, Rect other)
	{
		int distance = -1;
		int bottom = rect.getBottom();
		int otherBottom = other.getBottom();
		
		if (other.y >= bottom) {
			distance = other.y - (bottom - 1);
		} else if (otherBottom - 1 <= rect.y) {
			distance = rect.y - (otherBottom - 1);
		}
		
		return distance;
	}
	
	public static int distanceX (Rect rect, int x)
	{
		// DEBUG
		// Aproveitando pontos 205,346 em 0 -> 6 , 0 -> 9, distX = -200, distY = -338
		int distance = -1;
		int right = rect.getRight();

		if (x <= rect.x) {
			distance = rect.x - x;
		} else if (x >= right - 1) {
			distance = x - (right - 1);
		}
		
		return distance;
	}
	
	public static int distanceY (Rect rect, int y)
	{
		int distance = -1;
		int bottom = rect.getBottom();

		if (y <= rect.y) {
			distance = rect.y - y;
		} else if (y >= bottom - 1) {
			distance = y - (bottom - 1);
		}
		
		return distance;
	}
	
	/**
	 * 
	 */
	public Rect() {
		this.x = 0;
		this.y = 0;
		this.width = 0;
		this.height = 0;
	}

	/**
	 * @param r
	 */
	public Rect(Rect r) {
		assign(r);
	}
	
	public void assign(Rect r) {
		this.x = r.x;
		this.y = r.y;
		this.width = r.width;
		this.height = r.height;
	}

	/**
	 * @param width
	 * @param height
	 */
	public Rect(int width, int height) {
		this.x = 0;
		this.y = 0;
		this.width = width;
		this.height = height;
	}

	/**
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public Rect(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	public static void union(Rect rect, int x, int y) {
		int right = rect.getRight();
		int bottom = rect.getBottom();
		
		if (x < rect.x) {
			rect.x = x;
		} else if (x >= right) {
			rect.setRight(x+1);
		}
		
		if (y < rect.y) {
			rect.y = y;
		} else if (y >= bottom) {
			rect.setBottom(y+1);
		}
	}
	
	public static boolean union(Rect rect, Rect other, Rect result) {
		boolean enlarge = false;
		int right = rect.getRight();
		int bottom = rect.getBottom();
		int otherRight = other.getRight();
		int otherBottom = other.getBottom();
		
		if (other.x < rect.x) {
			result.x = other.x;
		} else {
			result.x = rect.x;
		}
		
		if (other.y < rect.y) {
			result.y = other.y;
		} else {
			result.y = rect.y;
		}
		
		if (otherRight > right) {
			result.width = otherRight - result.x;
			enlarge = true;
		} else {
			result.width = right - result.x;
		}
		
		if (otherBottom > bottom) {
			result.height = otherBottom - result.y;
			enlarge = true;
		} else {
			result.height = bottom - result.y;
		}
		
		return enlarge;
	}

	public static boolean intersection(Rect rect, Rect other, Rect result) {
		result.x = rect.x;
		result.y = rect.y;
		result.width = rect.width;
		result.height = rect.height;
		boolean reduce = false;
		
		if (distanceX(rect, other) > 0 || distanceY(rect, other) > 0) {
			result.width = 0;
			result.height = 0;
			return reduce;
		}
		
		int right = rect.getRight();
		int bottom = rect.getBottom();
		int otherRight = other.getRight();
		int otherBottom = other.getBottom();
		// verifica qual esquerda fica mais a direita
		if (rect.x > other.x) {
			result.x = rect.x;
//			reduce = true;
		} else if (other.x > rect.x) {
			result.x = other.x;
			reduce = true;
		}
		// verifica qual topo fica mais para baixo
		if (rect.y > other.y) {
			result.y = rect.y;
//			reduce = true;
		} else if (other.y > rect.y) {
			result.y = other.y;
			reduce = true;
		}
		// verifica qual direita fica mais a esquerda
		if (right < otherRight) {
			result.setRight(right);
			reduce = true;
		} else if (otherRight < right) {
			result.setRight(otherRight);
			reduce = true;
		}
		// verifica qual bottom fica mais para cima
		if (bottom < otherBottom) {
			result.setBottom(bottom);
			reduce = true;
		} else if (otherBottom < bottom) {
			result.setBottom(otherBottom);
			reduce = true;
		}
		
		return reduce;
	}
	
	public void grow(int i, int j, int minX, int minY, int maxX, int maxY) {
		this.x -= i;
		this.y -= j;
		this.width += i;
		this.height += j;
		if (this.x < minX)
			this.x = minX;
		if (this.y < minY)
			this.y = minY;
		if (this.getRight() > maxX)
			this.setRight(maxX);
		if (this.getBottom() > maxY)
			this.setBottom(maxY);
	}

	public void scale(int factor) {
		this.x /= factor;
		this.y /= factor;
		this.width /= factor;
		this.height /= factor;
	}
	
	public static boolean joinInners(List<Rect> list, int distToleranceX, int distToleranceY) {
		boolean ret = false;
		boolean joined;
		
		do {
			joined = false;
			
			for (int i = 0; i < list.size(); i++) {
				Rect rect = list.get(i);
				int j = i+1;
				
				while (j < list.size()) {
					Rect rectOther = list.get(j);
					int distX = Rect.distanceX(rect, rectOther);
					int distY = Rect.distanceY(rect, rectOther);
							
					if (distX < distToleranceX && distY < distToleranceY) {
						Rect.union(rect, rectOther, rect);
						list.remove(j);
						joined = true;
						ret = true;
					} else {
						j++;
					}
				}
			}
		} while (joined == true);

		return ret;
	}

	public static Rect findInner(List<Rect> list, int colRef, int rowRef, int distToleranceX, int distToleranceY) {
		Rect found = null;
		
		for (Rect rect : list) {
			int distX = Rect.distanceX(rect, colRef);
			int distY = Rect.distanceY(rect, rowRef);
					
			if (distX < distToleranceX && distY < distToleranceY) {
				// encerra a pesquisa de regiões
				found = rect;
				// DEBUG :
//				System.out.printf("Aproveitando pontos %d,%d em %s, distX = %d, distY = %d\n", colRef, rowRef, rect.toString(), distX, distY);
				break;
			} else {
//				System.out.printf("rect.x = %d, rect.right = %d, rect.y = %d, rect.bottom = %d\n", rect.x, rect.getRight(), rect.y, rect.getBottom());
//				System.out.printf("x = %d, y = %d, distX = %d, distY = %d\n", colRef, rowRef, distX, distY);								
			}
		}
		
		return found;
	}

	@Override
	public String toString() {
		return x + " -> " + getRight() + " , " + y + " -> " + getBottom();
	}

	
}
