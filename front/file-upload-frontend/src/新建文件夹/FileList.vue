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
        <template v-for="file in files" :key="file.id">
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
import axios from 'axios'


const files = ref([])
const contextFile = ref(null)
const contextMenuVisible = ref(false)
const contextStyle = ref({ top: '0px', left: '0px' })
const emit = defineEmits(['toggle-hide', 'refresh'])
const file = ref(null)

// ✅ 加载文件列表
async function fetchFiles() {
  try {
    const res = await axios.get('/api/files')
    files.value = res.data
  } catch (e) {
    ElMessage.error('获取文件列表失败')
  }
}

// ✅ 拖拽
function handleDragStart(file) {
  event.dataTransfer.setData('text/plain', file.filename)
  event.dataTransfer.setData('file-id', file.id)
}

function handleFileChange(e) {
  file.value = e.target.files[0]
}

async function uploadFile() {
  if (!file.value) return ElMessage.warning('请选择文件')

  const formData = new FormData()
  formData.append('file', file.value)

  try {
    await axios.post('/api/files/upload', formData)
    ElMessage.success('上传成功')
    file.value = null
    emit('refresh')  // ✅ 通知 App.vue 刷新文件列表
  } catch (e) {
    ElMessage.error('上传失败')
  }
}
// ================= 右键菜单逻辑 ===================


function openContextMenu(file, event) {
  event.preventDefault()
  contextFile.value = file
  contextStyle.value = {
    top: `${event.clientY}px`,
    left: `${event.clientX}px`
  }
  contextMenuVisible.value = true
}

// ✅ 删除文件
async function removeFile() {
  if (!contextFile.value) return
  try {
    await axios.delete(`/api/files/${contextFile.value.id}`)
    ElMessage.success(`已删除 ${contextFile.value.filename}`)
    contextFile.value = null
    contextMenuVisible.value = false
    fetchFiles() // 删除后刷新列表
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

// ✅ 自动隐藏菜单
onMounted(() => {
  window.addEventListener('click', () => {
    contextMenuVisible.value = false
  })
  fetchFiles()
})
defineExpose({
  fetchFiles
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
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;

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