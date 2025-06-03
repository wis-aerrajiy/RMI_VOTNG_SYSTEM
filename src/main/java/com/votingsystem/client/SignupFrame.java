package com.votingsystem.client;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.votingsystem.common.PasswordUtils;

/**
 * Signup frame for the Voting System client
 */
public class SignupFrame extends JFrame {
    
    private static final Logger LOGGER = Logger.getLogger(SignupFrame.class.getName());
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JButton signupButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    
    private VotingClient client;
    private LoginFrame loginFrame;
    
    /**
     * Constructor for the SignupFrame
     * 
     * @param client the VotingClient instance
     * @param loginFrame the LoginFrame instance
     */
    public SignupFrame(VotingClient client, LoginFrame loginFrame) {
        super("Online Voting System - Sign Up");
        
        this.client = client;
        this.loginFrame = loginFrame;
        
        // Set up the UI
        initUI();
    }
    
    private void initUI() {
        // Set up the frame
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(320, 240);
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
        usernameField = new JTextField(12);
        usernameField.setPreferredSize(new Dimension(120, 25));
        usernameField.setMaximumSize(new Dimension(120, 25));
        userPanel.add(usernameLabel);
        userPanel.add(usernameField);
        formPanel.add(userPanel);
        
        // Password field
        JPanel passPanel = new JPanel();
        passPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));
        JLabel passwordLabel = new JLabel("Pass:");
        passwordField = new JPasswordField(12);
        passwordField.setPreferredSize(new Dimension(120, 25));
        passwordField.setMaximumSize(new Dimension(120, 25));
        passPanel.add(passwordLabel);
        passPanel.add(passwordField);
        formPanel.add(passPanel);
        
        // Confirm password field
        JPanel confirmPanel = new JPanel();
        confirmPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 2));
        JLabel confirmPasswordLabel = new JLabel("Confirm:");
        confirmPasswordField = new JPasswordField(12);
        confirmPasswordField.setPreferredSize(new Dimension(120, 25));
        confirmPasswordField.setMaximumSize(new Dimension(120, 25));
        confirmPanel.add(confirmPasswordLabel);
        confirmPanel.add(confirmPasswordField);
        formPanel.add(confirmPanel);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        
        signupButton = new JButton("Sign Up");
        signupButton.addActionListener(e -> signup());
        signupButton.setPreferredSize(new Dimension(100, 30));
        
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> cancel());
        cancelButton.setPreferredSize(new Dimension(100, 30));
        
        buttonPanel.add(signupButton);
        buttonPanel.add(cancelButton);
        
        formPanel.add(new JLabel()); // Empty label for spacing
        formPanel.add(buttonPanel);
        
        // Status label
        statusLabel = new JLabel("Please enter your information");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Add components to the main panel
        JLabel titleLabel = new JLabel("Create Account", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // Add the main panel to the frame
        add(mainPanel);
        
        // Set default button
        getRootPane().setDefaultButton(signupButton);
    }
    
    private void signup() {
        // Get the username and password
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        // Validate input
        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            statusLabel.setText("All fields are required");
            return;
        }
        
        if (!password.equals(confirmPassword)) {
            statusLabel.setText("Passwords do not match");
            return;
        }
        
        // Hash the password before sending it to the server
        String hashedPassword = PasswordUtils.hashPassword(password);
        if (hashedPassword == null) {
            statusLabel.setText("Error hashing password. Please try again.");
            signupButton.setEnabled(true);
            return;
        }
        
        // Disable the signup button
        signupButton.setEnabled(false);
        statusLabel.setText("Signing up...");
        
        // Signup in a background thread
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return client.signup(username, hashedPassword);
            }
            
            @Override
            protected void done() {
                try {
                    boolean signedUp = get();
                    
                    if (signedUp) {
                        JOptionPane.showMessageDialog(
                            SignupFrame.this,
                            "Account created successfully. You can now log in.",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        
                        // Close the signup frame and show the login frame
                        dispose();
                        loginFrame.setVisible(true);
                    } else {
                        statusLabel.setText("Failed to create account. Please try again.");
                        signupButton.setEnabled(true);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error during signup", e);
                    
                    String errorMessage = e.getCause() instanceof SecurityException ? 
                            e.getCause().getMessage() : "Error during signup";
                    
                    statusLabel.setText(errorMessage);
                    signupButton.setEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
    
    private void cancel() {
        // Close the signup frame and show the login frame
        dispose();
        loginFrame.setVisible(true);
    }
}
