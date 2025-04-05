<template>
  <div>
    <h2>文件上传</h2>
    <input type="file" @change="handleFileUpload" />
    <button @click="uploadFile">上传</button>

    <h2>文件下载</h2>
    <input v-model="downloadFilename" placeholder="输入文件名" />
    <button @click="downloadFile">下载</button>
  </div>
</template>

<script>
import axios from 'axios';

export default {
  data() {
    return {
      selectedFile: null,
      downloadFilename: ''
    };
  },
  methods: {
    handleFileUpload(event) {
      this.selectedFile = event.target.files[0];
    },
    uploadFile() {
      const formData = new FormData();
      formData.append("file", this.selectedFile);
      axios.post("/api/files/upload", formData)
        .then(res => alert("上传成功: " + res.data))
        .catch(err => alert("上传失败"));
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