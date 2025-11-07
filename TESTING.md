# Testing Guide

This document provides comprehensive information about testing the LinDonn Delivery 2 application.

## Automated Testing

### GitHub Actions CI/CD

The project uses GitHub Actions for continuous integration. Workflows automatically run on:
- Push to `main` or `develop` branches
- Pull requests to `main` or `develop`
- Manual trigger via GitHub Actions UI

### Available Workflows

#### 1. Android CI (`android-ci.yml`)
**Main CI/CD Pipeline**

**Jobs:**
- **test**: Runs unit tests
  - Uses JUnit for unit testing
  - Tests run on Ubuntu latest
  - Results uploaded as artifacts
  
- **build**: Builds APKs
  - Builds debug APK
  - Builds release APK (optional, continues on error)
  - APKs uploaded as artifacts (30 days retention)
  
- **lint**: Code quality checks
  - Runs Android lint
  - Generates HTML reports
  - Results uploaded as artifacts

**Artifacts:**
- Test results (7 days retention)
- Debug APK (30 days retention)
- Release APK (30 days retention)
- Lint reports (7 days retention)

#### 2. Instrumentation Tests (`android-instrumentation-tests.yml`)
**UI/Instrumentation Testing**

- Runs on macOS (required for Android emulator)
- Creates Android Virtual Device (AVD) automatically
- Runs `connectedAndroidTest`
- Uploads test results as artifacts

**Note**: This workflow is optional and requires macOS runners which may have limited availability on free GitHub plans.

#### 3. Code Quality (`code-quality.yml`)
**Code Quality Checks**

- Kotlin lint checks (ktlint - optional)
- Dependency update checks
- Runs in parallel with main CI

### Viewing Results

1. Go to your GitHub repository
2. Click on the "Actions" tab
3. Select a workflow run
4. View job results and download artifacts

### Workflow Status Badge

Add this to your README.md (replace `yourusername` with your GitHub username):

```markdown
[![Android CI](https://github.com/yourusername/LinDonnDelivery2/actions/workflows/android-ci.yml/badge.svg)](https://github.com/yourusername/LinDonnDelivery2/actions)
```

## Local Testing

### Prerequisites
- Android Studio or command line tools
- JDK 17 or later
- Android SDK (API 24+)
- Connected device or emulator (for instrumentation tests)

### Running Tests

#### Unit Tests
```bash
# Run all unit tests
./gradlew test

# Run tests for specific module
./gradlew :app:test

# Run tests with coverage
./gradlew test jacocoTestReport
```

#### Instrumentation Tests
```bash
# Run all instrumentation tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run tests for specific flavor
./gradlew connectedDebugAndroidTest
```

#### Lint Checks
```bash
# Run lint checks
./gradlew lint

# View lint results
open app/build/reports/lint-results.html
```

#### Build Verification
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Build all variants
./gradlew build
```

### Test Structure

```
app/src/
├── test/                    # Unit tests
│   └── java/com/example/lindonndelivery2/
│       └── ExampleUnitTest.kt
│
└── androidTest/             # Instrumentation tests
    └── java/com/example/lindonndelivery2/
        └── ExampleInstrumentedTest.kt
```

## Writing Tests

### Unit Tests

Unit tests run on the JVM and don't require an Android device.

**Example:**
```kotlin
import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}
```

### Instrumentation Tests

Instrumentation tests run on Android devices/emulators and can test UI components.

**Example:**
```kotlin
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.lindonndelivery2", appContext.packageName)
    }
}
```

### Compose UI Tests

For testing Jetpack Compose UI:

```kotlin
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class MyComposeTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun myTest() {
        composeTestRule.setContent {
            MyComposable()
        }
        composeTestRule.onNodeWithText("Click me").performClick()
    }
}
```

## Test Coverage

### Generate Coverage Reports

```bash
# Run tests with coverage
./gradlew test jacocoTestReport

# View coverage report
open app/build/reports/jacoco/test/html/index.html
```

### Coverage Goals
- Aim for at least 70% code coverage
- Critical paths should have 90%+ coverage
- UI components should have basic smoke tests

## Continuous Integration Setup

### Initial Setup

1. **Push workflows to repository**
   ```bash
   git add .github/workflows/
   git commit -m "Add GitHub Actions workflows"
   git push
   ```

2. **Verify workflows run**
   - Go to GitHub Actions tab
   - Check that workflows appear
   - Trigger a test run

### Configuration

#### Secrets (Optional)
If you need API keys for integration tests:

1. Go to Repository Settings → Secrets and variables → Actions
2. Add secrets:
   - `SUPABASE_URL`: Your Supabase project URL
   - `SUPABASE_ANON_KEY`: Your Supabase anon key
   - `FCM_SERVER_KEY`: Firebase Cloud Messaging server key

#### Using Secrets in Workflows
Workflows can access secrets via `${{ secrets.SECRET_NAME }}`.

## Troubleshooting

### Common Issues

#### Tests Fail in CI but Pass Locally
- Check Android SDK version matches
- Verify Gradle version is consistent
- Check for flaky tests (add retries)

#### Build Failures
- Verify `google-services.json` placeholder is created
- Check Gradle cache is working
- Review build logs for specific errors

#### Slow Test Execution
- Enable Gradle caching in workflows
- Use test sharding for parallel execution
- Consider using Gradle build cache

### Debugging

#### View Test Output
```bash
# Run tests with verbose output
./gradlew test --info

# Run specific test
./gradlew test --tests "com.example.lindonndelivery2.ExampleUnitTest"
```

#### Download Artifacts
- Go to GitHub Actions → Workflow run
- Download test results artifact
- Extract and review HTML reports

## Best Practices

1. **Write Tests First**: Use TDD when possible
2. **Test Critical Paths**: Focus on user-facing features
3. **Keep Tests Fast**: Unit tests should run quickly
4. **Isolate Tests**: Tests should be independent
5. **Use Mocks**: Mock external dependencies
6. **Regular Updates**: Keep test dependencies updated
7. **Review Failures**: Investigate and fix test failures promptly

## Resources

- [Android Testing Guide](https://developer.android.com/training/testing)
- [JUnit Documentation](https://junit.org/junit4/)
- [Espresso Testing](https://developer.android.com/training/testing/espresso)
- [Compose Testing](https://developer.android.com/jetpack/compose/testing)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)

