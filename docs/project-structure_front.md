# Frontend Project Structure - ChatGenius MVP

## Root Directory Structure
```
chatgenius-frontend/
├── src/                    # Source code
├── public/                 # Static assets
├── docs/                   # Documentation
├── tests/                  # Test files
├── .github/                # GitHub workflows
└── scripts/                # Build/deploy scripts
```

## Source Code Structure
```
src/
├── app/                    # Next.js App Router
│   ├── (auth)/            # Auth-protected routes
│   │   ├── channels/      # Channel pages
│   │   │   ├── [id]/      # Individual channel view
│   │   │   │   ├── page.tsx   # Channel messages view
│   │   │   │   ├── loading.tsx
│   │   │   │   └── error.tsx
│   │   │   └── page.tsx   # Channels list view
│   │   ├── profile/       # User profile pages
│   │   │   ├── page.tsx   # Profile view/edit
│   │   │   └── settings/
│   │   │       └── page.tsx   # User settings
│   │   └── search/        # Search pages
│   │       └── page.tsx   # Global search
│   ├── api/               # API routes
│   │   ├── auth/          # Auth endpoints
│   │   │   ├── login/
│   │   │   │   └── route.ts
│   │   │   ├── register/
│   │   │   │   └── route.ts
│   │   │   └── logout/
│   │   │       └── route.ts
│   │   ├── channels/      # Channel endpoints
│   │   │   ├── route.ts   # GET (list), POST (create)
│   │   │   └── [id]/
│   │   │       ├── route.ts   # GET, PUT, DELETE
│   │   │       ├── messages/
│   │   │       │   └── route.ts
│   │   │       └── members/
│   │   │           └── route.ts
│   │   ├── messages/      # Message endpoints
│   │   │   ├── route.ts
│   │   │   └── [id]/
│   │   │       └── route.ts
│   │   └── users/         # User endpoints
│   │       ├── route.ts
│   │       └── [id]/
│   │           └── route.ts
│   ├── layout.tsx
│   └── page.tsx
├── components/            # React components
│   ├── ui/               # shadcn/ui components
│   │   ├── button.tsx
│   │   ├── input.tsx
│   │   ├── dialog.tsx
│   │   └── avatar.tsx
│   ├── auth/             # Auth components
│   │   ├── login-form.tsx
│   │   └── register-form.tsx
│   ├── chat/             # Chat components
│   │   ├── channel/
│   │   │   ├── channel-list.tsx
│   │   │   ├── channel-item.tsx
│   │   │   ├── channel-header.tsx
│   │   │   └── channel-members.tsx
│   │   ├── message/
│   │   │   ├── message-list.tsx
│   │   │   ├── message-item.tsx
│   │   │   ├── message-input.tsx
│   │   │   └── message-thread.tsx
│   │   └── presence/
│   │       ├── typing-indicator.tsx
│   │       ├── online-status.tsx
│   │       └── user-presence.tsx
│   └── shared/           # Shared components
│       ├── layout/
│       │   ├── sidebar.tsx
│       │   └── header.tsx
│       └── feedback/
│           ├── toast.tsx
│           └── error-boundary.tsx
├── lib/                  # Utility functions
│   ├── utils/           # General utilities
│   │   ├── date.ts
│   │   ├── string.ts
│   │   └── validation.ts
│   ├── api/             # API clients
│   │   ├── auth.ts
│   │   ├── channels.ts
│   │   ├── messages.ts
│   │   └── users.ts
│   ├── hooks/           # Custom hooks
│   │   ├── useAuth.ts
│   │   ├── useChannel.ts
│   │   ├── useMessage.ts
│   │   ├── usePresence.ts
│   │   └── useWebSocket.ts
│   └── websocket/       # WebSocket setup
│       ├── socket.ts
│       └── events.ts
├── stores/              # Zustand stores
│   ├── auth-store.ts
│   ├── channel-store.ts
│   ├── message-store.ts
│   └── presence-store.ts
├── types/               # TypeScript types
│   ├── auth.ts
│   ├── channel.ts
│   ├── message.ts
│   └── user.ts
└── styles/             # Global styles
    ├── globals.css
    └── tailwind.css
```

## Test Directory Structure
```
tests/
├── unit/                  # Unit tests
│   ├── components/
│   │   ├── auth/
│   │   ├── chat/
│   │   └── shared/
│   └── hooks/
├── integration/           # Integration tests
│   ├── api/
│   │   ├── auth/
│   │   ├── channels/
│   │   └── messages/
│   └── websocket/
└── e2e/                   # End-to-end tests
    ├── auth.spec.ts
    ├── channels.spec.ts
    └── messages.spec.ts
```

## Configuration Files
```
chatgenius-frontend/
├── .env                   # Environment variables
├── .env.local            # Local environment variables
├── .env.test             # Test environment variables
├── .eslintrc.js          # ESLint configuration
├── .prettierrc           # Prettier configuration
├── jest.config.js        # Jest configuration
├── next.config.js        # Next.js configuration
├── tailwind.config.js    # Tailwind configuration
├── tsconfig.json         # TypeScript configuration
└── package.json          # Project dependencies
```

## Environment Variables
```
# .env
NEXT_PUBLIC_API_URL=http://localhost:8080/api
NEXT_PUBLIC_WS_URL=ws://localhost:8080/ws
NEXT_PUBLIC_UPLOAD_URL=http://localhost:8080/upload
```

## Key Dependencies
```json
{
  "dependencies": {
    "next": "14.x",
    "react": "18.x",
    "react-dom": "18.x",
    "@tanstack/react-query": "5.x",
    "zustand": "4.x",
    "socket.io-client": "4.x",
    "tailwindcss": "3.x",
    "shadcn-ui": "0.x",
    "@hookform/resolvers": "3.x",
    "react-hook-form": "7.x",
    "zod": "3.x"
  },
  "devDependencies": {
    "typescript": "5.x",
    "@types/react": "18.x",
    "@types/node": "20.x",
    "jest": "29.x",
    "@testing-library/react": "14.x",
    "cypress": "13.x"
  }
}
``` 