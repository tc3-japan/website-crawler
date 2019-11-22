<template>
  <component :is="layout">
    <transition name="fade" mode="out-in">
        <router-view></router-view>
    </transition>
  </component>
</template>

<script>

const defaultLayout = "full";

export default {
  name: 'app',
  computed: {
    layout() {
      return (this.$route.meta.layout || defaultLayout) + '-layout';
    }
  }
};
</script>

<style lang="scss">
  @import "assets/base.scss";
</style>