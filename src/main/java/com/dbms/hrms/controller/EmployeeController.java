package com.dbms.hrms.controller;

import com.dbms.hrms.model.Employee;
import com.dbms.hrms.repository.EmployeeRepository;
import com.dbms.hrms.util.ExcelExporter;
import com.dbms.hrms.util.PdfExporter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Controller
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeRepository employeeRepository;


    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("employee", new Employee());
        return "employee/form";
    }

    @PostMapping
    public String create(HttpServletRequest request,
                         @RequestParam(required = false) String empCode,
                         @RequestParam String name,
                         @RequestParam(required = false) String email,
                         @RequestParam(required = false) String phone,
                         @RequestParam(required = false) String position,
                         @RequestParam(required = false) LocalDate hireDate,
                         @RequestParam(required = false) BigDecimal salary,
                         Model model) {

        // basic duplicate check
        if (empCode != null && !empCode.isBlank() && employeeRepository.findByEmpCode(empCode).isPresent()) {
            model.addAttribute("error", "Employee code already exists.");
            model.addAttribute("employee", Employee.builder()
                    .empCode(empCode).name(name).email(email).phone(phone).position(position).hireDate(hireDate).salary(salary).build());
            return "employee/form";
        }

        Integer createdBy = (request.getSession(false) != null && request.getSession().getAttribute("hrUserId") instanceof Integer)
                ? (Integer) request.getSession().getAttribute("hrUserId") : null;

        Employee e = Employee.builder()
                .empCode(empCode)
                .name(name)
                .email(email)
                .phone(phone)
                .position(position)
                .hireDate(hireDate)
                .salary(salary)
                .createdAt(LocalDateTime.now())
                .createdBy(createdBy)
                .build();

        employeeRepository.save(e);
        return "redirect:/employees";
    }

    // --- EDIT ---
    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Employee e = employeeRepository.findById(id).orElse(null);
        if (e == null) return "redirect:/employees";
        model.addAttribute("employee", e);
        return "employee/form";
    }

    @PostMapping("/update")
    public String update(HttpServletRequest request,
                         @RequestParam Integer id,
                         @RequestParam(required = false) String empCode,
                         @RequestParam String name,
                         @RequestParam(required = false) String email,
                         @RequestParam(required = false) String phone,
                         @RequestParam(required = false) String position,
                         @RequestParam(required = false) LocalDate hireDate,
                         @RequestParam(required = false) BigDecimal salary) {

        Employee e = employeeRepository.findById(id).orElse(null);
        if (e == null) return "redirect:/employees";

        // check duplicate empCode (if changed)
        if (empCode != null && !empCode.isBlank() && !empCode.equals(e.getEmpCode())) {
            if (employeeRepository.findByEmpCode(empCode).isPresent()) {
                // simple behaviour: do not update and redirect back to edit with message not implemented to keep code simple
                return "redirect:/employees/edit/" + id + "?error=code_exists";
            }
        }

        e.setEmpCode(empCode);
        e.setName(name);
        e.setEmail(email);
        e.setPhone(phone);
        e.setPosition(position);
        e.setHireDate(hireDate);
        e.setSalary(salary);

        employeeRepository.save(e);
        return "redirect:/employees";
    }

    // --- DELETE ---
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        employeeRepository.findById(id).ifPresent(employeeRepository::delete);
        return "redirect:/employees";
    }

    // --- EXPORTS ---
    @GetMapping("/export/excel")
    public void exportExcel(HttpServletResponse res) throws IOException {
        List<Employee> list = employeeRepository.findAll();
        List<String> headers = List.of("ID", "Emp Code", "Name", "Email", "Phone", "Position", "Hire Date", "Salary");
        List<List<String>> rows = list.stream().map(e -> List.of(
                safe(e.getId()), safe(e.getEmpCode()), safe(e.getName()), safe(e.getEmail()),
                safe(e.getPhone()), safe(e.getPosition()), safe(e.getHireDate()), safe(e.getSalary())
        )).collect(Collectors.toList());
        ExcelExporter.export("employees", headers, rows, res);
    }

    @GetMapping("/export/pdf")
    public void exportPdf(HttpServletResponse res) throws IOException {
        List<Employee> list = employeeRepository.findAll();
        List<String> headers = List.of("ID", "Emp Code", "Name", "Email", "Phone", "Position", "Hire Date", "Salary");
        List<List<String>> rows = list.stream().map(e -> List.of(
                safe(e.getId()), safe(e.getEmpCode()), safe(e.getName()), safe(e.getEmail()),
                safe(e.getPhone()), safe(e.getPosition()), safe(e.getHireDate()), safe(e.getSalary())
        )).collect(Collectors.toList());
        PdfExporter.export("employees", headers, rows, res);
    }

    private static String safe(Object o) {
        return o == null ? "" : o.toString();
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword, Model model) {
        List<Employee> employees;

        if (keyword != null && !keyword.isBlank()) {
            employees = employeeRepository.search(keyword);
        } else {
            employees = employeeRepository.findAll();
        }

        model.addAttribute("employees", employees);
        model.addAttribute("keyword", keyword);

        return "employee/list";
    }

}

