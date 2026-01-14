<script>
  import { onMount, onDestroy } from 'svelte';
  import { Motion } from '@capacitor/motion';
  import VictimUI from '../components/VictimUI.svelte';

  let status = 'safe'; // 'safe', 'warning', 'trapped'
  let countdown = 10;
  let timer;
  let motionListener;
  let beep; // This will be bound to the audio element in VictimUI
  let armed = false; // System is not armed by default
  let acceleration = { x: 0, y: 0, z: 0 };

  // Set a threshold for shake detection (m/s^2). This value may need tuning.
  const SHAKE_THRESHOLD = 15;

  // This function is called when a significant shake is detected
  function triggerEarthquake() {
    if (status !== 'safe' || !armed) return; // Prevent re-triggering
    if (beep) {
      beep.play();
    }
    status = 'warning';
    countdown = 10;

    // Timer counts down from 10
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
    status = 'safe';
    countdown = 10;
    if (beep) {
      beep.pause();
      beep.currentTime = 0;
    }
  }

  async function armSystem() {
    try {
      // Request permission for motion sensors on iOS 13+
      if (typeof DeviceMotionEvent !== 'undefined' && typeof DeviceMotionEvent.requestPermission === 'function') {
        const permission = await DeviceMotionEvent.requestPermission();
        console.log('Motion sensor permission:', permission);
        if (permission !== 'granted') {
          console.error('Permission for motion sensors not granted.');
          return;
        }
      }

      motionListener = await Motion.addListener('accel', (event) => {
        acceleration = event.acceleration;
        const { x, y, z } = event.acceleration;
        const magnitude = Math.sqrt(x * x + y * y + z * z);
        
        if (magnitude > SHAKE_THRESHOLD) {
          triggerEarthquake();
        }
      });
      
      // Play and pause audio to unlock it
      if(beep) {
        await beep.play();
        beep.pause();
        beep.currentTime = 0;
      }

      armed = true;
    } catch (e) {
      console.error("Failed to initialize motion sensors.", e);
    }
  }

  onDestroy(() => {
    if (motionListener) {
      motionListener.remove();
    }
    if (timer) {
      clearInterval(timer);
    }
  });
</script>

{#if !armed}
  <div class="h-screen w-full bg-slate-900 text-white font-sans flex flex-col items-center justify-center p-4">
    <button on:click={armSystem} class="w-full max-w-xs py-4 bg-cyan-500 text-slate-900 font-bold text-xl rounded-lg shadow-2xl active:scale-95 transition-transform">
      ARM SYSTEM
    </button>
  </div>
{:else}
  <VictimUI {status} {countdown} {cancelAlarm} bind:beep={beep} {acceleration} />
{/if}
