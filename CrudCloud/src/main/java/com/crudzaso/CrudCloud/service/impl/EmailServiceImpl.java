package com.crudzaso.CrudCloud.service.impl;

// import com.crudzaso.CrudCloud.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Implementation of EmailService using Gmail SMTP.
 *
 * Sends email notifications for:
 * - User registration
 * - Instance creation
 * - Password rotation
 * - Plan upgrades
 * - Errors
 *
 * CONFIGURATION:
 * Set environment variables (not hardcoded):
 * - GMAIL_USERNAME: Gmail address
 * - GMAIL_APP_PASSWORD: App-specific password (not regular password)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl {
	// COMENTADO TEMPORALMENTE PARA EVITAR CONFLICTOS CON EMAILSERVICE
	// Usar EmailService directamente desde service/EmailService.java

	/*
	private final JavaMailSender mailSender;

	@Value("${mail.from:noreply@crudcloud.com}")
	private String fromEmail;

	@Value("${mail.from-name:CrudCloud}")
	private String fromName;

	@Override
	public void sendWelcomeEmail(String userEmail, String userName) {
		// Implementation here
	}
	*/
}
