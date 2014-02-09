##NodeRouter

#### Overview

NodeRouter is an implementation of an asynchronous linking between two services: A Java-based back end and a Node JS (Specifically the popular Sockets.io package) front end. It's primary use is an illustration of how a small Node proxy can be implemented to address several common Java deficiencies.

While backends coded in Java are extremely typical, it is difficult to code stateful front ends without employing various HTTP hacks, such as long polling or frequent AJAX requests, which mimic a stable back end connection to listen for new data. In a long poll, for instance, the server never terminates the HTTP connection, which remains open for the duration of the connection and is highly inefficient. High frequency AJAX requests add unnecessary overhead, and are needed to constantly maintain front end status even when no updates have been made. 

NodeRouter illustrates a proxy server that avoids either technique, allowing a stateful front end to communicate with a Java service via ordinary TCP/IP. Front end clients communicate, in Javascript via Socket.io, with the Node JS proxy server unaware of the Java implementation. Likewise, the Java side is able to communicate with the front end at any time via server events sent to the Node JS TCP link, and then passed on in real time to the Node JS front end listener. 

NodeRouter also illustrates usage of a non-blocking Java TCP backend link. This allows:

1. Real time, event-driven listening to the connection
2. The ability to emit events at any point in time; regardless of listening for input

... and is accomplished via an Executor that handles TCP inbound messages in an alternative to the thread available to write messages.

#### Socket Emissions

To simulate periodic server and client events, NodeRouter simulates front end JavaScript events and back-end Java events with frequencies of 5-10s each. Each end prints a transmission as it's being sent, and prints incoming events asynchronously. 

Here, the Java server simulates server events by calling ```link.emitString()``` with whatever transmission it wished to send:

```java
while(!link.isTerminated()) {
	try {
		int randomWait = (int) (5 + (Math.random() * 5)) + 1;
		TimeUnit.SECONDS.sleep(randomWait);
		System.out.println("Server event " + randomWait);
		link.emitString("Server event " + randomWait);
	} catch(Exception ex) {
		System.out.println("Connection dropped. Exiting.");
		System.exit(0);
	}
}
```

Here, the front end simulates a Javascript timeout and then sends a transmission:

```javascript
setTimeout(sendPayload, 1000);

function sendPayload() {
	waitTime = (Math.random() * 5000) + 5000; // 5000 - 10,000 range
	waitInt = Math.round(waitTime / 1000);
	console.log("Client event " + waitInt);
	socket.emit('clientData', "Client event " + waitInt);
 	setTimeout(sendPayload, waitTime);
}
```

#### Running

To run the NodeRouter project, you must have Node JS, Socket.io, and node-static installed as well as a JVM. NodeRouter includes a ```package.json``` file to pull the requisite node dependencies once they are installed via npm (Node package manager)

1. Install Node JS, Socket.io and node-static, along with a JVM (Rec. 1.7)
2. Build the Java project or download the built .jar file
3. Start the node router Java project from the command line or via IDE. Port 6514 will be used by default if none is given.
	```> java -jar noderouter.jar <tcp port>```
4. Start the node server from the command line.
	```> node proxy.js```
5. Point a modern browser that Socket.io supports to the proxy file (proxy.html)

The front end and back end will begin communicating when the HTTP connection comes online. Note that the Java backend will attempt to emit TCP server messages as soon as the connection is established, but the message will only be transmitted to the front end via the Node proxy after the HTTP connection has been established.

You may send additional runtime events to the backend via the instructions presented at the proxy.html page.

#### Running Node from Java

If the Node server is always to be run in conjunction with the Java TCP server, consider use of a ProcessBuilder which can launch, manage and monitor the Node JS server. 

#### License

This software is released under the MIT license http://opensource.org/licenses/MIT.

#### Contact

Forward any feedback to the author at josh dot dickson at wpi dot edu.



