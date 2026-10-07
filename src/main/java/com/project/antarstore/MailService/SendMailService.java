package com.project.antarstore.MailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.project.antarstore.Model.Users;

@Service
public class SendMailService {

	@Autowired
	private JavaMailSender javaMailSender;

	// FIX: "jakarta.mail.MessagingException: can't determine local email
	// address" happens when a message has no explicit From address -
	// JavaMail then tries to auto-detect one from the local machine's
	// hostname/user and fails (common on Windows, especially after network
	// changes). Explicitly setting From removes the guesswork entirely.
	@Value("${spring.mail.username}")
	private String mailFrom;

	public void sendOtpMail(Users user) throws Exception {
		SimpleMailMessage mailMessage = new SimpleMailMessage();

		String subject = "Welcome to AntarStore - Verify your OTP";
		String message = "Hello " + user.getName() + ",\n\n"
				+ "Welcome to AntarStore.\n"
				+ "Use the following OTP to complete your verification:\n\n"
				+ "OTP: " + user.getOtp() + "\n\n"
				+ "This OTP will expire in 5 minutes. For security reasons, never share this OTP with anyone.\n\n"
				+ "Thank you,\nTeam AntarStore";
		mailMessage.setFrom(mailFrom);
		mailMessage.setSubject(subject);
		mailMessage.setText(message);
		mailMessage.setTo(user.getEmail());
		javaMailSender.send(mailMessage);
	}
}
