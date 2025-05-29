import { createRouter, createWebHistory } from 'vue-router'

// ✅ 页面组件导入（根据你项目实际路径修改）
import HomeView from '../components/HomeView.vue'
import SwitchAccountView from '../components/SwitchAccountView.vue'
import ChangePasswordView from '../components/ChangePasswordView.vue'
import LoginView from '../components/LoginView.vue'

const routes = [
  {
    path: '/',
    component: HomeView
  },
  {
    path: '/switch-account',
    component: SwitchAccountView
  },
  {
    path: '/change-password',
    component: ChangePasswordView
  },
  { 
    path: '/login', 
    component: LoginView 
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
