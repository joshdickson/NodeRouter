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
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A class that illustrates the use of a non blocking TCP linker to a front end web service 
 * through a Node JS-based proxy server
 * @author Joshua Dickson <josh.dickson@wpi.edu>
 */
public class Router implements Closeable {
	
	private final ExecutorService executor;
	private BufferedReader in;
	private BufferedWriter out;
	private final BlockingQueue<String> outputQueue;
	private Process nodeProcess;
	private final ProcessBuilder builder;
	private boolean shutdownFlag;
	
	/**
	 * Construct a Router
	 */
	public Router(String server) {
		shutdownFlag = false;
		executor = Executors.newSingleThreadExecutor();
		builder = new ProcessBuilder("/usr/local/bin/node", "nodeproxy/" + server);
		outputQueue = new ArrayBlockingQueue<String>(1000);
		
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
		shutdownFlag = true;
		nodeProcess.destroy();
		executor.shutdown();
	}
	
	/**
	 * Start the node process and bind to its standard input and output
	 * @throws IOException 
	 */
	public void bind() throws IOException {
		
		nodeProcess = builder.start();
		
		in = new BufferedReader(new InputStreamReader(nodeProcess.getInputStream()));
		out = new BufferedWriter(new OutputStreamWriter(nodeProcess.getOutputStream()));
		
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
				while(!shutdownFlag) {
					try {
						String line = in.readLine();
												
						if(line != null)
							outputQueue.offer(line);	
						
					} catch (IOException e) {
						e.printStackTrace();
					} 
				}
				
			}
		});
	}
	
	/**
	 * Get the queue that inbound strings are placed into
	 * @return
	 */
	public BlockingQueue<String> getQueue() {
		return outputQueue;
	}
	
	/**
	 * Send a string to the router
	 * @param payload the string to send
	 */
	public void send(String payload) {
		try {
			out.write(payload);
			if(!payload.endsWith("\n")) out.write("\n");
			out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
