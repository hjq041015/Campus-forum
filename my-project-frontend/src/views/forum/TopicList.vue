<script setup>
import {computed,reactive,ref} from "vue";
import LightCard from "@/components/LightCard.vue";
import {Calendar, CollectionTag, EditPen, Link} from "@element-plus/icons-vue";
import Weather from "@/components/Weather.vue";
import {get} from "@/net/index.js";
import {ElMessage} from "element-plus";
import TopicEditor from "@/components/TopicEditor.vue";


const today = computed(() => {
    const date = new Date()
    return `${date.getFullYear()} 年 ${date.getMonth() + 1} 月 ${date.getDate()} 日`
})

const weather = reactive({
    location: {},
    now: {},
    hourly: [],
    success : false
})

navigator.geolocation.getCurrentPosition(position => {
    const longitude = position.coords.longitude
    const latitude = position.coords.latitude
    get(`/api/forum/weather?longitude=${longitude}&latitude=${latitude}`, data => {
        Object.assign(weather, data)
        weather.success = true
    })
}, error => {
    console.info(error)
    ElMessage.warning('位置信息获取超时，请检测网络设置')
    get(`/api/forum/weather?longitude=121.3912291&latitude=31.2513263`, data => {
        Object.assign(weather, data)
        weather.success = true
    })
}, {
    timeout: 3000,
    enableHighAccuracy: true
})

const editor = ref(false)


</script>

<template>
<div style="display:flex;gap: 20px; margin: 20px auto; max-width:900px;">
            <div style="flex: 1">
            <light-card>
                <div @click="editor=true"  class="create-topic">
                    <el-icon><EditPen/></el-icon>
                    点击发表主题...
                </div>
            </light-card>
                <light-card style="margin-top: 10px; height: 30px">

                </light-card>
                <div style="margin-top: 10px; display: flex; flex-direction: column;gap: 10px">
                   <light-card style="height: 150px;" v-for="item in 10">

                   </light-card>
                </div>

            <light-card style="margin-top: 10px;display: flex;gap: 7px">

            </light-card>
            </div>
            <div style="width: 280px" >
                <div style="position: sticky;top: 20px">
                <light-card>
                    <div style="font-weight: bold ">
                        <el-icon><CollectionTag/></el-icon>
                        论坛公告
                    </div>
                    <el-divider style="margin: 10px 0"/>
                    <div style="font-size: 14px; margin: 10px; color: gray">
                        《原神》是由米哈游开发的动作角色扮演游戏，于2020年9月登陆Microsoft Windows、Android、iOS、PlayStation 4，2021年4月在PlayStation 5上线。游戏具有动漫风格的开放世界环境，采用免费游玩和内购制抽卡的游戏模式。
                    </div>
                </light-card>
                <light-card style="margin-top: 10px">
                    <div style="font-weight: bold">
                        <el-icon><Calendar/></el-icon>
                        天气信息
                    </div>
                    <el-divider style="margin: 10px 0"/>
                    <weather :data="weather"/>
                </light-card>
                <light-card style="margin-top: 10px">
                    <div class="info-test">
                        <div>当前日期</div>
                        <div>{{today}}</div>
                    </div>
                    <div class="info-test">
                        <div>当前ip地址</div>
                        <div>127.0.0.1</div>
                    </div>
                </light-card>
                <div style="font-size: 14px; margin-top: 10px; color: gray">
                    <el-icon><Link/></el-icon>
                    友情链接
                     <el-divider style="margin: 10px 0"/>
                </div>
                <div style="display: grid; grid-template-columns: repeat(2,1fr); grid-gap: 10px; margin-top: 10px">
                     <div class="friend-link">
                        <el-image style="height: 100%" src="https://element-plus.org/images/js-design-banner.jpg"/>
                    </div>

                </div>
            </div>
            </div>
            <topic-editor :show="editor" @success="editor = false" @close="editor = false"/>
</div>
</template>

<style lang="less" scoped>
.info-test {
    display: flex;
    justify-content: space-between;
    color: grey;
    font-size: 14px;
}
.create-topic {
    background-color: #efefef;
    border-radius: 5px;
    height: 40px;
    color: grey;
    font-size: 14px;
    line-height: 40px;
    padding: 0 10px;

    &:hover {
        cursor: pointer;
    }
}
</style>