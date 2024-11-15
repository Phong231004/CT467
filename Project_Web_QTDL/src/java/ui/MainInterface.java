package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;

public class MainInterface {
    public MainInterface() {
        // Create the main frame
        JFrame frame = new JFrame("Computer Store Management System");
        frame.setSize(500, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);

        // Create a main panel with a box layout
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(230, 230, 250));

        // Add a title label
        JLabel titleLabel = new JLabel("Welcome to the Computer Store Management System", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        // Add a welcome message
        JLabel welcomeLabel = new JLabel("Successfully Logged In!", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        // Create a panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);

        // Create a "Reports" button
        JButton historyButton = new JButton("View Reports");
        historyButton.setFont(new Font("Arial", Font.PLAIN, 14));
        historyButton.setFocusPainted(false);
        historyButton.setBackground(new Color(100, 149, 237));
        historyButton.setForeground(Color.WHITE);
        historyButton.setPreferredSize(new Dimension(150, 40));

        // Add action listener to "Reports" button
        historyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    // Open the URI for reports
                    Desktop.getDesktop().browse(new URI("http://localhost:9090/projectwebqtdl/quanly/ThongKe"));
                } catch (IOException | java.net.URISyntaxException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(frame, "Cannot open report!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Add components to button panel
        buttonPanel.add(historyButton);

        // Add components to main panel
        panel.add(titleLabel);
        panel.add(welcomeLabel);
        panel.add(buttonPanel);

        // Add the main panel to the frame
        frame.add(panel);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        // Run the main interface in the Event Dispatch Thread
        SwingUtilities.invokeLater(MainInterface::new);
    }
}
