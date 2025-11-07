# GitHub Actions Workflows

This directory contains GitHub Actions workflows for automated testing and CI/CD.

## Workflows

### 1. `android-ci.yml`
**Main CI/CD Pipeline**
- Runs on: Push to `main`/`develop`, Pull Requests, Manual trigger
- Jobs:
  - **test**: Runs unit tests
  - **build**: Builds debug and release APKs (after tests pass)
  - **lint**: Runs Android lint checks

**Artifacts:**
- Test results (7 days retention)
- Debug APK (30 days retention)
- Release APK (30 days retention)
- Lint reports (7 days retention)

### 2. `android-instrumentation-tests.yml`
**UI/Instrumentation Tests**
- Runs on: Push to `main`, Pull Requests, Manual trigger
- Jobs:
  - **instrumented-tests**: Runs UI tests on Android emulator

**Requirements:**
- Runs on macOS (required for Android emulator)
- Creates Android Virtual Device (AVD) automatically
- Runs `connectedAndroidTest`

**Artifacts:**
- Instrumentation test results (7 days retention)

### 3. `code-quality.yml`
**Code Quality Checks**
- Runs on: Push to `main`/`develop`, Pull Requests, Manual trigger
- Jobs:
  - **ktlint**: Kotlin code style checks (optional)
  - **dependency-check**: Checks for dependency updates

## Setup

### Prerequisites
1. GitHub repository with Actions enabled
2. Secrets configured (if needed):
   - `SUPABASE_URL` (optional, for integration tests)
   - `SUPABASE_ANON_KEY` (optional, for integration tests)

### Configuration

#### Adding Secrets (Optional)
If you need to use real API keys for integration tests:

1. Go to Repository Settings → Secrets and variables → Actions
2. Add the following secrets:
   - `SUPABASE_URL`: Your Supabase project URL
   - `SUPABASE_ANON_KEY`: Your Supabase anon key
   - `FCM_SERVER_KEY`: Firebase Cloud Messaging server key (optional)

#### Using Secrets in Workflows
To use secrets in workflows, reference them as:
```yaml
env:
  SUPABASE_URL: ${{ secrets.SUPABASE_URL }}
  SUPABASE_ANON_KEY: ${{ secrets.SUPABASE_ANON_KEY }}
```

## Workflow Status Badge

Add this to your README.md to show workflow status:

```markdown
![Android CI](https://github.com/yourusername/LinDonnDelivery2/workflows/Android%20CI/badge.svg)
```

## Troubleshooting

### Build Failures
- Check if `google-services.json` placeholder is created correctly
- Verify Gradle cache is working
- Check Android SDK setup

### Test Failures
- Review test output in Actions tab
- Download test results artifact
- Check for flaky tests

### Lint Failures
- Review lint reports in artifacts
- Fix lint issues locally: `./gradlew lint`
- Update lint rules if needed

## Local Testing

Before pushing, test workflows locally:

```bash
# Run unit tests
./gradlew test

# Run lint
./gradlew lint

# Build APK
./gradlew assembleDebug
```

## Performance

- **Gradle Cache**: Workflows cache Gradle dependencies for faster builds
- **Parallel Jobs**: Tests and builds run in parallel when possible
- **Timeout**: Each job has a timeout to prevent hanging builds

## Contributing

When adding new workflows:
1. Follow existing workflow structure
2. Add appropriate timeouts
3. Use caching where possible
4. Upload artifacts for debugging
5. Add documentation to this README

