package com.iyte_yazilim.proje_pazari.application.dtos;

import java.time.LocalDate;
import java.util.List;

public record AnalyticsTrendsDTO(List<DailyDataPoint> dataPoints, int totalDays) {

    public record DailyDataPoint(
            LocalDate date, long newUsers, long newProjects, long newApplications) {}
}
