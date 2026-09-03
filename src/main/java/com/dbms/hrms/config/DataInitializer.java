package com.dbms.hrms.config;

import com.dbms.hrms.model.*;
import com.dbms.hrms.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final HrUserRepository hrUserRepository;
    private final EmployeeRepository employeeRepository;
    private final RequisitionRepository requisitionRepository;
    private final CandidateRepository candidateRepository;
    private final AttendanceRepository attendanceRepository;
    private final PaymentRepository paymentRepository;

    @Override
    public void run(String... args) {
        if (hrUserRepository.count() == 0) {
            log.info("HRMS database is empty. Initializing default sample data for instant demo...");

            // 1. Create Default Admin User
            HrUser admin = hrUserRepository.save(HrUser.builder()
                    .username("admin")
                    .password("password123")
                    .fullName("HR Administrator")
                    .build());

            // 2. Create Sample Employees
            Employee emp1 = employeeRepository.save(Employee.builder()
                    .empCode("EMP-1001")
                    .name("Alice Johnson")
                    .email("alice.johnson@example.com")
                    .phone("+1-555-0101")
                    .position("Senior Software Engineer")
                    .hireDate(LocalDate.of(2023, 1, 15))
                    .salary(new BigDecimal("95000.00"))
                    .createdAt(LocalDateTime.now())
                    .createdBy(admin.getId())
                    .build());

            Employee emp2 = employeeRepository.save(Employee.builder()
                    .empCode("EMP-1002")
                    .name("David Miller")
                    .email("david.miller@example.com")
                    .phone("+1-555-0102")
                    .position("Product Manager")
                    .hireDate(LocalDate.of(2023, 3, 1))
                    .salary(new BigDecimal("105000.00"))
                    .createdAt(LocalDateTime.now())
                    .createdBy(admin.getId())
                    .build());

            Employee emp3 = employeeRepository.save(Employee.builder()
                    .empCode("EMP-1003")
                    .name("Sarah Williams")
                    .email("sarah.w@example.com")
                    .phone("+1-555-0103")
                    .position("UI/UX Designer")
                    .hireDate(LocalDate.of(2023, 6, 20))
                    .salary(new BigDecimal("82000.00"))
                    .createdAt(LocalDateTime.now())
                    .createdBy(admin.getId())
                    .build());

            // 3. Create Sample Requisitions
            Requisition req1 = requisitionRepository.save(Requisition.builder()
                    .title("Full Stack Java Developer")
                    .description("Looking for a Spring Boot and modern frontend developer with 3+ years experience.")
                    .createdBy(admin.getId())
                    .createdAt(LocalDateTime.now().minusDays(10))
                    .build());

            Requisition req2 = requisitionRepository.save(Requisition.builder()
                    .title("Database Administrator")
                    .description("Responsible for MySQL optimization, backups, and data architecture.")
                    .createdBy(admin.getId())
                    .createdAt(LocalDateTime.now().minusDays(5))
                    .build());

            // 4. Create Sample Candidates
            candidateRepository.save(Candidate.builder()
                    .requisition(req1)
                    .name("Michael Brown")
                    .email("michael.b@example.com")
                    .phone("+1-555-0201")
                    .resumeUrl("https://example.com/resumes/michael-brown.pdf")
                    .status("INTERVIEW")
                    .appliedAt(LocalDateTime.now().minusDays(7))
                    .build());

            candidateRepository.save(Candidate.builder()
                    .requisition(req1)
                    .name("Emma Watson")
                    .email("emma.w@example.com")
                    .phone("+1-555-0202")
                    .resumeUrl("https://example.com/resumes/emma-watson.pdf")
                    .status("APPLIED")
                    .appliedAt(LocalDateTime.now().minusDays(3))
                    .build());

            candidateRepository.save(Candidate.builder()
                    .requisition(req2)
                    .name("Robert Green")
                    .email("robert.g@example.com")
                    .phone("+1-555-0203")
                    .resumeUrl("https://example.com/resumes/robert-green.pdf")
                    .status("SHORTLISTED")
                    .appliedAt(LocalDateTime.now().minusDays(2))
                    .build());

            // 5. Create Sample Attendance
            attendanceRepository.save(Attendance.builder()
                    .employeeId(emp1.getId())
                    .date(LocalDate.now())
                    .status("present")
                    .reason("On-time arrival")
                    .recordedBy(admin.getId())
                    .recordedAt(LocalDateTime.now())
                    .build());

            attendanceRepository.save(Attendance.builder()
                    .employeeId(emp2.getId())
                    .date(LocalDate.now())
                    .status("present")
                    .reason("On-time arrival")
                    .recordedBy(admin.getId())
                    .recordedAt(LocalDateTime.now())
                    .build());

            attendanceRepository.save(Attendance.builder()
                    .employeeId(emp3.getId())
                    .date(LocalDate.now())
                    .status("late")
                    .reason("Traffic delay")
                    .recordedBy(admin.getId())
                    .recordedAt(LocalDateTime.now())
                    .build());

            // 6. Create Sample Payments
            paymentRepository.save(Payment.builder()
                    .employeeId(emp1.getId())
                    .amount(new BigDecimal("7916.66"))
                    .paymentDate(LocalDateTime.now().minusDays(1))
                    .method("Bank Transfer")
                    .remarks("Monthly Salary - August")
                    .paidBy(admin.getId())
                    .createdAt(LocalDateTime.now().minusDays(1))
                    .build());

            paymentRepository.save(Payment.builder()
                    .employeeId(emp2.getId())
                    .amount(new BigDecimal("8750.00"))
                    .paymentDate(LocalDateTime.now().minusDays(1))
                    .method("Bank Transfer")
                    .remarks("Monthly Salary - August")
                    .paidBy(admin.getId())
                    .createdAt(LocalDateTime.now().minusDays(1))
                    .build());

            paymentRepository.save(Payment.builder()
                    .employeeId(emp3.getId())
                    .amount(new BigDecimal("6833.33"))
                    .paymentDate(LocalDateTime.now().minusDays(1))
                    .method("Bank Transfer")
                    .remarks("Monthly Salary - August")
                    .paidBy(admin.getId())
                    .createdAt(LocalDateTime.now().minusDays(1))
                    .build());

            log.info("HRMS default demo data initialized successfully!");
        }
    }
}
