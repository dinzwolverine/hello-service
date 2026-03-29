package com.devops.helloservice.controller;

import com.devops.helloservice.model.ApiResponse;
import com.devops.helloservice.service.HelloService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HelloController {

    private final HelloService helloService;

    public HelloController(HelloService helloService) {
        this.helloService = helloService;
    }

    @GetMapping("/hello")
    public ResponseEntity<ApiResponse> hello(
            @RequestParam(defaultValue = "World") String name) {
        return ResponseEntity.ok(helloService.greet(name));
    }
    @GetMapping("/health")
    public ResponseEntity<Map<String,String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "hello-service"));
    }

    @GetMapping("/version")
    public ResponseEntity<Map<String,String>> version() {
        return ResponseEntity.ok(Map.of(
            "version", "1.0.0",
            "build",   System.getenv().getOrDefault("BUILD_NUMBER", "local")
        ));
    }
}
