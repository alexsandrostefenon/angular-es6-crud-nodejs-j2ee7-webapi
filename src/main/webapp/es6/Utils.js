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
	static strAsciiHexToFlags(strAsciiHex, numBits) {
		if (strAsciiHex == null || strAsciiHex.length == 0) {
			return null;
		}

		if (numBits == undefined) {
			numBits = 32;
		}

		const flags = new Array(numBits);

		for (let i = strAsciiHex.length-1, j = 0; i >= 0; i--) {
			let ch = strAsciiHex.charAt(i);
			let byte = parseInt(ch, 16);

			for (let k = 0; k < 4; k++, j++) {
				let bit = 1 << k;
				let value = byte & bit;
				let flag = value != 0 ? true : false;
	    		flags[j] = flag;
			}
		}

		return flags;
	}

	// faz o inverso da funcao strAsciiHexToFlags
	static flagsToStrAsciiHex(flags) {
		let value = 0;

		for (let i = 0; i < flags.length; i++) {
			let flag = flags[i];
			let bit = 1 << i;

			if (flag == true) {
				value |= bit;
			}
		}

		let strAsciiHex = value.toString(16);
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
