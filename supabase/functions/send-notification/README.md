# Send Notification Function

Supabase Edge Function to send Firebase Cloud Messaging (FCM) notifications.

## Setup

### 1. Install Supabase CLI

```bash
npm install -g supabase
```

### 2. Login to Supabase

```bash
supabase login
```

### 3. Link Your Project

```bash
supabase link --project-ref lqdlbvavdudwbwieegxr
```

### 4. Set Environment Variables

Set the FCM API key:

```bash
supabase secrets set FCM_API_KEY=your-fcm-api-key-here
```

**To get FCM API Key:**
- Option A: Create API Key in Google Cloud Console (see FCM_SERVER_KEY_ALTERNATIVES.md)
- Option B: Use Service Account Access Token (see below)

### 5. Deploy Function

```bash
supabase functions deploy send-notification
```

## Usage

### Send Notification

```bash
curl -X POST https://lqdlbvavdudwbwieegxr.supabase.co/functions/v1/send-notification \
  -H "Authorization: Bearer YOUR_ANON_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "token": "user-fcm-token",
    "title": "Order Update",
    "body": "Your order is ready!",
    "data": {
      "orderId": "123"
    }
  }'
```

### From Your App

```kotlin
// Get FCM token
val fcmToken = FirebaseMessaging.getInstance().token.await()

// Send notification request
val response = httpClient.post("https://lqdlbvavdudwbwieegxr.supabase.co/functions/v1/send-notification") {
    header("Authorization", "Bearer $supabaseAnonKey")
    header("Content-Type", "application/json")
    setBody(Json.encodeToString(NotificationRequest(
        token = fcmToken,
        title = "Order Update",
        body = "Your order is ready!",
        orderId = orderId
    )))
}
```

## Using Service Account (Alternative)

If using Service Account instead of API Key, you'll need to get an access token first.

### Get Access Token

```typescript
// This requires Firebase Admin SDK
import { initializeApp, cert } from 'firebase-admin/app';
import { getAuth } from 'firebase-admin/auth';

const serviceAccount = JSON.parse(Deno.env.get('FIREBASE_SERVICE_ACCOUNT') || '{}');

initializeApp({
  credential: cert(serviceAccount)
});

const accessToken = await getAuth().createCustomToken('service-account');
```

Then use this access token as `FCM_API_KEY`.

## Testing

Test the function locally:

```bash
supabase functions serve send-notification
```

Then send a test request:

```bash
curl -X POST http://localhost:54321/functions/v1/send-notification \
  -H "Authorization: Bearer YOUR_ANON_KEY" \
  -H "Content-Type: application/json" \
  -d '{
    "token": "test-token",
    "title": "Test",
    "body": "Test notification"
  }'
```

## Database Trigger (Optional)

Create a database trigger to automatically send notifications when orders are updated:

```sql
CREATE OR REPLACE FUNCTION notify_order_update()
RETURNS TRIGGER AS $$
DECLARE
  user_fcm_token text;
  notification_payload jsonb;
BEGIN
  -- Get user's FCM token
  SELECT fcm_token INTO user_fcm_token
  FROM public.users
  WHERE id = NEW.uid;
  
  -- Only send if user has FCM token
  IF user_fcm_token IS NOT NULL THEN
    -- Call Supabase Edge Function via HTTP
    -- Note: This requires pg_net extension
    PERFORM net.http_post(
      url := 'https://lqdlbvavdudwbwieegxr.supabase.co/functions/v1/send-notification',
      headers := jsonb_build_object(
        'Content-Type', 'application/json',
        'Authorization', 'Bearer ' || current_setting('app.settings.service_role_key', true)
      ),
      body := jsonb_build_object(
        'token', user_fcm_token,
        'title', 'Order Update',
        'body', 'Your order status: ' || NEW.status,
        'orderId', NEW.id::text,
        'data', jsonb_build_object('status', NEW.status)
      )
    );
  END IF;
  
  RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Create trigger
CREATE TRIGGER order_notification_trigger
AFTER INSERT OR UPDATE ON public.orders
FOR EACH ROW
EXECUTE FUNCTION notify_order_update();
```

## Troubleshooting

### "FCM_API_KEY not configured"
- Set the secret: `supabase secrets set FCM_API_KEY=your-key`

### "Unauthorized"
- Make sure you're sending the Authorization header with a valid token

### "Failed to send notification"
- Check that the FCM token is valid
- Verify the API key has correct permissions
- Check function logs: `supabase functions logs send-notification`

