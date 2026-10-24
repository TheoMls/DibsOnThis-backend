# DibsOnThis Backend API

Kotlin & Ktor RESTful API server for managing users, groups, and task distribution in the **DibsOnThis** application.

---

## Tech Stack

* **Language:** Kotlin
* **Framework:** Ktor (Server Engine: Netty)
* **Serialization:** kotlinx.serialization (JSON)
* **Target Port:** `8080`

---

## Getting Started

### Prerequisites

* JDK 17 or higher
* Gradle (or use the included `gradlew` wrapper)

### Running Locally

```bash
# Clone the repository
git clone https://github.com/your-username/DibsOnThis-backend.git
cd DibsOnThis-backend

# Run the server
./gradlew run
```

The server will start at `http://localhost:8080` (or `http://<YOUR_LOCAL_IP>:8080` for physical mobile client testing).

---

## API Endpoints Summary

### General
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `GET` | `/` | Health check / Server status ping |

---

### User Routes (`/users`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/users` | Create / Register a new user |
| `GET` | `/users/me` | Fetch authenticated user profile |
| `GET` | `/users/{id}` | Fetch user details by ID *(Testing/Dev)* |

---

### Group Routes (`/groups`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/groups` | Create a new group |
| `POST` | `/groups/join` | Join a group via invite code |
| `POST` | `/groups/{groupId}/leave` | Leave a group |
| `GET` | `/groups/{groupId}` | Get group info, members, and details |

---

### Task Routes (`/tasks` & `/groups/{groupId}/tasks`)
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/groups/{groupId}/tasks` | Create a task within a group |
| `GET` | `/groups/{groupId}/tasks` | Get all tasks belonging to a group |
| `GET` | `/tasks/{taskId}` | Get details of a specific task |
| `PATCH` | `/tasks/{taskId}` | Update task status (`OPEN`, `CLAIMED`, `COMPLETED`) |
| `DELETE` | `/tasks/{taskId}` | Delete a task |

---

## Testing Request Examples

### Create User
```json
// POST /users
{
  "username": "theod",
  "email": "user@example.com"
}
```

### Create Task
```json
// POST /groups/{groupId}/tasks
{
  "title": "Buy groceries",
  "description": "Milk and eggs"
}
```

### Claim / Update Task Status
```json
// PATCH /tasks/{taskId}
{
  "status": "CLAIMED"
}
```