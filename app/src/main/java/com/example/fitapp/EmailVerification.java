package com.example.fitapp;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailVerification {

    // Method to send an email
    public static void sendEmail(String recipient, String subject, String content) {

        // Configure email properties
        Properties properties = new Properties();

        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.ssl.trust", "*");

        // Sender email credentials
        String myAccount = "letsfit2024mad@gmail.com";
        String password = "letsfit2024";

        // Create a session with the sender's credentials
        Session session = Session.getInstance(properties, new Authenticator() {

            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(myAccount, password);

            }
        });

        // Prepare the email message
        Message msg = prepareMessage(session, myAccount, recipient, subject, content);

        try {
            // Send the email
            Transport.send(msg);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    // Method to prepare an email message
    private static Message prepareMessage(Session session, String myAccountEmail, String recepient, String subject, String content) {
        Message message = new MimeMessage(session);
        try {
            // Set sender and recipient email addresses, subject, and content
            message.setFrom(new InternetAddress(myAccountEmail));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recepient));
            message.setSubject(subject);
            message.setText(content);

            return message;

        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }

        return null;
    }
}
