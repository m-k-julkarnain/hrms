package com.dbms.hrms.controller;

import com.dbms.hrms.model.Payment;
import com.dbms.hrms.model.Employee;
import com.dbms.hrms.repository.PaymentRepository;
import com.dbms.hrms.repository.EmployeeRepository;
import com.dbms.hrms.util.ExcelExporter;
import com.dbms.hrms.util.PdfExporter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.BindingResult;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final EmployeeRepository employeeRepository;

    // ---------- CREATE ----------
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("payment", Payment.builder().build());
        model.addAttribute("employees", employeeRepository.findAll());
        model.addAttribute("methods", List.of("Cash", "Bank Transfer", "Cheque"));
        return "payment/form";
    }

    @PostMapping
    public String create(@ModelAttribute Payment payment, BindingResult br, HttpServletRequest req) {
        if (payment.getCreatedAt() == null) payment.setCreatedAt(LocalDateTime.now());
        if (payment.getPaymentDate() == null) payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);
        return "redirect:/payments";
    }

    // ---------- EDIT / UPDATE ----------
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Optional<Payment> opt = paymentRepository.findById(id);
        if (opt.isEmpty()) {
            return "redirect:/payments";
        }
        model.addAttribute("payment", opt.get());
        model.addAttribute("employees", employeeRepository.findAll());
        model.addAttribute("methods", List.of("Cash", "Bank Transfer", "Cheque"));
        return "payment/form";
    }

    @PostMapping("/update")
    public String update(@ModelAttribute Payment payment, BindingResult br) {
        if (payment.getId() == null || paymentRepository.findById(payment.getId()).isEmpty()) {
            return "redirect:/payments";
        }
        Payment existing = paymentRepository.findById(payment.getId()).get();
        if (payment.getCreatedAt() == null) payment.setCreatedAt(existing.getCreatedAt());
        if (payment.getPaymentDate() == null) payment.setPaymentDate(existing.getPaymentDate());
        paymentRepository.save(payment);
        return "redirect:/payments";
    }

    // ---------- DELETE ----------
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        paymentRepository.deleteById(id);
        return "redirect:/payments";
    }

    // ---------- EXPORTS: accept search params too ----------
    @GetMapping("/export/excel")
    public void exportExcel(@RequestParam(required = false) String payMonth,
                            @RequestParam(required = false) Integer year,
                            @RequestParam(required = false) String employeeIds,
                            @RequestParam(required = false) String startDate,
                            @RequestParam(required = false) String endDate,
                            HttpServletResponse res) throws IOException {

        List<Payment> list = fetchByFiltersWithSearchParams(payMonth, year, null, employeeIds, startDate, endDate);
        Map<Integer, String> empNames = employeeRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName));

        List<String> headers = List.of("ID","Employee ID","Employee Name","Amount","Payment Date","Method","Remarks","Paid By","Created At");
        List<List<String>> rows = new ArrayList<>();
        for (Payment p : list) {
            rows.add(List.of(
                    safe(p.getId()),
                    safe(p.getEmployeeId()),
                    safe(empNames.get(p.getEmployeeId())),
                    // format amount safely for exports
                    p.getAmount() != null ? p.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString() : "",
                    safe(p.getPaymentDate()),
                    safe(p.getMethod()),
                    safe(p.getRemarks()),
                    safe(p.getPaidBy()),
                    safe(p.getCreatedAt())
            ));
        }
        ExcelExporter.export("payments" + (payMonth == null ? "" : "-" + payMonth), headers, rows, res);
    }

    @GetMapping("/export/pdf")
    public void exportPdf(@RequestParam(required = false) String payMonth,
                          @RequestParam(required = false) Integer year,
                          @RequestParam(required = false) String employeeIds,
                          @RequestParam(required = false) String startDate,
                          @RequestParam(required = false) String endDate,
                          HttpServletResponse res) throws IOException {

        List<Payment> list = fetchByFiltersWithSearchParams(payMonth, year, null, employeeIds, startDate, endDate);
        Map<Integer, String> empNames = employeeRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName));

        List<String> headers = List.of("ID","Employee ID","Employee Name","Amount","Payment Date","Method","Remarks","Paid By","Created At");
        List<List<String>> rows = new ArrayList<>();
        for (Payment p : list) {
            rows.add(List.of(
                    safe(p.getId()),
                    safe(p.getEmployeeId()),
                    safe(empNames.get(p.getEmployeeId())),
                    p.getAmount() != null ? p.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString() : "",
                    safe(p.getPaymentDate()),
                    safe(p.getMethod()),
                    safe(p.getRemarks()),
                    safe(p.getPaidBy()),
                    safe(p.getCreatedAt())
            ));
        }
        PdfExporter.export("payments" + (payMonth == null ? "" : "-" + payMonth), headers, rows, res);
    }

    // ---------- LIST (existing behavior) ----------
    @GetMapping
    public String list(@RequestParam(required = false) String payMonth,
                       @RequestParam(required = false) Integer year,
                       @RequestParam(required = false) String keyword,
                       Model model) {

        List<Payment> list = fetchByFilters(payMonth, year, keyword);

        Map<Integer, String> employeeNames = employeeRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName));

        // build formatted amounts map (Integer keys)
        Map<Integer, String> formattedAmounts = list.stream()
                .collect(Collectors.toMap(
                        Payment::getId,
                        p -> p.getAmount() != null ? p.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString() : ""
                ));

        model.addAttribute("payments", list);
        model.addAttribute("employeeNames", employeeNames);
        model.addAttribute("formattedAmounts", formattedAmounts); // <-- added
        model.addAttribute("payMonth", payMonth);
        model.addAttribute("year", year);
        model.addAttribute("keyword", keyword);

        return "payment/list";
    }

    /**
     * NEW: search by comma-separated employeeIds and/or date range
     */
    @GetMapping("/search")
    public String search(@RequestParam(required = false) String employeeIds,
                         @RequestParam(required = false) String startDate,
                         @RequestParam(required = false) String endDate,
                         Model model) {

        LocalDate today = LocalDate.now();
        LocalDate sDate = null;
        LocalDate eDate = null;
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        try {
            if (startDate != null && !startDate.isBlank()) {
                sDate = LocalDate.parse(startDate.trim(), df);
            }
        } catch (Exception ignored) { sDate = null; }

        try {
            if (endDate != null && !endDate.isBlank()) {
                eDate = LocalDate.parse(endDate.trim(), df);
            }
        } catch (Exception ignored) { eDate = null; }

        if (sDate == null && eDate == null) {
            eDate = today;
            sDate = today.minusDays(30);
        } else if (sDate == null) {
            sDate = eDate.minusDays(30);
        } else if (eDate == null) {
            eDate = sDate.plusDays(30);
        }

        if (sDate.isAfter(eDate)) {
            LocalDate tmp = sDate;
            sDate = eDate;
            eDate = tmp;
        }

        LocalDateTime startLdt = sDate.atStartOfDay();
        LocalDateTime endLdt = eDate.atTime(23, 59, 59, 999_999_999);

        List<Integer> ids = parseCsvToInts(employeeIds);

        List<Payment> results;
        if (ids.isEmpty()) {
            results = paymentRepository.findByPaymentDateBetween(startLdt, endLdt);
        } else {
            results = paymentRepository.findByEmployeeIdInAndPaymentDateBetween(ids, startLdt, endLdt);
        }

        Map<Integer, String> employeeNames = employeeRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName));

        // formattedAmounts for search results
        Map<Integer, String> formattedAmounts = results.stream()
                .collect(Collectors.toMap(
                        Payment::getId,
                        p -> p.getAmount() != null ? p.getAmount().setScale(2, RoundingMode.HALF_UP).toPlainString() : ""
                ));

        model.addAttribute("payments", results);
        model.addAttribute("employeeNames", employeeNames);
        model.addAttribute("formattedAmounts", formattedAmounts); // <-- added
        model.addAttribute("employeeIds", employeeIds);
        model.addAttribute("startDate", (sDate != null ? sDate.format(df) : ""));
        model.addAttribute("endDate", (eDate != null ? eDate.format(df) : ""));

        return "payment/list";
    }

    // ----------------- Helpers -----------------

    private List<Payment> fetchByFilters(String payMonth, Integer year, String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            String k = keyword.trim();
            if (k.matches("\\d+")) {
                try {
                    Integer empId = Integer.valueOf(k);
                    return paymentRepository.findByEmployeeId(empId);
                } catch (NumberFormatException ex) {
                    return Collections.emptyList();
                }
            } else {
                return paymentRepository.search(k);
            }
        }

        if (payMonth != null && !payMonth.isBlank()) {
            try {
                YearMonth ym = YearMonth.parse(payMonth);
                LocalDateTime start = ym.atDay(1).atStartOfDay();
                LocalDateTime end = ym.atEndOfMonth().atTime(23, 59, 59, 999_999_999);
                return paymentRepository.findByPaymentDateBetween(start, end);
            } catch (Exception ex) {
                return paymentRepository.findAll();
            }
        }

        if (year != null) {
            LocalDateTime start = Year.of(year).atDay(1).atStartOfDay();
            LocalDateTime end = Year.of(year).atMonth(12).atEndOfMonth().atTime(23,59,59,999_999_999);
            return paymentRepository.findByPaymentDateBetween(start, end);
        }

        return paymentRepository.findAll();
    }

    private List<Payment> fetchByFiltersWithSearchParams(String payMonth, Integer year, String keyword,
                                                         String employeeIds, String startDate, String endDate) {
        if ((startDate != null && !startDate.isBlank()) || (endDate != null && !endDate.isBlank()) || (employeeIds != null && !employeeIds.isBlank())) {
            LocalDate today = LocalDate.now();
            LocalDate sDate = null;
            LocalDate eDate = null;
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            try {
                if (startDate != null && !startDate.isBlank()) sDate = LocalDate.parse(startDate.trim(), df);
            } catch (Exception ignored) { sDate = null; }
            try {
                if (endDate != null && !endDate.isBlank()) eDate = LocalDate.parse(endDate.trim(), df);
            } catch (Exception ignored) { eDate = null; }
            if (sDate == null && eDate == null) {
                eDate = today;
                sDate = today.minusDays(30);
            } else if (sDate == null) {
                sDate = eDate.minusDays(30);
            } else if (eDate == null) {
                eDate = sDate.plusDays(30);
            }
            if (sDate.isAfter(eDate)) {
                LocalDate tmp = sDate; sDate = eDate; eDate = tmp;
            }
            LocalDateTime startLdt = sDate.atStartOfDay();
            LocalDateTime endLdt = eDate.atTime(23, 59, 59, 999_999_999);
            List<Integer> ids = parseCsvToInts(employeeIds);
            if (ids.isEmpty()) {
                return paymentRepository.findByPaymentDateBetween(startLdt, endLdt);
            } else {
                return paymentRepository.findByEmployeeIdInAndPaymentDateBetween(ids, startLdt, endLdt);
            }
        }
        return fetchByFilters(payMonth, year, keyword);
    }

    private List<Integer> parseCsvToInts(String csv) {
        if (csv == null || csv.isBlank()) return Collections.emptyList();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    try {
                        return Integer.valueOf(s);
                    } catch (NumberFormatException ex) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
    }

    private Integer getSessionUserId(HttpServletRequest request) {
        Object sid = (request.getSession(false) == null) ? null : request.getSession().getAttribute("hrUserId");
        if (sid instanceof Integer) return (Integer) sid;
        if (sid instanceof Long) return ((Long) sid).intValue();
        if (sid instanceof String) {
            try { return Integer.valueOf((String) sid); } catch (Exception ignored) {}
        }
        return null;
    }

    private static String safe(Object o) { return o == null ? "" : o.toString(); }
}
