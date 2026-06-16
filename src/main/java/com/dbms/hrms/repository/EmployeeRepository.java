package com.dbms.hrms.repository;

import com.dbms.hrms.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    Optional<Employee> findByEmpCode(String empCode);
    @Query("SELECT e FROM Employee e WHERE " +
            "e.name LIKE %:keyword% OR " +
            "e.email LIKE %:keyword% OR " +
            "e.phone LIKE %:keyword% OR " +
            "e.empCode LIKE %:keyword%")
    List<Employee> search(@Param("keyword") String keyword);

}
