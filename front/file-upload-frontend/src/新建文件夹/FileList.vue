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
        <li
          v-for="file in files"
          :key="file"
          class="file-item"
          draggable="true"
          @dragstart="handleDragStart(file)"
          @contextmenu.prevent="openContextMenu(file, $event)"
        >
          📄 {{ file }}
        </li>
      </ul>
    </el-scrollbar>

    <!-- 自定义右键菜单 -->
    <div
      v-if="contextMenuVisible"
      class="context-menu"
      :style="contextStyle"
    >
      <div @click="removeFile">🗑 删除文件</div>
    </div>
  </el-aside>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Minus } from '@element-plus/icons-vue'

defineEmits(['toggle-hide'])

const files = ref([
  '文件1.pdf',
  '文档2.docx',
  '图片3.png'
])

function handleDragStart(file) {
  event.dataTransfer.setData('text/plain', file)
}

// ================= 右键菜单逻辑 ===================
const contextMenuVisible = ref(false)
const contextFile = ref(null)
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

function removeFile() {
  if (contextFile.value) {
    files.value = files.value.filter(f => f !== contextFile.value)
    ElMessage.success(`文件「${contextFile.value}」已删除`)
    contextFile.value = null
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
  scrollbar-width: none;             /* Firefox */
  -ms-overflow-style: none;          /* IE 10+ */
}
.scroll-area::-webkit-scrollbar {
  width: 0;
  height: 0;
  display: none;                     /* Chrome/Safari */
}

.file-item {
  background: #fff;
  padding: 8px;
  margin-bottom: 6px;
  border-radius: 6px;
  cursor: pointer;
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