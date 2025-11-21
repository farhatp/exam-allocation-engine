package com.example.examallocation;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import jakarta.annotation.PostConstruct;

@SpringBootApplication
public class ExamAllocationApplication {

    @PostConstruct
    public void init(){
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"));
        System.out.println("Application Time Zone set to: "+TimeZone.getDefault());
    }

    public static void main(String[] args) {
        SpringApplication.run(ExamAllocationApplication.class, args);
    }
}
