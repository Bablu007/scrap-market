package com.scrapmarket.scrap_market.common;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class MailService {
    Logger logger = org.slf4j.LoggerFactory.getLogger(MailService.class);
    private TemplateEngine templateEngine;
    private final int TIMEOUT = 5; // OTP validity duration
    public MailService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }
    public boolean sendOtpEmail(JavaMailSender mailSender, String toEmail, String otp) {
        try {
            // Set up Thymeleaf context and variables
            Context context = new Context();
            Map<String, Object> variables = new HashMap<>();
            variables.put("name",toEmail);
            variables.put("otp", otp);
            variables.put("time", TIMEOUT);// include OTP in the template
            context.setVariables(variables);
            // Render HTML from template
            String htmlContent = templateEngine.process("email-template", context);
            // Create and send email
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("Your OTP Code");
            helper.setText(htmlContent, true); // true = HTML
            helper.setFrom("babluagrahari07@gmail.com"); // optional but recommended
            mailSender.send(message);
            return true;
        } catch (Exception e) {
            logger.error("Failed to send email: {}", e.getMessage());
            return false;
        }
    }
}