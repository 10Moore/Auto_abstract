<template>
  <div class="layout">
    <!-- 左侧边栏 -->
    <Sidebar
      :files="allFiles"
      @refresh="refreshAllData"
      @show-summary="showSummary"
    />

    <!-- 中间主区域 -->
    <main class="main" @dragover.prevent @drop="handleMainAreaDrop">
      <FileUploader @refresh="refreshAllData" />
      <SearchBox :files="filteredFiles" @show-summary="showSummary" />
      <DownloadBox />

      <!-- 摘要展示区域 -->
      <SummaryView 
        v-if="showSummaryPanel" 
        :content="currentSummary"
        :file-id="currentFileId" 
        @close="hideSummary"
      />

      <!-- 展开右侧边栏按钮 -->
      <div 
        v-if="!showRightSidebar" 
        class="show-right-bar" 
        @click="toggleRightSidebar"
      >
        <el-icon><ArrowLeft /></el-icon> 展开文件栏
      </div>
    </main>

    <!-- 右侧边栏 -->
    <FileList
      v-if="showRightSidebar"
      :files="allFiles"
      @toggle-hide="toggleRightSidebar"
      @refresh="refreshAllData"
      @show-summary="showSummary"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElLoading } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import axios from 'axios'

// 组件导入
import Sidebar from './components/Sidebar.vue'
import FileList from './components/FileList.vue'
import FileUploader from './components/FileUploader.vue'
import SearchBox from './components/SearchBox.vue'
import DownloadBox from './components/DownloadBox.vue'
import SummaryView from './components/SummaryView.vue'


// 状态管理
const allFiles = ref([])
const showRightSidebar = ref(true)
const searchQuery = ref('')
const showSummaryPanel = ref(false)
const currentSummary = ref('')
const currentFileId = ref(null)


// 计算属性：过滤文件列表
const filteredFiles = computed(() => {
  return allFiles.value.filter(file => 
    file.filename.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})

// 初始化加载数据
onMounted(() => {
  refreshAllData()
})

// 刷新所有数据
async function refreshAllData() {
  const loadingInstance = ElLoading.service({
    target: '.layout',
    text: '正在加载数据...'
  })

  try {
    const [filesRes, foldersRes] = await Promise.all([
      axios.get('/api/files'),
      axios.get('/api/folders')
    ])
    allFiles.value = filesRes.data
  } catch (error) {
    ElMessage.error('数据加载失败: ' + error.message)
  } finally {
    loadingInstance.close()
  }
}

// 主区域拖放处理（从文件夹移出）
async function handleMainAreaDrop(event) {
  const fileId = event.dataTransfer.getData('file-id')
  const folderId = event.dataTransfer.getData('from-folder')

  if (!fileId || !folderId) return

  try {
    await axios.put(`/api/files/${fileId}/remove-from-folder/${folderId}`)
    ElMessage.success('文件已移出文件夹')
    refreshAllData()
  } catch (error) {
    ElMessage.error('操作失败: ' + error.message)
  }
}

async function showSummary(fileId) {
  currentFileId.value = fileId;  // 更新当前文件ID
  try {
    const res = await axios.get(`/api/summaries/${fileId}`)
    currentSummary.value = res.data.content;
    showSummaryPanel.value = true;
  } catch (error) {
    ElMessage.error('获取摘要失败');
  }
}


function hideSummary() {
  showSummaryPanel.value = false
  currentSummary.value = ''
}

// 切换右侧边栏
function toggleRightSidebar() {
  showRightSidebar.value = !showRightSidebar.value
}

// 暴露方法给模板
defineExpose({
  refreshAllData
})
</script>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
  position: relative;
}

.main {
  flex: 1;
  padding: 20px;
  overflow: auto;
  background-color: #f5f7fa;
  position: relative;
}

.show-right-bar {
  position: absolute;
  top: 20px;
  right: 0;
  background: var(--el-color-primary);
  color: white;
  padding: 8px 12px;
  border-radius: 4px 0 0 4px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
}

.show-right-bar:hover {
  background: var(--el-color-primary-light-3);
  right: 2px;
}

.show-right-bar .el-icon {
  font-size: 16px;
}
</style>