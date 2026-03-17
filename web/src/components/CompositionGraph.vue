<script setup>
import VueApexCharts from 'vue3-apexcharts'

defineProps({
  options: {
    type: Object,
    required: true
  },
  series: {
    type: Array,
    required: true
  }
})
</script>

<template>
  <VueApexCharts
    type="line"
    height="250"
    :options="options"
    :series="series"
  />
</template>
