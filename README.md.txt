# ✈️ SkyBook — Flight Management System

## 📋 Project Description

The **Flight Management System (SkyBook)** is a Java-based desktop application developed as part of an Object-Oriented Programming (OOP) course project. It provides a modern graphical interface (GUI) built with **Java Swing** that allows users to manage flights and bookings efficiently. The system supports adding flights (admin only), booking seats, searching by city, and viewing all flights — all stored and managed through a **MySQL database** using JDBC connectivity. This project demonstrates key OOP principles including **Encapsulation**, **Abstraction**, **Inheritance**, and **Modularity**.

---

## 👤 Developer

| Full Name | CMS / ID | Section |
|-----------|----------|---------|
| Keertan-Shiv | (023-25-0105) | *(BSCS-E)* |

---

## 🎯 Purpose

The purpose of this project is to apply Object-Oriented Programming concepts in a real-world scenario. The system simulates a basic flight booking tool that can be used by an admin or customer to:

- View all available flights
- Add new flights (admin login required)
- Book seats on a flight with a generated receipt
- Search flights by city name
- Manage seat availability automatically after each booking

---

## 🗂️ Main Modules

| Module | Class | Description |
|--------|-------|-------------|
| **Entry Point** | `FlightGUI.main()` | Launches the Swing application |
| **Database** | `DBConnection` | Handles MySQL connection via JDBC |
| **Flight Model** | `Flight` | OOP entity class with encapsulated fields and getters/setters |
| **Admin** | `Admin` | Handles admin login and flight addition |
| **Customer** | `Customer` | Represents a passenger who can book flights |
| **Business Logic** | `FlightService` | All DB operations: add, getAll, book, search, exists, getFlight |
| **GUI - Flights Tab** | `buildFlightsTab()` | Displays all flights in a styled table |
| **GUI - Add Tab** | `buildAddTab()` | Admin-only form to add a new flight |
| **GUI - Book Tab** | `buildBookTab()` | Customer booking form with receipt printout |
| **GUI - Search Tab** | `buildSearchTab()` | Search flights by origin or destination city |

---

## 🧱 OOP Concepts Applied

- **Encapsulation** — `Flight` class uses `private` fields with `public` getters/setters to protect data.
- **Abstraction** — `FlightService` abstracts all database logic away from the GUI.
- **Inheritance** — `FlightGUI` extends `JFrame`; GUI components extend Swing base classes.
- **Modularity** — Each feature (Add, Book, Search, View) is a separate method/panel in the GUI class.
- **Exception Handling** — `RuntimeException` is used for overbooking, duplicate flights, and DB errors.
- **Event-Driven Programming** — Java Swing `ActionListener` handles all user interactions.

---

## 🗃️ Database Setup

This project uses **MySQL** as the backend database.

### Step 1 — Install MySQL
Download and install [MySQL Community Server](https://dev.mysql.com/downloads/mysql/).

### Step 2 — Create the Database

Open MySQL Workbench or MySQL terminal and run:

```sql
CREATE DATABASE flightdb;

USE flightdb;

CREATE TABLE flights (
    flightNo INT PRIMARY KEY,
    fromCity VARCHAR(100),
    toCity   VARCHAR(100),
    seats    INT,
    price    DOUBLE
);
```

### Step 3 — Configure DB Credentials

Open `FlightGUI.java` inside the `DBConnection` class and update the password:

```java
Connection c = DriverManager.getConnection(
    "jdbc:mysql://localhost:3306/flightdb", "root", "shivkirtan");
```

---

## ⚙️ How to Run

### Prerequisites

Make sure you have the following installed:

- ✅ Java JDK 11 or above
- ✅ IntelliJ IDEA (or any Java IDE)
- ✅ MySQL Server running locally
- ✅ MySQL Connector/J (JDBC Driver) — [Download here](https://dev.mysql.com/downloads/connector/j/)

### Step 1 — Clone or Download the Project

```bash
git clone https://github.com/shivkirtan09-goat/FlightManagementSystem.git
```

Or download the ZIP and extract it.

### Step 2 — Open in IntelliJ IDEA

1. Open IntelliJ IDEA
2. Click **File → Open**
3. Select the project folder

### Step 3 — Add MySQL JDBC Driver

1. Go to **File → Project Structure → Libraries**
2. Click **+** → **Java**
3. Navigate to your downloaded `mysql-connector-j-x.x.x.jar`
4. Click **OK** and **Apply**

### Step 4 — Set Up the Database

Run the SQL commands from the **Database Setup** section above.

### Step 5 — Compile & Run (Command Line)

If running from terminal instead of IntelliJ, use these commands:

```bash
# Compile
javac -cp .;mysql-connector-j-9.7.0.jar FlightGUI.java

# Run
java -cp .;mysql-connector-j-9.7.0.jar FlightGUI
```

> **Note:** On Linux/macOS, replace `;` with `:` in the classpath separator.

### Step 6 — Run via IntelliJ

1. Open `FlightGUI.java`
2. Click the **Run** button (▶️) or press `Shift + F10`
3. The **SkyBook Dashboard** window will open

### Default Admin Login

| Username | Password |
|----------|----------|
| admin | 1234 |

---

## 🖥️ Application Features

| Tab | Feature |
|-----|---------|
| ✈ Flights | View all flights in a live table |
| ➕ Add | Admin login + add new flight |
| 🎫 Book | Enter passenger name, flight no, seats — get a receipt |
| 🔍 Search | Search flights by city name |

---

## 🎬 Demo Video

> 📺 **YouTube Demo:** [https://youtu.be/ddwMc6v6zMQ](https://youtu.be/ddwMc6v6zMQ)

---

## 🔗 GitHub Repository

> 📁 **Repository:** [https://github.com/shivkirtan09-goat/FlightManagementSystem.git]
(https://github.com/shivkirtan09-goat/FlightManagementSystem.git)

---

## 🛠️ Technologies Used

| Technology | Purpose |
|------------|---------|
| Java (JDK 11+) | Core programming language |
| Java Swing | GUI (Graphical User Interface) |
| MySQL | Database management |
| JDBC | Java Database Connectivity |
| IntelliJ IDEA | Development environment |

---

## 📁 Project Structure

```
FlightManagementSystem/
│
├── src/
│   └── FlightGUI.java       # All classes in one file:
│                            #   DBConnection   — DB connection
│                            #   Flight         — entity model
│                            #   Admin          — admin role
│                            #   Customer       — customer role
│                            #   FlightService  — business logic & DB ops
│                            #   FlightGUI      — Swing GUI (main class)
│
├── flightdb.sql             # MySQL database setup script
└── README.md
```

---

## 📌 Notes

- Make sure MySQL server is **running** before launching the application.
- Admin credentials are hardcoded as `admin / 1234` for demo purposes.
- Seat count updates automatically in the database after every booking.
- Search works on both origin and destination city (partial match supported).
- On **Linux/macOS**, replace `;` with `:` in the compile/run commands.

---

## 📄 License

This project is developed for **academic purposes** as part of an OOP course assignment.
