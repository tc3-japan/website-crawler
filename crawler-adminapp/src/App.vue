<template>
  <component :is="layout">
    <router-view class="view"></router-view>
  </component>
</template>

<script>

const defaultLayout = "sidebar";

export default {
  name: 'app',
  computed: {
    layout() {
      return (this.$route.meta.layout || defaultLayout) + '-layout';
    }
  }
}
</script>

<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
