/**
 * The MIT License (MIT)
 * 
 * Copyright (c) 2014 Joshua Dickson
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package noderouter;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A class that illustrates the use of a non blocking TCP linker to a front end web service 
 * through a Node JS-based proxy server
 * @author Joshua Dickson <josh.dickson@wpi.edu>
 */
public class TCPLink implements Closeable {
	
	private final ExecutorService executor;
	private final DataOutputStream out;
	private final BufferedReader in;
	private final ServerSocket serverSocket;
	private final Socket connectionSocket;
	
	/**
	 * Run the project
	 * @param args the port on which to open the TCP connection
	 */
	public static void main(String[] args) {
		if(args.length == 0) {
			args = new String[]{"6514"};
		}
		
		TCPLink link = new TCPLink(Integer.parseInt(args[0]));
		link.bind();
		
		/**
		 * Simulate server events every 5-10s and send them to the browser 
		 */
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
				
		link.close();
		
		
	}
	
	/**
	 * Get whether or not the executor has terminated and completed all of its tasks
	 * @return
	 */
	public boolean isTerminated() {
		return executor.isTerminated();
	}

	
	/* (non-Javadoc)
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() {
		try {
			connectionSocket.close();
			serverSocket.close();
		} catch(Exception ex) {
			System.err.println("There was a problem shutting down.");
			System.exit(1);
		}
	}
	
	/**
	 * Construct a TCP link, blocking until a connection is established
	 */
	public TCPLink(int port) {
		
		/**
		 * Set up temporary sockets to be null to allow for final declaration
		 */
		DataOutputStream outTemporary = null;
		BufferedReader inTemporary = null;
		ServerSocket serverSocketTemporary = null;
		Socket connectionSocketTemporary = null;
		
		/**
		 * Try to complete the set up, exiting if the process fails
		 */
		try {
			serverSocketTemporary = new ServerSocket(port);
			System.out.println("Listening for incoming connection: ");
			connectionSocketTemporary = serverSocketTemporary.accept();
			outTemporary = new DataOutputStream(connectionSocketTemporary.getOutputStream());
			inTemporary = new BufferedReader(new InputStreamReader(
					connectionSocketTemporary.getInputStream()));
		} catch(Exception ex) {
			System.err.println("Error binding to the socket");
			System.exit(1);
		}
		
		/**
		 * Assign the true variables to their final values
		 */
		out = outTemporary;
		in = inTemporary;
		serverSocket = serverSocketTemporary;
		connectionSocket = connectionSocketTemporary;
		
		/**
		 * Set the executor
		 */
		executor = Executors.newSingleThreadExecutor();
	}
	
	/**
	 * Bind a listener to the TCP port which prints and echos incoming TCP lines, defining a
	 * line as a number of bytes ending in a new line or carriage return
	 */
	public void bind() {
		
		/**
		 * Submit a new task to the executor that listens to inbound TCP messages
		 */
		executor.submit(new Runnable() {
			
			/* (non-Javadoc)
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
				
				/**
				 * Print new lines while the connection is bound
				 */
				while(connectionSocket.isBound()) {
					
					try {
						/**
						 * Block on reading a new line
						 */
						String input = in.readLine();
						
						if(input == null) {
							break;
						} else {
							input = input.trim();
							System.out.println(input);
						}
						
					} catch(Exception ex) {
						ex.printStackTrace();
					}
				}
				
			}
		});
		
		/**
		 * Schedule the executor to shut down after the connection drops, closing any open
		 * port resources
		 */
		try {
			executor.shutdown();
		} catch(Exception ex) {
			System.err.println("There was a problem shutting down.");
			System.exit(1);
		}
	}
	
	/**
	 * Emit a string to the TCP link
	 * @param payload the string to send
	 */
	public void emitString(String payload) {
		try {
			out.writeUTF(payload);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
