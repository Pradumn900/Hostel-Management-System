# 🏠 Hostel Management System

A comprehensive hostel management system for wardens to manage students, attendance, complaints, and fees.

## 📋 Features

- **🔐 Warden Authentication** – Secure JWT-based login
- **👨‍🎓 Student Management** – Add/Edit/Delete students with full personal details (name, father's name, registration no., phone, email, address)
- **🔍 Search by Registration No.** – Quickly find students by their Reg. No.
- **🚪 Room Management** – Scalable room system (add as many rooms as needed), 3 students per room by default
- **✅ Daily Attendance** – Room-based widget view (4–6 rooms per page), mark Present/Absent per student, shows live present count
- **💰 Fee Tracking** – Add/update fee records with status (Paid/Unpaid/Partial), filter by status
- **📋 Complaint Tracking** – Register complaints (with optional student link), mark as In Progress / Resolved

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Frontend | HTML5, CSS3, Vanilla JavaScript |
| Backend | Java 17, Spring Boot 3.2, Spring Security, Spring Data JPA |
| Database | MySQL 8.x |
| Auth | JWT (JSON Web Tokens) |
| Build | Apache Maven |

## 📁 Project Structure

```
hostel-management/
└── backend/
    ├── pom.xml
    └── src/
        ├── main/
        │   ├── java/com/hostel/management/
        │   │   ├── HostelManagementApplication.java
        │   │   ├── config/          # SecurityConfig, DataInitializer
        │   │   ├── controller/      # REST API controllers
        │   │   ├── dto/             # Request/Response DTOs
        │   │   ├── model/           # JPA entities
        │   │   ├── repository/      # Spring Data repositories
        │   │   ├── security/        # JWT provider & filter
        │   │   └── service/         # Business logic
        │   └── resources/
        │       ├── application.properties
        │       └── static/          # Frontend (HTML/CSS/JS)
        │           ├── index.html   → redirects to login
        │           ├── login.html
        │           ├── dashboard.html
        │           ├── attendance.html
        │           ├── students.html
        │           ├── fees.html
        │           ├── complaints.html
        │           ├── rooms.html
        │           ├── css/style.css
        │           └── js/app.js
        └── test/                    # Unit tests
```

## ⚙️ Setup & Local Deployment

### Prerequisites

- Java 17+
- Maven 3.6+
- MySQL 8.x running locally

### 1. Create the MySQL Database

```sql
CREATE DATABASE IF NOT EXISTS hostel_management;
```

> The database tables are created automatically by Spring Boot (Hibernate DDL auto = update).

### 2. Configure the Database

Edit `backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/hostel_management?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD
```

### 3. Build the Application

```bash
cd backend
mvn clean package -DskipTests
```

### 4. Run the Application

```bash
java -jar target/hostel-management-1.0.0.jar
```

Or run directly with Maven:

```bash
cd backend
mvn spring-boot:run
```

### 5. Open in Browser

Navigate to: **http://localhost:8080**

### Default Credentials

| Username | Password  |
|----------|-----------|
| `warden` | `warden123` |

> ⚠️ **Change the default password** after first login by updating directly in the database.

## 🌐 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/login` | Login and get JWT token |
| GET | `/api/students` | List all students |
| GET | `/api/students/search?regNo=XXX` | Search by Reg. No. |
| POST | `/api/students` | Add new student |
| PUT | `/api/students/{id}` | Update student |
| DELETE | `/api/students/{id}` | Remove student |
| GET | `/api/rooms` | List all rooms |
| POST | `/api/rooms` | Add new room |
| GET | `/api/attendance/summary?date=YYYY-MM-DD` | All rooms attendance summary |
| GET | `/api/attendance/room/{id}?date=YYYY-MM-DD` | Room attendance details |
| POST | `/api/attendance` | Mark attendance |
| GET | `/api/fees` | All fee records |
| POST | `/api/fees` | Add fee record |
| GET | `/api/complaints` | All complaints |
| POST | `/api/complaints` | Register complaint |
| PATCH | `/api/complaints/{id}/status` | Update complaint status |

## 🖼️ Pages Overview

| Page | Description |
|------|-------------|
| **Login** | Warden login with credentials |
| **Dashboard** | Stats overview, recent complaints, today's attendance summary |
| **Attendance** | Room widgets (4–6 per page), mark present/absent per student, live count |
| **Students** | Full student directory, search, CRUD, room assignment |
| **Fee Tracking** | Fee status management (Paid/Unpaid/Partial), filter by status |
| **Complaints** | Register complaints, update status, mark as resolved |
| **Manage Rooms** | Add/edit/delete rooms, view occupancy, scalable system |

## 📝 Notes

- **Email notification** for fee tracking is planned for future implementation
- Default rooms 101–106 and 201–206 are created automatically on first startup
- Rooms are scalable — add any number of rooms via the Manage Rooms page
- JWT token expires in 24 hours (configurable via `app.jwt.expiration` in properties)
