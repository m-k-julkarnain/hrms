package com.dbms.hrms.controller;

import com.dbms.hrms.model.Requisition;
import com.dbms.hrms.repository.RequisitionRepository;
import com.dbms.hrms.util.ExcelExporter;
import com.dbms.hrms.util.PdfExporter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Controller
@RequestMapping("/requisitions")
public class RequisitionController {

    private final RequisitionRepository requisitionRepository;

    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("requisition", new Requisition());
        return "requisition/form";
    }

    @PostMapping
    public String create(HttpServletRequest request,
                         @RequestParam String title,
                         @RequestParam(required = false) String description) {

        Integer createdBy = (request.getSession(false) != null && request.getSession().getAttribute("hrUserId") instanceof Integer)
                ? (Integer) request.getSession().getAttribute("hrUserId") : null;

        Requisition r = Requisition.builder()
                .title(title)
                .description(description)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .build();
        requisitionRepository.save(r);
        return "redirect:/requisitions";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        Requisition r = requisitionRepository.findById(id).orElse(null);
        if (r == null) return "redirect:/requisitions";
        model.addAttribute("requisition", r);
        return "requisition/form";
    }

    @PostMapping("/update")
    public String update(@RequestParam Integer id,
                         @RequestParam String title,
                         @RequestParam(required = false) String description) {
        Requisition r = requisitionRepository.findById(id).orElse(null);
        if (r == null) return "redirect:/requisitions";
        r.setTitle(title);
        r.setDescription(description);
        requisitionRepository.save(r);
        return "redirect:/requisitions";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        requisitionRepository.findById(id).ifPresent(requisitionRepository::delete);
        return "redirect:/requisitions";
    }

    @GetMapping("/export/excel")
    public void exportExcel(HttpServletResponse res) throws IOException {
        List<Requisition> all = requisitionRepository.findAll();
        List<String> headers = List.of("ID","Title","Description","Created By","Created At");
        List<List<String>> rows = all.stream().map(r -> List.of(
                safe(r.getId()), safe(r.getTitle()), safe(r.getDescription()), safe(r.getCreatedBy()), safe(r.getCreatedAt())
        )).toList();
        ExcelExporter.export("requisitions", headers, rows, res);
    }

    @GetMapping("/export/pdf")
    public void exportPdf(HttpServletResponse res) throws IOException {
        List<Requisition> all = requisitionRepository.findAll();
        List<String> headers = List.of("ID","Title","Description","Created By","Created At");
        List<List<String>> rows = all.stream().map(r -> List.of(
                safe(r.getId()), safe(r.getTitle()), safe(r.getDescription()), safe(r.getCreatedBy()), safe(r.getCreatedAt())
        )).toList();
        PdfExporter.export("requisitions", headers, rows, res);
    }

    @GetMapping
    public String list(@RequestParam(required = false) String keyword, Model model) {

        List<Requisition> list;

        if (keyword != null && !keyword.isBlank()) {
            list = requisitionRepository.search(keyword);
        } else {
            list = requisitionRepository.findAll();
        }

        model.addAttribute("requisitions", list);
        model.addAttribute("keyword", keyword);

        return "requisition/list";
    }

    private static String safe(Object o) { return o == null ? "" : o.toString(); }
}
