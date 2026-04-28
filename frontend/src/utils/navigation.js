const AUTH_PATHS = new Set(['/login', '/register'])

export const getSafeRedirectPath = (redirect, fallback = '/') => {
  if (typeof redirect !== 'string') {
    return fallback
  }

  if (!redirect.startsWith('/') || redirect.startsWith('//')) {
    return fallback
  }

  const path = redirect.split(/[?#]/, 1)[0]
  if (AUTH_PATHS.has(path)) {
    return fallback
  }

  return redirect
}
