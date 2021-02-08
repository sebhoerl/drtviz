<template>
  <div id="map-view"></div>
</template>

<script>
import * as axios from "axios";
import * as THREE from "three";
import * as _ from "lodash";

export default {
  name: 'MapView',
  props: ["controlState"],
  watch: {
    "controlState.time": _.throttle(function() {
      if (Number.isFinite(this.controlState.time)) {
        this.requestedSimulationTime = this.controlState.time;
      }
    }, 100)
  },
  data: function() {
    return {
      camera: undefined, scene: undefined, renderer: undefined,
      tickIntervalId: undefined,

      needsRedraw: false, needsUpate: false, isUpdating: false,

      //centerX: 3000.0, centerY: -3000.0,
      centerX: 565615.9484495135, centerY: 5936262.051940319,
      cameraDistance: 9000.0,
      groundAngle: 286.0, sphereAngle: 60.0,
      isCameraMoving: false, isCameraRotating: false,
      isCameraInitialized: false,

      requestedSimulationTime: 8.0 * 3600.0,
      currentSimulationTime: 0.0 * 3600.0,

      vehicles: {}, requests: {}, assignments: {}, relocations: {},

      networkGeometry: undefined,
      networkMesh: undefined
    }
  },
  mounted: function() {
    this.scene = new THREE.Scene();
    this.scene.background = new THREE.Color(0xffffff);

    this.camera = new THREE.PerspectiveCamera(
      75, 1.0, 0.1, 1000000);

    this.renderer = new THREE.WebGLRenderer();

    this.onResize();
    window.addEventListener('resize', this.onResize);

    this.$el.appendChild(this.renderer.domElement);

    this.tickIntervalId = setInterval(this.onTick, 100);

    /*this.camera.position.x = 3000.0;
    this.camera.position.y = -6000.0;
    this.camera.position.z = 4000.0;

    this.camera.lookAt(3000.0, -3000.0, 0.0);*/

    this.updateCamera();

    this.loadNetwork();

    this.renderer.domElement.addEventListener("wheel", e => {
      this.cameraDistance += 1000 * e.deltaY;

      if (this.cameraDistance < 1000) {
        this.cameraDistance = 1000.0;
      }

      this.updateCamera();
    });

    this.renderer.domElement.oncontextmenu = () => false;

    this.renderer.domElement.addEventListener("mousedown", e => {
      if (e.button == 2) {
        this.isCameraMoving = true;
        this.isCameraRotating = false;
      } else if (e.button == 0) {
        this.isCameraRotating = true;
        this.isCameraMoving = false;
        e.preventDefault();
        e.stopPropagation();
      }
    });

    this.renderer.domElement.addEventListener("mouseup", () => {
      this.isCameraMoving = false;
      this.isCameraRotating = false;
    });

    this.renderer.domElement.addEventListener("mouseleave", () => {
      this.isCameraMoving = false;
      this.isCameraRotating = false;
    });

    this.renderer.domElement.addEventListener("mousemove", e => {
      if (this.isCameraMoving) {
        var strength = 100 * this.cameraDistance / 10000.0;

        this.centerX -= strength * Math.cos(2.0 * Math.PI * this.groundAngle / 360.0) * e.movementY;
        this.centerY -= strength * Math.sin(2.0 * Math.PI * this.groundAngle / 360.0) * e.movementY;
        this.centerX -= strength * Math.cos(2.0 * Math.PI * this.groundAngle / 360.0 + 0.5 * Math.PI) * e.movementX;
        this.centerY -= strength * Math.sin(2.0 * Math.PI * this.groundAngle / 360.0 + 0.5 * Math.PI) * e.movementX;

        this.updateCamera();
      } else if (this.isCameraRotating) {
        this.groundAngle -= 5e-1 * e.movementX;
        this.sphereAngle -= 5e-1 * e.movementY;

        if (this.sphereAngle < 30.0) {
          this.sphereAngle = 30.0;
        }

        if (this.sphereAngle > 90.0) {
          this.sphereAngle = 90.0;
        }

        if (this.groundAngle > 360.0) {
          this.groundAngle -= 360.0;
        }

        if (this.groundAngle < 0.0) {
          this.groundAngle = 360.0 - this.groundAngle;
        }

        this.updateCamera();
      }
    });
  },
  destroyed: function() {
    clearInterval(this.tickIntervalId);
  },
  methods: {
    onResize: function() {
      var width = this.$el.clientWidth;
      var height = this.$el.clientHeight;

      this.camera.aspect = width / height;
      this.camera.updateProjectionMatrix();

      this.renderer.setSize(width, height);
    },
    onTick: function() {
      var needsUpdate = this.needsUpate;
      needsUpdate |= this.requestedSimulationTime != this.currentSimulationTime;

      if (needsUpdate) {
        this.startUpdate();
      }

      if (this.needsRedraw) {
        this.needsRedraw = false;

        requestAnimationFrame(() => {
          this.renderer.render(this.scene, this.camera);
        });
      }
    },
    startUpdate: function() {
      if (!this.isUpdating) {
        this.isUpdating = true;
        var requestedSimulationTime = this.requestedSimulationTime;

        axios.post("http://localhost:9000/visualisation", {
          subject: "vehicles", time: requestedSimulationTime
        }).then(response => {
          this.processUpdate(response.data);

          this.currentSimulationTime = requestedSimulationTime;
          this.needsRedraw = true;
          this.isUpdating = false;
        });
      }
    },
    processUpdate: function(data) {
      this.processVehicles(data);
      this.processRequests(data);
      this.processAssignments(data);
      this.processRelocations(data);
    },
    processVehicles: function(data) {
      var deletableVehicles = {};
      for (var id in this.vehicles) {
        deletableVehicles[id] = true;
      }

      data.vehicles.forEach(v => {
        var vehicle = this.vehicles[v.id];

        if (vehicle == undefined) {
          var geometry = new THREE.CylinderGeometry(20.0, 20.0, 50.0, 32);
          var material = new THREE.MeshBasicMaterial({ color: 0x5d69b1 });

          var mesh = new THREE.Mesh(geometry, material);
          mesh.rotation.x = THREE.Math.degToRad(90);
          this.scene.add(mesh);

          vehicle = {
            mesh: mesh
          };

          this.vehicles[v.id] = vehicle;
        }

        vehicle.mesh.position.x = v.x;
        vehicle.mesh.position.y = v.y;
        deletableVehicles[v.id] = false;
      });

      for (id in deletableVehicles) {
        if (deletableVehicles[id]) {
          this.scene.remove(this.vehicles[id].mesh);
          delete this.vehicles[id];
        }
      }
    },
    processRequests: function(data) {
      var deletableRequests = {};
      for (var id in this.requests) {
        deletableRequests[id] = true;
      }

      data.requests.forEach(r => {
        var request = this.requests[r.id];

        if (request == undefined) {
          var distance = Math.sqrt(
            Math.pow(r.origin[0] - r.destination[0], 2.0) +
            Math.pow(r.origin[1] - r.destination[1], 2.0)
          );

          var startLocation = new THREE.Vector3(r.origin[0], r.origin[1], 0.0);
          var endLocation = new THREE.Vector3(r.destination[0], r.destination[1], 0.0);
          var controlLocation = new THREE.Vector3(
            0.5 * (r.origin[0] + r.destination[0]),
            0.5 * (r.origin[1] + r.destination[1]),
            0.25 * distance
          );

          var curve = new THREE.QuadraticBezierCurve3(
            startLocation, controlLocation, endLocation
          );

          var geometry = new THREE.BufferGeometry().setFromPoints(
            curve.getPoints(32)
          );

          var requestColor = 0xe58606;

          var material = new THREE.LineBasicMaterial({ color : requestColor });
          var mesh = new THREE.Line(geometry, material);

          this.scene.add(mesh);

          geometry = new THREE.SphereGeometry(40, 20, 20);
          material = new THREE.MeshBasicMaterial({ color: requestColor });
          var dot = new THREE.Mesh(geometry, material);

          dot.position.set(
            r.origin[0], r.origin[1], 0.0
          )
          this.scene.add(dot);

          request = {
            mesh: mesh,
            dot: dot,
            curve: curve
          };

          this.requests[r.id] = request;
        }

        request.curve.getPoint(r.relativeLocation, request.dot.position);

        deletableRequests[r.id] = false;
      });

      for (id in deletableRequests) {
        if (deletableRequests[id]) {
          this.scene.remove(this.requests[id].mesh);
          this.scene.remove(this.requests[id].dot);
          delete this.requests[id];
        }
      }
    },
    processAssignments: function(data) {
      var deletableAssignments = {};
      for (var id in this.assignments) {
        deletableAssignments[id] = true;
      }

      data.assignments.forEach(a => {
        var assignment = this.assignments[a.requestId + "#" + a.vehicleId];

        if (assignment == undefined) {
          assignment = {
            mesh: undefined,
            vehicleId: a.vehicleId,
            requestId: a.requestId
          };

          this.assignments[a.requestId + "#" + a.vehicleId] = assignment;
        }

        var vehicle = this.vehicles[a.vehicleId];
        var request = this.requests[a.requestId];

        if (vehicle && request) {
          if (assignment.mesh) {
            assignment.mesh.geometry.dispose();
            this.scene.remove(assignment.mesh);
          }

          var vehiclePosition = vehicle.mesh.position;
          var requestPosition = request.dot.position;

          var points = [];
          points.push(vehiclePosition);
          points.push(requestPosition);

          var geometry = new THREE.BufferGeometry().setFromPoints(points);
          var material = new THREE.LineBasicMaterial({ color : 0x99c945 });
          var mesh = new THREE.Line(geometry, material);

          assignment.mesh = mesh;
          this.scene.add(mesh);

          deletableAssignments[a.requestId + "#" + a.vehicleId] = false;
        }
      });

      for (id in deletableAssignments) {
        if (deletableAssignments[id]) {
          if (this.assignments[id].mesh) {
            this.scene.remove(this.assignments[id].mesh);
          }

          delete this.assignments[id];
        }
      }
    },
    processRelocations: function(data) {
      var deletableRelocations = {};
      for (var id in this.relocations) {
        deletableRelocations[id] = true;
      }

      data.relocations.forEach(r => {
        var relocation = this.relocations[r.vehicleId];

        if (relocation == undefined) {
          relocation = {
            mesh: undefined,
            vehicleId: r.vehicleId,
            destination: new THREE.Vector3(r.destination[0], r.destination[1], 0.0)
          };

          this.relocations[r.vehicleId] = relocation;
        }

        var vehicle = this.vehicles[r.vehicleId];

        if (vehicle) {
          if (relocation.mesh) {
            relocation.mesh.geometry.dispose();
            this.scene.remove(relocation.mesh);
          }

          var vehiclePosition = vehicle.mesh.position;

          var points = [];
          points.push(vehiclePosition);
          points.push(relocation.destination);

          var geometry = new THREE.BufferGeometry().setFromPoints(points);
          var material = new THREE.LineBasicMaterial({ color : 0x00ffff });
          var mesh = new THREE.Line(geometry, material);

          relocation.mesh = mesh;
          this.scene.add(mesh);

          deletableRelocations[r.vehicleId] = false;
        }
      });

      for (id in deletableRelocations) {
        if (deletableRelocations[id]) {
          if (this.relocations[id].mesh) {
            this.scene.remove(this.relocations[id].mesh);
          }

          delete this.relocations[id];
        }
      }
    },
    loadNetwork: function() {
      axios.post("http://localhost:9000/visualisation", {
        subject: "network"
      }).then(response => {
        this.processNetwork(response.data);
        this.needsRedraw = true;
      });
    },
    processNetwork: function(data) {
      var points = [];

      data.links.forEach(l => {
        points.push(new THREE.Vector3(l.from[0], l.from[1], 0.0));
        points.push(new THREE.Vector3(l.to[0], l.to[1], 0.0));
      });

      var networkMaterial = new THREE.LineBasicMaterial({
        color: 0xdddddd
      });

      this.networkGeometry = new THREE.BufferGeometry().setFromPoints(points);
      this.networkMesh = new THREE.LineSegments(this.networkGeometry, networkMaterial);
      this.scene.add(this.networkMesh);

      if (!this.isCameraInitialized) {
        this.networkGeometry.computeBoundingBox();

        this.centerX = 0.5 * this.networkGeometry.boundingBox.min.x + 0.5 * this.networkGeometry.boundingBox.max.x;
        this.centerY = 0.5 * this.networkGeometry.boundingBox.min.y + 0.5 * this.networkGeometry.boundingBox.max.y;

        this.sphereAngle = 70.0;
        this.groundAngle = 0.0;

        this.updateCamera();
      }
    },
    updateCamera: function() {
      var elevationFactor = Math.cos(2.0 * Math.PI * this.sphereAngle / 360.0);

      this.camera.position.x = this.centerX + Math.cos(2.0 * Math.PI * this.groundAngle / 360.0) * this.cameraDistance * elevationFactor;
      this.camera.position.y = this.centerY + Math.sin(2.0 * Math.PI * this.groundAngle / 360.0) * this.cameraDistance * elevationFactor;
      this.camera.position.z = (1.0 - elevationFactor) * this.cameraDistance;

      if (this.sphereAngle == 90.0) {
        var angle = 2.0 * Math.PI * this.groundAngle / 360.0;
        this.camera.up = new THREE.Vector3(-Math.cos(angle), -Math.sin(angle), 0.0);
        this.camera.lookAt(this.centerX, this.centerY, 0.0);
      } else {
        this.camera.up = new THREE.Vector3(0.0, 0.0, 1.0);
        this.camera.lookAt(this.centerX, this.centerY, 0.0);
      }

      this.camera.updateProjectionMatrix();
      this.needsRedraw = true;
    }
  },
}
</script>

<style scoped>
#map-view {
  padding: 0px 0px 0px 0px;
  margin: 0px 0px 0px 0px;
  width: 100%;
  height: 100%;
}
</style>
