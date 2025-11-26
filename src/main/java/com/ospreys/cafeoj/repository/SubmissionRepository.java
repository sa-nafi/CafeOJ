package com.ospreys.cafeoj.repository;

import com.ospreys.cafeoj.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    
    @Query("SELECT COUNT(DISTINCT s.problem) FROM Submission s WHERE s.user.id = :userId AND s.status = :status")
    long countDistinctProblemByUserIdAndStatus(@org.springframework.data.repository.query.Param("userId") Long userId, @org.springframework.data.repository.query.Param("status") String status);

    java.util.List<Submission> findByUserId(Long userId);
}
