package com.sakibavdibasicipia.example.view;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.model.Filters;
import com.sakibavdibasicipia.example.config.DatabaseConfig;
import com.sakibavdibasicipia.example.controller.EmployeeController;
import com.sakibavdibasicipia.example.model.Employee;
import com.sakibavdibasicipia.example.model.Salary;
import com.sakibavdibasicipia.example.util.TableUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseEvent;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class Manager {
    private JPanel panel1;
    private JLabel label1;
    private JLabel label2;
    private JLabel label3;
    private JTable table1;
    private JComboBox comboBox1;
    private JButton dodajNovogUposlenikaButton;
    private JButton odjavaButton;
    private DefaultTableModel tableModel;
    private boolean updatingComboBox = false;
    private ObjectId departmentId;

    public Manager(String managerId) {
        Document managerDoc = DatabaseConfig.getDatabase().getCollection("users").find(Filters.eq("_id", new ObjectId(managerId))).first();
        this.departmentId = managerDoc.getObjectId("department_id");
        JFrame frame = new JFrame("Menadžer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.add(panel1);
        frame.setVisible(true);
        dodajNovogUposlenikaButton.addActionListener(e -> openAddEmployeeDialog(managerId));

        tableModel = new DefaultTableModel();
        table1.setModel(tableModel);
        loadEmployeeData();

        comboBox1.addItemListener(e -> {
            if(updatingComboBox) return;
            if (e.getStateChange() == ItemEvent.SELECTED) {
                ComboItem selectedItem = (ComboItem) comboBox1.getSelectedItem();
                if (selectedItem != null) {
                    String employeeId = selectedItem.getValue();
                    Date terminationDate = new Date();

                    int response = JOptionPane.showConfirmDialog(
                            panel1,
                            "Da li ste sigurni da želite raskinuti ugovor sa " + selectedItem.getText() + "?",
                            "Raskini ugovor",
                            JOptionPane.YES_NO_OPTION
                    );
                    if (response == JOptionPane.YES_OPTION) {
                        updatingComboBox = true;
                        DatabaseConfig.getDatabase().getCollection("users")
                                .updateOne(
                                        Filters.eq("_id", new ObjectId(employeeId)),
                                        new Document("$set", new Document("termination_date", terminationDate))
                                );
                        comboBox1.removeItem(selectedItem);
                        loadEmployeeData();
                    }
                }
            }
        });

        table1.addMouseListener(new MouseInputAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table1.rowAtPoint(e.getPoint());
                    String name = (String) table1.getValueAt(row, 0);
                    String email = (String) table1.getValueAt(row, 2);

                    String employeeId = new EmployeeController().getEmployeeIdByDetails(name.substring(0, name.indexOf(" ")), name.substring(name.indexOf(" ") + 1), email);

                    if (employeeId != null) {
                        fetchAndDisplaySalaryChart(employeeId, name);
                    }
                }
            }
        });

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, -1);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        String lastMonth = sdf.format(calendar.getTime());

        AggregateIterable<Document> res = DatabaseConfig.getDatabase().getCollection("salaries").aggregate(List.of(
                new Document("$match", new Document("month", lastMonth)),
                new Document("$lookup", new Document("from", "users")
                        .append("localField", "employee_id")
                        .append("foreignField", "_id")
                        .append("as", "employee")),
                new Document("$unwind", "$employee"),
                new Document("$lookup", new Document("from", "departments")
                        .append("localField", "employee.department_id")
                        .append("foreignField", "_id")
                        .append("as", "department")),
                new Document("$unwind", "$department"),
                new Document("$match", new Document("employee.department_id", departmentId)),
                new Document("$sort", new Document("salary", -1)),
                new Document("$limit", 1),
                new Document("$project", new Document("_id", 0)
                        .append("full_name", new Document("$concat", List.of("$employee.name", " ", "$employee.last_name")))
                        .append("salary", 1))
        ));

        label1.setText(label1.getText() + " " + res.first().getString("full_name") + " (" + res.first().getInteger("salary") + "BAM)");

        calendar.add(Calendar.MONTH, -1);
        String monthBefore = sdf.format(calendar.getTime());

        AggregateIterable<Document> result = DatabaseConfig.getDatabase().getCollection("salaries").aggregate(Arrays.asList(
                new Document("$lookup", new Document()
                        .append("from", "users")
                        .append("localField", "employee_id")
                        .append("foreignField", "_id")
                        .append("as", "employee")),
                new Document("$unwind", "$employee"),
                new Document("$match", new Document()
                        .append("month", new Document("$in", Arrays.asList(monthBefore, lastMonth)))
                        .append("employee.department_id", departmentId)),
                new Document("$group", new Document()
                        .append("_id", "$employee_id")
                        .append("employee_full_name", new Document("$first",
                                new Document("$concat", Arrays.asList("$employee.name", " ", "$employee.last_name"))))
                        .append("november_salary", new Document("$sum",
                                new Document("$cond", Arrays.asList(
                                        new Document("$eq", Arrays.asList("$month", monthBefore)),
                                        "$salary",
                                        0
                                ))))
                        .append("december_salary", new Document("$sum",
                                new Document("$cond", Arrays.asList(
                                        new Document("$eq", Arrays.asList("$month", lastMonth)),
                                        "$salary",
                                        0
                                ))))),
                new Document("$project", new Document()
                        .append("employee_full_name", 1)
                        .append("salary_diff", new Document("$subtract", Arrays.asList("$december_salary", "$november_salary")))),
                new Document("$facet", new Document()
                        .append("biggest_increase", Arrays.asList(
                                new Document("$sort", new Document("salary_diff", -1)),
                                new Document("$limit", 1)
                        ))
                        .append("biggest_decrease", Arrays.asList(
                                new Document("$sort", new Document("salary_diff", 1)),
                                new Document("$limit", 1)
                        )))
        ));

        String biggestIncreaseName = "No data";
        String biggestDecreaseName = "No data";
        Integer biggestIncreaseValue = new Integer(0);
        Integer biggestDecreaseValue = new Integer(0);

        for (Document doc : result) {
            List<Document> biggestIncrease = (List<Document>) doc.get("biggest_increase");
            List<Document> biggestDecrease = (List<Document>) doc.get("biggest_decrease");

            if (!biggestIncrease.isEmpty()) {
                biggestIncreaseName = biggestIncrease.get(0).getString("employee_full_name");
                biggestIncreaseValue = biggestIncrease.get(0).getInteger("salary_diff", 0);
            }

            if (!biggestDecrease.isEmpty()) {
                biggestDecreaseName = biggestDecrease.get(0).getString("employee_full_name");
                biggestDecreaseValue = biggestDecrease.get(0).getInteger("salary_diff", 0);
            }
        }

        label2.setText(label2.getText() + " " + biggestIncreaseName + " (+" + biggestIncreaseValue.toString() + "BAM)");
        label3.setText(label3.getText() + " " + biggestDecreaseName + " (" + biggestDecreaseValue.toString() + "BAM)");


        odjavaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
    }

    private void loadEmployeeData() {
        List<Employee> employees = new EmployeeController().getEmployeesByDepartment(departmentId.toString());
        TableUtils.refreshEmployeeTable(tableModel, comboBox1, employees);
        updatingComboBox = false;
    }

    private void fetchAndDisplaySalaryChart(String employeeId, String title) {
        EmployeeController ec = new EmployeeController();
        List<Salary> salariesData = ec.getEmployeeSalaries(employeeId);

        String[] months = new String[salariesData.size()];
        double[] salaries = new double[salariesData.size()];

        for (int i = 0; i < salariesData.size(); i++) {
            months[i] = salariesData.get(i).getMonth();
            salaries[i] = salariesData.get(i).getSalary();
        }

        SalaryChart chart = new SalaryChart(title, months, salaries);
        chart.display();
    }

    private void openAddEmployeeDialog(String managerId) {
        JDialog addEmployeeDialog = new JDialog();
        addEmployeeDialog.setTitle("Podaci o novom uposleniku");
        addEmployeeDialog.setSize(400, 400);
        addEmployeeDialog.setLocationRelativeTo(null);

        JPanel dialogPanel = new JPanel();
        dialogPanel.setLayout(new GridLayout(8, 2));

        JTextField nameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JTextField dateOfBirthField = new JTextField();
        JTextField usernameField = new JTextField();
        usernameField.setEditable(false);
        JTextField emailField = new JTextField();
        emailField.setEditable(false);

        dialogPanel.add(new JLabel("Ime:"));
        dialogPanel.add(nameField);
        dialogPanel.add(new JLabel("Prezime:"));
        dialogPanel.add(lastNameField);
        dialogPanel.add(new JLabel("Datum rođenja ([dan].[mjesec].[godina]):"));
        dialogPanel.add(dateOfBirthField);
        dialogPanel.add(new JLabel("Korisničko ime:"));
        dialogPanel.add(usernameField);
        dialogPanel.add(new JLabel("Email:"));
        dialogPanel.add(emailField);

        nameField.addCaretListener(e -> updateUsernameAndEmail(nameField, lastNameField, usernameField, emailField));
        lastNameField.addCaretListener(e -> updateUsernameAndEmail(nameField, lastNameField, usernameField, emailField));

        ObjectId roleId = DatabaseConfig.getDatabase().getCollection("roles").find(Filters.eq("name", "Employee")).first().getObjectId("_id");

        JButton submitButton = new JButton("Dodaj");
        submitButton.addActionListener(e -> {
            String name = nameField.getText();
            String lastName = lastNameField.getText();
            String dateOfBirth = dateOfBirthField.getText();
            String username = usernameField.getText();
            String email = emailField.getText();

            if (!name.isEmpty() && !lastName.isEmpty() && !dateOfBirth.isEmpty() && !email.isEmpty() && !username.isEmpty()) {
                String password = generateMD5Hash(username.substring(username.indexOf(".")+1) + username.charAt(0));

                SimpleDateFormat sdf = new SimpleDateFormat("dd.M.yyyy");
                Date birthDate = null;
                try {
                    birthDate = sdf.parse(dateOfBirth);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(addEmployeeDialog, "Neispravan format datuma.");
                    return;
                }

                Date hireDate = new Date();

                Document newEmployee = new Document()
                        .append("name", name)
                        .append("last_name", lastName)
                        .append("username", username)
                        .append("email", email)
                        .append("password", password)
                        .append("date_of_birth", birthDate)
                        .append("hire_date", hireDate)
                        .append("department_id", departmentId)
                        .append("role_id", roleId);

                DatabaseConfig.getDatabase().getCollection("users").insertOne(newEmployee);
                updatingComboBox = true;
                loadEmployeeData();
                addEmployeeDialog.dispose();
            } else {
                JOptionPane.showMessageDialog(addEmployeeDialog, "Sva polja su obavezna.");
            }
        });

        dialogPanel.add(submitButton);

        addEmployeeDialog.add(dialogPanel);
        addEmployeeDialog.setModal(true);
        addEmployeeDialog.setVisible(true);
    }

    private void updateUsernameAndEmail(JTextField nameField, JTextField lastNameField, JTextField usernameField, JTextField emailField) {
        String name = nameField.getText().toLowerCase()
                .replaceAll("\\s", "")
                .replaceAll("š", "s")
                .replaceAll("č", "c")
                .replaceAll("ć", "c")
                .replaceAll("ž", "z")
                .replaceAll("đ", "dj");

        String lastName = lastNameField.getText().toLowerCase()
                .replaceAll("\\s", "")
                .replaceAll("š", "s")
                .replaceAll("č", "c")
                .replaceAll("ć", "c")
                .replaceAll("ž", "z")
                .replaceAll("đ", "dj");

        usernameField.setText(name + "." + lastName);
        emailField.setText(name + "." + lastName + "@example.com");
    }

    private String generateMD5Hash(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] digest = md.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
