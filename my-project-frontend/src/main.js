
import { createApp } from 'vue'
import App from './App.vue'
import router from "@/router/index.js";
import axios from "axios";
import {createPinia} from "pinia";
import '@/assets/quills.css'

axios.defaults.baseURL = 'http://localhost:8080'
const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
