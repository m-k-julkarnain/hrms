package com.dbms.hrms.controller;

import com.dbms.hrms.model.Attendance;
import com.dbms.hrms.model.Employee;
import com.dbms.hrms.repository.AttendanceRepository;
import com.dbms.hrms.repository.EmployeeRepository;
import com.dbms.hrms.util.ExcelExporter;
import com.dbms.hrms.util.PdfExporter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeRepository employeeRepository;

    // LIST (filter by date)
    @GetMapping
    public String list(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                       Model model) {
        LocalDate d = (date == null) ? LocalDate.now() : date;
        List<Attendance> list = attendanceRepository.findByDate(d);
        List<Employee> employees = employeeRepository.findAll();
        Map<Integer, String> empNames = employees.stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName));
        model.addAttribute("attendanceList", list);
        model.addAttribute("date", d);
        model.addAttribute("employeeNames", empNames);
        return "attendance/list";
    }

    // NEW single
    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("attendance", new Attendance());
        model.addAttribute("employees", employeeRepository.findAll());
        model.addAttribute("statuses", List.of("present", "absent", "late"));
        return "attendance/form";
    }

    // CREATE single (same as save from new)
    @PostMapping
    public String create(HttpServletRequest request,
                         @RequestParam Integer employeeId,
                         @RequestParam("date") String dateStr,
                         @RequestParam String status,
                         @RequestParam(required = false) String reason,
                         Model model) {

        LocalDate date;
        try { date = parseDateFlexible(dateStr); } catch (Exception ex) {
            model.addAttribute("error", "Invalid date format.");
            model.addAttribute("employees", employeeRepository.findAll());
            model.addAttribute("statuses", List.of("present", "absent", "late"));
            return "attendance/form";
        }

        // If there is an existing attendance for same employee+date, replace it
        attendanceRepository.findByEmployeeIdAndDate(employeeId, date).ifPresent(attendanceRepository::delete);

        Attendance a = Attendance.builder()
                .employeeId(employeeId)
                .date(date)
                .status(status)
                .reason(reason)
                .recordedBy(getSessionUserId(request))
                .recordedAt(LocalDateTime.now())
                .build();
        attendanceRepository.save(a);
        return "redirect:/attendance?date=" + date.toString();
    }

    // EDIT single form
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Attendance a = attendanceRepository.findById(id).orElse(null);
        if (a == null) return "redirect:/attendance";
        model.addAttribute("attendance", a);
        model.addAttribute("employees", employeeRepository.findAll());
        model.addAttribute("statuses", List.of("present", "absent", "late"));
        return "attendance/form";
    }

    // UPDATE single
    @PostMapping("/update")
    public String update(HttpServletRequest request,
                         @RequestParam Integer id,
                         @RequestParam Integer employeeId,
                         @RequestParam("date") String dateStr,
                         @RequestParam String status,
                         @RequestParam(required = false) String reason) {

        Attendance a = attendanceRepository.findById(id).orElse(null);
        if (a == null) return "redirect:/attendance";

        LocalDate date;
        try { date = parseDateFlexible(dateStr); } catch (Exception ex) { date = a.getDate(); }

        // If changing employee+date to something that collides, remove old record to avoid unique constraints
        attendanceRepository.findByEmployeeIdAndDate(employeeId, date).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                attendanceRepository.delete(existing);
            }
        });

        a.setEmployeeId(employeeId);
        a.setDate(date);
        a.setStatus(status);
        a.setReason(reason);
        a.setRecordedBy(getSessionUserId(request));
        a.setRecordedAt(LocalDateTime.now());
        attendanceRepository.save(a);
        return "redirect:/attendance?date=" + date.toString();
    }

    // DELETE single
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        attendanceRepository.findById(id).ifPresent(attendanceRepository::delete);
        return "redirect:/attendance";
    }

    // BULK form (unchanged from previous) - kept for convenience
    @GetMapping("/bulk")
    public String bulkForm(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                           Model model) {
        LocalDate d = (date == null) ? LocalDate.now() : date;
        List<Employee> employees = employeeRepository.findAll();
        Map<Integer, Attendance> existing = attendanceRepository.findByDate(d)
                .stream().collect(Collectors.toMap(Attendance::getEmployeeId, x -> x));
        model.addAttribute("employees", employees);
        model.addAttribute("existing", existing);
        model.addAttribute("date", d);
        model.addAttribute("statuses", List.of("present", "absent", "late"));
        return "attendance/bulk";
    }

    // BULK save (unchanged)
    @PostMapping("/bulk")
    public String bulkSave(HttpServletRequest request,
                           @RequestParam("date") String dateStr,
                           @RequestParam(name = "employeeId", required = false) List<String> employeeIdParams,
                           @RequestParam(name = "status", required = false) List<String> statusParams,
                           Model model) {

        LocalDate date;
        try { date = parseDateFlexible(dateStr); } catch (Exception ex) {
            model.addAttribute("error", "Invalid date format.");
            return "attendance/bulk";
        }

        // normalize employee ids
        List<Integer> employeeIds = new ArrayList<>();
        if (employeeIdParams != null) {
            if (employeeIdParams.size() == 1 && employeeIdParams.get(0).contains(",")) {
                for (String part : employeeIdParams.get(0).split(",")) {
                    try { employeeIds.add(Integer.valueOf(part.trim())); } catch (Exception ignored) {}
                }
            } else {
                for (String s : employeeIdParams) {
                    try { employeeIds.add(Integer.valueOf(s.trim())); } catch (Exception ignored) {}
                }
            }
        }

        // normalize statuses
        List<String> statuses = new ArrayList<>();
        if (statusParams != null) {
            if (statusParams.size() == 1 && statusParams.get(0).contains(",")) {
                for (String part : statusParams.get(0).split(",")) statuses.add(part.trim());
            } else {
                statuses.addAll(statusParams);
            }
        }

        if (employeeIds.size() != statuses.size()) {
            model.addAttribute("error", "Mismatched employees and statuses.");
            return "attendance/bulk";
        }

        Integer recordedBy = getSessionUserId(request);

        for (int i = 0; i < employeeIds.size(); i++) {
            Integer empId = employeeIds.get(i);
            String st = statuses.get(i) == null ? "present" : statuses.get(i);

            Optional<Attendance> opt = attendanceRepository.findByEmployeeIdAndDate(empId, date);
            Attendance a;
            if (opt.isPresent()) {
                a = opt.get();
                a.setStatus(st);
                a.setReason(null);
                a.setRecordedBy(recordedBy);
                a.setRecordedAt(LocalDateTime.now());
            } else {
                a = Attendance.builder()
                        .employeeId(empId)
                        .date(date)
                        .status(st)
                        .reason(null)
                        .recordedBy(recordedBy)
                        .recordedAt(LocalDateTime.now())
                        .build();
            }
            attendanceRepository.save(a);
        }

        return "redirect:/attendance?date=" + date.toString();
    }

    // EXPORT EXCEL
    @GetMapping("/export/excel")
    public void exportExcel(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                            HttpServletResponse res) throws IOException {
        LocalDate d = (date == null) ? LocalDate.now() : date;
        List<Attendance> list = attendanceRepository.findByDate(d);
        Map<Integer, String> empNames = employeeRepository.findAll().stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName));

        List<String> headers = List.of("ID", "Employee ID", "Employee Name", "Date", "Status", "Reason", "Recorded By", "Recorded At");
        List<List<String>> rows = list.stream()
                .map(a -> List.of(
                        safe(a.getId()),
                        safe(a.getEmployeeId()),
                        safe(empNames.get(a.getEmployeeId())),
                        safe(a.getDate()),
                        safe(a.getStatus()),
                        safe(a.getReason()),
                        safe(a.getRecordedBy()),
                        safe(a.getRecordedAt())
                )).collect(Collectors.toList());

        ExcelExporter.export("attendance-" + d.toString(), headers, rows, res);
    }

    // EXPORT PDF
    @GetMapping("/export/pdf")
    public void exportPdf(@RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date,
                          HttpServletResponse res) throws IOException {
        LocalDate d = (date == null) ? LocalDate.now() : date;
        List<Attendance> list = attendanceRepository.findByDate(d);
        Map<Integer, String> empNames = employeeRepository.findAll().stream()
                .collect(Collectors.toMap(Employee::getId, Employee::getName));

        List<String> headers = List.of("ID", "Employee ID", "Employee Name", "Date", "Status", "Reason", "Recorded By", "Recorded At");
        List<List<String>> rows = list.stream()
                .map(a -> List.of(
                        safe(a.getId()),
                        safe(a.getEmployeeId()),
                        safe(empNames.get(a.getEmployeeId())),
                        safe(a.getDate()),
                        safe(a.getStatus()),
                        safe(a.getReason()),
                        safe(a.getRecordedBy()),
                        safe(a.getRecordedAt())
                )).collect(Collectors.toList());

        PdfExporter.export("Attendance-" + d.toString(), headers, rows, res);
    }

    // helpers
    private static LocalDate parseDateFlexible(String dateStr) {
        if (dateStr == null) throw new IllegalArgumentException("date is null");
        dateStr = dateStr.trim();
        try { return LocalDate.parse(dateStr); } catch (Exception ignored) {}
        try {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("d/M/yyyy");
            return LocalDate.parse(dateStr, f);
        } catch (Exception ignored) {}
        try {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("d-M-yyyy");
            return LocalDate.parse(dateStr, f);
        } catch (Exception ignored) {}
        throw new IllegalArgumentException("Unrecognized date format: " + dateStr);
    }

    private static String safe(Object o) {
        if (o == null) return "";
        return o.toString();
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
}
