<template>
    <div id="fusion-app">
        <router-view />
    </div>
</template>

<script>
  export default {
    name: 'App',
  };
</script>

<style lang="scss">
  #fusion-app {
    // height: calc(100vh - var(--tm-header-height));
    height: 100vh;
    background: $layout-main-background;
    font-family: 'Avenir', Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
  }
</style>
