// Supabase Edge Function to send FCM notifications
import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from 'https://esm.sh/@supabase/supabase-js@2'

const FCM_API_KEY = Deno.env.get('FCM_API_KEY') || ''
// Using legacy FCM API which works with API keys
const FCM_ENDPOINT = 'https://fcm.googleapis.com/fcm/send'

interface FCMRequest {
  token: string
  title: string
  body: string
  data?: Record<string, string>
  orderId?: string
}

serve(async (req) => {
  // Handle CORS
  if (req.method === 'OPTIONS') {
    return new Response('ok', {
      headers: {
        'Access-Control-Allow-Origin': '*',
        'Access-Control-Allow-Methods': 'POST',
        'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
      },
    })
  }

  try {
    // Get Supabase client
    const supabaseClient = createClient(
      Deno.env.get('SUPABASE_URL') ?? '',
      Deno.env.get('SUPABASE_ANON_KEY') ?? '',
      {
        global: {
          headers: { Authorization: req.headers.get('Authorization')! },
        },
      }
    )

    // Verify authentication
    const {
      data: { user },
    } = await supabaseClient.auth.getUser()

    if (!user) {
      return new Response(
        JSON.stringify({ error: 'Unauthorized' }),
        { 
          status: 401,
          headers: { 
            'Content-Type': 'application/json',
            'Access-Control-Allow-Origin': '*',
          },
        }
      )
    }

    // Parse request body
    const { token, title, body, data, orderId }: FCMRequest = await req.json()

    if (!token || !title || !body) {
      return new Response(
        JSON.stringify({ error: 'Missing required fields: token, title, body' }),
        { 
          status: 400,
          headers: { 
            'Content-Type': 'application/json',
            'Access-Control-Allow-Origin': '*',
          },
        }
      )
    }

    if (!FCM_API_KEY) {
      return new Response(
        JSON.stringify({ error: 'FCM_API_KEY not configured' }),
        { 
          status: 500,
          headers: { 
            'Content-Type': 'application/json',
            'Access-Control-Allow-Origin': '*',
          },
        }
      )
    }

    // Prepare FCM message using legacy FCM API (works with API keys)
    const fcmMessage = {
      to: token,
      notification: {
        title: title,
        body: body,
      },
      data: {
        ...data,
        ...(orderId && { orderId: orderId }),
      },
      priority: 'high',
    }

    // Send FCM notification using legacy FCM API
    const fcmResponse = await fetch(FCM_ENDPOINT, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `key=${FCM_API_KEY}`,
      },
      body: JSON.stringify(fcmMessage),
    })

    if (!fcmResponse.ok) {
      const errorText = await fcmResponse.text()
      console.error('FCM Error:', errorText)
      return new Response(
        JSON.stringify({ 
          error: 'Failed to send notification',
          details: errorText 
        }),
        { 
          status: fcmResponse.status,
          headers: { 
            'Content-Type': 'application/json',
            'Access-Control-Allow-Origin': '*',
          },
        }
      )
    }

    const fcmResult = await fcmResponse.json()

    return new Response(
      JSON.stringify({ 
        success: true, 
        messageId: fcmResult.message_id || fcmResult.multicast_id,
        token: token 
      }),
      { 
        status: 200,
        headers: { 
          'Content-Type': 'application/json',
          'Access-Control-Allow-Origin': '*',
        },
      }
    )
  } catch (error) {
    console.error('Error:', error)
    return new Response(
      JSON.stringify({ error: error.message }),
      { 
        status: 500,
        headers: { 
          'Content-Type': 'application/json',
          'Access-Control-Allow-Origin': '*',
        },
      }
    )
  }
})

