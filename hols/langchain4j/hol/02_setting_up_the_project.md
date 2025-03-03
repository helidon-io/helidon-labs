# 2. Setting Up the Project

In This Section, We Will:

- Explore the **bootstrap project** that includes a minimal **Helidon SE** application.
- Understand the project structure and its key components.
- Build and run the project to ensure the setup is correct.
- Verify that the provided **REST service** is working.

---

## Bootstrap the Project

This repository includes a **bootstrap project**, which provides a **Helidon SE** application with a basic REST service. The project is located in the `code/bootstrap` directory.

### Project Structure
The bootstrap project contains the following key files:

- **`pom.xml`**
   - Maven build script with a minimal set of dependencies required to build and run the application.

- **`ApplicationMain.java`**
   - Contains the `main` method and initializes the application.

- **`ChatBotService.java`**
   - A simple **RESTful service** that listens on the `/chat` endpoint.
   - Currently, it responds with `"Hello"` to every request.
   - _We will modify this service to implement our chatbot functionality._

- **`application.yaml`**
   - Contains application configuration settings.
   - _We will extend this file as we enhance the application._

## Build and Run the Project

### 1. Build the Project
Run the following command to **clean** and **build** the project:

```sh
mvn clean package
```

### 2. Run the Application
Start the application using:

```sh
java -jar target/helidon-ai-hol.jar
```

### 3. Verify the Application is Running

Open your browser and navigate to:

```
http://localhost:8080/chat
```

You should receive the following response:

```
Hello
```

Alternatively, you can test it using `curl`:

```sh
curl -X GET http://localhost:8080/chat
```

---

### Next Step → [Enabling AI Capabilities in the Project](03_enabling_ai_capabilities_in_the_project.md)
