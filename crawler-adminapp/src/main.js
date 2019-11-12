import Vue from 'vue';
import App from './App.vue';
import router from './router';
import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.min.css';
import BootstrapVue from 'bootstrap-vue';
import ToggleButton from 'vue-js-toggle-button';

// import 'vue-sidebar-menu/dist/vue-sidebar-menu.css';

import SidebarLayout from './layouts/Sidebar.vue';
import FullLayout from './layouts/Full.vue';

Vue.component('sidebar-layout', SidebarLayout);
Vue.component('full-layout', FullLayout);

Vue.use(BootstrapVue);
Vue.use(ToggleButton);

Vue.config.productionTip = false;

new Vue({
  router,
 // Buefy,
  render: h => h(App),
}).$mount('#app');
