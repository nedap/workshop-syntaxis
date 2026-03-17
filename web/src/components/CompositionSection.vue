<script setup>
import CompositionGraph from './CompositionGraph.vue'
import CompositionTable from './CompositionTable.vue'

defineProps({
  archetypeCompositionsPair: {
    type: Object,
    required: true
  },
  tableColumns: {
    type: Array,
    required: true
  },
  graphOptions: {
    type: Object,
    required: true
  },
  graphSeries: {
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
  <details class="composition-section">
    <summary class="composition-summary">
      <h2 class="composition-title">{{ archetypeCompositionsPair.name }}</h2>
    </summary>
    <details class="subsection">
      <summary class="subsection-summary">Graph</summary>
      <CompositionGraph
        :options="graphOptions"
        :series="graphSeries"
      />
    </details>
    <details class="subsection">
      <summary class="subsection-summary">Compositions</summary>
      <CompositionTable
        :columns="tableColumns"
        :compositions="archetypeCompositionsPair.compositions"
        :getFetchedValue="getFetchedValue"
        :formatDateValue="formatDateValue"
      />
    </details>
  </details>
</template>

<style scoped>
.composition-summary {
  cursor: pointer;
}

.composition-title {
  display: inline-block;
  vertical-align: middle;
  margin: 0;
}

.subsection {
  margin-top: 0.5rem;
}

.subsection-summary {
  cursor: pointer;
  font-weight: 600;
  margin: 0.25rem 0 0.25rem 0.5rem;
  display: list-item;
}
</style>
