
<template>
  <div
    class="download-box"
    @dragover.prevent
    @drop="handleDrop"
  >
    <h3>📥 下载区域</h3>

    <div v-if="downloadFiles.length > 0">
      <ul>
        <li
          v-for="file in downloadFiles"
          :key="file.id"
          class="file-name"
          @contextmenu.prevent="openContextMenu(file, $event)"
        >
          📄 {{ file.filename }}
        </li>
      </ul>
      <el-button type="primary" @click="downloadAll">批量下载</el-button>
    </div>
    <el-empty v-else description="将文件拖入此区域进行下载" />

    <div
      v-if="contextMenuVisible"
      class="context-menu"
      :style="contextStyle"
    >
      <div @click="removeFile">🗑 移除文件</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const downloadFiles = ref([])

// function handleDrop(event) {
//   const filename = event.dataTransfer.getData('text/plain')
//   const fileId = event.dataTransfer.getData('file-id')
//   if (!filename || !fileId) return

//   const exists = downloadFiles.value.some(f => f.id === fileId)
//   if (!exists) {
//     downloadFiles.value.push({ id: fileId, filename })
//   }
// }
function handleDrop(event) {
  event.preventDefault();
  
  // 尝试从不同来源获取文件信息
  const fileId = event.dataTransfer.getData('file-id');
  const filename = event.dataTransfer.getData('file-name') || 
                   event.dataTransfer.getData('text/plain');
  
  if (!fileId || !filename) return;

  // 尝试解析完整文件数据（如果有）
  const fileData = event.dataTransfer.getData('file-data');
  const file = fileData ? JSON.parse(fileData) : { id: fileId, filename };

  // 避免重复添加
  if (!downloadFiles.value.some(f => f.id === file.id)) {
    downloadFiles.value.push(file);
  }
}


function downloadAll() {
  downloadFiles.value.forEach(file => {
    const link = document.createElement('a')
    link.href = `/api/files/download/${file.filename}`
    link.download = file.filename
    document.body.appendChild(link)
    link.click()
  })
  downloadFiles.value = []
}

const contextFile = ref(null)
const contextMenuVisible = ref(false)
const contextStyle = ref({ top: '0px', left: '0px' })

function openContextMenu(file, event) {
  contextFile.value = file
  contextStyle.value = {
    top: `${event.clientY}px`,
    left: `${event.clientX}px`
  }
  contextMenuVisible.value = true
}

function removeFile() {
  if (contextFile.value) {
    downloadFiles.value = downloadFiles.value.filter(f => f.id !== contextFile.value.id)
    contextMenuVisible.value = false
  }
}

onMounted(() => {
  window.addEventListener('click', () => {
    contextMenuVisible.value = false
  })
})
</script>

<style scoped>
.download-box {
  background: #f7f9fa;
  padding: 16px;
  border: 1px dashed #bbb;
  border-radius: 8px;
  margin: 16px 0;
  min-height: 160px;
  text-align: center;
  position: relative;
}

.remove-area:hover {
  background-color: #f0f0f0;
}
.file-name {
  max-width: 100%;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  margin: 4px auto;
  padding: 4px;
  cursor: context-menu;
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
