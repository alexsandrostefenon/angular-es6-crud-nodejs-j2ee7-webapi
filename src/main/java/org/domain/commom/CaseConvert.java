package org.domain.commom;

public class CaseConvert {

	public static String convertCaseUnderscoreToCamel(String str, boolean isFirstUpper) {
		String ret = "";
		int i = 0;
		boolean nextIsUpper = false;
		// if isFirstUpper == false, force first upper sequence to lower
		if (isFirstUpper == false) {
			for (; i < str.length(); i++) {
				char ch = str.charAt(i);
		
				if (ch < 'A' || ch > 'Z') {
					break;
				}
		
				ret = ret + Character.toLowerCase(ch);
			}
		} else {
			nextIsUpper = true;
		}
		
		for (; i < str.length(); i++) {
			char ch = str.charAt(i);
	
			if (nextIsUpper == true) {
				ch = Character.toUpperCase(ch);
				nextIsUpper = false;
			}
	
			if (ch == '_') {
				nextIsUpper = true;
			} else {
				ret = ret + ch;
			}
		}
	
		return ret;
	}

}
