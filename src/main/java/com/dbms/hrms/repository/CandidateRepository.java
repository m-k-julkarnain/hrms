package com.dbms.hrms.repository;

import com.dbms.hrms.model.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CandidateRepository extends JpaRepository<Candidate, Integer> {

    List<Candidate> findByRequisition_Id(Integer reqId);

    @Query("""
        SELECT c FROM Candidate c
        WHERE c.name LIKE %:keyword%
           OR c.email LIKE %:keyword%
           OR c.phone LIKE %:keyword%
    """)
    List<Candidate> search(@Param("keyword") String keyword);
}
