import { NextRequest } from 'next/server';

export async function GET(request: NextRequest, { params }: { params: Promise<{ slug: string[] }> }) {
  return handleProxy(request, await params);
}

export async function POST(request: NextRequest, { params }: { params: Promise<{ slug: string[] }> }) {
  return handleProxy(request, await params);
}

export async function PUT(request: NextRequest, { params }: { params: Promise<{ slug: string[] }> }) {
  return handleProxy(request, await params);
}

export async function DELETE(request: NextRequest, { params }: { params: Promise<{ slug: string[] }> }) {
  return handleProxy(request, await params);
}

export async function OPTIONS() {
  const responseHeaders = new Headers();
  responseHeaders.set('Access-Control-Allow-Origin', '*');
  responseHeaders.set('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  responseHeaders.set('Access-Control-Allow-Headers', 'Content-Type, Authorization');
  return new Response(null, { status: 204, headers: responseHeaders });
}

async function handleProxy(request: NextRequest, params: { slug: string[] }) {
  try {
    const slug = params.slug.join('/');
    const backendUrl = `https://cortisense-backend.onrender.com/${slug}`;
    
    // Create new headers without host to let fetch handle it properly
    const headers = new Headers(request.headers);
    headers.delete('host');
    headers.delete('origin');
    headers.delete('referer');

    let body = undefined;
    if (request.method !== 'GET' && request.method !== 'HEAD') {
      const text = await request.text();
      if (text) body = text;
    }

    const response = await fetch(backendUrl, {
      method: request.method,
      headers: headers,
      body: body,
      redirect: 'manual',
    });

    const responseHeaders = new Headers(response.headers);
    
    // Delete any backend CORS headers so they don't conflict with our own if needed
    responseHeaders.delete('Access-Control-Allow-Origin');
    responseHeaders.delete('Access-Control-Allow-Credentials');
    responseHeaders.delete('Access-Control-Allow-Methods');
    responseHeaders.delete('Access-Control-Allow-Headers');

    // Add local CORS so browser allows it
    responseHeaders.set('Access-Control-Allow-Origin', '*');

    return new Response(response.body, {
      status: response.status,
      statusText: response.statusText,
      headers: responseHeaders,
    });
  } catch (error: any) {
    console.error('Proxy Error:', error);
    return new Response(JSON.stringify({ error: 'Proxy error', details: error.message }), {
      status: 500,
      headers: { 'Content-Type': 'application/json' },
    });
  }
}
