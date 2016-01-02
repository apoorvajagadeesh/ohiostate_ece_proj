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
import os
import commands

def getbytes():
	result = commands.getoutput("netstat -ib")
	#print result
	line = result.split("\n");
	#print line[9];
	txbytes = line[9].split("  ")[10].split(" ")[2];
	#print txbytes
	return txbytes;

if __name__ == '__main__':
	for i in range(1,2):
		before1 = getbytes();
		procopen2 = os.system('python /Users/apoorvajagadeesh/iot/restpost.py');
		after1 = getbytes();
		tx1 = int(after1)-int(before1);
		print "Network usage for REST Post"
		print tx1;
		
		before2 = getbytes();
		procopen = os.system('python /Users/apoorvajagadeesh/iot/sendjson.py');
		after2 = getbytes();
		tx2 = int(after2)-int(before2);
		print "Network usage for MQTT publish"
		print tx2;
		