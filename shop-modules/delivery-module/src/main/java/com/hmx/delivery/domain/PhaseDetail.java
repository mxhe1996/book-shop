package com.hmx.delivery.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PhaseDetail{

    private Date phaseTime;

    private String comment;
}
