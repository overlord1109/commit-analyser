package com.overlord.gitstats.analyser.regex;

public class JavaMethodRegex {

    //Pattern to match Java methods. Package private constructors without a preceding whitespace are not captured in this regex
    public static final String JAVA_METHOD_PATTERN = "(public|protected|private|static|\\s)*[\\w<>\\[\\]]*\\s+(\\w+) *\\([^)]*\\) *(\\{?|[^;])";
}
