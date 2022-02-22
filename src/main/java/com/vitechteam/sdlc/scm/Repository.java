package com.vitechteam.sdlc.scm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vitechteam.sdlc.env.model.Environment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Repository(String name, String organization, String branch, String url) {

    public static String DEFAULT_BRANCH = "main";

    public static final String NAME_FORMAT = "%s-%s-%s";

    public static final String NAME_PATTERN_STR = "^(?<kind>env|infra)-(?<saloName>.+)-(?<envKey>\\w+)$";
    public static final Pattern NAME_PATTERN = Pattern.compile(NAME_PATTERN_STR);

    public Repository(String name, String organization) {
        this(name, organization, DEFAULT_BRANCH, "");
    }

    public static Repository envRepo(String name, String envKey) {
        return new Repository(NAME_FORMAT.formatted("env", name, envKey), "");
    }

    public static Repository infraRepo(String name, String envKey) {
        return new Repository(NAME_FORMAT.formatted("infra", name, envKey), "");
    }

    @JsonIgnore
    public String fullName() {
        return organization + "/" + name;
    }

    @JsonIgnore
    public String kind() {
        return extractFromName("kind");
    }

    @JsonIgnore
    public String saloName() {
        return extractFromName("saloName");
    }

    @JsonIgnore
    public String envKey() {
        return extractFromName("envKey");
    }

    @JsonIgnore
    public boolean isDev() {
        return Environment.DEV_ENV_KEY.equals(envKey());
    }

    @JsonIgnore
    public boolean isSaloInstallation() {
        return NAME_PATTERN.matcher(name).matches();
    }

    private String extractFromName(String group) {
        final Matcher matcher = NAME_PATTERN.matcher(name);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Repository name doesn't match Salo naming pattern: [" + name + "]");
        }
        return matcher.group(group);
    }
}
