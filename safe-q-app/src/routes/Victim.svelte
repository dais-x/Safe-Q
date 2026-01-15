<script>
	import { onMount, onDestroy } from 'svelte';
	import { App } from '@capacitor/app';
	import { KeepAwake } from '@capacitor-community/keep-awake';
	import { Geolocation } from '@capacitor/geolocation';

	export let navigate;

	// --- App State ---
	let sensorData = {
		accel: { x: 0, y: 0, z: 0 },
		gyro: { alpha: 0, beta: 0, gamma: 0 },
		motion: 0
	};
	let isAlarmActive = false;
	
	// --- Audio State ---
	let audioContext = null;
	let oscillator = null;
	let pulseInterval = null;
	let beepTimeout = null;
	let emergencyAudio = null;
	let playCount = 0;

	// --- Constants ---
	const FALL_THRESHOLD = 30; // ~3g impact (m/s^2)
	const WEBHOOK_URL = 'https://example.com/webhook'; // Placeholder URL

	// --- Lifecycle Hooks ---
	onMount(async () => {
		const link = document.createElement('link');
		link.href = 'https://fonts.googleapis.com/css2?family=Fira+Code:wght@300;400;500&display=swap';
		link.rel = 'stylesheet';
		document.head.appendChild(link);

		startSensors();
		try {
			await KeepAwake.keepAwake();
		} catch (e) {
			console.error("KeepAwake failed", e);
		}
	});

	onDestroy(async () => {
		stopSensors();
		stopAlarm();
		try {
			await KeepAwake.allowSleep();
		} catch (e) {}
	});

	// --- Sensor Handling ---
	function startSensors() {
		if (typeof window !== 'undefined' && window.DeviceMotionEvent) {
			window.addEventListener('devicemotion', handleMotion, true);
			window.addEventListener('deviceorientation', handleOrientation, true);
		}
	}

	function stopSensors() {
		if (typeof window !== 'undefined' && window.DeviceMotionEvent) {
			window.removeEventListener('devicemotion', handleMotion, true);
			window.removeEventListener('deviceorientation', handleOrientation, true);
		}
	}

	function handleMotion(event) {
		const { x, y, z } = event.accelerationIncludingGravity || { x:0, y:0, z:0 };
		sensorData.accel = { 
			x: (x || 0).toFixed(2), 
			y: (y || 0).toFixed(2), 
			z: (z || 0).toFixed(2) 
		};
		
		const mag = Math.sqrt(x*x + y*y + z*z);
		sensorData.motion = mag.toFixed(2);

		if (mag > FALL_THRESHOLD) {
			triggerAlarm();
		}
	}

	function handleOrientation(event) {
		sensorData.gyro = {
			alpha: (event.alpha || 0).toFixed(1),
			beta: (event.beta || 0).toFixed(1),
			gamma: (event.gamma || 0).toFixed(1)
		};
	}

	// --- Alarm Logic ---
	async function triggerAlarm() {
		if (isAlarmActive) return;
		isAlarmActive = true;
		
		// 2. Play Alarm Sequence
		playHighPitchSound();

		// 3. Send Location Webhook
		try {
			const coordinates = await Geolocation.getCurrentPosition({
				enableHighAccuracy: true,
				timeout: 10000
			});

			const { latitude, longitude } = coordinates.coords;
			const mapLink = `https://maps.google.com/?q=${latitude},${longitude}`;

			const payload = {
				type: "FALL_DETECTED",
				timestamp: new Date().toISOString(),
				location: mapLink,
				lat: latitude,
				lng: longitude
			};

			await fetch(WEBHOOK_URL, {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify(payload)
			});
			console.log("Location sent:", mapLink);

		} catch (err) {
			console.error("Geolocation/Webhook Error:", err);
		}
	}

	function playHighPitchSound() {
		try {
			const AudioContext = window.AudioContext || window.webkitAudioContext;
			if (!AudioContext) return;

			audioContext = new AudioContext();
			oscillator = audioContext.createOscillator();
			const gainNode = audioContext.createGain();

			oscillator.type = 'square';
			oscillator.frequency.setValueAtTime(3000, audioContext.currentTime);
			
			oscillator.frequency.exponentialRampToValueAtTime(4000, audioContext.currentTime + 0.1);
			oscillator.frequency.exponentialRampToValueAtTime(3000, audioContext.currentTime + 0.2);

			gainNode.gain.setValueAtTime(1, audioContext.currentTime); 

			oscillator.connect(gainNode);
			gainNode.connect(audioContext.destination);

			oscillator.start();
			
			pulseInterval = setInterval(() => {
				if (oscillator && audioContext) {
					const now = audioContext.currentTime;
					oscillator.frequency.setValueAtTime(3000, now);
					oscillator.frequency.linearRampToValueAtTime(4000, now + 0.1);
				}
			}, 200);

			beepTimeout = setTimeout(() => {
				stopOscillator();
				playEmergencyAudio();
			}, 8000);

		} catch (e) {
			console.error("Audio error", e);
		}
	}

	function stopOscillator() {
		if (pulseInterval) {
			clearInterval(pulseInterval);
			pulseInterval = null;
		}
		if (oscillator) {
			try {
				oscillator.stop();
				oscillator.disconnect();
			} catch (e) {}
			oscillator = null;
		}
		if (audioContext) {
			try {
				audioContext.close();
			} catch (e) {}
			audioContext = null;
		}
	}

	function playEmergencyAudio() {
		if (emergencyAudio) return;

		playCount = 0;
		emergencyAudio = new Audio('/emergency_guide.wav');
		
		emergencyAudio.addEventListener('ended', () => {
			playCount++;
			if (playCount < 3) {
				emergencyAudio.currentTime = 0;
				emergencyAudio.play();
			} else {
				emergencyAudio = null;
			}
		});

		emergencyAudio.play().catch(e => console.error("Play failed", e));
	}

	function stopAlarm() {
		isAlarmActive = false;
		
		if (beepTimeout) {
			clearTimeout(beepTimeout);
			beepTimeout = null;
		}
		stopOscillator();

		if (emergencyAudio) {
			emergencyAudio.pause();
			emergencyAudio = null;
		}
		playCount = 0;
	}
</script>

<div class="w-full">
	{#if !isAlarmActive}
		<div class="flex flex-col items-center justify-center text-gray-800 space-y-4">
			<h1 class="text-2xl font-bold tracking-wider">Monitoring for Falls...</h1>
			<p class="text-gray-600">This app is continuously monitoring for sudden impacts.</p>
			<div class="text-xs text-gray-400 font-mono">
				<p>Accel: {sensorData.accel.x}, {sensorData.accel.y}, {sensorData.accel.z}</p>
				<p>Gyro: {sensorData.gyro.alpha}, {sensorData.gyro.beta}, {sensorData.gyro.gamma}</p>
				<p>Motion: {sensorData.motion} m/s²</p>
			</div>
		</div>
	{:else}
		<div class="text-gray-800">
			<h2 class="text-xl font-bold uppercase tracking-widest text-red-600 animate-pulse">FALL DETECTED</h2>
			<p class="mt-4">Broadcasting for help. Do you want to cancel?</p>
		</div>
		<button on:click={stopAlarm} class="btn btn-victim mt-8">
			CANCEL ALARM
		</button>
	{/if}

	<div class="mt-10">
		<button on:click={() => navigate('home')} class="text-gray-500">Back to Home</button>
	</div>
</div>
