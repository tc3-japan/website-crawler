import Vue from 'vue';
import App from './App.vue';
import router from './router';
import 'bootstrap';
import 'bootstrap/dist/css/bootstrap.min.css';
import Buefy from 'buefy';
import 'buefy/dist/buefy.css';
import BootstrapVue from 'bootstrap-vue'



import SidebarLayout from './layouts/Sidebar.vue';
import FullLayout from './layouts/Full.vue';

Vue.component('sidebar-layout', SidebarLayout);
Vue.component('full-layout', FullLayout);

Vue.use(Buefy);
Vue.use(BootstrapVue);

Vue.config.productionTip = false

new Vue({
  router,
  Buefy,
  render: h => h(App),
}).$mount('#app')
