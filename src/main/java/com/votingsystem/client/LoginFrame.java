package com.votingsystem.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.votingsystem.common.PasswordUtils;

/**
 * Login frame for the Voting System client
 */
public class LoginFrame extends JFrame {
    
    private static final Logger LOGGER = Logger.getLogger(LoginFrame.class.getName());
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;
    
    private VotingClient client;
    
    /**
     * Constructor for the LoginFrame
     * 
     * @param serverHost the hostname of the RMI server
     */
    public LoginFrame(String serverHost) {
        super("Online Voting System - Login");
        
        // Initialize the client
        client = new VotingClient(serverHost);
        
        // Set up the UI
        initUI();
        
        // Connect to the server
        connectToServer();
    }
    
    private void initUI() {
        // Set up the frame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);
        setResizable(true);
        setLocationRelativeTo(null);
        
        // Create the main panel
        JPanel mainPanel = new JPanel(new BorderLayout(5, 5));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create the form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        
        // Username field
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));
        JLabel usernameLabel = new JLabel("User:");
        usernameField = new JTextField(12); // Medium text field
        usernameField.setPreferredSize(new Dimension(120, 25));
        usernameField.setMaximumSize(new Dimension(120, 25));
        userPanel.add(usernameLabel);
        userPanel.add(usernameField);
        formPanel.add(userPanel);
        
        // Password field
        JPanel passPanel = new JPanel();
        passPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));
        JLabel passwordLabel = new JLabel("Pass:");
        passwordField = new JPasswordField(12); // Medium text field
        passwordField.setPreferredSize(new Dimension(120, 25));
        passwordField.setMaximumSize(new Dimension(120, 25));
        passPanel.add(passwordLabel);
        passPanel.add(passwordField);
        formPanel.add(passPanel);
        
        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        
        loginButton = new JButton("Login");
        loginButton.addActionListener(e -> login());
        buttonPanel.add(loginButton);
        
        JButton signupButton = new JButton("Sign Up");
        signupButton.addActionListener(e -> openSignupFrame());
        buttonPanel.add(signupButton);
        
        // Add buttons to form panel
        formPanel.add(new JLabel()); // Empty label for spacing
        formPanel.add(buttonPanel);
        
        // Status label
        statusLabel = new JLabel("Please enter your credentials");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Add components to the main panel
        JLabel titleLabel = new JLabel("Voting System", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // Add the main panel to the frame
        add(mainPanel);
        
        // Set default button
        getRootPane().setDefaultButton(loginButton);
    }
    
    private void connectToServer() {
        // Disable the login button
        loginButton.setEnabled(false);
        statusLabel.setText("Connecting to server...");
        
        // Connect to the server in a background thread
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return client.connect();
            }
            
            @Override
            protected void done() {
                try {
                    boolean connected = get();
                    
                    if (connected) {
                        statusLabel.setText("Connected to server. Please login.");
                        loginButton.setEnabled(true);
                    } else {
                        statusLabel.setText("Failed to connect to server. Please restart the application.");
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error connecting to server", e);
                    statusLabel.setText("Error connecting to server: " + e.getMessage());
                }
            }
        };
        
        worker.execute();
    }
    
    private void login() {
        // Get the username and password
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        // Validate input
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Username and password cannot be empty");
            return;
        }
        
        // Hash the password before sending it to the server
        String hashedPassword = PasswordUtils.hashPassword(password);
        if (hashedPassword == null) {
            statusLabel.setText("Error hashing password. Please try again.");
            loginButton.setEnabled(true);
            return;
        }
        
        // Disable the login button
        loginButton.setEnabled(false);
        statusLabel.setText("Logging in...");
        
        // Login in a background thread
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return client.login(username, hashedPassword);
            }
            
            @Override
            protected void done() {
                try {
                    boolean loggedIn = get();
                    
                    if (loggedIn) {
                        // Open the main frame
                        openMainFrame();
                    } else {
                        statusLabel.setText("Login failed. Please check your credentials.");
                        loginButton.setEnabled(true);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error during login", e);
                    statusLabel.setText("Error during login: " + e.getMessage());
                    loginButton.setEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
    
    private void openMainFrame() {
        // Create and show the main frame
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame(client);
            mainFrame.setVisible(true);
            
            // Add a window listener to handle logout on close
            mainFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    // Reset the login form
                    usernameField.setText("");
                    passwordField.setText("");
                    statusLabel.setText("Please enter your credentials");
                    loginButton.setEnabled(true);
                    
                    // Show the login frame again
                    setVisible(true);
                }
            });
            
            // Hide the login frame
            setVisible(false);
        });
    }
    
    private void openSignupFrame() {
        // Create and show the signup frame
        SwingUtilities.invokeLater(() -> {
            SignupFrame signupFrame = new SignupFrame(client, this);
            signupFrame.setVisible(true);
            
            // Hide the login frame
            setVisible(false);
        });
    }
    
    /**
     * Main method to start the application
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // Set the look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to set system look and feel", e);
        }
        
        // Get the server host from command line arguments or use localhost
        final String serverHost = args.length > 0 ? args[0] : "localhost";
        
        // Create and show the login frame
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame(serverHost);
            loginFrame.setVisible(true);
        });
    }
}
