package com.sakibavdibasicipia.example.view;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.sakibavdibasicipia.example.config.DatabaseConfig;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Employee {
    private JProgressBar progressBar1;
    private JPanel panel1;
    private JTextArea textArea1;
    private JTextField textField1;
    private JButton button1;
    private JLabel currentSalaryLabel;
    private JLabel previousSalaryLabel;
    private JButton personalInfoButton;
    private JLabel imeLabel;
    private JButton odjavaButton;
    private JScrollPane announcementsPane;
    private JFrame frame;

    private final MongoCollection<Document> salaryCollection;
    private final MongoCollection<Document> promotionsCollection;
    private final MongoCollection<Document> usersCollection;
    private final String employeeId;
    private boolean accepted;

    public Employee(String employeeId) {
        this.salaryCollection = DatabaseConfig.getDatabase().getCollection("salaries");
        this.promotionsCollection = DatabaseConfig.getDatabase().getCollection("promotions");
        this.usersCollection = DatabaseConfig.getDatabase().getCollection("users");
        this.employeeId = employeeId;

        loadSalaryData();
        loadEmployeeData();

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addArticle();
            }
        });

        checkForPromotion();

        personalInfoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openEditInfoDialog();
            }
        });

        frame = new JFrame("Uposlenik");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        textField1.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        textArea1.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        frame.add(panel1);
        frame.setVisible(true);
        odjavaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
    }

    private void loadSalaryData() {
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String previousMonth = LocalDate.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));

        Document currentMonthDoc = salaryCollection.find(Filters.and(Filters.eq("employee_id", new ObjectId(employeeId)), Filters.eq("month", currentMonth))).first();
        Document previousMonthDoc = salaryCollection.find(Filters.and(Filters.eq("employee_id", new ObjectId(employeeId)), Filters.eq("month", previousMonth))).first();

        int currentSalary = currentMonthDoc != null ? currentMonthDoc.getInteger("salary") : 0;
        int previousSalary = previousMonthDoc != null ? previousMonthDoc.getInteger("salary") : 0;

        currentSalaryLabel.setText(currentSalary + "BAM");
        previousSalaryLabel.setText(previousSalary + "BAM");

        updateProgressBar(currentSalary, previousSalary);
    }

    private void addArticle() {
        String title = textField1.getText().trim();
        String content = textArea1.getText().trim();

        if (title.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(panel1, "Članak mora imati naslov i sadržaj.", "Greška", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        Document currentMonthDoc = salaryCollection.find(
                Filters.and(Filters.eq("employee_id", new ObjectId(employeeId)), Filters.eq("month", currentMonth))
        ).first();

        if (currentMonthDoc == null) {
            Document newDoc = new Document("employee_id", new ObjectId(employeeId))
                    .append("month", currentMonth)
                    .append("articles_written", 1)
                    .append("salary", 50);
            salaryCollection.insertOne(newDoc);
        } else {
            int newArticlesWritten = currentMonthDoc.getInteger("articles_written") + 1;
            int newSalary = currentMonthDoc.getInteger("salary") + 50;

            salaryCollection.updateOne(
                    Filters.eq("_id", currentMonthDoc.getObjectId("_id")),
                    new Document("$set", new Document("articles_written", newArticlesWritten).append("salary", newSalary))
            );
        }

        loadSalaryData();
        textField1.setText("");
        textArea1.setText("");
    }

    private void updateProgressBar(int currentSalary, int previousSalary) {
        int progress = (previousSalary > 0) ? (currentSalary * 100) / previousSalary : 0;
        progressBar1.setValue(progress);
        progressBar1.setString(progress + "%");
        progressBar1.setStringPainted(true);

        if (progress <= 50) {
            int red = 255;
            int green = (int) (255 * (progress / 50.0));
            progressBar1.setForeground(new Color(red, green, 0));
        } else {
            int red = (int) (255 * (1 - (progress - 50) / 50.0));
            int green = 255;
            progressBar1.setForeground(new Color(red, green, 0));
        }
    }

    private void checkForPromotion() {
        Document promotionDoc = promotionsCollection.find(Filters.and(Filters.eq("employee_id", new ObjectId(employeeId)), Filters.eq("status", false))).first();

        if (promotionDoc != null) {
            JPanel btnPanel = new JPanel();
            btnPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
            JButton btn = new JButton("Ponuda za unaprijeđenje!");
            btn.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    MongoCollection<Document> usersCollection = DatabaseConfig.getDatabase().getCollection("users");
                    Document employeeDoc = usersCollection.find(new Document("_id", new ObjectId(employeeId))).first();

                    String employeeName = "";
                    if (employeeDoc != null) {
                        employeeName = employeeDoc.getString("name") + " " + employeeDoc.getString("last_name");
                    }
                    Date originalDate = promotionDoc.getDate("date");
                    SimpleDateFormat outputFormat = new SimpleDateFormat("dd.MM.yyyy");
                    String formattedDate = outputFormat.format(originalDate);
                    Contract c = new Contract(formattedDate, employeeName, Employee.this);
                }
            });
            btnPanel.add(btn);
            announcementsPane.setViewportView(btnPanel);
        }
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
        if (accepted) {
            promoteEmployee();
        } else {
            promotionsCollection.deleteOne(Filters.eq("employee_id", employeeId));
        }
    }

    private void promoteEmployee() {
        Document promotionDoc = promotionsCollection.find(Filters.and(Filters.eq("employee_id", new ObjectId(employeeId)), Filters.eq("status", false))).first();
        promotionsCollection.updateOne(
                Filters.eq("_id", promotionDoc.getObjectId("_id")),
                new Document("$set", new Document("status", true))
        );

        ObjectId managerRoleId = getManagerRoleId();
        if (managerRoleId != null) {
            usersCollection.updateOne(
                    Filters.eq("_id", new ObjectId(employeeId)),
                    new Document("$set", new Document("role_id", managerRoleId))
            );
        }
        frame.dispose();
        new Manager(employeeId);
    }

    private ObjectId getManagerRoleId() {
        MongoCollection<Document> rolesCollection = DatabaseConfig.getDatabase().getCollection("roles");
        Document roleDoc = rolesCollection.find(Filters.eq("name", "Manager")).first();
        return roleDoc != null ? roleDoc.getObjectId("_id") : null;
    }

    private void loadEmployeeData() {
        Document employeeDoc = usersCollection.find(new Document("_id", new ObjectId(employeeId))).first();
        if (employeeDoc != null) {
            String employeeName = employeeDoc.getString("name") + " " + employeeDoc.getString("last_name");
            imeLabel.setText(employeeName);
        }
    }

    private void openEditInfoDialog() {
        Document employeeDoc = usersCollection.find(new Document("_id", new ObjectId(employeeId))).first();
        if (employeeDoc != null) {
            String firstName = employeeDoc.getString("name");
            String lastName = employeeDoc.getString("last_name");
            Date birthDate = employeeDoc.getDate("date_of_birth");
            String birthDateFormatted = new SimpleDateFormat("dd.MM.yyyy").format(birthDate);
            String email = employeeDoc.getString("email");

            JTextField firstNameField = new JTextField(firstName);
            JTextField lastNameField = new JTextField(lastName);
            JTextField birthDateField = new JTextField(birthDateFormatted);

            JPanel dialogPanel = new JPanel();
            dialogPanel.setLayout(new GridLayout(6, 2));
            dialogPanel.add(new JLabel("Ime:"));
            dialogPanel.add(firstNameField);
            dialogPanel.add(new JLabel("Prezime:"));
            dialogPanel.add(lastNameField);
            dialogPanel.add(new JLabel("Datum rođenja:"));
            dialogPanel.add(birthDateField);

            int option = JOptionPane.showConfirmDialog(null, dialogPanel, "Edit Personal Information", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (option == JOptionPane.OK_OPTION) {
                String birthDateText = birthDateField.getText().trim();
                Date birthDateParsed = null;
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
                    birthDateParsed = dateFormat.parse(birthDateText);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Pogrešno unesen datum.");
                    return;
                }

                updateEmployeeInfo(firstNameField.getText(), lastNameField.getText(), birthDateParsed);
            }
        }
    }

    private void updateEmployeeInfo(String firstName, String lastName, Date birthDate) {
        Document updateDoc = new Document("$set", new Document("name", firstName)
                .append("last_name", lastName)
                .append("date_of_birth", birthDate));

        usersCollection.updateOne(Filters.eq("_id", new ObjectId(employeeId)), updateDoc);
        loadEmployeeData();
    }
}



