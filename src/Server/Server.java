package Server;

import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;

import Common.*;

/*
 * Class: Server
 * Function: Handles the main CORBA setup and also the kicks off the database connection function
 * Most of the code in this file is based on the helpfile as per the course notes and:
 * https://docs.oracle.com/javase/7/docs/technotes/guides/idl/jidlExample.html
 */
public class Server {

	/*
	 * Method: Main
	 * Function: Starts the CORBA related functions and gets everything available for use
	 */
	public static void main(String args[]) {
		DatabaseHelper database = new DatabaseHelper();
		database.getConnection();

		try {
			// Create and initialize the ORB
			ORB orb = ORB.init(args, null);

			// Get reference to rootpoa & activate the POAManager
			POA rootPOA = POAHelper.narrow(orb.resolve_initial_references("RootPOA"));
			rootPOA.the_POAManager().activate();

			// Creates a servant and registers it with the orb
			ServerHelper serverHelper = new ServerHelper();
			serverHelper.setORB(orb);

			// Get object reference from the servant
			org.omg.CORBA.Object ref = rootPOA.servant_to_reference(serverHelper);
		    ServerServant href = ServerServantHelper.narrow(ref);

		    // Gets the root naming context
		    org.omg.CORBA.Object obj = orb.resolve_initial_references("NameService");
	        NamingContextExt namingContext = NamingContextExtHelper.narrow(obj);

			// Bind the Object Reference in Naming
	        NameComponent path[] = namingContext.to_name("TME2");
	        namingContext.rebind(path, href);

	        // If everything is ok, display the welcome message that the server is running
			System.out.println("------------------------------Welcome!------------------------------");
			System.out.println("---------------The server application is now running!---------------");
			System.out.println("--------------------------------------------------------------------");

	        orb.run();
		}
		// Otherwise, we have run into an error - print it out
		catch(Exception e) {
			System.out.println("Error! : " + e.getMessage());
		}
	}
}
