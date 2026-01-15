<script>
  export let navigate;

  let bleDevices = [
    { name: 'Victim Beacon 1', rssi: -60 },
    { name: 'Victim Beacon 2', rssi: -75 },
  ];

  let nanDevices = [
    { name: 'Victim Phone 1', distance: 10 },
    { name: 'Victim Phone 2', distance: 25 },
  ];

</script>

<div class="w-full">
  <div class="w-full px-6 flex justify-between items-center border-b border-gray-200 pb-3">
    <div>
      <h1 class="text-xl font-bold text-gray-800">RESCUE SCANNER</h1>
      <p class="text-xs text-gray-500">Searching for nearby devices...</p>
    </div>
  </div>

  <div class="w-full px-6 mt-6">
    <h2 class="text-lg font-bold text-gray-800">BLE Devices</h2>
    <ul class="mt-2 space-y-2">
      {#each bleDevices as device}
        <li class="p-4 bg-white rounded-lg border border-gray-200">
          <div class="flex justify-between items-center">
            <span class="font-bold text-gray-800">{device.name}</span>
            <span class="text-sm text-gray-600">{device.rssi} dBm</span>
          </div>
        </li>
      {:else}
        <li class="text-center text-gray-500 py-4">No BLE devices found.</li>
      {/each}
    </ul>
  </div>

  <div class="w-full px-6 mt-6">
    <h2 class="text-lg font-bold text-gray-800">NAN Devices</h2>
    <ul class="mt-2 space-y-2">
      {#each nanDevices as device}
        <li class="p-4 bg-white rounded-lg border border-gray-200">
          <div class="flex justify-between items-center">
            <span class="font-bold text-gray-800">{device.name}</span>
            <span class="text-sm text-gray-600">{device.distance} m</span>
          </div>
        </li>
      {:else}
        <li class="text-center text-gray-500 py-4">No NAN devices found.</li>
      {/each}
    </ul>
  </div>

  <div class="mt-10">
      <button on:click={() => navigate('home')} class="text-gray-500">Back to Home</button>
  </div>

</div>