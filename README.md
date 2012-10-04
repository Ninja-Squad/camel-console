Camel Monitoring Console
========================

What is this ?
--------------

We think Camel lacks a good monitoring application, one that can control differents installs of the same app, aggregate the results, save them, show cool graphs over time, show real time data, send alerts, etc...

What can I do with it ?
-----------------------
For now, the console is a simple app manager where you can : 

* add a new application to monitor 
* add severals installs of this app 

The only thing necessary on your Camel application is to [enable JMX monitoring](http://camel.apache.org/camel-jmx.html) (easy peasy : 2 minutes, tops!).

Launching the app
-----------------

1. Start by cloning the project
1. Install MongoDB ans start an instance with 
```$ mongod```
1. Install the war in your favorite server (Tomcat or Jetty are fine)
1. Enjoy!

Rest API
--------

* add a new application

```$ curl -XPOST http://localhost:9006/apps -d '{"name":"camel-quote", "version":"1.0"}'```

* get all applications

```$ curl http://localhost:9006/apps```

* get an application by its name

```$ curl http://localhost:9006/apps/camel-quote```

* add a new install for an app

```$ curl -XPOST http://localhost:9006/apps/camel-quote -d '{"host":"localhost", "port":"1100"}' ```

* get this app to check the installs

```$ curl http://localhost:9006/apps/camel-quote```

* delete the application

```$ curl -XDELETE http://localhost:9006/apps/camel-quote```
