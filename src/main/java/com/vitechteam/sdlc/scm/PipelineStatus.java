package com.vitechteam.sdlc.scm;

public record PipelineStatus(
        String status,
        String conclusion,
        String logsUrl,
        String commit
) {
}
