package ui;

import javax.swing.*;
import database.LoginUser;

public class LoginForm {
    private JFrame frame;

    public LoginForm() {
        frame = new JFrame("User Login");
        JPanel panel = new JPanel();
        frame.setSize(350, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        placeComponents(panel);
    }

    public void display() {
        frame.setVisible(true);  // Hiển thị form đăng nhập
    }

    private void placeComponents(JPanel panel) {
        panel.setLayout(null);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(10, 20, 80, 25);
        panel.add(userLabel);

        JTextField userText = new JTextField(20);
        userText.setBounds(100, 20, 165, 25);
        panel.add(userText);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(10, 50, 80, 25);
        panel.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(100, 50, 165, 25);
        panel.add(passwordText);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(180, 80, 80, 25);
        panel.add(loginButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(10, 80, 80, 25);
        panel.add(cancelButton);

        loginButton.addActionListener(e -> {
            String username = userText.getText();
            String password = new String(passwordText.getPassword());

            boolean success = LoginUser.validateLogin(username, password);
            if (success) {
                JOptionPane.showMessageDialog(null, "Login successful!");
                frame.dispose();
                new MainInterface(); // Mở giao diện chính
            } else {
                JOptionPane.showMessageDialog(null, "Login failed. Please check your username and password.");
            }
        });

        cancelButton.addActionListener(e -> {
            frame.dispose(); // Đóng form Login
            MainApp.main(null); // Quay lại MainApp
        });
    }
}
