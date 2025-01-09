# Software Design Document (SDD)

## System Architecture

### High-Level Architecture
ChatGenius follows a modern Spring-based architecture with real-time capabilities:

```
[Client Layer]
    ↓ ↑ WebSocket/HTTP
[Spring Application Layer]
    ↓ ↑
[Data Layer]
```

### Component Overview

#### 1. Frontend Components
- **Layout System**
  - Root Layout (auth check, providers)
  - Navigation Components
  - Workspace Layout

- **Authentication Components**
  - Login/Register Forms
  - OAuth Providers Integration
  - Session Management

- **Chat Interface**
  - Channel List
  - Message Thread View
  - Message Input
  - File Upload Interface
  - Emoji Picker
  - User Presence Indicator

- **AI Avatar System**
  - Avatar Customization Interface
  - Voice/Video Message Interface
  - AI Response Preview
  - Context Settings

#### 2. Backend Services

- **Spring WebSocket Service**
  - STOMP WebSocket Handler
  - Message Broadcasting
  - Presence System
  - Typing Indicators

- **Spring Security Service**
  - User Management
  - JWT Authentication
  - Permission Control

- **Message Service**
  - Message CRUD Operations
  - File Upload Handling
  - Search Indexing (Elasticsearch)
  - Thread Management

- **AI Service**
  - Message Generation
  - Context Management
  - Voice Synthesis
  - Video Avatar Generation
  - User Style Learning

#### 3. Data Models

```java
// Core Models
@Entity
public class User {
    @Id
    private UUID id;
    private String email;
    private String username;
    private String fullName;
    private String avatarUrl;
    
    @Enumerated(EnumType.STRING)
    private UserStatus status;
    
    @OneToOne(cascade = CascadeType.ALL)
    private AISettings aiSettings;
}

@Entity
public class Channel {
    @Id
    private UUID id;
    private String name;
    private String description;
    
    @Enumerated(EnumType.STRING)
    private ChannelType type;
    
    @ManyToMany
    private Set<User> members;
    private LocalDateTime createdAt;
}

@Entity
public class Message {
    @Id
    private UUID id;
    
    @ManyToOne
    private Channel channel;
    
    @ManyToOne
    private User user;
    
    private String content;
    
    @Enumerated(EnumType.STRING)
    private MessageType type;
    
    @OneToMany(cascade = CascadeType.ALL)
    private List<Attachment> attachments;
    
    private UUID threadId;
    
    @OneToMany(cascade = CascadeType.ALL)
    private List<Reaction> reactions;
    
    private LocalDateTime createdAt;
}

// AI-Specific Models
@Entity
public class AISettings {
    @Id
    private UUID id;
    private boolean isEnabled;
    
    @Embedded
    private PersonalitySettings personality;
    private String voiceId;
    
    @Embedded
    private AvatarSettings avatarSettings;
    
    @Embedded
    private ContextPreferences contextPreferences;
}

@Embeddable
public class AvatarSettings {
    private String model;
    private String style;
    
    @Convert(converter = JsonConverter.class)
    private Map<String, Object> customizations;
}
```

### Data Flow

1. **Real-time Message Flow**
```
User Types → Input Validation → STOMP WebSocket → 
Spring Service Processing → JPA Database Write → 
Message Broadcasting → Client Updates
```

2. **AI Response Flow**
```
Message Received → Context Collection → 
AI Processing → Response Generation → 
Optional Voice/Video Synthesis → 
Message Delivery → Client Rendering
```

3. **File Sharing Flow**
```
File Selected → Client-side Validation → 
Spring MultipartFile Upload → Storage Service → 
JPA Metadata Storage → Channel Notification
```

### Security Architecture

1. **Authentication**
   - Spring Security with JWT
   - OAuth2 integration
   - Token rotation
   - Rate limiting with Spring Cloud Gateway

2. **Data Security**
   - JPA-based row-level security
   - End-to-end encryption for DMs
   - Spring Security content filtering
   - Input validation with Bean Validation

3. **AI Security**
   - Prompt injection prevention
   - Content filtering
   - Rate limiting
   - User consent management

### Performance Considerations

1. **Optimizations**
   - Spring Data JPA pagination
   - Lazy loading with Hibernate
   - Spring Resource Handlers
   - Spring Cache abstraction

2. **Caching Strategy**
   - Spring Cache annotations
   - Redis caching
   - Static asset caching
   - AI response caching

3. **Real-time Performance**
   - WebSocket session management
   - Message batching
   - Presence optimization
   - Typing indicator throttling

### Scalability Design

1. **Horizontal Scaling**
   - Stateless Spring services
   - Spring Cloud Load Balancing
   - Database sharding strategy
   - Redis cluster

2. **Resource Management**
   - Connection pool (HikariCP)
   - Spring AMQP queues
   - Spring Batch jobs
   - Rate limiting

### Monitoring & Observability

1. **Metrics**
   - Spring Actuator metrics
   - Micrometer integration
   - AI response quality
   - Error rates

2. **Logging**
   - SLF4J/Logback
   - AI interaction logs
   - Spring Security audit
   - Spring Cloud Sleuth traces

### Disaster Recovery

1. **Backup Strategy**
   - Database backups
   - File storage backups
   - Spring Cloud Config backups
   - User data exports

2. **Recovery Plans**
   - Failover procedures
   - Data restoration
   - Service recovery
   - Communication plans 

## Infrastructure Components
- AWS RDS for PostgreSQL database
- AWS S3 for file storage
- Redis for caching and real-time features
- AWS CloudWatch for monitoring

## Data Storage
### Primary Database (AWS RDS)
- PostgreSQL database for persistent storage
- Automated backups and point-in-time recovery
- Read replicas for scaling (optional)

### File Storage (AWS S3)
- Secure file uploads and downloads
- CDN integration for faster content delivery
- Lifecycle policies for storage management

### Caching Layer
- Redis for session management
- Distributed caching for performance
- Real-time data synchronization

## Security Architecture
- AWS IAM for service-level security
- Security groups for network isolation
- JWT for user authentication
- Spring Security for API protection 