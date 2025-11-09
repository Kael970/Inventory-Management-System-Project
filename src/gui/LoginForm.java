package gui;

import services.AuthService;
import models.User;
import utils.SessionManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Login Form
 * GUI for user authentication
 */
public class LoginForm extends JFrame {
    private JTextField emailField;
    private JPasswordField passwordField;
    private JCheckBox rememberMeCheckbox;
    private JButton loginButton;
    private JLabel adminLinkLabel;
    private JLabel signInLabel;
    private AuthService authService;

    public LoginForm() {
        authService = new AuthService();
        initComponents();
    }

    private void initComponents() {
        setTitle("IMS - Inventory Management System");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel with white background
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackground(Color.WHITE);

        // Left panel with logo (blue background)
        JPanel leftPanel = new JPanel();
        leftPanel.setBounds(0, 0, 450, 600);
        leftPanel.setBackground(new Color(118, 156, 191)); // Blue color
        leftPanel.setLayout(null);

        // Logo label
        JLabel logoLabel = new JLabel("IMS", SwingConstants.CENTER);
        logoLabel.setFont(new Font("Arial", Font.BOLD, 120));
        logoLabel.setForeground(new Color(26, 54, 93)); // Dark blue
        logoLabel.setBounds(0, 200, 450, 150);
        leftPanel.add(logoLabel);

        mainPanel.add(leftPanel);

        // Right panel with login form
        JPanel rightPanel = new JPanel();
        rightPanel.setBounds(450, 0, 450, 600);
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setLayout(null);

        // IMS Icon and Title
        JLabel imsIconLabel = new JLabel("IMS");
        imsIconLabel.setFont(new Font("Arial", Font.BOLD, 18));
        imsIconLabel.setForeground(new Color(26, 54, 93));
        imsIconLabel.setBounds(80, 80, 50, 30);
        rightPanel.add(imsIconLabel);

        JLabel titleLabel = new JLabel("Inventory management system");
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        titleLabel.setForeground(Color.GRAY);
        titleLabel.setBounds(130, 85, 250, 20);
        rightPanel.add(titleLabel);

        // Email label and field
        JLabel emailLabel = new JLabel("Email Address");
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        emailLabel.setBounds(80, 170, 300, 20);
        rightPanel.add(emailLabel);

        emailField = new JTextField("johndoe@gmail.com");
        emailField.setBounds(80, 195, 290, 35);
        emailField.setFont(new Font("Arial", Font.PLAIN, 14));
        emailField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        rightPanel.add(emailField);

        // Password label and field
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        passwordLabel.setBounds(80, 245, 300, 20);
        rightPanel.add(passwordLabel);

        passwordField = new JPasswordField("password");
        passwordField.setBounds(80, 270, 290, 35);
        passwordField.setFont(new Font("Arial", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        rightPanel.add(passwordField);

        // Remember Me checkbox
        rememberMeCheckbox = new JCheckBox("Remember Me");
        rememberMeCheckbox.setBounds(80, 320, 150, 20);
        rememberMeCheckbox.setBackground(Color.WHITE);
        rememberMeCheckbox.setFont(new Font("Arial", Font.PLAIN, 12));
        rememberMeCheckbox.setSelected(true);
        rightPanel.add(rememberMeCheckbox);

        // Forgot Password link
        JLabel forgotPasswordLabel = new JLabel("Forgot Password?");
        forgotPasswordLabel.setBounds(260, 320, 120, 20);
        forgotPasswordLabel.setForeground(new Color(0, 123, 255));
        forgotPasswordLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        forgotPasswordLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rightPanel.add(forgotPasswordLabel);

        // Login button
        loginButton = new JButton("Login");
        loginButton.setBounds(80, 360, 290, 40);
        loginButton.setBackground(new Color(0, 123, 255));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.addActionListener(e -> handleLogin());
        rightPanel.add(loginButton);

        // Login as Admin label
        JLabel loginAsLabel = new JLabel("Login as ");
        loginAsLabel.setBounds(160, 420, 60, 20);
        loginAsLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        rightPanel.add(loginAsLabel);

        adminLinkLabel = new JLabel("Admin");
        adminLinkLabel.setBounds(220, 420, 50, 20);
        adminLinkLabel.setForeground(new Color(0, 123, 255));
        adminLinkLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        adminLinkLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        adminLinkLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                emailField.setText("admin");
                passwordField.setText("admin123");
            }
        });
        rightPanel.add(adminLinkLabel);

        // Don't have an account label
        JLabel noAccountLabel = new JLabel("Don't have an account? ");
        noAccountLabel.setBounds(130, 450, 150, 20);
        noAccountLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        rightPanel.add(noAccountLabel);

        signInLabel = new JLabel("Sign In");
        signInLabel.setBounds(275, 450, 60, 20);
        signInLabel.setForeground(new Color(0, 123, 255));
        signInLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        signInLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rightPanel.add(signInLabel);

        mainPanel.add(rightPanel);

        // Enter key to login
        passwordField.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    handleLogin();
                }
            }
        });

        add(mainPanel);
    }

    private void handleLogin() {
        String username = emailField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Please enter both username and password.",
                "Login Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        User user = authService.login(username, password);

        if (user != null) {
            SessionManager.setCurrentUser(user);
            JOptionPane.showMessageDialog(this,
                "Welcome, " + user.getFullName() + "!",
                "Login Successful",
                JOptionPane.INFORMATION_MESSAGE);

            // Open dashboard
            this.dispose();
            new DashboardForm().setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                "Invalid username or password.",
                "Login Failed",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            new LoginForm().setVisible(true);
        });
    }
}

