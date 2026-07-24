package com.hisham.taskmanager.model;

import java.time.LocalDate;

public record TaskItem(
        long id,
        String title,
        String description,
        TaskStatus status,
        Long assigneeId,
        LocalDate deadline,
        String completionGuide) {
}
