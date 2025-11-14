import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './style.css'
// 已移除Font Awesome，使用emoji表情代替
import { provideTreeholeStore } from './store/treeholeStore'

const app = createApp(App);

app.use(router);

// 提供treeholeStore给整个应用
provideTreeholeStore(app);

app.mount('#app');