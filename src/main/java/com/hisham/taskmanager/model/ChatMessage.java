package com.hisham.taskmanager.model;

import java.time.Instant;

public record ChatMessage(long id, String author, String message, Instant sentAt) {
}
