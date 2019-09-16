#!/usr/bin/python3

import requests, json, sys

class Test:
	def __init__(self, url, user="spending", password="123456"):
		self.url = url + "/rest/"
		self.user = user
		self.password = password

	def diff(obj, obj_old):
		set_current, set_past = set(obj.keys()), set(obj_old.keys())
		intersect = set_current.intersection(set_past)
		ret = {};
		ret["added"] = set_current - intersect 
		ret["removed"] = set_past - intersect 
		ret["changed"] = set(o for o in intersect if obj_old[o] != obj[o])
		ret["unchanged"] = set(o for o in intersect if obj_old[o] == obj[o])
		return ret;

	def request(self, method, resource, action, params={}, obj=None):
		url = self.url + resource + "/" + action
		headers={"Authorization": "Token " + self.authctoken}
		print("test.request({}, {}, {}, {})".format(method, url, params, obj))
		return requests.request(method, url, params=params, data=obj, headers=headers)

	def create(self, resource, obj):
		response = self.request("POST", resource, "create", {}, obj)

		if response.status_code == 200:
			if response.headers['Content-Type'] == 'application/json; charset=utf-8':
				return response.json()
			else:
				raise Exception("test.create({}, {}) : response content_type invalid : {}".format(resource, obj, response.headers["Content-Type"]))
		else:
			raise Exception("test.create({}, {}) : response status_code error {}".format(resource, obj, response.status_code))

	def login(self):
		response = requests.post(self.url+"/authc", json={"userId": self.user,"password": self.password})
		#print(response.headers)

		if response.status_code == 200:
			if response.headers['Content-Type'] == 'application/json; charset=utf-8':
				login_response = response.json();
				self.authctoken = login_response['user']['authctoken'];
				#print(json.dumps(login_response['user'], indent=4));

class PersonTest(Test):
	def test(url):
		test = PersonTest(url)
		test.login()

		request_person = {"crudGroupOwner":2,
				"name":"WMS SUPERMERCADOS DO BRASIL LTDA","cnpjCpf":"93209765016110","ieRg":"0240296389",
				"cnae":4711302,"crt":1,"zip":"92310-001","country":1058,"uf":43,"city":4304606,"district":"Tabaí",
				"address":"Av. Guilherme Schell","addressNumber":"8800","credit":0}
		response_person = test.create("person", request_person)
		diff_person = Test.diff(response_person, request_person)
		#print("added : {}".format(diff_person["added"]))
		#print("removed : {}".format(diff_person["removed"]))
		#print("changed : {}".format(diff_person["changed"]))
		#print("unchanged : {}".format(diff_person["unchanged"]))

		if ("id" in diff_person["added"]):
			print("PersonTest.created new record, id : {}".format(response_person["id"]))
		else:
			raise Exception("test.person.create({}, {}) : response without id : {}".format(response_pserson))

url = "http://localhost:9080/crud"

if (len(sys.argv) >= 2):
	url = sys.argv[1]

PersonTest.test(url)
