package com.devops.helloservice.service;

import com.devops.helloservice.model.ApiResponse;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
public class HelloService {
    public ApiResponse greet(String name) {
        return new ApiResponse(
            "Hello, " + name + "! Welcome to DevOps Pipeline.",
            "hello-service",
            Instant.now().toString()
        );
    }
}
