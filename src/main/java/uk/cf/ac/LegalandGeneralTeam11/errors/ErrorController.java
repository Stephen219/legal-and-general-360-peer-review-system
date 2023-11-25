package uk.cf.ac.LegalandGeneralTeam11.errors;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {

    @GetMapping("/403")
    public String error403() {
        return "error/403";
    }

}
