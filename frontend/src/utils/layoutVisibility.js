export const shouldShowResourceSidebar = (route) => {
  const meta = route?.meta || {}
  return !meta.hideHeader && !meta.hideResourceSidebar
}
