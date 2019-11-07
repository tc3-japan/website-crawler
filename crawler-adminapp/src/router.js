import Vue from 'vue';
import VueRouter from 'vue-router'

Vue.use(VueRouter);

let router = new VueRouter({
    scrollBehavior() {
        return window.scrollTo({ top: 0, behavior: 'smooth' });
    },
    routes : [
        { 
            path: '/', 
            name: 'login',
            meta : { layout : 'full' }, 
            component: () => import('./views/Login.vue') 
        },
        { 
            path: '/home', 
            name: 'home', 
            component: () => import('./views/Home.vue') 
        },
        { 
            path: '/sites', 
            name: 'sites', 
            component: () => import('./views/Sites.vue') 
        }
    ]
})

export default router;