<script setup>
import {ref} from "vue";
import {Delta, QuillEditor} from "@vueup/vue-quill";
import '@vueup/vue-quill/dist/vue-quill.snow.css';
import {post} from "@/net/index.js";
import {ElMessage} from "element-plus";
const props = defineProps({
    show:Boolean,
    tid: String,
    quote:Object
})
const content = ref()
const emit = defineEmits(['close','comment'])


const init = () => content.value = new Delta()

function submitComment() {
    post('api/forum/add-comment',{
        tid:props.tid,
        quote: props.quote,
        content: JSON.stringify(content.value)
    },() =>{
        ElMessage.success("发表评论成功")
        emit('comment')
    })
}


</script>

<template>
    <div>
        <el-drawer @open="init" :title="'发表评论'" :model-value="show" @close="emit('close')" direction="btt" :size="270" >
            <div>
                <div>
                    <quill-editor  style="height: 120px" v-model:content="content" placeholder="请发表友善的评论，不要使用脏话骂人"/>
                </div>
                <div style="margin-top: 10px; text-align: right">
                  <el-button type="success" @click="submitComment" plain>发表评论</el-button>
                </div>
            </div>
        </el-drawer>
    </div>

</template>

<<style lang="less" scoped>
:deep(.el-drawer) {
    width: 800px;
    margin: 20px auto;
    border-radius: 10px;
}
:deep(.el-drawer__header) {
    margin: 0;
}
:deep(.el-drawer__body) {
    padding: 10px;
}
</style>