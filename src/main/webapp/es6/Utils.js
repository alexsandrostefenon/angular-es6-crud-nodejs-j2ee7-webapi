export class Utils {

	// Comm
	static get EOT () {return 0x04};
	static get ACK () {return 0x06};
	static get DC3 () {return 0x13};
	static get NAK () {return 0x15};
	static get SYN () {return 0x16};
	static get ETB () {return 0x17};
	static get CAN () {return 0x18};

	static pack(buffer, offset, size, data) {
	      for (var i = 0; i < size; i++) {
	          buffer[i+offset] = data[i];
	      }

	  	return offset + size;
	}

	// retorna um array de boolean, um elemento para cada bit, ou seja, cada caracter ascii hex gera quatro elementos.
	static strAsciiHexToFlags(strAsciiHex) {
		if (strAsciiHex == null || strAsciiHex.length == 0) {
			return null;
		}

		var numNibbles = (strAsciiHex.length);
		var flags = new Array(numNibbles * 4);

		for (var i = 0, j = 0; i < numNibbles; i++) {
			var ch = strAsciiHex.charAt(i);
			var byte = parseInt(ch, 16);

			for (var k = 3; k >= 0; k--, j++) {
				var bit = 1 << j;
				var value = byte & bit;
				var flag = value != 0 ? true : false;
	    		flags[j] = flag;
			}
		}

		return flags;
	}

	// faz o inverso da funcao strAsciiHexToFlags
	static flagsToStrAsciiHex(flags) {
		var value = 0;
		var numNibbles = flags.length;

		for (var i = 0; i < numNibbles; i++) {
			var flag = flags[i];
			var bit = 0x80000000 >> i;

			if (flag == true) {
				value |= bit;
			}
		}

		var strAsciiHex = value.toString(16);

		while (strAsciiHex.length < numNibbles) {
			strAsciiHex = strAsciiHex + '0';
		}

		if (strAsciiHex.length > numNibbles) {
			strAsciiHex = strAsciiHex.substring(0, numNibbles);
		}

		return strAsciiHex;
	}

	static getFieldTypes() {
    	var types = ["i", "b", "s", "n1", "n2", "n3", "n4", "c", "datetime-local", "date", "time"];
		return types;
	}

	static clone(objRef, fields) {
		var obj = {};

		for (var fieldName of fields) {
			obj[fieldName] = objRef[fieldName];
		}

		return obj;
	}

	static findInList(list, value) {
		var index = -1;

		if (list != undefined) {
			for (var i = 0; i < list.length; i++) {
				if (list[i] == value) {
					index = i;
					break;
				}
			}
		}

		return index;
	}

	static padLeft(str, size, ch) {
		while (str.length < size) {
			str = ch + str;
		}

		return str;
	}
}
