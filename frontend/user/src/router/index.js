import { createRouter, createWebHistory } from 'vue-router'
import DefaultLayout from '@/layouts/DefaultLayout.vue'

const routes = [
  {
    path: '/',
    component: DefaultLayout,
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home/index.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'article/:id',
        name: 'Article',
        component: () => import('@/views/Article/index.vue'),
        meta: { title: '文章详情' }
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('@/views/Category/index.vue'),
        meta: { title: '分类' }
      },
      {
        path: 'category/:id',
        name: 'CategoryDetail',
        component: () => import('@/views/Category/Detail.vue'),
        meta: { title: '分类文章' }
      },
      {
        path: 'tag',
        name: 'Tag',
        component: () => import('@/views/Tag/index.vue'),
        meta: { title: '标签' }
      },
      {
        path: 'tag/:id',
        name: 'TagDetail',
        component: () => import('@/views/Tag/Detail.vue'),
        meta: { title: '标签文章' }
      },
      {
        path: 'archive',
        name: 'Archive',
        component: () => import('@/views/Archive/index.vue'),
        meta: { title: '归档' }
      },
      {
        path: 'about',
        name: 'About',
        component: () => import('@/views/About/index.vue'),
        meta: { title: '关于' }
      },
      {
        path: 'search',
        name: 'Search',
        component: () => import('@/views/Search/index.vue'),
        meta: { title: '搜索' }
      },
      {
        path: 'user',
        name: 'User',
        redirect: '/user/profile',
        children: [
          {
            path: 'profile',
            name: 'UserProfile',
            component: () => import('@/views/User/Profile.vue'),
            meta: { title: '个人资料', requiresAuth: true }
          },
          {
            path: 'favorites',
            name: 'UserFavorites',
            component: () => import('@/views/User/Favorites.vue'),
            meta: { title: '我的收藏', requiresAuth: true }
          },
          {
            path: 'settings',
            name: 'UserSettings',
            component: () => import('@/views/User/Settings.vue'),
            meta: { title: '设置', requiresAuth: true }
          },
          {
            path: 'articles',
            name: 'UserArticles',
            component: () => import('@/views/User/Articles.vue'),
            meta: { title: '我的文章', requiresAuth: true }
          }
        ]
      },
      {
        path: 'write',
        name: 'WriteArticle',
        component: () => import('@/views/Write/index.vue'),
        meta: { title: '写文章', requiresAuth: true }
      },
      {
        path: 'write/:id',
        name: 'EditArticle',
        component: () => import('@/views/Write/index.vue'),
        meta: { title: '编辑文章', requiresAuth: true }
      }
    ]
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register/index.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/Error/404.vue'),
    meta: { title: '页面不存在' }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 我的博客` : '我的博客'
  next()
})

export default router
