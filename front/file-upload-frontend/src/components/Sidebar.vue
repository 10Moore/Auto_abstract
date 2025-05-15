<template>
  <el-aside width="260px" class="sidebar">
    <div class="folder-header">
      <h3>📁 我的文件夹</h3>
      <el-button 
        :icon="Plus" 
        size="small" 
        circle 
        @click="addFolder" 
      />
    </div>

    <el-scrollbar class="folder-scrollbar">
      <div
        v-for="folder in folders"
        :key="folder.id"
        class="folder-item"
        @dragover.prevent="handleDragOver($event, folder.id)"
        @dragleave="handleDragLeave"
        @drop="handleDropToFolder(folder.id, $event)"
        :class="{ 'drag-over': dragOverFolderId === folder.id }"
      >
        <div class="folder-title" @click="toggleFolder(folder)">
          <!-- 可编辑的文件夹名称 -->
          <template v-if="folder.editing">
            <el-input
              v-model="folder.editName"
              size="small"
              @blur="saveFolderName(folder)"
              @keyup.enter="saveFolderName(folder)"
              autofocus
              @click.stop
            />
          </template>
          <template v-else>
            <span 
              class="folder-name" 
              :title="folder.name"
              @dblclick.stop="startEditing(folder)"
            >
              {{ folder.name }}
            </span>

          </template>
          
          <el-button
            :icon="Delete"
            size="small"
            circle
            @click.stop="deleteFolder(folder.id)"
          />
        </div>

        <div v-show="folder.expanded" class="folder-content">
          <div class="folder-attribute">
            <span class="attribute-label">主题：</span>
            <el-input
              v-model="folder.attribute"
              size="small"
              @blur="updateFolder(folder)"
              placeholder="输入主题"
            />
          </div>

          <div class="file-list">
            <div v-if="loadingFiles[folder.id]" class="loading-files">
              <el-icon class="is-loading"><Loading /></el-icon>
              加载中...
            </div>
            
            <template v-else>
              <div
                v-for="file in folderFiles[folder.id]"
                :key="file.id"
                class="file-item"
                draggable="true"
                @dragstart="handleDragStart(file, folder.id)"
                @click="$emit('show-summary', file.id)"
                @contextmenu.prevent="openContextMenu($event, file, folder)"
              >
                <span class="file-name" :title="file.filename">📄 {{ file.filename }}</span>
                <div class="relevance-stars" v-if="file.relevance">
                  <el-icon v-for="i in 5" :key="i" :class="{ 'active-star': i <= file.relevance }">
                    <Star />
                  </el-icon>
                </div>
                <el-icon 
                  class="remove-file-icon" 
                  @click.stop="removeFileFromFolder(file.id, folder.id)"
                >
                  <Close />
                </el-icon>
              </div>
              <div v-if="!folderFiles[folder.id]?.length" class="empty-folder">
                （暂无文件）
              </div>
            </template>
          </div>
        </div>
      </div>
    </el-scrollbar>

    <!-- 右键菜单 -->
    <div
      v-if="contextMenu.visible"
      class="context-menu"
      :style="{
        left: `${contextMenu.x}px`,
        top: `${contextMenu.y}px`
      }"
    >
      <div class="menu-item" @click="downloadContextFile">
        <el-icon><Download /></el-icon>
        <span>下载文件</span>
      </div>
      <div class="menu-item" @click="removeContextFile">
        <el-icon><Delete /></el-icon>
        <span>移除文件</span>
      </div>
    </div>
  </el-aside>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { 
  Plus, Delete, Close, Download, Loading 
} from '@element-plus/icons-vue'
import { ElMessage, ElLoading } from 'element-plus'
import axios from 'axios'
import { Star } from '@element-plus/icons-vue'

// 配置后端API地址（与您的Controller路径一致）
const API_BASE = 'http://localhost:8080/api'

const emit = defineEmits(['refresh'])

// 状态管理
const folders = ref([])
const folderFiles = reactive({})
const loadingFiles = reactive({})
const dragOverFolderId = ref(null)
const contextMenu = reactive({
  visible: false,
  x: 0,
  y: 0,
  file: null,
  folder: null
})

// 初始化加载
onMounted(() => {
  loadFolders()
})

// 加载文件夹列表
// 在 loadFolders() 方法中修改数据加载逻辑
async function loadFolders() {
  try {
    const res = await axios.get(`${API_BASE}/folders`);
    folders.value = res.data.map(f => ({ 
      ...f,
      expanded: false,
      editing: false,    // 新增编辑状态
      editName: ''       // 新增编辑暂存值
    }));
  } catch (error) {
    handleApiError(error, '加载文件夹');
  }
}

// 进入编辑模式
function startEditing(folder) {
  folder.editing = true;
  folder.editName = folder.name;
}

// 保存修改
async function saveFolderName(folder) {
  if (!folder.editName?.trim()) {
    ElMessage.warning("文件夹名不能为空");
    folder.editing = false;
    return;
  }

  try {
    const res = await axios.put(
      `${API_BASE}/folders/${folder.id}`,
      { name: folder.editName },
      { headers: { "Content-Type": "application/json" } }
    );
    
    folder.name = folder.editName;
    folder.editing = false;
    ElMessage.success('名称修改成功');
  } catch (error) {
    console.error('修改失败:', error.response?.data);
    ElMessage.error(`修改失败: ${error.response?.data?.message || error.message}`);
    folder.editing = false;
  }
}


// 添加文件夹（与您的后端createFolder匹配）
async function addFolder() {
  try {
    const res = await axios.post(`${API_BASE}/folders`, {
      name: "新建文件夹",
      attribute: "未分类",
      createTime: new Date().toISOString() // 添加时间戳
    }, {
      headers: { 'Content-Type': 'application/json' }
    });

    // 确保包含所有必要字段
    folders.value.push({
      id: res.data.id,
      name: res.data.name,
      attribute: res.data.attribute,
      createTime: res.data.createTime,
      expanded: true,
      editing: false,
      editName: ''
    });
    ElMessage.success('文件夹添加成功');
  } catch (error) {
    console.error('添加失败:', {
      request: error.config,
      response: error.response?.data
    });
    ElMessage.error(`添加失败: ${error.response?.data?.message || error.message}`);
  }
}


// 删除文件夹（与您的后端deleteFolder匹配）
async function deleteFolder(folderId) {
  try {
    await axios.delete(`${API_BASE}/folders/${folderId}`)
    folders.value = folders.value.filter(f => f.id !== folderId)
    delete folderFiles[folderId]
    ElMessage.success('文件夹已删除')
    emit('refresh')
  } catch (error) {
    console.error('删除失败详情:', error.response?.data)
    ElMessage.error(`删除失败: ${error.response?.data?.message || error.message}`)
  }
}

// 统一错误处理
function handleApiError(error, action = '操作') {
  console.error(`${action}失败:`, {
    status: error.response?.status,
    data: error.response?.data,
    message: error.message
  })
  
  let errMsg = error.response?.data?.message || 
              error.response?.statusText || 
              error.message
  
  // 处理Spring Boot的默认错误格式
  if (error.response?.data?.error) {
    errMsg = `${error.response.data.error}: ${error.response.data.message}`
  }
  
  ElMessage.error(`${action}失败: ${errMsg}`)
}


// 更新文件夹信息
async function updateFolder(folder) {
  try {
    const payload = {
      name: String(folder.name || ''),
      attribute: String(folder.attribute || '')
    };

    console.log('Sending payload:', payload);

    const response = await axios.put(
      `${API_BASE}/folders/${folder.id}`,
      payload,
      {
        headers: {
          'Content-Type': 'application/json',
          'Accept': 'application/json'
        },
        validateStatus: status => status < 500 // 允许400状态码继续处理
      }
    );

    if (response.status === 400) {
      throw new Error(response.data?.message || 'Invalid request');
    }

    console.log('Update successful:', response.data);
    ElMessage.success('更新成功');
    return response.data;
  } catch (error) {
    console.error('Update error details:', {
      message: error.message,
      response: error.response?.data,
      stack: error.stack
    });

    let userMessage = error.message;
    if (error.response?.data) {
      userMessage = error.response.data.error 
        ? `${error.response.data.error}: ${error.response.data.message}`
        : JSON.stringify(error.response.data);
    }

    ElMessage.error(`更新失败: ${userMessage}`);
    throw error; // 继续向上抛出
  }
}

// 加载文件夹内文件
async function loadFolderFiles(folderId) {
  loadingFiles[folderId] = true
  try {
    const res = await axios.get(`${API_BASE}/files/folder/${folderId}`)
    folderFiles[folderId] = res.data
  } catch (error) {
    ElMessage.error(`加载文件失败: ${error.message}`)
    folderFiles[folderId] = []
  } finally {
    loadingFiles[folderId] = false
  }
}

// 切换文件夹展开状态
function toggleFolder(folder) {
  folder.expanded = !folder.expanded
  if (folder.expanded && !folderFiles[folder.id]) {
    loadFolderFiles(folder.id)
  }
}

// 拖拽处理
// 在拖拽开始时设置数据
function handleDragStart(file, folderId) {
  event.dataTransfer.setData('file-id', file.id);
  event.dataTransfer.setData('file-name', file.filename);
  event.dataTransfer.setData('file-data', JSON.stringify(file)); // 完整文件数据
  event.dataTransfer.effectAllowed = 'copy'; // 明确允许复制操作
}

function handleDragOver(event, folderId) {
  event.preventDefault()
  dragOverFolderId.value = folderId
}

function handleDragLeave() {
  dragOverFolderId.value = null
}

async function handleDropToFolder(folderId, event) {
  event.preventDefault();
  dragOverFolderId.value = null;

  const fileId = event.dataTransfer.getData('file-id');
  if (!fileId) return;

  const loadingInstance = ElLoading.service({
    lock: true,
    text: '正在关联文件...'
  });

  try {
    // 1. 先计算相关度
    console.log('正在请求相关度计算，fileId:', fileId, 'folderId:', folderId); // 添加日志
    const relevanceRes = await axios.get(`${API_BASE}/files/${fileId}/relevance/${folderId}`);
    console.log('相关度返回结果:', relevanceRes.data); // 添加日志


    // 修改为已存在的后端接口路径
    const res = await axios.post(
      `${API_BASE}/files/${fileId}/copy-to-folder/${folderId}`
    );
    
    await loadFolderFiles(folderId);

    console.log('文件夹文件加载完成:', folderFiles[folderId]); // 添加日志
    
    if (folderFiles[folderId]) {
      const file = folderFiles[folderId].find(f => f.id == fileId);
      if (file) {
        file.relevance = relevanceRes.data;
        console.log('文件相关度已设置:', file); // 添加日志
      }
    }

    ElMessage.success(res.data || '文件关联成功');
    emit('refresh');
  } catch (error) {
    console.error('关联失败详情:', {
      request: error.config,
      response: error.response?.data
    });
    ElMessage.error(`关联失败: ${error.response?.data || error.message}`);
  } finally {
    loadingInstance.close();
  }
}



// 从文件夹移除文件
async function removeFileFromFolder(fileId, folderId) {
  try {
    await axios.put(`${API_BASE}/files/${fileId}/remove-from-folder/${folderId}`)
    folderFiles[folderId] = folderFiles[folderId].filter(f => f.id !== fileId)
    ElMessage.success('文件已移除')
    emit('refresh')
  } catch (error) {
    console.error('移除失败详情:', error.response?.data)
    ElMessage.error(`移除失败: ${error.response?.data?.message || error.message}`)
  }
}
// 右键菜单
function openContextMenu(event, file, folder) {
  event.preventDefault()
  contextMenu.visible = true
  contextMenu.x = event.clientX
  contextMenu.y = event.clientY
  contextMenu.file = file
  contextMenu.folder = folder
}

function downloadContextFile() {
  if (!contextMenu.file) return
  const link = document.createElement('a')
  link.href = `${API_BASE}/files/download/${contextMenu.file.filename}`
  link.download = contextMenu.file.filename
  link.click()
  closeContextMenu()
}

function removeContextFile() {
  if (!contextMenu.file || !contextMenu.folder) return
  removeFileFromFolder(contextMenu.file.id, contextMenu.folder.id)
  closeContextMenu()
}

function closeContextMenu() {
  contextMenu.visible = false
}

// 点击其他地方关闭菜单
window.addEventListener('click', closeContextMenu)
</script>

<style scoped>
.sidebar {
  height: 100%;
  background-color: #f8f9fa;
  border-right: 1px solid #e0e0e0;
  padding: 12px;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

.folder-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 4px;
  margin-bottom: 12px;
}

.folder-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 500;
}

.folder-scrollbar {
  flex: 1;
  height: 0;
}

.folder-item {
  margin-bottom: 8px;
  background: white;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.folder-item.drag-over {
  border: 2px dashed #409eff;
  background-color: #f0f7ff;
}

.folder-title {
  padding: 8px 12px;
  display: flex;
  align-items: center;
  cursor: pointer;
  user-select: none;
}

.folder-title:hover {
  background-color: #f5f5f5;
}

.folder-name {
  flex: 1;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.folder-content {
  padding: 0 12px 12px;
}

.folder-attribute {
  margin-bottom: 8px;
}

.attribute-label {
  font-size: 13px;
  color: #666;
}

.file-list {
  max-height: 300px;
  overflow-y: auto;
}

.file-item {
  display: flex;
  align-items: center;
  padding: 6px 8px;
  margin-bottom: 4px;
  background: #f5f7fa;
  border-radius: 4px;
  cursor: move;
}

.file-item:hover {
  background: #e4e7ed;
}

.file-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
}

.remove-file-icon {
  color: #c0c4cc;
  font-size: 14px;
  margin-left: 4px;
  cursor: pointer;
}

.remove-file-icon:hover {
  color: #f56c6c;
}

.empty-folder {
  color: #999;
  font-size: 13px;
  text-align: center;
  padding: 8px;
  font-style: italic;
}

.loading-files {
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
  padding: 8px;
  font-size: 13px;
}

.context-menu {
  position: fixed;
  background: white;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  z-index: 2000;
  min-width: 120px;
}

.menu-item {
  padding: 8px 12px;
  display: flex;
  align-items: center;
  cursor: pointer;
}

.menu-item:hover {
  background-color: #f5f5f5;
}

.menu-item .el-icon {
  margin-right: 6px;
  font-size: 14px;
}
/* 添加星星样式 */
.relevance-stars {
  display: flex;
  margin-left: 8px;
}

.relevance-stars .el-icon {
  color: #c0c4cc;
  font-size: 14px;
  margin-right: 2px;
}

.relevance-stars .active-star {
  color: #f7ba2a;
}
</style>