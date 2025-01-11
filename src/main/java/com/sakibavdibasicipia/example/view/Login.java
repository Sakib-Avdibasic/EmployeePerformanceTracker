package com.sakibavdibasicipia.example.view;

import com.mongodb.client.MongoCollection;
import com.sakibavdibasicipia.example.config.DatabaseConfig;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Login {
    private JPanel panel1;
    private JTextField textField1;
    private JPasswordField passwordField1;
    private JButton button1;

    public Login() {
        JFrame frame = new JFrame("Prijava");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.add(panel1);
        frame.setVisible(true);
        DatabaseConfig.getDatabase().runCommand(new Document("ping", 1));

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });
    }

    private void login() {
        String username = textField1.getText();
        char[] passwordChars = passwordField1.getPassword();
        String password = new String(passwordChars);

        Document userDoc = DatabaseConfig.getDatabase().getCollection("users").find(new Document("username", username)).first();
        if (userDoc != null && checkPassword(password, userDoc.getString("password"))) {
            if (userDoc.containsKey("termination_date") && userDoc.getDate("termination_date") != null) {
                JOptionPane.showMessageDialog(panel1, "Vaš račun je deaktiviran zbog raskida ugovora.");
                textField1.setText("");
                passwordField1.setText("");
                return;
            }
            ObjectId roleId = userDoc.getObjectId("role_id");
            String userId = userDoc.getObjectId("_id").toHexString();

            MongoCollection<Document> rolesCollection = DatabaseConfig.getDatabase().getCollection("roles");
            Document roleDoc = rolesCollection.find(new Document("_id", roleId)).first();

            if (roleDoc != null) {
                String roleName = roleDoc.getString("name");

                if ("CEO".equals(roleName)) {
                    new CEO();
                } else if ("Manager".equals(roleName)) {
                    new Manager(userId);
                } else {
                    new Employee(userId);
                }
            } else {
                JOptionPane.showMessageDialog(panel1, "Role not found!");
            }
            textField1.setText("");
            passwordField1.setText("");
        } else {
            JOptionPane.showMessageDialog(panel1, "Pogrešno korisničko ime ili lozinka.");
        }
    }

    private boolean checkPassword(String inputPassword, String storedPasswordHash) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(inputPassword.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().equals(storedPasswordHash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 hashing algorithm not found", e);
        }
    }

}
