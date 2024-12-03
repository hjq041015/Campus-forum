<script setup>
import LightCard from "@/components/LightCard.vue";
import router from "@/router/index.js";
import TopicTag from "@/components/TopicTag.vue";
import {ref} from "vue";
import {get} from "@/net/index.js";
import {ElMessage} from "element-plus";
defineProps({
    show:Boolean
})

const emit = defineEmits(['close'])
const list = ref([])
function  init() {
    get("api/forum/list-collect ",data => list.value = data)
}

function deleteCollection(tid, index) {
    get(`api/forum/interact?tid=${tid}&type=collect&state=false`,() => {
        ElMessage.success('取消收藏成功')
        list.value.splice(index,1)
    })
}
</script>

<template>
<el-drawer :model-value="show" @close="emit('close')" @open="init" title="我的帖子收藏列表">
    <div class="collect-list">
        <light-card class="topic-card" v-for="(item,index)  in list"
        @click="router.push(`/index/topic-detail/${item.id}`)">
            <topic-tag :type="item.type"/>
            <div class="title">
                <b>{{item.title}}</b>
            </div>
            <el-link type="danger" @click.stop="deleteCollection(item.id,index)">删除</el-link>
        </light-card>
    </div>
</el-drawer>
</template>

<style scoped>
.collect-list {
    display: flex;
    flex-direction: column;
    gap: 10px;
}

.topic-card {
    background-color: rgba(128, 128, 128, 0.2);
    transition: .3s;
    display: flex;
    justify-content: space-between;

    .title {
        margin-left: 5px;
        font-size: 14px;
        flex: 1;
        white-space: nowrap;
        display: block;
        overflow: hidden;
        text-overflow: ellipsis;
    }

    &:hover {
        scale: 1.02;
        cursor: pointer;
    }
}
</style>