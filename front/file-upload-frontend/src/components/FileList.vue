<template>
  <el-aside width="240px" class="file-list">
    <div class="file-list-header">
      <h3>📄 已上传文件</h3>
      <el-button
        :icon="Minus"
        size="small"
        circle
        @click="$emit('toggle-hide')"
        title="隐藏文件栏"
      />
    </div>

    <el-scrollbar class="scroll-area" native>
      <ul>
        <template v-if="files && files.length">
          <el-tooltip
            v-for="file in files"
            :key="file.id"
            :content="file.filename"
            placement="top"
          >
            <li @click.stop
              class="file-item"
              draggable="true"
              @dragstart="handleDragStart(file)"
              @click="$emit('show-summary', file.id)"
              @contextmenu.prevent.stop="openContextMenu(file, $event)"
            >
              📄 {{ file.filename }}
            </li>
          </el-tooltip>
        </template>
      </ul>
    </el-scrollbar>

    <!-- 自定义右键菜单 -->
    <div
      v-if="contextMenuVisible"
      class="context-menu"
      :style="contextStyle"
    >
      <div @click="removeFile">🗑 删除文件</div>
      <div @click="downloadFile">⬇ 下载文件</div>
    </div>
  </el-aside>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Minus } from '@element-plus/icons-vue'
import axios from 'axios'

defineProps({
  files: Array
})
const emit = defineEmits(['toggle-hide', 'show-summary'])

const contextMenuVisible = ref(false)
const contextFile = ref(null)
const contextStyle = ref({ top: '0px', left: '0px' })

function handleDragStart(file) {
  if (!file) return
  event.dataTransfer.setData('text/plain', file.filename)
  event.dataTransfer.setData('file-id', file.id)
}

function openContextMenu(file, event) {
  if (!file || !file.id) return
  contextFile.value = file
  contextStyle.value = {
    top: `${event.clientY}px`,
    left: `${event.clientX}px`
  }
  contextMenuVisible.value = true
}

async function removeFile() {
  if (!contextFile.value) return
  try {
    await axios.delete(`/api/files/${contextFile.value.id}`)
    ElMessage.success(`文件「${contextFile.value.filename}」已删除`)
    contextFile.value = null
    contextMenuVisible.value = false
    emit('refresh')
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

function downloadFile() {
  if (!contextFile.value || !contextFile.value.filename) return
  const filename = contextFile.value.filename
  axios({
    url: `/api/files/download/${filename}`,
    method: 'GET',
    responseType: 'blob'
  }).then(response => {
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', filename)
    document.body.appendChild(link)
    link.click()
  }).catch(() => {
    ElMessage.error('下载失败')
  })

  contextMenuVisible.value = false
}

onMounted(() => {
  window.addEventListener('click', () => {
    contextMenuVisible.value = false
  })
})
</script>

<style scoped>
.file-list {
  background: #f4f6f8;
  border-left: 1px solid #ddd;
  padding: 16px 12px;
}

.file-list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.scroll-area {
  max-height: calc(100vh - 80px);
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}
.scroll-area::-webkit-scrollbar {
  width: 0;
  height: 0;
  display: none;
}

.file-item {
  background: #fff;
  padding: 8px;
  margin-bottom: 6px;
  border-radius: 6px;
  cursor: pointer;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.06);

  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
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