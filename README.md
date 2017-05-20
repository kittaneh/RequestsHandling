# Requests Handler

### Description

The aim of the project to demonstrate handling massive amount of http requests, the system uses two approaches:

 * Slice/parse large Apache tomcat access_log file.
 * Simulate online requests with custom headers.

### Installation

First, You have to install Java 8 on your machine , and make sure JAVA_HOME variable is set.

Then, clone/fork the current project to a destination folder.

Use the following command inside your destination direcotry:
```javascript
./grailsw run-app
```
This command will make sure that Grails 2.5.6 is installed and fully configured on your machine.

### Usage

The system will run on your machine using the following url:

```javascript
http://localhost:8080/RequestsHandling
```
It supports the following features:

 * Slice: configured to slice the build-in access_log sample to web-app/temp.
 ```javascript
http://localhost:8080/RequestsHandling/home/slice
```

 * Parse: configured to parse the slices in web-app/temp.
 ```javascript
 http://localhost:8080/RequestsHandling/home/parse
```

* Simulate: configured to simulate onlie requests with custom headers.
 ```javascript
 http://localhost:8080/RequestsHandling/home/simulate
```

* Top: configured to show the top N requests sources domains or IPs.
 ```javascript
 http://localhost:8080/RequestsHandling/home/top/10
```

### Where to go next:

There is an out of the box support for dealing with huge logs files (maybe > 50GB), the idea is to use tools such as Flunetd along side MongoDB to parse and store data in NoSQL form. Take a look at these links for more details:

 * [What tools can I use to mine data from large Apache Tomcat log files?](https://www.quora.com/What-tools-can-I-use-to-mine-data-from-large-Apache-Tomcat-log-files)
 * [Store Apache Logs into MongoDB](http://docs.fluentd.org/v0.12/articles/apache-to-mongodb)