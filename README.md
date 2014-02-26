##NodeRouter


[![Build Status](https://travis-ci.org/joshdickson40/NodeRouter.png?branch=master)](https://travis-ci.org/joshdickson40/NodeRouter)


#### Overview

NodeRouter is an implementation of an asynchronous linking between two services: A Java-based back end and a Node JS (Specifically the popular Sockets.io package) front end. It's primary use is an illustration of how a lightweight Node proxy can be implemented to address several common Java deficiencies.

While web service back ends written in Java are typical, it is difficult to code stateful front ends without employing various HTTP hacks, such as long polling or frequent AJAX requests, which mimic a stable back end connection to listen for new data. In a long poll, for instance, the server never terminates the HTTP connection, which remains open for the duration of the connection and is highly inefficient. High frequency AJAX requests add unnecessary overhead, and are needed to constantly maintain front end status even when no updates have been made. 

NodeRouter illustrates a proxy server that avoids either technique, allowing a stateful front end to communicate with a Java service via Node's Socket.io package. Front end clients are able to communicate with the Node JS proxy server unaware of the Java implementation. Likewise, the Java side is able to communicate with the front end at any time via server events sent to the Node JS process.

NodeRouter's interaction with the Node server is non-blocking, meaning it's possible to continuously send and receive data. This allows:

1. Real time, event-driven listening to the connection
2. The ability to emit events at any point in time; regardless of listening for input


#### Usage

The Router class manages a process that is spawned to manage the Node server. A test server and a production server are included. The test server will simply echo messages that are sent to it by the Java process, which is handy for debugging. A new router instance is launched via:

```java
Router router = new Router("testproxy.js");
router.bind();
```

Messages can now be relayed to the server by calling:

```java
router.send("Message to send");
```

The router instance will automatically buffer received messages into an internal queue. The queue can be accessed via a getter:

```java
router.getQueue();
```

#### Running

To run the NodeRouter project, you must have Node, Socket.io, and node-static installed as well as a JVM. NodeRouter includes a ```package.json``` file to pull the requisite node dependencies once they are installed via npm (Node package manager).


#### Future Builds

Two opportunities for adding to the project include the ability to launch and manage multiple Node processes and configure the ports that they bind to, likely coming later in 2014.

#### License

This software is released under the MIT license http://opensource.org/licenses/MIT.

#### Contact

Forward any feedback to the author at josh dot dickson at wpi dot edu.

