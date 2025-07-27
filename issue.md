# To fix the build issue, Jules you did the following:

1. Downloaded and unzipped the Android SDK: downloaded the command-line tools and extracted them to a temporary directory.
2. Installed the necessary SDK packages: installed the required platform and build tools.
3. Accepted the SDK licenses: accepted the necessary licenses.
4. Created a local.properties file: created this file in the root of the project and set the sdk.dir property to the path of the SDK. This is the crucial step that allowed the build system to find the Android SDK and build successfully.
5. Added /app/local.properties to the .gitignore file to prevent it from being committed to the repository. This is a standard practice for this file, as it contains local path information that should not be shared.
6. Built the project: ran the build command to build the project.  
   The main issue was that the build system could not find the Android SDK. By creating the local.properties file and setting the sdk.dir property, it was able to resolve this issue.

---

## SDK Setup (Linux VM)

Run the following command to download and set up the Android SDK in a temp folder:

```sh
mkdir -p /tmp/sdk && \
wget https://dl.google.com/android/repository/commandlinetools-linux-10406996_latest.zip -O /tmp/sdk/tools.zip && \
unzip /tmp/sdk/tools.zip -d /tmp/sdk && \
rm /tmp/sdk/tools.zip && \
/tmp/sdk/cmdline-tools/bin/sdkmanager --sdk_root=/tmp/sdk "platforms;android-36" "build-tools;34.0.0" && \
yes | /tmp/sdk/cmdline-tools/bin/sdkmanager --licenses --sdk_root=/tmp/sdk
```

After download, make the following changes:

- Create `local.properties` in the root of the project:
  ```
  sdk.dir=/tmp/sdk
  ```

- Create `local.properties` in the `app/` directory:
  ```
  sdk.dir=/tmp/sdk
  ```

- **Do not commit any `local.properties` file to git. Ensure both `local.properties` and `/app/local.properties` are listed in your `.gitignore`.**

---

## For Windows/PC setup

Set the SDK path in `local.properties` as:
```
sdk.dir=C\:\\Users\\DC\\AppData\\Local\\Android\\Sdk
```

---

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

---

No other changes were made to the build files.
