import {createRouter,createWebHistory} from "vue-router";
import {unauthorized} from "@/net/index.js";

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes:[
        {
            path:'/',
            name:'welcome',
            component:() => import('@/views/WelcomeView.vue'),
            children:[{
                path:'',
                name:'welcome-login',
                component:() => import('@/views/welcome/LoginPage.vue')
            },{
                path:"register",
                name:"welcome-register",
                component:() =>import('@/views/welcome/RegisterPage.vue')
            },{
                path:"reset",
                name:"welcome-reset",
                component:() =>import('@/views/welcome/resetPage.vue' )
            }

               ]
        }, {
            path: '/index',
            name: 'index',
            component: () => import('@/views/IndexView.vue'),
            children:[{
                path: 'user-setting',
                name: 'user-setting',
                component:() => import('@/views/settings/UserSetting.vue')
            },
            {
                path: 'privacy-setting',
                name: 'privacy-setting',
                component:() => import('@/views/settings/PrivacySetting.vue')
            }]
        }
    ]
})
// 配置路由守卫
router.beforeEach((to,from,next) =>{
    const isUnauthorized = unauthorized()
    // 组件名称前面为welcome-并且已经登录 定位到index页
    if (to.name.startsWith('welcome-') && !isUnauthorized) {
        next('/index')
    // 组件请求路径前为/index并且未登录 定位到登录页
    }else if(to.fullPath.startsWith('/index') && isUnauthorized) {
        next('/')
    }else {
        next()
    }
})
export default router;