<script>
  import { Link } from "svelte-routing";
</script>

<div class="h-screen w-full bg-slate-900 text-white font-sans flex flex-col items-center justify-center p-4">
  <div class="text-center mb-12">
    <h1 class="text-5xl font-bold text-cyan-300">Safe-Q</h1>
    <p class="text-cyan-400/70 mt-2">Your Safety, Connected.</p>
  </div>

  <div class="w-full max-w-sm mx-auto space-y-6">
    <!-- Victim Mode Card -->
    <Link to="/victim" class="no-underline block p-6 bg-slate-800 rounded-lg border border-slate-700 hover:border-cyan-400 hover:bg-slate-700 transition-all duration-300">
      <h2 class="text-2xl font-bold text-white">Victim Mode</h2>
      <p class="text-slate-400 mt-2">Activate distress signal and motion detection after a potential disaster.</p>
    </Link>

    <!-- Rescuer Mode Card -->
    <Link to="/rescuer" class="no-underline block p-6 bg-slate-800 rounded-lg border border-slate-700 hover:border-cyan-400 hover:bg-slate-700 transition-all duration-300">
      <h2 class="text-2xl font-bold text-white">Rescuer Mode</h2>
      <p class="text-slate-400 mt-2">Scan the area for nearby distress signals from victims.</p>
    </Link>
  </div>
</div>