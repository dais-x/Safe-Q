<script>
  import { onMount, onDestroy } from 'svelte';
  import { BleClient, ScanMode } from '@capacitor-community/bluetooth-le';

  export let navigate;

  let bleDevices = [];
  let nanDevices = []; // Placeholder for future NAN implementation
  let isScanning = false;
  let error = null;

  async function startBleScan() {
    try {
      isScanning = true;
      error = null;
      bleDevices = []; // Clear previous results

      await BleClient.initialize();

      // Android requires location services to be enabled for Bluetooth scanning
      if ((await BleClient.isEnabled()) === false) {
        throw new Error('Bluetooth is not enabled.');
      }
      // You might want to prompt the user to enable it.
      
      // Request permissions
      const hasPermissions = await BleClient.requestLEScan({
        scanMode: ScanMode.SCAN_MODE_LOW_LATENCY,
      });

      // Start scanning
      await BleClient.startLEScan(
        {
          services: [], // Scan for all services
        },
        (result) => {
          // Add discovered devices to the list, avoiding duplicates
          if (!bleDevices.some(d => d.device.deviceId === result.device.deviceId)) {
            bleDevices = [...bleDevices, result];
          }
        }
      );

      // Stop scanning after 10 seconds
      setTimeout(async () => {
        await BleClient.stopLEScan();
        isScanning = false;
      }, 10000);

    } catch (err) {
      error = err.message;
      isScanning = false;
    }
  }

  onMount(() => {
    startBleScan();
  });

  onDestroy(async () => {
    try {
      await BleClient.stopLEScan();
    } catch (err) {
      // Ignore errors on stop
    }
  });

</script>

<div class="w-full">
  <div class="w-full px-6 flex justify-between items-center border-b border-gray-200 pb-3">
    <div>
      <h1 class="text-xl font-bold text-gray-800">RESCUE SCANNER</h1>
      <p class="text-xs text-gray-500">{isScanning ? 'Scanning for devices...' : 'Scan complete.'}</p>
    </div>
  </div>

  {#if error}
    <div class="p-4 m-4 bg-red-100 text-red-700 rounded-lg">
      <p><strong>Error:</strong> {error}</p>
      <p class="text-sm">Please ensure Bluetooth and Location services are enabled.</p>
    </div>
  {/if}

  <div class="w-full px-6 mt-6">
    <h2 class="text-lg font-bold text-gray-800">BLE Devices Discovered</h2>
    <ul class="mt-2 space-y-2">
      {#each bleDevices as device (device.device.deviceId)}
        <li class="p-4 bg-white rounded-lg border border-gray-200">
          <div class="flex justify-between items-center">
            <span class="font-bold text-gray-800">{device.device.name || 'Unnamed Device'}</span>
            <span class="text-sm text-gray-600">{device.rssi} dBm</span>
          </div>
          <p class="text-xs text-gray-500">{device.device.deviceId}</p>
        </li>
      {:else}
        <li class="text-center text-gray-500 py-4">{isScanning ? 'Waiting for devices...' : 'No BLE devices found.'}</li>
      {/each}
    </ul>
  </div>

  <div class="w-full px-6 mt-6">
    <h2 class="text-lg font-bold text-gray-500">NAN Devices (Unavailable)</h2>
    <ul class="mt-2 space-y-2">
      <li class="text-center text-gray-400 py-4">Wi-Fi Aware (NAN) plugin is not available.</li>
    </ul>
  </div>

  <div class="mt-10">
      <button on:click={() => navigate('home')} class="text-gray-500">Back to Home</button>
  </div>
</div>
