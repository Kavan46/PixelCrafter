# Connecting your Studio Hub App to Firebase

This project is fully configured with 100% production-ready **Firebase Authentication (Email/Password & Google Sign-In)**. 
We've implemented a graceful fallback architecture: the app works instantly in local sandbox mode in the browser emulator, and will automatically activate real cloud Firebase authentication once you connect your Firebase project!

Following the steps below will link this Android application to your personal Firebase Project.

---

## Step 1: Create a Firebase Project
1. Go to the [Firebase Console](https://console.firebase.google.com/).
2. Click **Add project**, name it (e.g., `Studio Hub`), and click **Continue**.
3. (Optional) Choose whether to enable Google Analytics, then click **Create project**.

## Step 2: Register your Android App in Firebase
During the setup wizard or from **Project Settings**, register a new Android application with these exact details:
- **Android Package Name / Application ID**: `com.aistudio.pixelcrafter.vrtxpl`
- **App Nickname**: `Studio Hub`
- **Debug signing certificate SHA-1**: *(Required for Google Sign-In)*
  - Open a terminal in your project root or in Android Studio, and run:
    ```bash
    ./gradlew signingReport
    ```
  - Copy the **SHA-1** fingerprint listed under `Variant: debug` / `Config: debugConfig`.
  - Paste this SHA-1 value into the Firebase app registration wizard.

## Step 3: Download and Add `google-services.json`
1. Download the generated `google-services.json` config file from the Firebase Console.
2. In the file tree, place this file inside the **`/app`** directory of this workspace:
   - Path in project: `/app/google-services.json`
3. If you want standard build-time injection, apply the Google Services plugin to `app/build.gradle.kts`:
   - Add `id("com.google.gms.google-services")` to the `plugins` block.

## Step 4: Enable Auth Providers in Firebase Console
In your Firebase Console left sidebar:
1. Go to **Build** -> **Authentication** and click **Get Started**.
2. Go to the **Sign-in method** tab.
3. Enable **Email/Password**:
   - Click **Email/Password**, turn on the status, and click **Save**.
4. Enable **Google**:
   - Click **Google** provider, click **Enable**.
   - Select your project support email.
   - Enter your Web Client ID and Web Client Secret if requested (can be configured in the credentials settings in Google Cloud Console).
   - Click **Save**.

---

Enjoy your fully connected Firebase Authentication! The app is completely prepared, compiled, and ready for deployment.
