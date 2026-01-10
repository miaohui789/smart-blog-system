import { createRouter, createWebHistory } from 'vue-router'
import AdminLayout from '@/layouts/AdminLayout.vue'

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
        meta: { title: '文章管理', icon: 'Document' },
        children: [
          { path: 'list', name: 'ArticleList', component: () => import('@/views/Article/List.vue'), meta: { title: '文章列表' } },
          { path: 'create', name: 'ArticleCreate', component: () => import('@/views/Article/Create.vue'), meta: { title: '创建文章' } },
          { path: 'edit/:id', name: 'ArticleEdit', component: () => import('@/views/Article/Edit.vue'), meta: { title: '编辑文章' } }
        ]
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('@/views/Category/index.vue'),
        meta: { title: '分类管理', icon: 'Folder' }
      },
      {
        path: 'tag',
        name: 'Tag',
        component: () => import('@/views/Tag/index.vue'),
        meta: { title: '标签管理', icon: 'PriceTag' }
      },
      {
        path: 'comment',
        name: 'Comment',
        component: () => import('@/views/Comment/index.vue'),
        meta: { title: '评论管理', icon: 'ChatDotRound' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/User/index.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'system',
        name: 'System',
        redirect: '/system/role',
        meta: { title: '系统管理', icon: 'Setting' },
        children: [
          { path: 'role', name: 'Role', component: () => import('@/views/System/Role/index.vue'), meta: { title: '角色管理' } },
          { path: 'menu', name: 'Menu', component: () => import('@/views/System/Menu/index.vue'), meta: { title: '菜单管理' } },
          { path: 'config', name: 'Config', component: () => import('@/views/System/Config/index.vue'), meta: { title: '系统配置' } },
          { path: 'log', name: 'Log', component: () => import('@/views/System/Log/index.vue'), meta: { title: '操作日志' } }
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
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 博客管理系统` : '博客管理系统'
  next()
})

export default router
