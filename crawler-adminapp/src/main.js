import Vue from 'vue'
import App from './App.vue'
import router from './router';

import SidebarLayout from './layouts/Sidebar.vue';
import FullLayout from './layouts/Full.vue';

Vue.component('sidebar-layout', SidebarLayout);
Vue.component('full-layout', FullLayout);

Vue.config.productionTip = false

new Vue({
  router,
  render: h => h(App),
}).$mount('#app')
