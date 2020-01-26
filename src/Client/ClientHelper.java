package Client;

import java.net.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.locks.*;
import java.io.*;
import java.nio.file.*;

/*
 * Class: ClientHelper
 * Function: Used to handle file requests and functions related to socket connections.
 */
public class ClientHelper {
	// The port used for connections
	int port;
	// Determines whether or not the incoming and outgoing threads should terminate
	boolean alive = true;
	
	// This socket being the server socket is to handle incoming requests
	ServerSocket serverSocket;
	
	// Provides a queue structure for sockets. Also create a lock and condition to monitor in order to lock and unlock accordingly
	Queue<Socket> pendingRequests = new ArrayDeque<Socket>();
	Lock lock = new ReentrantLock();
	Condition isWaiting = lock.newCondition();
	
	// Create a new dispatcher that listens on the given port.
	public ClientHelper(int port) {
		this.port = port;
	}

	/*
	 * Method: run
	 * Function: Allows the incoming and outgoing threads to run to listen and handle requests for files
	 */
	public void run() {
		try {
			// Start the outgoing thread which sends out files to the requestor
			Thread outgoingThread = new Thread() {
				public void run() {
					try {
						while(alive) {
							Socket socket = null;
							// Lock the lock
							lock.lock();
							// Suspend execution of the thread until something comes in
							if(pendingRequests.size() == 0) {
								try {
									isWaiting.await();
								}
								catch (Exception e) {
									System.out.println("Error: " + e.getMessage());
								}
							}
							// If alive is set to false (upon a close command), end this.
							if(!alive) {
								return;
							}
							// Get the socket from the pending requests queue.
							socket = pendingRequests.poll();
							// Unlock the lock
							lock.unlock();
							
							// Read the input from the socket and store it in a BufferedReader
							BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
							
							// Keep track of variables in order to use them effectively
							// File and user will store the filename and the user combined
							String fileAndUser = "";
							// Filename stores the filename that has been requested
							String filename = "";
							// User contains the name of the user who has the file (for finding the directory)
							String user = "";
							// Used for reading the input
							String line = "";

							while ((line = reader.readLine()) != null) {
								// If the line length is 0, stop reading
								if(line.length() == 0) {
									break;
								}
								// Based on the set up, the request will come in the form (GET special_string HTTP/1.1
								// The special string needs to be deconsructed to get the required information
								if(line.startsWith("GET")) {
									// Split between GET and the rest of the string at the space
									String [] split = line.split(" ");
									// The file and the user is going to be the second part of this string
									fileAndUser = split[1];
									// Now split it again as the filename and user are separated by }
									String [] secondarySplit = fileAndUser.split("}");
									filename = secondarySplit[0];
									user = secondarySplit[1];
								}
							}
							
							// Now, we can transfer the file to where it is needed
							BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
							// Find where the file should be stored based on the defined filesystem. That is /users/[username]/shared/[filename]
							Path file = Paths.get("users\\" + user +"\\shared\\" + filename);

							// As long as there is a filename, and the file exists - send back a 200 response code
							if(!filename.equals("") && Files.exists(file)) {
								writer.write("HTTP/1.1 200 Continue\r\n");
								writer.write("\r\n");
								writer.flush();
								// Now write the data to the socket since the file exists and the request is valid
								socket.getOutputStream().write(Files.readAllBytes(file));
								}
							// Otherwise, there is an error, and since it can be any number of errors - return 520 which indicates unknown error
							else {
									writer.write("HTTP/1.1 520 Unknown Error\r\n");
									writer.write("\r\n");
									writer.flush();
								}
							// Close the socket
							socket.close();
						}
					}
					// Otherwise, some unspecified error occurred
					catch(Exception e) {
						System.out.println("Error: " + e.getMessage());
					}
				}
			}; outgoingThread.start();
			
			// Listen for incoming connections
			Thread incomingThread = new Thread()
			{
			    public void run() 
			    {
			    	try {
			    		// Get the port currently in use and use it to listen for requests from a client
			    		serverSocket = new ServerSocket(port);
						while(alive) {
							// Once a socket has been heard and accepted, lock the thread - add a socket to the pendingRequests queue
							Socket socket = serverSocket.accept();
							lock.lock();
							pendingRequests.add(socket);
							// Signal the condition and the outgoing thread which will be awaiting for this status change
							isWaiting.signalAll();
							// Unlock the lock
							lock.unlock();
						}
						// Close the socket if there is an exit command that sets alive to false
						serverSocket.close();
			    	}
			    	// Otherwise, if there is an error - stop everything and return
			    	catch(Exception e) {
			    		if(!alive) {
			    			return;
						}
						System.out.println("Error: " + e.getMessage());
			    	}
				}

			};incomingThread.start();
		}
		// Print the result of any other errors that may have occurred
		catch(Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	/*
	 * Method: terminate
	 * Function: Closes the sockets and ends the main loop of the program along with resetting the locks
	 */
	public void terminate() {
		alive = false;
		try {
			serverSocket.close();
			lock.lock();
			isWaiting.signalAll();
			lock.unlock();
		}
		// Print the result of any other errors that may have ocurred
		catch(Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
}
