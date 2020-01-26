package Client;

import org.omg.CORBA.*;
import org.omg.CosNaming.*;
import java.io.*;
import java.net.*;
import java.nio.file.*;

import Common.*;

/*
 * Class: Client
 * Function: Client handles various aspects of the user experience and drives much of the interaction on the front end before making
 * calls to the server through CORBA on behalf of the user's requests.
 */

public class Client {

	// ServerServant is an instantiation of a server that supports CORBA as per the generated files in Common and from the IDL.
	static ServerServant server;
	// This is to capture user input
	static BufferedReader input;
	// The following variables are used to hold user values like their name, password, address, and port - so that commands can be structured and sent to the server via CORBA
	static String username;
	static String password;
	static String address;
    static int port = 3000;
    // The debug flag is used for testing some outputs, it is false by default - but can be manually set to true to view more printout information
	static boolean debugFlag = false;

	// The ClientHelper class handles the incoming and outgoing requests during the socket connection and downloading phases.
	// The requestListener - when run in a loop will wait for requests
	static ClientHelper requestListener = null;


	/*
	 * Method: main
	 * Function: Starts the client application and handles login / user registration before passing off to the mainDirectory method.
	 * It acts as a gatekeeper before accessing the main program loop.
	 */
	public static void main(String args[]) {
		try {
			// Track the user's input as they will select an option
            input = new BufferedReader(new InputStreamReader(System.in));
            // Create and initialize the orb
            ORB orb = ORB.init(args, null);
            // Get the root naming context
            org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
            NamingContextExt ncRef = NamingContextExtHelper.narrow(objRef);
            // Set up and resolve the naming related to the server
            server = ServerServantHelper.narrow(ncRef.resolve_str("TME2"));

            // Print the first message to the screen that lets the user select whether to login or register
            System.out.println("------------------------------Welcome!------------------------------");
            System.out.println("---------------The client application is now running!---------------");
            System.out.println("---Please enter the option you wish to execute followed by Enter ---");
            System.out.println("\t[1] | Login");
            System.out.println("\t[2] | Register User");
			System.out.println("\t[3] | Delete User");
            System.out.println("--------------------------------------------------------------------");
            System.out.print("Your Entry: ");

            String userEntry = input.readLine();

            if (userEntry.equals("1")) {
                try {
                    System.out.println("-------------------------------Login--------------------------------");
                    // Get the username and password from the user
                    System.out.print("Username: ");
                    username = input.readLine();
                    System.out.print("Password: ");
                    password = input.readLine();
                    System.out.println("--------------------Authorizing - Please Wait...--------------------");

                    // Create a new user object called userProfile. In doing this, the user's plaintext password will be hashed using MD5 and compared against the database
                    User userProfile = new User(username, password);

                    // Some extra options if the debug flag is enabled - mostly just to check that hashing is working correctly
                    if (debugFlag) {
                        System.out.println();
                        System.out.println("Hashed Password: The hashed password for this user is: " + userProfile.getHashedPassword());
                    }

                    // Send a request through CORBA to the database to log the user in using their username and hashed password)
                    String credentialStatus = server.login(username, userProfile.getHashedPassword());
                    // Since the login returns a string (either success, or failure) - print the results out either way
                    if (credentialStatus.equals("Success!")) {
                        System.out.println("Database Authorization Response: " + credentialStatus);
                        System.out.println("--------------------------------------------------------------------");

                        // Since the user exists in the database, get their current address and their port for connecting
                        address = InetAddress.getLocalHost().getHostAddress();
                        // The ports are assigned upon user creation automatically and avoid duplication (in case two clients are connected on the same address at the same time)
                        port = server.getConnectionPort(username);

                        // Now save the address and port via CORBA, just in case they are using a different computer
                        server.setConnectionAddress(username, address);
                        // The user's files are stored in a directory structure based on the location of the source files for this project.
						// The code will automatically create directories for each user under users/[username]/shared and users/[username]/not-shared
                        server.clearSharedFiles(username);
                        server.clearNotSharedFiles(username);
                        // Register the contents of what the user is currently sharing or not sharing to the server through CORBA (based on the location of the files in the filesystem)
                        server.registerFiles(username);

						// Some extra options if the debug flag is enabled - mostly just to check the address and port are pulled correctly
                        if (debugFlag) {
                            System.out.println();
                            System.out.println("Network Info: The connection address and port are: " + address + ":" + port);
                        }

                        // Now give the user access to the main directory
                        mainDirectory();
                    }
                    // Otherwise, the user may have entered an incorrect password or non-existent username. Inform them and then terminate
                    else {
                        System.out.println("Database Authorization Response Response: " + credentialStatus);
                        System.out.println("--------------------------------------------------------------------");
                        System.out.println("The client will now terminate. Goodbye!");
                        System.out.println("--------------------------------------------------------------------");
                    }
                }
                // Otherwise, we've hit some sort of error. Print it and return.
                catch(Exception e){
                        System.out.println("Error! : " + e.getMessage());
                        return;
                    }
            }

            // Otherwise, if the user has entered 2, it means they want to create an account
            else if (userEntry.equals("2")) {
            	// Get the username and password from the console
                System.out.println();
                System.out.println("--------------------------User Registration-------------------------");
                System.out.print("Username: ");
                username = input.readLine();
                System.out.print("Password: ");
                password = input.readLine();
                System.out.println("---------------------Creating - Please Wait...----------------------");
                // Create a new user object with the selected username and password (this will hash the password)
                User userProfile = new User(username, password);
                // Send the request to the server to register the user
                String registrationStatus = userProfile.register(server);
                // If a success message comes back, inform the user
                if (registrationStatus.equals("Success!")) {
                    System.out.println("Database Registration Response: " + registrationStatus);
                    System.out.println("--------------------------------------------------------------------");

                    // Get the current address of the user and the port assigned to them
                    address = InetAddress.getLocalHost().getHostAddress();
                    port = server.getConnectionPort(username);

                    // Update the connection address for the user, just in case they changed networks
                    server.setConnectionAddress(username, address);
					// The user's files are stored in a directory structure based on the location of the source files for this project.
					// The code will automatically create directories for each user under users/[username]/shared and users/[username]/not-shared
                    server.clearSharedFiles(username);
                    server.clearNotSharedFiles(username);
					// Register the contents of what the user is currently sharing or not sharing to the server through CORBA (based on the location of the files in the filesystem)
                    server.registerFiles(username);

					// Some extra options if the debug flag is enabled - mostly just to check the address and port are pulled correctly
                    if (debugFlag) {
                        System.out.println();
                        System.out.println("Network Info: The connection address and port are: " + address + ":" + port);
                    }

					// Now give the user access to the main directory
                    mainDirectory();
                }
				// Otherwise, the user may have entered a username that already exists. Inform them and terminate
                else {
                    System.out.println("Database Registration Response: " + registrationStatus);
                    System.out.println("--------------------------------------------------------------------");
                    System.out.println("The client will now terminate. Goodbye!");
                    System.out.println("--------------------------------------------------------------------");
                }
            }

			else if (userEntry.equals("3")) {
				// Get the username and password from the console
				System.out.println();
				System.out.println("----------------------------User Deletion---------------------------");
				System.out.print("Username: ");
				username = input.readLine();
				System.out.print("Password: ");
				password = input.readLine();
				System.out.println("---------------------Deleting - Please Wait...----------------------");
				// Create a new user object with the selected username and password (this will hash the password)
				User userProfile = new User(username, password);
				// Send the request to the server to delete the user
				String deletionStatus = userProfile.delete(server);
				// If a success message comes back, inform the user
				if (deletionStatus.equals("Success!")) {
					System.out.println("Database Deletion Response: " + deletionStatus);
					System.out.println("--------------------------------------------------------------------");

					try {
						// Now delete the user's files and directories
						File mainPath = new File("users/" + username);
						File sharedPath = new File("users/" + username + "/shared");
						File notSharedPath = new File("users/" + username + "/not-shared");

						// Get all the user's shared files and delete them
						File[] sharedFiles = sharedPath.listFiles();

						for (File file : sharedFiles) {
							file.delete();
						}

						// Delete the shared folder
						sharedPath.delete();

						// Get all the user's non-shared files and delete them
						File[] notSharedFiles = notSharedPath.listFiles();

						for (File file : notSharedFiles) {
							file.delete();
						}

						// Delete the non-shared folder
						notSharedPath.delete();

						// Delete the user's path
						mainPath.delete();
					} catch (Exception e) {
						System.out.println("The client will now terminate. Goodbye!");
						System.out.println("--------------------------------------------------------------------");
					}
					System.out.println("The client will now terminate. Goodbye!");
					System.out.println("--------------------------------------------------------------------");
				}
				// Otherwise, the user may have entered a username that does not exist, or an incorrect password. Inform them and terminate
				else {
					System.out.println("Database Deletion Response: " + deletionStatus);
					System.out.println("--------------------------------------------------------------------");
					System.out.println("The client will now terminate. Goodbye!");
					System.out.println("--------------------------------------------------------------------");
				}
			}
            // If all else fails, the user did not enter a valid response. Inform them and terminate.
            else {
				System.out.println("Invalid! Enter the key associated with the option and press Enter.");
				System.out.println("The client will now terminate. Goodbye!");
				System.out.println("--------------------------------------------------------------------");
			}
        }
			// Otherwise, we've hit some sort of error. Print it and the function will terminate.
            catch (Exception e) {
            System.out.println("Error : " + e.getMessage()) ;
        }
	}

	/*
	 * Method: mainDirectory
	 * Function: The main menu of the application once logged in.
	 * Handles all user-facing aspects of the system and makes requests through the server
	 */
	static void mainDirectory() throws Exception {
		// Since we're active while this is running, open a ClientHelper on the port to listen for and respond to requests
		requestListener = new ClientHelper(port);
		requestListener.run();
		// If alive true, we still want to run the program
		boolean alive = true;
		while(alive) {
			System.out.println();
			System.out.println("-----------------------------Main Menu:-----------------------------");
			System.out.println("---Please enter the option you wish to execute followed by Enter ---");
			System.out.println("File Viewing Options:");
			System.out.println("\t[1] | View All Of My Files (Shared And Not Shared)");
			System.out.println("\t[2] | View All Shared Files (Shared By You And Other Users)");
			System.out.println("File Sharing Options:");
			System.out.println("\t[3] | Share A File");
			System.out.println("\t[4] | Stop Sharing A File");
			System.out.println("File Discovery Options:");
			System.out.println("\t[5] | Search For A File");
			System.out.println("System Options:");
            System.out.println("\t[r] | Refresh File System");
			System.out.println("\t[x] | Log Out");
			System.out.println("--------------------------------------------------------------------");
			System.out.print("Your Entry: ");

			// Get the user's choice
			String userEntry = input.readLine();

			// Call the View All Files function which will report the contents of the user's shared and not-shared directories
			if(userEntry.equals("1")) {
				System.out.println();
				System.out.println("------------------------------My Files------------------------------");
				viewMyFiles();
				System.out.println("--------------------------------------------------------------------");
				System.out.println("---------------------Returning To The Main Menu---------------------");
			}

			// Call the View Shared Files function which will report all files shared by any user currently on the system
			else if(userEntry.equals("2")) {
                System.out.println();
                System.out.println("--------------------------All Shared Files--------------------------");
				viewSharedFiles();
                System.out.println("--------------------------------------------------------------------");
                System.out.println("---------------------Returning To The Main Menu---------------------");
			}

			// Call the File Sharing function which will allow the user to add previously non-shared files to a shared status
			else if(userEntry.equals("3")) {
				System.out.println();
				System.out.println("------------------------File Sharing Utility------------------------");
				addFileToShared();
				System.out.println("--------------------------------------------------------------------");
				System.out.println("---------------------Returning To The Main Menu---------------------");
			}

			// Call the File Un-Sharing function which will allow the user to add previously shared files to a non-shared status
			else if(userEntry.equals("4")) {
				System.out.println();
				System.out.println("-----------------------File Un-Sharing Utility----------------------");
				removeFileFromShared();
				System.out.println("--------------------------------------------------------------------");
				System.out.println("---------------------Returning To The Main Menu---------------------");
			}

			// Call the File Search function which will allow the user to search for shared files, and if they'd like - download them
			else if(userEntry.equals("5")) {
				System.out.println("-------------------------File Search Utility------------------------");
				searchForFile();
				System.out.println("--------------------------------------------------------------------");
				System.out.println("---------------------Returning To The Main Menu---------------------");
			}

			// If the user enters x, this means they want to log out - so log out, and close the requestListener
			else if(userEntry.equals("x") || userEntry.equals("X"))  {
				// Set alive to false in order to kill the main loop
				alive = false;
				System.out.println("-----------------------------Logging Out----------------------------");
				logout();
				System.out.println("--------------------------------------------------------------------");
				System.out.println("------------------------------Goodbye!------------------------------");
				requestListener.terminate();
			}

			// If the user enters r, refresh the main file system
            else if(userEntry.equals("r") || userEntry.equals("R"))  {
                System.out.println("-----------------------Refreshing File System-----------------------");
                refreshFileSystem();
                System.out.println("--------------------------------------------------------------------");
                System.out.println("---------------------Returning To The Main Menu---------------------");
            }

            // Otherwise, the user has entered an invalid entry - inform them and loop again
			else {
			    System.out.println("Invalid! Enter the key associated with the option and press Enter.");
                System.out.println("--------------------------------------------------------------------");
                System.out.println("---------------------Returning To The Main Menu---------------------");
            }
		}
	}

	/*
	 * Method: viewMyFiles
	 * Function: Returns a list of all of the user's files - both shared and not shared
	 * The design of this program is such that each user will have 2 folders, shared and not-shared
	 * If a file exists in the shared folder, it will be registered on the server and sharable to others
	 * If a file exists in the not-shared folder, it will be registered on the server as not-sharable until the user changes it
	 */
	static void viewMyFiles() {

		// In the project directory root there is a folder called users - this houses all of the files and structure used for the user's filesystem
		File sharedPath = new File("users/" + username +"/shared");
		File notSharedPath = new File("users/" + username +"/not-shared");

		// The file array will store a list of all files that are in each directory
		File[] sharedFiles = sharedPath.listFiles();
		File[] notSharedFiles = notSharedPath.listFiles();

		// If there length of the sharedFiles array is 0, there are no shared files. Otherwise, loop through the array and print out the file names.
		System.out.println("Shared Files: ");
		if (sharedFiles.length == 0) {
			System.out.println("\t --- " + username + " has no shared files. ---");
		}
		for(File file : sharedFiles) {
			System.out.println("\t + " + file.getName());

		}

		System.out.println();

		// If there length of the notSharedFiles array is 0, there are no not-shared files. Otherwise, loop through the array and print out the file names.
		System.out.println("Non-Shared Files: ");
		if (notSharedFiles.length == 0) {
			System.out.println("\t --- " + username + " has no non-shared files. ---");
		}
		for(File file : notSharedFiles) {
			System.out.println("\t + " + file.getName());
		}

		System.out.println();

		// This has been added in order to allow the user to review the results before going back to the main menu
		// Upon pressing Enter, the main menu will come back up again and ready for a new menu option to be selected
		try {
            System.out.println("Press any key to return");
            input.readLine();
        }
		// Otherwise, we've hit some sort of error. Print it and the function will terminate.
		catch (Exception e) {
		    System.out.println("Error! " + e.getMessage());
        }
	}

	/*
	 * Method: viewSharedFiles
	 * Function: Returns a list of all files being shared
	 * The design of this program is such that files are only listed as shared when the user is logged in
	 * If a user logs out, their previously shared files will be removed from availability which will reflect on this list
	 * If a file is shared, the filename will be shown - but not the user sharing it.
	 * Duplicates of files being shared by different users has not been disallowed
	 */
    static void viewSharedFiles() {
    	// Make a call to the database via the CORBA server and return all the shared files in a string array
        String [] sharedFiles = server.viewAllSharedFiles();

        System.out.println("Files currently being shared are: ");
        // If the length of the array is 0, nothing is being shared
        if (sharedFiles.length == 0) {
            System.out.println("\t --- There are no files currently being shared. ---");
			System.out.println();
			// This has been added in order to allow the user to review the results before going back to the main menu
			// Upon pressing Enter, the main menu will come back up again and ready for a new menu option to be selected
			try {
				System.out.println("Press any key to return");
				input.readLine();
			} catch (Exception e) {
				System.out.println("Error! " + e.getMessage());
			}
			return;
        }
        // However, if there are shared files - print them to the screen for the user's reference
        for(int i = 0; i < sharedFiles.length; i++) {
            System.out.println("\t+ " + sharedFiles[i]);
        }
        System.out.println();
        // This has been added in order to allow the user to review the results before going back to the main menu
		// Upon pressing Enter, the main menu will come back up again and ready for a new menu option to be selected
        try {
            System.out.println("Press any key to return");
            input.readLine();
        } catch (Exception e) {
            System.out.println("Error! " + e.getMessage());
        }
    }

	/*
	 * Method: addFileToShared
	 * Function: Allows the user to share a file that was previously unshared
	 * The design of this program is such that when a user decides to share a file, it is registered in the
	 * sharedfiles table in the database via CORBA and unregistered in the notsharedfiles table in the database via CORBA.
	 * Next, the user's filesystem will update to move the file physically from the not-shared to the shared directory.
	 */
	static void addFileToShared() {
		try {
			int fileMatchNumber = 0;

			// Get the current path of the user's not-shared files
			File notSharedPath = new File("users/" + username +"/not-shared");
			// Get the contents of the directory and store it in an array
			File[] notSharedFiles = notSharedPath.listFiles();

			System.out.println("Non-Shared Files: ");
			// If the user has no non-shared files, let them know.
			if (notSharedFiles.length == 0) {
				System.out.println("\t --- " + username + " has no non-shared files. ---");
                System.out.println();
				// This has been added in order to allow the user to review the results before going back to the main menu
				// Upon pressing Enter, the main menu will come back up again and ready for a new menu option to be selected
                try {
                    System.out.println("Press any key to return");
                    input.readLine();
                } catch (Exception e) {
                    System.out.println("Error! " + e.getMessage());
                }
                return;
			}

			// If the directory has files in it, print them on the screen to help the user with selection
			for(File file : notSharedFiles) {
				System.out.println("\t + " + file.getName());
			}
			// Let the user enter which file they would like to share
            System.out.println();
			System.out.println("Enter the name of the file you wish to share followed by Enter");
			System.out.print("Your Entry: ");

			String fileToShare = input.readLine();

			// If the filename the user has entered is in the directory, then set the fileMatchNumber to be 1
			for (int i = 0; i < notSharedFiles.length; i++) {
				if (fileToShare.equals(notSharedFiles[i].getName())) {
					fileMatchNumber = 1;
				}
			}
            System.out.println();
			// If the fileMatchNumber is equal to 1, then pass the request to the database via the CORBA server to register the file as available for sharing
			if (fileMatchNumber == 1) {
				System.out.println(server.startFileShare(username, fileToShare));

				// Now, copy the file to the shared directory and remove it from the user's not-shared directory
				File nonSharedFileToMove = new File("users/" + username +"/not-shared/" + fileToShare);
				if(nonSharedFileToMove.renameTo(new File("users/" + username +"/shared/" + fileToShare))) {
					nonSharedFileToMove.delete();
				}
			}
			// Otherwise, the user entered an invalid filename
			else {
				System.out.println("--------------------------------------------------------------------");
				System.out.println("You have entered an invalid file name.");
			}
		}
		// Catch any errors and terminate the function
		catch (Exception e) {
			System.out.println("Error! : " + e.getMessage());
			return;
		}
        System.out.println();
		// This has been added in order to allow the user to review the results before going back to the main menu
		// Upon pressing Enter, the main menu will come back up again and ready for a new menu option to be selected
        try {
            System.out.println("Press any key to return");
            input.readLine();
        } catch (Exception e) {
            System.out.println("Error! " + e.getMessage());
        }
	}

	/*
	 * Method: removeFileFromShared
	 * Function: Allows the user to remove (or unregister) a file as being shared
	 * The design of this program is such that when a user decides to un-share a file, it is registered in the
	 * notsharedfiles table in the database via CORBA and unregistered in the sharedfiles table in the database via CORBA.
	 * Next, the user's filesystem will update to move the file physically from the shared to the not-shared directory.
	 * This means that users will no longer be able to download the file from this peer
	 */
	static void removeFileFromShared() {
		try {
			int fileMatchNumber = 0;

			// Get the current path of the user's shared files
			File sharedPath = new File("users/" + username +"/shared");

			// Get the contents of the directory and store it in an array
			File[] sharedFiles = sharedPath.listFiles();

			System.out.println("Shared Files: ");
			// If the user has no shared files, let them know.
			if (sharedFiles.length == 0) {
				System.out.println("\t --- " + username + " has shared files. ---");
                System.out.println();
				// This has been added in order to allow the user to review the results before going back to the main menu
				// Upon pressing Enter, the main menu will come back up again and ready for a new menu option to be selected
                try {
                    System.out.println("Press any key to return");
                    input.readLine();
                } catch (Exception e) {
                    System.out.println("Error! " + e.getMessage());
                }
                return;
			}
			// If the directory has files in it, print them on the screen to help the user with selection
			for(File file : sharedFiles) {
				System.out.println("\t + " + file.getName());
			}

			// Let the user enter which file they would like to un-share
			System.out.println("Enter the name of the file you wish to un-share followed by Enter");
			System.out.print("Your Entry: ");

			String fileToRemoveFromShare = input.readLine();

			// If the filename the user has entered is in the directory, then set the fileMatchNumber to be 1
			for (int i = 0; i < sharedFiles.length; i++) {
				if (fileToRemoveFromShare.equals(sharedFiles[i].getName())) {
					fileMatchNumber = 1;
				}
			}
			// If the fileMatchNumber is equal to 1, then pass the request to the database via the CORBA server to de-register the file as available for sharing
			if (fileMatchNumber == 1) {
				System.out.println(server.stopFileShare(username, fileToRemoveFromShare));

				// Now, copy the file to the not-shared directory and remove it from the user's shared directory
				File sharedFileToMove = new File("users/" + username +"/shared/" + fileToRemoveFromShare);
				if(sharedFileToMove.renameTo(new File("users/" + username +"/not-shared/" + fileToRemoveFromShare))) {
					sharedFileToMove.delete();
				}
			}
			// Otherwise, the user entered an invalid filename
			else {
				System.out.println("--------------------------------------------------------------------");
				System.out.println("You have entered an invalid file name.");
			}
		}
		// Catch any errors and terminate the function
		catch (Exception e) {
			System.out.println("Error! : " + e.getMessage());
			return;
		}
        System.out.println();
		// This has been added in order to allow the user to review the results before going back to the main menu
		// Upon pressing Enter, the main menu will come back up again and ready for a new menu option to be selected
        try {
            System.out.println("Press any key to return");
            input.readLine();
        } catch (Exception e) {
            System.out.println("Error! " + e.getMessage());
        }
	}

	/*
	 * Method: searchForFile
	 * Function: Allows the user to search for a shared file by name.
	 * If a file is available the user will be given the option to download the file via a socket connection
	 * During this time the other user with the file is kept from the user wishing to download the file (anonymous)
	 * This method relies on a download helper to create the connection and download the file
	 */
	static void searchForFile() {
		try {
			// Get the path for the user's current shared and not-shared directories
			File sharedPath = new File("users/" + username + "/shared");
			File notSharedPath = new File("users/" + username + "/not-shared");

			// Allow the user to enter the name of the file they want to search for
			System.out.println("Enter the name of the file you wish to search for followed by Enter");
			System.out.print("Your Entry: ");

			String fileToSearchFor = input.readLine();

			// Create an array of filenames for the shared and not-shared files
			File[] sharedFiles = sharedPath.listFiles();
			File[] notSharedFiles = notSharedPath.listFiles();

			// Check through the shared files array to see if the user already owns the file and is sharing it
			for (File file : sharedFiles) {
				if (fileToSearchFor.equals(file.getName())) {
					// If it exists, tell the user and prompt to exit
					System.out.println();
					System.out.println("You already own the file: " + fileToSearchFor);
					System.out.println("It is in your shared files.");
					System.out.println();
					// This has been added in order to allow the user to review the results before going back to the main menu
					// Upon pressing Enter, the main menu will come back up again and ready for a new menu option to be selected
					try {
						System.out.println("Press any key to return");
						input.readLine();
					} catch (Exception e) {
						System.out.println("Error! " + e.getMessage());
					}
					return;
				}
			}

			// Check through the not-shared files array to see if the user already owns the file and is not sharing it
			for (File file : notSharedFiles) {
				if (fileToSearchFor.equals(file.getName())) {
					// If it exists, tell the user and prompt to exit
					System.out.println();
					System.out.println("You already own the file: " + fileToSearchFor);
					System.out.println("It is in your non-shared files.");
					System.out.println();
					// This has been added in order to allow the user to review the results before going back to the main menu
					// Upon pressing Enter, the main menu will come back up again and ready for a new menu option to be selected
					try {
						System.out.println("Press any key to return");
						input.readLine();
					} catch (Exception e) {
						System.out.println("Error! " + e.getMessage());
					}
					return;
				}
			}
			// Otherwise, this means the user does not have the file
			// In this case, send a request to the server via CORBA to see who owns the file
			String[] usersWithFile = server.findFile(fileToSearchFor);
			// If no one owns a file by this name that is available for sharing, let the user know
			if(usersWithFile.length == 0){
				System.out.println("No match was found for: '"+ fileToSearchFor + "'");
				System.out.println("It does not exist, or is not currently being shared.");
			}
			// Otherwise a match was found
			// Give the user an option to download the file and share it, download the file and keep it in their not-shared directory, or return to the main menu
			// Keep the owner of the file's information hidden from the user
			else {
				System.out.println("A match for : "+ fileToSearchFor + " was found.");
				System.out.println("Would you like to download the file?");
				System.out.println("\t[1] | Download And Share The File");
				System.out.println("\t[2] | Download And Do Not Share The File");
				System.out.println("\t[3] | Return To The Main Menu");
				System.out.println("--------------------------------------------------------------------");
				System.out.print("Your Entry: ");
				String userEntry = input.readLine();

				// If the user enters 1, start the download file method with the flag of 1
				if(userEntry.equals("1")){
					downloadFile(usersWithFile, fileToSearchFor, 1);
					return;
				}

				// If the user enters 2, start the download file method with the flag of 2
				else if (userEntry.equals("2")) {
					downloadFile(usersWithFile, fileToSearchFor, 2);
					return;
				}
				// If the user enters 3, bring them back to the main menu
				else if (userEntry.equals("3")){
					return;
				}
				// Otherwise, they entered something invalid, return to the main menu
				else {
					System.out.println("Invalid entry! Returning to the main menu.");
					return;
				}
			}
		} catch (Exception e) {
			System.out.println("Error! : " + e.getMessage());
			return;
		}
        System.out.println();
		// This has been added in order to allow the user to review the results before going back to the main menu
		// Upon pressing Enter, the main menu will come back up again and ready for a new menu option to be selected
        try {
            System.out.println("Press any key to return");
            input.readLine();
        } catch (Exception e) {
            System.out.println("Error! " + e.getMessage());
        }
	}

	/*
	 * Method: refreshFileSystem
	 * Function: Allows the server to register and update files based on the user adding them to the directory.
	 * When a new user is created, the directories shared and not-shared will be empty.
	 * So, you can drag and drop files into the shared and not-shared directories and run this command to force an update to the server via CORBA
	 */
    static void refreshFileSystem() {
    	// Clear all existing registrations on the server via CORBA for this username
        server.clearSharedFiles(username);
        server.clearNotSharedFiles(username);
        // Register the files on the server through CORBA that have been added to the directories manually
        server.registerFiles(username);
        System.out.println();
        return;
    }

	/*
	 * Method: logout
	 * Function: De-registers all files from the server via CORBA so that they are no longer listed as shared / not-shared
	 * This is so that when a user (peer) goes offline, their files are no longer listed as shared
	 */
	static void logout() {
		server.clearSharedFiles(username);
		server.clearNotSharedFiles(username);
	}

	/*
	 * Method: downloadFile
	 * Function: Sets up the protocols and commands for sending a file over the socket. A special string format
	 * in the addresses array is used to determine the location of the file for download. Since the filesystem is structured
	 * /users/[username]/shared , the username will need to be passed as well - but is NOT visible to the user
	 * Parameters: addresses ~ an array of strings of format http://[user-address]:[user-port]}[username]
	 * 				filename ~ a string containing the name of the file to download
	 * 				int downloadType - if 1, download the file to the user's shared directory, if 2 - not-shared
	 */
	static void downloadFile(String [] addresses, String filename, int downloadType) {
		try {
			HttpURLConnection con = null;
			// For each address in addresses, deconstruct the string and try to connect
			for(String address: addresses) {
				// Uses the character } to separate the URL from the username - split it
				String[] stringDeconstruction = address.split("}");
				// The address part is before the } and the username is after
				String addressPart = stringDeconstruction[0];
				String usernamePart = stringDeconstruction[1];
				// Now, try to make a URL object that is a combination of the address, the filename, and username
				try {
					// Create the URL object and set the con to the casted HttpURLConnection
					URL url = new URL(addressPart + "/" + filename + "}" + usernamePart);
					con = (HttpURLConnection)url.openConnection();
					break;
				}
				// If unable to connect, keep looping
				catch(ConnectException e) {
					con = null;
				}
			}
			// If unable to make any connections, return to the calling function after informing the user
			if(con == null) {
				System.out.println("Unable to connect to a user peer to download.");
				System.out.println("The user may have gone offline or stopped sharing the file.");
				return;
			}

			// Create a new byteStream in order to transfer files of all types
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
			// Set a buffer based on a reasonable size
			byte[] buffer = new byte[4096];

			// Set up an InputStream for the connection to prepare for the file's contents
			InputStream incoming = con.getInputStream();

			// Write the bytes of the file through the byteStream
			while(true) {
				int i = incoming.read(buffer);
				if( i < 0 ) {
					break;
				}
				byteStream.write(buffer,0,i);
			}
			incoming.close();

			// If the download type was 1, assemble the byteStream.toByteArray which should create the original file
			// Then save it to the user's directory under "shared" and inform the user of the status
			if(downloadType == 1) {
				Path sharedPath = Paths.get("users/"+username+"/shared/" + filename);
				Files.write(sharedPath, byteStream.toByteArray());
				server.clearSharedFiles(username);
        server.clearNotSharedFiles(username);
				server.registerFiles(username);
				System.out.println("The file has been downloaded and is now also being shared!");
			}

			// If the download type was 2, assemble the byteStream.toByteArray which should create the original file
			// Then save it to the user's directory under "not-shared" and inform the user of the status
			else{
				Path notSharedPath = Paths.get("users/"+username+"/not-shared/" + filename);
				Files.write(notSharedPath, byteStream.toByteArray());
				server.clearNotSharedFiles(username);
        server.clearSharedFiles(username);
				server.registerFiles(username);
				System.out.println("The file has been downloaded and is not being shared!");
			}
		}
		// Otherwise, we hit some sort of error. Print to the user and the function will terminate
		catch (Exception e) {
			System.out.println("Error! : " + e.getMessage());
		}
	}
}
