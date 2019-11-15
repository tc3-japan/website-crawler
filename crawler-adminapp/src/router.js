import Vue from 'vue';
import VueRouter from 'vue-router';
import { loadLanguageAsync } from './lang';
import store from './store';

Vue.use(VueRouter);

let router = new VueRouter({
    scrollBehavior() {
        return window.scrollTo({ top: 0, behavior: 'smooth' });
    },
    routes : [
        { 
            path: '/', 
            name: 'login',
            meta : { requiresAuth: false, layout : 'full' }, 
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
});

router.beforeEach((to, from, next) => {
    let requiresAuth = true;
        
    if (to.meta.requiresAuth !== undefined) {
        requiresAuth = to.meta.requiresAuth;
    }

    // if(requiresAuth && !store.getters.isAuthenticated)
    //     return next('/');

    if (store.state.language) {
        console.log('already got language', store.state.language);
        loadLanguageAsync(store.state.language).then(() => next());
    } else if (navigator.languages && navigator.languages.length > 0) {
        console.log('committing language', navigator.languages[0]);
        loadLanguageAsync(navigator.languages[0]).then(() => next());
    }
    else {
        next();
    }
});

export default router;