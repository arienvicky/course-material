package com.example.bulkmailer;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

public class SmtpConfig {
	public final String smtpHost;
	public final int smtpPort;
	public final String username;
	public final String password;
	public final String fromAddress;
	public final String defaultSubject;
	public final boolean startTls;
	public final boolean sslEnable;
	public final int rateLimitPerSecond;

	private SmtpConfig(String smtpHost, int smtpPort, String username, String password, String fromAddress, String defaultSubject, boolean startTls, boolean sslEnable, int rateLimitPerSecond) {
		this.smtpHost = smtpHost;
		this.smtpPort = smtpPort;
		this.username = username;
		this.password = password;
		this.fromAddress = fromAddress;
		this.defaultSubject = defaultSubject;
		this.startTls = startTls;
		this.sslEnable = sslEnable;
		this.rateLimitPerSecond = rateLimitPerSecond;
	}

	public static SmtpConfig load(Path propertiesPath) throws IOException {
		Properties properties = new Properties();
		try (FileInputStream fis = new FileInputStream(propertiesPath.toFile())) {
			properties.load(fis);
		}

		String host = required(properties, "smtp.host");
		int port = Integer.parseInt(properties.getProperty("smtp.port", "587"));
		String user = properties.getProperty("smtp.username", "");
		String pass = properties.getProperty("smtp.password", "");
		String from = required(properties, "mail.from");
		String subject = properties.getProperty("mail.subject", "");
		boolean starttls = Boolean.parseBoolean(properties.getProperty("smtp.starttls", "true"));
		boolean ssl = Boolean.parseBoolean(properties.getProperty("smtp.ssl", "false"));
		int rate = Integer.parseInt(properties.getProperty("rate.limit.per.second", "0"));

		return new SmtpConfig(host, port, user, pass, from, subject, starttls, ssl, rate);
	}

	private static String required(Properties p, String key) {
		String v = p.getProperty(key);
		if (v == null || v.isBlank()) {
			throw new IllegalArgumentException("Missing required property: " + key);
		}
		return v.trim();
	}
}