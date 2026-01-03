package com.salon.ui;

import com.salon.dao.UserDAO;
import com.salon.model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class LoginFrame extends JFrame {
    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public LoginFrame() {
        setTitle("Salon Management - Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 2, 10, 10));

        JLabel lblUser = new JLabel("Username:");
        JLabel lblPass = new JLabel("Password:");
        txtUsername = new JTextField();
        txtPassword = new JPasswordField();
        JButton btnLogin = new JButton("Login");

        add(lblUser);
        add(txtUsername);
        add(lblPass);
        add(txtPassword);
        add(new JLabel());
        add(btnLogin);

        btnLogin.addActionListener(e -> login());

        setVisible(true);
    }

    private void login() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());

        UserDAO dao = new UserDAO();
        User user = dao.login(username, password);

        if (user != null) {
            JOptionPane.showMessageDialog(this, "Welcome, " + user.getFullName());
            dispose();
            if ("ADMIN".equals(user.getRole()))
                new AdminDashboard(user);
            else
                new StaffDashboard(user);
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials!");
        }
    }

    public static void main(String[] args) {
        new LoginFrame();
    }
}
