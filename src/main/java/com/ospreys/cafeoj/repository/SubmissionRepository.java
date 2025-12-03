package com.ospreys.cafeoj.repository;

import com.ospreys.cafeoj.model.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    
    @Query("SELECT COUNT(DISTINCT s.problem) FROM Submission s WHERE s.user.id = :userId AND s.status = :status")
    long countDistinctProblemByUserIdAndStatus(@Param("userId") Long userId, @Param("status") String status);

    /**
     * Get IDs of all problems solved (ACCEPTED) by a user.
     * Optimized query that only fetches problem IDs instead of full Submission entities.
     */
    @Query("SELECT DISTINCT s.problem.id FROM Submission s WHERE s.user.id = :userId AND s.status = 'ACCEPTED'")
    Set<Long> findSolvedProblemIdsByUserId(@Param("userId") Long userId);

    /**
     * Get IDs of all problems attempted by a user (regardless of status).
     * Optimized query that only fetches problem IDs instead of full Submission entities.
     */
    @Query("SELECT DISTINCT s.problem.id FROM Submission s WHERE s.user.id = :userId")
    Set<Long> findAttemptedProblemIdsByUserId(@Param("userId") Long userId);

    List<Submission> findByUserIdAndProblemIdOrderBySubmissionDateDesc(Long userId, Long problemId);
}
