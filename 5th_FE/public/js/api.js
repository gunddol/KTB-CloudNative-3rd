async function apiFetch(path, opts = {}) {
  const headers = new Headers(opts.headers || {});
  headers.set('Accept', 'application/json');
  const uid = Auth.userId();
  if (uid) headers.set('X-USER-ID', String(uid));
  if (opts.body && !headers.has('Content-Type')) headers.set('Content-Type','application/json');
  const res = await fetch(`/api${path}`, { ...opts, headers });
  const contentType = res.headers.get('content-type') || '';
  let data = null;
  if (contentType.includes('application/json')) data = await res.json(); else data = await res.text();
  if (!res.ok) { const err = new Error((data && data.message) || res.statusText); err.status = data?.status || res.status; err.payload = data; throw err; }
  return data;
}
