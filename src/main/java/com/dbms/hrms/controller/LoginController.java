package com.dbms.hrms.controller;

import com.dbms.hrms.model.HrUser;
import com.dbms.hrms.repository.HrUserRepository;
import com.dbms.hrms.security.AuthInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@Controller
public class LoginController {

    private final HrUserRepository hrUserRepository;

    @GetMapping({"/", "/index", "/dashboard"})
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "error", required = false) String error, Model model) {
        model.addAttribute("error", error);
        return "login";
    }

    @PostMapping("/login")
    public String loginSubmit(@RequestParam String username,
                              @RequestParam String password,
                              HttpServletRequest request,
                              Model model) {

        HrUser user = hrUserRepository.findByUsername(username).orElse(null);
        if (user == null) {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }

        if (!user.getPassword().equals(password)) {
            model.addAttribute("error", "Invalid username or password");
            return "login";
        }

        HttpSession session = request.getSession(true);
        session.setAttribute(AuthInterceptor.SESSION_HR_USER_ID, user.getId());
        session.setAttribute("hrUserName", user.getFullName() == null ? user.getUsername() : user.getFullName());

        return "redirect:/dashboard";
    }
}
