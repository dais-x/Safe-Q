<script>
  export let bleDevices = [];
  export let nanDevices = [];
</script>

<div class="h-screen w-full bg-slate-900 text-cyan-300 font-sans overflow-hidden flex flex-col items-center justify-start py-10">
  
  <div class="w-full px-6 flex justify-between items-center border-b border-cyan-900/50 pb-3">
    <div>
      <h1 class="text-xl font-bold text-white">RESCUE SCANNER</h1>
      <p class="text-xs text-cyan-400/70">Searching for nearby devices...</p>
    </div>
  </div>

  <div class="w-full px-6 mt-6">
    <h2 class="text-lg font-bold text-white">BLE Devices</h2>
    <ul class="mt-2 space-y-2">
      {#each bleDevices as device}
        <li class="p-4 bg-slate-800 rounded-lg border border-slate-700">
          <div class="flex justify-between items-center">
            <span class="font-bold">{device.name}</span>
            <span class="text-sm text-cyan-400/80">{device.rssi} dBm</span>
          </div>
        </li>
      {:else}
        <li class="text-center text-gray-500 py-4">No BLE devices found.</li>
      {/each}
    </ul>
  </div>

  <div class="w-full px-6 mt-6">
    <h2 class="text-lg font-bold text-white">NAN Devices</h2>
    <ul class="mt-2 space-y-2">
      {#each nanDevices as device}
        <li class="p-4 bg-slate-800 rounded-lg border border-slate-700">
          <div class="flex justify-between items-center">
            <span class="font-bold">{device.name}</span>
            <span class="text-sm text-cyan-400/80">{device.distance} m</span>
          </div>
        </li>
      {:else}
        <li class="text-center text-gray-500 py-4">No NAN devices found.</li>
      {/each}
    </ul>
  </div>

</div>
