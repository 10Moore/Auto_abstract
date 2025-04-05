<template>
  <el-aside width="260px" class="sidebar">
    <div class="sidebar-header">
      <h3>📁 我的文件夹</h3>
      <el-button
        type="primary"
        :icon="Plus"
        size="small"
        circle
        @click="addFolder"
      />
    </div>

    <el-scrollbar height="calc(100vh - 100px)">
      <el-collapse v-model="activeFolders">
        <el-collapse-item
          v-for="folder in folders"
          :key="folder.id"
          :name="folder.id"
          @drop="handleDrop($event, folder)"
          @dragover.prevent
        >
          <template #title>
            <div class="folder-header">
              <el-input
                v-model="folder.name"
                size="small"
                placeholder="文件夹名称"
                @change="updateFolder(folder)"
                class="folder-name-input"
              />
              <el-button
                :icon="Delete"
                size="small"
                circle
                type="danger"
                @click.stop="deleteFolder(folder.id)"
              />
            </div>
          </template>

          <div class="folder-body">
            <span class="theme-label">主题：</span>
            <el-input
              v-model="folder.attribute"
              size="small"
              placeholder="输入主题"
              @change="updateFolder(folder)"
              class="theme-input"
            />
          </div>

          <!-- 显示归属的文件 -->
          <ul class="folder-files" v-if="folder.files.length">
            <li
              v-for="file in folder.files"
              :key="file"
              class="folder-file"
              draggable="true"
              @dragstart="handleFileDragStart(file, folder)"
              @contextmenu.prevent="openContextMenu(file, folder, $event)"
            >
              📄 {{ file }}
            </li>
          </ul>
        </el-collapse-item>
      </el-collapse>
    </el-scrollbar>

    <!-- 右键菜单 -->
    <el-dropdown
      ref="contextMenu"
      trigger="manual"
      :visible="menuVisible"
      :teleported="false"
      @command="handleCommand"
    >
      <template #dropdown>
        <el-dropdown-menu>
          <el-dropdown-item command="remove">从文件夹移除</el-dropdown-item>
        </el-dropdown-menu>
      </template>
    </el-dropdown>
  </el-aside>

  <!-- 右键菜单 -->
  <div
    v-if="contextMenuVisible"
    class="context-menu"
    :style="contextStyle"
  >
    <div @click="handleContextRemove">🗑 移除文件</div>
  </div>

</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Delete, Plus } from '@element-plus/icons-vue'

let folderIdCounter = 1000

const folders = ref([
  { id: 1, name: '默认文件夹', attribute: '', files: [] }
])
const activeFolders = ref(['1'])

// 拖拽时记录目标
function handleDrop(event, folder) {
  const fileName = event.dataTransfer.getData('text/plain')
  if (fileName && !folder.files.includes(fileName)) {
    folder.files.push(fileName)
    ElMessage.success(`文件「${fileName}」添加到「${folder.name}」`)
  }
}

// 文件拖出时记录起点（可做高亮等扩展）
function handleFileDragStart(file, folder) {
  event.dataTransfer.setData('text/plain', file)
  event.dataTransfer.setData('from-folder', folder.id)
}

// ---------- 右键菜单逻辑 ----------
const contextMenu = ref(null)
const menuVisible = ref(false)
const selectedFile = ref(null)
const selectedFolder = ref(null)
const menuPosition = ref({ x: 0, y: 0 })


function handleCommand(command) {
  menuVisible.value = false
  if (command === 'remove' && selectedFile.value && selectedFolder.value) {
    const folder = selectedFolder.value
    folder.files = folder.files.filter(f => f !== selectedFile.value)
    ElMessage.success(`文件「${selectedFile.value}」已移除`)
  }
}

// 其他函数不变
function addFolder() {
  const newFolder = {
    id: folderIdCounter++,
    name: '新建文件夹',
    attribute: '',
    files: []
  }
  folders.value.push(newFolder)
  activeFolders.value.push(newFolder.id.toString())
}
function deleteFolder(id) {
  folders.value = folders.value.filter(f => f.id !== id)
  activeFolders.value = activeFolders.value.filter(i => i !== id.toString())
  ElMessage.success('已删除文件夹')
}
function updateFolder(folder) {
  ElMessage.success(`更新成功: ${folder.name}`)
}


// 右键菜单相关
const contextMenuVisible = ref(false)
const contextFile = ref(null)
const contextFolder = ref(null)
const contextStyle = ref({ top: '0px', left: '0px' })

onMounted(() => {
  window.addEventListener('click', () => {
    contextMenuVisible.value = false
  })
})

function openContextMenu(file, folder, event) {
  event.preventDefault()
  contextFile.value = file
  contextFolder.value = folder
  contextStyle.value = {
    top: `${event.clientY}px`,
    left: `${event.clientX}px`
  }
  contextMenuVisible.value = true
}

function handleContextRemove() {
  if (!contextFolder.value || !contextFile.value) return
  contextFolder.value.files = contextFolder.value.files.filter(f => f !== contextFile.value)
  ElMessage.success(`文件「${contextFile.value}」已移除`)
  contextMenuVisible.value = false
}

// 暴露方法供 App.vue 调用
defineExpose({
  removeFileFromFolder(folderId, fileName) {
    const folder = folders.value.find(f => f.id === folderId)
    if (folder) {
      folder.files = folder.files.filter(f => f !== fileName)
      ElMessage.success(`文件「${fileName}」已从「${folder.name}」中移除`)
    }
  }
})
</script>

<style scoped>
.sidebar {
  background-color: #f4f6f8;
  border-right: 1px solid #ddd;
  padding: 16px 12px;
}
.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}
.folder-header {
  display: flex;
  align-items: center;
  gap: 8px;
}
.folder-body {
  display: flex;
  align-items: center;
  margin-top: 10px;
  padding-left: 4px;
  gap: 8px;
}
.folder-name-input {
  flex: 1;
}
.theme-label {
  font-size: 13px;
  color: #555;
  white-space: nowrap;
}
.theme-input {
  width: 100px;
}
.folder-files {
  padding: 4px 8px;
  margin-top: 8px;
}
.folder-file {
  font-size: 14px;
  color: #333;
  padding: 4px 6px;
  border-radius: 4px;
  cursor: grab;
}
.folder-file:hover {
  background-color: #f0f0f0;
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