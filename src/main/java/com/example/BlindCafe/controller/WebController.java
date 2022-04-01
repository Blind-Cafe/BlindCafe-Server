package com.example.BlindCafe.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class WebController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/policy/privacy")
    public String privacy() {
        return "policy/privacy";
    }

    @GetMapping("/policy/usage")
    public String usage() {
        return "policy/usage";
    }

    @GetMapping("/chatting")
    public String chatting() {return "chatting"; }

    // API Docs
    @GetMapping("/docs")
    public String docs() { return "docs/index"; }
}
