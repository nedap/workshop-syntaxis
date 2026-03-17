<script setup>
defineProps({
  client: {
    type: Object,
    required: true
  }
})
</script>

<template>
  <div class="client-header">
    <h1 class="client-name">{{ client.firstName }} {{ client.lastName }}</h1>
  </div>
</template>

<style scoped>
.client-header {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.client-name {
  margin: 0;
}
</style>
