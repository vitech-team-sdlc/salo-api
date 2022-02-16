package com.vitechteam.sdlc.env.model.cluster;

import lombok.Builder;
import lombok.Value;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.Collection;
import java.util.List;

@Value
@Builder
public class NodeGroup {
    String name;
    @Positive
    int maxSize;
    @Positive
    int minSize;
    @PositiveOrZero
    int spotSize;
    @Builder.Default
    Collection<Label> labels = List.of();
    @Builder.Default
    Collection<Taint> taints = List.of();
    @Builder.Default
    Collection<Tag> tags = List.of();
    @Positive
    int volumeSize;
    @Builder.Default
    Collection<String> vmTypes = List.of();
}
