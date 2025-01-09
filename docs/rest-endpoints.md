# REST API Endpoints

## Channel Management
- [x] `GET /api/chat/channels` - List all channels
- [x] `GET /api/chat/channels/:channelId` - Get channel details
- [x] `POST /api/chat/channels` - Create a channel
- [x] `PUT /api/chat/channels/:channelId` - Update channel details
- [x] `DELETE /api/chat/channels/:channelId` - Delete a channel

## Message Management
- [x] `GET /api/chat/channels/:channelId/messages` - Get messages for a channel
- [x] `POST /api/chat/channels/:channelId/messages` - Send a message to a channel
- [x] `DELETE /api/chat/channels/:channelId/messages/:messageId` - Delete a message
- [x] `PUT /api/chat/channels/:channelId/messages/:messageId` - Edit a message
- [x] `GET /api/chat/channels/:channelId/messages/search` - Search messages in channel

## Channel Membership
- [ ] `POST /api/chat/channels/:channelId/members` - Add member to channel
- [ ] `DELETE /api/chat/channels/:channelId/members/:userId` - Remove member from channel
- [ ] `GET /api/chat/channels/:channelId/members` - List channel members

## Online Presence
- [ ] `GET /api/chat/channels/:channelId/presence` - Get online users in channel
- [ ] `GET /api/chat/users/:userId/status` - Get user's online status

## Typing Indicators
- [ ] `GET /api/chat/channels/:channelId/typing` - Get currently typing users

## Message Delivery Status
- [ ] `GET /api/chat/messages/:messageId/status` - Get message delivery status
- [ ] `PUT /api/chat/messages/:messageId/status` - Update message status (read/delivered)

## System
- [x] `GET /api/health` - API health check
- [x] `GET /api` - API documentation and endpoints listing 