<script setup>
import 'element-plus/theme-chalk/index.css'
import { useDark, useToggle } from '@vueuse/core'
import {onMounted} from "vue";
import {unauthorized} from "@/net/index.js";
import {getUserinfo} from "@/net/api/user.js";
import {ref,provide} from "vue";


useDark({
  selector: 'html',
  attribute: 'class',
  valueDark: 'dark',
  valueLight: 'light'
})

useDark({
  onChanged(dark) { useToggle(dark) }
})

const loading = ref(false)
provide('userLoading',loading)
onMounted(() => {
    if(!unauthorized()) {
        getUserinfo(loading)
    }
})
</script>

<template>
<div>
  <router-view/>
</div>
</template>

<style scoped>

</style>
