<template>
  <b-container fluid style="height: 100%; position:absolute">
    <b-row align-v="stretch" style="height: 100%;">
      <b-col style="height: 100%;" cols="2">
        <p>
          <b>Time</b>
          <b-form-input v-model="controlState.time" number="true"></b-form-input>
          <b>Interval</b>
          <b-form-input v-model="controlState.interval" number="true"></b-form-input>
        </p>
        <p>
          <span style="color: #5d69b1">Vehicles</span><br />
          <span style="color: #e58606">Requests</span><br />
          <span style="color: #99c945">Assignments</span><br />
          <span style="color: #00ffff">Rebalancing</span>
        </p>
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
      time: 46000, // 12 * 3600.0,
      interval: 1.0,
    });

    return {
      controlState: controlState
    };
  },
  mounted: function() {
    setInterval(() => {
      if (Number.isFinite(this.controlState.interval) && Number.isFinite(this.controlState.time)) {
        this.controlState.time += this.controlState.interval;
      }
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
