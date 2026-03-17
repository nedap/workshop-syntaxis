<script setup>
import { ref, onMounted } from 'vue'

const clients = ref([])

onMounted(async () => {
  // Assuming there will be an API endpoint at /api/clients
  // For now, if it fails, we can show some mock data or an error
  try {
    const response = await fetch('/api/clients')
    if (response.ok) {
      clients.value = await response.json()
    } else {
      console.error('Failed to fetch clients')
    }
  } catch (error) {
    console.error('Error fetching clients:', error)
  }
})
</script>

<template>
  <div class="clients">
    <h1>Clients</h1>
    <ul v-if="clients.length">
      <li v-for="client in clients" :key="client.id">
        <a :href="`/client/${client.id}`">{{ client.firstName }} {{ client.lastName }}</a>
      </li>
    </ul>
    <p v-else>Loading clients...</p>
  </div>
</template>

<style scoped>
@media (min-width: 1024px) {
  .clients {
    max-height: 100vh;
    display: flex;
    flex-direction: column;
    justify-content: center;
  }
}

ul {
  list-style-type: none;
  padding: 0;
  max-height: 500px;
  overflow: scroll;
}

li {
  padding: 0.5rem 0;
  border-bottom: 1px solid var(--color-border);
}
</style>
