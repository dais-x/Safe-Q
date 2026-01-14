<script>
  import { onMount, onDestroy } from 'svelte';
  import { Motion } from '@capacitor/motion';
  import VictimUI from '../components/VictimUI.svelte';

  let status = 'safe'; // 'safe', 'warning', 'trapped'
  let countdown = 10;
  let timer;
  let motionListener;
  let beep; // This will be bound to the audio element in VictimUI

  // Set a threshold for shake detection (m/s^2). This value may need tuning.
  const SHAKE_THRESHOLD = 2; 

  // This function is called when a significant shake is detected
  function triggerEarthquake() {
    if (status !== 'safe') return; // Prevent re-triggering
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

  onMount(async () => {
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
        const { x, y, z } = event.acceleration;
        const magnitude = Math.sqrt(x * x + y * y + z * z);
        console.log('Accelerometer data:', event.acceleration);
        console.log('Calculated magnitude:', magnitude);
        
        if (magnitude > SHAKE_THRESHOLD) {
          triggerEarthquake();
        }
      });
    } catch (e) {
      console.error("Failed to initialize motion sensors.", e);
    }
  });

  onDestroy(() => {
    if (motionListener) {
      motionListener.remove();
    }
    if (timer) {
      clearInterval(timer);
    }
  });
</script>

<VictimUI {status} {countdown} {cancelAlarm} bind:beep={beep} />