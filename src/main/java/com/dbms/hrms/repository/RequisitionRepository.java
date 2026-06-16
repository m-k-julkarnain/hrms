package com.dbms.hrms.repository;

import com.dbms.hrms.model.Requisition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RequisitionRepository extends JpaRepository<Requisition, Integer> {
    @Query("SELECT r FROM Requisition r WHERE " +
            "r.title LIKE %:keyword% OR " +
            "r.description LIKE %:keyword%")
    List<Requisition> search(@Param("keyword") String keyword);

}
