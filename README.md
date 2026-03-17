# Workshop Nedap @ Syntaxis
**Open standards in care: hands-on with openEHR**

This repository contains the workshop material for the openEHR workshop. It consists of a Java-based API and a Vue.js web frontend.

## Prerequisites

Before you begin, ensure you have the following installed on your machine:

### For the API (Spring Boot)
- **Java Development Kit (JDK) 21**: The project requires Java 21 to compile and run. You can find the installation guide [here](https://docs.oracle.com/en/java/javase/21/install/overview-jdk-installation.html).

### For the Web Component (Vue.js)
- **Node.js and NPM**: Recommended Node.js versions are `^20.19.0` or `>=22.12.0`. NPM is usually bundled with Node.js. You can find the installation guide [here](https://docs.npmjs.com/downloading-and-installing-node-js-and-npm).

---

## Installation

### 1. Clone the repository
```bash
git clone https://github.com/nedap/workshop-syntaxis.git
cd workshop-syntaxis
```

### 2. Set up the Database
The project uses a SQLite database. To initialize and seed the database with initial data, run the following command from the project root:

**Linux / macOS:**
```bash
./gradlew seedDatabase
```

**Windows:**
```cmd
gradlew.bat seedDatabase
```

---

## Running the Project

To run the full project, you need to start both the API and the Web component.

### 1. Run the API (Backend)
Navigate to the project root and run the Spring Boot application:

**Linux / macOS:**
```bash
./gradlew bootRun
```

**Windows:**
```cmd
gradlew.bat bootRun
```

The API will be available at `http://localhost:8080` (default Spring Boot port).

### 2. Run the Web Component (Frontend)
Navigate to the `web` directory, install the dependencies, and start the development server:

**All Platforms:**
```bash
cd web
npm install
npm run dev
```

The web interface will be available at `http://localhost:5173` (default Vue.js development port).
