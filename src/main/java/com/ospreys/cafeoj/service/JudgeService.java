package com.ospreys.cafeoj.service;

import com.ospreys.cafeoj.model.Problem;
import com.ospreys.cafeoj.model.Submission;
import com.ospreys.cafeoj.model.TestCase;
import com.ospreys.cafeoj.repository.SubmissionRepository;
import com.ospreys.cafeoj.repository.TestCaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class JudgeService {

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private TestCaseRepository testCaseRepository;

    private static final String JUDGE_DIR = "/judge-data";

    @Async
    public void judge(Submission submission) {
        long submissionId = submission.getId();
        Path submissionDir = Paths.get(JUDGE_DIR, String.valueOf(submissionId));

        try {
            // 1. Setup Workspace
            Files.createDirectories(submissionDir);
            
            // Sanitize Code
            String code = submission.getCode();
            // Remove package declaration
            code = code.replaceAll("package\\s+.*;", "");
            // Rename public class to Solution
            code = code.replaceAll("public\\s+class\\s+\\w+", "public class Solution");
            
            Files.write(submissionDir.resolve("Solution.java"), code.getBytes(StandardCharsets.UTF_8));

            // 2. Compile
            // We use a docker container to compile to ensure environment consistency
            // But for simplicity/speed in this MVP, we can try to compile in the same container if javac is available,
            // OR spin up a container. Let's spin up a container for compilation to be safe.
            
            // Actually, for MVP, let's run javac inside the execution container for each test case? 
            // No, compile once, run multiple times.
            
            // Let's compile using a docker container
            ProcessBuilder compilePb = new ProcessBuilder(
                    "docker", "run", "--rm",
                    "-v", "judge-data:/judge-data",
                    "-w", "/judge-data/" + submissionId,
                    "eclipse-temurin:21-jdk-alpine",
                    "javac", "Solution.java"
            );
            compilePb.redirectErrorStream(true);
            
            // Capture output to see why it failed
            File compileLog = submissionDir.resolve("compile.log").toFile();
            compilePb.redirectOutput(compileLog);
            
            Process compileProcess = compilePb.start();
            boolean compiled = compileProcess.waitFor(60, TimeUnit.SECONDS);
            
            if (!compiled || compileProcess.exitValue() != 0) {
                String errorLog = Files.readString(compileLog.toPath(), StandardCharsets.UTF_8);
                System.err.println("Compilation Failed for Submission " + submissionId + ":\n" + errorLog);
                
                submission.setStatus("COMPILE_ERROR");
                submissionRepository.save(submission);
                cleanup(submissionDir);
                return;
            }

            // 3. Run Test Cases
            List<TestCase> testCases = testCaseRepository.findByProblemId(submission.getProblem().getId());
            Problem problem = submission.getProblem();
            
            for (int i = 0; i < testCases.size(); i++) {
                TestCase testCase = testCases.get(i);
                String containerName = "judge-" + submissionId + "-" + i;

                // Write input file
                Files.write(submissionDir.resolve("input.txt"), testCase.getInput().getBytes(StandardCharsets.UTF_8));

                // Run Container
                ProcessBuilder runPb = new ProcessBuilder(
                        "docker", "run", "--rm",
                        "--name", containerName,
                        "-v", "judge-data:/judge-data",
                        "-w", "/judge-data/" + submissionId,
                        "--memory=" + problem.getMemoryLimit() + "m",
                        "--cpus=1.0",
                        "--network=none",
                        "eclipse-temurin:21-jdk-alpine",
                        "sh", "-c", "java Solution < input.txt"
                );
                
                File outputFile = submissionDir.resolve("output.txt").toFile();
                runPb.redirectOutput(outputFile);
                runPb.redirectErrorStream(true);

                Process runProcess = runPb.start();
                
                // Time Limit Check
                boolean finished = runProcess.waitFor((long)(problem.getTimeLimit() * 1000 + 2000), TimeUnit.MILLISECONDS);

                if (!finished) {
                    runProcess.destroyForcibly();
                    // Explicitly kill the container
                    new ProcessBuilder("docker", "rm", "-f", containerName).start().waitFor();
                    
                    submission.setStatus("TLE");
                    submissionRepository.save(submission);
                    cleanup(submissionDir);
                    return;
                }

                if (runProcess.exitValue() != 0) {
                    submission.setStatus("RTE");
                    submissionRepository.save(submission);
                    cleanup(submissionDir);
                    return;
                }

                // Compare Output
                String actualOutput = Files.readString(outputFile.toPath(), StandardCharsets.UTF_8).trim();
                String expectedOutput = testCase.getExpectedOutput().trim();

                // Normalize line endings
                actualOutput = actualOutput.replace("\r\n", "\n");
                expectedOutput = expectedOutput.replace("\r\n", "\n");

                if (!actualOutput.equals(expectedOutput)) {
                    submission.setStatus("WA");
                    submissionRepository.save(submission);
                    cleanup(submissionDir);
                    return;
                }
            }

            // If all passed
            submission.setStatus("ACCEPTED");
            submissionRepository.save(submission);

        } catch (Exception e) {
            e.printStackTrace();
            submission.setStatus("INTERNAL_ERROR");
            submissionRepository.save(submission);
        } finally {
            cleanup(submissionDir);
        }
    }

    private void cleanup(Path dir) {
        try {
            // Simple recursive delete
            if (Files.exists(dir)) {
                Files.walk(dir)
                        .sorted((a, b) -> b.compareTo(a)) // Delete files before dirs
                        .forEach(p -> {
                            try { Files.delete(p); } catch (IOException e) {}
                        });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
