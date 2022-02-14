package com.vitechteam.sdlc.env.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitechteam.sdlc.TestFixtures;
import com.vitechteam.sdlc.testConfig.SmallTest;
import org.junit.jupiter.api.Assertions;

class SaloTest implements TestFixtures {

    @SmallTest
    void testSerialize() throws JsonProcessingException {
        final Salo serialization = newSalo("serialization", "serialization-sdlc", "client-id", "secret");
        final String json = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(serialization);
        Assertions.assertFalse(json.trim().isEmpty());
    }
}
