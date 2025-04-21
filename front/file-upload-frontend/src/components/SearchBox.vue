
<template>
  <div class="search-box">
    <el-input
      v-model="searchTerm"
      placeholder="搜索文件名..."
      clearable
      class="search-input"
    />

    <div class="results" v-if="searchTerm">
      <template v-if="filteredFiles.length > 0">
        <ul>
          <template v-for="file in filteredFiles" :key="file.id">
            <el-tooltip :content="file.filename" placement="top">
              <li
                class="file-item"
                draggable="true"
                @dragstart="handleDragStart(file)"
                @contextmenu.prevent="openContextMenu(file, $event)"
              >
                📄 {{ file.filename }}
              </li>
            </el-tooltip>
          </template>
        </ul>
      </template>
      <el-empty v-else description="未找到相关文件" />
    </div>

    <div
      v-if="contextMenuVisible"
      class="context-menu"
      :style="contextStyle"
    >
      <div @click="downloadFile"> 📁 下载文件</div>
      <div @click="removeFile"> 🗑 删除文件</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const props = defineProps({
  files: Array
})

const searchTerm = ref('')
const filteredFiles = computed(() => {
  return props.files.filter(f => f.filename.includes(searchTerm.value))
})

const contextFile = ref(null)
const contextMenuVisible = ref(false)
const contextStyle = ref({ top: '0px', left: '0px' })

function openContextMenu(file, event) {
  event.preventDefault()
  contextFile.value = file
  contextStyle.value = {
    top: `${event.clientY}px`,
    left: `${event.clientX}px`
  }
  contextMenuVisible.value = true
}

function downloadFile() {
  const file = contextFile.value
  const link = document.createElement('a')
  link.href = `/api/files/download/${file.filename}`
  link.download = file.filename
  document.body.appendChild(link)
  link.click()
  contextMenuVisible.value = false
}

async function removeFile() {
  const file = contextFile.value
  try {
    await axios.delete(`/api/files/${file.id}`)
    ElMessage.success(`已删除文件：${file.filename}`)
    contextMenuVisible.value = false
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

function handleDragStart(file) {
  event.dataTransfer.setData('text/plain', file.filename)
  event.dataTransfer.setData('file-id', file.id)
}

onMounted(() => {
  window.addEventListener('click', () => {
    contextMenuVisible.value = false
  })
})
</script>

<style scoped>
.search-box {
  padding: 16px;
  margin-top: 12px;
}
.search-input {
  width: 100%;
  max-width: 400px;
  margin-bottom: 16px;
}
.results ul {
  list-style: none;
  padding: 0;
}
.file-item {
  background: #fff;
  padding: 8px;
  margin-bottom: 6px;
  border-radius: 6px;
  cursor: pointer;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);
}
.file-item:hover {
  background: #f0f0f0;
}
.context-menu {
  position: fixed;
  background: #fff;
  border: 1px solid #ccc;
  padding: 6px 12px;
  border-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  z-index: 1000;
  cursor: pointer;
  font-size: 14px;
}
.context-menu div:hover {
  background: #f5f5f5;
}
</style>
