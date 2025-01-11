# ChatGenius Technical Stack

## Core Technologies

### Frontend
- **Framework**: Next.js 14 with App Router
  - Server Components for improved performance
  - Server Actions for backend operations
  - Streaming and Progressive Rendering
  - Built-in API routes
  - File-system based routing

- **Styling**: 
  - TailwindCSS for utility-first styling
    - JIT (Just-In-Time) compilation
    - Custom theme configuration
    - Responsive design utilities
  - shadcn/ui components
    - Accessible components
    - Customizable themes
    - Radix UI primitives
    - Built-in dark mode support

### Backend & Data
- **Database**: Supabase (PostgreSQL)
  - Real-time capabilities via WebSocket
  - Row Level Security (RLS)
  - Built-in full-text search
  - Database backups
  - Automatic API generation
  - PostgREST integration

- **Authentication**: 
  - Supabase Auth
    - JWT-based authentication
    - OAuth providers support
    - Row Level Security integration
    - Session management
    - Email verification
    - Password reset flow

### State Management & Forms
- **State Management**: Zustand
  - Lightweight and performant
  - Middleware support
  - DevTools integration
  - TypeScript support
  - Async actions
  - Computed states

- **Forms**: 
  - React Hook Form
    - Performant form validation
    - TypeScript integration
    - Field array support
    - Form state management
  - Zod schema validation
    - Type inference
    - Custom validation rules
    - Error messages
    - Schema composition

## AI Integration Stack

### Language Models
- OpenAI GPT-4 API
  - Chat completion API
  - Function calling
  - Response streaming
  - Context management

### Voice & Video Synthesis
- D-ID for video avatar generation
  - Real-time video synthesis
  - Custom avatar support
  - Expression control

- ElevenLabs for voice synthesis
  - Voice cloning
  - Real-time synthesis
  - Emotion control

### AI Development Tools
- LangChain for AI orchestration
  - Prompt management
  - Context handling
  - Memory systems
  - Chain composition

- Vector Database: pgvector (Supabase)
  - Semantic search
  - Similarity queries
  - Embedding storage
  - PostgreSQL native integration

## DevOps & Infrastructure

### Hosting
- Vercel for frontend
  - Edge functions
  - Analytics
  - CI/CD integration
  - Preview deployments

- Supabase Cloud for backend
  - Database hosting
  - Auto-scaling
  - Backups
  - Dashboard

### Monitoring & Analytics
- Langfuse for AI observability
  - Prompt monitoring
  - Performance tracking
  - Cost analysis
  - Debug tooling

- Vercel Analytics
  - Web vitals
  - User flows
  - Performance metrics
  - Real-time monitoring

### Development Tools
- TypeScript for type safety
- ESLint + Prettier for code formatting
- Husky for git hooks
- Jest + Testing Library for testing 