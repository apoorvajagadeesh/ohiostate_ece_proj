#!/usr/bin/env python
'''
Created on Nov 2015
@author: apoorva.jagadeesh@gmail.com
'''
# main.py
import paho.mqtt.publish as publish
from json import dumps
from ssl import PROTOCOL_TLSv1
import sys
import resource

class CarriotsMqttClient():
    host = 'mqtt.carriots.com'
    port = '1883'
    auth = {}
    topic = '%s/streams'
    tls = None

    def __init__(self, auth, tls=None):
        self.auth = auth
        self.topic = '%s/streams' % auth['username']
        if tls:
            self.tls = tls
            self.port = '8883'

    def publish(self, msg):
        try:
            publish.single(topic=self.topic, payload=msg, hostname=self.host, auth=self.auth, tls=self.tls, port=self.port)
        except Exception, ex:
            print ex


if __name__ == '__main__':
    auth = {'username': 'b8b2f11c98bf1ec63e697a8054a344c5ed9ba3e63d42a06b94061d1d89641e29', 'password': ''}
    #tls_dict = {'ca_certs': 'ca_certs.crt', 'tls_version': PROTOCOL_TLSv1}  # ssl version
    #byteArray = bytes("100001110101");
    byteArray = sys.argv[1];
    msg_dict = {'protocol': 'v2', 'device': 'defaultDevice@apojosu.apojosu', 'at': 'now', 'data': byteArray}
    client_mqtt = CarriotsMqttClient(auth=auth)                     # non ssl version
    #client_mqtt = CarriotsMqttClient(auth=auth, tls=tls_dict)      # ssl version
    client_mqtt.publish(dumps(msg_dict))
    #print resource.getrusage(resource.RUSAGE_SELF).ru_maxrss
