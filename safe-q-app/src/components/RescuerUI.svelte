<script>
  export let signal = 0; // 0 to 100
</script>

<div class="h-screen w-full bg-black text-green-500 font-mono overflow-hidden relative flex flex-col items-center justify-between py-10">
  
  <div class="w-full px-6 flex justify-between items-end border-b border-green-900 pb-2">
    <div>
      <h1 class="text-2xl font-bold">RESCUE RADAR</h1>
      <p class="text-xs text-green-700">SCANNING FREQ: 2.4GHz (BLE)</p>
    </div>
    <div class="text-right">
      <div class="text-3xl font-bold">{signal}%</div>
      <div class="text-xs">SIGNAL STRENGTH</div>
    </div>
  </div>

  <div class="relative w-[350px] h-[350px] flex items-center justify-center">
    <div class="absolute inset-0 border border-green-900 rounded-full"></div>
    <div class="absolute inset-[50px] border border-green-900 rounded-full"></div>
    <div class="absolute inset-[100px] border border-green-900 rounded-full"></div>
    
    <div class="absolute w-full h-[1px] bg-green-900 top-1/2"></div>
    <div class="absolute h-full w-[1px] bg-green-900 left-1/2"></div>

    <div class="absolute w-full h-full rounded-full animate-spin-slow bg-conic-gradient"></div>

    {#if signal > 0}
      <div class="absolute w-4 h-4 bg-red-500 rounded-full shadow-[0_0_15px_red] animate-ping"
           style="transform: translateY(-{signal * 1.5}px)">
      </div>
    {/if}
  </div>

  <div class="w-full px-6">
    <div class="text-xs text-center text-gray-600 mb-2">USE ARROW KEYS TO SIMULATE SIGNAL</div>
    <div class="w-full h-8 border border-green-800 p-1">
      <div class="h-full bg-green-600 transition-all duration-200" style="width: {signal}%"></div>
    </div>
    <div class="flex justify-between mt-2 text-xs">
      <span>DIST: {signal > 0 ? (100 - signal) / 10 : '--'}m</span>
      <span>STATUS: {signal > 80 ? 'CRITICAL PROXIMITY' : 'SEARCHING'}</span>
    </div>
  </div>

</div>

<style>
  .animate-spin-slow {
    animation: spin 4s linear infinite;
  }
  @keyframes spin {
    from { transform: rotate(0deg); }
    to { transform: rotate(360deg); }
  }
  .bg-conic-gradient {
    background: conic-gradient(from 0deg, transparent 0deg, transparent 270deg, rgba(34, 197, 94, 0.2) 360deg);
  }
</style>
