package com.example.bulkmailer;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import java.io.File;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class MailSender {
	private final SmtpConfig config;
	private final Session session;
	private long lastSentMillis = 0L;

	public MailSender(SmtpConfig config) {
		this.config = config;
		this.session = Session.getInstance(buildMailProperties(config), new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				if (config.username == null || config.username.isBlank()) return null;
				return new PasswordAuthentication(config.username, config.password);
			}
		});
	}

	public void sendHtmlMail(RecipientRecord recipient, String subject, String htmlBody) throws MessagingException {
		respectRateLimit();

		MimeMessage message = new MimeMessage(session);
		message.setFrom(new InternetAddress(config.fromAddress));
		message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient.to));
		if (recipient.cc != null && !recipient.cc.isBlank()) {
			message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(recipient.cc));
		}
		if (recipient.bcc != null && !recipient.bcc.isBlank()) {
			message.setRecipients(Message.RecipientType.BCC, InternetAddress.parse(recipient.bcc));
		}
		message.setSubject(subject);
		message.setSentDate(new Date());

		MimeBodyPart bodyPart = new MimeBodyPart();
		bodyPart.setContent(htmlBody, "text/html; charset=UTF-8");

		MimeMultipart multipart = new MimeMultipart();
		multipart.addBodyPart(bodyPart);

		if (recipient.attachments != null) {
			for (Path path : recipient.attachments) {
				MimeBodyPart attachment = new MimeBodyPart();
				File file = path.toFile();
				try {
					attachment.attachFile(file);
					multipart.addBodyPart(attachment);
				} catch (Exception e) {
					System.err.println("Failed to attach file: " + file + " - " + e.getMessage());
				}
			}
		}

		message.setContent(multipart);
		Transport.send(message);
		lastSentMillis = System.currentTimeMillis();
	}

	private void respectRateLimit() {
		if (config.rateLimitPerSecond <= 0) return;
		long minIntervalMs = 1000L / Math.max(1, config.rateLimitPerSecond);
		long now = System.currentTimeMillis();
		long elapsed = now - lastSentMillis;
		if (lastSentMillis != 0 && elapsed < minIntervalMs) {
			try {
				Thread.sleep(minIntervalMs - elapsed);
			} catch (InterruptedException ignored) {
				Thread.currentThread().interrupt();
			}
		}
	}

	private static Properties buildMailProperties(SmtpConfig config) {
		Properties props = new Properties();
		props.put("mail.transport.protocol", "smtp");
		props.put("mail.smtp.host", config.smtpHost);
		props.put("mail.smtp.port", String.valueOf(config.smtpPort));
		props.put("mail.smtp.auth", String.valueOf(config.username != null && !config.username.isBlank()));
		props.put("mail.smtp.starttls.enable", String.valueOf(config.startTls));
		props.put("mail.smtp.ssl.enable", String.valueOf(config.sslEnable));
		props.put("mail.mime.charset", "UTF-8");
		return props;
	}
}