package com.dbms.hrms.repository;

import com.dbms.hrms.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findByEmployeeId(Integer employeeId);

    // filter by payment_date range
    List<Payment> findByPaymentDateBetween(LocalDateTime start, LocalDateTime end);

    // filter by multiple employee ids + payment_date range
    List<Payment> findByEmployeeIdInAndPaymentDateBetween(List<Integer> employeeIds,
                                                          LocalDateTime start,
                                                          LocalDateTime end);

    // optional generic search (method / remarks / amount)
    @Query("SELECT p FROM Payment p WHERE " +
            "CAST(p.amount AS string) LIKE %:keyword% OR " +
            "p.method LIKE %:keyword% OR " +
            "p.remarks LIKE %:keyword%")
    List<Payment> search(@Param("keyword") String keyword);
}
