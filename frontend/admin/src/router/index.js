import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '@/layouts/AdminLayout.vue'
import { useUserStore } from '@/stores/user'
import { getToken } from '@/utils/auth'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: AdminLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard/index.vue'),
        meta: { title: '仪表盘', icon: 'Odometer' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile/index.vue'),
        meta: { title: '个人中心', hidden: true }
      },
      {
        path: 'article',
        name: 'Article',
        redirect: '/article/list',
        meta: { title: '文章管理', icon: 'Document', requiresContent: true },
        children: [
          { path: 'list', name: 'ArticleList', component: () => import('@/views/Article/List.vue'), meta: { title: '文章列表', requiresContent: true } },
          { path: 'create', name: 'ArticleCreate', component: () => import('@/views/Article/Create.vue'), meta: { title: '创建文章', requiresContent: true } },
          { path: 'edit/:id', name: 'ArticleEdit', component: () => import('@/views/Article/Edit.vue'), meta: { title: '编辑文章', requiresContent: true } }
        ]
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('@/views/Category/index.vue'),
        meta: { title: '分类管理', icon: 'Folder', requiresContent: true }
      },
      {
        path: 'tag',
        name: 'Tag',
        component: () => import('@/views/Tag/index.vue'),
        meta: { title: '标签管理', icon: 'PriceTag', requiresContent: true }
      },
      {
        path: 'comment',
        name: 'Comment',
        component: () => import('@/views/Comment/index.vue'),
        meta: { title: '评论管理', icon: 'ChatDotRound', requiresContent: true }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/User/index.vue'),
        meta: { title: '用户管理', icon: 'User', requiresAdmin: true }
      },
      {
        path: 'system',
        name: 'System',
        redirect: '/system/role',
        meta: { title: '系统管理', icon: 'Setting', requiresAdmin: true },
        children: [
          { path: 'role', name: 'Role', component: () => import('@/views/System/Role/index.vue'), meta: { title: '角色管理', requiresAdmin: true } },
          { path: 'menu', name: 'Menu', component: () => import('@/views/System/Menu/index.vue'), meta: { title: '菜单管理', requiresAdmin: true } },
          { path: 'config', name: 'Config', component: () => import('@/views/System/Config/index.vue'), meta: { title: '系统配置', requiresAdmin: true } },
          { path: 'version', name: 'SystemVersion', component: () => import('@/views/System/Version.vue'), meta: { title: '版本管理', requiresAdmin: true } },
          { path: 'log', name: 'Log', component: () => import('@/views/System/Log/index.vue'), meta: { title: '操作日志', requiresAdmin: true } }
        ]
      },
      {
        path: 'vip',
        name: 'Vip',
        redirect: '/vip/member',
        meta: { title: '会员管理', icon: 'Medal', requiresAdmin: true },
        children: [
          { path: 'member', name: 'VipMember', component: () => import('@/views/Vip/Member.vue'), meta: { title: '会员列表', requiresAdmin: true } },
          { path: 'key', name: 'VipKey', component: () => import('@/views/Vip/Key.vue'), meta: { title: '密钥管理', requiresAdmin: true } },
          { path: 'heat', name: 'VipHeat', component: () => import('@/views/Vip/Heat.vue'), meta: { title: '加热记录', requiresAdmin: true } },
          { path: 'statistics', name: 'VipStatistics', component: () => import('@/views/Vip/Statistics.vue'), meta: { title: 'VIP统计', requiresAdmin: true } }
        ]
      },
      {
        path: 'ai',
        name: 'AiConfig',
        component: () => import('@/views/AI/Config.vue'),
        meta: { title: 'AI配置', icon: 'MagicStick', requiresAdmin: true }
      },
      {
        path: 'social',
        name: 'Social',
        redirect: '/social/follow',
        meta: { title: '社交管理', icon: 'Connection', requiresAdmin: true },
        children: [
          { path: 'follow', name: 'Follow', component: () => import('@/views/Follow/index.vue'), meta: { title: '关注管理', requiresAdmin: true } },
          { path: 'message', name: 'Message', component: () => import('@/views/Message/index.vue'), meta: { title: '私信管理', requiresAdmin: true } },
          { path: 'notification', name: 'Notification', component: () => import('@/views/Notification/index.vue'), meta: { title: '通知管理', requiresAdmin: true } }
        ]
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/Error/404.vue')
  }
]

const router = createRouter({
  history: createWebHistory('/admin/'),
  routes
})

// 检查是否是管理员
function isAdmin(userStore) {
  const roles = userStore.roles || []
  const permissions = userStore.permissions || []
  
  if (permissions.includes('*:*:*')) return true
  
  return roles.some(role => 
    role === '超级管理员' || 
    role === 'admin' || 
    role === 'ADMIN' ||
    role === 'super_admin'
  )
}

// 检查是否可以管理内容
function canManageContent(userStore) {
  const roles = userStore.roles || []
  const permissions = userStore.permissions || []
  
  if (permissions.includes('*:*:*')) return true
  if (permissions.some(p => p.startsWith('article:'))) return true
  
  return roles.some(role => 
    role === '超级管理员' || 
    role === 'admin' || 
    role === '内容编辑' ||
    role === 'editor'
  )
}

router.beforeEach(async (to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 博客管理系统` : '博客管理系统'
  
  const token = getToken()
  
  // 如果是登录页
  if (to.path === '/login') {
    if (token) {
      next('/dashboard')
    } else {
      next()
    }
    return
  }
  
  // 如果没有token，跳转到登录页
  if (!token) {
    next('/login')
    return
  }
  
  // 获取用户信息
  const userStore = useUserStore()
  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch (e) {
      next('/login')
      return
    }
  }
  
  // 检查权限
  if (to.meta.requiresAdmin && !isAdmin(userStore)) {
    ElMessage.error('您没有权限访问该页面')
    next('/dashboard')
    return
  }
  
  if (to.meta.requiresContent && !canManageContent(userStore)) {
    ElMessage.error('您没有权限访问该页面')
    next('/dashboard')
    return
  }
  
  next()
})

export default router
