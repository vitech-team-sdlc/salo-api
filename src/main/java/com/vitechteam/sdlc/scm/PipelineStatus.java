package com.vitechteam.sdlc.scm;

public record PipelineStatus(
        long id,
        String status,
        String conclusion,
        String logsUrl,
        String commit
) {
}
