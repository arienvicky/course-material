package com.example.bulkmailer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ExcelRecipientReader {
	public static List<RecipientRecord> read(Path excelPath) throws IOException {
		try (FileInputStream fis = new FileInputStream(excelPath.toFile()); Workbook workbook = WorkbookFactory.create(fis)) {
			Sheet sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();
			if (!rowIterator.hasNext()) {
				return Collections.emptyList();
			}

			Row headerRow = rowIterator.next();
			List<String> headers = new ArrayList<>();
			for (Cell cell : headerRow) {
				headers.add(getCellString(cell).toLowerCase(Locale.ROOT).trim());
			}

			int emailIdx = indexOfHeader(headers, List.of("email", "to"));
			int subjectIdx = indexOfHeader(headers, List.of("subject"));
			int ccIdx = indexOfHeader(headers, List.of("cc"));
			int bccIdx = indexOfHeader(headers, List.of("bcc"));
			int attachmentsIdx = indexOfHeader(headers, List.of("attachments", "attachment_paths"));

			List<RecipientRecord> results = new ArrayList<>();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				if (rowIsEmpty(row)) continue;

				String to = getCellString(row.getCell(emailIdx));
				if (to == null || to.isBlank()) continue;

				String subject = subjectIdx >= 0 ? getCellString(row.getCell(subjectIdx)) : null;
				String cc = ccIdx >= 0 ? getCellString(row.getCell(ccIdx)) : null;
				String bcc = bccIdx >= 0 ? getCellString(row.getCell(bccIdx)) : null;

				Map<String, String> variables = new HashMap<>();
				for (int i = 0; i < headers.size(); i++) {
					if (i == emailIdx || i == subjectIdx || i == ccIdx || i == bccIdx || i == attachmentsIdx) continue;
					String key = headers.get(i);
					String val = getCellString(row.getCell(i));
					if (val != null) variables.put(key, val);
				}

				List<Path> attachments = new ArrayList<>();
				if (attachmentsIdx >= 0) {
					String raw = getCellString(row.getCell(attachmentsIdx));
					if (raw != null && !raw.isBlank()) {
						for (String piece : raw.split("[;,]")) {
							String trimmed = piece.trim();
							if (!trimmed.isEmpty()) {
								Path p = Path.of(trimmed);
								if (Files.exists(p) && Files.isRegularFile(p)) {
									attachments.add(p);
								}
							}
						}
					}
				}

				results.add(new RecipientRecord(to, subject, cc, bcc, variables, attachments));
			}

			return results;
		}
	}

	private static int indexOfHeader(List<String> headers, List<String> options) {
		for (String option : options) {
			int idx = headers.indexOf(option.toLowerCase(Locale.ROOT));
			if (idx >= 0) return idx;
		}
		return -1;
	}

	private static String getCellString(Cell cell) {
		if (cell == null) return null;
		switch (cell.getCellType()) {
			case STRING:
				return cell.getStringCellValue().trim();
			case NUMERIC:
				return String.valueOf((long) cell.getNumericCellValue());
			case BOOLEAN:
				return String.valueOf(cell.getBooleanCellValue());
			case FORMULA:
				try {
					return cell.getStringCellValue().trim();
				} catch (Exception e) {
					return String.valueOf(cell.getNumericCellValue());
				}
			default:
				return null;
		}
	}

	private static boolean rowIsEmpty(Row row) {
		if (row == null) return true;
		for (Cell cell : row) {
			String s = getCellString(cell);
			if (s != null && !s.isBlank()) return false;
		}
		return true;
	}
}