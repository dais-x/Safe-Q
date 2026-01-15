<script>
  import Victim from './routes/Victim.svelte';
  import Rescuer from './routes/Rescuer.svelte';

  let currentView = 'home'; // 'home', 'victim', 'rescuer'

  function navigate(view) {
    currentView = view;
  }
</script>

<main class="app-container">
  {#if currentView === 'home'}
    <h1 class="app-title">SafeQ</h1>

    <div class="button-group">
      <button on:click={() => navigate('victim')} class="btn btn-victim">
        Victim
      </button>
      
      <button on:click={() => navigate('rescuer')} class="btn btn-rescuer">
        Rescuers
      </button>
    </div>
  {:else if currentView === 'victim'}
    <Victim {navigate} />
  {:else if currentView === 'rescuer'}
    <Rescuer {navigate} />
  {/if}
</main>

<style>
  /* I added :global(body) so the background covers the whole page 
     even though this is just one component */
  :global(body) {
    margin: 0;
    padding: 0;
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    background-color: #f8f9fa;
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
  }

  .app-container {
    width: 100%;
    max-width: 400px;
    padding: 20px;
    text-align: center;
  }

  .app-title {
    font-size: 3rem;
    font-weight: 800;
    color: #1a1a1a;
    margin-bottom: 50px;
    letter-spacing: -1px;
  }

  .button-group {
    display: flex;
    flex-direction: column;
    gap: 20px;
  }

  .btn {
    border: none;
    width: 100%;
    padding: 20px;
    border-radius: 16px;
    font-size: 1.2rem;
    font-weight: 600;
    cursor: pointer;
    transition: transform 0.2s, box-shadow 0.2s;
    color: white;
    box-shadow: 0 4px 6px rgba(0,0,0,0.1);
    text-decoration: none;
  }

  .btn:hover {
    transform: translateY(-3px);
    box-shadow: 0 6px 12px rgba(0,0,0,0.15);
  }

  .btn:active {
    transform: scale(0.98);
  }

  .btn-victim {
    background-color: #ff4757;
  }

  .btn-rescuer {
    background-color: #3742fa;
  }
</style>