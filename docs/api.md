# API Documentation

## Authentication

### Register User
```http
POST /api/auth/register
```

Request body:
```json
{
  "username": "string",
  "email": "string",
  "password": "string"
}
```

Response:
```json
{
  "id": "uuid",
  "username": "string",
  "email": "string",
  "status": "OFFLINE",
  "createdAt": "timestamp"
}
```

### Login
```http
POST /api/auth/login
```

Request body:
```json
{
  "username": "string",
  "password": "string"
}
```

Response:
```json
{
  "token": "string",
  "type": "Bearer",
  "expiresIn": "number"
}
```

## Channels

### List Channels
```http
GET /api/channels
```

Response:
```json
[
  {
    "id": "uuid",
    "name": "string",
    "type": "PUBLIC|PRIVATE|DIRECT_MESSAGE",
    "memberCount": "number",
    "createdAt": "timestamp"
  }
]
```

### Create Channel
```http
POST /api/channels
```

Request body:
```json
{
  "name": "string",
  "type": "PUBLIC|PRIVATE|DIRECT_MESSAGE"
}
```

Response:
```json
{
  "id": "uuid",
  "name": "string",
  "type": "PUBLIC|PRIVATE|DIRECT_MESSAGE",
  "createdAt": "timestamp"
}
```

### Get Channel Details
```http
GET /api/channels/{id}
```

Response:
```json
{
  "id": "uuid",
  "name": "string",
  "type": "PUBLIC|PRIVATE|DIRECT_MESSAGE",
  "members": [
    {
      "id": "uuid",
      "username": "string",
      "status": "ONLINE|OFFLINE|AWAY|DO_NOT_DISTURB"
    }
  ],
  "createdAt": "timestamp"
}
```

### Delete Channel
```http
DELETE /api/channels/{id}
```

Response: `204 No Content`

### List Channel Members
```http
GET /api/channels/{id}/members
```

Response:
```json
[
  {
    "id": "uuid",
    "username": "string",
    "status": "ONLINE|OFFLINE|AWAY|DO_NOT_DISTURB"
  }
]
```

### Add Member to Channel
```http
POST /api/channels/{channelId}/members/{userId}
```

Response: `200 OK`

### Remove Member from Channel
```http
DELETE /api/channels/{channelId}/members/{userId}
```

Response: `200 OK`

## Messages

### Get Channel Messages
```http
GET /api/channels/{id}/messages
```

Query parameters:
- `page` (optional): Page number (default: 0)
- `size` (optional): Page size (default: 20)

Response:
```json
{
  "content": [
    {
      "id": "uuid",
      "content": "string",
      "type": "TEXT|FILE|AI|SYSTEM",
      "user": {
        "id": "uuid",
        "username": "string"
      },
      "thread": {
        "id": "uuid",
        "replyCount": "number"
      },
      "createdAt": "timestamp",
      "updatedAt": "timestamp"
    }
  ],
  "totalElements": "number",
  "totalPages": "number",
  "size": "number",
  "number": "number"
}
```

### Send Message
```http
POST /api/channels/{id}/messages
```

Request body:
```json
{
  "content": "string",
  "type": "TEXT|FILE|AI|SYSTEM",
  "threadId": "uuid (optional)"
}
```

Response:
```json
{
  "id": "uuid",
  "content": "string",
  "type": "TEXT|FILE|AI|SYSTEM",
  "user": {
    "id": "uuid",
    "username": "string"
  },
  "thread": {
    "id": "uuid",
    "replyCount": "number"
  },
  "createdAt": "timestamp"
}
```

### Update Message
```http
PUT /api/messages/{id}
```

Request body:
```json
{
  "content": "string"
}
```

Response:
```json
{
  "id": "uuid",
  "content": "string",
  "updatedAt": "timestamp"
}
```

### Delete Message
```http
DELETE /api/messages/{id}
```

Response: `204 No Content`

### Get Thread Replies
```http
GET /api/messages/{threadId}/replies
```

Response:
```json
[
  {
    "id": "uuid",
    "content": "string",
    "type": "TEXT|FILE|AI|SYSTEM",
    "user": {
      "id": "uuid",
      "username": "string"
    },
    "createdAt": "timestamp"
  }
]
```

## WebSocket Events

### Connect
```
CONNECT /ws
```
Headers:
- `Authorization: Bearer <token>`

### Subscribe to Channel
```
SUBSCRIBE /topic/channel.{channelId}
```

### Send Message
```
SEND /app/chat.message
```

Payload:
```json
{
  "channelId": "uuid",
  "content": "string",
  "type": "TEXT|FILE|AI|SYSTEM"
}
```

### Update Presence
```
SEND /app/presence
```

Payload:
```json
{
  "status": "ONLINE|OFFLINE|AWAY|DO_NOT_DISTURB"
}
```

### Typing Indicator
```
SEND /app/typing
```

Payload:
```json
{
  "channelId": "uuid",
  "typing": "boolean"
}
```

## Error Responses

All error responses follow this format:
```json
{
  "timestamp": "timestamp",
  "status": "number",
  "error": "string",
  "message": "string",
  "path": "string"
}
```

Common error status codes:
- `400 Bad Request`: Invalid input
- `401 Unauthorized`: Missing or invalid authentication
- `403 Forbidden`: Insufficient permissions
- `404 Not Found`: Resource not found
- `409 Conflict`: Resource conflict
- `500 Internal Server Error`: Server error 