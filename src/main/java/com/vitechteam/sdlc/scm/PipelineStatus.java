package com.vitechteam.sdlc.scm;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PipelineStatus {
    long id;
    String status;
    String conclusion;
    String logsUrl;
    String commit;
}
