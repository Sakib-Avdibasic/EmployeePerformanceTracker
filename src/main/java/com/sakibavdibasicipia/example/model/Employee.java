package com.sakibavdibasicipia.example.model;

import org.bson.types.ObjectId;
import org.bson.Document;
import java.util.Date;

public class Employee {
    private ObjectId id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private Date hireDate;
    private Date dateOfBirth;

    public Employee(String firstName, String lastName, String username, String email, Date hireDate, Date dateOfBirth) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.hireDate = hireDate;
        this.dateOfBirth = dateOfBirth;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Date getHireDate() {
        return hireDate;
    }

    public void setHireDate(Date hireDate) {
        this.hireDate = hireDate;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public Document toDocument() {
        return new Document("name", firstName)
                .append("last_name", lastName)
                .append("username", username)
                .append("email", email)
                .append("hire_date", hireDate)
                .append("date_of_birth", dateOfBirth);
    }

    public static Employee fromDocument(Document document) {
        Employee employee = new Employee(
                document.getString("name"),
                document.getString("last_name"),
                document.getString("username"),
                document.getString("email"),
                document.getDate("hire_date"),
                document.getDate("date_of_birth")
        );
        employee.setId(document.getObjectId("_id"));
        return employee;
    }
}

