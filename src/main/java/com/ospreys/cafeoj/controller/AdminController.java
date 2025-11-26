package com.ospreys.cafeoj.controller;

import com.ospreys.cafeoj.model.Problem;
import com.ospreys.cafeoj.model.TestCase;
import com.ospreys.cafeoj.repository.ProblemRepository;
import com.ospreys.cafeoj.repository.TestCaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
public class AdminController {

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private TestCaseRepository testCaseRepository;

    @GetMapping("/admin")
    public String dashboard() {
        return "admin-dashboard";
    }

    @GetMapping("/admin/problem/new")
    public String newProblemForm(Model model) {
        return "add-problem";
    }

    @PostMapping("/admin/problem/save")
    public String saveProblem(
            @RequestParam String title,
            @RequestParam("descriptionFile") MultipartFile descriptionFile,
            @RequestParam("sampleInput") MultipartFile sampleInput,
            @RequestParam("sampleOutput") MultipartFile sampleOutput,
            @RequestParam(value = "hiddenInputs", required = false) List<MultipartFile> hiddenInputs,
            @RequestParam(value = "hiddenOutputs", required = false) List<MultipartFile> hiddenOutputs
    ) throws IOException {
        // Read Description from file
        String description = new String(descriptionFile.getBytes(), StandardCharsets.UTF_8);

        // Save Problem
        Problem problem = new Problem(title, description);
        problemRepository.save(problem);

        // Save Sample Test Case
        String sampleIn = new String(sampleInput.getBytes(), StandardCharsets.UTF_8);
        String sampleOut = new String(sampleOutput.getBytes(), StandardCharsets.UTF_8);
        TestCase sampleCase = new TestCase(problem, sampleIn, sampleOut, true);
        testCaseRepository.save(sampleCase);

        // Save Hidden Test Cases
        if (hiddenInputs != null && hiddenOutputs != null && hiddenInputs.size() == hiddenOutputs.size()) {
            for (int i = 0; i < hiddenInputs.size(); i++) {
                String input = new String(hiddenInputs.get(i).getBytes(), StandardCharsets.UTF_8);
                String output = new String(hiddenOutputs.get(i).getBytes(), StandardCharsets.UTF_8);
                if (!input.isEmpty()) {
                    TestCase hiddenCase = new TestCase(problem, input, output, false);
                    testCaseRepository.save(hiddenCase);
                }
            }
        }

        return "redirect:/admin";
    }
}
