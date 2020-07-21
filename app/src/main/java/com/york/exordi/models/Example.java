package com.york.exordi.models;

import java.util.List;

public class Example {

    private String name;
    private String surname;
    private List<String> favoriteColors;
    private Occupation occupation;
}

class Occupation {
    private String job;
    private String student;
    private String mother;

    public Occupation(String job, String student, String mother) {
        this.job = job;
        this.student = student;
        this.mother = mother;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public String getStudent() {
        return student;
    }

    public void setStudent(String student) {
        this.student = student;
    }

    public String getMother() {
        return mother;
    }

    public void setMother(String mother) {
        this.mother = mother;
    }
}
