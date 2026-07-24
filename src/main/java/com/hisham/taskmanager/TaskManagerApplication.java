package com.hisham.taskmanager;

import com.hisham.taskmanager.model.*;
import com.hisham.taskmanager.service.ProjectService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class TaskManagerApplication {
    private final ProjectService service = new ProjectService(Clock.systemUTC());

    public static void main(String[] args) throws IOException {
        new TaskManagerApplication().start(8080);
    }

    void start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", this::index);
        server.createContext("/api/project", exchange -> send(exchange, 200, Json.toJson(service.snapshot())));
        server.createContext("/api/notifications", exchange -> send(exchange, 200, Json.toJson(service.notifications())));
        server.createContext("/api/chat", this::chat);
        server.createContext("/api/members", this::members);
        server.createContext("/api/tasks", this::tasks);
        server.createContext("/api/admin/login", this::adminLogin);
        server.createContext("/api/admin/goals", this::goals);
        server.start();
        System.out.println("Task manager API running at http://localhost:" + port + "/api/project");
    }

    private void index(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if (!"/".equals(path) && !"/index.html".equals(path)) {
            send(exchange, 404, "{\"error\":\"Not found\"}");
            return;
        }
        try (InputStream page = TaskManagerApplication.class.getResourceAsStream("/static/index.html")) {
            byte[] response = page.readAllBytes();
            exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        }
    }

    private void adminLogin(HttpExchange exchange) throws IOException {
        Map<String, String> body = Json.object(requestBody(exchange));
        if (!service.adminLogin(body.get("username"), body.get("password"))) {
            send(exchange, 401, "{\"error\":\"Invalid admin credentials\"}");
            return;
        }
        send(exchange, 200, "{\"authenticated\":true,\"message\":\"Admin can add goals and monitor the project.\"}");
    }

    private void goals(HttpExchange exchange) throws IOException {
        if (!service.adminLogin(exchange.getRequestHeaders().getFirst("X-Admin-Username"), exchange.getRequestHeaders().getFirst("X-Admin-Password"))) {
            send(exchange, 401, "{\"error\":\"Only admin@hisham can add goals\"}");
            return;
        }
        Map<String, String> body = Json.object(requestBody(exchange));
        send(exchange, 200, Json.toJson(service.addGoal(body.get("title"), body.get("description"), Boolean.parseBoolean(body.getOrDefault("completed", "false")))));
    }

    private void members(HttpExchange exchange) throws IOException {
        Map<String, String> body = Json.object(requestBody(exchange));
        send(exchange, 200, Json.toJson(service.addMember(body.get("name"), body.get("email"), body.get("role"))));
    }

    private void tasks(HttpExchange exchange) throws IOException {
        if ("PATCH".equals(exchange.getRequestMethod()) && exchange.getRequestURI().getPath().matches("/api/tasks/\\d+/status")) {
            long id = Long.parseLong(exchange.getRequestURI().getPath().split("/")[3]);
            Map<String, String> statusBody = Json.object(requestBody(exchange));
            send(exchange, 200, Json.toJson(service.updateTaskStatus(id, TaskStatus.valueOf(statusBody.get("status")))));
            return;
        }
        Map<String, String> body = Json.object(requestBody(exchange));
        TaskItem task = service.addTask(body.get("title"), body.get("description"), TaskStatus.valueOf(body.getOrDefault("status", "TODO")),
                body.containsKey("assigneeId") ? Long.valueOf(body.get("assigneeId")) : null,
                body.containsKey("deadline") ? LocalDate.parse(body.get("deadline")) : null,
                body.get("completionGuide"));
        send(exchange, 200, Json.toJson(task));
    }

    private void chat(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            send(exchange, 200, Json.toJson(service.chat()));
            return;
        }
        Map<String, String> body = Json.object(requestBody(exchange));
        send(exchange, 200, Json.toJson(service.addChat(body.get("author"), body.get("message"))));
    }

    private String requestBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private void send(HttpExchange exchange, int status, String json) throws IOException {
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }

    static class Json {
        static Map<String, String> object(String raw) {
            Map<String, String> values = new LinkedHashMap<>();
            String inner = raw.trim().replaceAll("^\\{|}$", "");
            if (inner.isBlank()) return values;
            for (String pair : inner.split(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)")) {
                String[] parts = pair.split(":", 2);
                values.put(clean(parts[0]), clean(parts[1]));
            }
            return values;
        }

        static String toJson(Object value) {
            if (value == null) return "null";
            if (value instanceof String text) return "\"" + text.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
            if (value instanceof Number || value instanceof Boolean) return value.toString();
            if (value instanceof Iterable<?> items) {
                StringBuilder out = new StringBuilder("[");
                boolean first = true;
                for (Object item : items) {
                    if (!first) out.append(',');
                    out.append(toJson(item));
                    first = false;
                }
                return out.append(']').toString();
            }
            return recordToJson(value);
        }

        private static String recordToJson(Object record) {
            StringBuilder out = new StringBuilder("{");
            boolean first = true;
            for (var component : record.getClass().getRecordComponents()) {
                try {
                    if (!first) out.append(',');
                    out.append(toJson(component.getName())).append(':').append(toJson(component.getAccessor().invoke(record)));
                    first = false;
                } catch (ReflectiveOperationException e) {
                    throw new IllegalStateException(e);
                }
            }
            return out.append('}').toString();
        }

        private static String clean(String value) {
            return value.trim().replaceAll("^\\\"|\\\"$", "");
        }
    }
}
