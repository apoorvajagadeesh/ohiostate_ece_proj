/*
Created on Nov 2016
@author: apoorva.jagadeesh@gmail.com
*/


var express = require('express');
var request = require('request');
var client = require('https');
var compress = require('compression');
var bodyParser     = require('body-parser');
var os = require('os');
var expressJwt = require('express-jwt');
var jwt = require('jsonwebtoken');
var influx = require('influx');
var sys = require('sys')


//exec("curl -i -XPOST 'http://192.168.0.10:8086/write?db=sensorstats' --data-binary 'accelaration,accx="+0.5+",accy="+0.1+",accz="+0.7+"'");

var dns = require('dns'); var addr;
dns.lookup(require('os').hostname(), function (err, add, fam) {
  console.log('addr: '+add);
  addr = add;
});

var influxClient = influx({
  host : '127.0.0.1',
  port : 8086, 
  username : 'root',
  password : 'root',
  database : 'sensorstats'
});


function getIpAddress(callback) {
    dns.lookup(require('os').hostname(), function (err, add, fam) {
        callback(add);
    });
}
var app = express();
app.use(compress());
app.use(bodyParser.urlencoded({ extended: false }));
app.use(bodyParser.json());

app.listen(8080);

app.get('/sensordata', function(req,res) {
return res.send("ok");
});

app.post('/accdata', function(req, res) {
    	console.log(req.body);
	//influxClient.writePoints('accelaration',point,function(err,dat){
    return res.send("ok");
});

