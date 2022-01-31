package com.vitechteam.sdlc.scm;

public record Repository(String name, String organization, String branch, String url) {

  public static String DEFAULT_BRANCH = "main";

  public Repository(String name, String organization) {
    this(name, organization, DEFAULT_BRANCH, "");
  }

  public String fullName() {
    return organization + "/" + name;
  }
}
