import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

public class BankManagementSystem {
    private static JTextField usernameField;
    private static JPasswordField passwordField;

    public static void main(String[] args) {
        JFrame loginFrame = new JFrame("Bank Login");
        loginFrame.setSize(350, 250);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setLayout(null);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(30, 30, 100, 25);
        loginFrame.add(usernameLabel);

        usernameField = new JTextField();
        usernameField.setBounds(120, 30, 150, 25);
        loginFrame.add(usernameField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(30, 70, 100, 25);
        loginFrame.add(passwordLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(120, 70, 150, 25);
        loginFrame.add(passwordField);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(120, 110, 80, 25);
        loginFrame.add(loginButton);

        JButton createAccountButton = new JButton("Create Account");
        createAccountButton.setBounds(120, 150, 150, 25);
        loginFrame.add(createAccountButton);

        loginButton.addActionListener(e -> loginUser());

        createAccountButton.addActionListener(e -> openCreateAccountForm());

        loginFrame.setVisible(true);
    }

    private static void openCreateAccountForm() {
        JFrame createAccountFrame = new JFrame("Create Account");
        createAccountFrame.setSize(400, 300);
        createAccountFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        createAccountFrame.setLayout(null);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(30, 30, 100, 25);
        createAccountFrame.add(usernameLabel);

        JTextField newUsernameField = new JTextField();
        newUsernameField.setBounds(150, 30, 150, 25);
        createAccountFrame.add(newUsernameField);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(30, 70, 100, 25);
        createAccountFrame.add(passwordLabel);

        JPasswordField newPasswordField = new JPasswordField();
        newPasswordField.setBounds(150, 70, 150, 25);
        createAccountFrame.add(newPasswordField);

        JLabel initialDepositLabel = new JLabel("Initial Deposit:");
        initialDepositLabel.setBounds(30, 110, 100, 25);
        createAccountFrame.add(initialDepositLabel);

        JTextField depositField = new JTextField();
        depositField.setBounds(150, 110, 150, 25);
        createAccountFrame.add(depositField);

        JButton submitButton = new JButton("Create Account");
        submitButton.setBounds(150, 150, 150, 25);
        createAccountFrame.add(submitButton);

        submitButton.addActionListener(e -> {
            String newUsername = newUsernameField.getText();
            String newPassword = new String(newPasswordField.getPassword());
            double initialDeposit = Double.parseDouble(depositField.getText());

            createAccount(newUsername, newPassword, initialDeposit);
            createAccountFrame.dispose();
        });

        createAccountFrame.setVisible(true);
    }
    private static void createAccount(String username, String password, double initialDeposit) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Insert user into users table
            String userQuery = "INSERT INTO users (username, password) VALUES (?, ?)";
            PreparedStatement userStmt = conn.prepareStatement(userQuery, Statement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, username);
            userStmt.setString(2, password);
            userStmt.executeUpdate();

            // Retrieve the generated user_id
            ResultSet generatedKeys = userStmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                int userId = generatedKeys.getInt(1);

                // Generate a unique account number
                String accountNumber = generateAccountNumber(userId);

                // Insert account into accounts table
                String accountQuery = "INSERT INTO accounts (user_id, balance, account_number) VALUES (?, ?, ?)";
                PreparedStatement accountStmt = conn.prepareStatement(accountQuery);
                accountStmt.setInt(1, userId);
                accountStmt.setDouble(2, initialDeposit);
                accountStmt.setString(3, accountNumber);
                int rowsAffected = accountStmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(null, "Account created successfully! Your account number is: " + accountNumber);
                } else {
                    JOptionPane.showMessageDialog(null, "Failed to create account.");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Failed to retrieve user ID.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    private static String generateAccountNumber(int userId) {
        // Example: Combine user ID and current timestamp to generate a unique account number
        long timestamp = System.currentTimeMillis();
        return "AC" + userId + timestamp;
    }


    
    

    private static void loginUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(null, "Login Successful");
                int userId = rs.getInt("user_id");

                // Dispose the login frame
                SwingUtilities.getWindowAncestor(usernameField).dispose();

                // Show the dashboard
                showDashboard(userId);
            } else {
                JOptionPane.showMessageDialog(null, "Invalid Credentials");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Database Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }


    private static void showDashboard(int userId) {
        JFrame dashboardFrame = new JFrame("Dashboard");
        dashboardFrame.setSize(400, 300);
        dashboardFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dashboardFrame.setLayout(null);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT account_number FROM accounts WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String accountNumber = rs.getString("account_number");
                JLabel accountLabel = new JLabel("Account Number: " + accountNumber);
                accountLabel.setBounds(20, 20, 300, 25);
                dashboardFrame.add(accountLabel);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Database Error: " + ex.getMessage());
            ex.printStackTrace();
        }

        JLabel welcomeLabel = new JLabel("Welcome, User " + userId);
        welcomeLabel.setBounds(20, 60, 200, 25);
        dashboardFrame.add(welcomeLabel);

        JButton viewBalanceButton = new JButton("View Balance");
        viewBalanceButton.setBounds(50, 100, 150, 25);
        dashboardFrame.add(viewBalanceButton);

        JButton depositButton = new JButton("Deposit");
        depositButton.setBounds(50, 140, 150, 25);
        dashboardFrame.add(depositButton);

        JButton withdrawButton = new JButton("Withdraw");
        withdrawButton.setBounds(50, 180, 150, 25);
        dashboardFrame.add(withdrawButton);

        viewBalanceButton.addActionListener(e -> viewBalance(userId));
        depositButton.addActionListener(e -> deposit(userId));
        withdrawButton.addActionListener(e -> withdraw(userId));

        dashboardFrame.setVisible(true);
    }

    private static void viewBalance(int userId) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT balance FROM accounts WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                double balance = rs.getDouble("balance");
                JOptionPane.showMessageDialog(null, "Your balance is: " + balance);
            } else {
                JOptionPane.showMessageDialog(null, "Account not found.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Database Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static void deposit(int userId) {
        String amountStr = JOptionPane.showInputDialog("Enter amount to deposit:");
        double amount = Double.parseDouble(amountStr);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "UPDATE accounts SET balance = balance + ? WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setDouble(1, amount);
            stmt.setInt(2, userId);
            int rowsUpdated = stmt.executeUpdate();

            if (rowsUpdated > 0) {
                JOptionPane.showMessageDialog(null, "Deposit Successful");
            } else {
                JOptionPane.showMessageDialog(null, "Account not found.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Database Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static void withdraw(int userId) {
        String amountStr = JOptionPane.showInputDialog("Enter amount to withdraw:");
        double amount = Double.parseDouble(amountStr);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = "SELECT balance FROM accounts WHERE user_id = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                double balance = rs.getDouble("balance");
                if (balance >= amount) {
                    String updateQuery = "UPDATE accounts SET balance = balance - ? WHERE user_id = ?";
                    PreparedStatement updateStmt = conn.prepareStatement(updateQuery);
                    updateStmt.setDouble(1, amount);
                    updateStmt.setInt(2, userId);
                    updateStmt.executeUpdate();

                    JOptionPane.showMessageDialog(null, "Withdrawal Successful");
                } else {
                    JOptionPane.showMessageDialog(null, "Insufficient Balance");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Account not found.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(null, "Database Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
