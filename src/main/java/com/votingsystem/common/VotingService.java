package com.votingsystem.common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

/**
 * Remote interface for the Voting Service
 */
public interface VotingService extends Remote {
    
    /**
     * Register a new user
     * 
     * @param username the username
     * @param password the password
     * @return true if registration is successful, false otherwise
     * @throws RemoteException if a remote communication error occurs
     * @throws SecurityException if the username already exists
     */
    boolean signup(String username, String password) throws RemoteException, SecurityException;
    
    /**
     * Authenticate a user with username and password
     * 
     * @param username the username
     * @param password the password
     * @return a session token if authentication is successful, null otherwise
     * @throws RemoteException if a remote communication error occurs
     * @throws SecurityException if authentication fails
     */
    String login(String username, String password) throws RemoteException, SecurityException;
    
    /**
     * Get all available polls
     * 
     * @param sessionToken the session token obtained from login
     * @return a list of available polls
     * @throws RemoteException if a remote communication error occurs
     * @throws SecurityException if the session token is invalid
     */
    List<Poll> getAvailablePolls(String sessionToken) throws RemoteException, SecurityException;
    
    /**
     * Cast a vote for a specific poll or change an existing vote
     * 
     * @param sessionToken the session token obtained from login
     * @param pollId the ID of the poll
     * @param optionId the ID of the selected option
     * @return true if the vote was successfully cast or changed, false otherwise
     * @throws RemoteException if a remote communication error occurs
     * @throws SecurityException if the session token is invalid
     * @throws IllegalArgumentException if the poll or option doesn't exist
     */
    boolean vote(String sessionToken, int pollId, int optionId) 
            throws RemoteException, SecurityException, IllegalArgumentException;
    
    /**
     * Get the results of a specific poll
     * 
     * @param sessionToken the session token obtained from login
     * @param pollId the ID of the poll
     * @return a map of option IDs to vote counts
     * @throws RemoteException if a remote communication error occurs
     * @throws SecurityException if the session token is invalid
     * @throws IllegalArgumentException if the poll doesn't exist
     */
    Map<Integer, Integer> getPollResults(String sessionToken, int pollId) 
            throws RemoteException, SecurityException, IllegalArgumentException;
    
    /**
     * Logout the user
     * 
     * @param sessionToken the session token obtained from login
     * @return true if logout was successful
     * @throws RemoteException if a remote communication error occurs
     */
    boolean logout(String sessionToken) throws RemoteException;
    
    /**
     * Get the option ID that the user previously voted for in a specific poll
     * 
     * @param sessionToken the session token obtained from login
     * @param pollId the ID of the poll
     * @return the option ID that the user voted for, or -1 if the user hasn't voted in this poll
     * @throws RemoteException if a remote communication error occurs
     * @throws SecurityException if the session token is invalid
     * @throws IllegalArgumentException if the poll doesn't exist
     */
    int getUserVote(String sessionToken, int pollId) 
            throws RemoteException, SecurityException, IllegalArgumentException;
    
    /**
     * Check if the user is an admin
     * 
     * @param sessionToken the session token obtained from login
     * @return true if the user is an admin, false otherwise
     * @throws RemoteException if a remote communication error occurs
     * @throws SecurityException if the session token is invalid
     */
    boolean isAdmin(String sessionToken) throws RemoteException, SecurityException;
    
    /**
     * Create a new poll (admin only)
     * 
     * @param sessionToken the session token obtained from login
     * @param title the title of the poll
     * @param description the description of the poll
     * @param options the list of options for the poll
     * @return the ID of the newly created poll, or -1 if creation failed
     * @throws RemoteException if a remote communication error occurs
     * @throws SecurityException if the session token is invalid or the user is not an admin
     */
    int createPoll(String sessionToken, String title, String description, List<String> options) 
            throws RemoteException, SecurityException;
}
