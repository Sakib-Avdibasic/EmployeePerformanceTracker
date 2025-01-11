package com.sakibavdibasicipia.example.model;

public class Salary {
    private String month;
    private int salary;

    public Salary(String month, int salary) {
        this.month = month;
        this.salary = salary;
    }

    public String getMonth() {
        return month;
    }

    public int getSalary() {
        return salary;
    }
}

