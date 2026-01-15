<script>
  import { onMount, onDestroy } from 'svelte';
  import { App } from '@capacitor/app';
  import { BleClient, ScanMode } from '@capacitor-community/bluetooth-le';
  import WifiAware from '../plugins/wifi-aware';

  export let navigate;

  let bleDevices = [];
  let nanDevices = [];
  let isScanning = false;
  let error = null;
  let appReadyListener;
  let nanListener;

  async function startScans() {
    // Start both BLE and NAN scans
    startBleScan();
    startNanScan();
  }

  async function startBleScan() {
    try {
      isScanning = true;
      error = null;
      bleDevices = [];

      await BleClient.initialize();
      
      console.log('Starting BLE scan...');
      await BleClient.startLEScan({ services: [] }, (result) => {
        if (result && result.device) {
          if (!bleDevices.some(d => d.device.deviceId === result.device.deviceId)) {
            bleDevices = [...bleDevices, result];
          }
        }
      });

      setTimeout(async () => await BleClient.stopLEScan(), 15000);

    } catch (err) {
      console.error('BLE Scan Error:', err);
      error = err.message;
      isScanning = false;
    }
  }

  async function startNanScan() {
    try {
      nanDevices = [];
      console.log('Starting NAN scan...');
      
      nanListener = await WifiAware.addListener('serviceDiscovered', (res) => {
        console.log('NAN Service Discovered:', res);
        const newDevice = { name: 'Victim (NAN)', id: res.peerHandle };
        if (!nanDevices.some(d => d.id === newDevice.id)) {
            nanDevices = [...nanDevices, newDevice];
        }
      });

      await WifiAware.startSubscribing();
      console.log('NAN Subscribing service started.');

    } catch (err) {
      console.error('NAN Scan Error:', err);
      // Optionally update the UI to show NAN error
    }
  }

  async function stopScans() {
    try {
      await BleClient.stopLEScan();
    } catch (err) {}
    try {
      await WifiAware.stop();
      if (nanListener) await nanListener.remove();
    } catch (err) {}
    isScanning = false;
    console.log('All scans stopped.');
  }

  onMount(() => {
    appReadyListener = App.addListener('appReady', () => {
      console.log('App is ready. Starting scans.');
      startScans();
    });
  });

  onDestroy(async () => {
    if (appReadyListener) await appReadyListener.remove();
    await stopScans();
  });

</script>

<div class="w-full">
  <div class="w-full px-6 flex justify-between items-center border-b border-gray-200 pb-3">
    <div>
      <h1 class="text-xl font-bold text-gray-800">RESCUE SCANNER</h1>
      <p class="text-xs text-gray-500">{isScanning ? 'Scanning for devices...' : 'Scans complete.'}</p>
    </div>
    <button on:click={startScans} disabled={isScanning} class="btn btn-rescuer text-sm py-2 px-4">
      Rescan
    </button>
  </div>

  {#if error}
    <div class="p-4 m-4 bg-red-100 text-red-700 rounded-lg">
      <p><strong>Error:</strong> {error}</p>
    </div>
  {/if}

  <div class="w-full px-6 mt-6">
    <h2 class="text-lg font-bold text-gray-800">NAN Devices Discovered</h2>
    <ul class="mt-2 space-y-2">
      {#each nanDevices as device (device.id)}
        <li class="p-4 bg-white rounded-lg border border-green-300">
          <div class="flex justify-between items-center">
            <span class="font-bold text-green-700">{device.name}</span>
          </div>
          <p class="text-xs text-gray-500">{device.id}</p>
        </li>
      {:else}
        <li class="text-center text-gray-500 py-4">{isScanning ? 'Searching...' : 'No NAN devices found.'}</li>
      {/each}
    </ul>
  </div>

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
        <li class="text-center text-gray-500 py-4">{isScanning ? 'Searching...' : 'No BLE devices found.'}</li>
      {/each}
    </ul>
  </div>

  <div class="mt-10">
      <button on:click={() => navigate('home')} class="text-gray-500">Back to Home</button>
  </div>
</div>