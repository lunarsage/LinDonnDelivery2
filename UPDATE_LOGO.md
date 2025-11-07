# How to Update the App Logo

## Quick Steps

1. **Place your logo file in the `logo/` directory**
   - Supported formats: PNG, JPG, SVG
   - Recommended: PNG with transparent background
   - Size: At least 512x512 pixels for best quality

2. **Run the logo setup (or manually copy):**
   ```bash
   # Copy logo to drawable directory
   cp logo/logo.png app/src/main/res/drawable/logo_image.png
   ```

3. **Update the foreground drawable:**
   - Edit `app/src/main/res/drawable/ic_launcher_foreground.xml`
   - Replace the placeholder paths with your logo
   - Or use a bitmap reference if using PNG

4. **Update background color (optional):**
   - Edit `app/src/main/res/drawable/ic_launcher_background.xml`
   - Change the fillColor to match your brand

5. **Rebuild the app:**
   ```bash
   ./gradlew clean assembleDebug
   ```

## Using a PNG Logo

If you have a PNG logo:

1. Copy your logo to: `app/src/main/res/drawable/logo_image.png`
2. Update `ic_launcher_foreground.xml` to use:
   ```xml
   <bitmap
       android:src="@drawable/logo_image"
       android:gravity="center" />
   ```

## Using a Vector Logo

1. Convert your logo to SVG
2. Use Android Studio's Vector Asset Studio to convert to XML
3. Replace the paths in `ic_launcher_foreground.xml`

## Current Setup

- Background: Orange (#FF6B35) - Change in `ic_launcher_background.xml`
- Foreground: Placeholder logo - Update in `ic_launcher_foreground.xml`
- Adaptive Icon: Configured in `mipmap-anydpi-v26/ic_launcher.xml`

## Logo Directory

Place your logo files in the `logo/` directory at the project root. The app will reference them from the drawable resources.

