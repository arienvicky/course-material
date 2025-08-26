package com.example.bulkmailer;

import org.apache.commons.text.StringSubstitutor;

import java.util.HashMap;
import java.util.Map;

public class TemplateRenderer {
	public static String render(String template, Map<String, String> variables) {
		Map<String, String> safe = new HashMap<>();
		if (variables != null) safe.putAll(variables);
		StringSubstitutor substitutor = new StringSubstitutor(safe);
		substitutor.setEnableUndefinedVariableException(false);
		return substitutor.replace(template);
	}
}