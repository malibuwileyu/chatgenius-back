# Implementation Guide - Real-time Chat MVP

## API Specification

### REST Endpoints

#### Authentication
```
POST   /api/auth/register     - Register new user
POST   /api/auth/login        - Login user
POST   /api/auth/logout       - Logout user
POST   /api/auth/refresh      - Refresh JWT token
```

#### User Management
```
GET    /api/users             - List users
POST   /api/users             - Create user
GET    /api/users/{id}        - Get user details
PUT    /api/users/{id}        - Update user
DELETE /api/users/{id}        - Delete user
GET    /api/users/{id}/status - Get user online status
```

#### Channel Management
```
GET    /api/channels              - List all channels
POST   /api/channels              - Create new channel
GET    /api/channels/{id}         - Get channel details
PUT    /api/channels/{id}         - Update channel
DELETE /api/channels/{id}         - Delete channel
GET    /api/channels/{id}/members - List channel members
POST   /api/channels/{id}/members - Add member to channel
DELETE /api/channels/{id}/members - Remove member from channel
```

#### Message Management
```
GET    /api/channels/{id}/messages     - Get channel messages
POST   /api/channels/{id}/messages     - Send message to channel
GET    /api/messages/{id}              - Get message details
PUT    /api/messages/{id}              - Update message
DELETE /api/messages/{id}              - Delete message
POST   /api/messages/{id}/reactions    - Add reaction to message
DELETE /api/messages/{id}/reactions    - Remove reaction
```

### WebSocket Events

#### Client Events
```
channel:join     - Join a channel
channel:leave    - Leave a channel
message:send     - Send a message
typing:start     - Start typing indicator
typing:stop      - Stop typing indicator
presence:update  - Update user presence
reaction:add     - Add reaction to message
reaction:remove  - Remove reaction from message
```

#### Server Events
```
channel:joined     - Channel join confirmation
channel:left      - Channel leave confirmation
message:received  - New message notification
typing:updated    - Typing status update
presence:updated  - User presence update
error:occurred    - Error notification
reaction:updated  - Reaction update notification
```

#### WebSocket Topics
```
/topic/channel.{channelId}         - Channel-specific messages
/topic/user.{userId}               - User-specific notifications
/topic/presence.{channelId}        - Channel presence updates
/topic/typing.{channelId}          - Typing indicators
/topic/error                       - Error broadcasts
```

## MVP Implementation Checklist

### Day 1: Project Foundation
1. Project Setup
   - [x] Initialize Spring Boot project [2024-01-08 20:45]
   - [x] Add required dependencies:
     - Spring Web [2024-01-08 20:45]
     - Spring WebSocket [2024-01-08 20:45]
     - Spring Data JPA [2024-01-08 20:45]
     - Spring Security [2024-01-08 20:45]
     - Lombok [2024-01-08 20:45]
     - PostgreSQL Driver [2024-01-08 20:45]
     - Redis [2024-01-08 20:45]
     - AWS RDS (PostgreSQL) [2024-01-08 20:45]
   - [x] Configure Maven/Gradle build [2024-01-08 20:45]
   - [x] Set up project structure [2024-01-08 21:00]
   - [x] Set up basic health check endpoint [2024-01-09 21:55]
   - [x] Verify server is running correctly [2024-01-09 21:55]

2. Configuration
   - [x] Set up application.yml [2024-01-08 21:00]
     - Database connection
     - Redis connection
     - WebSocket settings
     - Server configuration
   - [x] Configure development environment [2024-01-08 21:45]

3. Database Setup
   - [x] Create database schemas [2024-01-08 21:15]
   - [x] Configure Hibernate [2024-01-08 21:15]
   - [x] Set up connection pool [2024-01-08 21:15]
   - [x] Create JPA entities: [2024-01-08 21:30]
     - User.java (id, username, email, status)
     - Channel.java (id, name, type, createdAt)
     - Message.java (id, content, channelId, userId, createdAt)
     - Attachment.java
     - Reaction.java
   - [x] Create repositories with custom queries: [2024-01-08 21:30]
     - UserRepository
       - findByUsername
       - findByStatus
       - findByChannelId
     - ChannelRepository
       - findByMemberId
       - findPublicChannels
       - findDirectMessageChannels
     - MessageRepository
       - findByChannelId
       - findLatestInChannel
       - findRepliesByThreadId
   - [x] Test database connectivity [2024-01-08 21:45]

### Day 2: Core Implementation
1. Service Layer Development
   - [x] Implement base services:
     - ChatService
       - [x] createChannel [2024-01-08 23:45]
       - [x] joinChannel (implemented as addMember) [2024-01-08 23:45]
       - [x] leaveChannel (implemented as removeMember) [2024-01-08 23:45]
       - [x] getChannelMembers [2024-01-08 23:45]
       - [x] getPublicChannels [2024-01-09 20:30]
       - [x] getChannel [2024-01-09 20:30]
       - [x] deleteChannel [2024-01-09 20:30]
     - MessageService
       - [x] sendMessage (implemented as createMessage) [2024-01-08 23:45]
       - [x] getChannelMessages [2024-01-08 23:45]
       - [x] deleteMessage [2024-01-08 23:45]
       - [x] updateMessage [2024-01-09 20:30]
       - [x] getMessage [2024-01-09 20:30]
       - [x] createReply [2024-01-09 20:30]
       - [x] getThreadReplies [2024-01-09 20:30]
       - [x] searchMessages [2024-01-09 20:30]
     - UserService
       - [x] createUser [2024-01-09 19:00]
       - [x] updateStatus [2024-01-09 19:00]
       - [x] getUserChannels [2024-01-09 19:00]
       - [x] getUser [2024-01-09 19:00]
       - [x] deleteUser [2024-01-09 19:00]
     - PresenceService
       - [ ] updateUserPresence
       - [ ] getOnlineUsers
       - [ ] handleDisconnect
   - [x] Add error handling [2024-01-09 00:15]
     - [x] Base exception class [2024-01-09 00:15]
     - [x] Resource not found exception [2024-01-09 00:15]
     - [x] Validation exception [2024-01-09 00:15]
     - [x] Unauthorized exception [2024-01-09 00:15]
     - [x] Global exception handler [2024-01-09 00:15]
   - [ ] Implement Redis caching:
     - [ ] Channel member lists
     - [ ] Recent messages
     - [ ] User presence
   - [x] Add service tests [2024-01-09 19:15]
     - [x] UserServiceTest (10 tests) [2024-01-09 19:15]
     - [x] ChatServiceTest (12 tests) [2024-01-09 19:15]
     - [x] MessageServiceTest (13 tests) [2024-01-09 19:15]
     - [x] Integration tests [2024-01-09 20:45]
       - [x] ChannelControllerTest [2024-01-09 20:45]
       - [x] MessageControllerTest [2024-01-09 20:45]

2. WebSocket Infrastructure
   - [x] Configure STOMP WebSocket [2024-01-08 21:00]
   - [x] Configure WebSocket endpoints and broker [2024-01-08 23:45]
   - [ ] Implement WebSocket handlers:
     - ChatWebSocketHandler
     - PresenceWebSocketHandler
   - [ ] Set up channel subscription handling:
     - Channel join/leave
     - Message broadcasting
     - Presence updates
     - Typing indicators
   - [ ] Implement event listeners:
     - Connection events
     - Subscription events
     - Disconnect events
   - [ ] Add WebSocket security
   - [ ] Test WebSocket functionality:
     - Connection management
     - Message delivery
     - Presence updates

3. Basic Security Setup
   - [x] Configure Spring Security [2024-01-08 20:45]
   - [ ] Implement user authentication
   - [ ] Set up session management
   - [ ] Add security tests

### Day 3: API & Integration
1. REST API Implementation
   - [x] Channel Management Endpoints: [2024-01-09 20:30]
     ```
     GET    /api/channels              - List available channels
     POST   /api/channels              - Create new channel
     GET    /api/channels/{id}         - Get channel details
     DELETE /api/channels/{id}         - Delete channel
     GET    /api/channels/{id}/members - List channel members
     POST   /api/channels/{id}/members - Join channel
     DELETE /api/channels/{id}/members - Leave channel
     ```
   - [x] Message Management Endpoints: [2024-01-09 20:30]
     ```
     GET    /api/channels/{id}/messages - Get channel messages
     POST   /api/channels/{id}/messages - Send message
     DELETE /api/messages/{id}          - Delete message
     PUT    /api/messages/{id}          - Update message
     GET    /api/messages/{id}          - Get message details
     POST   /api/messages/{id}/replies  - Create reply
     GET    /api/messages/{id}/replies  - Get thread replies
     ```
   - [ ] Real-time Event Endpoints:
     ```
     GET    /api/channels/{id}/presence - Get online users
     GET    /api/users/{id}/status      - Get user status
     ```

2. WebSocket Events Implementation
   - [ ] Channel Events:
     ```
     channel:join     -> channel:joined
     channel:leave    -> channel:left
     ```
   - [ ] Message Events:
     ```
     message:send     -> message:received
     typing:start     -> typing:updated
     typing:stop      -> typing:updated
     ```
   - [ ] Presence Events:
     ```
     presence:update  -> presence:updated
     ```
   - [ ] Error Handling:
     ```
     error:occurred   -> client notification
     ```

3. Integration & Testing
   - [x] Write integration tests: [2024-01-09 20:45]
     - Channel operations
       - [x] Create/join/leave [2024-01-09 20:45]
       - [x] Member management [2024-01-09 20:45]
       - [x] Message flow [2024-01-09 20:45]
     - [ ] WebSocket tests
       - Connection lifecycle
       - Event handling
       - Presence updates
     - [ ] Load tests
       - Multiple users

3. Documentation & Polish
   - [x] Create API documentation [2024-01-09 21:15]
   - [x] Write setup instructions [2024-01-09 21:15]
   - [x] Add deployment guide [2024-01-09 21:15]
   - [x] Create README.md [2024-01-09 21:15]
   - [x] Document configuration options [2024-01-09 21:15]

### Completion Criteria
1. Core Functionality
   - [ ] Users can register and authenticate
   - [x] Channels can be created and managed [2024-01-09 20:30]
   - [ ] Real-time messaging works
   - [x] Messages are persisted [2024-01-09 20:30]
   - [ ] WebSocket connections are stable

2. Technical Requirements
   - [x] All tests pass [2024-01-09 20:45]
   - [x] Error handling is comprehensive [2024-01-09 00:15]
   - [ ] Documentation is complete
   - [x] Code is properly formatted [2024-01-09 20:45]
   - [ ] Security measures are in place

## Auxiliary Todo

### Post-MVP Features
1. Authentication & Authorization
   - OAuth2 integration
   - Role-based access
   - Session management
   - Account management

2. Media Management
   - File upload/download
   - Image processing
   - Storage integration
   - CDN setup

3. AI Integration
   - OpenAI integration
   - Context management
   - Response streaming
   - Cost optimization

4. Performance Optimization
   - Query optimization
   - Caching strategy
   - Connection pooling
   - Load balancing

5. Monitoring & Logging
   - System monitoring
   - Performance metrics
   - Error tracking
   - Audit logging

6. Security Enhancements
   - End-to-end encryption
   - Rate limiting
   - Input validation
   - Security auditing

7. DevOps Setup
   - CI/CD pipeline
   - Docker deployment
   - Kubernetes setup
   - Environment management 

## Technology Stack
- Java 17+ (LTS)
- Spring Boot 3.x
- Spring WebSocket
- Spring Security
- Spring Data JPA
- AWS RDS (PostgreSQL)
- AWS S3 for file storage
- Redis for caching

## Database Configuration
The application uses AWS RDS for PostgreSQL as the primary database:
- Instance: db.t3.micro (free tier)
- Storage: 20GB
- PostgreSQL 15+
- Automated backups enabled
- Multi-AZ deployment (optional)

## Storage Configuration
AWS S3 is used for file storage:
- Bucket for user uploads
- Bucket for application assets
- IAM roles for secure access
- Direct upload capabilities

## Security
- AWS IAM for service authentication
- JWT for user authentication
- Spring Security for endpoint protection
- AWS security groups for database access 

## Suggested Process
- First Phase (REST-based messaging):
  - Implement message REST controllers x
  - Add controller-level validation x
  - Set up response DTOs x
  - Add controller tests x
  - Get basic message flow working
- Second Phase (Real-time layer):
  - Add WebSocket message handlers
  - Implement presence tracking
  - Set up real-time event broadcasting
  - Add WebSocket security
  - Handle connection management
