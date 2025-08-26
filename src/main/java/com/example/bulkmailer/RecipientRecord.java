package com.example.bulkmailer;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class RecipientRecord {
	public final String to;
	public final String subject;
	public final String cc;
	public final String bcc;
	public final Map<String, String> variables;
	public final List<Path> attachments;

	public RecipientRecord(String to, String subject, String cc, String bcc, Map<String, String> variables, List<Path> attachments) {
		this.to = to;
		this.subject = subject;
		this.cc = cc;
		this.bcc = bcc;
		this.variables = variables;
		this.attachments = attachments;
	}
}