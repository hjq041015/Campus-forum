<script setup>

import {computed, reactive, ref} from "vue";
import {EditPen, Lock, Message, User} from "@element-plus/icons-vue";
import router from "@/router/index.js";
import {ElMessage} from "element-plus";
import {get, post} from "@/net/index.js";
import {apiAuthAskCode, apiAuthRegister} from "@/net/api/user.js";


const form = reactive({
  username: '',
  password: '',
  password_repeat: '',
  email: '',
  code: ''
})

const coldTime = ref(0)
const formRef = ref()


const validateUsername = (rule,value,callback) => {
  if(value === '') {
      callback(new Error('请输入用户名'))
  }else if (!/^[a-zA-Z0-9\u4e00-\u9fa5]+$/.test(value)){
      callback(new Error('用户名不能包含特殊字符'))
  }else {
      callback()
  }
}

const validatePassword = (rule,value,callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  }else if (value !== form.password) {
    callback(new Error('两次输入的密码不一样'))
  }else {
    callback()
  }
}

const rules = {
    username: [
        {  validator:validateUsername, trigger: ['blur', 'change'] },
        { min: 2, max: 8, message: '用户名的长度必须在2-8个字符之间', trigger: ['blur', 'change'] },
    ],
    password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 16, message: '密码的长度必须在6-16个字符之间', trigger: ['blur', 'change'] }
    ],
    password_repeat: [
        { validator: validatePassword, trigger: ['blur', 'change'] },
    ],
    email: [
        { required: true, message: '请输入邮件地址', trigger: 'blur' },
        {type: 'email', message: '请输入合法的电子邮件地址', trigger: ['blur', 'change']}
    ],
    code: [
        { required: true, message: '请输入获取的验证码', trigger: 'blur' },
    ]
}



const validateEmail = () => {
    apiAuthAskCode(form.email, coldTime)
}

const isEmailValid =computed(() => {
 return  /\w[-\w.+]*@([A-Za-z0-9][-A-Za-z0-9]+\.)+[A-Za-z]{2,14}/.test(form.email)
})

const register = () => {
  formRef.value.validate((valid) => {
    if(valid) {
      apiAuthRegister({
          username: form.username,
          password: form.password,
          email: form.email,
          code: form.code
      })
    }else {
      ElMessage.warning(`请填写完整的注册表单内容`)
    }
  })
}


</script>

<template>
<div style="text-align: center; margin: 0 20px">
  <div style="margin-top: 100px">
            <div style="font-size: 25px;font-weight: bold">注册新用户</div>
            <div style="font-size: 14px;color: grey">欢迎注册我们的学习平台，请在下方填写相关信息</div>
  </div>
  <div style="margin-top: 50px">
    <el-form :model="form" :rules="rules" ref="formRef">
      <el-form-item prop="username">
        <el-input v-model="form.username" :maxlength="8" type="text" placeholder="请输入用户名">
          <template #prefix>
              <el-icon><User /></el-icon>
            </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password">
        <el-input v-model="form.password" :maxlength="16" type="password" placeholder="请输入密码">
           <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="password_repeat">
        <el-input v-model="form.password_repeat" :maxlength="16" type="password" placeholder="请确认密码">
           <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="email">
         <el-input v-model="form.email" type="email" placeholder="请输入邮箱">
          <template #prefix>
            <el-icon><Message /></el-icon>
          </template>
        </el-input>
      </el-form-item>
      <el-form-item prop="code">
        <el-row :gutter="10" style="width: 100%">
          <el-col :span="17">
            <el-input v-model="form.code" :maxlength="6" type="text" placeholder="请输入验证码">
              <template #prefix>
                <el-icon><EditPen /></el-icon>
              </template>
            </el-input>
          </el-col>
          <el-col :span="5">
             <el-button type="success" @click="validateEmail"
                                       :disabled="!isEmailValid || coldTime > 0">
                                {{coldTime > 0 ? '请稍后 ' + coldTime + ' 秒' : '获取验证码'}}
                            </el-button>
          </el-col>
        </el-row>
      </el-form-item>
    </el-form>
  </div>
  <div style="margin-top: 80px">
    <el-button @click="register"   style="width: 270px" type="warning" plain>注册</el-button>
  </div>
  <div style="margin-top: 20px">
    <span style="font-size: 14px; line-height: 15px; color:gray">已有账号?</span>
     <el-link type="primary" style="translate: 0 -2px" @click="router.push('/')">立即登录</el-link>
  </div>
</div>
</template>

<style scoped>

</style>