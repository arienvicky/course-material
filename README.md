# in28Minutes - Course Materials

Thank You for Choosing to Learn from in28Minutes.

## Bulk Mailer (Java)

Send personalized HTML emails in bulk using an Excel (.xlsx) sheet for recipients and variables, with per-recipient attachments.

### Features
- Read recipients and variables from `.xlsx` (first sheet). Headers define fields.
- Special headers (case-insensitive): `email`/`to`, `subject`, `cc`, `bcc`, `attachments` (semicolon or comma-separated file paths).
- Remaining headers are exposed as template variables like `${name}`.
- HTML templating via Apache Commons Text.
- Supports STARTTLS/SSL, auth, and rate limiting.
- Dry-run mode to preview without sending.

### Requirements
- Java 17+
- Maven 3.8+

### Build
```bash
mvn -q -f /workspace/pom.xml package
```

### Usage
```bash
java -jar target/bulk-mailer-1.0.0.jar /workspace/examples/config.properties /path/to/recipients.xlsx /workspace/examples/template.html --dry-run
```

- Remove `--dry-run` to actually send.
- Ensure attachment paths in the Excel file are absolute or relative to the current working directory.

### Excel format
First row must be headers. Example headers:
- `email`, `subject`, `name`, `order_id`, `attachments`

Attachment cell example:
```
/path/a.pdf; /path/b.png
```

### Config properties
```
smtp.host=...
smtp.port=587
smtp.username=...
smtp.password=...
smtp.starttls=true
smtp.ssl=false
mail.from=Sender Name <noreply@example.com>
mail.subject=Default Subject
rate.limit.per.second=2
```

### Run with Maven Exec (optional)
```bash
mvn -q -f /workspace/pom.xml exec:java -Dexec.args="/workspace/examples/config.properties /path/to/recipients.xlsx /workspace/examples/template.html --dry-run"
```

### Notes
- For `.xls` legacy format, convert to `.xlsx` first, or extend `ExcelRecipientReader` to use Apache POI HSSF.
- Test with a small set and your SMTP sandbox before sending to all recipients.