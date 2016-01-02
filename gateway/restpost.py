#!/usr/bin/env python
'''
Created on Dec 2015
@author: apoorva.jagadeesh@gmail.com
'''

import time
import httplib
import commands
import json
import sys
import resource

SERVER_ENDPOINT = "api.carriots.com"
headers={'Content-type':'application/json','carriots.apikey':'b8b2f11c98bf1ec63e697a8054a344c5ed9ba3e63d42a06b94061d1d89641e29'}
msg_dict = {
        "protocol":"v1",
        "at":"now",
        "device":"defaultDevice@apojosu.apojosu",
        "data":{
                "temp":"25",
                "hum":"60"
        },
        "checksum":""
}
try:
        HTTP_CONN= httplib.HTTPConnection(SERVER_ENDPOINT) 
        HTTP_CONN.request("POST", '/streams', json.dumps(msg_dict), headers)
        response=HTTP_CONN.getresponse()
        #print response.status,response.reason,response.read()
        HTTP_CONN.close()
        #print resource.getrusage(resource.RUSAGE_SELF).ru_maxrss
except Exception as e:
        print e.message
        pass
