package com.projekat.report_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReportResolvedEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long reportId;
    private Long userId;
    private int pointsToAward;
}