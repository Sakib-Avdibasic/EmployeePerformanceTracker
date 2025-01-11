package com.sakibavdibasicipia.example.controller;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.sakibavdibasicipia.example.config.DatabaseConfig;
import com.sakibavdibasicipia.example.model.Employee;
import com.sakibavdibasicipia.example.model.Salary;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EmployeeController {
    MongoDatabase db = DatabaseConfig.getDatabase();

    public List<Employee> getAllEmployees() {
        MongoCollection<Document> collection = db.getCollection("users");
        List<Employee> employees = new ArrayList<>();
        for (Document doc : collection.find(Filters.exists("termination_date", false))) {
            employees.add(Employee.fromDocument(doc));
        }
        return employees;
    }

    public List<Employee> getEmployeesByDepartment(String departmentId) {
        List<Employee> employees = new ArrayList<>();

        Document managerRoleDoc = db.getCollection("roles")
                .find(Filters.eq("name", "Manager"))
                .first();

        if (managerRoleDoc != null) {
            org.bson.types.ObjectId managerRoleId = managerRoleDoc.getObjectId("_id");

            db.getCollection("users")
                    .find(Filters.and(
                            Filters.eq("department_id", new org.bson.types.ObjectId(departmentId)),
                            Filters.ne("role_id", managerRoleId),
                            Filters.exists("termination_date", false)
                    ))
                    .forEach(document -> employees.add(Employee.fromDocument(document)));
        }

        return employees;
    }

    public String getEmployeeIdByDetails(String firstName, String lastName, String email) {
        Document query = new Document("name", firstName)
                .append("last_name", lastName)
                .append("email", email);

        Document employeeDoc = db.getCollection("users").find(query).first();
        if (employeeDoc != null) {
            return employeeDoc.getObjectId("_id").toString();
        }
        return null;
    }

    public int getEmployeeLastSalary(String employeeId) {
        MongoDatabase db = DatabaseConfig.getDatabase();
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String previousMonth = LocalDate.parse(currentMonth + "-01").minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));
        Document previousMonthDoc = db.getCollection("salaries").find(Filters.and(
                Filters.eq("employee_id", new ObjectId(employeeId)),
                Filters.eq("month", previousMonth)
        )).first();

        if (previousMonthDoc != null) {
            return previousMonthDoc.getInteger("salary", 0);
        }

        return 0;

    }

    public List<Salary> getEmployeeSalaries(String employeeId) {
        List<Salary> salaries = new ArrayList<>();
        String currentMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        String previousMonth = LocalDate.parse(currentMonth + "-01").minusMonths(1).format(DateTimeFormatter.ofPattern("yyyy-MM"));

        List<Document> results = db.getCollection("salaries")
                .find(Filters.and(
                        Filters.eq("employee_id", new ObjectId(employeeId)),
                        Filters.lt("month", currentMonth)
                ))
                .sort(new Document("month", 1))
                .into(new ArrayList<>());

        for (Document result : results) {
            String month = result.getString("month");
            int salary = result.getInteger("salary");
            salaries.add(new Salary(month, salary));
        }

        return salaries;
    }


}
