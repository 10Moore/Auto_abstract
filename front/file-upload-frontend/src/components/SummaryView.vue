<template>
  <div class="summary-view">
    <div class="summary-header">
      <h3>📝 文件摘要</h3>
      <el-button 
        :icon="Close" 
        size="small" 
        circle 
        @click="$emit('close')" 
        title="关闭摘要"
      />
    </div>
    
    <el-scrollbar class="summary-content">
      <div v-if="loading" class="loading-state">
        <el-icon class="is-loading"><Loading /></el-icon>
        正在生成摘要...
      </div>
      <div v-else-if="error" class="error-state">
        <el-icon><Warning /></el-icon>
        摘要加载失败: {{ error }}
      </div>
      <div v-else-if="content" class="content-text" v-html="renderedHtml"></div>
      <el-empty v-else description="暂无摘要内容" />
    </el-scrollbar>
  </div>
</template>

<script setup>
import { Close, Loading, Warning } from '@element-plus/icons-vue'
import { ref, onMounted, computed, watch } from 'vue'
import axios from 'axios'
import MarkdownIt from 'markdown-it'


const props = defineProps({
  fileId: String
})

const content = ref('')
const loading = ref(false)
const error = ref(null)

const md = new MarkdownIt()

// 将 Markdown 转换为 HTML
const renderedHtml = computed(() => {
  return content.value ? md.render(content.value) : ''
})

// 新增 loadSummary 方法
async function loadSummary(fileId) {
  try {
    loading.value = true
    const res = await axios.get(`/api/files/${fileId}/summary`)
    content.value = res.data
  } catch (e) {
    error.value = e.message
  } finally {
    loading.value = false
  }
}

// onMounted(async () => {
//   try {
//     loading.value = true
//     const res = await axios.get(`/api/files/${props.fileId}/summary`)
//     content.value = res.data
//   } catch (e) {
//     error.value = e.message
//   } finally {
//     loading.value = false
//   }
// })

// 在 onMounted 中加载初始摘要
onMounted(() => {
  if (props.fileId) {
    loadSummary(props.fileId)
  }
})

// 在 fileId 变化时重新加载摘要
watch(() => props.fileId, async (newFileId) => {
  if (newFileId) {
    await loadSummary(newFileId)
  }
})
</script>


<style scoped>
/* 添加加载和错误状态样式 */
.loading-state, .error-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 20px;
  color: var(--el-color-info);
}

.error-state {
  color: var(--el-color-danger);
}

/* 其余样式保持不变 */
.summary-view {
  position: absolute;
  top: 20px;
  left: 20px;
  right: 20px;
  bottom: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
  z-index: 100;
  display: flex;
  flex-direction: column;
}

.summary-header {
  padding: 12px 16px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.summary-content {
  flex: 1;
  padding: 16px;
}

.content-text {
  white-space: pre-wrap;
  line-height: 1.6;
}
</style>