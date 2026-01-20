// pages/article/detail/index.js
const articleApi = require('../../../api/article')
const commentApi = require('../../../api/comment')
const auth = require('../../../utils/auth')
const imageUtil = require('../../../utils/image')
const formatUtil = require('../../../utils/format')

Page({
  data: {
    id: null,
    article: null,
    isLiked: false,
    isFavorited: false,
    isFollowing: false,
    comments: [],
    commentPage: 1,
    commentPageSize: 10,
    hasMoreComments: true,
    loading: true,
    commentContent: '',
    showCommentInput: false,
    replyTo: null, // 回复的评论对象
    replyContent: '', // 回复内容
    showReplyInput: false, // 显示回复输入框
    editingComment: null, // 正在编辑的评论对象 {type: 'comment'|'reply', id, content, commentIndex, replyIndex}
    showEditInput: false, // 显示编辑输入框
    editContent: '' // 编辑内容
  },

  onLoad(options) {
    this.setData({ id: options.id })
    this.loadArticle()
    this.loadComments()
  },

  onShareAppMessage() {
    return {
      title: this.data.article?.title || '文章详情',
      path: `/pages/article/detail/index?id=${this.data.id}`,
      imageUrl: this.data.article?.cover
    }
  },

  // 返回
  goBack() {
    wx.navigateBack()
  },

  // 显示菜单
  showMenu() {
    wx.showActionSheet({
      itemList: ['举报', '复制链接'],
      success: (res) => {
        if (res.tapIndex === 1) {
          // 复制链接
          wx.setClipboardData({
            data: `/pages/article/detail/index?id=${this.data.id}`,
            success: () => {
              wx.showToast({
                title: '链接已复制',
                icon: 'success'
              })
            }
          })
        }
      }
    })
  },

  // 加载文章详情
  async loadArticle() {
    try {
      let article = await articleApi.getDetail(this.data.id)
      
      console.log('文章详情原始数据:', article)
      console.log('文章内容HTML:', article.contentHtml)
      console.log('文章内容:', article.content)
      
      // 处理图片路径
      article = imageUtil.processArticleImages(article)
      
      // 格式化时间
      article.createTime = formatUtil.formatRelativeTime(article.createTime)
      
      // 处理Markdown内容，添加样式
      if (article.contentHtml) {
        article.contentHtml = this.processMarkdownStyles(article.contentHtml)
        console.log('处理后的HTML:', article.contentHtml)
      } else if (article.content) {
        // 如果没有contentHtml，使用content字段
        article.contentHtml = this.processMarkdownStyles(article.content)
        console.log('使用content字段，处理后:', article.contentHtml)
      }
      
      this.setData({
        article,
        isLiked: article.isLiked,
        isFavorited: article.isFavorited,
        isFollowing: article.author?.isFollowing || false,
        loading: false
      })
      
      console.log('设置后的article数据:', this.data.article)
    } catch (err) {
      console.error('加载文章失败:', err)
      this.setData({ loading: false })
      
      wx.showToast({
        title: '加载失败',
        icon: 'none'
      })
    }
  },

  // 处理Markdown样式
  processMarkdownStyles(content) {
    if (!content) return ''
    
    let html = content
    
    // 如果内容包含HTML标签，说明已经是HTML格式
    if (html.includes('<h1>') || html.includes('<p>')) {
      // 直接添加样式
      html = html.replace(/<h1>/g, '<h1 style="font-size:48rpx;font-weight:700;margin:32rpx 0 24rpx;color:#e2e8f0;">')
      html = html.replace(/<h2>/g, '<h2 style="font-size:40rpx;font-weight:700;margin:28rpx 0 20rpx;color:#e2e8f0;">')
      html = html.replace(/<h3>/g, '<h3 style="font-size:36rpx;font-weight:600;margin:24rpx 0 16rpx;color:#e2e8f0;">')
      html = html.replace(/<p>/g, '<p style="margin:16rpx 0;line-height:1.8;color:#cbd5e1;">')
      html = html.replace(/<ul>/g, '<ul style="padding-left:40rpx;margin:16rpx 0;">')
      html = html.replace(/<ol>/g, '<ol style="padding-left:40rpx;margin:16rpx 0;">')
      html = html.replace(/<li>/g, '<li style="margin:8rpx 0;line-height:1.6;color:#cbd5e1;">')
      html = html.replace(/<code>/g, '<code style="padding:4rpx 12rpx;background:#1e293b;color:#3b82f6;font-size:28rpx;border-radius:8rpx;">')
      html = html.replace(/<pre>/g, '<pre style="padding:24rpx;background:#1e293b;border-radius:16rpx;overflow-x:auto;margin:24rpx 0;">')
      html = html.replace(/<blockquote>/g, '<blockquote style="padding:16rpx 24rpx;margin:24rpx 0;border-left:6rpx solid #3b82f6;background:rgba(59,130,246,0.05);color:#94a3b8;">')
      html = html.replace(/<img /g, '<img style="max-width:100%;border-radius:16rpx;margin:24rpx 0;" ')
      html = html.replace(/<a /g, '<a style="color:#3b82f6;text-decoration:underline;" ')
      return html
    }
    
    // Markdown转HTML
    // 转义HTML特殊字符
    html = html.replace(/&/g, '&amp;')
             .replace(/</g, '&lt;')
             .replace(/>/g, '&gt;')
    
    // 代码块（```）
    html = html.replace(/```([^`]+)```/g, '<pre style="padding:24rpx;background:#1e293b;border-radius:16rpx;overflow-x:auto;margin:24rpx 0;"><code style="color:#cbd5e1;">$1</code></pre>')
    
    // 行内代码（`）
    html = html.replace(/`([^`]+)`/g, '<code style="padding:4rpx 12rpx;background:#1e293b;color:#3b82f6;font-size:28rpx;border-radius:8rpx;">$1</code>')
    
    // 标题
    html = html.replace(/^### (.+)$/gm, '<h3 style="font-size:36rpx;font-weight:600;margin:24rpx 0 16rpx;color:#e2e8f0;">$1</h3>')
    html = html.replace(/^## (.+)$/gm, '<h2 style="font-size:40rpx;font-weight:700;margin:28rpx 0 20rpx;color:#e2e8f0;">$1</h2>')
    html = html.replace(/^# (.+)$/gm, '<h1 style="font-size:48rpx;font-weight:700;margin:32rpx 0 24rpx;color:#e2e8f0;">$1</h1>')
    
    // 粗体
    html = html.replace(/\*\*(.+?)\*\*/g, '<strong style="font-weight:700;color:#e2e8f0;">$1</strong>')
    
    // 斜体
    html = html.replace(/\*(.+?)\*/g, '<em style="font-style:italic;color:#94a3b8;">$1</em>')
    
    // 链接
    html = html.replace(/\[([^\]]+)\]\(([^)]+)\)/g, '<a href="$2" style="color:#3b82f6;text-decoration:underline;">$1</a>')
    
    // 图片
    html = html.replace(/!\[([^\]]*)\]\(([^)]+)\)/g, '<img src="$2" alt="$1" style="max-width:100%;border-radius:16rpx;margin:24rpx 0;" />')
    
    // 无序列表
    html = html.replace(/^\- (.+)$/gm, '<li style="margin:8rpx 0;line-height:1.6;color:#cbd5e1;">$1</li>')
    html = html.replace(/(<li[^>]*>.*<\/li>)/s, '<ul style="padding-left:40rpx;margin:16rpx 0;">$1</ul>')
    
    // 有序列表
    html = html.replace(/^\d+\. (.+)$/gm, '<li style="margin:8rpx 0;line-height:1.6;color:#cbd5e1;">$1</li>')
    
    // 引用
    html = html.replace(/^&gt; (.+)$/gm, '<blockquote style="padding:16rpx 24rpx;margin:24rpx 0;border-left:6rpx solid #3b82f6;background:rgba(59,130,246,0.05);color:#94a3b8;">$1</blockquote>')
    
    // 分隔线
    html = html.replace(/^---$/gm, '<hr style="border:none;border-top:2rpx solid #334155;margin:32rpx 0;" />')
    
    // 换行转段落
    html = html.split('\n\n').map(para => {
      para = para.trim()
      if (!para) return ''
      // 如果已经是HTML标签，不再包裹
      if (para.startsWith('<')) return para
      return `<p style="margin:16rpx 0;line-height:1.8;color:#cbd5e1;">${para.replace(/\n/g, '<br/>')}</p>`
    }).join('')
    
    return html
  },

  // 加载评论列表
  async loadComments() {
    try {
      const res = await commentApi.getList(this.data.id, {
        page: this.data.commentPage,
        pageSize: this.data.commentPageSize
      })

      console.log('=== 评论API返回数据 ===')
      console.log('原始数据:', res)

      let records = []
      if (Array.isArray(res)) {
        records = res
      } else if (res.records) {
        records = res.records
      } else if (res.list) {
        records = res.list
      }

      console.log('提取的评论列表:', records)
      if (records.length > 0) {
        console.log('第一条评论:', records[0])
        console.log('第一条评论的回复:', records[0].replies)
      }

      // 处理图片和时间，包括回复列表
      records = records.map((comment, index) => {
        console.log(`处理评论 ${index}:`, comment.id, '回复数:', comment.replies?.length || 0)
        
        const processedComment = {
          ...comment,
          user: {
            ...comment.user,
            avatar: imageUtil.getImageUrl(comment.user?.avatar)
          },
          createTime: formatUtil.formatRelativeTime(comment.createTime),
          isLiked: comment.isLiked || false,
          showAllReplies: false // 默认不展开全部回复，只显示前3条
        }

        // 处理回复列表 - 只有当replies存在且不为空时才处理
        if (comment.replies && Array.isArray(comment.replies) && comment.replies.length > 0) {
          console.log(`评论 ${comment.id} 有 ${comment.replies.length} 条回复`)
          processedComment.replies = comment.replies.map(reply => {
            console.log('原始回复数据:', reply)
            
            // 处理被回复用户信息
            let replyToUser = null
            if (reply.replyToUser) {
              replyToUser = {
                ...reply.replyToUser,
                avatar: imageUtil.getImageUrl(reply.replyToUser.avatar)
              }
            } else if (reply.replyUser) {
              // 兼容 replyUser 字段
              replyToUser = {
                ...reply.replyUser,
                avatar: imageUtil.getImageUrl(reply.replyUser.avatar)
              }
            }
            
            const processedReply = {
              ...reply,
              user: {
                ...reply.user,
                avatar: imageUtil.getImageUrl(reply.user?.avatar)
              },
              replyToUser: replyToUser,
              createTime: formatUtil.formatRelativeTime(reply.createTime),
              isLiked: reply.isLiked || false
            }
            
            console.log('处理后的回复:', processedReply)
            return processedReply
          })
          console.log(`处理后的回复列表:`, processedComment.replies)
        } else {
          // 确保replies是空数组而不是null或undefined
          processedComment.replies = []
          console.log(`评论 ${comment.id} 没有回复`)
        }

        return processedComment
      })

      console.log('最终处理后的评论列表:', records)

      this.setData({
        comments: [...this.data.comments, ...records],
        hasMoreComments: records.length === this.data.commentPageSize
      })

      console.log('setData后的评论列表:', this.data.comments)
    } catch (err) {
      console.error('加载评论失败:', err)
    }
  },

  // 点赞文章
  async handleLike() {
    if (!auth.checkLogin()) return

    try {
      if (this.data.isLiked) {
        await articleApi.unlike(this.data.id)
        this.setData({
          isLiked: false,
          'article.likeCount': this.data.article.likeCount - 1
        })
      } else {
        await articleApi.like(this.data.id)
        this.setData({
          isLiked: true,
          'article.likeCount': this.data.article.likeCount + 1
        })
        
        wx.vibrateShort()
      }
    } catch (err) {
      console.error('点赞失败:', err)
    }
  },

  // 收藏文章
  async handleFavorite() {
    if (!auth.checkLogin()) return

    try {
      if (this.data.isFavorited) {
        await articleApi.unfavorite(this.data.id)
        this.setData({ isFavorited: false })
        wx.showToast({
          title: '已取消收藏',
          icon: 'success'
        })
      } else {
        await articleApi.favorite(this.data.id)
        this.setData({ isFavorited: true })
        wx.showToast({
          title: '收藏成功',
          icon: 'success'
        })
      }
    } catch (err) {
      console.error('收藏失败:', err)
    }
  },

  // 关注作者
  async handleFollow() {
    if (!auth.checkLogin()) return

    try {
      const userApi = require('../../../api/user')
      if (this.data.isFollowing) {
        await userApi.unfollow(this.data.article.author.id)
        this.setData({ isFollowing: false })
        wx.showToast({
          title: '已取消关注',
          icon: 'success'
        })
      } else {
        await userApi.follow(this.data.article.author.id)
        this.setData({ isFollowing: true })
        wx.showToast({
          title: '关注成功',
          icon: 'success'
        })
      }
    } catch (err) {
      console.error('关注失败:', err)
    }
  },

  // 分享
  handleShare() {
    wx.showShareMenu({
      withShareTicket: true,
      menus: ['shareAppMessage', 'shareTimeline']
    })
  },

  // 聚焦输入框
  focusInput() {
    if (!auth.checkLogin()) return
    this.setData({ showCommentInput: true })
  },

  // 关闭评论输入
  closeCommentInput() {
    this.setData({ 
      showCommentInput: false,
      commentContent: ''
    })
  },

  // 输入评论
  onCommentInput(e) {
    this.setData({
      commentContent: e.detail.value
    })
  },

  // 发表评论
  async handleComment() {
    if (!auth.checkLogin()) return

    const content = this.data.commentContent.trim()
    if (!content) {
      wx.showToast({
        title: '请输入评论内容',
        icon: 'none'
      })
      return
    }

    try {
      const newComment = await commentApi.create({
        articleId: this.data.id,
        content
      })

      wx.showToast({
        title: '评论成功',
        icon: 'success'
      })

      // 获取当前用户信息
      const currentUser = auth.getUserInfo()
      
      // 构造新评论对象
      const comment = {
        id: newComment.id || Date.now(),
        content: content,
        user: {
          id: currentUser.id,
          nickname: currentUser.nickname || currentUser.username,
          avatar: imageUtil.getImageUrl(currentUser.avatar),
          vipLevel: currentUser.vipLevel || 0
        },
        createTime: '刚刚',
        likeCount: 0,
        isLiked: false,
        showAllReplies: false,
        replies: []
      }

      // 将新评论添加到列表开头，并更新评论数
      this.setData({
        comments: [comment, ...this.data.comments],
        showCommentInput: false,
        commentContent: '',
        'article.commentCount': (this.data.article.commentCount || 0) + 1
      })
    } catch (err) {
      console.error('评论失败:', err)
      wx.showToast({
        title: err.message || '评论失败',
        icon: 'none'
      })
    }
  },

  // 跳转到用户详情
  goToUserDetail() {
    if (!this.data.article) return
    
    wx.navigateTo({
      url: `/pages/user/detail/index?id=${this.data.article.author.id}`,
      fail: () => {
        wx.showToast({
          title: '用户主页开发中',
          icon: 'none'
        })
      }
    })
  },

  // 跳转到标签
  goToTag(e) {
    const id = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/tag/detail/index?id=${id}`
    })
  },

  // 点赞评论
  async likeComment(e) {
    if (!auth.checkLogin()) return

    const { id, index } = e.currentTarget.dataset
    const comment = this.data.comments[index]

    try {
      if (comment.isLiked) {
        await commentApi.unlike(id)
        this.setData({
          [`comments[${index}].isLiked`]: false,
          [`comments[${index}].likeCount`]: comment.likeCount - 1
        })
      } else {
        await commentApi.like(id)
        this.setData({
          [`comments[${index}].isLiked`]: true,
          [`comments[${index}].likeCount`]: comment.likeCount + 1
        })
        wx.vibrateShort()
      }
    } catch (err) {
      console.error('点赞评论失败:', err)
    }
  },

  // 点赞回复
  async likeReply(e) {
    if (!auth.checkLogin()) return

    const { id, commentIndex, replyIndex } = e.currentTarget.dataset
    const reply = this.data.comments[commentIndex].replies[replyIndex]

    try {
      if (reply.isLiked) {
        await commentApi.unlike(id)
        this.setData({
          [`comments[${commentIndex}].replies[${replyIndex}].isLiked`]: false,
          [`comments[${commentIndex}].replies[${replyIndex}].likeCount`]: reply.likeCount - 1
        })
      } else {
        await commentApi.like(id)
        this.setData({
          [`comments[${commentIndex}].replies[${replyIndex}].isLiked`]: true,
          [`comments[${commentIndex}].replies[${replyIndex}].likeCount`]: reply.likeCount + 1
        })
        wx.vibrateShort()
      }
    } catch (err) {
      console.error('点赞回复失败:', err)
    }
  },

  // 展开/收起回复
  toggleReplies(e) {
    const index = e.currentTarget.dataset.index
    const showAllReplies = this.data.comments[index].showAllReplies
    this.setData({
      [`comments[${index}].showAllReplies`]: !showAllReplies
    })
  },

  // 回复评论
  replyComment(e) {
    if (!auth.checkLogin()) return

    const { id, nickname, index } = e.currentTarget.dataset
    this.setData({
      replyTo: {
        id,
        nickname,
        commentIndex: index
      },
      showReplyInput: true,
      replyContent: ''
    })
  },

  // 回复子评论
  replyToReply(e) {
    if (!auth.checkLogin()) return

    const { id, nickname, commentIndex, parentId } = e.currentTarget.dataset
    this.setData({
      replyTo: {
        id,
        nickname,
        commentIndex,
        parentId
      },
      showReplyInput: true,
      replyContent: ''
    })
  },

  // 关闭回复输入
  closeReplyInput() {
    this.setData({
      showReplyInput: false,
      replyTo: null,
      replyContent: ''
    })
  },

  // 输入回复内容
  onReplyInput(e) {
    this.setData({
      replyContent: e.detail.value
    })
  },

  // 发送回复
  async handleReply() {
    if (!auth.checkLogin()) return

    const content = this.data.replyContent.trim()
    if (!content) {
      wx.showToast({
        title: '请输入回复内容',
        icon: 'none'
      })
      return
    }

    try {
      const data = {
        articleId: this.data.id,
        content,
        parentId: this.data.replyTo.parentId || this.data.replyTo.id,
        replyToId: this.data.replyTo.id
      }

      console.log('发送回复数据:', data)
      const newReply = await commentApi.create(data)
      console.log('后端返回的回复:', newReply)

      wx.showToast({
        title: '回复成功',
        icon: 'success'
      })

      // 获取当前用户信息
      const currentUser = auth.getUserInfo()
      console.log('当前用户信息:', currentUser)
      
      if (!currentUser || !currentUser.id) {
        console.error('无法获取用户信息，重新加载评论列表')
        // 如果获取不到用户信息，重新加载评论列表
        this.setData({
          showReplyInput: false,
          replyTo: null,
          replyContent: '',
          commentPage: 1,
          comments: []
        })
        this.loadComments()
        return
      }
      
      // 构造新回复对象
      const reply = {
        id: newReply.id || newReply.data?.id || Date.now(), // 兼容不同的返回格式
        content: content,
        user: {
          id: currentUser.id,
          nickname: currentUser.nickname || currentUser.username || '我',
          avatar: imageUtil.getImageUrl(currentUser.avatar),
          vipLevel: currentUser.vipLevel || 0
        },
        replyToUser: this.data.replyTo.parentId ? {
          id: this.data.replyTo.id,
          nickname: this.data.replyTo.nickname
        } : null,
        createTime: '刚刚',
        likeCount: 0,
        isLiked: false
      }

      console.log('构造的回复对象:', reply)

      // 找到父评论的ID
      const parentCommentId = this.data.replyTo.parentId || this.data.replyTo.id
      console.log('父评论ID:', parentCommentId)
      
      // 在评论列表中找到对应的父评论
      const commentIndex = this.data.comments.findIndex(c => c.id == parentCommentId)
      console.log('找到的评论索引:', commentIndex)
      
      if (commentIndex !== -1) {
        // 获取当前评论的回复列表（创建新数组以触发视图更新）
        const replies = [...(this.data.comments[commentIndex].replies || [])]
        console.log('原回复列表长度:', replies.length)
        
        // 将新回复添加到回复列表末尾
        replies.push(reply)
        console.log('新回复列表长度:', replies.length)
        
        // 更新评论的回复列表和展开状态，同时更新文章评论数
        this.setData({
          [`comments[${commentIndex}].replies`]: replies,
          [`comments[${commentIndex}].showAllReplies`]: true, // 自动展开以显示新回复
          showReplyInput: false,
          replyTo: null,
          replyContent: '',
          'article.commentCount': (this.data.article.commentCount || 0) + 1
        })
        
        console.log('更新后的评论:', this.data.comments[commentIndex])
        console.log('回复列表是否展开:', this.data.comments[commentIndex].showAllReplies)
      } else {
        // 如果找不到父评论，重新加载评论列表
        console.warn('找不到父评论，重新加载评论列表')
        this.setData({
          showReplyInput: false,
          replyTo: null,
          replyContent: '',
          commentPage: 1,
          comments: []
        })
        this.loadComments()
      }
    } catch (err) {
      console.error('回复失败:', err)
      wx.showToast({
        title: err.message || '回复失败',
        icon: 'none'
      })
    }
  },

  // 加载更多评论
  loadMoreComments() {
    if (!this.data.hasMoreComments) return

    this.setData({
      commentPage: this.data.commentPage + 1
    })
    this.loadComments()
  },

  // 长按评论显示操作菜单
  onCommentLongPress(e) {
    const { id, index, userId, content } = e.currentTarget.dataset
    const currentUser = auth.getUserInfo()
    
    if (!currentUser) return

    // 只有评论作者才能操作
    if (currentUser.id != userId) {
      return
    }

    const itemList = ['修改评论', '删除评论']
    
    wx.showActionSheet({
      itemList,
      success: (res) => {
        if (res.tapIndex === 0) {
          this.editComment(id, content, index)
        } else if (res.tapIndex === 1) {
          this.deleteComment(id, index)
        }
      }
    })
  },

  // 修改评论
  editComment(commentId, content, commentIndex) {
    this.setData({
      editingComment: {
        type: 'comment',
        id: commentId,
        commentIndex: commentIndex
      },
      editContent: content,
      showEditInput: true
    })
  },

  // 删除评论
  deleteComment(commentId, commentIndex) {
    wx.showModal({
      title: '确认删除',
      content: '删除评论后，该评论下的所有回复也会被删除，确定要删除吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await commentApi.remove(commentId)
            
            // 计算要减少的评论数（1条评论 + 所有回复）
            const comment = this.data.comments[commentIndex]
            const replyCount = comment.replies ? comment.replies.length : 0
            const totalCount = 1 + replyCount
            
            // 从列表中移除评论
            const comments = [...this.data.comments]
            comments.splice(commentIndex, 1)
            
            this.setData({
              comments,
              'article.commentCount': Math.max(0, (this.data.article.commentCount || 0) - totalCount)
            })
            
            wx.showToast({
              title: '删除成功',
              icon: 'success'
            })
          } catch (err) {
            console.error('删除评论失败:', err)
            wx.showToast({
              title: err.message || '删除失败',
              icon: 'none'
            })
          }
        }
      }
    })
  },

  // 长按回复显示操作菜单
  onReplyLongPress(e) {
    const { id, commentIndex, replyIndex, userId, content } = e.currentTarget.dataset
    const currentUser = auth.getUserInfo()
    
    if (!currentUser) return

    // 只有回复作者才能操作
    if (currentUser.id != userId) {
      return
    }

    const itemList = ['修改回复', '删除回复']
    
    wx.showActionSheet({
      itemList,
      success: (res) => {
        if (res.tapIndex === 0) {
          this.editReply(id, content, commentIndex, replyIndex)
        } else if (res.tapIndex === 1) {
          this.deleteReply(id, commentIndex, replyIndex)
        }
      }
    })
  },

  // 修改回复
  editReply(replyId, content, commentIndex, replyIndex) {
    this.setData({
      editingComment: {
        type: 'reply',
        id: replyId,
        commentIndex: commentIndex,
        replyIndex: replyIndex
      },
      editContent: content,
      showEditInput: true
    })
  },

  // 删除回复
  deleteReply(replyId, commentIndex, replyIndex) {
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这条回复吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await commentApi.remove(replyId)
            
            // 从回复列表中移除
            const replies = [...this.data.comments[commentIndex].replies]
            replies.splice(replyIndex, 1)
            
            this.setData({
              [`comments[${commentIndex}].replies`]: replies,
              'article.commentCount': Math.max(0, (this.data.article.commentCount || 0) - 1)
            })
            
            wx.showToast({
              title: '删除成功',
              icon: 'success'
            })
          } catch (err) {
            console.error('删除回复失败:', err)
            wx.showToast({
              title: err.message || '删除失败',
              icon: 'none'
            })
          }
        }
      }
    })
  },

  // 关闭编辑输入框
  closeEditInput() {
    this.setData({
      showEditInput: false,
      editingComment: null,
      editContent: ''
    })
  },

  // 输入编辑内容
  onEditInput(e) {
    this.setData({
      editContent: e.detail.value
    })
  },

  // 提交编辑
  async handleEdit() {
    const content = this.data.editContent.trim()
    if (!content) {
      wx.showToast({
        title: '请输入内容',
        icon: 'none'
      })
      return
    }

    try {
      const { type, id, commentIndex, replyIndex } = this.data.editingComment

      // 调用更新接口（需要后端支持）
      await commentApi.update(id, { content })

      if (type === 'comment') {
        // 更新评论内容
        this.setData({
          [`comments[${commentIndex}].content`]: content,
          showEditInput: false,
          editingComment: null,
          editContent: ''
        })
      } else if (type === 'reply') {
        // 更新回复内容
        this.setData({
          [`comments[${commentIndex}].replies[${replyIndex}].content`]: content,
          showEditInput: false,
          editingComment: null,
          editContent: ''
        })
      }

      wx.showToast({
        title: '修改成功',
        icon: 'success'
      })
    } catch (err) {
      console.error('修改失败:', err)
      wx.showToast({
        title: err.message || '修改失败',
        icon: 'none'
      })
    }
  }
})
