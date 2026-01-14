<script>
  import { onMount, onDestroy } from 'svelte';
  import RescuerUI from '../components/RescuerUI.svelte';
  
  let signal = 0; // 0 to 100

  // Keyboard controls for the DEMO (Arrow Up/Down)
  function handleKeydown(e) {
    if (e.key === 'ArrowUp') {
      signal = Math.min(signal + 10, 100);
    }
    if (e.key === 'ArrowDown') {
      signal = Math.max(signal - 10, 0);
    }
  }

  onMount(() => {
    // Listen for key presses
    if (typeof window !== 'undefined') {
        window.addEventListener('keydown', handleKeydown);
    }
  });

  onDestroy(() => {
    if (typeof window !== 'undefined') {
      window.removeEventListener('keydown', handleKeydown);
    }
  });
</script>

<RescuerUI {signal} />