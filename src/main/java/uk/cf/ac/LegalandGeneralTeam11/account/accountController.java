package uk.cf.ac.LegalandGeneralTeam11.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import uk.cf.ac.LegalandGeneralTeam11.Form.Form;
import uk.cf.ac.LegalandGeneralTeam11.Form.FormServiceImpl;
import uk.cf.ac.LegalandGeneralTeam11.FormRequest.FormRequest;
import uk.cf.ac.LegalandGeneralTeam11.FormRequest.FormRequestService;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;


@Controller

public class accountController {

    FormRequestService formRequestService;


    FormServiceImpl formservice;

    @Autowired
    public accountController(FormRequestService formRequestService, FormServiceImpl formservice) {
        this.formservice = formservice;
        this.formRequestService = formRequestService;
    }


    @GetMapping("/account")
    public ModelAndView getAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        System.out.println(currentPrincipalName);
        String username = currentPrincipalName;

        List<Form> forms = formservice.getFormByUser(username);
        ModelAndView modelAndView = new ModelAndView("account/dashboard");
        modelAndView.addObject("forms", forms);

        return modelAndView;


    }
    @GetMapping("/admin")
    public ModelAndView getAdmin(Model model) {
        List<FormRequest> formRequests = formRequestService.getAllByStatus("Pending");
        List<Form> allForms = formservice.getAllForms();
        List<Form> inProgressForms = formservice.getFormsByStatus("In Progress");
//        List<String> adjustedDates = inProgressForms.stream()
//                .map(form -> {
//                    LocalDateTime localDateTime = form.getFormDate().atStartOfDay();
//                    Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
//                    LocalDateTime adjustedDateTime = localDateTime.plusWeeks(2);
//                    return adjustedDateTime.toString(); // You can use a formatter for a different format
//                })
//                .collect(Collectors.toList());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");

        // Loop through the forms and perform date manipulation
        List<String> adjustedDates = inProgressForms.stream()
                .map(form -> {
                    LocalDateTime localDateTime = form.getFormDate().atStartOfDay();
                    Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
                    LocalDateTime adjustedDateTime = localDateTime.plusWeeks(2);
                    return adjustedDateTime.format(formatter);
                })
                .collect(Collectors.toList());




        ModelAndView modelAndView = new ModelAndView("account/adminDashboard");
        modelAndView.addObject("allForms", allForms);
        modelAndView.addObject("inProgressForms", inProgressForms);
        modelAndView.addObject("formRequests", formRequests);
        modelAndView.addObject("adjustedDates", adjustedDates);
        System.out.println(formRequests);

        return modelAndView;
    }
}
