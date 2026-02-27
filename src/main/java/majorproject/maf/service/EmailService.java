package majorproject.maf.service;

import lombok.RequiredArgsConstructor;
import majorproject.maf.model.Alert;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        String subject = "Your MAFA Verification Code";
        String message = "Your OTP for email verification is: " + otp + "\n\nThis code is valid for 5 minutes.";

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(toEmail);
        mailMessage.setSubject(subject);
        mailMessage.setText(message);

        mailSender.send(mailMessage);
    }

    @Async("emailTaskExecutor")
    public void sendAlertEmail(Alert alert) {
        try {
            // 1. Determine the phrasing based on the condition
            String conditionText = alert.getAlertCondition().name().equals("ABOVE")
                    ? "risen above"
                    : "dropped below";

            // 2. Draft the Subject
            String subject = String.format("Stock Alert Triggered: %s has %s %s",
                    alert.getSymbol(), conditionText, alert.getTargetPrice());

            // 3. Draft the Body
            SimpleMailMessage mailMessage = getSimpleMailMessage(alert, subject);

            mailSender.send(mailMessage);

            System.out.println("Alert email sent successfully to: " + alert.getUserEmail());

        } catch (Exception e) {
            // Catching exceptions is critical in @Async methods. 
            // If the thread crashes, it won't crash your main checkAlerts loop.
            System.err.println("Failed to send alert email to " + alert.getUserEmail() + ": " + e.getMessage());
        }
    }

    private static SimpleMailMessage getSimpleMailMessage(Alert alert, String subject) {
        String body = String.format(
                """
                        Hello,
                        
                        Your custom alert has been triggered!
                        
                        Stock Symbol: %s
                        Condition Met: %s %.2f
                        Status: TRIGGERED
                        
                        Log in to your MAFA dashboard to take action.
                        
                        Best regards,
                        MAFA Alerts Team""",
                alert.getSymbol(), alert.getAlertCondition(), alert.getTargetPrice()
        );

        // 4. Build and Send the Message
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(alert.getUserEmail());
        mailMessage.setSubject(subject);
        mailMessage.setText(body);
        return mailMessage;
    }

}
