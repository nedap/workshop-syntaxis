<script setup>
defineProps({
  columns: {
    type: Array,
    required: true
  },
  compositions: {
    type: Array,
    required: true
  },
  getFetchedValue: {
    type: Function,
    required: true
  },
  formatDateValue: {
    type: Function,
    required: true
  }
})
</script>

<template>
  <table class="composition-table">
    <thead>
      <tr>
        <th v-for="col in columns" :key="col.key">
          {{ col.label }}
        </th>
      </tr>
    </thead>
    <tbody>
      <tr v-for="composition in compositions" :key="composition.id">
        <td v-for="col in columns" :key="col.key">
          <span v-if="col.key === 'date'">
            {{ formatDateValue(getFetchedValue(composition.id, col.path)) }}
          </span>
          <span v-else>
            {{ getFetchedValue(composition.id, col.path) }}
          </span>
        </td>
      </tr>
    </tbody>
  </table>
</template>

<style scoped>
.composition-table {
  width: 100%;
  border-collapse: collapse;
  margin-left: 0.5rem;
  margin-bottom: 2rem;
}

.composition-table th,
.composition-table td {
  border: 1px solid #ddd;
  padding: 0.5rem;
  text-align: left;
}

.composition-table th {
  font-weight: 600;
}
</style>
