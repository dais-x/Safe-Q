<script>
  export let signal = 0; // 0 to 100
</script>

<div class="h-screen w-full bg-slate-900 text-cyan-300 font-sans overflow-hidden flex flex-col items-center justify-between py-10">
  
  <div class="w-full px-6 flex justify-between items-center border-b border-cyan-900/50 pb-3">
    <div>
      <h1 class="text-xl font-bold text-white">RESCUE SCANNER</h1>
      <p class="text-xs text-cyan-400/70">Searching for BLE Beacons...</p>
    </div>
    <div class="text-right">
      <div class="text-3xl font-bold text-white">{signal}%</div>
      <div class="text-xs text-cyan-400/70">SIGNAL STRENGTH</div>
    </div>
  </div>

  <!-- Radar -->
  <div class="relative w-[300px] h-[300px] flex items-center justify-center">
    <!-- Grid and Circles -->
    <div class="absolute inset-0 border border-cyan-800/50 rounded-full"></div>
    <div class="absolute inset-[25%] border-t border-cyan-800/50 rounded-full"></div>
    <div class="absolute inset-[50%] border-t border-cyan-800/50 rounded-full"></div>
    <div class="absolute w-full h-[1px] bg-gradient-to-r from-transparent via-cyan-900 to-transparent top-1/2"></div>
    <div class="absolute h-full w-[1px] bg-gradient-to-b from-transparent via-cyan-900 to-transparent left-1/2"></div>
    
    <!-- Sweep Animation -->
    <div class="absolute w-full h-full animate-spin-slow">
      <div class="w-full h-full bg-gradient-to-t from-cyan-500/0 to-cyan-500/20 rounded-full"></div>
    </div>
    
    <!-- Signal Dot -->
    {#if signal > 0}
      <div 
        class="absolute w-5 h-5 bg-cyan-400 rounded-full shadow-[0_0_15px_rgba(56,189,248,0.7)] animate-pulse"
        style="opacity: {signal / 100}; transform: translateY(-{signal * 1.1}px) scale({0.5 + signal / 200});"
      >
      </div>
    {/if}
  </div>

  <div class="w-full px-6 space-y-3">
    <div class="text-xs text-center text-gray-500">USE ARROW KEYS TO SIMULATE SIGNAL</div>
    <!-- Progress Bar -->
    <div class="w-full h-2 bg-cyan-900/50 rounded-full overflow-hidden">
      <div class="h-full bg-cyan-400 rounded-full transition-all duration-300" style="width: {signal}%"></div>
    </div>
    <div class="flex justify-between text-sm text-cyan-400/80">
      <span>DIST (EST): {signal > 0 ? ((100 - signal) / 5 + 5).toFixed(1) : '--'}m</span>
      <span>STATUS: {signal > 80 ? 'CRITICAL' : signal > 40 ? 'STRONG' : 'WEAK'}</span>
    </div>
  </div>

</div>

<style>
  .animate-spin-slow {
    animation: spin 3s linear infinite;
  }
  @keyframes spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
  }
</style>
