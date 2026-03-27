<template>
  <header class="header">
    <div class="header-container">
      <!-- 左侧：Logo + 搜索框 -->
      <div class="header-left">
        <router-link to="/" class="logo">
          <img v-if="configStore.siteLogo" :src="configStore.siteLogo" alt="logo" class="logo-img" />
          <span v-else class="logo-icon">✦</span>
          <span class="logo-text">{{ configStore.siteName }}</span>
        </router-link>
        
        <div class="search-box" @click="openSearch">
          <el-icon><Search /></el-icon>
          <span>搜索</span>
          <kbd>Ctrl K</kbd>
        </div>
        
        <!-- AI助手入口 -->
        <router-link to="/ai" class="ai-btn" title="AI助手">
          <strong>AI</strong>
          <div id="container-stars">
            <div id="stars"></div>
          </div>
          <div id="glow">
            <div class="circle"></div>
            <div class="circle"></div>
          </div>
        </router-link>
      </div>

      <!-- 中间：导航菜单 -->
      <nav class="nav-tabs">
        <input type="radio" id="nav-home" name="nav-tabs" :checked="currentNav === '/'" @change="navigateTo('/')">
        <label class="nav-tab" for="nav-home">首页</label>
        <input type="radio" id="nav-study" name="nav-tabs" :checked="currentNav === '/study'" @change="navigateTo('/study')">
        <label class="nav-tab" for="nav-study">学习</label>
        <input type="radio" id="nav-category" name="nav-tabs" :checked="currentNav === '/category'" @change="navigateTo('/category')">
        <label class="nav-tab" for="nav-category">分类</label>
        <input type="radio" id="nav-tag" name="nav-tabs" :checked="currentNav === '/tag'" @change="navigateTo('/tag')">
        <label class="nav-tab" for="nav-tag">标签</label>
        <input type="radio" id="nav-archive" name="nav-tabs" :checked="currentNav === '/archive'" @change="navigateTo('/archive')">
        <label class="nav-tab" for="nav-archive">归档</label>
        <input type="radio" id="nav-about" name="nav-tabs" :checked="currentNav === '/about'" @change="navigateTo('/about')">
        <label class="nav-tab" for="nav-about">关于</label>
        <span class="nav-glider" :style="gliderStyle"></span>
      </nav>

      <!-- 右侧：功能按钮 -->
      <div class="header-right">
        <!-- 消息通知铃铛下拉菜单 -->
        <el-dropdown v-if="userStore.isLoggedIn" trigger="click" @command="handleBellCommand">
          <div class="notification-bell" title="消息中心">
            <el-icon :size="20"><Bell /></el-icon>
            <span v-if="totalBellUnread > 0" class="bell-badge">{{ totalBellUnread > 99 ? '99+' : totalBellUnread }}</span>
          </div>
          <template #dropdown>
            <el-dropdown-menu class="bell-dropdown">
              <el-dropdown-item command="notification">
                <el-icon><Bell /></el-icon>
                <span>系统通知</span>
                <span v-if="notificationUnread > 0" class="dropdown-badge">{{ notificationUnread > 99 ? '99+' : notificationUnread }}</span>
              </el-dropdown-item>
              <el-dropdown-item command="message">
                <el-icon><ChatDotRound /></el-icon>
                <span>私信消息</span>
                <span v-if="unreadCount > 0" class="dropdown-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <!-- 主题切换开关 -->
        <label class="theme-switch" @click.prevent="toggleTheme">
          <input 
            type="checkbox" 
            class="theme-switch__checkbox"
            :checked="themeStore.isDark"
          >
          <div class="theme-switch__container">
            <div class="theme-switch__clouds"></div>
            <div class="theme-switch__stars-container">
              <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 144 55" fill="none">
                <path fill-rule="evenodd" clip-rule="evenodd" d="M135.831 3.00688C135.055 3.85027 134.111 4.29946 133 4.35447C134.111 4.40947 135.055 4.85867 135.831 5.71123C136.607 6.55462 136.996 7.56303 136.996 8.72727C136.996 7.95722 137.172 7.25134 137.525 6.59129C137.886 5.93124 138.372 5.39954 138.98 5.00535C139.598 4.60199 140.268 4.39114 141 4.35447C139.88 4.2903 138.936 3.85027 138.16 3.00688C137.384 2.16348 136.996 1.16425 136.996 0C136.996 1.16425 136.607 2.16348 135.831 3.00688ZM31 23.3545C32.1114 23.2995 33.0551 22.8503 33.8313 22.0069C34.6075 21.1635 34.9956 20.1642 34.9956 19C34.9956 20.1642 35.3837 21.1635 36.1599 22.0069C36.9361 22.8503 37.8798 23.2903 39 23.3545C38.2679 23.3911 37.5976 23.602 36.9802 24.0053C36.3716 24.3995 35.8864 24.9312 35.5248 25.5913C35.172 26.2513 34.9956 26.9572 34.9956 27.7273C34.9956 26.563 34.6075 25.5546 33.8313 24.7112C33.0551 23.8587 32.1114 23.4095 31 23.3545ZM0 36.3545C1.11136 36.2995 2.05513 35.8503 2.83131 35.0069C3.6075 34.1635 3.99559 33.1642 3.99559 32C3.99559 33.1642 4.38368 34.1635 5.15987 35.0069C5.93605 35.8503 6.87982 36.2903 8 36.3545C7.26792 36.3911 6.59757 36.602 5.98015 37.0053C5.37155 37.3995 4.88644 37.9312 4.52481 38.5913C4.172 39.2513 3.99559 39.9572 3.99559 40.7273C3.99559 39.563 3.6075 38.5546 2.83131 37.7112C2.05513 36.8587 1.11136 36.4095 0 36.3545ZM56.8313 24.0069C56.0551 24.8503 55.1114 25.2995 54 25.3545C55.1114 25.4095 56.0551 25.8587 56.8313 26.7112C57.6075 27.5546 57.9956 28.563 57.9956 29.7273C57.9956 28.9572 58.172 28.2513 58.5248 27.5913C58.8864 26.9312 59.3716 26.3995 59.9802 26.0053C60.5976 25.602 61.2679 25.3911 62 25.3545C60.8798 25.2903 59.9361 24.8503 59.1599 24.0069C58.3837 23.1635 57.9956 22.1642 57.9956 21C57.9956 22.1642 57.6075 23.1635 56.8313 24.0069ZM81 25.3545C82.1114 25.2995 83.0551 24.8503 83.8313 24.0069C84.6075 23.1635 84.9956 22.1642 84.9956 21C84.9956 22.1642 85.3837 23.1635 86.1599 24.0069C86.9361 24.8503 87.8798 25.2903 89 25.3545C88.2679 25.3911 87.5976 25.602 86.9802 26.0053C86.3716 26.3995 85.8864 26.9312 85.5248 27.5913C85.172 28.2513 84.9956 28.9572 84.9956 29.7273C84.9956 28.563 84.6075 27.5546 83.8313 26.7112C83.0551 25.8587 82.1114 25.4095 81 25.3545ZM136 36.3545C137.111 36.2995 138.055 35.8503 138.831 35.0069C139.607 34.1635 139.996 33.1642 139.996 32C139.996 33.1642 140.384 34.1635 141.16 35.0069C141.936 35.8503 142.88 36.2903 144 36.3545C143.268 36.3911 142.598 36.602 141.98 37.0053C141.372 37.3995 140.886 37.9312 140.525 38.5913C140.172 39.2513 139.996 39.9572 139.996 40.7273C139.996 39.563 139.607 38.5546 138.831 37.7112C138.055 36.8587 137.111 36.4095 136 36.3545ZM101.831 49.0069C101.055 49.8503 100.111 50.2995 99 50.3545C100.111 50.4095 101.055 50.8587 101.831 51.7112C102.607 52.5546 102.996 53.563 102.996 54.7273C102.996 53.9572 103.172 53.2513 103.525 52.5913C103.886 51.9312 104.372 51.3995 104.98 51.0053C105.598 50.602 106.268 50.3911 107 50.3545C105.88 50.2903 104.936 49.8503 104.16 49.0069C103.384 48.1635 102.996 47.1642 102.996 46C102.996 47.1642 102.607 48.1635 101.831 49.0069Z" fill="currentColor"></path>
              </svg>
            </div>
            <div class="theme-switch__circle-container">
              <div class="theme-switch__sun-moon-container">
                <div class="theme-switch__moon">
                  <div class="theme-switch__spot"></div>
                  <div class="theme-switch__spot"></div>
                  <div class="theme-switch__spot"></div>
                </div>
              </div>
            </div>
          </div>
        </label>

        <!-- 外部链接 -->
        <a href="https://github.com/miaohui789" target="_blank" class="external-link github-link" title="GitHub">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
            <path d="M12 0C5.37 0 0 5.37 0 12c0 5.31 3.435 9.795 8.205 11.385.6.105.825-.255.825-.57 0-.285-.015-1.23-.015-2.235-3.015.555-3.795-.735-4.035-1.41-.135-.345-.72-1.41-1.23-1.695-.42-.225-1.02-.78-.015-.795.945-.015 1.62.87 1.845 1.23 1.08 1.815 2.805 1.305 3.495.99.105-.78.42-1.305.765-1.605-2.67-.3-5.46-1.335-5.46-5.925 0-1.305.465-2.385 1.23-3.225-.12-.3-.54-1.53.12-3.18 0 0 1.005-.315 3.3 1.23.96-.27 1.98-.405 3-.405s2.04.135 3 .405c2.295-1.56 3.3-1.23 3.3-1.23.66 1.65.24 2.88.12 3.18.765.84 1.23 1.905 1.23 3.225 0 4.605-2.805 5.625-5.475 5.925.435.375.81 1.095.81 2.22 0 1.605-.015 2.895-.015 3.3 0 .315.225.69.825.57A12.02 12.02 0 0024 12c0-6.63-5.37-12-12-12z"/>
          </svg>
        </a>
        <a href="https://gitee.com/miao-huiyi" target="_blank" class="external-link gitee-link" title="Gitee">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
            <path d="M11.984 0A12 12 0 0 0 0 12a12 12 0 0 0 12 12 12 12 0 0 0 12-12A12 12 0 0 0 12 0a12 12 0 0 0-.016 0zm6.09 5.333c.328 0 .593.266.592.593v1.482a.594.594 0 0 1-.593.592H9.777c-.982 0-1.778.796-1.778 1.778v5.63c0 .327.266.592.593.592h5.63c.982 0 1.778-.796 1.778-1.778v-.296a.593.593 0 0 0-.592-.593h-4.15a.592.592 0 0 1-.592-.592v-1.482a.593.593 0 0 1 .593-.592h6.815c.327 0 .593.265.593.592v3.408a4 4 0 0 1-4 4H5.926a.593.593 0 0 1-.593-.593V9.778a4.444 4.444 0 0 1 4.445-4.444h8.296z"/>
          </svg>
        </a>
        <a href="https://space.bilibili.com/1589988727" target="_blank" class="external-link bilibili-link" title="Bilibili">
          <svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor">
            <path d="M17.813 4.653h.854c1.51.054 2.769.578 3.773 1.574 1.004.995 1.524 2.249 1.56 3.76v7.36c-.036 1.51-.556 2.769-1.56 3.773s-2.262 1.524-3.773 1.56H5.333c-1.51-.036-2.769-.556-3.773-1.56S.036 18.858 0 17.347v-7.36c.036-1.511.556-2.765 1.56-3.76 1.004-.996 2.262-1.52 3.773-1.574h.774l-1.174-1.12a1.234 1.234 0 0 1-.373-.906c0-.356.124-.659.373-.907l.027-.027c.267-.249.573-.373.92-.373.347 0 .653.124.92.373L9.653 4.44c.071.071.134.142.187.213h4.267a.836.836 0 0 1 .16-.213l2.853-2.747c.267-.249.573-.373.92-.373.347 0 .662.151.929.4.267.249.391.551.391.907 0 .355-.124.657-.373.906zM5.333 7.24c-.746.018-1.373.276-1.88.773-.506.498-.769 1.13-.786 1.894v7.52c.017.764.28 1.395.786 1.893.507.498 1.134.756 1.88.773h13.334c.746-.017 1.373-.275 1.88-.773.506-.498.769-1.129.786-1.893v-7.52c-.017-.765-.28-1.396-.786-1.894-.507-.497-1.134-.755-1.88-.773zM8 11.107c.373 0 .684.124.933.373.25.249.383.569.4.96v1.173c-.017.391-.15.711-.4.96-.249.25-.56.374-.933.374s-.684-.125-.933-.374c-.25-.249-.383-.569-.4-.96V12.44c0-.373.129-.689.386-.947.258-.257.574-.386.947-.386zm8 0c.373 0 .684.124.933.373.25.249.383.569.4.96v1.173c-.017.391-.15.711-.4.96-.249.25-.56.374-.933.374s-.684-.125-.933-.374c-.25-.249-.383-.569-.4-.96V12.44c.017-.391.15-.711.4-.96.249-.249.56-.373.933-.373z"/>
          </svg>
        </a>
        
        <template v-if="userStore.isLoggedIn">
          <router-link to="/write" class="write-btn">
            <div class="svg-wrapper-1">
              <div class="svg-wrapper">
                <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" width="20" height="20">
                  <path fill="none" d="M0 0h24v24H0z"></path>
                  <path fill="currentColor" d="M1.946 9.315c-.522-.174-.527-.455.01-.634l19.087-6.362c.529-.176.832.12.684.638l-5.454 19.086c-.15.529-.455.547-.679.045L12 14l6-8-8 6-8.054-2.685z"></path>
                </svg>
              </div>
            </div>
            <span>写文章</span>
          </router-link>
          
          <!-- 用户头像悬浮卡片 -->
          <div class="user-avatar-wrapper" @mouseenter="handleUserCardEnter" @mouseleave="handleUserCardLeave">
            <div class="user-avatar-trigger" @click.stop.prevent="handleUserAvatarClick" :class="{ 'is-active': showUserCard }">
              <div class="user-avatar" :class="'vip-level-' + (userStore.userInfo?.vipLevel || 0)">
                <img 
                  v-if="userStore.userInfo?.avatar" 
                  :src="userStore.userInfo.avatar" 
                  @error="onAvatarError" 
                />
                <span v-else>{{ avatarText }}</span>
                <!-- VIP头像装饰框 -->
                <div v-if="userStore.userInfo?.vipLevel > 0" class="avatar-vip-frame" :class="'level-' + userStore.userInfo.vipLevel"></div>
                <!-- VIP小图标 -->
                <div v-if="userStore.userInfo?.vipLevel > 0" class="avatar-vip-badge" :class="'level-' + userStore.userInfo.vipLevel">
                  <svg viewBox="0 0 24 24">
                    <path fill="currentColor" d="M5 16L3 5l5.5 5L12 4l3.5 6L21 5l-2 11H5m14 3c0 .6-.4 1-1 1H6c-.6 0-1-.4-1-1v-1h14v1z"/>
                  </svg>
                </div>
              </div>
            </div>
            
            <!-- 悬浮卡片 -->
            <Transition name="user-card">
              <div v-show="showUserCard" class="user-card" :class="'vip-card-' + (userStore.userInfo?.vipLevel || 0)" @mouseenter="handleUserCardEnter" @mouseleave="handleUserCardLeave" @click="handleCardClick">
                <!-- VIP专属背景装饰 -->
                <div v-if="userStore.userInfo?.vipLevel > 0" class="vip-card-bg" :class="'level-' + userStore.userInfo.vipLevel"></div>
                <div class="user-card-header" :class="'vip-header-' + (userStore.userInfo?.vipLevel || 0)">
                  <div class="user-card-avatar" :class="'vip-level-' + (userStore.userInfo?.vipLevel || 0)" @click="goToMyPage">
                    <img 
                      v-if="userStore.userInfo?.avatar" 
                      :src="userStore.userInfo.avatar" 
                      @error="onAvatarError" 
                    />
                    <span v-else>{{ avatarText }}</span>
                    <!-- 卡片内VIP头像框 -->
                    <div v-if="userStore.userInfo?.vipLevel > 0" class="card-avatar-frame" :class="'level-' + userStore.userInfo.vipLevel"></div>
                  </div>
                  <div class="user-card-info">
                    <VipUsername 
                      :username="userStore.userInfo?.nickname || '用户'" 
                      :vipLevel="userStore.userInfo?.vipLevel || 0"
                      class="user-card-name"
                    />
                    <p class="user-card-bio">{{ userStore.userInfo?.intro || '这个人很懒，什么都没写~' }}</p>
                    <!-- VIP到期时间 -->
                    <p v-if="userStore.userInfo?.vipLevel > 0 && userStore.userInfo?.vipExpireTime" class="vip-expire-info">
                      <span class="vip-expire-label">会员到期：</span>
                      <span class="vip-expire-date">{{ formatVipExpireDate(userStore.userInfo.vipExpireTime) }}</span>
                    </p>
                  </div>
                </div>
                
                <div class="user-card-stats">
                  <div class="stat-item" @click="goToFollowing">
                    <span class="stat-value">{{ userStats.followingCount || 0 }}</span>
                    <span class="stat-label">关注</span>
                  </div>
                  <div class="stat-item" @click="goToFollowers">
                    <span class="stat-value">{{ userStats.followerCount || 0 }}</span>
                    <span class="stat-label">粉丝</span>
                  </div>
                  <div class="stat-item" @click="goToArticles">
                    <span class="stat-value">{{ userStats.articleCount || 0 }}</span>
                    <span class="stat-label">文章</span>
                  </div>
                </div>
                
                <div class="user-card-menu">
                  <div class="menu-item" @click="handleCommand('profile')">
                    <el-icon><User /></el-icon>
                    <span>个人中心</span>
                  </div>
                  <div class="menu-item" @click="handleCommand('articles')">
                    <el-icon><Document /></el-icon>
                    <span>投稿管理</span>
                  </div>
                  <div class="menu-item" @click="handleCommand('message')">
                    <el-icon><ChatDotRound /></el-icon>
                    <span>私信消息</span>
                  </div>
                  <div class="menu-item" @click="handleCommand('favorites')">
                    <el-icon><Star /></el-icon>
                    <span>我的收藏</span>
                  </div>
                  <div class="menu-item" @click="handleCommand('study')">
                    <el-icon><Reading /></el-icon>
                    <span>我的学习</span>
                  </div>
                  <div class="menu-item" @click="handleCommand('vip')">
                    <el-icon><Medal /></el-icon>
                    <span>会员中心</span>
                  </div>
                  <div class="menu-item" @click="handleCommand('skin')">
                    <el-icon><Picture /></el-icon>
                    <span>我的皮肤</span>
                  </div>
                  <div class="menu-item" @click="handleCommand('settings')">
                    <el-icon><Setting /></el-icon>
                    <span>设置</span>
                  </div>
                </div>
                
                <div class="user-card-footer">
                  <button class="logout-btn" @click="handleCommand('logout')">
                    <el-icon><SwitchButton /></el-icon>
                    <span>退出登录</span>
                  </button>
                </div>
              </div>
            </Transition>
          </div>
        </template>
        <template v-else>
          <router-link to="/login" class="login-btn">登录</router-link>
        </template>
        <button
          class="mobile-menu-btn"
          :class="{ active: showMobileMenu }"
          type="button"
          @click="toggleMobileMenu"
        >
          <el-icon v-if="showMobileMenu"><Close /></el-icon>
          <el-icon v-else><Menu /></el-icon>
        </button>
      </div>
    </div>

    <!-- 搜索弹窗 -->
    <Teleport to="body">
      <Transition name="search-fade">
        <div v-if="showSearch" class="search-overlay" @click.self="closeSearch">
          <div class="search-modal">
            <div class="search-input-wrapper">
              <el-icon class="search-icon"><Search /></el-icon>
              <input
                ref="searchInputRef"
                v-model="keyword"
                type="text"
                class="search-input"
                placeholder="搜索文章、面试题、用户..."
                @keyup.enter="doSearch"
                @keyup.esc="closeSearch"
              />
              <kbd v-if="!keyword" class="esc-hint">ESC</kbd>
            </div>

            <div v-if="keyword" class="search-results-shell">
              <div class="search-meta-bar">
                <span>{{ searching ? '正在搜索...' : `找到 ${searchResults.length + studyResults.length + userResults.length} 条即时结果` }}</span>
                <button v-if="keyword" class="search-page-link" @click="goToSearchPage">
                  查看全部
                </button>
              </div>

              <div class="search-scroll">
                <!-- 用户搜索结果 -->
                <div v-if="userResults.length" class="search-section">
                  <div class="search-section-title">
                    <el-icon><User /></el-icon>
                    <span>用户</span>
                  </div>
                  <div class="search-users">
                    <router-link
                      v-for="user in userResults"
                      :key="user.id"
                      :to="`/user/${user.id}`"
                      class="search-user-item"
                      @click="closeSearch"
                    >
                      <div class="user-avatar-small" :class="'vip-level-' + (user.vipLevel || 0)">
                        <img v-if="user.avatar" :src="user.avatar" :alt="user.nickname" />
                        <span v-else>{{ (user.nickname || user.username || 'U').charAt(0).toUpperCase() }}</span>
                      </div>
                      <div class="user-info-small">
                        <div class="user-name-row">
                          <VipUsername 
                            :username="user.nickname || user.username" 
                            :vipLevel="user.vipLevel || 0"
                          />
                        </div>
                        <p class="user-intro">{{ user.intro || '这个人很懒，什么都没写~' }}</p>
                      </div>
                    </router-link>
                  </div>
                </div>
                
                <!-- 文章搜索结果 -->
                <div v-if="searchResults.length" class="search-section">
                  <div class="search-section-title">
                    <el-icon><Document /></el-icon>
                    <span>文章</span>
                  </div>
                  <div class="search-articles">
                    <router-link
                      v-for="item in searchResults"
                      :key="item.id"
                      :to="`/article/${item.id}`"
                      class="search-result-item"
                      @click="closeSearch"
                    >
                      <h4 v-html="item.titleHighlight || highlightKeyword(item.title)"></h4>
                      <p v-html="item.summaryHighlight || highlightKeyword(item.summary || '')"></p>
                      <div v-if="item.tags && item.tags.length" class="search-tags">
                        <span 
                          v-for="tag in item.tags" 
                          :key="tag.id" 
                          class="search-tag"
                          :class="{ 'tag-matched': isTagMatched(tag.name) }"
                          :style="{ '--tag-color': tag.color || '#a855f7' }"
                        >
                          {{ tag.name }}
                        </span>
                      </div>
                    </router-link>
                  </div>
                </div>

                <!-- 面试题搜索结果 -->
                <div v-if="studyResults.length" class="search-section">
                  <div class="search-section-title">
                    <el-icon><Reading /></el-icon>
                    <span>面试题</span>
                  </div>
                  <div class="search-articles">
                    <router-link
                      v-for="item in studyResults"
                      :key="item.id"
                      :to="`/study/learn/${item.id}`"
                      class="search-result-item search-study-item"
                      @click="closeSearch"
                    >
                      <h4 v-html="item.titleHighlight || highlightKeyword(item.title)"></h4>
                      <p v-html="item.answerSummaryHighlight || highlightKeyword(item.answerSummary || '')"></p>
                      <div class="search-tags">
                        <span class="search-tag">{{ item.categoryName || '未分类' }}</span>
                        <span v-if="item.questionCode" class="search-tag">{{ item.questionCode }}</span>
                        <span v-if="item.difficulty" class="search-tag">难度 {{ item.difficulty }}</span>
                      </div>
                    </router-link>
                  </div>
                </div>
                
                <div v-if="!searching && !userResults.length && !searchResults.length && !studyResults.length" class="search-empty">
                  未找到相关内容
                </div>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <Teleport to="body">
      <Transition name="mobile-menu-fade">
        <div v-if="showMobileMenu" class="mobile-menu-overlay" @click.self="closeMobileMenu">
          <div class="mobile-menu-panel">
            <div class="mobile-menu-head">
              <div class="mobile-menu-title">
                <span class="mobile-menu-site">{{ configStore.siteName }}</span>
              </div>
              <button class="mobile-menu-close" type="button" @click="closeMobileMenu">
                <el-icon><Close /></el-icon>
              </button>
            </div>

            <div class="mobile-menu-content">
              <section class="mobile-menu-section">
                <div class="mobile-menu-section-title">站点导航</div>
                <nav class="mobile-nav-list">
                  <button
                    v-for="item in navEntries"
                    :key="item.path"
                    class="mobile-nav-item"
                    :class="{ active: currentNav === item.path }"
                    type="button"
                    @click="navigateTo(item.path)"
                  >
                    <span>{{ item.label }}</span>
                    <span class="mobile-nav-arrow">›</span>
                  </button>
                </nav>
              </section>

              <section class="mobile-menu-section">
                <div class="mobile-menu-section-title">快捷入口</div>
                <div class="mobile-action-list">
                  <button class="mobile-action-item" type="button" @click="openSearchFromMenu">
                    <el-icon><Search /></el-icon>
                    <span>搜索内容</span>
                  </button>
                  <button class="mobile-action-item" type="button" @click="navigateTo('/ai')">
                    <span class="mobile-ai-badge">AI</span>
                    <span>AI 助手</span>
                  </button>
                  <button
                    v-if="userStore.isLoggedIn"
                    class="mobile-action-item"
                    type="button"
                    @click="navigateTo('/write')"
                  >
                    <el-icon><Document /></el-icon>
                    <span>写文章</span>
                  </button>
                  <button
                    v-if="userStore.isLoggedIn"
                    class="mobile-action-item"
                    type="button"
                    @click="navigateTo('/notification')"
                  >
                    <el-icon><Bell /></el-icon>
                    <span>消息通知</span>
                    <span v-if="totalBellUnread > 0" class="mobile-action-badge">{{ totalBellUnread > 99 ? '99+' : totalBellUnread }}</span>
                  </button>
                  <button
                    v-if="!userStore.isLoggedIn"
                    class="mobile-action-item"
                    type="button"
                    @click="navigateTo('/login')"
                  >
                    <el-icon><User /></el-icon>
                    <span>登录 / 注册</span>
                  </button>
                </div>
              </section>

              <section v-if="userStore.isLoggedIn" class="mobile-menu-section">
                <div class="mobile-menu-section-title">个人中心</div>
                <div class="mobile-action-list">
                  <button class="mobile-action-item" type="button" @click="handleCommand('profile')">
                    <el-icon><User /></el-icon>
                    <span>个人资料</span>
                  </button>
                  <button class="mobile-action-item" type="button" @click="handleCommand('articles')">
                    <el-icon><Document /></el-icon>
                    <span>投稿管理</span>
                  </button>
                  <button class="mobile-action-item" type="button" @click="handleCommand('favorites')">
                    <el-icon><Star /></el-icon>
                    <span>我的收藏</span>
                  </button>
                  <button class="mobile-action-item" type="button" @click="handleCommand('study')">
                    <el-icon><Reading /></el-icon>
                    <span>我的学习</span>
                  </button>
                  <button class="mobile-action-item" type="button" @click="handleCommand('settings')">
                    <el-icon><Setting /></el-icon>
                    <span>账号设置</span>
                  </button>
                  <button class="mobile-action-item logout" type="button" @click="handleCommand('logout')">
                    <el-icon><SwitchButton /></el-icon>
                    <span>退出登录</span>
                  </button>
                </div>
              </section>

              <section class="mobile-menu-section">
                <div class="mobile-menu-section-title">外部链接</div>
                <div class="mobile-external-list">
                  <a href="https://github.com/miaohui789" target="_blank" class="mobile-external-item" @click="closeMobileMenu">GitHub</a>
                  <a href="https://gitee.com/miao-huiyi" target="_blank" class="mobile-external-item" @click="closeMobileMenu">Gitee</a>
                  <a href="https://space.bilibili.com/1589988727" target="_blank" class="mobile-external-item" @click="closeMobileMenu">Bilibili</a>
                </div>
              </section>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </header>
</template>

<script setup>
import { ref, computed, nextTick, watch, onMounted, onUnmounted } from 'vue'
import { Search, User, Star, Setting, SwitchButton, Document, Medal, ChatDotRound, UserFilled, Connection, Bell, Picture, Reading, Menu, Close } from '@element-plus/icons-vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useThemeStore } from '@/stores/theme'
import { useConfigStore } from '@/stores/config'
import { useNotificationStore } from '@/stores/notification'
import { useMessageStore } from '@/stores/message'
import { searchAll } from '@/api/search'
import { getUserProfile } from '@/api/follow'
import VipUsername from '@/components/VipUsername/index.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const themeStore = useThemeStore()
const configStore = useConfigStore()
const notificationStore = useNotificationStore()
const messageStore = useMessageStore()

const showSearch = ref(false)
const keyword = ref('')
const searchResults = ref([])
const studyResults = ref([])
const userResults = ref([])
const searching = ref(false)
const searchInputRef = ref(null)
const avatarLoadError = ref(false)
const showMobileMenu = ref(false)

// 用户卡片相关
const showUserCard = ref(false)
let userCardTimer = null
const userStats = ref({
  followingCount: 0,
  followerCount: 0,
  articleCount: 0
})

// 显示用户卡片
function handleUserCardEnter() {
  if (window.innerWidth <= 768) return // 移动端不响应悬浮
  if (userCardTimer) {
    clearTimeout(userCardTimer)
    userCardTimer = null
  }
  showUserCard.value = true
  // 每次显示卡片时刷新统计数据
  fetchUserStats()
}

// 延迟隐藏用户卡片
function handleUserCardLeave() {
  if (window.innerWidth <= 768) return // 移动端不响应悬浮
  userCardTimer = setTimeout(() => {
    showUserCard.value = false
  }, 150)
}

// 移动端点击头像显示卡片
function handleUserAvatarClick(e) {
  if (window.innerWidth <= 768) {
    e.stopPropagation() // 阻止事件冒泡
    e.preventDefault()
    
    // 用 nextTick 确保状态切换正常
    nextTick(() => {
      showUserCard.value = !showUserCard.value
      if (showUserCard.value) {
        fetchUserStats()
      }
    })
  } else {
    // 桌面端点击也应该能进入个人主页
    goToMyPage()
  }
}

// 获取用户统计数据
async function fetchUserStats() {
  if (!userStore.isLoggedIn || !userStore.userInfo?.id) return
  try {
    const res = await getUserProfile(userStore.userInfo.id)
    if (res.code === 200 && res.data) {
      userStats.value = {
        followingCount: res.data.followCount || 0,
        followerCount: res.data.fansCount || 0,
        articleCount: res.data.articleCount || 0
      }
    }
  } catch (e) {
    console.error('获取用户统计失败', e)
  }
}

// 跳转到个人主页
function goToMyPage() {
  if (window.innerWidth <= 768) {
    showUserCard.value = false
  }
  router.push(`/user/${userStore.userInfo?.id}`)
}

// 跳转到关注列表
function goToFollowing() {
  if (window.innerWidth <= 768) {
    showUserCard.value = false
  }
  router.push('/user/following')
}

// 跳转到粉丝列表
function goToFollowers() {
  if (window.innerWidth <= 768) {
    showUserCard.value = false
  }
  router.push(`/user/${userStore.userInfo?.id}/followers`)
}

// 跳转到文章列表
function goToArticles() {
  if (window.innerWidth <= 768) {
    showUserCard.value = false
  }
  router.push('/user/articles')
}

// 格式化VIP到期时间
function formatVipExpireDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

// 使用computed直接从store获取未读数
const notificationUnread = computed(() => notificationStore.totalUnread)
const unreadCount = computed(() => messageStore.unreadCount)

// 铃铛总未读数（通知 + 私信）
const totalBellUnread = computed(() => notificationUnread.value + unreadCount.value)

// 获取未读消息数（私信）
async function fetchUnreadCount() {
  if (!userStore.isLoggedIn) return
  await messageStore.fetchUnreadCount()
}

// 获取未读通知数
async function fetchNotificationUnread() {
  if (!userStore.isLoggedIn) return
  await notificationStore.fetchUnreadCount()
}

// 导航相关
const navEntries = [
  { path: '/', label: '首页' },
  { path: '/study', label: '学习' },
  { path: '/category', label: '分类' },
  { path: '/tag', label: '标签' },
  { path: '/archive', label: '归档' },
  { path: '/about', label: '关于' }
]
const navItems = navEntries.map(item => item.path)
const currentNav = computed(() => {
  const path = route.path
  if (path === '/') return '/'
  // 匹配以导航项开头的路径
  const matched = navItems.find(item => item !== '/' && path.startsWith(item))
  // 如果没有匹配到，返回空字符串（滑块隐藏）
  return matched || ''
})

const gliderStyle = computed(() => {
  const index = navItems.indexOf(currentNav.value)
  if (index === -1) {
    // 非导航页面时隐藏滑块
    return { opacity: 0, transform: 'translateX(0)' }
  }
  return { 
    opacity: 1,
    transform: `translateX(${index * 100}%)` 
  }
})

const currentNavLabel = computed(() => {
  return navEntries.find(item => item.path === currentNav.value)?.label || '更多功能'
})

function navigateTo(path) {
  closeMobileMenu()
  router.push(path)
}

const avatarText = computed(() => {
  const nickname = userStore.userInfo?.nickname
  return nickname ? nickname.charAt(0).toUpperCase() : 'U'
})

function toggleTheme(event) {
  themeStore.toggleTheme(event)
}

function onAvatarError() {
  avatarLoadError.value = true
}

watch(() => userStore.userInfo?.avatar, () => {
  avatarLoadError.value = false
})

watch(
  () => route.fullPath,
  () => {
    if (window.innerWidth <= 768) {
      showUserCard.value = false
    }
    closeMobileMenu()
  }
)

// 监听登录状态变化，重新获取用户统计
watch(() => userStore.isLoggedIn, (loggedIn) => {
  if (loggedIn) {
    fetchUserStats()
  } else {
    userStats.value = { followingCount: 0, followerCount: 0, articleCount: 0 }
  }
})

function openSearch() {
  closeMobileMenu()
  showSearch.value = true
  keyword.value = ''
  searchResults.value = []
  studyResults.value = []
  userResults.value = []
  nextTick(() => searchInputRef.value?.focus())
}

// 高亮搜索关键词
function highlightKeyword(text) {
  if (!text || !keyword.value.trim()) return text
  const kw = keyword.value.trim()
  const regex = new RegExp(`(${kw.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi')
  return text.replace(regex, '<mark class="search-highlight">$1</mark>')
}

// 检查标签是否匹配关键词
function isTagMatched(tagName) {
  if (!tagName || !keyword.value.trim()) return false
  return tagName.toLowerCase().includes(keyword.value.trim().toLowerCase())
}

function closeSearch() {
  showSearch.value = false
  searchResults.value = []
  studyResults.value = []
  userResults.value = []
}

function toggleMobileMenu() {
  showMobileMenu.value = !showMobileMenu.value
}

function closeMobileMenu() {
  showMobileMenu.value = false
}

function openSearchFromMenu() {
  closeMobileMenu()
  nextTick(() => openSearch())
}

function goToSearchPage() {
  if (!keyword.value.trim()) return
  closeSearch()
  router.push({
    path: '/search',
    query: { keyword: keyword.value.trim() }
  })
}

async function doSearch() {
  if (!keyword.value.trim()) return
  searching.value = true
  try {
    const res = await searchAll({ keyword: keyword.value, pageSize: 5 })
    searchResults.value = res.data?.articles?.list || []
    studyResults.value = res.data?.studyQuestions?.list || []
    userResults.value = res.data?.users || []
  } catch (e) {
    console.error(e)
  } finally {
    searching.value = false
  }
}

let searchTimer = null
watch(keyword, (val) => {
  clearTimeout(searchTimer)
  if (val.trim()) {
    searchTimer = setTimeout(doSearch, 300)
  } else {
    searchResults.value = []
    studyResults.value = []
    userResults.value = []
  }
})

watch(
  [showSearch, showMobileMenu],
  ([searchOpen, menuOpen]) => {
    document.body.style.overflow = searchOpen || menuOpen ? 'hidden' : ''
  }
)

// 快捷键 Ctrl+K 打开搜索
function handleKeydown(e) {
  if ((e.ctrlKey || e.metaKey) && e.key === 'k') {
    e.preventDefault()
    openSearch()
  }
}

// 定时器引用
let unreadTimer = null

// 点击外部关闭用户卡片
function handleClickOutside(e) {
  if (window.innerWidth <= 768 && showUserCard.value) {
    const card = document.querySelector('.user-card')
    const trigger = document.querySelector('.user-avatar-trigger')
    
    // 如果点击的不是卡片内部，也不是触发按钮，就关闭卡片
    if (
      (!card || !card.contains(e.target)) && 
      (!trigger || !trigger.contains(e.target))
    ) {
      // 加一点延迟，防止和click事件冲突
      setTimeout(() => {
        showUserCard.value = false
      }, 10)
    }
  }
}

// 处理滚动关闭卡片
function handleScroll() {
  if (window.innerWidth <= 768 && showUserCard.value) {
    showUserCard.value = false
  }
}

// 阻止卡片内部点击事件冒泡，防止触发 handleClickOutside
function handleCardClick(e) {
  if (window.innerWidth <= 768) {
    e.stopPropagation()
  }
}

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
  document.addEventListener('click', handleClickOutside)
  document.addEventListener('scroll', handleScroll, { passive: true })
  fetchUnreadCount()
  fetchNotificationUnread()
  fetchUserStats()
  // 定时刷新未读数（每30秒）
  unreadTimer = setInterval(() => {
    fetchUnreadCount()
    fetchNotificationUnread()
  }, 30000)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
  document.removeEventListener('click', handleClickOutside)
  document.removeEventListener('scroll', handleScroll)
  if (unreadTimer) {
    clearInterval(unreadTimer)
    unreadTimer = null
  }
  document.body.style.overflow = ''
})

// 铃铛下拉菜单处理
function handleBellCommand(command) {
  closeMobileMenu()
  if (command === 'notification') {
    router.push('/notification')
  } else if (command === 'message') {
    router.push('/message')
  }
}

function handleCommand(command) {
  if (window.innerWidth <= 768) {
    showUserCard.value = false
  }
  closeMobileMenu()
  switch (command) {
    case 'mypage':
      router.push(`/user/${userStore.userInfo?.id}`)
      break
    case 'profile':
      router.push('/user/profile')
      break
    case 'message':
      router.push('/message')
      break
    case 'following':
      router.push('/user/following')
      break
    case 'vip':
      router.push('/vip/center')
      break
    case 'skin':
      router.push('/user/skin')
      break
    case 'articles':
      router.push('/user/articles')
      break
    case 'favorites':
      router.push('/user/favorites')
      break
    case 'study':
      router.push('/user/study')
      break
    case 'settings':
      router.push('/user/settings')
      break
    case 'logout':
      userStore.logout()
      router.push('/')
      break
  }
}
</script>

<style lang="scss" scoped>
@import '@/assets/styles/variables.scss';

.header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 100;
  padding: calc(env(safe-area-inset-top, 0px) + 14px) 20px 0;
  background: transparent;
  transition: all 0.3s ease;
}

:root[data-theme="light"] .header {
  background: transparent;
}

.header-container {
  max-width: 100%;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: relative;
  background: rgba(24, 24, 27, 0.78);
  border: 1px solid var(--border-color);
  border-radius: 22px;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.14);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
}

:root[data-theme="light"] .header-container {
  background: rgba(255, 255, 255, 0.74);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 24px;
  min-width: 0;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  
  .logo-img {
    width: 28px;
    height: 28px;
    object-fit: contain;
  }
  
  .logo-icon {
    font-size: 22px;
    color: $primary-color;
  }
  
  .logo-text {
    font-size: 18px;
    font-weight: 700;
    color: var(--text-primary);
  }
}

.search-box {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
  
  .el-icon {
    font-size: 14px;
    color: var(--text-muted);
  }
  
  span {
    font-size: 13px;
    color: var(--text-muted);
  }
  
  kbd {
    font-size: 11px;
    color: var(--text-disabled);
    background: var(--bg-card-hover);
    padding: 2px 6px;
    border-radius: 4px;
    margin-left: 16px;
  }
  
  &:hover {
    border-color: $primary-color;
    background: rgba($primary-color, 0.05);
  }
  
  @media (max-width: 768px) {
    span, kbd { display: none; }
    padding: 8px;
  }
}

.ai-btn {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 72px;
  height: 36px;
  overflow: hidden;
  background-size: 300% 300%;
  cursor: pointer;
  backdrop-filter: blur(1rem);
  border-radius: 5rem;
  transition: 0.5s;
  animation: ai-gradient 5s ease infinite;
  border: double 3px transparent;
  background-image: linear-gradient(#212121, #212121), linear-gradient(137.48deg, #ffdb3b 10%, #fe53bb 45%, #8f51ea 67%, #0044ff 87%);
  background-origin: border-box;
  background-clip: content-box, border-box;
  text-decoration: none;
  position: relative;
  
  strong {
    z-index: 2;
    font-size: 14px;
    font-weight: 700;
    letter-spacing: 3px;
    color: #ffffff;
    text-shadow: 0 0 4px white;
  }
  
  #container-stars {
    position: absolute;
    z-index: -1;
    width: 100%;
    height: 100%;
    overflow: hidden;
    transition: 0.5s;
    backdrop-filter: blur(1rem);
    border-radius: 5rem;
  }
  
  #stars {
    position: relative;
    background: transparent;
    width: 200rem;
    height: 200rem;
    
    &::after {
      content: "";
      position: absolute;
      top: -10rem;
      left: -100rem;
      width: 100%;
      height: 100%;
      animation: ai-starRotate 120s linear infinite;
      background-image: radial-gradient(#ffffff 1px, transparent 1%);
      background-size: 18px 18px;
    }
    
    &::before {
      content: "";
      position: absolute;
      top: 0;
      left: -50%;
      width: 170%;
      height: 500%;
      animation: ai-star 80s linear infinite;
      background-image: radial-gradient(#ffffff 1.2px, transparent 1%);
      background-size: 22px 22px;
      opacity: 0.7;
    }
  }
  
  #glow {
    position: absolute;
    display: flex;
    width: 60px;
    
    .circle {
      width: 100%;
      height: 20px;
      filter: blur(1.5rem);
      animation: ai-pulse 4s infinite;
      z-index: -1;
      
      &:nth-of-type(1) {
        background: rgba(254, 83, 186, 0.636);
      }
      
      &:nth-of-type(2) {
        background: rgba(142, 81, 234, 0.704);
      }
    }
  }
  
  &:hover {
    transform: scale(1.08);
    
    #container-stars {
      z-index: 1;
      background-color: #212121;
    }
  }
  
  &:active {
    border: double 3px #fe53bb;
    background-origin: border-box;
    background-clip: content-box, border-box;
    animation: none;
    
    .circle {
      background: #fe53bb;
    }
  }
}

@keyframes ai-gradient {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}

@keyframes ai-pulse {
  0% { transform: scale(0.75); box-shadow: 0 0 0 0 rgba(0, 0, 0, 0.7); }
  70% { transform: scale(1); box-shadow: 0 0 0 10px rgba(0, 0, 0, 0); }
  100% { transform: scale(0.75); box-shadow: 0 0 0 0 rgba(0, 0, 0, 0); }
}

@keyframes ai-star {
  from { transform: translateY(0); }
  to { transform: translateY(-135rem); }
}

@keyframes ai-starRotate {
  from { transform: rotate(360deg); }
  to { transform: rotate(0); }
}

/* 新导航样式 */
.nav-tabs {
  display: flex;
  position: absolute;
  left: 50%;
  transform: translateX(-50%);
  background: var(--bg-card);
  box-shadow: 0 0 1px 0 rgba(168, 85, 247, 0.15), 0 4px 12px 0 rgba(168, 85, 247, 0.1);
  padding: 6px;
  border-radius: 99px;
  
  input[type="radio"] {
    display: none;
  }
  
  @media (max-width: 900px) {
    display: none;
  }
}

.nav-tab {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 32px;
  width: 56px;
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  border-radius: 99px;
  cursor: pointer;
  transition: color 0.2s ease;
  z-index: 2;
  
  &:hover {
    color: var(--text-primary);
  }
}

.nav-tabs input[type="radio"]:checked + .nav-tab {
  color: $primary-color;
}

.nav-glider {
  position: absolute;
  display: flex;
  height: 32px;
  width: 56px;
  background: rgba($primary-color, 0.15);
  z-index: 1;
  border-radius: 99px;
  transition: transform 0.25s ease-out, opacity 0.2s ease;
  left: 6px;
}

:root[data-theme="light"] {
  .nav-tabs {
    background: #fff;
    box-shadow: 0 0 1px 0 rgba(24, 94, 224, 0.15), 0 6px 12px 0 rgba(24, 94, 224, 0.15);
  }
  
  .nav-glider {
    background: #e6eef9;
  }
  
  .nav-tabs input[type="radio"]:checked + .nav-tab {
    color: #185ee0;
  }
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.mobile-menu-btn {
  display: none;
  width: 40px;
  height: 40px;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  background: var(--bg-card);
  color: var(--text-secondary);
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;

  .el-icon {
    font-size: 18px;
  }

  &:hover,
  &.active {
    color: var(--text-primary);
    border-color: rgba($primary-color, 0.3);
    background: rgba($primary-color, 0.08);
  }
}

/* 主题切换按钮 - 太阳月亮动画 */
.theme-switch {
  /* 将按钮从 View Transition 中排除，保持自身动画 */
  view-transition-name: theme-switch;
  
  --toggle-size: 10px;
  --container-width: 5.625em;
  --container-height: 2.5em;
  --container-radius: 6.25em;
  --container-light-bg: #3D7EAE;
  --container-night-bg: #1D1F2C;
  --circle-container-diameter: 3.375em;
  --sun-moon-diameter: 2.125em;
  --sun-bg: #ECCA2F;
  --moon-bg: #C4C9D1;
  --spot-color: #959DB1;
  --circle-container-offset: calc((var(--circle-container-diameter) - var(--container-height)) / 2 * -1);
  --stars-color: #fff;
  --clouds-color: #F3FDFF;
  --back-clouds-color: #AACADF;
  --transition: .5s cubic-bezier(0, -0.02, 0.4, 1.25);
  --circle-transition: .3s cubic-bezier(0, -0.02, 0.35, 1.17);
  
  &, *, *::before, *::after {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
    font-size: var(--toggle-size);
  }
  
  .theme-switch__checkbox {
    display: none;
  }
  
  .theme-switch__container {
    width: var(--container-width);
    height: var(--container-height);
    background-color: var(--container-light-bg);
    border-radius: var(--container-radius);
    overflow: hidden;
    cursor: pointer;
    -webkit-box-shadow: 0em -0.062em 0.062em rgba(0, 0, 0, 0.25), 0em 0.062em 0.125em rgba(255, 255, 255, 0.94);
    box-shadow: 0em -0.062em 0.062em rgba(0, 0, 0, 0.25), 0em 0.062em 0.125em rgba(255, 255, 255, 0.94);
    -webkit-transition: var(--transition);
    -o-transition: var(--transition);
    transition: var(--transition);
    position: relative;
    
    &::before {
      content: "";
      position: absolute;
      z-index: 1;
      inset: 0;
      -webkit-box-shadow: 0em 0.05em 0.187em rgba(0, 0, 0, 0.25) inset;
      box-shadow: 0em 0.05em 0.187em rgba(0, 0, 0, 0.25) inset;
      border-radius: var(--container-radius);
    }
  }
  
  .theme-switch__circle-container {
    width: var(--circle-container-diameter);
    height: var(--circle-container-diameter);
    background-color: rgba(255, 255, 255, 0.1);
    position: absolute;
    left: var(--circle-container-offset);
    top: var(--circle-container-offset);
    border-radius: var(--container-radius);
    -webkit-box-shadow: inset 0 0 0 3.375em rgba(255, 255, 255, 0.1), 0 0 0 0.625em rgba(255, 255, 255, 0.1), 0 0 0 1.25em rgba(255, 255, 255, 0.1);
    box-shadow: inset 0 0 0 3.375em rgba(255, 255, 255, 0.1), 0 0 0 0.625em rgba(255, 255, 255, 0.1), 0 0 0 1.25em rgba(255, 255, 255, 0.1);
    display: -webkit-box;
    display: -ms-flexbox;
    display: flex;
    -webkit-transition: var(--circle-transition);
    -o-transition: var(--circle-transition);
    transition: var(--circle-transition);
    pointer-events: none;
  }
  
  .theme-switch__sun-moon-container {
    pointer-events: auto;
    position: relative;
    z-index: 2;
    width: var(--sun-moon-diameter);
    height: var(--sun-moon-diameter);
    margin: auto;
    border-radius: var(--container-radius);
    background-color: var(--sun-bg);
    -webkit-box-shadow: 0.062em 0.062em 0.062em 0em rgba(254, 255, 239, 0.61) inset, 0em -0.062em 0.062em 0em #a1872a inset;
    box-shadow: 0.062em 0.062em 0.062em 0em rgba(254, 255, 239, 0.61) inset, 0em -0.062em 0.062em 0em #a1872a inset;
    -webkit-filter: drop-shadow(0.062em 0.125em 0.125em rgba(0, 0, 0, 0.25)) drop-shadow(0em 0.062em 0.125em rgba(0, 0, 0, 0.25));
    filter: drop-shadow(0.062em 0.125em 0.125em rgba(0, 0, 0, 0.25)) drop-shadow(0em 0.062em 0.125em rgba(0, 0, 0, 0.25));
    overflow: hidden;
    -webkit-transition: var(--transition);
    -o-transition: var(--transition);
    transition: var(--transition);
  }
  
  .theme-switch__moon {
    -webkit-transform: translateX(100%);
    -ms-transform: translateX(100%);
    transform: translateX(100%);
    width: 100%;
    height: 100%;
    background-color: var(--moon-bg);
    border-radius: inherit;
    box-shadow: 0.062em 0.062em 0.062em 0em rgba(254, 255, 239, 0.61) inset, 0em -0.062em 0.062em 0em #969696 inset;
    -webkit-transition: var(--transition);
    -o-transition: var(--transition);
    transition: var(--transition);
    position: relative;
  }
  
  .theme-switch__spot {
    position: absolute;
    top: 0.75em;
    left: 0.312em;
    width: 0.75em;
    height: 0.75em;
    border-radius: var(--container-radius);
    background-color: var(--spot-color);
    box-shadow: 0em 0.0312em 0.062em rgba(0, 0, 0, 0.25) inset;
    
    &:nth-of-type(2) {
      width: 0.375em;
      height: 0.375em;
      top: 0.937em;
      left: 1.375em;
    }
    
    &:nth-last-of-type(3) {
      width: 0.25em;
      height: 0.25em;
      top: 0.312em;
      left: 0.812em;
    }
  }
  
  .theme-switch__clouds {
    width: 1.25em;
    height: 1.25em;
    background-color: var(--clouds-color);
    border-radius: var(--container-radius);
    position: absolute;
    bottom: -0.625em;
    left: 0.312em;
    box-shadow: 0.937em 0.312em var(--clouds-color), -0.312em -0.312em var(--back-clouds-color), 1.437em 0.375em var(--clouds-color), 0.5em -0.125em var(--back-clouds-color), 2.187em 0 var(--clouds-color), 1.25em -0.062em var(--back-clouds-color), 2.937em 0.312em var(--clouds-color), 2em -0.312em var(--back-clouds-color), 3.625em -0.062em var(--clouds-color), 2.625em 0em var(--back-clouds-color), 4.5em -0.312em var(--clouds-color), 3.375em -0.437em var(--back-clouds-color), 4.625em -1.75em 0 0.437em var(--clouds-color), 4em -0.625em var(--back-clouds-color), 4.125em -2.125em 0 0.437em var(--back-clouds-color);
    -webkit-transition: 0.5s cubic-bezier(0, -0.02, 0.4, 1.25);
    -o-transition: 0.5s cubic-bezier(0, -0.02, 0.4, 1.25);
    transition: 0.5s cubic-bezier(0, -0.02, 0.4, 1.25);
  }
  
  .theme-switch__stars-container {
    position: absolute;
    color: var(--stars-color);
    top: -100%;
    left: 0.312em;
    width: 2.75em;
    height: auto;
    -webkit-transition: var(--transition);
    -o-transition: var(--transition);
    transition: var(--transition);
    -webkit-transform: translateY(0);
    -ms-transform: translateY(0);
    transform: translateY(0);
  }
  
  /* 暗色模式状态 */
  .theme-switch__checkbox:checked + .theme-switch__container {
    background-color: var(--container-night-bg);
  }
  
  .theme-switch__checkbox:checked + .theme-switch__container .theme-switch__circle-container {
    left: calc(100% - var(--circle-container-offset) - var(--circle-container-diameter));
  }
  
  .theme-switch__checkbox:checked + .theme-switch__container .theme-switch__circle-container:hover {
    left: calc(100% - var(--circle-container-offset) - var(--circle-container-diameter) - 0.187em);
  }
  
  .theme-switch__circle-container:hover {
    left: calc(var(--circle-container-offset) + 0.187em);
  }
  
  .theme-switch__checkbox:checked + .theme-switch__container .theme-switch__moon {
    -webkit-transform: translate(0);
    -ms-transform: translate(0);
    transform: translate(0);
  }
  
  .theme-switch__checkbox:checked + .theme-switch__container .theme-switch__clouds {
    bottom: -4.062em;
  }
  
  .theme-switch__checkbox:checked + .theme-switch__container .theme-switch__stars-container {
    top: 50%;
    -webkit-transform: translateY(-50%);
    -ms-transform: translateY(-50%);
    transform: translateY(-50%);
  }
}

.theme-icon-enter-active,
.theme-icon-leave-active {
  transition: all 0.2s ease;
}

.theme-icon-enter-from {
  opacity: 0;
  transform: rotate(-90deg) scale(0.5);
}

.theme-icon-leave-to {
  opacity: 0;
  transform: rotate(90deg) scale(0.5);
}

.notification-bell {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  color: var(--text-secondary);
  text-decoration: none;
  transition: all 0.2s;
  cursor: pointer;
  
  .el-icon {
    transition: transform 0.1s ease;
  }
  
  &:hover {
    color: $primary-color;
    background: rgba($primary-color, 0.1);
    
    .el-icon {
      animation: bell-shake 0.5s ease;
    }
  }
  
  .bell-badge {
    position: absolute;
    top: 2px;
    right: 2px;
    min-width: 16px;
    height: 16px;
    padding: 0 4px;
    background: #ef4444;
    border-radius: 8px;
    font-size: 10px;
    font-weight: 600;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    line-height: 1;
  }
}

@keyframes bell-shake {
  0% { transform: rotate(0); }
  15% { transform: rotate(14deg); }
  30% { transform: rotate(-12deg); }
  45% { transform: rotate(10deg); }
  60% { transform: rotate(-8deg); }
  75% { transform: rotate(4deg); }
  90% { transform: rotate(-2deg); }
  100% { transform: rotate(0); }
}

// 铃铛下拉菜单样式
.bell-dropdown {
  min-width: 180px;
  
  .el-dropdown-menu__item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 12px 16px;
    
    .el-icon {
      font-size: 16px;
    }
    
    span:first-of-type {
      flex: 1;
    }
  }
  
  .dropdown-badge {
    min-width: 18px;
    height: 18px;
    padding: 0 6px;
    background: #ef4444;
    border-radius: 9px;
    font-size: 11px;
    font-weight: 600;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.external-link {
  display: flex;
  align-items: center;
  color: var(--text-secondary);
  text-decoration: none;
  transition: all 0.2s;
  
  svg {
    transition: color 0.2s;
  }
  
  &:hover {
    color: var(--text-primary);
  }
  
  /* GitHub - 紫色 */
  &.github-link:hover {
    color: #a855f7;
  }
  
  /* Gitee - 红色 */
  &.gitee-link:hover {
    color: #c71d23;
  }
  
  /* Bilibili - 粉色 */
  &.bilibili-link:hover {
    color: #fb7299;
  }
}

:root[data-theme="dark"] {
  .external-link.github-link:hover {
    color: #a855f7;
  }
}

.mobile-menu-overlay {
  position: fixed;
  inset: 0;
  z-index: 1001;
  display: flex;
  justify-content: flex-end; /* 改回右侧弹出，更符合移动端习惯 */
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
}

.mobile-menu-panel {
  width: min(70vw, 260px);
  height: 100vh;
  background: rgba(var(--bg-card-rgb), 0.98);
  border-left: 1px solid var(--border-color); /* 改回左边框 */
  box-shadow: -18px 0 48px rgba(15, 23, 42, 0.18); /* 改回阴影方向 */
  display: flex;
  flex-direction: column;
}

.mobile-menu-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 16px 14px 12px;
  border-bottom: 1px solid var(--border-color);
}

.mobile-menu-title {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.mobile-menu-site {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}

.mobile-menu-route {
  display: none;
}

.mobile-menu-close {
  width: 32px;
  height: 32px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: var(--bg-card-hover);
  color: var(--text-secondary);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.mobile-menu-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mobile-menu-section {
  padding: 12px;
  background: var(--bg-card-hover);
  border: 1px solid var(--border-color);
  border-radius: 16px;
}

.mobile-menu-section-title {
  font-size: 11px;
  font-weight: 700;
  color: var(--text-muted);
  letter-spacing: 0.04em;
  margin-bottom: 10px;
}

.mobile-nav-list,
.mobile-action-list,
.mobile-external-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.mobile-nav-item,
.mobile-action-item,
.mobile-external-item {
  width: 100%;
  min-width: 0;
  padding: 10px 12px;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  background: var(--bg-card);
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  cursor: pointer;
  transition: all 0.2s ease;
  font-size: 13px;
}

.mobile-nav-item {
  justify-content: space-between;

  &.active {
    color: $primary-color;
    border-color: rgba($primary-color, 0.26);
    background: rgba($primary-color, 0.08);
  }
}

.mobile-nav-arrow {
  color: var(--text-disabled);
  font-size: 14px;
  line-height: 1;
}

.mobile-action-item {
  justify-content: flex-start;

  .el-icon {
    font-size: 14px;
  }

  &.logout {
    color: #ef4444;
  }
}

.mobile-external-item {
  justify-content: center;
  font-weight: 600;
}

.mobile-action-badge {
  margin-left: auto;
  min-width: 16px;
  height: 16px;
  padding: 0 5px;
  border-radius: 999px;
  background: #ef4444;
  color: #fff;
  font-size: 10px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.mobile-ai-badge {
  width: 18px;
  height: 18px;
  border-radius: 50%;
  background: linear-gradient(135deg, #ffdb3b 10%, #fe53bb 45%, #8f51ea 67%, #0044ff 87%);
  color: #fff;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.mobile-menu-fade-enter-active,
.mobile-menu-fade-leave-active {
  transition: opacity 0.2s ease;

  .mobile-menu-panel {
    transition: transform 0.24s ease;
  }
}

.mobile-menu-fade-enter-from,
.mobile-menu-fade-leave-to {
  opacity: 0;

  .mobile-menu-panel {
    transform: translateX(100%); /* 从右侧滑入 */
  }
}

.write-btn {
  display: flex;
  align-items: center;
  padding: 0.6em 1em;
  padding-left: 0.8em;
  background: $primary-color;
  border-radius: 12px;
  color: white;
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
  overflow: hidden;
  transition: all 0.2s ease;
  cursor: pointer;
  
  .svg-wrapper-1 {
    display: flex;
    align-items: center;
  }
  
  .svg-wrapper {
    display: flex;
    align-items: center;
    transition: transform 0.3s ease-in-out;
  }
  
  svg {
    display: block;
    transform-origin: center center;
    transition: transform 0.3s ease-in-out;
  }
  
  span {
    display: block;
    margin-left: 0.3em;
    transition: all 0.3s ease-in-out;
  }
  
  &:hover {
    background: $primary-dark;
    box-shadow: 0 4px 12px rgba($primary-color, 0.3);
    
    .svg-wrapper {
      animation: fly-1 0.6s ease-in-out infinite alternate;
    }
    
    svg {
      transform: translateX(1.2em) rotate(45deg) scale(1.1);
    }
    
    span {
      transform: translateX(4em);
    }
  }
  
  &:active {
    transform: scale(0.95);
  }
  
  @media (max-width: 768px) {
    span { display: none; }
    padding: 0.6em;
    
    &:hover {
      svg {
        transform: rotate(45deg) scale(1.1);
      }
    }
  }
}

@keyframes fly-1 {
  from {
    transform: translateY(0.1em);
  }
  to {
    transform: translateY(-0.1em);
  }
}

.login-btn {
  padding: 8px 20px;
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.2s ease;
  
  &:hover {
    border-color: $primary-color;
    color: $primary-color;
    background: rgba($primary-color, 0.05);
  }
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

/* 用户头像悬浮卡片 */
.user-avatar-wrapper {
  position: relative;
  z-index: 101;
}

.user-avatar-trigger {
  cursor: pointer;
  position: relative;
  z-index: 101;
  pointer-events: auto;
  -webkit-tap-highlight-color: transparent;
  padding: 4px; // 增加点击区域
  margin: -4px; // 抵消 padding 带来的位移
  display: flex;
  align-items: center;
  justify-content: center;
  touch-action: manipulation; // 优化移动端触摸行为
  
  @media (max-width: 768px) {
    // 移动端进一步增加点击区域
    padding: 8px;
    margin: -8px;
  }
}

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  overflow: visible;
  display: flex;
  align-items: center;
  justify-content: center;
  background: $primary-color;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
  position: relative;
  z-index: 101;
  pointer-events: auto;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 50%;
  }
  
  span {
    font-size: 13px;
    font-weight: 600;
    color: #fff;
  }
  
  /* VIP头像装饰框 */
  .avatar-vip-frame {
    position: absolute;
    inset: -3px;
    border-radius: 50%;
    pointer-events: none;
    
    &.level-1 {
      border: 2px solid #cd7f32;
      box-shadow: 0 0 8px rgba(205, 127, 50, 0.5);
    }
    
    &.level-2 {
      border: 2px solid #c0c0c0;
      box-shadow: 0 0 8px rgba(192, 192, 192, 0.6);
    }
    
    &.level-3 {
      border: 2px solid #ffd700;
      box-shadow: 0 0 12px rgba(255, 215, 0, 0.7);
      animation: vipFrameGlow 2s ease-in-out infinite;
    }
  }
  
  /* VIP小图标 */
  .avatar-vip-badge {
    position: absolute;
    bottom: -2px;
    right: -2px;
    width: 14px;
    height: 14px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    
    svg {
      width: 10px;
      height: 10px;
    }
    
    &.level-1 {
      background: linear-gradient(135deg, #cd7f32, #b8860b);
      color: #fff;
    }
    
    &.level-2 {
      background: linear-gradient(135deg, #e8e8e8, #c0c0c0);
      color: #444;
    }
    
    &.level-3 {
      background: linear-gradient(135deg, #ffd700, #ffb700);
      color: #5c4813;
      animation: vipBadgePulse 2s ease-in-out infinite;
    }
  }
  
  /* VIP等级头像边框动画 */
  &.vip-level-1 {
    box-shadow: 0 0 0 2px rgba(205, 127, 50, 0.3);
  }
  
  &.vip-level-2 {
    box-shadow: 0 0 0 2px rgba(192, 192, 192, 0.3);
  }
  
  &.vip-level-3 {
    box-shadow: 0 0 0 2px rgba(255, 215, 0, 0.3);
  }
}

@keyframes vipFrameGlow {
  0%, 100% { box-shadow: 0 0 8px rgba(255, 215, 0, 0.5); }
  50% { box-shadow: 0 0 16px rgba(255, 215, 0, 0.9); }
}

@keyframes vipBadgePulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.1); }
}

.user-avatar-trigger:hover .user-avatar,
.user-avatar-trigger.is-active .user-avatar {
  transform: scale(1.15);
  box-shadow: 0 4px 12px rgba($primary-color, 0.4);
}

/* 用户悬浮卡片 */
.user-card {
  position: absolute;
  top: 100%;
  right: 0;
  width: 260px; /* 默认也改窄一点 */
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  z-index: 1001;
  overflow: hidden;
  padding-top: 12px;
  margin-top: 8px;
  pointer-events: auto;
  
  @media (max-width: 768px) {
    padding-top: 0;
    margin-top: 0;
    width: 240px; /* 移动端下拉菜单宽度更窄一些 */
  }
  
  &::before {
    content: '';
    position: absolute;
    top: -8px;
    left: 0;
    right: 0;
    height: 20px;
    background: transparent;
  }
  
  &::after {
    content: '';
    position: absolute;
    top: 4px;
    right: 16px;
    width: 16px;
    height: 16px;
    background: var(--bg-card);
    border-left: 1px solid var(--border-color);
    border-top: 1px solid var(--border-color);
    transform: rotate(45deg);
    z-index: 1;
    
    @media (max-width: 768px) {
      display: none;
    }
  }
  
  /* VIP卡片边框 */
  &.vip-card-1 {
    border-color: rgba(205, 127, 50, 0.5);
    &::after { border-color: rgba(205, 127, 50, 0.5); }
  }
  
  &.vip-card-2 {
    border-color: rgba(192, 192, 192, 0.5);
    &::after { border-color: rgba(192, 192, 192, 0.5); }
  }
  
  &.vip-card-3 {
    border-color: rgba(255, 215, 0, 0.5);
    box-shadow: 0 8px 32px rgba(255, 215, 0, 0.15);
    &::after { border-color: rgba(255, 215, 0, 0.5); }
  }
}

/* VIP卡片背景装饰 */
.vip-card-bg {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 80px;
  pointer-events: none;
  z-index: 0;
  
  &.level-1 {
    background: linear-gradient(135deg, rgba(205, 127, 50, 0.15) 0%, transparent 60%);
  }
  
  &.level-2 {
    background: linear-gradient(135deg, rgba(192, 192, 192, 0.2) 0%, transparent 60%);
  }
  
  &.level-3 {
    background: linear-gradient(135deg, rgba(255, 215, 0, 0.2) 0%, rgba(255, 183, 0, 0.1) 30%, transparent 60%);
    animation: vipBgShimmer 3s ease-in-out infinite;
  }
}

@keyframes vipBgShimmer {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.user-card-header {
  display: flex;
  gap: 10px;
  padding: 10px;
  background: linear-gradient(135deg, rgba($primary-color, 0.1), rgba($primary-color, 0.05));
  position: relative;
  z-index: 1;
  
  /* VIP专属头部背景 */
  &.vip-header-1 {
    background: linear-gradient(135deg, rgba(205, 127, 50, 0.15), rgba(205, 127, 50, 0.05));
  }
  
  &.vip-header-2 {
    background: linear-gradient(135deg, rgba(192, 192, 192, 0.15), rgba(192, 192, 192, 0.05));
  }
  
  &.vip-header-3 {
    background: linear-gradient(135deg, rgba(255, 215, 0, 0.2), rgba(255, 183, 0, 0.08));
  }
}

.user-card-avatar {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  overflow: visible;
  flex-shrink: 0;
  cursor: pointer;
  transition: transform 0.3s;
  position: relative;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 50%;
  }
  
  span {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    background: $primary-color;
    border-radius: 50%;
    font-size: 20px;
    font-weight: 600;
    color: #fff;
  }
  
  &:hover {
    transform: scale(1.08);
  }
  
  /* 卡片内VIP头像框 */
  .card-avatar-frame {
    position: absolute;
    inset: -3px;
    border-radius: 50%;
    pointer-events: none;
    
    &.level-1 {
      border: 3px solid #cd7f32;
      box-shadow: 0 0 12px rgba(205, 127, 50, 0.5);
    }
    
    &.level-2 {
      border: 3px solid #c0c0c0;
      box-shadow: 0 0 12px rgba(192, 192, 192, 0.6);
    }
    
    &.level-3 {
      border: 3px solid #ffd700;
      box-shadow: 0 0 16px rgba(255, 215, 0, 0.7);
      animation: vipFrameGlow 2s ease-in-out infinite;
    }
  }
  
  /* VIP等级边框 */
  &.vip-level-1 {
    box-shadow: 0 0 0 2px rgba(205, 127, 50, 0.3);
  }
  
  &.vip-level-2 {
    box-shadow: 0 0 0 2px rgba(192, 192, 192, 0.3);
  }
  
  &.vip-level-3 {
    box-shadow: 0 0 0 2px rgba(255, 215, 0, 0.3);
  }
}

.user-card-info {
  flex: 1;
  min-width: 0;
  max-width: calc(100% - 68px);
  display: flex;
  flex-direction: column;
  justify-content: center;
  overflow: hidden;
}

.user-card-name {
  font-size: 14px;
  font-weight: 600;
}

.user-card-bio {
  font-size: 11px;
  color: var(--text-muted);
  margin-top: 4px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.3;
  max-height: 28.6px;
  word-break: break-all;
  max-width: 100%;
}

.vip-expire-info {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 4px;
  font-size: 10px;
  
  .vip-expire-label {
    color: var(--text-muted);
  }
  
  .vip-expire-date {
    color: #ffd700;
    font-weight: 500;
  }
}

.user-card-stats {
  display: flex;
  justify-content: space-around;
  padding: 8px;
  border-bottom: 1px solid var(--border-color);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 8px;
  transition: background 0.2s;
  
  &:hover {
    background: var(--bg-card-hover);
  }
  
  .stat-value {
    font-size: 15px;
    font-weight: 600;
    color: var(--text-primary);
  }
  
  .stat-label {
    font-size: 11px;
    color: var(--text-muted);
    margin-top: 2px;
  }
}

.user-card-menu {
  padding: 6px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 12px;
  border-radius: 8px;
  cursor: pointer;
  color: var(--text-secondary);
  transition: all 0.2s;
  
  .el-icon {
    font-size: 15px;
  }
  
  span {
    font-size: 13px;
  }
  
  &:hover {
    background: var(--bg-card-hover);
    color: $primary-color;
  }
}

.user-card-footer {
  padding: 6px 12px 12px;
}

.logout-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 100%;
  padding: 8px;
  background: transparent;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  color: var(--text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
  
  &:hover {
    border-color: #ef4444;
    color: #ef4444;
    background: rgba(239, 68, 68, 0.05);
  }
}

/* 用户卡片动画 */
@media (max-width: 768px) {
  .user-card {
    position: fixed;
    top: calc(env(safe-area-inset-top, 0px) + 70px);
    right: 16px;
    width: 240px; /* 移动端下拉菜单宽度更窄一些 */
    max-width: calc(100vw - 32px); /* 保证不超出屏幕 */
    transform-origin: top right;
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.2);
    border-radius: 12px; /* 移动端圆角稍微小一点 */
  }
  
  .user-card-header {
    padding: 10px;
  }
  
  .user-card-avatar {
    width: 40px;
    height: 40px;
  }
  
  .user-card-name {
    font-size: 13px;
  }
  
  .user-card-stats {
    padding: 6px;
  }
  
  .stat-item {
    padding: 2px 6px;
    
    .stat-value {
      font-size: 14px;
    }
  }
  
  .user-card-menu {
    padding: 4px;
  }
  
  .menu-item {
    padding: 8px 10px;
    
    .el-icon {
      font-size: 14px;
    }
    
    span {
      font-size: 13px;
    }
  }
  
  .user-card-footer {
    padding: 4px 10px 10px;
  }
  
  .logout-btn {
    padding: 8px;
    font-size: 13px;
  }
}

.user-card-enter-active,
.user-card-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.user-card-enter-from,
.user-card-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.95);
  
  @media (max-width: 768px) {
    transform: scale(0.95);
  }
}

.user-info:hover .user-avatar {
  transform: scale(1.05);
}

/* 未读消息徽章 */
.unread-badge {
  display: inline-block;
  margin-left: 8px;
  padding: 2px 6px;
  background: #ef4444;
  color: #fff;
  font-size: 11px;
  border-radius: 10px;
  min-width: 18px;
  text-align: center;
}

/* 搜索弹窗 */
.search-overlay {
  position: fixed;
  inset: 0;
  background: var(--search-overlay-bg);
  backdrop-filter: blur(8px);
  z-index: 1000;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  overflow-y: auto;
  padding: 78px 16px 24px;
}

.search-modal {
  width: 100%;
  max-width: 620px;
  max-height: calc(100vh - 100px);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.search-input-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: rgba(var(--bg-card-rgb), 0.94);
  border: 1px solid var(--border-light);
  border-radius: 16px;
  box-shadow: 0 18px 40px rgba(0, 0, 0, 0.12);
  
  .search-icon {
    font-size: 18px;
    color: var(--text-muted);
  }
  
  .search-input {
    flex: 1;
    background: none;
    border: none;
    outline: none;
    font-size: 16px;
    font-weight: 500;
    color: var(--text-primary);
    
    &::placeholder {
      color: var(--text-disabled);
    }
  }
  
  .esc-hint {
    font-size: 11px;
    color: var(--text-disabled);
    background: var(--bg-card-hover);
    padding: 3px 8px;
    border-radius: 4px;
  }
}

.search-results-shell {
  display: flex;
  flex-direction: column;
  min-height: 0;
  background: rgba(var(--bg-card-rgb), 0.96);
  border: 1px solid var(--border-light);
  border-radius: 16px;
  box-shadow: 0 18px 48px rgba(0, 0, 0, 0.14);
  overflow: hidden;
}

.search-meta-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 10px 14px;
  border-bottom: 1px solid var(--border-color);
  background: linear-gradient(180deg, rgba(var(--bg-card-rgb), 0.98) 0%, rgba(var(--bg-card-rgb), 0.9) 100%);

  span {
    font-size: 12px;
    color: var(--text-muted);
  }
}

.search-page-link {
  border: none;
  background: rgba($primary-color, 0.12);
  color: $primary-color;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: rgba($primary-color, 0.18);
    transform: translateY(-1px);
  }
}

.search-scroll {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 8px;
  max-height: min(58vh, 560px);
}

.search-result-item {
  display: block;
  padding: 12px 14px;
  text-decoration: none;
  transition: background 0.15s, transform 0.15s, border-color 0.15s;
  border: 1px solid transparent;
  border-radius: 12px;
  
  &:hover {
    background: var(--bg-card-hover);
    transform: translateY(-1px);
    border-color: rgba($primary-color, 0.18);
  }
  
  h4 {
    font-size: 16px;
    line-height: 1.4;
    font-weight: 600;
    color: var(--text-primary);
    margin-bottom: 4px;
    
    :deep(.search-highlight) {
      background: rgba(59, 130, 246, 0.14);
      color: $primary-color;
      padding: 0 4px;
      border-radius: 6px;
    }
  }
  
  p {
    font-size: 13px;
    line-height: 1.6;
    color: var(--text-muted);
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    
    :deep(.search-highlight) {
      background: rgba(59, 130, 246, 0.14);
      color: $primary-color;
      padding: 0 4px;
      border-radius: 6px;
    }
  }
}

.search-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 6px;
}

.search-tag {
  display: inline-block;
  padding: 3px 8px;
  font-size: 11px;
  color: var(--text-muted);
  background: var(--bg-card-hover);
  border-radius: 999px;
  transition: all 0.2s;
  
  &.tag-matched {
    background: rgba(168, 85, 247, 0.2);
    color: var(--primary-color);
    border: 1px solid rgba($primary-color, 0.4);
  }
}

.search-empty {
  padding: 36px 24px;
  text-align: center;
  color: var(--text-disabled);
  font-size: 13px;
}

/* 搜索分区 */
.search-section {
  background: transparent;
  border: 1px solid var(--border-light);
  border-radius: 14px;
  overflow: hidden;
  margin-bottom: 10px;

  &:last-child {
    margin-bottom: 0;
  }
}

.search-section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 14px;
  font-size: 12px;
  font-weight: 700;
  color: var(--text-muted);
  background: rgba(var(--bg-card-rgb), 0.82);
  border-bottom: 1px solid var(--border-color);
  backdrop-filter: blur(10px);
  
  .el-icon {
    font-size: 14px;
  }
}

/* 用户搜索结果 */
.search-users {
  display: flex;
  flex-direction: column;
}

.search-user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  text-decoration: none;
  border-bottom: 1px solid var(--border-color);
  transition: background 0.15s;
  
  &:last-child {
    border-bottom: none;
  }
  
  &:hover {
    background: var(--bg-card-hover);
  }
}

.user-avatar-small {
  width: 34px;
  height: 34px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  font-size: 16px;
  font-weight: 600;
  color: #fff;
  
  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
  
  /* 普通VIP - 铜色边框 */
  &.vip-level-1 {
    box-shadow: 0 0 0 2px #cd7f32;
  }
  
  /* 高级VIP - 银色边框 */
  &.vip-level-2 {
    box-shadow: 0 0 0 2px #c0c0c0;
  }
  
  /* 超级VIP - 金色边框 */
  &.vip-level-3 {
    box-shadow: 0 0 0 2px #ffd700;
  }
}

.user-info-small {
  flex: 1;
  min-width: 0;
}

.user-name-row {
  display: flex;
  align-items: center;
  gap: 4px;

  :deep(.vip-username) {
    font-size: 14px;
    font-weight: 600;
  }
}

.user-intro {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 文章搜索结果 */
.search-articles {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px;
}

.search-study-item {
  h4 {
    font-size: 15px;
  }
}

.search-scroll::-webkit-scrollbar {
  width: 8px;
}

.search-scroll::-webkit-scrollbar-thumb {
  background: rgba(148, 163, 184, 0.45);
  border-radius: 999px;
}

.search-scroll::-webkit-scrollbar-track {
  background: transparent;
}

.search-fade-enter-active,
.search-fade-leave-active {
  transition: opacity 0.15s ease;
}

.search-fade-enter-from,
.search-fade-leave-to {
  opacity: 0;
}

@media (max-width: 768px) {
  .search-overlay {
    padding: 70px 10px 16px;
  }

  .search-modal {
    max-width: none;
    max-height: calc(100vh - 86px);
  }

  .search-input-wrapper {
    padding: 10px 12px;

    .search-input {
      font-size: 15px;
    }
  }

  .search-result-item h4 {
    font-size: 15px;
  }
}

@media (max-width: 900px) {
  .header-container {
    border-radius: 18px;
    padding: 0 14px;
  }

  .header-left {
    gap: 10px;
    flex: 1;
  }

  .header-right {
    gap: 10px;
  }

  .ai-btn {
    width: 58px;
    height: 34px;

    strong {
      font-size: 13px;
      letter-spacing: 2px;
    }
  }

  .mobile-menu-btn {
    display: inline-flex;
  }

  .external-link {
    display: none;
  }
}

@media (max-width: 640px) {
  .header-container {
    border-radius: 16px;
    height: 60px;
    padding: 0 12px;
  }

  .logo {
    gap: 6px;

    .logo-img {
      width: 24px;
      height: 24px;
    }

    .logo-text {
      font-size: 16px;
    }
  }

  .search-box {
    padding: 8px 10px;
  }

  .header-right {
    gap: 8px;
  }

  .theme-switch {
    --toggle-size: 9px;
  }

  .notification-bell {
    width: 36px;
    height: 36px;
  }
}

@media (max-width: 420px) {
  .logo .logo-text {
    display: none;
  }

  .ai-btn {
    width: 48px;

    strong {
      letter-spacing: 1px;
      font-size: 12px;
    }
  }
}
</style>
