package com.ospreys.cafeoj.controller;

import com.ospreys.cafeoj.model.Problem;
import com.ospreys.cafeoj.model.Submission;
import com.ospreys.cafeoj.model.TestCase;
import com.ospreys.cafeoj.model.User;
import com.ospreys.cafeoj.repository.ProblemRepository;
import com.ospreys.cafeoj.repository.SubmissionRepository;
import com.ospreys.cafeoj.repository.TestCaseRepository;
import com.ospreys.cafeoj.service.JudgeService;
import com.ospreys.cafeoj.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ProblemController {

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private TestCaseRepository testCaseRepository;

    @Autowired
    private JudgeService judgeService;

    @Autowired
    private UserService userService;

    @GetMapping("/problems")
    public String listProblems(Model model, Principal principal) {
        List<Problem> allProblems = problemRepository.findAll();
        List<ProblemStatusDTO> problemList = new ArrayList<>();

        Map<Long, String> problemStatusMap = new HashMap<>();

        if (principal != null) {
            String username = principal.getName();
            User user = userService.findByUsername(username).orElse(null);
            if (user != null) {
                List<Submission> userSubmissions = submissionRepository.findByUserId(user.getId());
                for (Submission s : userSubmissions) {
                    Long pid = s.getProblem().getId();
                    String currentStatus = problemStatusMap.getOrDefault(pid, "NOT_TRIED");
                    
                    if ("ACCEPTED".equals(s.getStatus())) {
                        problemStatusMap.put(pid, "SOLVED");
                    } else if (!"SOLVED".equals(currentStatus)) {
                        // If not already solved, mark as TRIED (unless it was already tried, doesn't matter)
                        problemStatusMap.put(pid, "TRIED");
                    }
                }
            }
        }

        for (Problem p : allProblems) {
            String status = problemStatusMap.getOrDefault(p.getId(), "NOT_TRIED");
            problemList.add(new ProblemStatusDTO(p, status));
        }

        model.addAttribute("problems", problemList);
        return "problems";
    }

    @GetMapping("/problem/{id}")
    public String viewProblem(@PathVariable Long id, Model model, Principal principal) {
        Problem problem = problemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid problem Id:" + id));
        List<TestCase> testCases = testCaseRepository.findByProblemId(id);
        
        // Find the sample test case (isExample = true)
        TestCase sampleCase = testCases.stream()
                .filter(TestCase::isExample)
                .findFirst()
                .orElse(null);

        Submission lastSubmission = null;
        if (principal != null) {
            User user = userService.findByUsername(principal.getName()).orElse(null);
            if (user != null) {
                // Ideally we should have a method to findTopByProblemAndUserOrderBySubmissionDateDesc
                // For now, let's just filter the list (inefficient but works for MVP)
                List<Submission> submissions = submissionRepository.findByUserId(user.getId());
                lastSubmission = submissions.stream()
                        .filter(s -> s.getProblem().getId().equals(id))
                        .max((s1, s2) -> s1.getSubmissionDate().compareTo(s2.getSubmissionDate()))
                        .orElse(null);
            }
        }

        model.addAttribute("problem", problem);
        model.addAttribute("sampleCase", sampleCase);
        model.addAttribute("lastSubmission", lastSubmission);
        return "problem-detail";
    }

    @PostMapping("/problem/{id}/submit")
    public String submitSolution(@PathVariable Long id, @RequestParam("solutionFile") MultipartFile solutionFile, Principal principal) throws IOException {
        if (principal == null) {
            return "redirect:/login";
        }

        Problem problem = problemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid problem Id:" + id));
        User user = userService.findByUsername(principal.getName()).orElseThrow(() -> new IllegalArgumentException("User not found"));

        String code = new String(solutionFile.getBytes(), StandardCharsets.UTF_8);

        // Create Submission
        Submission submission = new Submission(user, problem, "PENDING", code);
        submissionRepository.save(submission);

        // Trigger Async Judging
        judgeService.judge(submission);

        return "redirect:/problem/" + id; // Redirect back to problem page to see status
    }

    // Simple DTO for the view
    public static class ProblemStatusDTO {
        private Problem problem;
        private String status; // SOLVED, TRIED, NOT_TRIED

        public ProblemStatusDTO(Problem problem, String status) {
            this.problem = problem;
            this.status = status;
        }

        public Problem getProblem() { return problem; }
        public String getStatus() { return status; }
    }
}
