<template>
  <div class="layout">
    <!-- 左侧边栏 -->
    <Sidebar ref="sidebarRef" />

    <!-- 中间主区域：上传文件 + 拖拽到空白处移除 -->
    <div
      class="main"
      @dragover.prevent
      @drop="handleDropToMain"
    >
      <FileUploader @refresh="refreshFiles" />

      <!-- 展开右侧边栏按钮，仅在隐藏时显示 -->
      <div v-if="!showRightSidebar" class="show-right-bar" @click="toggleRightSidebar">
        👉 展开文件栏
      </div>
    </div>

    <!-- 右侧边栏（可隐藏） -->
    <FileList ref="fileListRef" v-if="showRightSidebar" @toggle-hide="toggleRightSidebar" />
  </div>
</template>




<script>
import Sidebar from './components/Sidebar.vue'
import FileUploader from './components/FileUploader.vue'
import FileList from './components/FileList.vue'

export default {
  components: {
    Sidebar,
    FileUploader,
    FileList
  },
  data() {
    return {
      showRightSidebar: true
    }
  },
  methods: {
      toggleRightSidebar() {
      this.showRightSidebar = !this.showRightSidebar
    },
    handleDropToMain(event) {
      const file = event.dataTransfer.getData('text/plain')
      const fromFolderId = event.dataTransfer.getData('from-folder')
      if (file && fromFolderId) {
        this.$refs.sidebarRef.removeFileFromFolder(Number(fromFolderId), file)
      }
    },
    refreshFiles() {
      this.$refs.fileListRef?.fetchFiles?.()
    }
  }
}
</script>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
}
.main {
  flex: 1;
  padding: 16px;
  background-color: #fff;
  overflow: auto;
  min-width: 400px;
  position: relative;
}
.show-right-bar {
  position: absolute;
  top: 20px;
  right: 0;
  background-color: #e2e2e2;
  padding: 8px 12px;
  border-radius: 6px 0 0 6px;
  cursor: pointer;
  color: #333;
  font-weight: bold;
}
.show-right-bar:hover {
  background-color: #ccc;
}
</style>