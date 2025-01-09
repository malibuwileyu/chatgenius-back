# Configuration Guide

## Application Configuration

The application is configured through `application.yml` files in the `src/main/resources` directory. Different profiles can be used for different environments.

### Core Configuration

```yaml
spring:
  application:
    name: chatgenius-backend
  
  profiles:
    active: dev # Options: dev, test, prod

server:
  port: 8080
  servlet:
    context-path: /
```

### Database Configuration

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/chatgenius
    username: your_username
    password: your_password
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
      
  jpa:
    hibernate:
      ddl-auto: update # Options: none, validate, update, create, create-drop
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
```

### Redis Configuration

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: your_redis_password # Optional
    database: 0
    timeout: 60000
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 2
        max-wait: -1ms
```

### Security Configuration

```yaml
security:
  jwt:
    secret: your_jwt_secret_key
    expiration: 86400000 # 24 hours in milliseconds
    refresh-expiration: 604800000 # 7 days in milliseconds
  
  cors:
    allowed-origins: "*"
    allowed-methods: GET,POST,PUT,DELETE,OPTIONS
    allowed-headers: "*"
    exposed-headers: Authorization
    allow-credentials: true
    max-age: 3600
```

### WebSocket Configuration

```yaml
websocket:
  endpoint: /ws
  allowed-origins: "*"
  destination-prefixes: /app
  user-destination-prefix: /user
  heartbeat:
    incoming: 25000
    outgoing: 20000
```

### File Upload Configuration

```yaml
spring:
  servlet:
    multipart:
      enabled: true
      max-file-size: 10MB
      max-request-size: 10MB

storage:
  location: uploads
  allowed-content-types: image/jpeg,image/png,image/gif,application/pdf
```

## Environment-specific Configurations

### Development Profile (application-dev.yml)

```yaml
spring:
  jpa:
    show-sql: true
    hibernate:
      ddl-auto: update
      
logging:
  level:
    root: INFO
    com.chatgenius: DEBUG
    org.springframework.web: DEBUG
    org.hibernate.SQL: DEBUG
```

### Test Profile (application-test.yml)

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop

  redis:
    embedded: true
```

### Production Profile (application-prod.yml)

```yaml
spring:
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: none
      
logging:
  level:
    root: WARN
    com.chatgenius: INFO
    
server:
  tomcat:
    max-threads: 200
    min-spare-threads: 20
```

## Environment Variables

The following environment variables can be used to override configuration:

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/chatgenius
SPRING_DATASOURCE_USERNAME=your_username
SPRING_DATASOURCE_PASSWORD=your_password

# Redis
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379
SPRING_REDIS_PASSWORD=your_redis_password

# Security
SECURITY_JWT_SECRET=your_jwt_secret_key
SECURITY_JWT_EXPIRATION=86400000

# Server
SERVER_PORT=8080
```

## AWS Configuration

When deploying to AWS, additional configuration is needed:

```yaml
cloud:
  aws:
    credentials:
      access-key: your_access_key
      secret-key: your_secret_key
    region:
      static: us-east-1
    stack:
      auto: false

    s3:
      bucket: your-bucket-name
    
    rds:
      instance: your-rds-instance
```

## Logging Configuration

```yaml
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/application.log
    max-size: 10MB
    max-history: 10
  level:
    root: INFO
    com.chatgenius: INFO
    org.springframework: WARN
    org.hibernate: WARN
```

## Performance Tuning

### Connection Pool Settings

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
      max-lifetime: 1200000
```

### Cache Settings

```yaml
spring:
  cache:
    type: redis
    redis:
      time-to-live: 3600000
      cache-null-values: false
    cache-names:
      - users
      - channels
      - messages
```

### Thread Pool Settings

```yaml
server:
  tomcat:
    max-threads: 200
    min-spare-threads: 20
    max-connections: 10000
    accept-count: 100
``` 