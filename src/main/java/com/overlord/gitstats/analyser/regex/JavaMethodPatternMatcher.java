package com.overlord.gitstats.analyser.regex;

import java.util.regex.Pattern;

public class JavaMethodPatternMatcher {

    // Pattern to match Java method declarations. The motive of this regex is to identify commits that modified any lines that
    // match anything resembling java method declarations. These commits are candidates for deeper analysis by comparing
    // argument lists for methods obtained by parsing old and new Java classes.
    public static final String JAVA_METHOD_PATTERN = "[+\\-](?!.*if)(?!.*while)(?!.*do)(public|protected|private|static|\\s)*[\\w<>\\[\\]]*\\s+(\\w+) *\\([^)]*\\) *(\\{?|[^;])";

    private final Pattern pattern;

    public static JavaMethodPatternMatcher instance() {
        return new JavaMethodPatternMatcher();
    }

    public Pattern getPattern() {
        return this.pattern;
    }

    private JavaMethodPatternMatcher() {
        this.pattern = Pattern.compile(JAVA_METHOD_PATTERN);
    }

    public boolean matches(String input) {
        return pattern.matcher(input).find();
    }
}
