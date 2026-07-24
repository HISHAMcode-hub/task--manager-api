# Bank Management Project Task Manager API

A lightweight Java project management API for guiding a Java bank management system with netbanking from planning to release.
It lists project tasks, adds team members, allocates work, tracks progress, supports team chat, stores deadlines, and returns deadline reminders.

## Admin access

Only the administrator needs to authenticate when adding project goals:

- Username: `admin@hisham`
- Password: `786`

Team members can be added and can use task, chat, notification, and project roadmap endpoints without login.

## Run

```bash
mvn exec:java
```

## Front end

Open `http://localhost:8080/` after running the app to use the `src/main/resources/static/index.html` dashboard for progress, members, task allocation, admin goals, reminders, roadmap, and chat.

## API overview

- `GET /api/project` — view members, goals, all tasks, progress percentage, notifications, and the roadmap template.
- `POST /api/admin/login` — validate admin credentials.
- `POST /api/admin/goals` — add admin-controlled goals with `X-Admin-Username` and `X-Admin-Password` headers.
- `POST /api/members` — add project members.
- `POST /api/tasks` — add and allocate tasks with a deadline and completion guide.
- `PATCH /api/tasks/{id}/status` — update progress by changing task status.
- `GET /api/notifications` — list task reminders for overdue or upcoming deadlines.
- `GET /api/chat` and `POST /api/chat` — read and send project messages.

## Project roadmap template

1. Requirements for accounts, customers, transfers, netbanking login, and audit rules.
2. Database schema, Java API services, validation, and role-based access.
3. Netbanking flows for balance, beneficiary management, fund transfer, statements, and notifications.
4. Security hardening with password hashing, OTP hooks, transaction limits, and audit logs.
5. Testing, UAT, deployment checklist, user guide, and post-launch monitoring.
