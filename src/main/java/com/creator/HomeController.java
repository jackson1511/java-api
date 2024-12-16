package com.creator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final UserRepository userRepository;

    @GetMapping()
    public ResponseEntity<?> home() {
        var newuser = new User();
        newuser.setName("test");
        userRepository.save(newuser);
        log.info("=== new user saved ===");
        var users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{user-id}")
    public ResponseEntity<?> getUser(@PathVariable("user-id") Long userId) {
        var user = userRepository.findById(userId);
        return ResponseEntity.ok(user);
    }

}
