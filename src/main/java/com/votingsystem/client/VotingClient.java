package com.votingsystem.client;

import com.votingsystem.common.Poll;
import com.votingsystem.common.PollOption;
import com.votingsystem.common.VotingService;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client class for the Voting System
 */
public class VotingClient {
    
    private static final Logger LOGGER = Logger.getLogger(VotingClient.class.getName());
    private static final int RMI_PORT = 1099;
    private static final String SERVICE_NAME = "VotingService";
    
    private String serverHost;
    private VotingService votingService;
    private String sessionToken;
    
    /**
     * Constructor for the VotingClient
     * 
     * @param serverHost the hostname of the RMI server
     */
    public VotingClient(String serverHost) {
        this.serverHost = serverHost;
        this.sessionToken = null;
    }
    
    /**
     * Connect to the RMI server
     * 
     * @return true if connection is successful, false otherwise
     */
    public boolean connect() {
        try {
            LOGGER.info("Connecting to RMI server at " + serverHost + ":" + RMI_PORT);
            
            // Set security manager if needed
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }
            
            // Get the registry
            Registry registry = LocateRegistry.getRegistry(serverHost, RMI_PORT);
            
            // Look up the remote object
            votingService = (VotingService) registry.lookup(SERVICE_NAME);
            
            LOGGER.info("Connected to RMI server successfully");
            return true;
            
        } catch (RemoteException | NotBoundException e) {
            LOGGER.log(Level.SEVERE, "Error connecting to RMI server", e);
            return false;
        }
    }
    
    /**
     * Register a new user
     * 
     * @param username the username
     * @param password the password
     * @return true if registration is successful, false otherwise
     */
    public boolean signup(String username, String password) {
        try {
            LOGGER.info("Attempting to register with username: " + username);
            
            boolean result = votingService.signup(username, password);
            
            if (result) {
                LOGGER.info("Registration successful");
            }
            
            return result;
            
        } catch (RemoteException e) {
            LOGGER.log(Level.SEVERE, "Remote error during registration", e);
            return false;
        } catch (SecurityException e) {
            LOGGER.log(Level.WARNING, "Registration failed: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Login to the voting system
     * 
     * @param username the username
     * @param password the password
     * @return true if login is successful, false otherwise
     */
    public boolean login(String username, String password) {
        try {
            LOGGER.info("Attempting to login with username: " + username);
            
            sessionToken = votingService.login(username, password);
            
            LOGGER.info("Login successful");
            return true;
            
        } catch (RemoteException e) {
            LOGGER.log(Level.SEVERE, "Remote error during login", e);
            return false;
        } catch (SecurityException e) {
            LOGGER.log(Level.WARNING, "Login failed: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Get all available polls
     * 
     * @return a list of available polls, or null if an error occurs
     */
    public List<Poll> getAvailablePolls() {
        try {
            LOGGER.info("Getting available polls");
            
            if (sessionToken == null) {
                LOGGER.warning("Not logged in");
                return null;
            }
            
            return votingService.getAvailablePolls(sessionToken);
            
        } catch (RemoteException e) {
            LOGGER.log(Level.SEVERE, "Remote error while getting polls", e);
            return null;
        } catch (SecurityException e) {
            LOGGER.log(Level.WARNING, "Session error: " + e.getMessage(), e);
            sessionToken = null;
            return null;
        }
    }
    
    /**
     * Cast a vote for a specific poll or change an existing vote
     * 
     * @param pollId the ID of the poll
     * @param optionId the ID of the selected option
     * @return true if the vote was successfully cast or changed, false otherwise
     */
    public boolean vote(int pollId, int optionId) {
        try {
            LOGGER.info("Casting vote for poll " + pollId + ", option " + optionId);
            
            if (sessionToken == null) {
                LOGGER.warning("Not logged in");
                return false;
            }
            
            return votingService.vote(sessionToken, pollId, optionId);
            
        } catch (RemoteException e) {
            LOGGER.log(Level.SEVERE, "Remote error while voting", e);
            return false;
        } catch (SecurityException e) {
            LOGGER.log(Level.WARNING, "Session error: " + e.getMessage(), e);
            sessionToken = null;
            return false;
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Voting error: " + e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Get the results of a specific poll
     * 
     * @param pollId the ID of the poll
     * @return a map of option IDs to vote counts, or null if an error occurs
     */
    public Map<Integer, Integer> getPollResults(int pollId) {
        try {
            LOGGER.info("Getting results for poll " + pollId);
            
            if (sessionToken == null) {
                LOGGER.warning("Not logged in");
                return null;
            }
            
            return votingService.getPollResults(sessionToken, pollId);
            
        } catch (RemoteException e) {
            LOGGER.log(Level.SEVERE, "Remote error while getting poll results", e);
            return null;
        } catch (SecurityException e) {
            LOGGER.log(Level.WARNING, "Session error: " + e.getMessage(), e);
            sessionToken = null;
            return null;
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Poll error: " + e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Logout from the voting system
     * 
     * @return true if logout is successful, false otherwise
     */
    public boolean logout() {
        try {
            LOGGER.info("Logging out");
            
            if (sessionToken == null) {
                LOGGER.warning("Not logged in");
                return false;
            }
            
            boolean result = votingService.logout(sessionToken);
            
            if (result) {
                sessionToken = null;
            }
            
            return result;
            
        } catch (RemoteException e) {
            LOGGER.log(Level.SEVERE, "Remote error while logging out", e);
            return false;
        }
    }
    
    /**
     * Check if the client is logged in
     * 
     * @return true if logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return sessionToken != null;
    }
    
    /**
     * Get the option ID that the user previously voted for in a specific poll
     * 
     * @param pollId the ID of the poll
     * @return the option ID that the user voted for, or -1 if the user hasn't voted in this poll or an error occurs
     */
    public int getUserVote(int pollId) {
        try {
            LOGGER.info("Getting user vote for poll " + pollId);
            
            if (sessionToken == null) {
                LOGGER.warning("Not logged in");
                return -1;
            }
            
            return votingService.getUserVote(sessionToken, pollId);
            
        } catch (RemoteException e) {
            LOGGER.log(Level.SEVERE, "Remote error while getting user vote", e);
            return -1;
        } catch (SecurityException e) {
            LOGGER.log(Level.WARNING, "Session error: " + e.getMessage(), e);
            sessionToken = null;
            return -1;
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.WARNING, "Error getting user vote: " + e.getMessage(), e);
            return -1;
        }
    }
    
    /**
     * Check if the current user is an admin
     * 
     * @return true if the user is an admin, false otherwise
     */
    public boolean isAdmin() {
        try {
            LOGGER.info("Checking if user is admin");
            
            if (sessionToken == null) {
                LOGGER.warning("Not logged in");
                return false;
            }
            
            return votingService.isAdmin(sessionToken);
            
        } catch (RemoteException e) {
            LOGGER.log(Level.SEVERE, "Remote error while checking admin status", e);
            return false;
        } catch (SecurityException e) {
            LOGGER.log(Level.WARNING, "Session error: " + e.getMessage(), e);
            sessionToken = null;
            return false;
        }
    }
    
    /**
     * Create a new poll (admin only)
     * 
     * @param title the title of the poll
     * @param description the description of the poll
     * @param options the list of options for the poll
     * @return the ID of the newly created poll, or -1 if creation failed
     */
    public int createPoll(String title, String description, List<String> options) {
        try {
            LOGGER.info("Creating poll: " + title);
            
            if (sessionToken == null) {
                LOGGER.warning("Not logged in");
                return -1;
            }
            
            return votingService.createPoll(sessionToken, title, description, options);
            
        } catch (RemoteException e) {
            LOGGER.log(Level.SEVERE, "Remote error while creating poll", e);
            return -1;
        } catch (SecurityException e) {
            LOGGER.log(Level.WARNING, "Security error: " + e.getMessage(), e);
            return -1;
        }
    }
    
    /**
     * Get the name of a poll option by its ID
     * 
     * @param poll the poll containing the option
     * @param optionId the ID of the option
     * @return the name of the option, or "Unknown" if not found
     */
    public String getOptionNameById(Poll poll, int optionId) {
        for (PollOption option : poll.getOptions()) {
            if (option.getId() == optionId) {
                return option.getText();
            }
        }
        return "Unknown";
    }
}
