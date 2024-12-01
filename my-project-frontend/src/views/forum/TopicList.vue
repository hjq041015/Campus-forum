<script setup>
import {computed,reactive,ref,watch} from "vue";
import LightCard from "@/components/LightCard.vue";
import {
    Calendar,
    Clock,
    CollectionTag,
    Compass,
    Document,
    Edit,
    EditPen,
    Link,
    Microphone,
    Picture
} from "@element-plus/icons-vue";
import Weather from "@/components/Weather.vue";
import {get} from "@/net/index.js";
import {ElMessage} from "element-plus";
import TopicEditor from "@/components/TopicEditor.vue";
import {useStore} from "@/store/index.js";
import axios from "axios";
import ColorDot from "@/components/ColorDot.vue";
import router from "@/router/index.js";
import TopicTag from "@/components/TopicTag.vue";


const store = useStore()
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


const topic = reactive({
    list: [],
    type: 0,
    page: 0,
    end: false,
    top: []
})

watch(() => topic.type, () => restList(), {immediate:true})


get("api/forum/top-topic",data => {
    topic.top = data
    console.log(data)
})

function updateList() {
    if (topic.end) return
    get(`api/forum/list-topic?page=${topic.page}&type=${topic.type}`, data => {
        if (data) {
            data.forEach(d => topic.list.push(d))
            topic.page++
        }
        if (!data || data.length < 10) {
            topic.end = true
        }
    })
}

function onTopicCreate() {
    editor.value = false
    restList()
}

function restList() {
    topic.page = 0
    topic.end = false
    topic.list = []
    updateList()
}

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
                 <div style="margin-top: 10px;display: flex;gap: 13px;font-size: 18px;color: grey">
                    <el-icon><Edit /></el-icon>
                    <el-icon><Document /></el-icon>
                    <el-icon><Compass /></el-icon>
                    <el-icon><Picture /></el-icon>
                    <el-icon><Microphone /></el-icon>
                </div>
            </light-card>
                <light-card style="margin-top: 10px; display: flex;flex-direction: column;gap: 10px">
                    <div v-for="item in topic.top" class="top-topic">
                        <el-tag type="info" size="small">置顶</el-tag>
                        <div>{{item.title}}</div>
                        <div>{{new Date(item.time).toLocaleString()}}</div>
                    </div>
                </light-card>
                <light-card style="margin-top: 10px; display: flex; gap:7px">
                    <div :class="`type-select-card ${topic.type === item.id ? 'active' : ''}` "
                        v-for="item in store.forum.types"
                        @click="topic.type = item.id">
                        <color-dot :color="item.color"/>
                        <span style="margin-left: 5px">{{item.name}}</span>
                    </div>
                </light-card>
                <transition>
                    <div v-if="topic.list.length">
                        <div v-infinite-scroll="updateList" style="margin-top: 10px; display: flex; flex-direction: column;gap: 10px">
                   <light-card  @click="router.push('/index/topic-detail/'+ item.id)" v-for="item in topic.list" class="topic-card">
                     <div style="display: flex">
                         <div>
                                    <el-avatar :size="30" :src="`${axios.defaults.baseURL}/image${item.avatar}`"/>
                         </div>
                         <div style="margin-left: 7px;transform: translateY(-2px)">
                             <div style="font-size: 13px;font-weight: bold">{{item.username}}</div>
                             <div style="font-size: 12px;color: grey">
                                 <el-icon><Clock/></el-icon>
                                 <div style="margin-left: 2px;display: inline-block;transform: translateY(-2px)">
                                     {{new Date(item.time).toLocaleString()}}

                                 </div>
                             </div>
                         </div>
 </div>
                       <div style="margin-top: 5px">
                            <topic-tag :type="item.type"/>
                          <span  style="font-weight: bold; margin-left: 7px">{{item.title}}</span>
                       </div>
                            <div class="topic-content">{{item.text}}</div>
                            <div style="display: grid; grid-template-columns: repeat(3,1fr);grid-gap: 10px">
                                <el-image class="topic-image" v-for="img in item.images" :src="img" fit="cover">

                                </el-image>
                            </div>
                   </light-card>
                </div>
                    </div>
                </transition>


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
            <topic-editor :show="editor" @success="onTopicCreate()"  @close="editor = false"/>
</div>
</template>

<style lang="less" scoped>
.top-topic {
    display: flex;

    div:first-of-type {
        font-size: 14px;
        margin-left: 14px;
        font-weight: bold;
        opacity: 0.8;
        transition: color .3s;

        &:hover {
            color:grey;
        }
    }

    div:nth-of-type(2) {
        flex: 1;
        color: grey;
        font-size: 13px;
        text-align: right;
    }

    &:hover {
        cursor: pointer;
    }
}
.type-select-card {
    background-color: #f5f5f5;
    padding: 2px 7px;
    font-size: 14px;
    border-radius: 3px;
    box-sizing: border-box;
    transition: background-color .3s;

    &.active {
        border: solid 1px #ead4c4;
    }

    &:hover {
        cursor: pointer;
        background-color: #dadada;
    }
}
.topic-card {
    padding: 15px;
    transition: scale .3s;

    &:hover {
        scale: 1.01;
        cursor: pointer;
    }
.topic-content {
    font-size: 13px;
    color: gray;
    margin: 5px 0;
    display: -webkit-box;
    -webkit-box-orient: vertical;
    -webkit-line-clamp: 3;
    overflow: hidden;
    text-overflow: ellipsis;
}

.topic-image {
    width: 100%;
    height: 100%;
    max-height: 110px;
    border-radius: 5px;
}
}
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