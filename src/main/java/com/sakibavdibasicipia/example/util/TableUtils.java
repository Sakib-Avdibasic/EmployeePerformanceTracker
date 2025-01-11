package com.sakibavdibasicipia.example.util;

import com.sakibavdibasicipia.example.controller.EmployeeController;
import com.sakibavdibasicipia.example.model.Employee;
import com.sakibavdibasicipia.example.view.ComboItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class TableUtils {
    public static void refreshEmployeeTable(DefaultTableModel tableModel, JComboBox<ComboItem> comboBox, List<Employee> employees) {
        tableModel.setRowCount(0);
        tableModel.setColumnIdentifiers(new String[]{"Ime i prezime", "Datum zaposlenja", "Email", "Datum rođenja", "Posljednja plata"});
        comboBox.removeAllItems();

        EmployeeController ec = new EmployeeController();
        SimpleDateFormat sdf = new SimpleDateFormat("dd. MMMM yyyy.", new Locale("bs", "BA"));
        for (Employee employee : employees) {
            Object[] rowData = {
                    employee.getFirstName() + " " + employee.getLastName(),
                    sdf.format(employee.getHireDate()),
                    employee.getEmail(),
                    sdf.format(employee.getDateOfBirth()),
                    ec.getEmployeeLastSalary(employee.getId().toString())
            };
            tableModel.addRow(rowData);
            comboBox.addItem(new ComboItem(employee.getFirstName() + " " + employee.getLastName(), employee.getId().toString()));
        }
    }
}
