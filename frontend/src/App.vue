<template>
  <b-container fluid style="height: 100%; position:absolute">
    <b-row align-v="stretch" style="height: 100%;">
      <b-col style="height: 100%;" cols="2">
        <p>
          <b>Time</b>
          <b-form-input type="number" v-model="controlState.time" number></b-form-input>
          <b>Interval</b>
          <b-form-input type="number" v-model="controlState.interval" number></b-form-input>
        </p>
        <p>
          <span style="color: #5d69b1">Vehicles</span><br />
          <span style="color: #e58606">Requests</span><br />
          <span style="color: #99c945">Assignments</span><br />
          <span style="color: #52bca3">Rebalancing</span>
        </p>
        <p>
          Progress:<br />
          <span v-if="!progressFinished">{{ formatTime(progressTime) }}</span>
          <span v-if="progressFinished">Finished</span>
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
import * as axios from "axios"

export default {
  name: 'App',
  components: {
    MapView
  },
  data: function() {
    var controlState = Vue.observable({
      time: 0.0, // 12 * 3600.0,
      interval: 0.0,
      apiEndpoint: "",
      isEndpointReady: false
    });

    return {
      controlState: controlState,
      progressTime: 0.0,
      progressFinished: false
    };
  },
  mounted: function() {
    setInterval(() => {
      if (Number.isFinite(this.controlState.interval) && Number.isFinite(this.controlState.time) && Math.abs(this.controlState.interval) >= 1.0) {
        if (this.progressFinished || this.controlState.time + this.controlState.interval < this.progressTime) {
          this.controlState.time += this.controlState.interval;
        }
      }
    }, 10);

    axios.get("/endpoint.json").then(response => {
      this.controlState.apiEndpoint = response.data["endpoint"];
      this.controlState.isEndpointReady = true;

      this.updateProgress();
    });
  },
  methods: {
    updateProgress: function() {
      axios.get(this.controlState.apiEndpoint + "/progress").then(response => {
        this.progressTime = response.data.time;
        this.progressFinished = response.data.finished;

        if (!this.progressFinished) {
          setTimeout(this.updateProgress, 1000);
        }
      });
    },
    formatTime: function(time) {
      var hour = Math.floor(time / 3600.0);
      time = time - hour * 3600;

      var minute = Math.floor(time / 60);
      time = time - minute * 60;

      var second = time;

      var output = "";

      if (hour < 10) {
        output += "0";
      }

      output += hour;
      output += ":";

      if (minute < 10) {
        output += "0";
      }

      output += minute;
      output += ":";

      if (second < 10) {
        output += "0";
      }

      output += second;
      return output;
    }
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
