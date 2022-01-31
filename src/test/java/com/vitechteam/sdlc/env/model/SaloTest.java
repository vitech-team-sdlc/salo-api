package com.vitechteam.sdlc.env.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitechteam.sdlc.SaloTestHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

class SaloTest implements SaloTestHelper {

  @Test
  void testSerialize() throws JsonProcessingException {
    final Salo serialization = newDummySalo("serialization");
    final String json = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .writerWithDefaultPrettyPrinter()
      .writeValueAsString(serialization);
    Assertions.assertFalse(json.trim().isEmpty());
    System.out.println(json);
  }
}
