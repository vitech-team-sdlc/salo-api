package com.vitechteam.sdlc.scm;

public record File(String content, String commitSha, String path) {

  public File(String path) {
    this(null, null, path);
  }
}
