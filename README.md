# ChatGenius Backend

A real-time chat application backend built with Spring Boot, WebSocket, and PostgreSQL.

## Features

- Real-time messaging using WebSocket
- Channel-based communication
- Thread-based replies
- User presence tracking
- Message reactions and attachments
- REST API for all operations
- Comprehensive test coverage

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- PostgreSQL 15+
- Redis (optional, for caching)

## Quick Start

1. Clone the repository:
```bash
git clone https://github.com/yourusername/chatgenius-backend.git
cd chatgenius-backend
```

2. Configure the database:
   - Create a PostgreSQL database
   - Update `src/main/resources/application.yml` with your database credentials

3. Build the project:
```bash
./mvnw clean install
```

4. Run the application:
```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

## Configuration

The application can be configured through `application.yml`. Key configuration options:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/chatgenius
    username: your_username
    password: your_password
  
  redis:
    host: localhost
    port: 6379
    
  security:
    jwt:
      secret: your_jwt_secret
      expiration: 86400000
```

See [Configuration Guide](docs/configuration.md) for detailed configuration options.

## API Documentation

The API documentation is available at:
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI Spec: `http://localhost:8080/v3/api-docs`

See [API Documentation](docs/api.md) for detailed endpoint documentation.

## Development

### Project Structure
```
src/
├── main/
│   ├── java/
│   │   └── com/chatgenius/
│   │       ├── config/      # Configuration classes
│   │       ├── controller/  # REST controllers
│   │       ├── dto/         # Data Transfer Objects
│   │       ├── exception/   # Custom exceptions
│   │       ├── model/       # Entity classes
│   │       ├── repository/  # Data access layer
│   │       ├── service/     # Business logic
│   │       └── websocket/   # WebSocket handlers
│   └── resources/
│       └── application.yml  # Application configuration
└── test/
    └── java/
        └── com/chatgenius/
            ├── integration/  # Integration tests
            ├── repository/   # Repository tests
            └── service/     # Service tests
```

### Running Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=MessageServiceTest

# Run with specific profile
./mvnw test -Dspring.profiles.active=test
```

## Deployment

See [Deployment Guide](docs/deployment.md) for detailed deployment instructions.

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details. 