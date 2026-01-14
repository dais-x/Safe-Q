<script>
  import { Router, Route, Link, useLocation } from "svelte-routing";
  import Home from "./routes/Home.svelte";
  import Victim from "./routes/Victim.svelte";
  import Rescuer from "./routes/Rescuer.svelte";

  export let url = "";
  const location = useLocation();

  let showNav = false;
  $: {
    // The useLocation hook is reactive, so this code will re-run when the path changes.
    // We show the nav on any page that is not the home page.
    showNav = $location.pathname !== '/';
  }
</script>

<Router {url}>
  <main>
    <Route path="victim" component="{Victim}" />
    <Route path="rescuer" component="{Rescuer}" />
    <Route path="/" component="{Home}" />
  </main>

  {#if showNav}
    <nav>
      <Link to="/">Home</Link>
      <Link to="victim">Victim</Link>
      <Link to="rescuer">Rescuer</Link>
    </nav>
  {/if}
</Router>

<style>
  nav {
    position: fixed;
    bottom: 0;
    left: 0;
    width: 100%;
    display: flex;
    justify-content: space-around;
    align-items: center;
    background-color: #0F172A; /* bg-slate-900 */
    border-top: 1px solid #1E293B; /* border-slate-700 */
    z-index: 50;
    height: 60px;
  }

  :global(nav a) {
    color: #94A3B8; /* text-slate-400 */
    text-decoration: none;
    font-family: sans-serif;
    font-weight: bold;
    padding: 0.5rem 1rem;
    border-radius: 0.5rem;
    transition: all 0.2s ease-in-out;
  }

  :global(nav a:hover) {
    color: white;
    background-color: #1E293B; /* bg-slate-700 */
  }
  
  :global(nav a.active) {
    color: #67E8F9; /* text-cyan-300 */
  }

  main {
    /* Only add bottom padding if the nav is showing */
    padding-bottom: var(--nav-height, 0);
  }
</style>

<svelte:head>
  <style>
    /* Use a CSS variable to dynamically set the padding based on nav visibility */
    :root {
      --nav-height: {showNav ? '60px' : '0'};
    }
  </style>
</svelte:head>