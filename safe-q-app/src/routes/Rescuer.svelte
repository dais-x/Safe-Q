<script>
  import RescuerUI from '../components/RescuerUI.svelte';
  
  let bleDevices = [
    { name: 'Victim Beacon 1', rssi: -60 },
    { name: 'Victim Beacon 2', rssi: -75 },
  ];

  let nanDevices = [
    { name: 'Victim Phone 1', distance: 10 },
    { name: 'Victim Phone 2', distance: 25 },
  ];

</script>

<RescuerUI {bleDevices} {nanDevices} />