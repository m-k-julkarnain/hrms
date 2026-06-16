package com.dbms.hrms.controller;

import com.dbms.hrms.model.Candidate;
import com.dbms.hrms.model.Requisition;
import com.dbms.hrms.repository.CandidateRepository;
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
@RequestMapping("/candidates")
public class CandidateController {

    private final CandidateRepository candidateRepository;
    private final RequisitionRepository requisitionRepository;

    // ---------- LIST ----------
    @GetMapping
    public String list(@RequestParam(required = false) Integer requisitionId,
                       @RequestParam(required = false) String keyword,
                       Model model) {

        List<Candidate> candidates;

        if (keyword != null && !keyword.isBlank()) {
            candidates = candidateRepository.search(keyword);
        } else if (requisitionId != null) {
            candidates = candidateRepository.findByRequisition_Id(requisitionId);
        } else {
            candidates = candidateRepository.findAll();
        }

        model.addAttribute("candidates", candidates);
        model.addAttribute("requisitions", requisitionRepository.findAll());
        model.addAttribute("selectedReq", requisitionId);
        model.addAttribute("keyword", keyword);

        return "candidate/list";
    }

    // ---------- FORM ----------
    @GetMapping("/new")
    public String form(Model model) {
        model.addAttribute("candidate", new Candidate());
        model.addAttribute("requisitions", requisitionRepository.findAll());
        return "candidate/form";
    }

    // ---------- CREATE ----------
    @PostMapping
    public String create(@RequestParam(required = false) Integer requisitionId,
                         @RequestParam String name,
                         @RequestParam(required = false) String email,
                         @RequestParam(required = false) String phone,
                         @RequestParam(required = false) String resumeUrl,
                         @RequestParam(required = false) String status) {

        Requisition req = requisitionId == null
                ? null
                : requisitionRepository.findById(requisitionId).orElse(null);

        Candidate candidate = Candidate.builder()
                .requisition(req)
                .name(name)
                .email(email)
                .phone(phone)
                .resumeUrl(resumeUrl)
                .status(status == null ? "applied" : status)
                .appliedAt(LocalDateTime.now())
                .build();

        candidateRepository.save(candidate);
        return "redirect:/candidates";
    }

    // ---------- EDIT ----------
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        return candidateRepository.findById(id)
                .map(c -> {
                    model.addAttribute("candidate", c);
                    model.addAttribute("requisitions", requisitionRepository.findAll());
                    return "candidate/form";
                })
                .orElse("redirect:/candidates");
    }

    // ---------- UPDATE ----------
    @PostMapping("/update")
    public String update(@RequestParam Integer id,
                         @RequestParam(required = false) Integer requisitionId,
                         @RequestParam String name,
                         @RequestParam(required = false) String email,
                         @RequestParam(required = false) String phone,
                         @RequestParam(required = false) String resumeUrl,
                         @RequestParam(required = false) String status) {

        Candidate c = candidateRepository.findById(id).orElse(null);
        if (c == null) return "redirect:/candidates";

        Requisition req = requisitionId == null
                ? null
                : requisitionRepository.findById(requisitionId).orElse(null);

        c.setRequisition(req);
        c.setName(name);
        c.setEmail(email);
        c.setPhone(phone);
        c.setResumeUrl(resumeUrl);
        if (status != null) c.setStatus(status);

        candidateRepository.save(c);
        return "redirect:/candidates";
    }

    // ---------- DELETE ----------
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        candidateRepository.deleteById(id);
        return "redirect:/candidates";
    }
}
