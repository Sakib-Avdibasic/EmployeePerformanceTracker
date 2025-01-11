package com.sakibavdibasicipia.example.view;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.sakibavdibasicipia.example.config.DatabaseConfig;
import com.sakibavdibasicipia.example.controller.EmployeeController;
import com.sakibavdibasicipia.example.model.Employee;
import com.sakibavdibasicipia.example.util.TableUtils;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class CEO {
    private JTable table1;
    private JLabel managerLabel;
    private JScrollPane departmentsPane;
    private JPanel panel1;
    private JComboBox comboBox1;
    private JLabel totalEarningsLabel;
    private JButton odjavaButton;
    private JButton uporediSaOstalimOdjelimaButton;
    private JLabel departmentLabel;
    private DefaultTableModel tableModel;
    private Map<String, DepartmentCache> departmentCache = new HashMap<>();

    public CEO() {
        tableModel = new DefaultTableModel();
        table1.setModel(tableModel);
        loadDepartments();
        JFrame frame = new JFrame("Generalni menadžer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.add(panel1);
        frame.setVisible(true);

        comboBox1.addItemListener(e -> handleComboBoxChange(e));

        odjavaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
            }
        });
        uporediSaOstalimOdjelimaButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DepartmentEarningsChart.displayBarChart(DatabaseConfig.getDatabase());
            }
        });
    }

    private void loadDepartments() {
        List<String> departments = fetchDepartments();

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
        String[] colors = {
                "E74C3C",
                "F39C12",
                "16A085",
                "2980B9",
                "8E44AD",
                "FF5733",
                "F1C40F",
                "2ECC71",
                "D35400",
                "9B59B6"
        };
        for (int i = 0; i <= departments.size(); i++) {
            JButton button;
            if (i == departments.size()) {
                button = new JButton("Dodaj novi odjel");
                button.setForeground(Color.BLACK);
                button.setBackground(Color.decode("#F4F6FC"));
                button.addActionListener(e -> {
                    JDialog dialog = new JDialog();
                    dialog.setTitle("Podaci o novom odjelu");
                    dialog.setLocationRelativeTo(null);

                    JPanel panel = new JPanel();
                    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

                    JTextField departmentNameField = new JTextField();
                    JComboBox<ComboItem> managerComboBox = new JComboBox<>();
                    JButton saveButton = new JButton("Dodaj");

                    List<ComboItem> employees = new ArrayList<>();
                    EmployeeController employeeController = new EmployeeController();
                    List<Employee> emps = employeeController.getAllEmployees();
                    for (Employee emp : emps) {
                        employees.add(new ComboItem(emp.getFirstName() + " " + emp.getLastName(), emp.getId().toString()));
                    }
                    for (ComboItem employee : employees) {
                        managerComboBox.addItem(employee);
                    }

                    panel.add(new JLabel("Naziv odjela"));
                    panel.add(departmentNameField);
                    panel.add(new JLabel("Menadžer"));
                    panel.add(managerComboBox);
                    panel.add(saveButton);

                    dialog.add(panel);
                    dialog.pack();

                    saveButton.addActionListener(a -> {
                        String departmentName = departmentNameField.getText();
                        ComboItem selectedManager = (ComboItem) managerComboBox.getSelectedItem();
                        if (!departmentName.isEmpty() && selectedManager != null) {
                            MongoCollection<Document> collection = DatabaseConfig.getDatabase().getCollection("departments");

                            Document newDepartment = new Document("name", departmentName);
                            collection.insertOne(newDepartment);

                            Document insertedDepartment = collection.find(new Document("name", departmentName)).first();
                            if (insertedDepartment != null) {
                                ObjectId departmentId = insertedDepartment.getObjectId("_id");
                                ObjectId managerRoleId = fetchRoleId("Manager");

                                MongoCollection<Document> usersCollection = DatabaseConfig.getDatabase().getCollection("users");
                                usersCollection.updateOne(
                                        new Document("_id", new ObjectId(selectedManager.getValue())),
                                        new Document("$set", new Document("department_id", departmentId).append("role_id", managerRoleId))
                                );
                            }
                            dialog.dispose();
                            loadDepartments();
                        } else {
                            JOptionPane.showMessageDialog(dialog, "Molimo popunite sva polja.");
                        }
                    });

                    dialog.setModal(true);
                    dialog.setVisible(true);
                });
            } else {
                final String departmentName = departments.get(i);
                button = new JButton(departments.get(i));
                button.addActionListener(e -> {
                    updateTableAndManager(departmentName);
                });
                button.setForeground(Color.WHITE);
                button.setBackground(Color.decode("#" + colors[i]));
            }
            button.setPreferredSize(new Dimension(160, 40));
            button.setFocusPainted(false);
            button.setFont(new Font("Arial", Font.PLAIN, 14));
            buttonPanel.add(button);
        }

        departmentsPane.setViewportView(buttonPanel);
        departmentsPane.setPreferredSize(new Dimension(departmentsPane.getPreferredSize().width, buttonPanel.getPreferredSize().height));
        departmentsPane.setBorder(null);
        if (!departments.isEmpty()) {
            updateTableAndManager(departments.get(0));
        }
    }

    private List<String> fetchDepartments() {
        List<String> departments = new ArrayList<>();
        MongoCollection<Document> collection = DatabaseConfig.getDatabase().getCollection("departments");
        for (Document doc : collection.find()) {
            departments.add(doc.getString("name"));
        }
        return departments;
    }

    private void updateTableAndManager(String departmentName) {
        for (ItemListener listener : comboBox1.getItemListeners()) {
            comboBox1.removeItemListener(listener);
        }

        DepartmentCache cache = departmentCache.get(departmentName);
        if (cache != null) {
            departmentLabel.setText(cache.departmentName);
            managerLabel.setText("\uD83D\uDC68\u200D Glavni urednik: " + cache.managerName);
            totalEarningsLabel.setText("\uD83D\uDCB0 Ukupna zarada tima: " + cache.totalEarnings + "BAM");
            TableUtils.refreshEmployeeTable(tableModel, comboBox1, cache.employees);
            comboBox1.addItemListener(e -> handleComboBoxChange(e));
            return;
        }

        MongoCollection<Document> departmentsCollection = DatabaseConfig.getDatabase().getCollection("departments");
        MongoCollection<Document> usersCollection = DatabaseConfig.getDatabase().getCollection("users");
        MongoCollection<Document> salariesCollection = DatabaseConfig.getDatabase().getCollection("salaries");

        Document departmentDoc = departmentsCollection.find(new Document("name", departmentName)).first();
        if (departmentDoc == null) return;

        ObjectId departmentId = departmentDoc.getObjectId("_id");
        departmentLabel.setText(departmentDoc.getString("name"));
        departmentLabel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK), BorderFactory.createEmptyBorder(0, 0, 6, 0)));

        ObjectId managerRoleId = fetchRoleId("Manager");
        Document managerDoc = usersCollection.find(
                new Document("department_id", departmentId)
                        .append("role_id", managerRoleId)
        ).first();

        List<Employee> employees = fetchEmployeesByDepartment(departmentId);

        TableUtils.refreshEmployeeTable(tableModel, comboBox1, employees);

        String managerName = (managerDoc != null)
                ? managerDoc.getString("name") + " " + managerDoc.getString("last_name")
                : "Odjel nema glavnog urednika";
        managerLabel.setText("\uD83D\uDC68\u200D Glavni urednik: " + managerName);

        LocalDate currentDate = LocalDate.now();
        LocalDate previousMonth = currentDate.minusMonths(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        String previousMonthStr = previousMonth.format(formatter);

        Document result = salariesCollection.aggregate(Arrays.asList(
                Aggregates.match(new Document("month", previousMonthStr)),
                Aggregates.lookup("users", "employee_id", "_id", "user_details"),
                Aggregates.unwind("$user_details"),
                Aggregates.match(new Document("user_details.department_id", departmentId)),
                Aggregates.group(null, Accumulators.sum("total_salary", "$salary"))
        )).first();

        Integer totalEarnings = result.getInteger("total_salary");

        comboBox1.addItemListener(e -> handleComboBoxChange(e));
        totalEarningsLabel.setText("\uD83D\uDCB0 Ukupna zarada tima: " + totalEarnings.toString() + "BAM");

        departmentCache.put(departmentName, new DepartmentCache(departmentName, employees, managerName, totalEarnings));
    }


    private List<Employee> fetchEmployeesByDepartment(ObjectId departmentId) {
        EmployeeController ec = new EmployeeController();
        return ec.getEmployeesByDepartment(departmentId.toHexString());
    }

    private ObjectId fetchRoleId(String roleName) {
        MongoCollection<Document> rolesCollection = DatabaseConfig.getDatabase().getCollection("roles");
        Document roleDoc = rolesCollection.find(new Document("name", roleName)).first();
        return roleDoc != null ? roleDoc.getObjectId("_id") : null;
    }

    private void handleComboBoxChange(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            ComboItem selectedItem = (ComboItem) comboBox1.getSelectedItem();
            if (selectedItem == null) return;

            String employeeName = selectedItem.getText();
            String employeeId = selectedItem.getValue();

            int result = JOptionPane.showConfirmDialog(
                    null,
                    "Želite li poslati uposleniku '" + employeeName + "' ponudu za unaprijeđenje?",
                    "Slanje ponude za unaprijeđenje",
                    JOptionPane.YES_NO_OPTION
            );

            if (result == JOptionPane.YES_OPTION) {
                MongoCollection<Document> promotionsCollection = DatabaseConfig.getDatabase().getCollection("promotions");
                Document promotionDoc = new Document("employee_id", new ObjectId(employeeId))
                        .append("date", LocalDate.now())
                        .append("status", false);
                promotionsCollection.insertOne(promotionDoc);
                JOptionPane.showMessageDialog(null, "Ponuda poslana uposleniku.");
            }
        }
    }

    private static class DepartmentCache {
        String departmentName;
        List<Employee> employees;
        String managerName;
        Integer totalEarnings;

        DepartmentCache(String departmentName, List<Employee> employees, String managerName, Integer totalEarnings) {
            this.departmentName = departmentName;
            this.employees = employees;
            this.managerName = managerName;
            this.totalEarnings = totalEarnings;
        }
    }

}
