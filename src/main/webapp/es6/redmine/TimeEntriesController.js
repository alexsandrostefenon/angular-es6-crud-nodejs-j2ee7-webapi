import {CrudController} from "../crud/CrudController.js";

class TimeEntriesController extends CrudController {

    process(action, params) {
    	super.process(action, params);

		if (action == "new") {
			this.instance.createdOn = this.instance.spentOn = new Date();
			const lastTimeEntry = this.filterResults[this.filterResults.length-1];

			if (lastTimeEntry != undefined) {
				this.instance.userId = lastTimeEntry.userId;
				this.instance.projectId = lastTimeEntry.projectId;
				this.instance.issueId = lastTimeEntry.issueId;
				this.instance.activityId = lastTimeEntry.activityId;
				this.instance.comments = lastTimeEntry.comments;
				this.instance.tyear = 1;
				this.instance.tmonth = 1;
				this.instance.tweek = 1;
				this.instance.hours = 0.0;
			}

			this.setValues(this.instance);

			if (chrome != undefined && chrome.idle != undefined) {
				TimeEntriesController.listener = newState => {
					if (newState == "idle") {
						this.log("chrome.idle.onStateChanged : Idle Event !");
//						chrome.idle.setDetectionInterval(60);
						if (this.timeoutID == undefined && this.instance.hours == 0.0) this.timeoutID = setTimeout(() => this.requestAndProcessUserResponse(), 5 * 60 * 1000); // fifteen minutes
					} else if (newState == "active") {
						this.log("chrome.idle.onStateChanged : Active Event !");
						
						if (this.timeoutID != undefined) {
							window.clearTimeout(this.timeoutID);
							this.timeoutID = undefined;
						}
					}
				};

//		    	this.setHours();
//				chrome.idle.setDetectionInterval(5*60);
				chrome.idle.onStateChanged.addListener(TimeEntriesController.listener);
			} else {
				this.timeoutID = setTimeout(() => this.requestAndProcessUserResponse(), 5 * 60 * 1000); // fifteen minutes
			}
		}
    }

    log(text) {
    	if (this.textAreaLog == undefined) {
    		this.textAreaLog = document.createElement("textarea");
    		this.textAreaLog.rows= 10;
    		document.getElementById(this.formId).appendChild(this.textAreaLog);
    	}

		this.textAreaLog.value = new Date().toLocaleString() + "\n" + text + "\n" + this.textAreaLog.value;
		console.log(text);
    }

    requestAndProcessUserResponse() {
		this.timeoutID = undefined;
		this.setHours();
		this.log(`idle :\non\n${this.instance.createdOn}\noff\n${this.instance.updatedOn}`);

		if (chrome != undefined && chrome.windows != undefined) chrome.windows.getCurrent(win => chrome.windows.update(win.id, {focused: true}));

		let result = false;
		//if (document.hidden == false)
		result = window.confirm("Finish time-entry and Start New ?");

		if (result == true) {
			this.save().then(response => {
				this.instance.updatedOn = this.instance.createdOn = this.instance.spentOn = new Date();
				this.instance.hours = 0.0;
				this.log(`new record : ${this.instance.createdOn}`);
//				chrome.idle.setDetectionInterval(15*60);
			});
		} else {
			this.instance.hours = 0.0;
		}

		if (chrome == undefined || chrome.idle == undefined) {
			this.timeoutID = setTimeout(() => this.requestAndProcessUserResponse(), 5 * 60 * 1000); // fifteen minutes
		}
    }

    setHours() {
    	this.instance.updatedOn = new Date();
    	this.instance.hours = (this.instance.updatedOn.valueOf() - this.instance.createdOn.valueOf()) / (1000*60*60);
    	this.instance.hours = Math.round(this.instance.hours * 100.0) / 100.0;
    }
    
    save() {
    	this.setHours();
    	return super.save();
// ServerConnectionUI.changeLocationHash(this.crudService.path + "/" + "new");
    }

}

export {TimeEntriesController}
