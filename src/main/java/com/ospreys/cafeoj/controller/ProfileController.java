package com.ospreys.cafeoj.controller;

import com.ospreys.cafeoj.model.User;
import com.ospreys.cafeoj.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.ospreys.cafeoj.repository.ProblemRepository problemRepository;

    @Autowired
    private com.ospreys.cafeoj.repository.SubmissionRepository submissionRepository;

    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {
        if (principal == null) {
            return "redirect:/login";
        }
        
        String username = principal.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        
        long totalProblems = problemRepository.count();
        long problemsSolved = submissionRepository.countDistinctProblemByUserIdAndStatus(user.getId(), "ACCEPTED");

        model.addAttribute("problemsSolved", problemsSolved);
        model.addAttribute("totalProblems", totalProblems);
        
        return "profile";
    }
}
