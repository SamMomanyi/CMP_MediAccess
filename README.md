MediAccess
A comprehensive healthcare management system built with Kotlin Multiplatform, featuring patient queue management, insurance verification, and pharmacy workflows.
🚀 Features
Patient Features (Mobile)

Insurance Cover Linking: Automatic and manual insurance verification
QR Code Check-in: Generate visit codes for seamless hospital check-ins
Real-time Queue Updates: Live position tracking with "Your Turn" notifications
Prescription & Billing: View medication details and visit expenditure summary
Health Articles: Curated health content via RSS feeds
Nearby Hospitals: Location-based hospital search

Staff Features (Desktop)
Admin Dashboard

Staff Management: Create accounts for doctors, receptionists, and pharmacists
Role-based access control with secure authentication

Receptionist Dashboard

Cover Approval: Review and approve/reject insurance requests
Visit Verification: Scan QR codes and assign patients to doctors
Queue Management: Real-time assignment to available doctors

Doctor Dashboard

Patient Queue: View waiting patients with live updates
Availability Toggle: Control on-duty status
Prescription Creation: Multi-medication prescriptions sent directly to pharmacy
Visit History: Track completed consultations

Pharmacist Dashboard

Prescription Queue: Real-time pharmacy queue management
Medication Dispensing: View prescriptions with dosage details
Billing: Calculate and record total visit costs


🛠️ Tech Stack
Core Technologies

Kotlin Multiplatform (KMP): Shared business logic across Android and Desktop
Compose Multiplatform: Declarative UI for Android and Desktop JVM
Coroutines & Flow: Asynchronous programming and reactive streams

Backend & Data

Firebase Firestore: Real-time cloud database (via GitLive SDK)
Firebase Auth: User authentication
Room Database: Local persistence (KMP + SQLite)
Ktor Client: HTTP networking (OkHttp engine for desktop)

Desktop-Specific

Firestore REST API: Direct Firestore access via Ktor + Google Service Account
Java Crypto: JWT signing for Firebase authentication

Architecture & DI

MVI Pattern: Unidirectional data flow with ViewModels
Koin: Dependency injection across platforms
Clean Architecture: Domain/Data/Presentation separation

Additional Libraries

Coil3: Image loading (network + base64)
Kotlinx Serialization: JSON parsing
Kotlinx DateTime: Cross-platform date/time handling


📁 Project Structure
composeApp/
├── commonMain/              # Shared code (Android + Desktop)
│   ├── features/
│   │   ├── identity/        # User auth, check-in, hospitals
│   │   ├── cover/           # Insurance linking
│   │   ├── queue/           # Queue domain models
│   │   └── pharmacy/        # Prescriptions, billing
│   └── core/                # Shared utilities, theme
│
├── androidMain/             # Android-specific code
│   ├── features/queue/      # Mobile queue repository (Firestore SDK)
│   └── di/                  # Android Koin modules
│
└── desktopMain/             # Desktop-specific code
    ├── features/
    │   ├── auth/            # Admin login, role selection
    │   ├── queue/           # Desktop queue (REST API)
    │   ├── verification/    # QR code verification
    │   └── pharmacy/        # Pharmacist dashboard
    └── di/                  # Desktop Koin modules

🔐 Authentication & Authorization
Mobile (Patients)

Firebase Authentication (Email/Password + Google Sign-In)
Secure token-based sessions

Desktop (Staff)

Role-based login (Admin, Receptionist, Doctor, Pharmacist)
SHA-256 password hashing
Room database for local staff accounts
Firestore for availability tracking

Default Admin Credentials:
Email: admin@hospital.com
Password: Admin123
Role: ADMIN

🔄 Key Workflows
1. Patient Visit Flow
Patient generates QR code → Receptionist scans & assigns to doctor 
→ Patient receives "Your Turn" notification → Doctor consultation 
→ Doctor creates prescription → Pharmacy dispenses medication 
→ Patient views expenditure summary
2. Insurance Verification Flow
Patient submits cover request → Receptionist reviews in admin panel 
→ Approve/Reject → Patient receives approval notification 
→ Cover card unlocked for check-ins
3. Queue Management Flow
Doctor toggles "Available" → Receptionist assigns patients 
→ Real-time queue updates via Firestore snapshots 
→ Doctor marks patient done → Next patient auto-promoted

🚀 Setup & Installation
Prerequisites

JDK 17+
Android Studio Ladybug or later
Firebase project with Firestore enabled
Google Cloud Service Account (for desktop Firestore access)

Configuration

Clone repository:

bash   git clone https://github.com/yourusername/mediaccess.git
   cd mediaccess

Firebase Setup (Mobile):

Add google-services.json to composeApp/
Enable Firestore, Authentication in Firebase Console


Service Account Setup (Desktop):

Download service account JSON from Google Cloud Console
Place at composeApp/src/desktopMain/resources/service-account.json


Build:

bash   ./gradlew desktopRun    # Desktop app
   ./gradlew installDebug  # Android app

🗄️ Database Schema
Firestore Collections

users - Patient profiles
cover_link_requests - Insurance verification requests
visit_codes - QR check-in codes
queue_entries - Active patient queue
staff_accounts - Staff metadata & availability
prescriptions - Doctor prescriptions
pharmacy_queue - Pharmacy waiting list

Room Tables (Desktop)

admin_accounts - Staff login credentials
cover_link_requests - Local cache


🎨 Design Highlights

Material 3 Design System with dynamic theming
Responsive layouts optimized for mobile and desktop
Real-time UI updates via Kotlin Flow collectors
Custom components: QR code generator, role-based dashboards, interactive maps


🧪 Development Notes
Platform-Specific Code

expect/actual declarations for platform differences
Conditional dependencies via sourceSets
Firestore access: GitLive SDK (mobile) vs REST API (desktop)

State Management

MVI pattern with sealed classes for UI states
Single source of truth via StateFlow
Immutable state updates with copy()


📄 License
This project is licensed under the MIT License - see the LICENSE file for details.

👨‍💻 Author
Samuel Momanyi
GitHub • LinkedIn

🙏 Acknowledgments

Firebase for backend infrastructure
JetBrains for Compose Multiplatform
Material Design for UI guidelines
