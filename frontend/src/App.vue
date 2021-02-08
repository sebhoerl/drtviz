<template>
  <b-container fluid style="height: 100%; position:absolute">
    <!--b-navbar toggleable="lg" type="dark" variant="info" style="position: absolute; z-index: 100; width: 100%; margin-left:-1em;">
      <b-navbar-brand href="#">DRT Visualizer</b-navbar-brand>
      <b-navbar-nav>
      </b-navbar-nav>
    </b-navbar-->
    <b-row align-v="stretch" style="height: 100%;">
      <b-col style="height: 100%;" cols="2">
        <input v-model="controlState.time" />
        {{ controlState.time }}
      </b-col>
      <b-col style="height:100%; margin: 0px 0px 0px 0px; padding: 0px 0px 0px 0px;">
        <MapView :controlState="controlState" />
      </b-col>
    </b-row>
  </b-container>
</template>

<script>
import MapView from './components/MapView.vue'
import Vue from 'vue'

export default {
  name: 'App',
  components: {
    MapView
  },
  data: function() {
    var controlState = Vue.observable({
      time: 46000 // 12 * 3600.0
    });

    return {
      controlState: controlState
    };
  },
  mounted: function() {
    setInterval(() => {
      this.controlState.time += 1;
    }, 10);
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
