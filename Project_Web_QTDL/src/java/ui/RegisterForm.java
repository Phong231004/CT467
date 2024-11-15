package ui;

import javax.swing.*;
import database.RegisterUser;

public class RegisterForm {
    private JFrame frame;

    public RegisterForm() {
        frame = new JFrame("User Registration");
        JPanel panel = new JPanel();
        frame.setSize(350, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(panel);
        placeComponents(panel);
    }

    public void display() {
        frame.setVisible(true);  // Hiển thị form đăng ký
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

        JLabel confirmLabel = new JLabel("Confirm:");
        confirmLabel.setBounds(10, 80, 80, 25);
        panel.add(confirmLabel);

        JPasswordField confirmPasswordText = new JPasswordField(20);
        confirmPasswordText.setBounds(100, 80, 165, 25);
        panel.add(confirmPasswordText);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setBounds(10, 110, 80, 25);
        panel.add(emailLabel);

        JTextField emailText = new JTextField(20);
        emailText.setBounds(100, 110, 165, 25);
        panel.add(emailText);

        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setBounds(10, 140, 80, 25);
        panel.add(phoneLabel);

        JTextField phoneText = new JTextField(20);
        phoneText.setBounds(100, 140, 165, 25);
        panel.add(phoneText);

        JButton registerButton = new JButton("Register");
        registerButton.setBounds(210, 180, 100, 25);
        panel.add(registerButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBounds(100, 180, 100, 25);
        panel.add(cancelButton);

        registerButton.addActionListener(e -> {
            String username = userText.getText();
            String password = new String(passwordText.getPassword());
            String confirmPassword = new String(confirmPasswordText.getPassword());
            String email = emailText.getText();
            String phone = phoneText.getText();

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(null, "Passwords do not match!");
                return;
            }

            boolean success = RegisterUser.register(username, password, email, phone);
            if (success) {
                JOptionPane.showMessageDialog(null, "Registration successful!");
                frame.dispose(); // Đóng form đăng ký
                new LoginForm().display(); // Quay lại form đăng nhập
            } else {
                JOptionPane.showMessageDialog(null, "Registration failed!");
            }
        });

        // Thêm sự kiện cho nút "Cancel" để quay lại MainApp
        cancelButton.addActionListener(e -> {
            frame.dispose(); // Đóng form đăng ký
            MainApp.main(null); // Quay lại màn hình MainApp
        });
    }
}
