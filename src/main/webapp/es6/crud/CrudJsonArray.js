import {CrudUiSkeleton} from "./CrudUiSkeleton.js";
import {Filter} from "./DataStore.js";
import {DatabaseUiAdapter} from "./ServerConnectionUI.js";
import {CrudService, ServerConnection} from "./ServerConnection.js";

class CrudJsonArray extends CrudUiSkeleton {

	constructor(parent, fields, fieldNameExternal, title, serverConnection, selectCallback) {
		super(serverConnection, fieldNameExternal, new DatabaseUiAdapter(serverConnection, fields), selectCallback);
		this.parent = parent;
		this.fieldNameExternal = fieldNameExternal;
		this.title = title;
	}

	get(parentInstance) {
		var data = parentInstance[this.fieldNameExternal];

		if (Array.isArray(data) == true) {
			this.list = data;
		} else if (typeof data === 'string' || data instanceof String) {
			this.list = JSON.parse(data);
		}
		
		this.filterResults = this.list;
		this.paginate();
	}

	clear() {
		this.list = [];
		super.clear();
	}
	// private, use in addItem, updateItem and removeItem
	updateParent() {
		this.parent.instance[this.fieldNameExternal] = this.list;
		return this.parent.update();
	}

	save() {
		// já verifica se é um item novo ou um update
		const primaryKey = this.getPrimaryKey(this.instance);

		if (Filter.findOne(this.list, primaryKey, index => this.list[index] = this.instance) == null) {
			this.list.push(this.instance);
		}

		this.updateParent();
		this.clear();
	}

	remove(index) {
		this.list.splice(index, 1);
		this.updateParent();
	}

	edit(index) {
		var item = this.list[index];
		this.setValues(item);
	}

	moveUp(index) {
		if (index > 0) {
			var tmp = this.list[index-1];
			this.list[index-1] = this.list[index];
			this.list[index] = tmp;
		}

		this.updateParent();
	}

	moveDown(index) {
		if (index < (this.list.length-1)) {
			var tmp = this.list[index+1];
			this.list[index+1] = this.list[index];
			this.list[index] = tmp;
		}

		this.updateParent();
	}

}

export {CrudJsonArray}
