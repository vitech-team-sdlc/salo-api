package com.vitechteam.sdlc.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vitechteam.sdlc.template.model.SaloTemplate;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@Component
public class Templates {

    private static final Collection<String> PREDEFINED_TEMPLATES = List.of(
            "/templates/envs_3_single_cluster.json",
            "/templates/envs_4_single_cluster.json"
    );

    private final ObjectMapper objectMapper;

    public Collection<SaloTemplate> defaults() {
        return PREDEFINED_TEMPLATES
                .stream()
                .map(Templates.class::getResourceAsStream)
                .map(this::readTemplate)
                .toList();
    }

    @SneakyThrows
    private SaloTemplate readTemplate(InputStream stream) {
        return objectMapper.readValue(stream, SaloTemplate.class);
    }
}
