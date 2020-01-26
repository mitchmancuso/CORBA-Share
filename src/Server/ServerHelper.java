package Server;

import org.omg.CORBA.*;

import Common.*;

/*
 * Class: ServerHelper
 * An extension of the ServerServantPOA that contains the methods that mirror the methods defined in the DatabaseHelper
 */
public class ServerHelper extends ServerServantPOA {
	ORB orb;
	DatabaseHelper database;

	/*
	 * Method: setOrb
	 * Function: Sets the orbVal and instantiates a new instance of a DatabaseHelper object
	 */
	public void setORB(ORB orbVal) {
		orb = orbVal;
		database = new DatabaseHelper();
	}

	/*
	 * Method: login
	 * Function: logs in given the credentials from the user input
	 * Parameters: String username - the username entered by the user | String hashedPassword - the hashedPassword determined by the User object
	 */
	public String login(String username, String hashedPassword) {
		// Since the database is instantiated when the orb is set, this will perform the login function as defined in DatabaseHelper
		return database.login(username, hashedPassword);
	}

	/*
	 * Method: registerUser
	 * Function: registers a user and adds their information to the database so it can be done via the command line
	 * Parameters: String username - the username entered by the user | String hashedPassword - the hashedPassword determined by the User object
	 */
	public String registerUser(String username, String hashedPassword) {
		// Since the database is instantiated when the orb is set, this will perform the registration function as defined in DatabaseHelper
		return database.registerUser(username, hashedPassword);
	}

	/*
	 * Method: deleteUser
	 * Function: deletes a user and removes their information from the database so it can be done via the command line
	 * Parameters: String username - the username entered by the user | String hashedPassword - the hashedPassword determined by the User object
	 * These are requested to authenticate the user before they can be deleted.
	 */
	public String deleteUser(String username, String hashedPassword) {
		// Since the database is instantiated when the orb is set, this will perform the delete user function as defined in DatabaseHelper
		return database.deleteUser(username, hashedPassword);
	}

	/*
	 * Method: registerFiles
	 * Function: registers a file via CORBA that the user currently has in their shared and not-shared file directories
	 * Parameters: String username - the username of the user who is requesting the registration
	 * The user is already authenticated at this point - so no need for the password
	 */
    public void registerFiles(String username) {
		// Since the database is instantiated when the orb is set, this will perform the register files function as defined in DatabaseHelper
		database.registerFiles(username);
	}

	/*
	 * Method: clearSharedFiles
	 * Function: clears the listing of shared files from the database for the user passed in the parameter.
	 * So, when the user logs off - this clears their files from being registered as shared - as they are no longer available
	 * Parameters: String username - the username of the user who needs to have shared files cleared
	 * The user is already authenticated at this point - so no need for the password
	 */
	public void clearSharedFiles(String username) {
		// Since the database is instantiated when the orb is set, this will perform the register files function as defined in DatabaseHelper
		database.clearSharedFiles(username);
	}

	/*
	 * Method: clearNotSharedFiles
	 * Function: clears the listing of non-shared files from the database for the user passed in the parameter.
	 * So, when the user logs off - this clears their files from being registered as non-shared.
	 * Parameters: String username - the username of the user who needs to have non-shared files cleared
	 * The user is already authenticated at this point - so no need for the password
	 */
	public void clearNotSharedFiles(String username) {
		// Since the database is instantiated when the orb is set, this will perform the clear not shared files function as defined in DatabaseHelper
		database.clearNotSharedFiles(username);
	}

	/*
	 * Method: getUserFromPort
	 * Function: gets the user who has the port passed in as the parameter. This is used where users have the same address.
	 * Parameters: int port - the port being cross-referenced to find the user
	 */
	public String getUserFromPort(int port) {
		// Since the database is instantiated when the orb is set, this will perform the get user from port function as defined in DatabaseHelper
		return database.getUserFromPort(port);
	}

	/*
	 * Method: getConnectionPort
	 * Function: gets the user's saved port from the database via CORBA. This is to ensure users do not have the same ports on the same address when their accounts are created
	 * Parameters: String username - the username of the user who the inquiry is about
	 * The user is already authenticated at this point - so no need for the password
	 */
	public int getConnectionPort(String username) {
		// Since the database is instantiated when the orb is set, this will perform the get connection port function as defined in DatabaseHelper
		return database.getConnectionPort(username);
	}

	/*
	 * Method: getConnectionAddress
	 * Function: gets the user's saved address from the database via CORBA.
	 * Parameters: String username - the username of the user who the inquiry is about
	 * The user is already authenticated at this point - so no need for the password
	 */
	public String getConnectionAddress(String username) {
		// Since the database is instantiated when the orb is set, this will perform the get connection address function as defined in DatabaseHelper
		return database.getConnectionAddress(username);
	}

	/*
	 * Method: setConnectionAddress
	 * Function: sets the user's connection address to the the database via CORBA.
	 * This is used to make sure the user hasn't changed computers or networks
	 * Parameters: String username - the username of the user who we are setting the address for | String address - the address to set the connection to
	 * The user is already authenticated at this point - so no need for the password
	 */
	public void setConnectionAddress(String username, String address) {
		// Since the database is instantiated when the orb is set, this will perform the set connection address function as defined in DatabaseHelper
		database.setConnectionAddress(username, address);
	}

	/*
	 * Method: viewAllSharedFiles
	 * Function: returns all shared files on the database through the server via CORBA.
	 * This will return a list of all shared files - keeping the users who own them, anonymous
	 */
	public String [] viewAllSharedFiles() {
		// Since the database is instantiated when the orb is set, this will perform the view all shared files function as defined in DatabaseHelper
		return database.viewAllSharedFiles();
	}

	/*
	 * Method: startFileShare
	 * Function: register's the user's selected file to share to the server via CORBA
	 * This will return a message if the registration of the file is successful
	 */
	public String startFileShare(String username, String filename) {
		// Since the database is instantiated when the orb is set, this will perform the start file share function as defined in DatabaseHelper
		return database.startFileShare(username, filename);
	}

	/*
	 * Method: stopFileShare
	 * Function: de-register's the user's selected file to stop sharing to the server via CORBA
	 * This will return a message if the de-registration of the file is successful
	 */
	public String stopFileShare(String username, String filename) {
		// Since the database is instantiated when the orb is set, this will perform the stop file share function as defined in DatabaseHelper
		return database.stopFileShare(username, filename);
	}

	/*
	 * Method: findFile
	 * Function: will return a list of addresses and ports that currently have the requested filename available to share
	 * Since muliple users can share the same file, this could return back multiple items
	 */
	public String [] findFile(String filename) {
		// Since the database is instantiated when the orb is set, this will perform the find file function as defined in DatabaseHelper
		return database.findFile(filename);
	}
}
