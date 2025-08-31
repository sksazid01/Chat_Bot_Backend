package com.ChatBot.chatbot_service.controller;

import com.ChatBot.chatbot_service.entity.ChatHistory;
import com.ChatBot.chatbot_service.entity.User;
import com.ChatBot.chatbot_service.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("{username}")
    public List<ChatHistory> getUserMessages(@PathVariable String username) {
        return userService.getUserMessages(username);
    }
    @PostMapping("/create")
    public String createUser(@RequestBody User user) {
        return userService.createUser(user);
    }
}
