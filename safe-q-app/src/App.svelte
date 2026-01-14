<script>
  import { Router, Route, Link } from "svelte-routing";
  import Home from "./routes/Home.svelte";
  import Victim from "./routes/Victim.svelte";
  import Rescuer from "./routes/Rescuer.svelte";

  export let url = "";
</script>

<Router {url}>
  <nav>
    <Link to="/">Home</Link>
    <Link to="victim">Victim</Link>
    <Link to="rescuer">Rescuer</Link>
  </nav>
  <div>
    <Route path="victim" component="{Victim}" />
    <Route path="rescuer" component="{Rescuer}" />
    <Route path="/" component="{Home}" />
  </div>
</Router>

<style>
  :global(body) {
    font-family: Arial, sans-serif;
    margin: 0;
    padding: 0;
    background-color: #f0f0f0;
  }

  nav {
    display: flex;
    justify-content: center;
    background-color: #333;
    padding: 1rem;
  }

  nav a {
    color: white;
    margin: 0 1rem;
    text-decoration: none;
    font-weight: bold;
    transition: color 0.3s;
  }

  nav a:hover {
    color: #4caf50;
  }

  div {
    padding: 2rem;
    text-align: center;
  }
</style>