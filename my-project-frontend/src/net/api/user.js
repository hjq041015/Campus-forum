import {get} from "@/net/index.js";
import {useStore} from "@/store/index.js";

export const getUserinfo = (loadingRef) => {
    loadingRef.value = false
    const store = useStore()
    get('api/user/info',(data) => {
    store.user = data
    loadingRef.value = false
})
}