# Filmorate: Social Platform for Film Lovers

Filmorate is a social platform designed for film enthusiasts to share their favorite movies, leave reviews, and connect with friends. The application provides a RESTful API to manage a collection of films and users, allowing for operations like adding friends, liking films, and retrieving lists of the most popular movies.

## Key Features

*   **User Management**: Create, update, and retrieve user profiles.
*   **Film Catalog**: Add, update, and browse film information, including details like release date, duration, and MPA rating.
*   **Social Interactions**:
    *   Users can add and remove each other as friends.
    *   Users can "like" films to recommend them.
*   **Popularity-Based Recommendations**: The service can generate a list of the most popular films based on the number of likes.
*   **Genre and MPA Ratings**: Films are categorized by genre and MPA ratings, which are managed as separate entities.

## Tech Stack

*   **Framework**: [Spring Boot](https://spring.io/projects/spring-boot) (v3.2.4)
*   **Language**: [Java](https://www.java.com/) (v21)
*   **Database**:
    *   [H2 Database](https://www.h2database.com/): A fast SQL database configured to persist data to a local file (`./db/filmorate`).
    *   [Spring Data JPA](https://spring.io/projects/spring-data-jpa): For simplified data access and repository management.
*   **API & Validation**:
    *   [Spring Web](https://docs.spring.io/spring-framework/reference/web/webmvc.html): For building the RESTful API.
    *   [Hibernate Validator](https://hibernate.org/validator/): For request data validation.
*   **Utilities**:
    *   [Lombok](https://projectlombok.org/): To reduce boilerplate code (e.g., getters, setters, constructors).
    *   [Logbook](https://github.com/zalando/logbook): For detailed HTTP request and response logging.
*   **Build Tool**: [Maven](https://maven.apache.org/)

## Architecture

The application follows a classic three-tier architecture, promoting separation of concerns and maintainability.

*   **Controller Layer** (`ru.yandex.practicum.filmorate.controller`):
    *   Handles incoming HTTP requests, validates request bodies (`DTOs`), and maps them to service layer calls.
    *   An `ErrorHandler` class provides centralized exception handling.
*   **Service Layer** (`ru.yandex.practicum.filmorate.service`):
    *   Contains the core business logic of the application.
    *   Orchestrates calls to different repositories to gather and process data (e.g., combining film data with its genres and likes).
*   **Data Access Layer (DAL)** (`ru.yandex.practicum.filmorate.dal`):
    *   Manages all interactions with the database.
    *   Uses a repository pattern with `JdbcTemplate` for executing SQL queries.
    *   The logic is further separated into specialized repositories for `Film`, `User`, `Genre`, `Like`, and `Friendship`, making the data logic modular and clean.

### Database Schema

The database schema is designed to efficiently manage relationships between users, films, genres, and likes.

![ER Diagram](database_er_diagram.png)

## How to Run

1.  **Prerequisites**:
    *   Java 21 or higher.
    *   Apache Maven.

2.  **Clone the repository**:
    ```bash
    git clone https://github.com/your-username/java-filmorate.git
    cd java-filmorate
    ```

3.  **Build and run the application**:
    ```bash
    mvn spring-boot:run
    ```
    The application will start on `http://localhost:8080`.

## API Endpoints

The main endpoints provided by the API are:

*   `GET /users`, `POST /users`, `PUT /users`
*   `GET /users/{id}`, `GET /users/{id}/friends`, `GET /users/{id}/friends/common/{otherId}`
*   `PUT /users/{id}/friends/{friendId}`, `DELETE /users/{id}/friends/{friendId}`
*   `GET /films`, `POST /films`, `PUT /films`
*   `GET /films/{id}`
*   `PUT /films/{id}/like/{userId}`, `DELETE /films/{id}/like/{userId}`
*   `GET /films/popular?count={count}`
*   `GET /genres`, `GET /genres/{id}`
*   `GET /mpa`, `GET /mpa/{id}`

For detailed request and response formats, please refer to the Postman collection included in the `/postman` directory.
