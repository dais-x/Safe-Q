<script>
  import { onMount, onDestroy } from 'svelte';
  import { App } from '@capacitor/app';

  export let navigate;

  // -- Component State --
  let status = 'monitoring'; // 'monitoring', 'warning', 'trapped'
  let countdown = 10;
  let timer;
  let beep;
  let activeSignal = '';
  let currentBatteryLevel = 100;

	// --- Sensor Data ---
	let sensorData = {
		accel: { x: 0, y: 0, z: 0 },
		motion: 0
	};
	
  // --- Fall Detection ---
	const FALL_THRESHOLD = 30; // ~3g impact in m/s^2

  // --- Functions ---

  function onBatteryStatus(status) {
    currentBatteryLevel = status.level;
  }

  async function triggerAlarm() {
    if (status !== 'monitoring') return;
    
    if (currentBatteryLevel > 40) activeSignal = 'NAN';
    else activeSignal = 'BLE';

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

  function cancelAlarm() {
    clearInterval(timer);
    status = 'monitoring';
    countdown = 10;
    if (beep) {
      beep.pause();
      beep.currentTime = 0;
    }
  }

	function startSensors() {
		if (typeof window !== 'undefined' && window.DeviceMotionEvent) {
			window.addEventListener('devicemotion', handleMotion, true);
		}
	}

	function stopSensors() {
		if (typeof window !== 'undefined' && window.DeviceMotionEvent) {
			window.removeEventListener('devicemotion', handleMotion, true);
		}
	}

	function handleMotion(event) {
		const { x, y, z } = event.accelerationIncludingGravity || { x:0, y:0, z:0 };
		sensorData.accel = { 
			x: (x || 0).toFixed(2), 
			y: (y || 0).toFixed(2), 
			z: (z || 0).toFixed(2) 
		};
		
		const mag = Math.sqrt(x*x + y*y + z*z);
		sensorData.motion = mag.toFixed(2);

		if (mag > FALL_THRESHOLD) {
			triggerAlarm();
		}
	}

  onMount(() => {
    App.addListener('appStateChange', ({ isActive }) => {});
    window.addEventListener("batterystatus", onBatteryStatus, false);
    startSensors();
  });

  onDestroy(() => {
    stopSensors();
    if (timer) clearInterval(timer);
    window.removeEventListener("batterystatus", onBatteryStatus, false);
  });
</script>

<div class="w-full">
  {#if status === 'monitoring'}
    <div class="flex flex-col items-center justify-center text-gray-800 space-y-4">
      <h1 class="text-2xl font-bold tracking-wider">Monitoring for Falls</h1>
			<p class="text-gray-600">Using device motion sensors.</p>
      <div class="text-xs text-gray-400 font-mono">
				<p>Motion: {sensorData.motion} m/s²</p>
			</div>
    </div>
  {:else if status === 'warning'}
    <div class="text-gray-800">
      <h2 class="text-xl font-bold uppercase tracking-widest">Fall Detected</h2>
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