#!/usr/bin/env python
'''
Created on Dec 2015
@author: apoorva.jagadeesh@gmail.com
'''

import sys
import platform
import subprocess
import os
from random import *
import time


for i in range(1,2):
	print i; 
	#generate random bindary data
	randBinList = lambda size: tuple(random() < 0.5 for _ in range(size))
	x = randBinList(10);
	binaryData = ''.join(['1' if y else '0' for y in x]);
	#print binaryData;
	time_enter=time.time()
	#procopen = subprocess.Popen([sys.executable,'/Users/apoorvajagadeesh/iot/sendjsonnew.py',str(binaryData)]);
	procopen = subprocess.Popen([sys.executable,'/Users/apoorvajagadeesh/iot/sendjson.py']);
	time_exit=time.time() 
	time_connect=time_exit-time_enter  
	print "time for mqtt"
	print time_connect
	time_enter2=time.time()
	procopen2 = subprocess.Popen([sys.executable,'/Users/apoorvajagadeesh/iot/restpost.py']);
	time_exit2=time.time() 
	time_connect2=time_exit2-time_enter2  
	print "time for http rest"
	print time_connect2


