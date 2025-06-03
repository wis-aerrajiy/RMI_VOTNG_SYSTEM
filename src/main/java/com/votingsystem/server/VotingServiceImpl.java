package com.votingsystem.server;

import com.votingsystem.common.Poll;
import com.votingsystem.common.PollOption;
import com.votingsystem.common.VotingService;
import com.votingsystem.common.PasswordUtils;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the VotingService interface
 */
public class VotingServiceImpl extends UnicastRemoteObject implements VotingService {
    
    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = Logger.getLogger(VotingServiceImpl.class.getName());
    
    // In-memory storage for users (username -> password)
    private final Map<String, String> users;
    
    // In-memory storage for active sessions (sessionToken -> username)
    private final Map<String, SessionInfo> activeSessions;
    
    // In-memory storage for polls (pollId -> Poll)
    private final Map<Integer, Poll> polls;
    
    // In-memory storage for votes (username -> Map of pollId -> optionId)
    private final Map<String, Map<Integer, Integer>> userVotes;
    
    // In-memory storage for poll results (pollId -> (optionId -> count))
    private final Map<Integer, Map<Integer, Integer>> pollResults;
    
    // Set of admin usernames
    private final Set<String> admins;
    
    // Next poll ID for auto-increment
    private int nextPollId;
    
    // Session timeout in milliseconds (30 minutes)
    private static final long SESSION_TIMEOUT = 30 * 60 * 1000;
    
    public VotingServiceImpl() throws RemoteException {
        super();
        this.users = new HashMap<>();
        this.activeSessions = new ConcurrentHashMap<>();
        this.polls = new HashMap<>();
        this.userVotes = new HashMap<>();
        this.pollResults = new HashMap<>();
        this.admins = new HashSet<>();
        this.nextPollId = 1;
        
        // Initialize with some sample data
        initializeSampleData();
        
        // Start a thread to clean up expired sessions
        startSessionCleanupThread();
        
        LOGGER.info("VotingService initialized");
    }
    
    private void initializeSampleData() {
        LOGGER.info("Initializing sample data");
        
        // Add sample users with hashed passwords
        users.put("admin", PasswordUtils.hashPassword("admin123"));
        
        // Set admin users
        admins.add("admin");
        
        // Add sample polls
        Poll poll1 = new Poll(1, "Favorite Programming Language", "What is your favorite programming language?");
        poll1.addOption(new PollOption(1, "Java"));
        poll1.addOption(new PollOption(2, "Python"));
        poll1.addOption(new PollOption(3, "JavaScript"));
        poll1.addOption(new PollOption(4, "C++"));
        polls.put(poll1.getId(), poll1);
        
        Poll poll2 = new Poll(2, "Best Operating System", "What is the best operating system?");
        poll2.addOption(new PollOption(1, "Windows"));
        poll2.addOption(new PollOption(2, "macOS"));
        poll2.addOption(new PollOption(3, "Linux"));
        polls.put(poll2.getId(), poll2);
        
        // Initialize results for each poll
        for (Poll poll : polls.values()) {
            Map<Integer, Integer> results = new HashMap<>();
            for (PollOption option : poll.getOptions()) {
                results.put(option.getId(), 0);
            }
            pollResults.put(poll.getId(), results);
        }
        
        // Set next poll ID to be one more than the highest existing poll ID
        nextPollId = polls.keySet().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
        
        LOGGER.info("Sample data initialized");
    }
    
    private void startSessionCleanupThread() {
        Thread cleanupThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(60 * 1000); // Check every minute
                    
                    long currentTime = System.currentTimeMillis();
                    List<String> expiredSessions = new ArrayList<>();
                    
                    for (Map.Entry<String, SessionInfo> entry : activeSessions.entrySet()) {
                        if (currentTime - entry.getValue().getLastAccessTime() > SESSION_TIMEOUT) {
                            expiredSessions.add(entry.getKey());
                        }
                    }
                    
                    for (String sessionToken : expiredSessions) {
                        activeSessions.remove(sessionToken);
                        LOGGER.info("Expired session removed: " + sessionToken);
                    }
                } catch (InterruptedException e) {
                    LOGGER.log(Level.SEVERE, "Session cleanup thread interrupted", e);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        
        cleanupThread.setDaemon(true);
        cleanupThread.start();
        
        LOGGER.info("Session cleanup thread started");
    }
    
    @Override
    public boolean signup(String username, String password) throws RemoteException, SecurityException {
        LOGGER.info("Signup attempt for username: " + username);
        
        // Check if the username already exists
        if (users.containsKey(username)) {
            LOGGER.warning("Signup failed: username already exists: " + username);
            throw new SecurityException("Username already exists");
        }
        
        // Add the user (password should already be hashed by client)
        users.put(username, password);
        userVotes.put(username, new HashMap<>());
        
        LOGGER.info("Signup successful for username: " + username);
        return true;
    }
    
    @Override
    public String login(String username, String password) throws RemoteException, SecurityException {
        LOGGER.info("Login attempt for username: " + username);
        
        // Check if the username exists and the password is correct
        // Password should already be hashed by client
        if (!users.containsKey(username) || !users.get(username).equals(password)) {
            LOGGER.warning("Login failed for username: " + username);
            throw new SecurityException("Invalid username or password");
        }
        
        // Generate a session token
        String sessionToken = UUID.randomUUID().toString();
        
        // Store the session
        activeSessions.put(sessionToken, new SessionInfo(username));
        
        LOGGER.info("User logged in successfully: " + username);
        return sessionToken;
    }
    
    @Override
    public List<Poll> getAvailablePolls(String sessionToken) throws RemoteException, SecurityException {
        validateSession(sessionToken);
        
        LOGGER.info("Getting available polls for session: " + sessionToken);
        
        // Update last access time
        activeSessions.get(sessionToken).updateLastAccessTime();
        
        return new ArrayList<>(polls.values());
    }
    
    @Override
    public boolean vote(String sessionToken, int pollId, int optionId) 
            throws RemoteException, SecurityException, IllegalArgumentException {
        String username = validateSession(sessionToken);
        
        LOGGER.info("Vote attempt by user: " + username + " for poll: " + pollId + ", option: " + optionId);
        
        // Update last access time
        activeSessions.get(sessionToken).updateLastAccessTime();
        
        // Check if poll exists
        if (!polls.containsKey(pollId)) {
            LOGGER.warning("Vote attempt for non-existent poll: " + pollId);
            throw new IllegalArgumentException("Poll does not exist");
        }
        
        Poll poll = polls.get(pollId);
        
        // Check if poll is active
        if (!poll.isActive()) {
            LOGGER.warning("Vote attempt for inactive poll: " + pollId);
            throw new IllegalStateException("Poll is not active");
        }
        
        // Check if option exists
        boolean optionExists = false;
        for (PollOption option : poll.getOptions()) {
            if (option.getId() == optionId) {
                optionExists = true;
                break;
            }
        }
        
        if (!optionExists) {
            LOGGER.warning("Vote attempt for non-existent option: " + optionId);
            throw new IllegalArgumentException("Option does not exist");
        }
        
        // Initialize user votes map if it doesn't exist
        if (!userVotes.containsKey(username)) {
            userVotes.put(username, new HashMap<>());
        }
        
        Map<Integer, Integer> userPollVotes = userVotes.get(username);
        Map<Integer, Integer> results = pollResults.get(pollId);
        
        // Check if user has already voted in this poll
        if (userPollVotes.containsKey(pollId)) {
            // User is changing their vote
            int previousVote = userPollVotes.get(pollId);
            LOGGER.info("User " + username + " is changing vote in poll: " + pollId + 
                      " from option: " + previousVote + " to option: " + optionId);
            
            // Remove the previous vote from results
            results.put(previousVote, results.get(previousVote) - 1);
            
            // Add the new vote to results
            results.put(optionId, results.get(optionId) + 1);
            
            // Update the user's vote for this poll
            userPollVotes.put(pollId, optionId);
            
            LOGGER.info("Vote changed successfully for user: " + username + " in poll: " + pollId);
        } else {
            // First time voting in this poll
            // Record the vote in results
            results.put(optionId, results.get(optionId) + 1);
            
            // Record the user's vote for this poll
            userPollVotes.put(pollId, optionId);
            
            LOGGER.info("Vote recorded successfully for user: " + username + " in poll: " + pollId);
        }
        
        return true;
    }
    
    @Override
    public Map<Integer, Integer> getPollResults(String sessionToken, int pollId) 
            throws RemoteException, SecurityException, IllegalArgumentException {
        validateSession(sessionToken);
        
        LOGGER.info("Getting results for poll: " + pollId);
        
        // Update last access time
        activeSessions.get(sessionToken).updateLastAccessTime();
        
        // Check if poll exists
        if (!polls.containsKey(pollId)) {
            LOGGER.warning("Results request for non-existent poll: " + pollId);
            throw new IllegalArgumentException("Poll does not exist");
        }
        
        return new HashMap<>(pollResults.get(pollId));
    }
    
    @Override
    public boolean logout(String sessionToken) throws RemoteException {
        LOGGER.info("Logout attempt with session token: " + sessionToken);
        
        if (activeSessions.containsKey(sessionToken)) {
            activeSessions.remove(sessionToken);
            LOGGER.info("User logged out successfully");
            return true;
        }
        
        LOGGER.warning("Logout attempt with invalid session token: " + sessionToken);
        return false;
    }
    
    @Override
    public int getUserVote(String sessionToken, int pollId) 
            throws RemoteException, SecurityException, IllegalArgumentException {
        String username = validateSession(sessionToken);
        
        LOGGER.info("Getting user vote for user: " + username + " in poll: " + pollId);
        
        // Update last access time
        activeSessions.get(sessionToken).updateLastAccessTime();
        
        // Check if poll exists
        if (!polls.containsKey(pollId)) {
            LOGGER.warning("Get user vote attempt for non-existent poll: " + pollId);
            throw new IllegalArgumentException("Poll does not exist");
        }
        
        // Check if user has voted in this poll
        if (!userVotes.containsKey(username) || !userVotes.get(username).containsKey(pollId)) {
            return -1; // User hasn't voted in this poll
        }
        
        // Return the option ID that the user voted for
        return userVotes.get(username).get(pollId);
    }
    
    @Override
    public boolean isAdmin(String sessionToken) throws RemoteException, SecurityException {
        String username = validateSession(sessionToken);
        
        LOGGER.info("Checking if user is admin: " + username);
        
        // Update last access time
        activeSessions.get(sessionToken).updateLastAccessTime();
        
        // Check if the user is an admin
        return admins.contains(username);
    }
    
    @Override
    public int createPoll(String sessionToken, String title, String description, List<String> options) 
            throws RemoteException, SecurityException {
        String username = validateSession(sessionToken);
        
        LOGGER.info("Create poll attempt by user: " + username);
        
        // Update last access time
        activeSessions.get(sessionToken).updateLastAccessTime();
        
        // Check if the user is an admin
        if (!admins.contains(username)) {
            LOGGER.warning("Non-admin user " + username + " attempted to create a poll");
            throw new SecurityException("Only administrators can create polls");
        }
        
        // Validate input
        if (title == null || title.trim().isEmpty()) {
            LOGGER.warning("Invalid poll title");
            throw new IllegalArgumentException("Poll title cannot be empty");
        }
        
        if (options == null || options.size() < 2) {
            LOGGER.warning("Invalid poll options: at least 2 options required");
            throw new IllegalArgumentException("At least 2 options are required for a poll");
        }
        
        // Create the poll
        int pollId = nextPollId++;
        Poll poll = new Poll(pollId, title, description);
        
        // Add options to the poll
        for (int i = 0; i < options.size(); i++) {
            String optionText = options.get(i);
            if (optionText == null || optionText.trim().isEmpty()) {
                LOGGER.warning("Invalid poll option: empty option text");
                throw new IllegalArgumentException("Poll option text cannot be empty");
            }
            poll.addOption(new PollOption(i + 1, optionText));
        }
        
        // Add the poll to the polls map
        polls.put(pollId, poll);
        
        // Initialize results for the poll
        Map<Integer, Integer> results = new HashMap<>();
        for (PollOption option : poll.getOptions()) {
            results.put(option.getId(), 0);
        }
        pollResults.put(pollId, results);
        
        LOGGER.info("Poll created successfully: " + pollId);
        return pollId;
    }
    
    /**
     * Validates the session token and returns the associated username
     * 
     * @param sessionToken the session token to validate
     * @return the username associated with the session token
     * @throws SecurityException if the session token is invalid or expired
     */
    private String validateSession(String sessionToken) throws SecurityException {
        if (sessionToken == null || !activeSessions.containsKey(sessionToken)) {
            LOGGER.warning("Invalid session token: " + sessionToken);
            throw new SecurityException("Invalid session token");
        }
        
        SessionInfo sessionInfo = activeSessions.get(sessionToken);
        long currentTime = System.currentTimeMillis();
        
        if (currentTime - sessionInfo.getLastAccessTime() > SESSION_TIMEOUT) {
            activeSessions.remove(sessionToken);
            LOGGER.warning("Expired session token: " + sessionToken);
            throw new SecurityException("Session has expired, please login again");
        }
        
        return sessionInfo.getUsername();
    }
    
    /**
     * Inner class to store session information
     */
    private static class SessionInfo {
        private final String username;
        private long lastAccessTime;
        
        public SessionInfo(String username) {
            this.username = username;
            this.lastAccessTime = System.currentTimeMillis();
        }
        
        public String getUsername() {
            return username;
        }
        
        public long getLastAccessTime() {
            return lastAccessTime;
        }
        
        public void updateLastAccessTime() {
            this.lastAccessTime = System.currentTimeMillis();
        }
    }
}
