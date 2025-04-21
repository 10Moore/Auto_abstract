<template>
  <div>
    <h2>文件上传</h2>
    <input type="file" multiple @change="handleFileUpload" />
    <button @click="uploadFile">上传</button>

    <h2>文件下载</h2>
    <input v-model="downloadFilename" placeholder="输入文件名" />
    <button @click="downloadFile">下载</button>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  emits: ['refresh'], // ✅ 声明事件
  data() {
    return {
      selectedFiles: [],
      downloadFilename: ''
    };
  },
  methods: {
    handleFileUpload(event) {
      this.selectedFiles = Array.from(event.target.files);
    },
    uploadFile() {
      if (this.selectedFiles.length === 0) {
        alert("请先选择文件")
        return
      }

      const uploadPromises = this.selectedFiles.map(file => {
        const formData = new FormData()
        formData.append("file", file)
        return axios.post("/api/files/upload", formData)
      })

      Promise.all(uploadPromises)
        .then(() => {
          alert("全部上传成功")
          this.$emit('refresh') // ✅ 通知父组件刷新
          this.selectedFiles = [] // 清空
        })
        .catch(() => {
          alert("部分上传失败")
        })
    },
    downloadFile() {
      axios({
        url: `/api/files/download/${this.downloadFilename}`,
        method: 'GET',
        responseType: 'blob'
      }).then(response => {
        const url = window.URL.createObjectURL(new Blob([response.data]));
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', this.downloadFilename);
        document.body.appendChild(link);
        link.click();
      });
    }
  }
};

</script>

<style scoped>
@import "../assets/styles.css";
</style>