package com.printqueue.model;

/**
 * Represents a registered student user in the campus printing system.
 */
public class Student {
    private String studentId;
    private String name;
    private String username;
    private String password;

    public Student(String studentId, String name, String username, String password) {
        this.studentId = studentId;
        this.name = name;
        this.username = username;
        this.password = password;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "Student [ID=" + studentId + ", Name=" + name + ", Username=" + username + "]";
    }
}
