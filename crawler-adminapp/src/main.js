import Vue from 'vue';
import App from './App.vue';
import router from './router';
import { i18n } from './lang';
import Vuelidate from 'vuelidate';
import store from './store';
import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap-vue/dist/bootstrap-vue.css';
import BootstrapVue from 'bootstrap-vue';
import ToggleButton from 'vue-js-toggle-button';

import SidebarLayout from './layouts/Sidebar.vue';
import FullLayout from './layouts/Full.vue';

Vue.component('sidebar-layout', SidebarLayout);
Vue.component('full-layout', FullLayout);

Vue.use(BootstrapVue);
Vue.use(ToggleButton);

Vue.use(Vuelidate);

Vue.config.productionTip = false;

new Vue({
  router,
  i18n,
  store,
  render: h => h(App),
}).$mount('#app');