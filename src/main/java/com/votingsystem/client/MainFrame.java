package com.votingsystem.client;

import com.votingsystem.common.Poll;
import com.votingsystem.common.PollOption;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main frame for the Voting System client
 */
public class MainFrame extends JFrame {
    
    private static final Logger LOGGER = Logger.getLogger(MainFrame.class.getName());
    
    private VotingClient client;
    
    private JList<Poll> pollList;
    private DefaultListModel<Poll> pollListModel;
    private JPanel pollDetailsPanel;
    private JPanel resultsPanel;
    private JButton voteButton;
    private JButton refreshButton;
    private JButton logoutButton;
    private JButton createPollButton;
    private JLabel statusLabel;
    
    private Poll selectedPoll;
    private ButtonGroup optionGroup;
    private JRadioButton[] optionButtons;
    
    // Admin panel components
    private JPanel adminPanel;
    private boolean isAdmin;
    
    /**
     * Constructor for the MainFrame
     * 
     * @param client the VotingClient instance
     */
    public MainFrame(VotingClient client) {
        super("Online Voting System");
        
        this.client = client;
        
        // Check if the user is an admin
        this.isAdmin = client.isAdmin();
        
        // Set up the UI
        initUI();
        
        // Load the polls
        loadPolls();
    }
    
    private void initUI() {
        // Set up the frame
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Create the main panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Create the poll list panel
        JPanel pollListPanel = new JPanel(new BorderLayout(5, 5));
        pollListPanel.setBorder(BorderFactory.createTitledBorder("Available Polls"));
        
        pollListModel = new DefaultListModel<>();
        pollList = new JList<>(pollListModel);
        pollList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pollList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                selectedPoll = pollList.getSelectedValue();
                displayPollDetails();
            }
        });
        
        JScrollPane pollScrollPane = new JScrollPane(pollList);
        pollScrollPane.setPreferredSize(new Dimension(250, 0));
        
        pollListPanel.add(pollScrollPane, BorderLayout.CENTER);
        
        // Create the poll details panel
        pollDetailsPanel = new JPanel(new BorderLayout(10, 10));
        pollDetailsPanel.setBorder(BorderFactory.createTitledBorder("Poll Details"));
        
        // Create the results panel
        resultsPanel = new JPanel(new BorderLayout(10, 10));
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Results"));
        
        // Create the button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        
        voteButton = new JButton("Vote");
        voteButton.addActionListener(e -> vote());
        voteButton.setEnabled(false);
        
        refreshButton = new JButton("Refresh");
        refreshButton.addActionListener(e -> loadPolls());
        
        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        
        buttonPanel.add(voteButton);
        buttonPanel.add(refreshButton);
        buttonPanel.add(logoutButton);
        
        // Add create poll button if user is admin
        if (isAdmin) {
            createPollButton = new JButton("Create Poll");
            createPollButton.addActionListener(e -> showCreatePollDialog());
            buttonPanel.add(createPollButton);
        }
        
        // Create the status label
        statusLabel = new JLabel("Ready");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Add components to the main panel
        mainPanel.add(pollListPanel, BorderLayout.WEST);
        
        // Create admin panel if user is admin
        if (isAdmin) {
            adminPanel = new JPanel(new BorderLayout());
            adminPanel.setBorder(BorderFactory.createTitledBorder("Admin Panel"));
            JLabel adminLabel = new JLabel("You are logged in as an administrator");
            adminLabel.setHorizontalAlignment(SwingConstants.CENTER);
            adminLabel.setForeground(new Color(0, 128, 0));
            adminPanel.add(adminLabel, BorderLayout.CENTER);
        }
        
        // Add the components to the main panel
        mainPanel.add(pollDetailsPanel, BorderLayout.CENTER);
        mainPanel.add(resultsPanel, BorderLayout.EAST);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add status label and possibly admin panel to the north
        if (isAdmin) {
            JPanel northPanel = new JPanel(new BorderLayout());
            northPanel.add(statusLabel, BorderLayout.NORTH);
            northPanel.add(adminPanel, BorderLayout.CENTER);
            mainPanel.add(northPanel, BorderLayout.NORTH);
        } else {
            mainPanel.add(statusLabel, BorderLayout.NORTH);
        }
        
        // Add the main panel to the frame
        add(mainPanel);
    }
    
    private void loadPolls() {
        // Disable the buttons
        voteButton.setEnabled(false);
        refreshButton.setEnabled(false);
        statusLabel.setText("Loading polls...");
        
        // Clear the poll list
        pollListModel.clear();
        
        // Clear the details panel
        pollDetailsPanel.removeAll();
        pollDetailsPanel.revalidate();
        pollDetailsPanel.repaint();
        
        // Clear the results panel
        resultsPanel.removeAll();
        resultsPanel.revalidate();
        resultsPanel.repaint();
        
        // Load the polls in a background thread
        SwingWorker<List<Poll>, Void> worker = new SwingWorker<List<Poll>, Void>() {
            @Override
            protected List<Poll> doInBackground() throws Exception {
                return client.getAvailablePolls();
            }
            
            @Override
            protected void done() {
                try {
                    List<Poll> polls = get();
                    
                    if (polls != null) {
                        // Add the polls to the list
                        for (Poll poll : polls) {
                            pollListModel.addElement(poll);
                        }
                        
                        statusLabel.setText("Polls loaded successfully");
                    } else {
                        statusLabel.setText("Failed to load polls. Please try again.");
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error loading polls", e);
                    statusLabel.setText("Error loading polls: " + e.getMessage());
                } finally {
                    refreshButton.setEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
    
    private void displayPollDetails() {
        // Clear the details panel
        pollDetailsPanel.removeAll();
        
        if (selectedPoll == null) {
            // No poll selected
            JLabel noSelectionLabel = new JLabel("Select a poll to view details");
            noSelectionLabel.setHorizontalAlignment(SwingConstants.CENTER);
            pollDetailsPanel.add(noSelectionLabel, BorderLayout.CENTER);
            pollDetailsPanel.revalidate();
            pollDetailsPanel.repaint();
            return;
        }
        
        // Create a panel for the poll details
        JPanel detailsPanel = new JPanel(new BorderLayout(10, 10));
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add the poll header
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel(selectedPoll.getTitle());
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        JLabel descriptionLabel = new JLabel(selectedPoll.getDescription());
        
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(descriptionLabel, BorderLayout.CENTER);
        
        detailsPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Add the options
        JPanel optionsPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));
        
        List<PollOption> options = selectedPoll.getOptions();
        optionGroup = new ButtonGroup();
        optionButtons = new JRadioButton[options.size()];
        
        // Get the user's current vote for this poll
        int userVote = client.getUserVote(selectedPoll.getId());
        boolean hasVoted = (userVote != -1);
        
        // Create a label to show if user has already voted
        JLabel voteStatusLabel = new JLabel();
        if (hasVoted) {
            String optionText = "Unknown";
            for (PollOption option : options) {
                if (option.getId() == userVote) {
                    optionText = option.getText();
                    break;
                }
            }
            voteStatusLabel.setText("You have voted for: " + optionText + ". You can change your vote below.");
            voteStatusLabel.setForeground(new Color(0, 128, 0)); // Dark green
        } else {
            voteStatusLabel.setText("You have not voted in this poll yet.");
            voteStatusLabel.setForeground(Color.BLUE);
        }
        headerPanel.add(voteStatusLabel, BorderLayout.SOUTH);
        
        for (int i = 0; i < options.size(); i++) {
            PollOption option = options.get(i);
            JRadioButton optionButton = new JRadioButton(option.getText());
            optionButton.setActionCommand(String.valueOf(option.getId()));
            
            // Select the user's current vote if they have voted
            if (hasVoted && option.getId() == userVote) {
                optionButton.setSelected(true);
            }
            
            optionGroup.add(optionButton);
            optionsPanel.add(optionButton);
            optionButtons[i] = optionButton;
        }
        
        detailsPanel.add(optionsPanel, BorderLayout.CENTER);
        
        // Add the details panel to the main panel
        pollDetailsPanel.add(detailsPanel, BorderLayout.CENTER);
        pollDetailsPanel.revalidate();
        pollDetailsPanel.repaint();
        
        // Enable the vote button and update its text based on whether user has voted
        voteButton.setEnabled(true);
        voteButton.setText(hasVoted ? "Change Vote" : "Vote");
        
        // Load the results
        loadResults();
    }
    
    private void loadResults() {
        // Clear the results panel
        resultsPanel.removeAll();
        
        // Load the results in a background thread
        SwingWorker<Map<Integer, Integer>, Void> worker = new SwingWorker<Map<Integer, Integer>, Void>() {
            @Override
            protected Map<Integer, Integer> doInBackground() throws Exception {
                return client.getPollResults(selectedPoll.getId());
            }
            
            @Override
            protected void done() {
                try {
                    Map<Integer, Integer> results = get();
                    
                    if (results != null) {
                        // Create the results panel
                        JPanel resultsContentPanel = new JPanel(new BorderLayout(10, 10));
                        
                        // Create a panel for the results
                        JPanel resultsListPanel = new JPanel(new GridLayout(0, 1, 5, 5));
                        
                        // Calculate the total votes
                        int totalVotes = 0;
                        for (int votes : results.values()) {
                            totalVotes += votes;
                        }
                        
                        // Add the results
                        for (Map.Entry<Integer, Integer> entry : results.entrySet()) {
                            int optionId = entry.getKey();
                            int votes = entry.getValue();
                            
                            String optionName = client.getOptionNameById(selectedPoll, optionId);
                            double percentage = totalVotes > 0 ? (votes * 100.0) / totalVotes : 0;
                            
                            JPanel resultPanel = new JPanel(new BorderLayout(5, 5));
                            resultPanel.add(new JLabel(optionName), BorderLayout.WEST);
                            
                            JProgressBar progressBar = new JProgressBar(0, 100);
                            progressBar.setValue((int) percentage);
                            progressBar.setStringPainted(true);
                            progressBar.setString(String.format("%d votes (%.1f%%)", votes, percentage));
                            
                            resultPanel.add(progressBar, BorderLayout.CENTER);
                            
                            resultsListPanel.add(resultPanel);
                        }
                        
                        resultsContentPanel.add(resultsListPanel, BorderLayout.CENTER);
                        
                        // Add a label for the total votes
                        JLabel totalVotesLabel = new JLabel("Total votes: " + totalVotes);
                        totalVotesLabel.setHorizontalAlignment(SwingConstants.CENTER);
                        resultsContentPanel.add(totalVotesLabel, BorderLayout.SOUTH);
                        
                        // Add the results content panel to the results panel
                        resultsPanel.add(resultsContentPanel, BorderLayout.CENTER);
                        resultsPanel.revalidate();
                        resultsPanel.repaint();
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error loading results", e);
                    JLabel errorLabel = new JLabel("Error loading results: " + e.getMessage());
                    errorLabel.setHorizontalAlignment(SwingConstants.CENTER);
                    resultsPanel.add(errorLabel, BorderLayout.CENTER);
                    resultsPanel.revalidate();
                    resultsPanel.repaint();
                }
            }
        };
        
        worker.execute();
    }
    
    private void vote() {
        // Get the selected option
        ButtonModel selectedButton = optionGroup.getSelection();
        
        if (selectedButton == null) {
            statusLabel.setText("Please select an option");
            return;
        }
        
        // Get the option ID
        int optionId = Integer.parseInt(selectedButton.getActionCommand());
        
        // Get the user's current vote
        int currentVote = client.getUserVote(selectedPoll.getId());
        boolean isChangingVote = (currentVote != -1);
        
        // Disable the vote button
        voteButton.setEnabled(false);
        statusLabel.setText(isChangingVote ? "Changing vote..." : "Submitting vote...");
        
        // Submit the vote in a background thread
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return client.vote(selectedPoll.getId(), optionId);
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    
                    if (success) {
                        statusLabel.setText(isChangingVote ? "Vote changed successfully" : "Vote submitted successfully");
                        
                        // Update the vote button text
                        voteButton.setText("Change Vote");
                        voteButton.setEnabled(true);
                        
                        // Reload the poll details to show the updated vote status
                        displayPollDetails();
                    } else {
                        statusLabel.setText(isChangingVote ? "Failed to change vote" : "Failed to submit vote");
                        voteButton.setEnabled(true);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error " + (isChangingVote ? "changing" : "submitting") + " vote", e);
                    statusLabel.setText("Error: " + e.getMessage());
                    voteButton.setEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
    
    private void logout() {
        // Disable the logout button
        logoutButton.setEnabled(false);
        statusLabel.setText("Logging out...");
        
        // Logout in a background thread
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return client.logout();
            }
            
            @Override
            protected void done() {
                try {
                    boolean success = get();
                    
                    if (success) {
                        LOGGER.info("Logout successful");
                        dispose();
                        new LoginFrame("localhost");
                    } else {
                        LOGGER.warning("Logout failed");
                        statusLabel.setText("Logout failed. Please try again.");
                        logoutButton.setEnabled(true);
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error during logout", e);
                    statusLabel.setText("Error during logout: " + e.getMessage());
                    logoutButton.setEnabled(true);
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Show dialog for creating a new poll (admin only)
     */
    private void showCreatePollDialog() {
        if (!isAdmin) {
            statusLabel.setText("Only administrators can create polls");
            return;
        }
        
        // Create a dialog for poll creation
        JDialog createPollDialog = new JDialog(this, "Create New Poll", true);
        createPollDialog.setSize(500, 400);
        createPollDialog.setLocationRelativeTo(this);
        
        JPanel dialogPanel = new JPanel(new BorderLayout(10, 10));
        dialogPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Form panel for poll details
        JPanel formPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        
        // Title field
        JLabel titleLabel = new JLabel("Poll Title:");
        JTextField titleField = new JTextField(20);
        formPanel.add(titleLabel);
        formPanel.add(titleField);
        
        // Description field
        JLabel descLabel = new JLabel("Poll Description:");
        JTextField descField = new JTextField(20);
        formPanel.add(descLabel);
        formPanel.add(descField);
        
        // Options panel
        JPanel optionsPanel = new JPanel(new BorderLayout(5, 5));
        optionsPanel.setBorder(BorderFactory.createTitledBorder("Poll Options"));
        
        JPanel optionListPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        List<JTextField> optionFields = new ArrayList<>();
        
        // Add initial option fields
        for (int i = 0; i < 3; i++) {
            JTextField optionField = new JTextField(20);
            optionFields.add(optionField);
            optionListPanel.add(optionField);
        }
        
        JButton addOptionButton = new JButton("Add Option");
        addOptionButton.addActionListener(e -> {
            JTextField optionField = new JTextField(20);
            optionFields.add(optionField);
            optionListPanel.add(optionField);
            optionListPanel.revalidate();
            optionListPanel.repaint();
        });
        
        JScrollPane optionsScrollPane = new JScrollPane(optionListPanel);
        optionsScrollPane.setPreferredSize(new Dimension(300, 150));
        
        optionsPanel.add(optionsScrollPane, BorderLayout.CENTER);
        optionsPanel.add(addOptionButton, BorderLayout.SOUTH);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton createButton = new JButton("Create Poll");
        JButton cancelButton = new JButton("Cancel");
        
        createButton.addActionListener(e -> {
            // Validate input
            String title = titleField.getText().trim();
            String description = descField.getText().trim();
            
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(createPollDialog, "Poll title cannot be empty", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            List<String> options = new ArrayList<>();
            for (JTextField field : optionFields) {
                String option = field.getText().trim();
                if (!option.isEmpty()) {
                    options.add(option);
                }
            }
            
            if (options.size() < 2) {
                JOptionPane.showMessageDialog(createPollDialog, "At least 2 options are required", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Create the poll
            createPollDialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            int pollId = client.createPoll(title, description, options);
            createPollDialog.setCursor(Cursor.getDefaultCursor());
            
            if (pollId != -1) {
                JOptionPane.showMessageDialog(createPollDialog, "Poll created successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                createPollDialog.dispose();
                loadPolls(); // Reload polls to show the new one
            } else {
                JOptionPane.showMessageDialog(createPollDialog, "Failed to create poll", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> createPollDialog.dispose());
        
        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);
        
        // Add components to dialog
        dialogPanel.add(formPanel, BorderLayout.NORTH);
        dialogPanel.add(optionsPanel, BorderLayout.CENTER);
        dialogPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        createPollDialog.add(dialogPanel);
        createPollDialog.setVisible(true);
    }
}
