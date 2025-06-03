package com.votingsystem.server;

import com.votingsystem.common.VotingService;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class for the Voting Server
 */
public class VotingServer {
    
    private static final Logger LOGGER = Logger.getLogger(VotingServer.class.getName());
    private static final int RMI_PORT = 1099;
    private static final String SERVICE_NAME = "VotingService";
    
    public static void main(String[] args) {
        try {
            // Set security manager if needed
            if (System.getSecurityManager() == null) {
                System.setSecurityManager(new SecurityManager());
            }
            
            LOGGER.info("Starting RMI registry on port " + RMI_PORT);
            
            // Create and export the registry instance on the specified port
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);
            
            // Create the service implementation
            VotingService votingService = new VotingServiceImpl();
            
            // Bind the service to the registry
            registry.rebind(SERVICE_NAME, votingService);
            
            LOGGER.info("VotingService bound to registry");
            System.out.println("VotingServer is running...");
            System.out.println("Press Ctrl+C to stop the server");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "VotingServer exception:", e);
            System.err.println("VotingServer exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
