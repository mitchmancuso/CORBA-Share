package Client;

import Common.ServerServant;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
 * Class: User
 * Function: User is mostly used to hash the user's plaintext password before sending it over to the server, and user management.
 * Users can be registered and deleted from the database rather than having to do it through MySQL's admin panel for testing
 */
public class User {

    //  A username and password which are the key components of a User
    String username;
    String password;

    /*
     * Method: User
     * Function: Creates the user object and hashes the plaintext password.
     * Parameters: String username - a username | String password - a plaintext password
     */
    public User(String username, String password) throws NoSuchAlgorithmException{
        this.username = username;
        this.password = hashPassword(password);
    }

    /*
     * Method: hashPassword
     * Function: Hashes the user's plaintext password so that it is not saved to the database in this format
     * Parameters: String passwordToHash
     */
    private String hashPassword(String passwordToHash) throws NoSuchAlgorithmException {

        // Creates a new message digest with MD5 as the algorithm type and UTF-8 as the character set
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hashInBytes = md.digest(passwordToHash.getBytes(StandardCharsets.UTF_8));

        // Builds a hash based on every byte
        StringBuilder builtHash = new StringBuilder();
        for (byte b : hashInBytes) {
            builtHash.append(String.format("%02x", b));
        }
        //Return the hashed password as a string
        return builtHash.toString();
    }

    /*
     * Method: register
     * Function: registers a user using a username and hashed password
     * Parameters: ServerServant server
     */
    public String register(ServerServant server) {
        // Returns either success or failure depending on things like the user already existing, etc.
        return server.registerUser(this.getUsername(),this.getHashedPassword());
    }

    /*
     * Method: delete
     * Function: deletes a user using a username and hashed password to verify the user has authorization to delete their own account
     * Parameters: ServerServant server
     */
    public String delete(ServerServant server) {
        // Returns either success or failure depending on things like the user trying to delete a user that doesn't exist, etc.
        return server.deleteUser(this.getUsername(),this.getHashedPassword());
    }

    /*
     * Method: getUsername
     * Function: returns the username of a User object in the form of a String
     */
    public String getUsername(){
        return this.username;
    }

    /*
     * Method: getHashedPassword
     * Function: returns the hashed password of a User object in the form of a String
     */
    public String getHashedPassword() {
        return this.password;
    }
}
