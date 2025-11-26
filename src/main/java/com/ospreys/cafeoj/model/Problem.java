
package com.ospreys.cafeoj.model;

import jakarta.persistence.*;

@Entity
@Table(name = "problems")
public class Problem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Double timeLimit = 1.0; // Seconds

    @Column(nullable = false)
    private Integer memoryLimit = 256; // MB

    public Problem() {}

    public Problem(String title, String description, Double timeLimit, Integer memoryLimit) {
        this.title = title;
        this.description = description;
        this.timeLimit = timeLimit != null ? timeLimit : 1.0;
        this.memoryLimit = memoryLimit != null ? memoryLimit : 256;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getTimeLimit() { return timeLimit; }
    public void setTimeLimit(Double timeLimit) { this.timeLimit = timeLimit; }

    public Integer getMemoryLimit() { return memoryLimit; }
    public void setMemoryLimit(Integer memoryLimit) { this.memoryLimit = memoryLimit; }
}
