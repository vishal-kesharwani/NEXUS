# Knowledge Nexus Frontend

A modern React + TypeScript mentorship platform frontend built with Vite, React Router, Tailwind CSS, and React Query.

## 🚀 Tech Stack

- **React 18** - UI Framework
- **TypeScript** - Type Safety
- **Vite** - Build Tool & Dev Server
- **React Router v6** - Routing
- **Tailwind CSS** - Styling
- **React Query** - State Management
- **Axios** - HTTP Client
- **Lucide Icons** - Icon Library

## 📁 Project Structure

```
src/
├── pages/              # Page components
│   ├── LoginPage.tsx
│   ├── RegisterPage.tsx
│   ├── DashboardPage.tsx
│   ├── ProfilePage.tsx
│   ├── SkillsPage.tsx
│   ├── FindMentorsPage.tsx
│   └── RequestsPage.tsx
├── components/         # Reusable components
│   └── ProtectedRoute.tsx
├── layouts/           # Layout components
│   └── MainLayout.tsx
├── services/          # API services
│   └── api.ts
├── context/           # React Context
│   └── AuthContext.tsx
├── types/             # TypeScript types
│   └── index.ts
├── utils/             # Utility functions
├── hooks/             # Custom hooks
├── App.tsx            # Main App component
└── main.tsx          # Entry point
```

## 🎯 Implemented Pages

### Authentication
- **Login** (`/login`) - User login with email/password
- **Register** (`/register`) - New user registration

### Authenticated Pages
- **Dashboard** (`/dashboard`) - Overview with statistics and recommendations
- **Profile** (`/profile`) - User profile management
- **Skills** (`/skills`) - Manage user skills and expertise
- **Find Mentors** (`/mentors`) - Search and browse mentors, view recommendations
- **Requests** (`/requests`) - Manage sent/received mentorship requests

## 🔧 Setup & Installation

### Prerequisites
- Node.js 16+
- npm or yarn

### Installation

```bash
# Install dependencies
npm install

# Create .env.local with API URL
echo "VITE_API_URL=http://localhost:8080/api" > .env.local
```

### Development

```bash
# Start development server
npm run dev

# Server runs at http://localhost:5173
```

### Build

```bash
# Build for production
npm run build

# Output in dist/
```

### Preview

```bash
# Preview production build locally
npm run preview
```

## 📡 API Integration

The frontend connects to the backend API at `http://localhost:8080/api` by default.

Configure via `.env.local`:
```env
VITE_API_URL=http://localhost:8080/api
```

### Available Endpoints

#### Authentication
- `POST /auth/login` - User login
- `POST /auth/register` - User registration
- `GET /users/me` - Get current user

#### Skills
- `GET /skills` - List all skills
- `POST /user-skills` - Add skill to user
- `GET /user-skills/me` - Get user's skills
- `DELETE /user-skills/{id}` - Remove skill

#### Mentors
- `GET /mentors/search?skill=X` - Search mentors by skill
- `GET /recommendations` - Get recommended mentors
- `GET /dashboard` - Get dashboard statistics

#### Mentorship
- `POST /mentorship/request` - Send mentorship request
- `GET /mentorship/sent` - Get sent requests
- `GET /mentorship/received` - Get received requests
- `PUT /mentorship/{id}/accept` - Accept request
- `PUT /mentorship/{id}/reject` - Reject request

## 🔐 Authentication

Uses JWT tokens stored in localStorage:
- Token is automatically added to all requests
- 401 responses redirect to login
- Session persists on page refresh

## 🎨 Styling

Uses Tailwind CSS with custom configuration. Key utilities:
- Responsive grid layouts
- Purple color scheme (`bg-purple-600`)
- Shadcn/UI-compatible components
- Lucide icons for UI

## 🚦 State Management

- **React Query** - Server state (API data)
- **React Context** - Auth state
- **Component State** - Local UI state

## 📦 Key Features

✅ JWT Authentication with auto token management
✅ Protected routes with auth guard
✅ Responsive design (mobile-first)
✅ Real-time API calls with React Query
✅ Form validation and error handling
✅ Loading states and skeletons
✅ Mentor discovery with search & recommendations
✅ Profile management
✅ Skills tracking
✅ Mentorship request workflow

## 🔄 Workflow

1. **Login/Register** → Receive JWT token
2. **Complete Profile** → Fill in professional details
3. **Add Skills** → Mark areas of expertise
4. **Browse Mentors** → View recommendations or search
5. **Send Request** → Request mentorship
6. **Manage Requests** → Accept/reject incoming requests

## 📝 Environment Variables

```env
VITE_API_URL=http://localhost:8080/api    # Backend API URL
```

## 🛠 Development Tips

### Adding a New Page

1. Create page component in `src/pages/NewPage.tsx`
2. Add route in `App.tsx`
3. Add navigation in `MainLayout.tsx`
4. Use `useAuth()` for auth state
5. Use React Query for data fetching

### Adding a New API Endpoint

1. Add function to `src/services/api.ts`
2. Create type in `src/types/index.ts`
3. Use in component with React Query

### Styling Components

Use Tailwind classes. Common patterns:
```tsx
className="flex items-center gap-2 px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700"
```

## 📚 Resources

- [React Documentation](https://react.dev)
- [Vite Documentation](https://vite.dev)
- [Tailwind CSS](https://tailwindcss.com)
- [React Query](https://tanstack.com/query/latest)
- [React Router](https://reactrouter.com)

## 🤝 Contributing

1. Create a feature branch
2. Make your changes
3. Test locally
4. Commit and push
5. Create a pull request

## 📄 License

This project is part of the Knowledge Nexus application.
import reactDom from 'eslint-plugin-react-dom'

export default defineConfig([
  globalIgnores(['dist']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      // Other configs...
      // Enable lint rules for React
      reactX.configs['recommended-typescript'],
      // Enable lint rules for React DOM
      reactDom.configs.recommended,
    ],
    languageOptions: {
      parserOptions: {
        project: ['./tsconfig.node.json', './tsconfig.app.json'],
        tsconfigRootDir: import.meta.dirname,
      },
      // other options...
    },
  },
])
```
