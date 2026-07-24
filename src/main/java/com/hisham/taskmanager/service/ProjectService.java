package com.hisham.taskmanager.service;

import com.hisham.taskmanager.model.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class ProjectService {
    private static final String ADMIN_USERNAME = "admin@hisham";
    private static final String ADMIN_PASSWORD = "786";

    private final Clock clock;
    private final AtomicLong memberIds = new AtomicLong(2);
    private final AtomicLong taskIds = new AtomicLong(4);
    private final AtomicLong goalIds = new AtomicLong(4);
    private final AtomicLong chatIds = new AtomicLong(1);
    private final Map<Long, Member> members = new LinkedHashMap<>();
    private final Map<Long, TaskItem> tasks = new LinkedHashMap<>();
    private final Map<Long, ProjectGoal> goals = new LinkedHashMap<>();
    private final List<ChatMessage> chat = new ArrayList<>();
    private final List<String> roadmap = List.of(
            "Phase 1: Requirements for accounts, customers, transfers, netbanking login, and audit rules",
            "Phase 2: Database schema, Java services, validation, and role-based access",
            "Phase 3: Netbanking flows for balance, beneficiary management, fund transfer, statements, and notifications",
            "Phase 4: Security hardening with password hashing, OTP hooks, transaction limits, and audit logs",
            "Phase 5: Testing, UAT, deployment checklist, user guide, and post-launch monitoring");

    public ProjectService(Clock clock) {
        this.clock = clock;
        seed();
    }

    public boolean adminLogin(String username, String password) {
        return ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password);
    }

    public synchronized ProjectSnapshot snapshot() {
        return new ProjectSnapshot("Java Bank Management System with Netbanking", "Banking", progressPercent(),
                List.copyOf(members.values()), List.copyOf(goals.values()), List.copyOf(tasks.values()), notifications(), roadmap);
    }

    public synchronized Member addMember(String name, String email, String role) {
        Member member = new Member(memberIds.getAndIncrement(), name, email, role);
        members.put(member.id(), member);
        return member;
    }

    public synchronized TaskItem addTask(String title, String description, TaskStatus status, Long assigneeId, LocalDate deadline, String completionGuide) {
        TaskItem task = new TaskItem(taskIds.getAndIncrement(), title, description, Optional.ofNullable(status).orElse(TaskStatus.TODO), assigneeId, deadline, completionGuide);
        tasks.put(task.id(), task);
        return task;
    }

    public synchronized TaskItem updateTaskStatus(long id, TaskStatus status) {
        TaskItem task = Optional.ofNullable(tasks.get(id)).orElseThrow();
        TaskItem updated = new TaskItem(task.id(), task.title(), task.description(), status, task.assigneeId(), task.deadline(), task.completionGuide());
        tasks.put(id, updated);
        return updated;
    }

    public synchronized ProjectGoal addGoal(String title, String description, boolean completed) {
        ProjectGoal goal = new ProjectGoal(goalIds.getAndIncrement(), title, description, completed);
        goals.put(goal.id(), goal);
        return goal;
    }

    public synchronized ChatMessage addChat(String author, String message) {
        ChatMessage chatMessage = new ChatMessage(chatIds.getAndIncrement(), author, message, Instant.now(clock));
        chat.add(chatMessage);
        return chatMessage;
    }

    public synchronized List<ChatMessage> chat() { return List.copyOf(chat); }

    public synchronized List<String> notifications() {
        LocalDate today = LocalDate.now(clock);
        return tasks.values().stream()
                .filter(task -> task.status() != TaskStatus.DONE && task.deadline() != null && !task.deadline().isAfter(today.plusDays(3)))
                .map(task -> "Reminder: '%s' is due on %s. Follow guide: %s".formatted(task.title(), task.deadline(), task.completionGuide()))
                .toList();
    }

    private int progressPercent() {
        if (tasks.isEmpty()) return 0;
        long done = tasks.values().stream().filter(task -> task.status() == TaskStatus.DONE).count();
        return (int) Math.round((done * 100.0) / tasks.size());
    }

    private void seed() {
        members.put(1L, new Member(1, "Project Admin", "admin@hisham", "Admin / Project Manager"));
        goals.put(1L, new ProjectGoal(1, "Complete bank management core modules", "Accounts, customers, deposits, withdrawals, and audit history", false));
        goals.put(2L, new ProjectGoal(2, "Deliver secure netbanking", "Login, beneficiary setup, transfers, statements, and notifications", false));
        goals.put(3L, new ProjectGoal(3, "Prepare release documentation", "Roadmap, test plan, deployment notes, and user guide", false));
        addTask("Define banking requirements", "List functional and security requirements", TaskStatus.IN_PROGRESS, 1L, LocalDate.now(clock).plusDays(2), "Confirm scope with stakeholders and convert each requirement into a testable checklist.");
        addTask("Design netbanking authentication", "Plan login, password policy, and OTP integration points", TaskStatus.TODO, 1L, LocalDate.now(clock).plusDays(5), "Create sequence diagrams and identify validation/error messages.");
        addTask("Create project roadmap template", "Publish phases, milestones, and release checklist", TaskStatus.DONE, 1L, LocalDate.now(clock).minusDays(1), "Keep roadmap updated after every sprint review.");
    }
}
