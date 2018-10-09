using System;

namespace org.domain.commom {
	public class CaseConvert {

		public static String CamelToUnderscore(String str) {
            var ret = "";
            var lastIsUpper = true;

			for (var i = 0; i < str.Length; i++) {
                Char ch = str[i];

                if (ch >= 'A' && ch <= 'Z') {
					ch = Char.ToLower(ch);

                    if (lastIsUpper == false) {
                        ret = ret + '_' + ch;
                    } else {
                        ret = ret + ch;
                    }

                    lastIsUpper = true;
                } else {
                    ret = ret + ch;
                    lastIsUpper = false;
                }
            }

			if (ret.Length > 0 && ret[0] == '_') {
				ret = ret.Substring(1);
            }

            return ret;
        }

        public static String UnderscoreToCamel(String str, Boolean isFirstUpper) {
            String ret = "";
            int i = 0;
            Boolean nextIsUpper = false;
            // if isFirstUpper == false, force first upper sequence to lower
            if (isFirstUpper == false) {
				for (; i < str.Length; i++) {
                    char ch = str[i];

                    if (ch < 'A' || ch > 'Z') {
                        break;
                    }

                    ret = ret + Char.ToLower(ch);
                }
            } else {
                nextIsUpper = true;
            }

			for (; i < str.Length; i++) {
                char ch = str[i];

                if (nextIsUpper == true) {
					ch = Char.ToUpper(ch);
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
}
