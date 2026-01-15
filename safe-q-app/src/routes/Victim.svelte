<script>
  import { onMount, onDestroy } from 'svelte';
  import { Motion } from '@capacitor/motion';
  import { App } from '@capacitor/app';

  export let navigate;

  // -- Component State --
  let status = 'monitoring'; // 'monitoring', 'warning', 'trapped'
  let countdown = 10;
  let timer;
  let motionListener;
  let beep; // This will be bound to the audio element
  let activeSignal = ''; // 'NAN' or 'BLE'
  let currentBatteryLevel = 100; // Initialize with a default value

  // -- Hackathon Algorithm: State & Parameters --

  // A low-pass filter helps smooth out noisy, high-frequency data from the accelerometer.
  // A higher `filterFactor` means more smoothing. 0.0 means no smoothing.
  const LOW_PASS_FILTER_FACTOR = 0.1;
  let smoothedMagnitude = 0;

  // -- Stage 1: Initial Jolt Detection --
  // This threshold needs to be high enough to ignore everyday bumps.
  // It acts as the "wake-up" call for the algorithm.
  const TRIGGER_G_FORCE = 2.0; // Gs

  // -- Stage 2: Sustained Shaking Analysis --
  // Once triggered, we enter a monitoring window to confirm a real earthquake.
  const MONITORING_WINDOW_MS = 3000; // 3 seconds

  // This threshold is lower. We're looking for continuous, significant shaking,
  // not necessarily huge spikes.
  const SUSTAINED_G_FORCE = 0.6; // Gs
  
  // We count how many times the G-force exceeds the sustained threshold.
  const SUSTAINED_SHAKE_COUNT_THRESHOLD = 8;

  // We also measure the "energy" of the event by accumulating the G-force.
  // This helps differentiate a long, gentle wobble from a short, violent shake.
  const ENERGY_THRESHOLD = 15;

  let isMonitoring = false;
  let monitoringStartTime = 0;
  let sustainedShakeCount = 0;
  let totalEnergy = 0;

  // --- Functions ---

  function onBatteryStatus(status) {
    currentBatteryLevel = status.level;
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
      beep.currentTime = 0;
      beep.play();
    }
    status = 'warning';
    countdown = 10;

    timer = setInterval(() => {
      countdown -= 1;
      if (countdown <= 0) {
        clearInterval(timer);
        status = 'trapped';
        if (beep) beep.pause();
      }
    }, 1000);
  }

  function resetDetectionState() {
    isMonitoring = false;
    monitoringStartTime = 0;
    sustainedShakeCount = 0;
    totalEnergy = 0;
  }

  function cancelAlarm() {
    clearInterval(timer);
    status = 'monitoring';
    countdown = 10;
    if (beep) {
      beep.pause();
      beep.currentTime = 0;
    }
    resetDetectionState();
  }

  async function startMonitoring() {
    try {
      // Enable background mode for continuous operation
      if (window.cordova && window.cordova.plugins.backgroundMode) {
        window.cordova.plugins.backgroundMode.enable();
        window.cordova.plugins.backgroundMode.requestIgnoreBatteryOptimizations();
        window.cordova.plugins.backgroundMode.on('activate', () => {
          window.cordova.plugins.backgroundMode.configure({
              title: 'Safe-Q is running',
              text: 'Monitoring for earthquakes in the background.'
          });
        });
      }

      // Request motion sensor permissions for iOS
      if (typeof DeviceMotionEvent !== 'undefined' && typeof DeviceMotionEvent.requestPermission === 'function') {
        const permission = await DeviceMotionEvent.requestPermission();
        if (permission !== 'granted') return;
      }

      // -- Main Algorithm Listener --
      motionListener = await Motion.addListener('accel', (event) => {
        const { x, y, z } = event.acceleration;
        const magnitude = Math.sqrt(x*x + y*y + z*z) / 9.81;

        // Apply the low-pass filter to the raw magnitude
        smoothedMagnitude = (magnitude * (1 - LOW_PASS_FILTER_FACTOR)) + (smoothedMagnitude * LOW_PASS_FILTER_FACTOR);

        const now = Date.now();

        if (!isMonitoring) {
          // STAGE 1: Look for the initial high-G jolt
          if (smoothedMagnitude > TRIGGER_G_FORCE) {
            isMonitoring = true;
            monitoringStartTime = now;
            sustainedShakeCount = 1; // The trigger event counts as the first shake
            totalEnergy = smoothedMagnitude;
          }
        } else {
          // STAGE 2: We are in the monitoring window
          const elapsedTime = now - monitoringStartTime;

          if (elapsedTime > MONITORING_WINDOW_MS) {
            // -- Window has ended, time to decide --
            if (sustainedShakeCount >= SUSTAINED_SHAKE_COUNT_THRESHOLD && totalEnergy >= ENERGY_THRESHOLD) {
              triggerAlert();
            }
            // Reset for the next event
            resetDetectionState();
          } else {
            // -- Still in the window, accumulate data --
            totalEnergy += smoothedMagnitude;
            if (smoothedMagnitude > SUSTAINED_G_FORCE) {
              sustainedShakeCount++;
            }
          }
        }
      });
    } catch (e) {
      console.error("Failed to initialize sensors or background mode.", e);
    }
  }

  onMount(() => {
    App.addListener('appStateChange', ({ isActive }) => {
      // Logic for background state can be added here if needed
    });

    window.addEventListener("batterystatus", onBatteryStatus, false);
    
    startMonitoring();
  });

  onDestroy(() => {
    if (motionListener) motionListener.remove();
    if (timer) clearInterval(timer);
    window.removeEventListener("batterystatus", onBatteryStatus, false);
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
      <div class="text-xs text-gray-400">
        (G-Force: {smoothedMagnitude.toFixed(2)})
      </div>
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
