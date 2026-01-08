import { useUserStore } from '@/stores/user'

export function hasPermission(permission) {
  const userStore = useUserStore()
  const permissions = userStore.permissions || []
  return permissions.includes('*') || permissions.includes(permission)
}

export function hasRole(role) {
  const userStore = useUserStore()
  const roles = userStore.roles || []
  return roles.includes('admin') || roles.includes(role)
}
