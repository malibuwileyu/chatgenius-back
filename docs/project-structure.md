# Project Structure

```
chatgeniusv2-backend/
├── src/
│   ├── main/
│   │   ├── java/com/chatgenius/
│   │   │   ├── config/                 # Configuration classes
│   │   │   │   ├── DatabaseConfig.java
│   │   │   │   ├── RedisConfig.java
│   │   │   │   ├── WebSocketConfig.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── AiConfig.java
│   │   │   │
│   │   │   ├── controller/            # REST Controllers
│   │   │   │   ├── AuthController.java
│   │   │   │   ├── ChatController.java
│   │   │   │   ├── UserController.java
│   │   │   │   └── MediaController.java
│   │   │   │
│   │   │   ├── service/              # Business Logic
│   │   │   │   ├── auth/
│   │   │   │   │   └── AuthService.java
│   │   │   │   ├── chat/
│   │   │   │   │   ├── ChatService.java
│   │   │   │   │   └── MessageService.java
│   │   │   │   ├── ai/
│   │   │   │   │   └── AiService.java
│   │   │   │   ├── media/
│   │   │   │   │   └── MediaService.java
│   │   │   │   └── user/
│   │   │   │       └── UserService.java
│   │   │   │
│   │   │   ├── repository/           # Data Access Layer
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── MessageRepository.java
│   │   │   │   ├── ChannelRepository.java
│   │   │   │   └── MediaRepository.java
│   │   │   │
│   │   │   ├── model/               # JPA Entities
│   │   │   │   ├── User.java
│   │   │   │   ├── Message.java
│   │   │   │   ├── Channel.java
│   │   │   │   └── Media.java
│   │   │   │
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   │   ├── request/
│   │   │   │   │   ├── AuthRequest.java
│   │   │   │   │   └── MessageRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── AuthResponse.java
│   │   │   │       └── MessageResponse.java
│   │   │   │
│   │   │   ├── exception/           # Custom Exceptions
│   │   │   │   ├── ApiException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   │
│   │   │   ├── security/            # Security Components
│   │   │   │   ├── JwtTokenProvider.java
│   │   │   │   └── UserDetailsServiceImpl.java
│   │   │   │
│   │   │   ├── websocket/          # WebSocket Components
│   │   │   │   ├── ChatWebSocketHandler.java
│   │   │   │   └── WebSocketEventListener.java
│   │   │   │
│   │   │   └── util/               # Utility Classes
│   │   │       ├── Constants.java
│   │   │       └── ValidationUtils.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml      # Main configuration
│   │       ├── application-dev.yml  # Development config
│   │       └── application-prod.yml # Production config
│   │
│   └── test/
│       └── java/com/chatgenius/
│           ├── controller/          # Controller Tests
│           ├── service/             # Service Tests
│           ├── repository/          # Repository Tests
│           └── integration/         # Integration Tests
│
├── scripts/                        # Utility Scripts
│   ├── setup-db.sql
│   └── init-data.sql
│
├── docs/                          # Documentation
│   ├── api/
│   │   └── openapi.yaml
│   ├── SDD.md
│   ├── TDD.md
│   ├── implementation.md
│   └── project-structure.md
│
├── .mvn/                         # Maven Wrapper
│   └── wrapper/
│
├── .github/                      # GitHub Configurations
│   └── workflows/
│       └── ci.yml
│
├── docker/                       # Docker Configurations
│   ├── Dockerfile
│   └── docker-compose.yml
│
├── .gitignore                    # Git Ignore Rules
├── mvnw                          # Maven Wrapper Script
├── mvnw.cmd                      # Maven Wrapper Script (Windows)
├── pom.xml                       # Maven Configuration
└── README.md                     # Project Documentation
```

## Directory Descriptions

### Source Code
- `config/`: Spring configuration classes
- `controller/`: REST API endpoints
- `service/`: Business logic implementation
- `repository/`: Data access interfaces
- `model/`: JPA entity classes
- `dto/`: Data transfer objects
- `exception/`: Custom exceptions and handlers
- `security/`: Security-related components
- `websocket/`: WebSocket handlers
- `util/`: Utility classes

### Resources
- `application.yml`: Application configuration files
- `scripts/`: Database and utility scripts
- `docs/`: Project documentation

### Build & Deploy
- `pom.xml`: Maven project configuration
- `docker/`: Docker-related files
- `.github/`: CI/CD configurations

### Testing
- `test/`: Test classes mirroring main structure
- `integration/`: End-to-end tests 