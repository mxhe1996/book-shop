package com.hmx.kafka.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Outbox {

    private Long id;

    private String uuid;

    private String topicName;

    private String eventBody;

}
