import { createApp } from 'vue'
import App from './App.vue'
import './assets/styles.css'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// ✅ 新增：引入 router
import router from './router'

// createApp(App).mount('#app')
const app = createApp(App)
app.use(ElementPlus)

// ✅ 新增：使用 router
app.use(router)

app.mount('#app')