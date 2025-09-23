# Build Environment Setup Instructions

This document provides detailed instructions for setting up the build environment for this project, particularly for use in a new VM.

## 1. Download and Set Up the Android SDK

The following command will download the Android SDK command-line tools, extract them to a temporary directory, and then install the required platform and build tools. It will also accept all the necessary licenses.

```sh
mkdir -p /tmp/sdk && \
wget https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip -O /tmp/sdk/tools.zip && \
unzip /tmp/sdk/tools.zip -d /tmp/sdk && \
rm /tmp/sdk/tools.zip && \
yes | /tmp/sdk/cmdline-tools/bin/sdkmanager --sdk_root=/tmp/sdk "platforms;android-36" "build-tools;34.0.0" && \
yes | /tmp/sdk/cmdline-tools/bin/sdkmanager --licenses --sdk_root=/tmp/sdk
```

**Explanation of the command:**

*   `mkdir -p /tmp/sdk`: Creates a directory at `/tmp/sdk` to store the Android SDK.
*   `wget ...`: Downloads the Android SDK command-line tools.
*   `unzip ...`: Extracts the downloaded zip file to the `/tmp/sdk` directory.
*   `rm /tmp/sdk/tools.zip`: Removes the downloaded zip file after extraction.
*   `/tmp/sdk/cmdline-tools/bin/sdkmanager ...`: Runs the SDK manager to install the specified platform and build tools.
*   `yes | ... --licenses`: Automatically accepts all SDK licenses.

## 2. Set the ANDROID_HOME Environment Variable

After the SDK is downloaded and set up, you need to set the `ANDROID_HOME` environment variable to the path where the SDK is located.

```sh
export ANDROID_HOME=/tmp/sdk
```

## 3. Build the Project

Once the SDK is set up and the `ANDROID_HOME` variable is exported, you can build the project using the following command:

```sh
./gradlew build
```

This command will build the project and generate the necessary APK files.

## For Windows/PC setup

Set the SDK path in `local.properties` as:
```
sdk.dir=C\:\\Users\\DC\\AppData\\Local\\Android\\Sdk
```

## If issues persist after setup, try the following:

### Optimize the Build Process

- **Use Gradle Daemon:**  
  The Gradle Daemon helps with build performance. It is enabled by default, but you can add this to your `gradle.properties`:
  ```
  org.gradle.daemon=true
  ```

- **Enable build caching:**  
  Add to your `gradle.properties`:
  ```
  org.gradle.caching=true
  ```

- **Enable parallel execution:**  
  Add to your `gradle.properties`:
  ```
  org.gradle.parallel=true
  ```

- **Reduce build tasks:**  
  Run only what you need, e.g. use:
  ```sh
  ./gradlew assembleDebug
  ```
  instead of
  ```sh
  ./gradlew build
  ```
  if you only need a debug APK.
