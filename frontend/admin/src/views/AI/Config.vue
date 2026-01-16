<template>
  <div class="ai-config-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>AI 配置</span>
          <el-tag :type="config.enabled === 1 ? 'success' : 'info'">
            {{ config.enabled === 1 ? '已启用' : '未启用' }}
          </el-tag>
        </div>
      </template>
      
      <el-form :model="config" label-width="120px" v-loading="loading">
        <el-form-item label="启用状态">
          <el-switch 
            v-model="config.enabled" 
            :active-value="1" 
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>
        
        <el-form-item label="AI服务商">
          <el-select v-model="config.provider" placeholder="选择服务商" style="width: 300px">
            <el-option label="DeepSeek" value="deepseek" />
            <el-option label="OpenAI" value="openai" />
            <el-option label="智谱AI (GLM)" value="zhipu" />
            <el-option label="通义千问" value="dashscope" />
            <el-option label="百度千帆" value="qianfan" />
            <el-option label="Moonshot (Kimi)" value="moonshot" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="模型名称">
          <el-input v-model="config.model" placeholder="如: deepseek-chat" style="width: 300px" />
          <div class="form-tip">
            DeepSeek: deepseek-chat, deepseek-coder<br>
            OpenAI: gpt-4, gpt-3.5-turbo<br>
            智谱: glm-4, glm-3-turbo
          </div>
        </el-form-item>
        
        <el-form-item label="API Key">
          <el-input 
            v-model="config.apiKey" 
            placeholder="输入API密钥" 
            style="width: 400px"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="API地址">
          <el-input v-model="config.baseUrl" placeholder="如: https://api.deepseek.com" style="width: 400px" />
          <div class="form-tip">
            DeepSeek: https://api.deepseek.com<br>
            OpenAI: https://api.openai.com<br>
            智谱: https://open.bigmodel.cn/api/paas
          </div>
        </el-form-item>
        
        <el-form-item label="最大Token数">
          <el-input-number v-model="config.maxTokens" :min="100" :max="8000" :step="100" />
          <span class="form-tip-inline">控制AI回复的最大长度</span>
        </el-form-item>
        
        <el-form-item label="温度参数">
          <el-slider v-model="temperatureValue" :min="0" :max="100" :step="5" style="width: 300px" />
          <span class="form-tip-inline">{{ (temperatureValue / 100).toFixed(2) }} (越高越有创意，越低越稳定)</span>
        </el-form-item>
        
        <el-form-item label="每日限制">
          <el-input-number v-model="config.dailyLimit" :min="1" :max="1000" :step="10" />
          <span class="form-tip-inline">每用户每日最大请求次数</span>
        </el-form-item>
        
        <el-form-item label="系统提示词">
          <el-input 
            v-model="config.systemPrompt" 
            type="textarea" 
            :rows="4" 
            placeholder="设置AI的角色和行为规范"
            style="width: 500px"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="saveConfig" :loading="saving">保存配置</el-button>
          <el-button @click="testConnection" :loading="testing">测试连接</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card style="margin-top: 20px">
      <template #header>
        <span>使用说明</span>
      </template>
      <div class="help-content">
        <h4>1. 获取API Key</h4>
        <ul>
          <li><strong>DeepSeek:</strong> 访问 <a href="https://platform.deepseek.com" target="_blank">platform.deepseek.com</a> 注册并获取API Key</li>
          <li><strong>OpenAI:</strong> 访问 <a href="https://platform.openai.com" target="_blank">platform.openai.com</a> 获取API Key</li>
          <li><strong>智谱AI:</strong> 访问 <a href="https://open.bigmodel.cn" target="_blank">open.bigmodel.cn</a> 获取API Key</li>
        </ul>
        
        <h4>2. 推荐配置</h4>
        <ul>
          <li>国内服务器推荐使用 DeepSeek 或 智谱AI，访问稳定</li>
          <li>温度参数建议设置为 0.7，平衡创意和稳定性</li>
          <li>最大Token数建议 2000，足够大多数对话场景</li>
        </ul>
        
        <h4>3. 费用说明</h4>
        <ul>
          <li>DeepSeek: 约 ¥1/百万token，性价比高</li>
          <li>智谱GLM-4: 约 ¥0.1/千token</li>
          <li>OpenAI GPT-4: 约 $0.03/千token</li>
        </ul>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAiConfig, updateAiConfig, testAiConnection } from '@/api/ai'

const loading = ref(false)
const saving = ref(false)
const testing = ref(false)

const config = ref({
  id: null,
  provider: 'deepseek',
  model: 'deepseek-chat',
  apiKey: '',
  baseUrl: 'https://api.deepseek.com',
  maxTokens: 2000,
  temperature: 0.7,
  systemPrompt: '你是小博，一个专业的计算机技术博客AI助手。你擅长解答编程、软件开发、计算机科学等技术问题。请用简洁、准确、专业的语言回答用户的问题，必要时可以提供代码示例。',
  enabled: 0,
  dailyLimit: 100
})

// 温度值转换（0-100 对应 0-1）
const temperatureValue = computed({
  get: () => Math.round((config.value.temperature || 0.7) * 100),
  set: (val) => { config.value.temperature = val / 100 }
})

async function fetchConfig() {
  loading.value = true
  try {
    const res = await getAiConfig()
    if (res.code === 200 && res.data) {
      config.value = { ...config.value, ...res.data }
    }
  } catch (e) {
    console.error('获取配置失败', e)
  } finally {
    loading.value = false
  }
}

async function saveConfig() {
  saving.value = true
  try {
    const res = await updateAiConfig(config.value)
    if (res.code === 200) {
      ElMessage.success('保存成功')
      fetchConfig()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function testConnection() {
  testing.value = true
  try {
    const res = await testAiConnection()
    if (res.code === 200) {
      ElMessage.success('连接测试成功')
    } else {
      ElMessage.error(res.message || '连接测试失败')
    }
  } catch (e) {
    ElMessage.error('连接测试失败')
  } finally {
    testing.value = false
  }
}

onMounted(() => {
  fetchConfig()
})
</script>

<style lang="scss" scoped>
.ai-config-page {
  padding: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  line-height: 1.6;
}

.form-tip-inline {
  font-size: 12px;
  color: #909399;
  margin-left: 12px;
}

.help-content {
  font-size: 14px;
  line-height: 1.8;
  color: #606266;
  
  h4 {
    margin: 16px 0 8px;
    color: #303133;
    
    &:first-child {
      margin-top: 0;
    }
  }
  
  ul {
    margin: 0;
    padding-left: 20px;
  }
  
  a {
    color: #409eff;
    text-decoration: none;
    
    &:hover {
      text-decoration: underline;
    }
  }
}
</style>
