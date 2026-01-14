<script>
    export let status = 'safe'; // 'safe', 'warning', 'trapped'
    export let countdown = 10;
    export let cancelAlarm = () => {};
    export let beep; // For binding the audio element
</script>

<div class="h-screen w-full flex flex-col items-center justify-center p-6 text-center font-sans transition-all duration-500"
     class:bg-slate-900={status === 'safe'}
     class:bg-amber-500={status === 'warning'}
     class:bg-red-900={status === 'trapped'}>

  <!-- Safe State -->
  {#if status === 'safe'}
    <div class="flex flex-col items-center justify-center text-cyan-300 space-y-4">
      <div class="relative w-48 h-48">
        <div class="absolute inset-0 border-2 border-cyan-300/30 rounded-full animate-ping"></div>
        <div class="absolute inset-2 border-2 border-cyan-300/50 rounded-full"></div>
        <div class="w-full h-full flex items-center justify-center">
          <svg class="w-16 h-16" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
        </div>
      </div>
      <h1 class="text-2xl font-bold tracking-wider text-white">SYSTEM ARMED</h1>
      <p class="text-cyan-400/70">Monitoring for significant impact...</p>
    </div>
  {/if}

  <!-- Warning State -->
  {#if status === 'warning'}
    <div class="text-white">
      <h2 class="text-xl font-bold uppercase tracking-widest text-white/80">Impact Detected</h2>
      <h1 class="text-9xl font-bold my-4">{countdown}</h1>
      <p class="text-white/80">Are you okay?</p>
    </div>
    <button on:click={cancelAlarm} class="mt-10 w-full max-w-xs py-4 bg-white text-slate-900 font-bold text-xl rounded-lg shadow-2xl active:scale-95 transition-transform">
      I AM SAFE
    </button>
  {/if}

  <!-- Trapped State -->
  {#if status === 'trapped'}
    <div class="flex flex-col items-center justify-center text-red-300 space-y-4">
      <div class="relative w-48 h-48">
        <div class="absolute inset-0 bg-red-500/50 rounded-full animate-pulse"></div>
        <div class="w-full h-full flex items-center justify-center">
          <svg class="w-16 h-16" fill="currentColor" viewBox="0 0 20 20"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM9 4a1 1 0 112 0v5a1 1 0 11-2 0V4zm1 11a1 1 0 100-2 1 1 0 000 2z" clip-rule="evenodd"></path></svg>
        </div>
      </div>
      <h1 class="text-3xl font-bold tracking-wider text-white animate-pulse">SOS BROADCASTING</h1>
      <p class="text-red-400/80">BLE Signal Active</p>
    </div>
  {/if}

  <audio src="data:audio/mpeg;base64,SUQzBAAAAAAAI1RTU0UAAAAPAAADTGF2ZjU4LjQ1LjE0MAAAAAAAAAAAAAAA//tAwAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA/+MYxAAAAANIAAAAAExBTUUzLjEwMFVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV/+MYxDsAAANIAAAAAFVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV/+MYxHYAAANIAAAAAFVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVV" bind:this={beep} loop></audio>
</div>