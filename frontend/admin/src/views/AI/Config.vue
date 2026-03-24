<template>
  <div class="ai-config-page">
    <!-- 模型列表 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <span>AI 模型配置</span>
          <el-button type="primary" @click="openAddDialog">
            <el-icon><Plus /></el-icon> 添加模型
          </el-button>
        </div>
      </template>
      
      <el-table :data="configs" v-loading="loading" style="width: 100%">
        <el-table-column label="模型名称" min-width="220">
          <template #default="{ row }">
            <div class="model-name-cell">
              <img 
                v-if="row.logoUrl" 
                :src="getFullUrl(row.logoUrl)" 
                class="model-logo"
              />
              <div class="model-name-info">
                <div class="model-name-row">
                  <span class="model-display-name">{{ row.displayName || row.model }}</span>
                  <el-tag v-if="row.isDefault === 1" type="warning" size="small" style="margin-left: 8px">默认</el-tag>
                </div>
                <span class="model-id-text">{{ row.model }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="服务商" width="130">
          <template #default="{ row }">
            <el-tag :type="getProviderTagType(row.provider)" disable-transitions>
              {{ getProviderLabel(row.provider) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="API地址" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <span>{{ row.baseUrl || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch 
              v-model="row.enabled" 
              :active-value="1" 
              :inactive-value="0"
              @change="handleToggleEnabled(row)"
              size="small"
            />
          </template>
        </el-table-column>
        <el-table-column label="使用场景" min-width="200">
          <template #default="{ row }">
            <div class="scene-tags">
              <el-tag v-if="row.useForChat === 1" size="small">AI对话</el-tag>
              <el-tag v-if="row.useForStudyScore === 1" type="success" size="small">面试评分</el-tag>
              <el-tag v-if="row.isDefaultStudyScore === 1" type="warning" size="small">评分默认</el-tag>
              <el-tag v-if="row.useForChat !== 1 && row.useForStudyScore !== 1" type="info" size="small">未分配</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="深度思考" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.supportThinking === 1" type="success" size="small">已开启</el-tag>
            <el-tag v-else type="info" size="small">关闭</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="每日限制" width="100" align="center">
          <template #default="{ row }">{{ row.dailyLimit }}</template>
        </el-table-column>
        <el-table-column label="排序" width="80" align="center">
          <template #default="{ row }">{{ row.sortOrder }}</template>
        </el-table-column>
        <el-table-column label="操作" width="340" align="center" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
            <el-button link type="warning" @click="handleSetDefault(row)" v-if="row.isDefault !== 1">设为默认</el-button>
            <el-button
              link
              type="success"
              @click="handleSetStudyScoreDefault(row)"
              v-if="row.useForStudyScore === 1 && row.isDefaultStudyScore !== 1"
            >
              设为评分默认
            </el-button>
            <el-button link type="primary" @click="openScoreTestDialog(row)">评分测试</el-button>
            <el-button link type="success" @click="handleTest(row)">测试</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 使用说明 -->
    <el-card style="margin-top: 20px">
      <template #header>
        <span>使用说明</span>
      </template>
      <div class="help-content">
        <h4>1. 多模型配置</h4>
        <ul>
          <li>每个AI模型可独立配置服务商、API Key、参数等</li>
          <li>启用的模型会在用户端显示，用户可自由切换</li>
          <li><strong>默认模型</strong>：新用户首次使用时默认选择的模型</li>
          <li><strong>评分默认模型</strong>：学习模块面试题 AI 打分专用模型，可与对话默认模型分开配置</li>
        </ul>
        
        <h4>2. 获取API Key</h4>
        <ul>
          <li><strong>硅基流动（推荐免费）:</strong> 访问 <a href="https://cloud.siliconflow.cn" target="_blank">cloud.siliconflow.cn</a> 注册，新用户赠送余额，含大量免费模型</li>
          <li><strong>DeepSeek:</strong> 访问 <a href="https://platform.deepseek.com" target="_blank">platform.deepseek.com</a> 注册并获取API Key</li>
          <li><strong>OpenAI:</strong> 访问 <a href="https://platform.openai.com" target="_blank">platform.openai.com</a> 获取API Key</li>
          <li><strong>智谱AI:</strong> 访问 <a href="https://open.bigmodel.cn" target="_blank">open.bigmodel.cn</a> 获取API Key（GLM-4-Flash 完全免费）</li>
        </ul>
        
        <h4>3. 推荐配置</h4>
        <ul>
          <li>国内服务器推荐使用 DeepSeek 或 智谱AI，访问稳定</li>
          <li>温度参数建议设置为 0.7，平衡创意和稳定性</li>
          <li>最大Token数建议 2000，足够大多数对话场景</li>
        </ul>

        <h4>4. 费用说明</h4>
        <ul>
          <li>DeepSeek: 约 ¥1/百万token，性价比高</li>
          <li>智谱GLM-4: 约 ¥0.1/千token</li>
          <li>OpenAI GPT-4: 约 $0.03/千token</li>
        </ul>
      </div>
    </el-card>

    <!-- 新增/编辑模型弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEdit ? '编辑模型配置' : '添加模型配置'" 
      width="650px"
      destroy-on-close
    >
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="120px">
        <el-form-item label="显示名称" prop="displayName">
          <el-input v-model="form.displayName" placeholder="如: DeepSeek Chat、GPT-4o" style="width: 350px" />
          <div class="form-tip">用户端显示的模型名称，便于用户识别</div>
        </el-form-item>

        <el-form-item label="模型Logo">
          <div class="logo-selector">
            <div 
              v-for="logo in logoList" 
              :key="logo.id" 
              class="logo-option"
              :class="{ active: form.logoId === logo.id }"
              @click="form.logoId = logo.id"
              :title="logo.name"
            >
              <img :src="getFullUrl(logo.logoUrl)" class="logo-option-img" />
            </div>
            <div 
              class="logo-option none-option"
              :class="{ active: !form.logoId }"
              @click="form.logoId = null"
              title="不使用Logo"
            >
              <span>无</span>
            </div>
          </div>
          <div class="form-tip">可在「Logo管理」中添加更多Logo，每个模型可自由选择</div>
        </el-form-item>
        
        <el-form-item label="AI服务商" prop="provider">
          <el-select v-model="form.provider" placeholder="选择服务商" style="width: 350px" @change="onProviderChange">
            <el-option label="DeepSeek" value="deepseek" />
            <el-option label="OpenAI" value="openai" />
            <el-option label="硅基流动 SiliconFlow" value="siliconflow" />
            <el-option label="智谱AI (GLM)" value="zhipu" />
            <el-option label="通义千问" value="dashscope" />
            <el-option label="百度千帆" value="qianfan" />
            <el-option label="Moonshot (Kimi)" value="moonshot" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="模型名称" prop="model">
          <el-input v-model="form.model" placeholder="如: deepseek-chat" style="width: 350px" />
          <div class="form-tip">
            DeepSeek: deepseek-chat, deepseek-coder, deepseek-reasoner<br>
            OpenAI: gpt-4o, gpt-4, gpt-3.5-turbo<br>
            智谱: glm-4, glm-4-flash, glm-3-turbo<br>
            硅基流动(免费): Qwen/Qwen2.5-7B-Instruct, internlm/internlm2_5-7b-chat<br>
            硅基流动(收费): Pro/deepseek-ai/DeepSeek-V3, Qwen/Qwen2.5-72B-Instruct
          </div>
        </el-form-item>
        
        <el-form-item label="API Key" prop="apiKey">
          <el-input 
            v-model="form.apiKey" 
            placeholder="输入API密钥" 
            style="width: 400px"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="API地址" prop="baseUrl">
          <el-input v-model="form.baseUrl" placeholder="如: https://api.deepseek.com" style="width: 400px" />
          <div class="form-tip">
            DeepSeek: https://api.deepseek.com/v1<br>
            OpenAI: https://api.openai.com/v1<br>
            硅基流动: https://api.siliconflow.cn/v1<br>
            智谱: https://open.bigmodel.cn/api/paas/v4<br>
            通义千问: https://dashscope.aliyuncs.com/compatible-mode/v1<br>
            <strong>注意：</strong>URL 需包含版本号，系统会自动拼接 /chat/completions
          </div>
        </el-form-item>
        
        <el-form-item label="最大Token数">
          <el-input-number v-model="form.maxTokens" :min="100" :max="8000" :step="100" />
          <span class="form-tip-inline">控制AI回复的最大长度</span>
        </el-form-item>
        
        <el-form-item label="温度参数">
          <el-slider v-model="temperatureValue" :min="0" :max="100" :step="5" style="width: 300px" />
          <span class="form-tip-inline">{{ (temperatureValue / 100).toFixed(2) }} (越高越有创意，越低越稳定)</span>
        </el-form-item>
        
        <el-form-item label="每日限制">
          <el-input-number v-model="form.dailyLimit" :min="1" :max="1000" :step="10" />
          <span class="form-tip-inline">每用户每日最大请求次数</span>
        </el-form-item>
        
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" :step="1" />
          <span class="form-tip-inline">越小越靠前</span>
        </el-form-item>

        <el-form-item label="启用状态">
          <el-switch 
            v-model="form.enabled" 
            :active-value="1" 
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          />
        </el-form-item>

        <el-form-item label="使用场景">
          <div class="scene-switches">
            <div class="scene-switch-item">
              <span>用于 AI 对话</span>
              <el-switch v-model="form.useForChat" :active-value="1" :inactive-value="0" />
            </div>
            <div class="scene-switch-item">
              <span>用于面试评分</span>
              <el-switch v-model="form.useForStudyScore" :active-value="1" :inactive-value="0" />
            </div>
          </div>
          <div class="form-tip">面试题抽查的 AI 打分只会使用「用于面试评分」并被设为评分默认的模型。</div>
        </el-form-item>
        
        <el-form-item label="深度思考">
          <el-switch 
            v-model="form.supportThinking" 
            :active-value="1" 
            :inactive-value="0"
            active-text="开启"
            inactive-text="关闭"
          />
          <div class="form-tip">
            适用于支持推理/思考链的模型（如 DeepSeek-R1 deepseek-reasoner）。<br>
            开启后用户端会展示AI的思考过程（reasoning_content）。
          </div>
        </el-form-item>
        
        <el-form-item label="系统提示词">
          <el-input 
            v-model="form.systemPrompt" 
            type="textarea" 
            :rows="4" 
            placeholder="设置AI的角色和行为规范"
            style="width: 500px"
          />
        </el-form-item>

        <el-form-item label="评分提示词">
          <el-input 
            v-model="form.scoreSystemPrompt" 
            type="textarea" 
            :rows="5" 
            placeholder="用于学习模块面试题 AI 打分的专用系统提示词"
            style="width: 500px"
          />
          <div class="form-tip">留空时系统会使用内置评分提示词；建议明确评分风格、输出口径和点评要求。</div>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="scoreTestDialogVisible"
      title="学习模块评分测试"
      width="980px"
      destroy-on-close
    >
      <div class="score-test-header" v-if="activeTestConfig">
        <div class="score-test-model">
          <strong>{{ activeTestConfig.displayName || activeTestConfig.model }}</strong>
          <span>{{ getProviderLabel(activeTestConfig.provider) }} / {{ activeTestConfig.model }}</span>
        </div>
        <div class="score-test-actions">
          <el-button @click="fillScoreTestDemo">填充示例</el-button>
          <el-button @click="resetScoreTestResult">清空结果</el-button>
          <el-button type="primary" :loading="scoreTesting" @click="submitScoreTest">开始评分测试</el-button>
        </div>
      </div>

      <div class="score-test-layout">
        <el-form ref="scoreTestFormRef" :model="scoreTestForm" :rules="scoreTestRules" label-position="top" class="score-test-form">
          <el-form-item label="题目标题" prop="questionTitle">
            <el-input v-model="scoreTestForm.questionTitle" maxlength="255" placeholder="例如：说一下 TCP 三次握手的过程及意义" />
          </el-form-item>
          <el-form-item label="题目内容">
            <el-input v-model="scoreTestForm.questionStem" type="textarea" :rows="4" maxlength="20000" show-word-limit placeholder="可补充题目背景、追问方向、限定条件等" />
          </el-form-item>
          <el-form-item label="标准答案" prop="standardAnswer">
            <el-input v-model="scoreTestForm.standardAnswer" type="textarea" :rows="8" maxlength="50000" show-word-limit placeholder="填写该题的标准答案、参考要点、答题结构" />
          </el-form-item>
          <el-form-item label="候选人答案" prop="candidateAnswer">
            <el-input v-model="scoreTestForm.candidateAnswer" type="textarea" :rows="8" maxlength="50000" show-word-limit placeholder="填写待评分的作答内容，尽量模拟真实面试表达" />
          </el-form-item>
          <div class="score-base-grid">
            <el-form-item label="满分">
              <el-input-number v-model="scoreTestForm.scoreFullMark" :min="1" :max="1000" controls-position="right" />
            </el-form-item>
            <el-form-item label="及格分">
              <el-input-number v-model="scoreTestForm.scorePassMark" :min="0" :max="1000" controls-position="right" />
            </el-form-item>
            <el-form-item label="自评结果">
              <el-select v-model="scoreTestForm.candidateSelfAssessment">
                <el-option label="记得" :value="1" />
                <el-option label="模糊" :value="2" />
                <el-option label="忘记" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item label="自评分">
              <el-input-number v-model="scoreTestForm.candidateSelfScore" :min="0" :max="1000" controls-position="right" />
            </el-form-item>
          </div>
          <el-form-item label="评分规则 JSON">
            <el-input v-model="scoreTestForm.scoreRubricJson" type="textarea" :rows="4" maxlength="5000" show-word-limit placeholder='可选，例如：{"accuracy":40,"completeness":30,"clarity":20,"practicality":10}' />
          </el-form-item>
          <el-form-item label="测试时覆盖评分提示词">
            <el-input v-model="scoreTestForm.promptOverride" type="textarea" :rows="4" maxlength="4000" show-word-limit placeholder="留空则使用模型配置中的评分提示词；这里可临时测试不同评分口径" />
          </el-form-item>
        </el-form>

        <div class="score-test-result">
          <el-empty v-if="!scoreTestResult" description="还未执行评分测试" />
          <template v-else>
            <el-alert
              :title="scoreTestResult.scoreStatus === 2 ? '评分测试成功' : '评分测试失败'"
              :type="scoreTestResult.scoreStatus === 2 ? 'success' : 'error'"
              :description="scoreTestResult.scoreStatus === 2 ? `模型已返回评分结果，耗时 ${scoreTestResult.durationMs || 0}ms` : (scoreTestResult.errorMessage || '模型未返回有效评分结果')"
              :closable="false"
              show-icon
            />

            <div class="score-result-cards">
              <div class="score-result-card">
                <span>AI得分</span>
                <strong>{{ scoreTestResult.aiScore ?? '-' }}</strong>
              </div>
              <div class="score-result-card">
                <span>满分 / 及格</span>
                <strong>{{ scoreTestResult.fullScore ?? '-' }} / {{ scoreTestResult.passScore ?? '-' }}</strong>
              </div>
              <div class="score-result-card">
                <span>Token</span>
                <strong>{{ scoreTestResult.totalTokens ?? 0 }}</strong>
              </div>
              <div class="score-result-card">
                <span>耗时</span>
                <strong>{{ scoreTestResult.durationMs ?? 0 }}ms</strong>
              </div>
            </div>

            <div class="score-overview-panel" v-if="scoreTestResult.scoreStatus === 2">
              <div class="score-overview-main">
                <div class="score-overview-value">
                  <strong>{{ scoreTestResult.aiScore ?? '-' }}</strong>
                  <span>/ {{ scoreTestResult.fullScore ?? '-' }}</span>
                </div>
                <div class="score-overview-tags">
                  <el-tag :type="scorePassTagType">{{ scorePassText }}</el-tag>
                  <el-tag v-if="scoreLevelText" :type="scoreLevelTagType">{{ scoreLevelText }}</el-tag>
                </div>
              </div>
              <el-progress
                :percentage="scorePercent"
                :stroke-width="10"
                :show-text="false"
                :status="scoreTestResult.passed ? 'success' : 'exception'"
              />
              <p class="score-overview-tip">
                {{ scoreOverviewText }}
              </p>
            </div>

            <el-descriptions :column="1" border class="score-meta-box">
              <el-descriptions-item label="模型">{{ scoreTestResult.configDisplayName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="服务商 / Model">{{ scoreTestResult.provider || '-' }} / {{ scoreTestResult.modelName || '-' }}</el-descriptions-item>
              <el-descriptions-item label="提示词版本">{{ scoreTestResult.promptVersion || '-' }}</el-descriptions-item>
            </el-descriptions>

            <div class="score-section">
              <h4>评分提示词</h4>
              <el-input :model-value="scoreTestResult.resolvedSystemPrompt || ''" type="textarea" :rows="5" readonly />
            </div>

            <div class="score-section" v-if="scoreTestResult.strengthsText || scoreTestResult.weaknessesText || scoreTestResult.suggestionText">
              <h4>评分结论</h4>
              <div class="feedback-grid">
                <div class="feedback-card" v-if="scoreTestResult.strengthsText">
                  <h5>优点</h5>
                  <p>{{ scoreTestResult.strengthsText }}</p>
                </div>
                <div class="feedback-card" v-if="scoreTestResult.weaknessesText">
                  <h5>不足</h5>
                  <p>{{ scoreTestResult.weaknessesText }}</p>
                </div>
                <div class="feedback-card" v-if="scoreTestResult.suggestionText">
                  <h5>建议</h5>
                  <p>{{ scoreTestResult.suggestionText }}</p>
                </div>
              </div>
            </div>

            <div class="score-section" v-if="dimensionScoreList.length">
              <h4>维度评分</h4>
              <div class="dimension-grid">
                <div v-for="item in dimensionScoreList" :key="item.key" class="dimension-card">
                  <div class="dimension-card-head">
                    <span>{{ item.label }}</span>
                    <strong>{{ item.displayValue }}</strong>
                  </div>
                  <el-progress :percentage="item.percent" :stroke-width="8" :show-text="false" />
                </div>
              </div>
            </div>

            <div class="score-section" v-if="keywordHitEntries.length">
              <h4>关键词命中</h4>
              <div class="keyword-grid">
                <div v-for="item in keywordHitEntries" :key="item.key" class="keyword-card">
                  <span>{{ item.label }}</span>
                  <strong>{{ item.value }}</strong>
                </div>
              </div>
            </div>

            <div class="score-section">
              <h4>维度评分 JSON</h4>
              <el-input :model-value="formatJson(scoreTestResult.dimensionScores)" type="textarea" :rows="6" readonly />
            </div>

            <div class="score-section">
              <h4>关键词命中 JSON</h4>
              <el-input :model-value="formatJson(scoreTestResult.keywordHitJson)" type="textarea" :rows="6" readonly />
            </div>

            <div class="score-section">
              <h4>模型原始返回</h4>
              <el-input :model-value="formatJson(scoreTestResult.rawResponse)" type="textarea" :rows="10" readonly />
            </div>
          </template>
        </div>
      </div>

      <template #footer>
        <el-button @click="scoreTestDialogVisible = false">关闭</el-button>
        <el-button type="primary" :loading="scoreTesting" @click="submitScoreTest">重新测试</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getAiConfigs, addAiConfig, updateAiConfig, deleteAiConfig, setDefaultAiConfig, setDefaultStudyScoreAiConfig, testAiConnection, testStudyScore, getAiLogos } from '@/api/ai'

const loading = ref(false)
const saving = ref(false)
const configs = ref([])
const logoList = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const scoreTestDialogVisible = ref(false)
const scoreTesting = ref(false)
const scoreTestFormRef = ref(null)
const scoreTestResult = ref(null)
const activeTestConfig = ref(null)

const defaultForm = {
  id: null,
  displayName: '',
  provider: 'deepseek',
  model: 'deepseek-chat',
  apiKey: '',
  baseUrl: 'https://api.deepseek.com/v1',
  maxTokens: 2000,
  temperature: 0.7,
  systemPrompt: '你是小博，一个专业的计算机技术博客AI助手。你擅长解答编程、软件开发、计算机科学等技术问题。请用简洁、准确、专业的语言回答用户的问题，必要时可以提供代码示例。',
  enabled: 1,
  dailyLimit: 100,
  sortOrder: 0,
  isDefault: 0,
  useForChat: 1,
  useForStudyScore: 1,
  isDefaultStudyScore: 0,
  supportThinking: 0,
  logoId: null,
  scoreSystemPrompt: '你是一名严格、客观、注重表达质量的技术面试官。请基于题目、标准答案和候选人作答进行评分，重点关注准确性、完整性、条理性、工程实践理解和面试表达。'
}

const form = ref({ ...defaultForm })

const defaultScoreTestForm = {
  configId: null,
  questionTitle: '',
  questionStem: '',
  standardAnswer: '',
  candidateAnswer: '',
  scoreFullMark: 100,
  scorePassMark: 60,
  scoreRubricJson: '',
  candidateSelfAssessment: 2,
  candidateSelfScore: 60,
  promptOverride: ''
}

const scoreTestForm = ref({ ...defaultScoreTestForm })

const formRules = {
  displayName: [{ required: true, message: '请输入显示名称', trigger: 'blur' }],
  provider: [{ required: true, message: '请选择服务商', trigger: 'change' }],
  model: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
  apiKey: [{ required: true, message: '请输入API Key', trigger: 'blur' }],
  baseUrl: [{ required: true, message: '请输入API地址', trigger: 'blur' }]
}

const scoreTestRules = {
  questionTitle: [{ required: true, message: '请输入题目标题', trigger: 'blur' }],
  standardAnswer: [{ required: true, message: '请输入标准答案', trigger: 'blur' }],
  candidateAnswer: [{ required: true, message: '请输入候选人答案', trigger: 'blur' }]
}

// 温度值转换（0-100 对应 0-1）
const temperatureValue = computed({
  get: () => Math.round((form.value.temperature || 0.7) * 100),
  set: (val) => { form.value.temperature = val / 100 }
})

const providerMap = {
  deepseek: { label: 'DeepSeek', type: '', baseUrl: 'https://api.deepseek.com/v1', model: 'deepseek-chat' },
  openai: { label: 'OpenAI', type: 'success', baseUrl: 'https://api.openai.com/v1', model: 'gpt-3.5-turbo' },
  siliconflow: { label: '硅基流动', type: 'primary', baseUrl: 'https://api.siliconflow.cn/v1', model: 'Qwen/Qwen2.5-7B-Instruct' },
  zhipu: { label: '智谱AI', type: 'warning', baseUrl: 'https://open.bigmodel.cn/api/paas/v4', model: 'glm-4-flash' },
  dashscope: { label: '通义千问', type: 'danger', baseUrl: 'https://dashscope.aliyuncs.com/compatible-mode/v1', model: 'qwen-turbo' },
  qianfan: { label: '百度千帆', type: 'info', baseUrl: 'https://aip.baidubce.com/v1', model: 'ernie-bot' },
  moonshot: { label: 'Moonshot', type: '', baseUrl: 'https://api.moonshot.cn/v1', model: 'moonshot-v1-8k' }
}

const resultLevelMap = {
  1: { label: '优秀', type: 'success' },
  2: { label: '良好', type: 'primary' },
  3: { label: '及格', type: 'warning' },
  4: { label: '待加强', type: 'danger' }
}

const dimensionLabelMap = {
  accuracy: '准确性',
  completeness: '完整性',
  clarity: '表达清晰度',
  practicality: '工程实践',
  structure: '结构化表达',
  depth: '深度理解',
  communication: '沟通表现',
  problemSolving: '问题分析'
}

function getProviderLabel(provider) {
  return providerMap[provider]?.label || provider
}

function getProviderTagType(provider) {
  return providerMap[provider]?.type || 'info'
}

function onProviderChange(provider) {
  const info = providerMap[provider]
  if (info) {
    form.value.baseUrl = info.baseUrl
    form.value.model = info.model
    if (!form.value.displayName) {
      form.value.displayName = info.label + ' ' + info.model
    }
  }
}

const baseApiUrl = import.meta.env.VITE_API_BASE_URL || ''

function getFullUrl(url) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://') || url.startsWith('//')) return url
  return baseApiUrl + url
}

async function fetchLogos() {
  try {
    const res = await getAiLogos()
    if (res.code === 200) {
      logoList.value = res.data || []
    }
  } catch (e) {
    console.error('获取Logo列表失败', e)
  }
}

async function fetchConfigs() {
  loading.value = true
  try {
    const res = await getAiConfigs()
    if (res.code === 200) {
      configs.value = res.data || []
    }
  } catch (e) {
    console.error('获取配置列表失败', e)
  } finally {
    loading.value = false
  }
}

function openAddDialog() {
  isEdit.value = false
  form.value = { ...defaultForm }
  dialogVisible.value = true
}

function openEditDialog(row) {
  isEdit.value = true
  form.value = { ...row }
  dialogVisible.value = true
}

function openScoreTestDialog(row) {
  if (row.enabled !== 1 || row.useForStudyScore !== 1) {
    ElMessage.warning('当前模型需启用且勾选“用于面试评分”后，评分测试才会真正调用成功')
  }
  activeTestConfig.value = row
  scoreTestForm.value = {
    ...defaultScoreTestForm,
    configId: row.id
  }
  scoreTestResult.value = null
  scoreTestDialogVisible.value = true
}

function resetScoreTestResult() {
  scoreTestResult.value = null
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  if (form.value.useForChat !== 1 && form.value.useForStudyScore !== 1) {
    ElMessage.warning('请至少启用一个使用场景')
    return
  }
  
  saving.value = true
  try {
    if (isEdit.value) {
      const res = await updateAiConfig(form.value)
      if (res.code === 200) {
        ElMessage.success('更新成功')
        dialogVisible.value = false
        fetchConfigs()
      } else {
        ElMessage.error(res.message || '更新失败')
      }
    } else {
      const res = await addAiConfig(form.value)
      if (res.code === 200) {
        ElMessage.success('添加成功')
        dialogVisible.value = false
        fetchConfigs()
      } else {
        ElMessage.error(res.message || '添加失败')
      }
    }
  } catch (e) {
    ElMessage.error(isEdit.value ? '更新失败' : '添加失败')
  } finally {
    saving.value = false
  }
}

async function handleToggleEnabled(row) {
  try {
    await updateAiConfig({ id: row.id, enabled: row.enabled })
    ElMessage.success(row.enabled === 1 ? '已启用' : '已禁用')
  } catch (e) {
    ElMessage.error('操作失败')
    fetchConfigs()
  }
}

async function handleSetDefault(row) {
  try {
    const res = await setDefaultAiConfig(row.id)
    if (res.code === 200) {
      ElMessage.success('已设为默认模型')
      fetchConfigs()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

function fillScoreTestDemo() {
  scoreTestForm.value = {
    ...scoreTestForm.value,
    questionTitle: '请说明 TCP 三次握手的过程及其作用',
    questionStem: '请从连接建立步骤、每次握手的目的、为什么不是两次或四次三个角度来回答。',
    standardAnswer: 'TCP 三次握手包括：客户端发送 SYN 报文请求建立连接；服务端返回 SYN+ACK 报文确认收到请求并同步自己的序列号；客户端再发送 ACK 报文确认，连接正式建立。其作用是让双方确认收发能力正常、同步初始序列号，并避免历史重复连接请求造成误连。不是两次是因为服务端无法确认客户端是否具备最终接收能力；不是四次是因为三次已经足够完成连接建立。',
    candidateAnswer: '首先客户端会先发一个 SYN 给服务端，服务端收到以后回一个 ACK 表示知道了。然后服务端再发一个 SYN 给客户端，客户端再回 ACK，这样双方就建立连接了。三次握手主要是为了保证双方都在线，避免网络不稳定。至于为什么不是两次，我理解两次应该也行，只是三次更安全一点。',
    scoreFullMark: 100,
    scorePassMark: 60,
    scoreRubricJson: '{"accuracy":40,"completeness":30,"clarity":20,"practicality":10}',
    candidateSelfAssessment: 2,
    candidateSelfScore: 68,
    promptOverride: scoreTestForm.value.promptOverride
  }
}

async function handleSetStudyScoreDefault(row) {
  try {
    const res = await setDefaultStudyScoreAiConfig(row.id)
    if (res.code === 200) {
      ElMessage.success('已设为评分默认模型')
      fetchConfigs()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (e) {
    ElMessage.error('操作失败')
  }
}

async function submitScoreTest() {
  const valid = await scoreTestFormRef.value?.validate().catch(() => false)
  if (!valid) return
  const fullScore = Number(scoreTestForm.value.scoreFullMark || 0)
  const passScore = Number(scoreTestForm.value.scorePassMark || 0)
  const selfScore = scoreTestForm.value.candidateSelfScore == null ? null : Number(scoreTestForm.value.candidateSelfScore)
  if (passScore > fullScore) {
    ElMessage.warning('及格分不能大于满分')
    return
  }
  if (selfScore != null && selfScore > fullScore) {
    ElMessage.warning('自评分不能大于满分')
    return
  }
  scoreTesting.value = true
  try {
    const res = await testStudyScore({
      ...scoreTestForm.value,
      questionTitle: scoreTestForm.value.questionTitle.trim(),
      questionStem: scoreTestForm.value.questionStem?.trim() || '',
      standardAnswer: scoreTestForm.value.standardAnswer.trim(),
      candidateAnswer: scoreTestForm.value.candidateAnswer.trim(),
      scoreRubricJson: scoreTestForm.value.scoreRubricJson?.trim() || '',
      promptOverride: scoreTestForm.value.promptOverride?.trim() || ''
    })
    if (res.code === 200) {
      scoreTestResult.value = res.data
      if (res.data?.scoreStatus === 2) {
        ElMessage.success('评分测试完成')
      } else {
        ElMessage.warning(res.data?.errorMessage || '评分测试失败')
      }
    } else {
      ElMessage.error(res.message || '评分测试失败')
    }
  } catch (e) {
    ElMessage.error('评分测试失败')
  } finally {
    scoreTesting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除模型 "${row.displayName || row.model}" 吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteAiConfig(row.id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      fetchConfigs()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

async function handleTest(row) {
  try {
    const res = await testAiConnection({ id: row.id })
    if (res.code === 200) {
      ElMessage.success('连接测试成功')
    } else {
      ElMessage.error(res.message || '连接测试失败')
    }
  } catch (e) {
    ElMessage.error('连接测试失败')
  }
}

function formatJson(value) {
  if (!value) return ''
  try {
    return JSON.stringify(typeof value === 'string' ? JSON.parse(value) : value, null, 2)
  } catch (e) {
    return typeof value === 'string' ? value : JSON.stringify(value, null, 2)
  }
}

function safeParseJson(value) {
  if (!value) return null
  if (typeof value !== 'string') return value
  try {
    return JSON.parse(value)
  } catch (e) {
    return null
  }
}

function normalizePercent(value, fullScore) {
  const numericValue = Number(value)
  const total = Number(fullScore || 100)
  if (!Number.isFinite(numericValue)) return 0
  if (numericValue >= 0 && numericValue <= 1) return Math.round(numericValue * 100)
  if (numericValue >= 0 && total > 0 && numericValue <= total) return Math.round((numericValue / total) * 100)
  return Math.max(0, Math.min(100, Math.round(numericValue)))
}

function extractNumericValue(raw) {
  if (typeof raw === 'number') return raw
  if (typeof raw === 'string') {
    const value = Number(raw)
    return Number.isFinite(value) ? value : null
  }
  if (raw && typeof raw === 'object') {
    const candidates = [raw.score, raw.value, raw.percent, raw.percentage]
    for (const item of candidates) {
      const value = Number(item)
      if (Number.isFinite(value)) return value
    }
  }
  return null
}

function formatDisplayValue(raw) {
  if (raw == null) return '-'
  if (typeof raw === 'number') return Number.isInteger(raw) ? String(raw) : raw.toFixed(2)
  if (typeof raw === 'string') return raw
  if (typeof raw === 'object') {
    if (raw.label) return String(raw.label)
    if (raw.score != null) return String(raw.score)
    if (raw.value != null) return String(raw.value)
    return JSON.stringify(raw)
  }
  return String(raw)
}

const scorePercent = computed(() => {
  const score = Number(scoreTestResult.value?.aiScore || 0)
  const fullScore = Number(scoreTestResult.value?.fullScore || 100)
  if (!Number.isFinite(score) || !Number.isFinite(fullScore) || fullScore <= 0) {
    return 0
  }
  return Math.max(0, Math.min(100, Math.round((score / fullScore) * 100)))
})

const scoreLevelText = computed(() => {
  const backendLabel = scoreTestResult.value?.resultLevelLabel
  if (backendLabel) return backendLabel
  return resultLevelMap[scoreTestResult.value?.resultLevel]?.label || ''
})

const scoreLevelTagType = computed(() => resultLevelMap[scoreTestResult.value?.resultLevel]?.type || 'info')

const scorePassText = computed(() => {
  if (scoreTestResult.value?.passed == null) return '待判定'
  return scoreTestResult.value.passed ? '达到及格线' : '未达及格线'
})

const scorePassTagType = computed(() => {
  if (scoreTestResult.value?.passed == null) return 'info'
  return scoreTestResult.value.passed ? 'success' : 'danger'
})

const scoreOverviewText = computed(() => {
  if (!scoreTestResult.value) return ''
  const score = scoreTestResult.value.aiScore ?? '-'
  const pass = scoreTestResult.value.passScore ?? '-'
  const level = scoreLevelText.value || '未分级'
  return `本次评分 ${score} 分，及格线 ${pass} 分，当前等级为“${level}”。`
})

const dimensionScoreList = computed(() => {
  const parsed = safeParseJson(scoreTestResult.value?.dimensionScores)
  if (!parsed || Array.isArray(parsed) || typeof parsed !== 'object') {
    return []
  }
  const fullScore = scoreTestResult.value?.fullScore || 100
  return Object.entries(parsed).map(([key, raw]) => {
    const numericValue = extractNumericValue(raw)
    return {
      key,
      label: dimensionLabelMap[key] || key,
      displayValue: formatDisplayValue(raw),
      percent: normalizePercent(numericValue == null ? raw : numericValue, fullScore)
    }
  })
})

const keywordHitEntries = computed(() => {
  const parsed = safeParseJson(scoreTestResult.value?.keywordHitJson)
  if (!parsed) {
    return []
  }
  if (Array.isArray(parsed)) {
    return parsed.map((item, index) => ({
      key: `keyword-${index}`,
      label: `命中项 ${index + 1}`,
      value: formatDisplayValue(item)
    }))
  }
  if (typeof parsed === 'object') {
    return Object.entries(parsed).map(([key, value]) => ({
      key,
      label: key,
      value: formatDisplayValue(value)
    }))
  }
  return [{
    key: 'keyword-raw',
    label: '关键词命中',
    value: formatDisplayValue(parsed)
  }]
})

onMounted(() => {
  fetchConfigs()
  fetchLogos()
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

.model-name-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.model-logo {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  object-fit: contain;
  flex-shrink: 0;
  background: var(--bg-darker);
  padding: 2px;
}

.model-name-info {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.model-name-row {
  display: flex;
  align-items: center;
}

.scene-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.logo-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.logo-option {
  width: 48px;
  height: 48px;
  border-radius: 10px;
  border: 2px solid var(--border-color);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  background: var(--bg-darker);
  overflow: hidden;

  &:hover {
    border-color: var(--primary-color);
    transform: scale(1.05);
  }

  &.active {
    border-color: var(--primary-color);
    background: var(--bg-card-hover);
    box-shadow: 0 0 0 2px var(--shadow-color);
  }

  &.none-option {
    background: var(--bg-card);
    color: var(--text-muted);
    font-size: 13px;
    font-weight: 500;
  }
}

.logo-option-img {
  width: 32px;
  height: 32px;
  object-fit: contain;
}

.model-display-name {
  font-weight: 600;
  font-size: 14px;
}

.model-id-text {
  font-size: 12px;
  color: var(--text-muted);
}

.form-tip {
  font-size: 12px;
  color: var(--text-muted);
  margin-top: 4px;
  line-height: 1.6;
}

.form-tip-inline {
  font-size: 12px;
  color: var(--text-muted);
  margin-left: 12px;
}

.scene-switches {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.scene-switch-item {
  width: 320px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 14px;
  border: 1px solid var(--border-color);
  border-radius: 8px;
  background: var(--bg-darker);
}

.score-test-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.score-test-model {
  display: flex;
  flex-direction: column;
  gap: 4px;

  strong {
    font-size: 16px;
    color: var(--text-primary);
  }

  span {
    font-size: 12px;
    color: var(--text-muted);
  }
}

.score-test-actions {
  display: flex;
  gap: 12px;
}

.score-test-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(320px, 380px);
  gap: 18px;
  align-items: start;
}

.score-test-form,
.score-test-result {
  min-width: 0;
}

.score-base-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 16px;
}

.score-result-cards {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin: 16px 0;
}

.score-result-card {
  padding: 14px 16px;
  border-radius: 10px;
  border: 1px solid var(--border-color);
  background: var(--bg-darker);

  span {
    display: block;
    font-size: 12px;
    color: var(--text-muted);
    margin-bottom: 8px;
  }

  strong {
    font-size: 22px;
    color: var(--text-primary);
  }
}

.score-overview-panel {
  padding: 16px;
  margin-bottom: 16px;
  border-radius: 12px;
  border: 1px solid var(--border-color);
  background: var(--bg-darker);
}

.score-overview-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.score-overview-value {
  display: flex;
  align-items: baseline;
  gap: 8px;

  strong {
    font-size: 28px;
    color: var(--text-primary);
    line-height: 1;
  }

  span {
    font-size: 14px;
    color: var(--text-muted);
  }
}

.score-overview-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.score-overview-tip {
  margin-top: 12px;
  color: var(--text-secondary);
  line-height: 1.7;
  font-size: 13px;
}

.score-meta-box {
  margin-bottom: 16px;
}

.score-section + .score-section {
  margin-top: 16px;
}

.score-section h4 {
  margin-bottom: 8px;
  color: var(--text-primary);
  font-size: 14px;
}

.feedback-grid {
  display: grid;
  gap: 12px;
}

.dimension-grid,
.keyword-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.feedback-card {
  padding: 14px 16px;
  border-radius: 10px;
  border: 1px solid var(--border-color);
  background: var(--bg-darker);

  h5 {
    margin-bottom: 8px;
    color: var(--text-primary);
    font-size: 13px;
  }

  p {
    color: var(--text-secondary);
    line-height: 1.7;
  }
}

.dimension-card,
.keyword-card {
  padding: 14px 16px;
  border-radius: 10px;
  border: 1px solid var(--border-color);
  background: var(--bg-darker);
}

.dimension-card-head,
.keyword-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.dimension-card-head {
  margin-bottom: 10px;

  span {
    color: var(--text-secondary);
    font-size: 13px;
  }

  strong {
    color: var(--text-primary);
    font-size: 14px;
  }
}

.keyword-card {
  span {
    color: var(--text-secondary);
    font-size: 13px;
  }

  strong {
    color: var(--text-primary);
    font-size: 14px;
    text-align: right;
    word-break: break-word;
  }
}

.help-content {
  font-size: 14px;
  line-height: 1.8;
  color: var(--text-secondary);
  
  h4 {
    margin: 16px 0 8px;
    color: var(--text-primary);
    
    &:first-child {
      margin-top: 0;
    }
  }
  
  ul {
    margin: 0;
    padding-left: 20px;
  }
  
  a {
    color: var(--primary-color);
    text-decoration: none;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

@media (max-width: 1200px) {
  .score-test-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .score-test-header,
  .score-overview-main,
  .dimension-card-head,
  .keyword-card {
    flex-direction: column;
    align-items: flex-start;
  }

  .score-test-actions,
  .score-overview-tags {
    width: 100%;
    flex-wrap: wrap;
  }

  .score-result-cards,
  .score-base-grid,
  .dimension-grid,
  .keyword-grid {
    grid-template-columns: 1fr;
  }
}
</style>
