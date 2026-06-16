package com.dbms.hrms.repository;

import com.dbms.hrms.model.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {
    List<Attendance> findByDate(LocalDate date);
    List<Attendance> findByEmployeeId(Integer employeeId);
    Optional<Attendance> findByEmployeeIdAndDate(Integer employeeId, LocalDate date);
}
