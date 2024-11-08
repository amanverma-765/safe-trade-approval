# STA Backend

This repository contains the backend for the Trademark Service, built with Kotlin, Java, Spring Boot, and Gradle. The backend provides various endpoints for managing trademarks and requires JWT token authorization for most endpoints.

## Table of Contents

- [Features](#features)
- [Setup](#setup)
    - [Prerequisites](#prerequisites)
    - [Docker Installation](#docker-installation)
    - [Running PostgreSQL with Docker](#running-postgresql-with-docker)
    - [Application Configuration](#application-configuration)
    - [Running the Application](#running-the-application)
- [Accessing API Documentation](#accessing-api-documentation)

## Features

- User registration and authentication
- JWT token-based authorization
- CRUD operations for trademarks
- Report generation

## Setup

### Prerequisites

- Java 17
- Gradle

### Docker Installation

1. Install Docker:
    - For Ubuntu:
      ```sh
      sudo apt-get update
      sudo apt-get install -y docker.io
      sudo systemctl start docker
      sudo systemctl enable docker
      ```
    - For other operating systems, follow the [official Docker installation guide](https://docs.docker.com/get-docker/).

### Running PostgreSQL with Docker

1. Clone the repository:
    ```sh
    git clone https://github.com/yatharth-webxela/trademark.webxela.com
    cd trademark.webxela.com/STA-Backend
    ```

2. Ensure the `docker-compose.yml` file is present in the root directory of the project.

3. Run the PostgreSQL and Adminer containers:
    ```sh
    docker-compose up -d
    ```

### Application Configuration

1. Update the `application.properties` file with your PostgreSQL credentials:
    ```properties
    spring.datasource.url=jdbc:postgresql://localhost:5432/your_database_name
    spring.datasource.username=your_username
    spring.datasource.password=your_password
    spring.datasource.driver-class-name=org.postgresql.Driver
    ```

### Running the Application

1. Build the project:
    ```sh
    ./gradlew build
    ```

2. Run the application:
    ```sh
    ./gradlew bootRun
    ```

3. The application will start on `http://localhost:8888`.

## Accessing API Documentation

The API documentation is available at `http://localhost:8888/api/v1/sta/docs`. This page provides detailed information about all the available endpoints, their usage, and the required JWT token authorization.


