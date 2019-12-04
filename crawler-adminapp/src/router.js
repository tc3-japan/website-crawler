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
            path: '/sites', 
            name: 'sites',
            meta : { requiresAuth: true, layout : 'sidebar' }, 
            component: () => import('./views/Sites.vue') 
        }
    ]
});

router.beforeEach((to, from, next) => {
    let requiresAuth = true;
        
    // Check if the page requires authentication based
    // on a flag in meta
    if (to.meta.requiresAuth !== undefined) {
        requiresAuth = to.meta.requiresAuth;
    }

    // If the request page requires authentication but the user is not 
    // authenticated redirect them to the login screen
    if(requiresAuth && !store.getters.isAuthenticated)
        return next('/');

    // Ensure the users desired language is loaded, based on stored state
    // and fallback to navigator language if no preferred language has been stored
    if (store.state.language) {
        loadLanguageAsync(store.state.language).then(() => next());
    } else if (navigator.languages && navigator.languages.length > 0) {
        loadLanguageAsync(navigator.languages[0]).then(() => next());
    } else {
        next();
    }
});

export default router;