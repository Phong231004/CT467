package ui;

import javax.swing.*;

public class MainApp {
    public static void main(String[] args) {
        // Hiển thị thông báo chào mừng với thứ tự nút: Register, Login
        int option = JOptionPane.showOptionDialog(null,
                "Welcome to the Computer Store Management Application!",
                "Welcome",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                new String[]{"Register", "Login"}, // Đổi thứ tự nút
                "Register");

        if (option == 1) { // Người dùng chọn "Login"
            LoginForm loginForm = new LoginForm();
            loginForm.display();
        } else if (option == 0) { // Người dùng chọn "Register"
            RegisterForm registerForm = new RegisterForm();
            registerForm.display();
        }
    }
}
