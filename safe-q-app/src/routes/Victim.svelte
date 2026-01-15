<script>
  import { onMount, onDestroy } from 'svelte';
  import { Motion } from '@capacitor/motion';
  import { App } from '@capacitor/app';
  // Note: @capacitor/battery was removed due to build issues.
  // Using cordova-plugin-battery-status directly via window.addEventListener('batterystatus')

  export let navigate;

  let status = 'monitoring'; // 'monitoring', 'warning', 'trapped'
  let countdown = 10;
  let timer;
  let motionListener;
  let beep; // This will be bound to the audio element
  let activeSignal = ''; // 'NAN' or 'BLE'
  let currentBatteryLevel = 100; // Initialize with a default value

  // Algorithm state
  let shakeCount = 0;
  let shakeWindowStart = 0;
  let lastShakeTime = 0;

  // Algorithm parameters
  const G_FORCE_THRESHOLD = 0.7; // Gs
  const SHAKE_COUNT_THRESHOLD = 3; // Number of shakes in time window
  const SHAKE_TIME_WINDOW = 1000; // ms (how long to count shakes)
  const SHAKE_DEBOUNCE_TIME = 200; // ms (min time between shakes to count as distinct)

  function onBatteryStatus(status) {
    currentBatteryLevel = status.level;
    console.log("Battery Level: " + status.level + "%, isPlugged: " + status.isPlugged);
  }

  async function triggerAlert() {
    if (status !== 'monitoring') return;
    
    // Determine active signal based on current battery level
    if (currentBatteryLevel > 40) {
      activeSignal = 'NAN';
    } else {
      activeSignal = 'BLE';
    }

    if (beep) {
      beep.currentTime = 0; // Rewind to start
      beep.play();
    }
    status = 'warning';
    countdown = 10;

    timer = setInterval(() => {
      countdown -= 1;
      if (countdown <= 0) {
        clearInterval(timer);
        status = 'trapped';
        if (beep) {
          beep.pause();
        }
      }
    }, 1000);
  }

  function cancelAlarm() {
    clearInterval(timer);
    status = 'monitoring';
    countdown = 10;
    if (beep) {
      beep.pause();
      beep.currentTime = 0;
    }
    // Reset shake detection state
    shakeCount = 0;
    shakeWindowStart = 0;
    lastShakeTime = 0;
  }

  async function startMonitoring() {
    try {
      // Enable background mode
      if (window.cordova && window.cordova.plugins && window.cordova.plugins.backgroundMode) {
        window.cordova.plugins.backgroundMode.enable();
        window.cordova.plugins.backgroundMode.requestIgnoreBatteryOptimizations();

        window.cordova.plugins.backgroundMode.on('activate', () => {
          console.log('Background mode activated');
          window.cordova.plugins.backgroundMode.configure({
              title: 'Safe-Q is running',
              text: 'Monitoring for earthquakes in the background.'
          });
        });
      }

      // Request permission for motion sensors on iOS 13+
      if (typeof DeviceMotionEvent !== 'undefined' && typeof DeviceMotionEvent.requestPermission === 'function') {
        const permission = await DeviceMotionEvent.requestPermission();
        if (permission !== 'granted') {
          console.error('Permission for motion sensors not granted.');
          return;
        }
      }

      motionListener = await Motion.addListener('accel', (event) => {
        const { x, y, z } = event.acceleration;
        // Convert m/s^2 to G-force (approx 9.81 m/s^2 per G)
        const magnitude = Math.sqrt(x*x + y*y + z*z) / 9.81;

        const now = Date.now();

        if (magnitude > G_FORCE_THRESHOLD && (now - lastShakeTime > SHAKE_DEBOUNCE_TIME)) {
          lastShakeTime = now;
          if (shakeCount === 0) {
            shakeWindowStart = now;
          }
          shakeCount++;
        }

        // Check if enough shakes happened within the time window
        if (shakeCount > 0 && (now - shakeWindowStart > SHAKE_TIME_WINDOW)) {
          if (shakeCount >= SHAKE_COUNT_THRESHOLD) {
            triggerAlert();
          }
          // Reset for next detection cycle
          shakeCount = 0;
          shakeWindowStart = 0;
        } else if (shakeCount > 0 && (now - shakeWindowStart < SHAKE_TIME_WINDOW)) {
          // If a shake is detected within the window, but not enough to trigger,
          // ensure we don't accidentally reset too early by a false negative
          // e.g. if the G-force momentarily drops below threshold but activity resumes
          // This else-if is mainly to prevent resetting if still within active window
        } else if (shakeCount > 0) { // If G-force has been low for a while after some shakes
          shakeCount = 0;
          shakeWindowStart = 0;
        }
      });
    } catch (e) {
      console.error("Failed to initialize sensors or background mode.", e);
    }
  }

  onMount(() => {
    App.addListener('appStateChange', ({ isActive }) => {
      if (!isActive) {
        // App is in background
      }
    });

    // Add battery status listener
    window.addEventListener("batterystatus", onBatteryStatus, false);
    // Initial battery check
    if (window.cordova && window.cordova.plugins && window.cordova.plugins.BatteryStatus) {
      window.cordova.plugins.BatteryStatus.getBatteryStatus(onBatteryStatus);
    }

    startMonitoring();
  });

  onDestroy(() => {
    if (motionListener) {
      motionListener.remove();
    }
    if (timer) {
      clearInterval(timer);
    }
    // Remove battery status listener
    window.removeEventListener("batterystatus", onBatteryStatus, false);

    // Optional: disable background mode when the component is destroyed
    if (window.cordova && window.cordova.plugins.backgroundMode) {
      window.cordova.plugins.backgroundMode.disable();
    }
  });
</script>

<div class="w-full">
  {#if status === 'monitoring'}
    <div class="flex flex-col items-center justify-center text-gray-800 space-y-4">
      <h1 class="text-2xl font-bold tracking-wider">Monitoring...</h1>
      <p class="text-gray-600">This app is continuously monitoring for earthquakes.</p>
    </div>
  {:else if status === 'warning'}
    <div class="text-gray-800">
      <h2 class="text-xl font-bold uppercase tracking-widest">Impact Detected</h2>
      <h1 class="text-9xl font-bold my-4">{countdown}</h1>
      <p>Are you okay?</p>
    </div>
    <button on:click={cancelAlarm} class="btn btn-victim">
      I AM SAFE
    </button>
  {:else if status === 'trapped'}
    <div class="flex flex-col items-center justify-center text-red-800 space-y-4">
      <h1 class="text-3xl font-bold tracking-wider animate-pulse">SOS BROADCASTING</h1>
      <p class="text-red-600">{activeSignal} Signal Active (Battery: {currentBatteryLevel}%)</p>
    </div>
  {/if}

  <div class="mt-10">
      <button on:click={() => navigate('home')} class="text-gray-500">Back to Home</button>
  </div>

  <audio bind:this={beep} src="data:audio/wav;base64,UklGRuBXBwBXQVZFZm10IBAAAAABAAEAgLsAAAB3AQACABAAZGF0YVpUBwCx/5T/O/9P/3j/mP90/3b/ZP9G/zL/R/9S/4z/n/+4/7n/tf+d/yH/dv6i/e38avwp/B79KP/zAPcAs//E/tX+Lf+e/7wA" preload="auto" playsinline></audio>
</div>
