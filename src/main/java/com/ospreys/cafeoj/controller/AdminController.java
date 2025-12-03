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
import org.springframework.lang.NonNull;

@Controller
public class AdminController {

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private TestCaseRepository testCaseRepository;

    @GetMapping("/admin")
    public String dashboard(Model model) {
        List<Problem> problems = problemRepository.findAll();
        model.addAttribute("problems", problems);
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
            @RequestParam(defaultValue = "1.0") Double timeLimit,
            @RequestParam(defaultValue = "256") Integer memoryLimit,
            @RequestParam("sampleInput") MultipartFile sampleInput,
            @RequestParam("sampleOutput") MultipartFile sampleOutput,
            @RequestParam(value = "hiddenInputs", required = false) List<MultipartFile> hiddenInputs,
            @RequestParam(value = "hiddenOutputs", required = false) List<MultipartFile> hiddenOutputs
    ) throws IOException {
        // Read Description from file
        String description = new String(descriptionFile.getBytes(), StandardCharsets.UTF_8);

        // Save Problem
        Problem problem = new Problem(title, description, timeLimit, memoryLimit);
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
    @PostMapping("/admin/problem/{id}/delete")
    public String deleteProblem(@org.springframework.web.bind.annotation.PathVariable @NonNull Long id) {
        problemRepository.deleteById(id);
        return "redirect:/admin";
    }

    @GetMapping("/admin/problem/{id}/edit")
    public String editProblemForm(@org.springframework.web.bind.annotation.PathVariable @NonNull Long id, Model model) {
        Problem problem = problemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid problem Id:" + id));
        List<TestCase> testCases = testCaseRepository.findByProblemId(id);
        model.addAttribute("problem", problem);
        model.addAttribute("testCases", testCases);
        return "edit-problem";
    }

    @PostMapping("/admin/problem/{id}/edit")
    public String updateProblem(
            @org.springframework.web.bind.annotation.PathVariable @NonNull Long id,
            @RequestParam String title,
            @RequestParam(required = false) MultipartFile descriptionFile,
            @RequestParam Double timeLimit,
            @RequestParam Integer memoryLimit,
            @RequestParam(required = false) List<Long> deleteTestCaseIds,
            @RequestParam(value = "newInputs", required = false) List<MultipartFile> newInputs,
            @RequestParam(value = "newOutputs", required = false) List<MultipartFile> newOutputs
    ) throws IOException {
        Problem problem = problemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid problem Id:" + id));
        
        problem.setTitle(title);
        problem.setTimeLimit(timeLimit);
        problem.setMemoryLimit(memoryLimit);

        if (descriptionFile != null && !descriptionFile.isEmpty()) {
            String description = new String(descriptionFile.getBytes(), StandardCharsets.UTF_8);
            problem.setDescription(description);
        }

        problemRepository.save(problem);

        // Delete selected test cases (only if they belong to this problem)
        if (deleteTestCaseIds != null && !deleteTestCaseIds.isEmpty()) {
            List<TestCase> casesToDelete = testCaseRepository.findAllById(deleteTestCaseIds);
            for (TestCase tc : casesToDelete) {
                if (tc.getProblem().getId().equals(id)) {
                    testCaseRepository.delete(tc);
                }
            }
        }

        // Add new test cases
        if (newInputs != null && newOutputs != null && newInputs.size() == newOutputs.size()) {
            for (int i = 0; i < newInputs.size(); i++) {
                String input = new String(newInputs.get(i).getBytes(), StandardCharsets.UTF_8);
                String output = new String(newOutputs.get(i).getBytes(), StandardCharsets.UTF_8);
                if (!input.isEmpty()) {
                    TestCase newCase = new TestCase(problem, input, output, false);
                    testCaseRepository.save(newCase);
                }
            }
        }

        return "redirect:/admin";
    }
}
