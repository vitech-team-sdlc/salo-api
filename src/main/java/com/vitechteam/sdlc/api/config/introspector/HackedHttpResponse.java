package com.vitechteam.sdlc.api.config.introspector;

import lombok.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.io.InputStream;

@Value
class HackedHttpResponse implements ClientHttpResponse {

  ClientHttpResponse response;
  InputStream responseBody;

  @Override
  public HttpStatus getStatusCode() throws IOException {
    return getResponse().getStatusCode();
  }

  @Override
  public int getRawStatusCode() throws IOException {
    return getResponse().getRawStatusCode();
  }

  @Override
  public String getStatusText() throws IOException {
    return getResponse().getStatusText();
  }

  @Override
  public void close() {
    getResponse().close();
  }

  @Override
  public InputStream getBody() throws IOException {
    return this.responseBody;
  }

  @Override
  public HttpHeaders getHeaders() {
    return getResponse().getHeaders();
  }
}
