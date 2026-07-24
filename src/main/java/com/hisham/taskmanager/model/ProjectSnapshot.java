package com.hisham.taskmanager.model;

import java.util.List;

public record ProjectSnapshot(
        String projectName,
        String domain,
        int progressPercent,
        List<Member> members,
        List<ProjectGoal> goals,
        List<TaskItem> tasks,
        List<String> notifications,
        List<String> roadmap) {
}
