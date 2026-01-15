<script>
  import { onMount, onDestroy } from 'svelte';
  import { App } from '@capacitor/app';
  import { BleClient, ScanMode } from '@capacitor-community/bluetooth-le';

  export let navigate;

  let bleDevices = [];
  let isScanning = false;
  let error = null;
  let appReadyListener;

  async function startBleScan() {
    try {
      error = null;
      isScanning = true;
      bleDevices = [];

      console.log('Attempting to initialize BleClient...');
      await BleClient.initialize();
      console.log('BleClient initialized.');
      
      // Log the BleClient object to inspect its properties in the console
      console.log('BleClient object:', BleClient);

      console.log('Starting BLE scan...');
      await BleClient.startLEScan(
        {
          scanMode: ScanMode.SCAN_MODE_LOW_LATENCY,
          services: [],
        },
        (result) => {
          if (result && result.device) {
            const existingDeviceIndex = bleDevices.findIndex(d => d.device.deviceId === result.device.deviceId);
            if (existingDeviceIndex > -1) {
              bleDevices[existingDeviceIndex] = result;
              bleDevices = [...bleDevices];
            } else {
              bleDevices = [...bleDevices, result];
            }
          }
        }
      );

      // Stop scanning after a predefined time
      setTimeout(async () => {
        await stopBleScan();
      }, 15000);

    } catch (err) {
      console.error('BLE Scan Error:', err);
      if (err.message.includes('BLUETOOTH_SCAN')) {
        error = 'Bluetooth permission was denied. Please grant permission in app settings.';
      } else if (err.message.includes('enabled')) {
        error = 'Bluetooth is not enabled. Please turn on Bluetooth.';
      } else {
        error = `Error: ${err.message}`;
      }
      isScanning = false;
    }
  }

  async function stopBleScan() {
    try {
      await BleClient.stopLEScan();
      console.log('BLE scan stopped.');
    } catch (err) {
      console.error('Error stopping scan:', err);
    }
    isScanning = false;
  }

  onMount(() => {
    // Wait for the Capacitor app to be fully ready before using plugins
    appReadyListener = App.addListener('appReady', () => {
      console.log('App is ready. Starting BLE scan.');
      startBleScan();
    });
  });

  onDestroy(async () => {
    if (appReadyListener) {
      await appReadyListener.remove();
    }
    await stopBleScan();
  });

</script>

<div class="w-full">
  <div class="w-full px-6 flex justify-between items-center border-b border-gray-200 pb-3">
    <div>
      <h1 class="text-xl font-bold text-gray-800">RESCUE SCANNER</h1>
      <p class="text-xs text-gray-500">{isScanning ? 'Scanning for devices...' : 'Scan complete.'}</p>
    </div>
    <button on:click={startBleScan} disabled={isScanning} class="btn btn-rescuer text-sm py-2 px-4">
      Rescan
    </button>
  </div>

  {#if error}
    <div class="p-4 m-4 bg-red-100 text-red-700 rounded-lg">
      <p><strong>Error:</strong> {error}</p>
    </div>
  {/if}

  <div class="w-full px-6 mt-6">
    <h2 class="text-lg font-bold text-gray-800">BLE Devices Discovered</h2>
    <ul class="mt-2 space-y-2">
      {#each bleDevices.sort((a, b) => b.rssi - a.rssi) as device (device.device.deviceId)}
        <li class="p-4 bg-white rounded-lg border border-gray-200">
          <div class="flex justify-between items-center">
            <span class="font-bold text-gray-800">{device.device.name || 'Unnamed Device'}</span>
            <span class="text-sm font-bold text-gray-800">{device.rssi} dBm</span>
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
