package com.example.bulkmailer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Main {
	public static void main(String[] args) throws Exception {
		if (args.length < 3) {
			System.out.println("Usage: java -jar bulk-mailer.jar <config.properties> <recipients.xlsx> <template.html> [--dry-run]");
			System.exit(1);
		}
		Path configPath = Path.of(args[0]);
		Path recipientsPath = Path.of(args[1]);
		Path templatePath = Path.of(args[2]);
		boolean dryRun = args.length >= 4 && "--dry-run".equalsIgnoreCase(args[3]);

		SmtpConfig config = SmtpConfig.load(configPath);
		String template = Files.readString(templatePath);

		List<RecipientRecord> recipients = ExcelRecipientReader.read(recipientsPath);
		System.out.println("Loaded recipients: " + recipients.size());

		MailSender mailSender = new MailSender(config);

		int sent = 0;
		int failed = 0;
		for (RecipientRecord recipient : recipients) {
			String subject = recipient.subject != null && !recipient.subject.isBlank() ? recipient.subject : config.defaultSubject;
			String html = TemplateRenderer.render(template, recipient.variables);
			if (dryRun) {
				System.out.printf("[DRY-RUN] To=%s Subject=%s Attachments=%d\n", recipient.to, subject, recipient.attachments == null ? 0 : recipient.attachments.size());
				continue;
			}
			try {
				mailSender.sendHtmlMail(recipient, subject, html);
				sent++;
				System.out.println("Sent: " + recipient.to);
			} catch (Exception e) {
				failed++;
				System.err.println("Failed: " + recipient.to + " - " + e.getMessage());
			}
		}

		System.out.printf("Done. Sent=%d Failed=%d\n", sent, failed);
	}
}