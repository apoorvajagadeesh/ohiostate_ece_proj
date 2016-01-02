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


for i in range(1,11):
	#generate random bindary data
	randBinList = lambda size: tuple(random() < 0.5 for _ in range(size))
	x = randBinList(10);
	binaryData = ''.join(['1' if y else '0' for y in x]);
	print binaryData;
	procopen = subprocess.Popen([sys.executable,'/Users/apoorvajagadeesh/iot/sendjsonnew.py',str(binaryData)]);



