##NodeRouter

#### Overview

NodeRouter is an implementation of an asynchronous linking between two services: A Java-based back end and a Node JS (Specifically the popular Sockets.io package front end). It's primary use is an illustration of how a small Node proxy can be implemented to address several common Java deficiencies.

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
