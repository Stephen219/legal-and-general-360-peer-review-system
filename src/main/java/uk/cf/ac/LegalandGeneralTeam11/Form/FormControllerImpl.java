package uk.cf.ac.LegalandGeneralTeam11.Form;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.structurizr.annotation.UsedByPerson;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.context.Context;
import uk.cf.ac.LegalandGeneralTeam11.FormRequest.FormRequest;
import uk.cf.ac.LegalandGeneralTeam11.FormRequest.FormRequestService;
import uk.cf.ac.LegalandGeneralTeam11.Graphs.GraphService;
import uk.cf.ac.LegalandGeneralTeam11.answers.Answer;
import uk.cf.ac.LegalandGeneralTeam11.answers.AnswerServiceInter;
import uk.cf.ac.LegalandGeneralTeam11.domain.Domain;
import uk.cf.ac.LegalandGeneralTeam11.domain.DomainService;
import uk.cf.ac.LegalandGeneralTeam11.emails.EmailServiceImpl;
import uk.cf.ac.LegalandGeneralTeam11.questions.Question;
import uk.cf.ac.LegalandGeneralTeam11.questions.QuestionServiceInter;
import uk.cf.ac.LegalandGeneralTeam11.user.UserService;
import java.time.LocalDate;
import java.util.List;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Controller
@UsedByPerson(name = "admin", description = "accept form", technology = "http(s)")
@UsedByPerson(name = "employee", description = "submits the review form", technology = "http(s)")

public class FormControllerImpl {
    @Autowired
    private FormService formService;

    private HttpServletRequest request;

    private FormRequestService FormRequestService;

    private QuestionServiceInter questionServiceInter;

    private AnswerServiceInter AnswerServiceInter;

    private EmailServiceImpl emailService;
    GraphService graphservic;
    @Autowired
    DomainService domainService;
    @Autowired
    UserService userservice;

    /**
     * Constructor for FormControllerImpl
     *
     * @param formServiceImpl the form service
     * @param request the request
     * @param formRequestService
     * @param questionServiceInter
     * @param answerServiceInter
     * @param emailService
     */

    @Autowired
    public FormControllerImpl(FormServiceImpl formServiceImpl, HttpServletRequest request, FormRequestService formRequestService, QuestionServiceInter questionServiceInter, AnswerServiceInter answerServiceInter, EmailServiceImpl emailService) {
        this.formService = formServiceImpl;
        this.request = request;
        this.FormRequestService = formRequestService;
        this.questionServiceInter = questionServiceInter;
        this.AnswerServiceInter = answerServiceInter;
        this.emailService = emailService;
    }

    /**
     * Gets the form request page
     * @param id
     * @return
     */



    @GetMapping("/accept/{id}")
    public String acceptForm(@PathVariable Long id) {
        String username = formService.getUsernameForFormRequest(id);
        FormRequest formRequest = FormRequestService.getFormRequestById(id);
        System.out.println(formRequest);
        formService.updateFormRequest(formRequest);
        LocalDate formDate = LocalDate.now();
        formService.grantFormAccess(username, formDate);
        return "redirect:/admin";
    }
    /**
     * Gets the form request page
     * @param id
     * @return
     */


    @GetMapping("/get_reviewers/{id}")
    public ModelAndView getReviewers(@PathVariable String id) {
        String url = buildUrl(request);
        System.out.println(url);
        System.out.println(url + "/get_reviewers/" + id);
        Form form = formService.getFormById(id);

        List<Domain> domains = domainService.getAllDomains();
        List<String> allowedDomains = domains.stream()
                .filter(Domain::getEnabled)
                .map(Domain::getDomain)
                .collect(Collectors.toList());

        ModelAndView modelAndView = new ModelAndView("forms/reviewer");
        modelAndView.addObject("form", form);
        modelAndView.addObject("allowedDomains", allowedDomains);
        return modelAndView;
    }



    /**
     * submits the reviewers
     * @param id
     * @return
     */

    /**
    * This method is used to submit the reviewers for a form
    * @param id the id of the form
    *
    * i have changed this to enable async processing of the emails
    * so the email are handlwed in a separate thread to avoid blocking the main thread after the form is rendered
     */

    @PostMapping("/submit_reviewers/{id}")
    public ModelAndView submitReviewers(@RequestParam("emails") List<String> uniqueEmails, @PathVariable String id) {
        String username = formService.getFormOwner(id);
        String ownerEmail = userservice.getUserByUserName(username).getEmail();
        //  enable the user to see his own results easily, their email also needs to be added to the list of reviewers
        uniqueEmails.add(ownerEmail);
        formService.addFormReviewers(id, uniqueEmails);
        Form form = formService.getFormById(id);
        ModelAndView modelAndView = new ModelAndView("redirect:/review/" + id);
        modelAndView.addObject("form", form);
        sendReviewInvitationEmailsAsync(uniqueEmails, id);
        return modelAndView;
    }


    @Async
   void sendReviewInvitationEmailsAsync(List<String> emails, String formId) {
        try {
            Thread.sleep(5000);
            for (String email : emails) {
                Thread.sleep(2000);
                sendReviewInvitationEmail(email, formId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
    /**
     * Sends an email to the reviewer with a link to the form
     * @param to the email address of the reviewer
     * @param formId the id of the form
     */
    @Async
     void sendReviewInvitationEmail(String to, String formId) {
        String url = buildUrl(request);
        Context context = new Context();
        context.setVariable("formId", formId);
        context.setVariable("to", to);
        context.setVariable("url", url + "/review/" + formId);
        String name = formService.getFormById(formId).getUsername();
        context.setVariable("name", name);
        emailService.sendSimpleMessage(to, "Review Form Invitation", "account/fillFormEmail", context);
    }



    /**
     * Gets the get url
     * @param request
     * @return
     */
    private String buildUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        System.out.println(scheme);
        System.out.println(scheme + "://" + request.getServerName() + ":" + request.getServerPort());
        String webAddress = request.getServerName();
        int port = request.getServerPort();
        return scheme + "://" + webAddress + ":" + port;
    }


    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/review/{formId}")
    public ModelAndView getForm(@PathVariable String formId) {




        // i added this try catch  code to handle null error for cyber security
        try {
            Form form = formService.getFormById(formId);

        List<Question> questions = questionServiceInter.getAllQuestions();
        List<Question> textQuestions = questionServiceInter.getTextAreaQuestions();
        ModelAndView modelAndView = new ModelAndView("forms/formImpl");
        modelAndView.addObject("form", form);
        modelAndView.addObject("questions", questions);
        modelAndView.addObject("textQuestions", textQuestions);
        modelAndView.addObject("isOwner", isOwner(form));
        return modelAndView;
    }
        catch (IllegalArgumentException e) {
            ModelAndView modelAndView = new ModelAndView("error/FormNotFound");
            modelAndView.setStatus(HttpStatus.NOT_FOUND);
            return modelAndView;
        }
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/review/{formId}")
    public String submitReview(@PathVariable String formId,
                               @RequestParam("responses") String responses,
                               @RequestParam(value = "email", required = false) String fillerEmail,
                               @RequestParam(value = "Who", required = false) String Relationship,
                               RedirectAttributes redirectAttributes) {
        System.out.println(responses);
        System.out.println(responses);
        System.out.println(responses);
        System.out.println(responses);
        System.out.println(responses);
        try {
            if (formService.checkFormCompleted(formId)) {
                formService.updateFormStatus(formId, "Completed");
                redirectAttributes.addFlashAttribute("flashError", "This form is no longer receiving responses");
                return "redirect:/review/" + formId;
            }
            System.out.println(responses);

            ObjectMapper objectMapper = new ObjectMapper();
            System.out.println(responses);
            List<Answer> answerList = objectMapper.readValue(responses, new TypeReference<List<Answer>>() {
            });
            System.out.println(answerList);

            List<String> reviewers = formService.getReviewersForAForm(formId);

            if (fillerEmail != null) {


                if (!reviewers.contains(fillerEmail)) {
                    redirectAttributes.addFlashAttribute("flashError", "You are not allowed to fill this form");
                    return "redirect:/review/" + formId;
                }

                if (formService.getIfHasFilledForm(formId, fillerEmail)) {
                    redirectAttributes.addFlashAttribute("flashError", "You have already filled this form");
                    return "redirect:/review/" + formId;
                }
                formService.updateReviewersAfterSubmission(formId, fillerEmail, Relationship);
                answerList.forEach(answer -> answer.setUsername(fillerEmail));

            } else {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                String username = authentication.getName();
                if (formService.ifUserHasSelfReviewed(formId, username)) {
                    redirectAttributes.addFlashAttribute("flashError", "You have already filled this form");
                    System.out.println("this code is now working");

                    return "redirect:/review/" + formId;
                }
                answerList.forEach(answer -> answer.setUsername(username));
            }
            answerList.forEach(answer -> answer.setFormId(formId));
            String email = userservice.getUserByUserName(formService.getFormOwner(formId)).getEmail();

            AnswerServiceInter.processAndSaveAnswers(answerList);
            formService.updateReviewersAfterSubmission(formId, email, "self");
        }
        catch (JsonProcessingException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("flashError", "An error occurred while processing the form");
            return "redirect:/review/" + formId;
        }
        catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("flashError", "An error occurred while processing the form");
            return "redirect:/review/" + formId;
        }
        return "redirect:/account";
    }
    Boolean isOwner(Form form) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        if (authentication != null && authentication.getName() != null) {

            if (form.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }
}
