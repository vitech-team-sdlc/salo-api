package com.vitechteam.sdlc.scm;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Repository(String name, String organization, String branch, String url) {

    public static String DEFAULT_BRANCH = "main";

    public static final String NAME_PATTERN_STR = "^(?<repoKind>env|infra)-(?<saloName>.+)-(?<envName>\\w+)$";
    public static final Pattern NAME_PATTERN = Pattern.compile(NAME_PATTERN_STR);

    public Repository(String name, String organization) {
        this(name, organization, DEFAULT_BRANCH, "");
    }

    @JsonIgnore
    public String fullName() {
        return organization + "/" + name;
    }

    @JsonIgnore
    public String saloName() {
        final Matcher matcher = NAME_PATTERN.matcher(name);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Repository name doesn't match naming pattern: [" + name + "]");
        }
        return matcher.group("saloName");
    }
}
