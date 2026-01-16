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
            name: 'UserProfileSelf',
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
          },
          {
            path: 'following',
            name: 'UserFollowing',
            component: () => import('@/views/User/Following.vue'),
            meta: { title: '我的关注', requiresAuth: true }
          }
        ]
      },
      {
        path: 'user/:userId',
        name: 'UserProfile',
        component: () => import('@/views/User/UserProfile.vue'),
        meta: { title: '用户主页' }
      },
      {
        path: 'user/:userId/followers',
        name: 'UserFollowers',
        component: () => import('@/views/User/Followers.vue'),
        meta: { title: '粉丝列表' }
      },
      {
        path: 'user/:userId/following',
        name: 'UserFollowingList',
        component: () => import('@/views/User/FollowingList.vue'),
        meta: { title: '关注列表' }
      },
      {
        path: 'message',
        name: 'MessageList',
        component: () => import('@/views/Message/index.vue'),
        meta: { title: '私信', requiresAuth: true }
      },
      {
        path: 'message/:userId',
        name: 'MessageChat',
        component: () => import('@/views/Message/index.vue'),
        meta: { title: '私信', requiresAuth: true }
      },
      {
        path: 'notification',
        name: 'Notification',
        component: () => import('@/views/Notification/index.vue'),
        meta: { title: '消息通知', requiresAuth: true }
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
      },
      {
        path: 'vip',
        name: 'Vip',
        redirect: '/vip/center',
        children: [
          {
            path: 'activate',
            name: 'VipActivate',
            component: () => import('@/views/Vip/Activate.vue'),
            meta: { title: 'VIP激活', requiresAuth: true }
          },
          {
            path: 'center',
            name: 'VipCenter',
            component: () => import('@/views/Vip/Center.vue'),
            meta: { title: 'VIP中心', requiresAuth: true }
          }
        ]
      }
    ]
  },
  {
    path: '/ai',
    name: 'AI',
    component: () => import('@/views/AI/index.vue'),
    meta: { title: 'AI助手' }
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Auth/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Auth/index.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/Auth/index.vue'),
    meta: { title: '忘记密码' }
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
  scrollBehavior(to, from, savedPosition) {
    // 如果有保存的位置（浏览器后退/前进），使用保存的位置
    if (savedPosition) {
      return new Promise((resolve) => {
        setTimeout(() => {
          resolve({ ...savedPosition, behavior: 'smooth' })
        }, 100)
      })
    }
    // 否则滚动到顶部，带平滑效果
    return new Promise((resolve) => {
      setTimeout(() => {
        resolve({ top: 0, behavior: 'smooth' })
      }, 100)
    })
  }
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 我的博客` : '我的博客'
  next()
})

export default router
