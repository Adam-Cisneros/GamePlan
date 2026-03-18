# GamePlan

## Description
This is a mobile app made in Android Studio with Kotlin. This is a companion app to help developers with large-scale projects and teams keep track
of their projects. To become a user, you can sign-in using Firebase Authentication using Email + Password, Google, or GitHub to sign-in. Once signed-in, the app will be able to associate your groups, projects, and tasks with only you and will persist through the app's Firestore Database. You can create multiple groups to do projects with, make projects to connect or re-connect to any group, and create as many tasks in each project until they are complete. You can set milestone releases and assign certain tasks under those milestones so you can even keep track of where your project is in terms of releases. There are progress bars on each milestone to track their progress in the project cards and progress bars on each prjects to track their progress in the group cards. 

## Figma Design
[GamePlan Figma](https://www.figma.com/design/QyA40yb7tFsbffHKLlzRz4/GamePlan-Design?node-id=46-129&t=r1fAdz18UW1clLyt-1)

## Android & Jetpack Compose Features
### UI Architecture
* Declarative UI: Unlike traditional XML, the app uses Compose to describe the UI in Kotlin. It relies on State-driven UI (using Flow and collectAsState) where the UI automatically updates when data in the ViewModel changes.

* Material Design 3: The app uses the latest androidx.compose.material3 library, providing modern components like LazyColumn for lists, TextField, and theming.

* Navigation Compose: Uses androidx.navigation:navigation-compose to handle the transition between screens (e.g., moving from a Group view to a Plan view) via a single-activity architecture.

* Material Icons Extended: Utilizes androidx.compose.material:material-icons-extended to provide a wide array of visual cues for UI actions (delete, edit, move, etc.).

### State/Lifecycle Management
* ViewModel: The GamePlanViewModel centralizes the business logic, surviving configuration changes (like screen rotations).

* Kotlin Coroutines & Flow: Essential for asynchronous programming. Flow<List<Task>> allows the app to "stream" live updates from the database to the UI without manual refreshes.

* ViewModel Factory: Uses a custom ViewModelProvider.Factory to instantiate the ViewModel, allowing for dependency injection in the future.

### Backend/Data Persistence
* Firebase Firestore: The primary database.

  * Real-time Synchronization: The .dataObjects<T>() extension (from the Firebase Kotlin SDK) converts Firestore documents directly into Kotlin        objects and updates them in real-time.

  * NoSQL Structure: Flexible storage for Groups, Plans, and Tasks linked by IDs (uid, planId, groupId).

* Firebase Authentication: Handles user identity. The app uses auth.currentUser?.uid to ensure users only see their own data, providing essential multi-tenant security.

### Identity/Security
* Google ID & Credentials Manager: The dependencies androidx.credentials and googleid indicate the app uses the modern Android "Credential Manager" API. This simplifies the login process by allowing users to sign in with their Google Account or saved passkeys.

### Android Utilities
* Firestore DocumentId Annotation: The @DocumentId annotation in your Task.kt class tells the Firebase library to automatically map the unique Firestore document string to the id field of your data class.

* Coroutines Play Services: The kotlinx-coroutines-play-services library allows you to use .await() on Firebase tasks, turning messy callbacks into clean, sequential code.

## Device Dependencies
### Minimum & Target SDK Versions
* Minimum SDK (minSdk): At least API 24 (Android 7.0) or higher. While Compose can run on API 21, the androidx.credentials and modern Firebase Google ID libraries perform best and are most stable on API 24+.

* Target SDK (targetSdk): Should be API 34 (Android 14) or API 35 (Android 15) to ensure compatibility with the latest Play Store requirements and the googleid library features.

### Required Device Features
* Internet Access: The app is "Cloud-First." It requires a constant internet connection to sync data with Firebase Firestore and to fetch data from the GitHub GraphQL API.

* Google Play Services: Because the app relies on firebase-auth, firebase-firestore, and credentials-play-services-auth, the target device must have Google Play Services installed and updated. (This means standard Amazon Fire tablets or "de-Googled" ROMs may require extra configuration).

* Authentication Hardware: To utilize the androidx.credentials features (Passkeys/Google One Tap), the device should ideally have a secure lock screen (PIN, Pattern, or Biometrics) enabled.

### 3rd-Party Configurations
* google-services.json: This file must be generated from a Firebase Console and placed in the app/ directory. Without it, the app will crash on startup because it won't know which Firestore database to connect to as there is no API key pushed to the GitHub.

* SHA-1 Fingerprint: The developer's local debug keystore SHA-1 must be registered in the Firebase Console to enable Google Sign-In and GitHub Authentication.

* GitHub OAuth App / PAT: To use the GitHub API features, a valid Client ID and Client Secret (for OAuth) or a Personal Access Token must be configured within the app's networking layer.

### Library Dependencies
* Kotlin 2.0.21: Uses the latest Compose Compiler integrated into Kotlin.

* Compose BOM 2024.11.00: Ensures all UI components (Material3, Foundation, UI) are version-compatible.

* Ktor (CIO Engine): Used for the GitHub API. The CIO engine is a high-performance, coroutine-based engine that doesn't require a large library like OkHttp as a dependency.

* Kotlinx Serialization: Used instead of Gson for the GitHub API responses to ensure type-safety and compatibility with Kotlin Data Classes.

### Permission Requirements
* android.permission.INTERNET: Required for Firestore and GitHub API.

* android.permission.ACCESS_NETWORK_STATE: Typically required by Firebase to monitor connection swaps (e.g., switching from Wi-Fi to Data).

## Future Additions
* Importing of current GitHub organizations (Groups), repos (Projects), project boards (Project Detail), and issues (Tasks) if signed in using GitHub

* Ability to add other users as friends to message, assign to tasks/groups, and complete projects with

* Built-in Agile Poker loop upon assigning tasks to group members until an estimated time is agreed upon

* Drawing board to brainstorm potential project ideas with friends

* Ability to assign personal skills to yourself so that you may make yourself publicly avaiable to other user's projects looking for members with your skills
