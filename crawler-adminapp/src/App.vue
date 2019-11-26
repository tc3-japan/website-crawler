<template>
  <component :is="layout">
    <transition name="fade" mode="out-in">
        <router-view></router-view>
    </transition>
  </component>
</template>

<script>
import AuthService from '@/services/authService';
import { api } from '@/services/api';
import Store from '@/store';
const defaultLayout = 'full';

export default {
  name: 'app',
  computed: {
    layout() {
      return (this.$route.meta.layout || defaultLayout) + '-layout';
    }
  },
  created: function () {
    let router = this.$router;
    return api().interceptors.response.use(undefined, function (err) {
        // If the user is unauthorized, return to login screen
        if (err.response.status === 401) {
          AuthService.logOut();
          router.push('/');
        }
    });
  }
};
</script>

<style lang="scss">
  @import "assets/base.scss";
</style>