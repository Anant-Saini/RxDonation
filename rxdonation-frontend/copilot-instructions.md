# RxDonation Frontend (Angular) - Copilot Instructions

## 🎯 Project Vision

A bridge between medicine donors and pharmacies, leveraging a modern, responsive Angular frontend to facilitate local community-driven redistribution of unexpired medications. The platform connects Donors (who can list unused medicines) with nearby Pharmacies (within a 2km radius using geospatial technology) while maintaining strict security, chain of custody, and real-time communication through a notification inbox system.

## 🚀 Must Use (Hard Constraints)

1.  **Angular Standalone + Signals:** Use Angular 17+ Standalone Components (No NgModules), Signals (`signal()`, `computed()`, `effect()`), and Reactive Forms for all state management.
2.  **Routing & Services:** Functional `provideRouter` with lazy-loading (`loadComponent`). Use `inject()` for dependency injection.
3.  **Forms & Validation:** Reactive Forms with strict validation for all user inputs (Auth, Medicine Details, Expiry Dates).
4.  **Styling:** Tailwind CSS for layout + Angular Material for UI components (Dialogs, Modals, Tables, Forms).
5.  **Security & Auth:** JWT-based authentication with Role-Based Access Control (RBAC) distinguishing Donor and Pharmacy actions. Secure token storage and refresh logic.
6.  **Geospatial & Coordinates:** Capture and manage latitude/longitude (WGS 84 coordinates). Use Haversine formula for distance calculations (display 2km radius search results).
7.  **Real-Time Communication:** In-app Notification Inbox powered by HTTP long-polling (60-second intervals). Update order status changes in real-time without page refresh.
8.  **Environment Variables:** All sensitive data (API URLs, credentials) injected via `environment.ts`; never hardcode secrets.

## 🏗️ Architecture & Patterns

### Folder Structure

```
src/app/
├── core/                    # Singleton services, guards, models
│   ├── services/           # Auth, Geolocation, Notification, Polling services
│   ├── guards/             # Auth guards for route protection
│   ├── interceptors/       # JWT token attachment, error handling
│   └── models/             # Interfaces & types (User, Order, Notification, etc.)
├── shared/                 # Reusable UI components
│   ├── components/         # Buttons, Modals, Notification Inbox, Tables, Filters
│   ├── pipes/              # Distance formatting, status badges
│   └── ui-kit/             # Angular Material wrappers (optional)
├── features/               # Feature-based standalone components
│   ├── auth/              # Login, Signup, Role Selection
│   ├── donor-dashboard/   # Place Order, Manage Medicines, My Orders
│   ├── pharmacy-dashboard/ # Nearby Donations, Accept Orders, My Orders with Status Filters
│   └── notifications/     # Notification Inbox with real-time updates
└── app.config.ts          # Root providers (routing, interceptors, etc.)
```

### Reactivity & State Management

- **Signals for State:** Use `signal()` for component-level state (current user, orders, filters).
- **Computed Values:** Use `computed()` for derived data (filtered orders, calculated distances).
- **Side Effects:** Use `effect()` only for critical side effects (e.g., polling service startup).
- **Control Flow:** Always use new syntax: `@if`, `@for`, `@switch` instead of `*ngIf`, `*ngFor`, `*ngSwitch`.

### API Interaction & Services

- **Service Layer:** Centralize all backend calls in services (AuthService, OrderService, NotificationService).
- **HTTP Interceptors:** Automatically attach JWT tokens; handle 401/403 errors by clearing auth state.
- **Environment Config:** Store API URLs in `environment.ts` for dev/prod separation.
- **Response Mapping:** Map backend responses to frontend models before exposing to components.

## 🛠️ Implementation Checklist (Priority Order)

### Phase 1: Core Infrastructure

1. **Authentication & Security:**
   - Implement `AuthService` with JWT token storage (localStorage or sessionStorage).
   - Build Reactive Forms for Login and Signup (with role selection for Donor/Pharmacy).
   - Create `AuthInterceptor` to attach JWT to all HTTP requests.
   - Handle 401/403 responses by clearing tokens and redirecting to login.
   - Use `AuthGuard` to protect routes by role (donor vs. pharmacy features).

2. **Reactivity & State Management:**
   - Initialize `currentUser` signal in `AuthService` with initial state from stored JWT.
   - Use `computed()` for derived values (e.g., `isAuthenticated`, `userRole`).
   - Avoid state management libraries; use Angular Signals + Services pattern.

3. **Geospatial & Location Services:**
   - Create `GeolocationService` to request user's latitude/longitude on signup/login.
   - Store coordinates in user profile (backend persists this).
   - Fallback: If geolocation denied, allow manual coordinate entry.

### Phase 2: Donor Features

4. **Donor Dashboard & Order Management:**
   - Build Reactive Form for "Place Donation Order" with:
     - Donor info (auto-filled from profile).
     - Multiple medicine entries (dynamic `FormArray` for add/remove).
     - Each medicine: name, quantity, expiry date (YYYY-MM-DD), optional notes.
   - Validation: Expiry date must be ≥ current date + 15 days.
   - Display inline validation errors; disable submit until all fields valid.
   - **API Call:** POST to backend `/api/orders` with order details.

5. **My Orders (Donor View):**
   - Fetch donor's orders using `signal()` and refresh on order state changes.
   - Display status (PENDING, ACCEPTED, COMPLETED, CANCELLED, EXPIRED) with visual badges.
   - Show distance to accepting pharmacy (if ACCEPTED).

### Phase 3: Pharmacy Features

6. **Pharmacy Dashboard - Responsive & Mobile-Friendly:**
   - Fetch "Nearby Donations" within 2km radius using API: `GET /api/pharmacy/nearby`.
   - Display as a list or card grid with:
     - Donor's location distance (Haversine calculation or backend-provided).
     - Medicine summary (item count, expiry date range).
     - Accept button (triggers state change to ACCEPTED).
   - Responsive design: sidebar on desktop, bottom sheet on mobile (use Angular Material `BottomSheet` or responsive layout).

7. **Order Items Modal (Angular Material Dialog):**
   - Click medicine card → open `MatDialog` showing full item details:
     - Item name, quantity, expiry date, donor notes, image (if available).
   - Use Angular Material `MatDialogModule` for consistent UX.
   - Confirmation button to accept order inside modal.

8. **My Orders (Pharmacy View) with Filters:**
   - Build a table using `MatTableModule` showing:
     - Order ID, donor distance, status, action date.
   - Add dropdown filters for `OrderStatus` (PENDING, ACCEPTED, COMPLETED, CANCELLED, EXPIRED).
   - Filter logic: Use `signal()` for selected filter + `computed()` for filtered list.
   - Update order status via API when accepted/completed.

### Phase 4: Real-Time Communication

9. **Notification Inbox (In-App Polling Service):**
   - Create `NotificationService` with HTTP polling every 60 seconds.
   - `GET /api/notifications` to fetch unread notifications.
   - Store notifications in `signal()` and expose unread count via `computed()`.
   - Poll only when user is authenticated and not navigating away.
   - Graceful stop when user logs out.

10. **Notification UI Component:**
    - Badge showing unread count in navbar/header.
    - Dropdown or dedicated page showing notification history.
    - Each notification shows: order ID, status change, timestamp.
    - "Mark as Read" functionality (update backend state).

### Phase 5: Geospatial & Advanced UX

11. **Distance Calculation & Display:**
    - Implement Haversine formula utility to calculate km between two coordinates.
    - Display distances in cards/lists as "X.XX km away".
    - Use `computed()` if distance depends on reactive input.

12. **Forms & Medicine Validation:**
    - **Medicine Expiry Validation:** ISO date format (YYYY-MM-DD), must be ≥ current UTC date + 15 days.
    - Show inline error: "Expiry date cannot be less than 15 days from today."
    - Prevent form submission if validation fails.

13. **Global Error Handling:**
    - Create `ErrorInterceptor` to catch HTTP errors.
    - Display user-friendly error messages (API unavailable, timeout, validation errors).
    - Show retry button for failed API calls.
    - Log errors to console (dev) without exposing backend details to users.

## 📏 Coding Standards

- **Naming:** CamelCase for variables/methods, kebab-case for component selectors and filenames.
- **DI Pattern:** Use `private authService = inject(AuthService);` instead of constructor injection.
- **Templates:** Keep logic in the `.ts` file; templates should only display data and trigger events.
- **Tailwind:** Use utility classes for layout and custom colors as defined in `tailwind.config.js`.

## 🗄️ Data Models (Frontend)

### Enums & Constants

```typescript
export enum UserRole {
  DONOR = 'DONOR',
  PHARMACY = 'PHARMACY',
}

export enum OrderStatus {
  PENDING = 'PENDING', // New order, awaiting pharmacy acceptance
  ACCEPTED = 'ACCEPTED', // Pharmacy has claimed the order
  COMPLETED = 'COMPLETED', // Order fulfilled, medicines transferred
  CANCELLED = 'CANCELLED', // Donor or pharmacy cancelled
  EXPIRED = 'EXPIRED', // Auto-expired after 10 days (backend-driven)
}
```

### Interfaces

```typescript
// User & Authentication
export interface User {
  id: string;
  email: string;
  role: UserRole;
  latitude: number;
  longitude: number;
  createdAt: string;
}

export interface AuthResponse {
  token: string;
  email: string;
  role: UserRole;
}

// Geospatial
export interface Location {
  lat: number;
  lng: number;
}

// Medicine & Orders
export interface MedicineItem {
  id: string;
  name: string;
  quantity: number;
  expiryDate: string; // ISO format: YYYY-MM-DD
  notes?: string;
  imageUrl?: string;
}

export interface DonationOrder {
  id: string;
  donorId: string;
  status: OrderStatus;
  items: MedicineItem[];
  pharmacyId?: string; // Set when ACCEPTED
  distanceKm?: number; // Calculated on frontend using Haversine
  createdAt: string;
  updatedAt: string;
  expiresAt: string; // Auto-expiry date (10 days from creation)
}

// Notifications
export interface Notification {
  id: string;
  userId: string;
  orderId: string;
  message: string;
  status: 'UNREAD' | 'READ';
  createdAt: string;
}
```

## ⚠️ Backend Integration & API Contract

### Environment Configuration

- **Dev API Base:** `http://localhost:8080`
- **Prod API Base:** Set via environment variables (e.g., `environment.prod.ts`)
- **CORS:** Backend must enable CORS for frontend origin.

### Key API Endpoints (Backend Contract)

```
POST   /api/auth/register        # Create new user (Donor or Pharmacy)
POST   /api/auth/login           # Login, returns JWT token
GET    /api/users/me             # Current authenticated user
GET    /api/orders               # Fetch user's orders (Donor or Pharmacy)
POST   /api/orders               # Create donation order (Donor)
PUT    /api/orders/{id}/accept   # Accept order (Pharmacy)
PUT    /api/orders/{id}/complete # Complete order (Pharmacy)
GET    /api/pharmacy/nearby      # Nearby donations within 2km radius
GET    /api/notifications        # Fetch unread notifications
PUT    /api/notifications/{id}   # Mark notification as read
```

### HTTP Headers & Security

- **Authorization:** All requests (except auth endpoints) must include `Authorization: Bearer {token}`.
- **Content-Type:** All requests/responses are `application/json`.
- **CORS Headers:** Backend handles CORS preflight requests.
- **Token Storage:** Store JWT in `localStorage` or `sessionStorage` (consider security implications).

### Error Handling & Status Codes

- **200 OK:** Successful request.
- **400 Bad Request:** Validation error; response includes error details.
- **401 Unauthorized:** Invalid/expired token; frontend must redirect to login.
- **403 Forbidden:** User lacks permissions for this action.
- **404 Not Found:** Resource does not exist.
- **500 Internal Server Error:** Server error; show user-friendly message and offer retry.

### Request/Response Format

- Backend expects/returns CamelCase JSON (`firstName`, not `first_name`).
- All datetime fields are ISO 8601 format (`2026-06-04T10:30:00Z`).
- Dates (without time) are `YYYY-MM-DD` format.

## 📌 Development Workflow

1. **Before implementation:** Check `environment.ts` for API base URL.
2. **For new features:** Create service first, then component.
3. **Testing:** Use browser DevTools to inspect API calls and verify token presence.
4. **Debugging:** Check browser console for Angular errors and API response logs.
