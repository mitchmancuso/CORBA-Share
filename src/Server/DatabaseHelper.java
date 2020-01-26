package Server;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;

/*
 * Class: DatabaseHelper
 * Where all functions from ServerHelper are fully programmed to perform various back-end tasks
 */
public class DatabaseHelper {
	// These 3 variables can be changed to reflect the location of a database along with the username and password
	private final static String JDBC_URL = "jdbc:mysql://localhost:3306/";
    private final static String JDBC_NAME = "main_database";
	// This flag is included in order to hide the non-ssl error
	private final static String JDBC_FLAGS = "?autoReconnect=true&useSSL=false";
	private final static String JDBC_USER = "user";
	private final static String JDBC_PW = "password";

	/*
	 * Method: getConnection
	 * Function: Connects to the database and ensures everything goes smoothly. If so, return a connection
	 */
	public Connection getConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			// Use the defined URL, User, and Password from above to connect. The flags are to auto-reconnect and suppress the warning message from not using SSL
			Connection dbConnection = DriverManager.getConnection((JDBC_URL + JDBC_NAME + JDBC_FLAGS), JDBC_USER, JDBC_PW);
			return dbConnection;
		}
		// If there is an error, print it back to the user - return null
		catch(Exception e) {
			System.out.println("Error! : " + e.getMessage());
		}
		return null;
	}

	/*
	 * Method: login
	 * Function: logs in given the credentials from the user input
	 * Parameters: String username - the username entered by the user | String hashedPassword - the hashedPassword determined by the User object
	 */
	public String login(String username, String hashedPassword) {
		try {
			// Get a connection to the database
			Connection con = getConnection();
			// Create a statement in order to execute queries
			Statement statement = con.createStatement();
			// This query is to chck the username and password against the database to see if it matches any existing user
			String userCorrectCredentialsSearch = "SELECT username FROM user WHERE username = '" + username + "' AND password = '" + hashedPassword + "'" ;
			// Put the results in a result set
			ResultSet userAuthorization = statement.executeQuery(userCorrectCredentialsSearch);
			// If there is a result in the result set - the username and password must be correct!
			if(userAuthorization.next()) {
				con.close();
				return "Success!";
			}
			// Otherwise, check to see if the username even exists
			else {
				String userExistsSearch = "SELECT * FROM user WHERE username = '" + username + "'" ;
				ResultSet usernameMatch = statement.executeQuery(userExistsSearch);
				// If the result set returns something, that means the password is incorrect
				if(usernameMatch.next()) {
					con.close();
					return "Incorrect Password!";
				}
				// Otherwise, this means the user doesn't even exist
				else {
					con.close();
					return "User does not exist!";
				}
			}
		}
		// If there is an error, print it back to the user
		catch(Exception e) {
			System.out.println("Error! : " + e.getMessage());
		}
		return "Your login could not be completed.";
	}

	/*
	 * Method: registerUser
	 * Function: registers a user and adds their information to the database so it can be done via the command line
	 * Parameters: String username - the username entered by the user | String hashedPassword - the hashedPassword determined by the User object
	 */
    public String registerUser(String username, String hashedPassword){
    	// Keeps a variable to assign a port
	    int portToAssign;
        try {
        	// Get a connection to the database and create a statement
            Connection con = getConnection();
            Statement searchStatement = con.createStatement();
            // Check to see if the user already exists
            String userExistsCheck = "SELECT * FROM user WHERE username = '" + username + "'" ;
            ResultSet userExists = searchStatement.executeQuery(userExistsCheck);

            // If so, let the user know and return
            if(userExists.next()) {
                con.close();
                return "This user already exists!";
            }
            // Otherwise, look through the database to see the currently used ports
            else {
                Statement assignPortStatement = con.createStatement();
                String findAvailablePort = "SELECT MAX(port) FROM user";
                ResultSet portResult = assignPortStatement.executeQuery(findAvailablePort);
                // Find a port that is not in use, and set that to be the portToAssign
                if(portResult.next()) {
                    String portToAssignString = portResult.getString(1);
                    portToAssign = Integer.parseInt(portToAssignString);
                    portToAssign = portToAssign + 1;
                }
                // Otherwise if there are no users, assign the port 3001
                else {
                    portToAssign = 3001;
                }
                // Now, since we have a username, hashed password, and port - we can create user
                Statement insertStatement = con.createStatement();
                String addUser = "INSERT INTO user (username, password, port) VALUES ('" + username + "'" + ",'" + hashedPassword + "','" + portToAssign + "')";
                // Enter a user into the database and return a success message
                insertStatement.executeUpdate(addUser);
                con.close();
                return "Success!";
            }
        }
		// If there is an error, print it back to the user
        catch (Exception e) {
            return "Error! : " + e.getMessage();
        }
    }

	/*
	 * Method: deleteUser
	 * Function: deletes a user and removes their information from the database so it can be done via the command line
	 * Parameters: String username - the username entered by the user | String hashedPassword - the hashedPassword determined by the User object
	 * These are requested to authenticate the user before they can be deleted.
	 */
	public String deleteUser(String username, String hashedPassword){
		try {
			// Get a connection to the database and create a statement
			Connection con = getConnection();
			Statement searchStatement = con.createStatement();
			// Check to see if the user even exists
			String userIsPresent = "SELECT * FROM user WHERE username = '" + username + "'" ;
			ResultSet userExists = searchStatement.executeQuery(userIsPresent);

			// If the user does not exist, tell the user
			if(!userExists.next()) {
				con.close();
				return "The user does not exist! The user: " + username + " could not be found.";
			}
			// Otherwise, the user exists
			else {
				// Check the password to make sure whoever is requesting the delete has authorization to do so
				Statement authStatement = con.createStatement();
				String userIsAuthorized = "SELECT * FROM user WHERE username = '" + username + "' AND password = '" + hashedPassword + "'" ;
				ResultSet userCredentials = authStatement.executeQuery(userIsAuthorized);

				// If the credentials are incorrect, inform the user and return
				if(!userCredentials.next()) {
					con.close();
					return "You entered an incorrect password! The user: " + username + " was not deleted.";
				}
				else {
					// Otherwise, the user exists, is authenticated and they can delete their own account

					// First, delete all shared files they have registered
					Statement deleteShared = con.createStatement();
					String sharedFilesOwnedByUser = "DELETE FROM sharedfiles WHERE username = '" + username + "'";
					deleteShared.executeUpdate(sharedFilesOwnedByUser);

					// Next, delete all not-shared files that are kept track of
					Statement deleteNotShared = con.createStatement();
					String notSharedFilesOwnedByUser = "DELETE FROM notsharedfiles WHERE username = '" + username + "'";
					deleteNotShared.executeUpdate(notSharedFilesOwnedByUser);

					// Finally, delete the user entirely from the users database
					Statement deleteUser = con.createStatement();
					String userToDelete = "DELETE FROM user WHERE username = '" + username + "'";
					deleteUser.executeUpdate(userToDelete);
				}
				con.close();
				// Return that the user was successfully deleted
				return "Success!";
			}
		}
		// If there is an error, print it back to the user
		catch (Exception e) {
			return "Error! : " + e.getMessage();
		}
	}

	/*
	 * Method: registerFiles
	 * Function: registers a file via CORBA that the user currently has in their shared and not-shared file directories
	 * Parameters: String username - the username of the user who is requesting the registration
	 * The user is already authenticated at this point - so no need for the password
	 */
	public void registerFiles(String username) {
		// Make directories for the user on the local filesystem for their shared and not-shared files (if they don't already exist)
		File sharedPath = new File("users/" + username +"/shared");
		sharedPath.mkdirs();
		File notSharedPath = new File("users/" + username +"/not-shared");
		notSharedPath.mkdirs();

		// Now, save the contents of both of these directories
		File[] sharedFiles = sharedPath.listFiles();
		File[] notSharedFiles = notSharedPath.listFiles();

		// If there are shared files in the directory
		if (sharedFiles.length != 0) {
			// Loop to add all shared files
			for (File file : sharedFiles) {
				try {
					// Get the filename of the shared file
					String filename = file.getName();
					Connection con = getConnection();
					Statement statement = con.createStatement();
					// Register it into the database
					String addUserFile = "INSERT INTO sharedfiles (username, filename) VALUES ('" + username + "'" + ",'" + filename + "')";
					statement.executeUpdate(addUserFile);
					con.close();
				}
				// If there is an error, print it back to the user
				catch (Exception e) {
					System.out.println("Error! : " + e.getMessage());
				}
			}
		}
		// If there are non-shared files in the directory
		if (notSharedFiles.length != 0) {
			// Loop to add all non-shared files
			for (File file : notSharedFiles) {
				try {
					// Get the filename of the non-shared file
					String filename = file.getName();
					Connection con = getConnection();
					Statement statement = con.createStatement();
					// Register it into the database of non-shared files
					String addUserFile = "INSERT INTO notsharedfiles (username, filename) VALUES ('" + username + "'" + ",'" + filename + "')";
					statement.executeUpdate(addUserFile);
					con.close();
				}
				// If there is an error, print it back to the user
				catch (Exception e) {
					System.out.println("Error! : " + e.getMessage());
				}
			}
		}
	}

	/*
	 * Method: clearSharedFiles
	 * Function: clears the listing of shared files from the database for the user passed in the parameter.
	 * So, when the user logs off - this clears their files from being registered as shared - as they are no longer available
	 * Parameters: String username - the username of the user who needs to have shared files cleared
	 * The user is already authenticated at this point - so no need for the password
	 */
	public void clearSharedFiles(String username) {
		try {
			// Get a connection to the database and create a statement
			Connection con = getConnection();
			Statement statement = con.createStatement();
			// De-register all shared files listed on the database for the user
			String sharedFilesOwnedByUser = "DELETE FROM sharedfiles WHERE username = '" + username + "'";
			statement.executeUpdate(sharedFilesOwnedByUser);
			con.close();
		}
		// If there is an error, print it back to the user
		catch(Exception e) {
			System.out.println("Error! : " + e.getMessage());
		}
	}

	/*
	 * Method: clearNotSharedFiles
	 * Function: clears the listing of non-shared files from the database for the user passed in the parameter.
	 * So, when the user logs off - this clears their files from being registered as non-shared.
	 * Parameters: String username - the username of the user who needs to have non-shared files cleared
	 * The user is already authenticated at this point - so no need for the password
	 */
	public void clearNotSharedFiles(String username) {
		try {
			// Get a connection to the database and create a statement
			Connection con = getConnection();
			Statement statement = con.createStatement();
			// De-register all non-shared files listed on the database for the user
			String notSharedFilesOwnedByUser = "DELETE FROM notsharedfiles WHERE username = '" + username + "'";
			statement.executeUpdate(notSharedFilesOwnedByUser);
			con.close();
		}
		// If there is an error, print it back to the user
		catch(Exception e) {
			System.out.println("Error! : " + e.getMessage());
		}
	}

	/*
	 * Method: getUserFromPort
	 * Function: gets the user who has the port passed in as the parameter. This is used where users have the same address.
	 * Parameters: int port - the port being cross-referenced to find the user
	 */
	public String getUserFromPort(int port) {
		// Keep a string to store the username
		String username;
		try {
			// Get a connection to the database and create a statement
			Connection con = getConnection();
			Statement statement = con.createStatement();
			// Find a username based on the port
			String userNameQuery = "SELECT username FROM user WHERE port = '" + port + "'" ;
			ResultSet userName = statement.executeQuery(userNameQuery);
			// If there is a result with a match to the port
			if(userName.next()) {
				// Get the username based on the column and return it
				username = userName.getString(1);
				con.close();
				return username;
			}
			// Otherwise, close
			else {
				con.close();
			}
		}
		// If there is an error, print it back to the user
		catch(Exception e) {
			System.out.println("Error! : " + e.getMessage());
		}
		// If nothing is found, null will be returned
		return "null";
	}

	/*
	 * Method: getConnectionPort
	 * Function: gets the user's saved port from the database via CORBA. This is to ensure users do not have the same ports on the same address when their accounts are created
	 * Parameters: String username - the username of the user who the inquiry is about
	 * The user is already authenticated at this point - so no need for the password
	 */
	public int getConnectionPort(String username) {
		// Keep a variable to store the result of the port lookup
		int port;
		try {
			// Get a connection to the database and create a statement
			Connection con = getConnection();
			Statement statement = con.createStatement();
			// Find the port based on the passed username
			String portString = "SELECT port FROM user WHERE username = '" + username + "'" ;
			ResultSet userPort = statement.executeQuery(portString);
			// If the port has been found
			if(userPort.next()) {
				// Convert it to an integer and return it
				port = Integer.parseInt (userPort.getString(1));
				con.close();
				return port;
			}
			// Otherwise, no port was found
			else {
					con.close();
				}
		}
		// If there is an error, print it back to the user
		catch(Exception e) {
			System.out.println("Error! : " + e.getMessage());
		}
		// Return a port value of 3999, though this should not happen...
		return 3999;
	}

	/*
	 * Method: getConnectionAddress
	 * Function: gets the user's saved address from the database via CORBA.
	 * Parameters: String username - the username of the user who the inquiry is about
	 * The user is already authenticated at this point - so no need for the password
	 */
	public String getConnectionAddress(String username) {
		// Get a variable to store the address
		String address;
		try {
			// Get a connection to the database and create a statement
			Connection con = getConnection();
			Statement statement = con.createStatement();
			// Find the address given the passed in username
			String addressString = "SELECT address FROM user WHERE username = '" + username + "'" ;
			ResultSet userAddress = statement.executeQuery(addressString);
			// If a result is returned from the database then turn it into a string and return it
			if(userAddress.next()) {
				address = userAddress.getString(1);
				con.close();
				return address;
			}
			// Otherwise, one was not found - so close the connection
			else {
				con.close();
			}
		}
		// If there is an error, print it back to the user
		catch(Exception e) {
			System.out.println("Error! : " + e.getMessage());
		}
		// Return a port value of localhost, though this also should not happen...
		return "http://localhost";
	}

	/*
	 * Method: setConnectionAddress
	 * Function: sets the user's connection address to the the database via CORBA.
	 * This is used to make sure the user hasn't changed computers or networks
	 * Parameters: String username - the username of the user who we are setting the address for | String address - the address to set the connection to
	 * The user is already authenticated at this point - so no need for the password
	 */
	public void setConnectionAddress(String username, String address) {
		try {
			// Get a connection to the database and create a statement
			Connection con = getConnection();
			Statement statement = con.createStatement();
			// Update the address from the parameter to the database for the specified username
			statement.execute("UPDATE user SET address = '" + address + "' WHERE username = '" + username + "'");
			con.close();
		}
		// If there is an error, print it back to the user
		catch(Exception e) {
			System.out.println("Error! : " + e.getMessage());
		}
	}

	/*
	 * Method: viewAllSharedFiles
	 * Function: returns all shared files on the database through the server via CORBA.
	 * This will return a list of all shared files - keeping the users who own them, anonymous
	 */
	public String [] viewAllSharedFiles() {
		// Store the shared files in an an array list of strings for easy appending
		ArrayList<String> sharedFiles = new ArrayList<String>();
		try {
			// Get a connection to the database and create a statement
			Connection con = getConnection();
			Statement statement = con.createStatement();
			// Get all of the files from the sharedFiles table
			String getSharedFiles = "SELECT * FROM sharedfiles";
			ResultSet result = statement.executeQuery(getSharedFiles);
			// While there are still results in the sharedFiles database
			while(result.next()) {
				// Add them to the array list by getting the column filename's string representation
                sharedFiles.add(result.getString("filename"));
            }
			// Close the connection
			con.close();
		}
		// If there is an error, print it back to the user
		catch (Exception e) {
            System.out.println("Error! : " + e.getMessage());
        }
		// Return the shared files in an array of strings
		return sharedFiles.toArray(new String[0]);
	}

	/*
	 * Method: startFileShare
	 * Function: register's the user's selected file to share to the server via CORBA
	 * This will return a message if the registration of the file is successful
	 */
	public String startFileShare(String username, String filename) {
		try {
			// Get a connection to the database and create a statement
			Connection con = getConnection();
			Statement searchStatement = con.createStatement();
			// Make sure the user isn't already sharing the file
			String fileExistsCheck = "SELECT * FROM sharedfiles WHERE username = '" + username + "' AND filename = '" + filename + "'" ;
			ResultSet fileExists = searchStatement.executeQuery(fileExistsCheck);

			// If a result comes back, it means there are already sharing the file - return
			if(fileExists.next()) {
				con.close();
				return "You are already sharing this file!";
			}
			// Otherwise make changes to the database
			else {
				// De-register the item from the not-shared files database
				Statement deleteStatement = con.createStatement();
				String notSharedFileDelete = "DELETE FROM notsharedfiles WHERE username = '" + username + "' AND filename = '" + filename + "'";
				deleteStatement.executeUpdate(notSharedFileDelete);

				// Register the item to the shared files database
				Statement insertStatement = con.createStatement();
				String addUserFile = "INSERT INTO sharedfiles (username, filename) VALUES ('" + username + "'" + ",'" + filename + "')";
				insertStatement.executeUpdate(addUserFile);
				con.close();
				// Let the user know their file was shared successfully
				return "Your file " + filename +" is now being shared.";
			}
		}
		// If there is an error, print it back to the user
		catch (Exception e) {
			return "Error! : " + e.getMessage();
		}
	}

	/*
	 * Method: stopFileShare
	 * Function: de-register's the user's selected file to stop sharing to the server via CORBA
	 * This will return a message if the de-registration of the file is successful
	 */
	public String stopFileShare(String username, String filename) {
		try {
			// Get a connection to the database and create a statement
			Connection con = getConnection();
			Statement searchStatement = con.createStatement();
			// Make sure the user isn't already not sharing the file
			String fileExistsCheck = "SELECT * FROM notsharedfiles WHERE username = '" + username + "' AND filename = '" + filename + "'" ;
			ResultSet fileExists = searchStatement.executeQuery(fileExistsCheck);

			// If a result comes back, it means the file is already not shared - return
			if(fileExists.next()) {
				con.close();
				return "This file is already not being shared!";
			}
			// Otherwise make changes to the database
			else {
				// De-register the item from the shared files database
				Statement deleteStatement = con.createStatement();
				String sharedFileDelete = "DELETE FROM sharedfiles WHERE username = '" + username + "' AND filename = '" + filename + "'";
				deleteStatement.executeUpdate(sharedFileDelete);

				// Register the item to the not-shared files database
				Statement insertStatement = con.createStatement();
				String addUserFile = "INSERT INTO notsharedfiles (username, filename) VALUES ('" + username + "'" + ",'" + filename + "')";
				insertStatement.executeUpdate(addUserFile);
				con.close();
				// Let the user know their file was shared successfully
				return "Your file " + filename +" is no longer being shared.";
			}
		}
		// If there is an error, print it back to the user
		catch (Exception e) {
			return "Error! : " + e.getMessage();
		}
	}

	/*
	 * Method: findFile
	 * Function: will return a list of addresses and ports that currently have the requested filename available to share
	 * Since muliple users can share the same file, this could return back multiple items
	 */
	public String [] findFile(String filename) {
		// Create an array list of strings that will store the available addresses for easy appending
		ArrayList<String> availableAddresses = new ArrayList<String>();
		try {
			// Get a connection to the database and create a statement
			Connection con = getConnection();
			Statement statement = con.createStatement();
			// Check the database to see if the shared file exists
			String searchForSharedFile = "SELECT * FROM sharedfiles WHERE filename = '" + filename + "'";
			ResultSet fileExists = statement.executeQuery(searchForSharedFile);
			// Create another array list that will hold all users with the file - because multiple users can share the same file
			ArrayList<String> usersWithFile = new ArrayList<String>();

			// While the file exists in the result set, it means we have a user that is sharing it
			while(fileExists.next()) {
				// Get the username and add it to the usersWithFile array list
				usersWithFile.add(fileExists.getString("username"));
			}

			// For each user with the file
			for(String user : usersWithFile) {
				// Find  their user profile and select their username, port, and address
				String searchForUserInformation = "SELECT address, port, username FROM user WHERE username = '" + user + "'";
				ResultSet usersWithFileResults = statement.executeQuery(searchForUserInformation);

				// While users are still in the result set
				while(usersWithFileResults.next()) {
					// Create a special URL that will be decoded by the download method in Client and by ClientHelper to help transfer the file over the socket.
					// The url is of the format: http://[address]:[port]}username
					// Since } is not valid as a web url, it'll be used as a regex match character to do a string split so that http://[address]:[port] can be separated from username
					availableAddresses.add("http://" + usersWithFileResults.getString("address") + ":" + usersWithFileResults.getString("port") + "}" + usersWithFileResults.getString("username"));
				}
			}
			con.close();
		}
		// If there is an error, print it back to the user
		catch (Exception e) {
			System.out.println("Error! : " + e.getMessage());
		}
		// Return the shared files in an array of strings
		return availableAddresses.toArray(new String[0]);
	}
}
