<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { useRoute } from 'vue-router'
import ClientHeader from '../components/ClientHeader.vue'
import CompositionSection from '../components/CompositionSection.vue'

const route = useRoute()

const isDarkMode = ref(false)

const client = ref(null)
const compositions = ref([])
const archetypeCompositionMap = ref([])
const compositionValuesMap = ref({})

const archetypeTableConfig = {
  'openEHR-EHR-COMPOSITION.body_temperature_report.v1.0.0': {
    columns: [
      { key: 'id', label: 'ID', path: 'id' },
      { key: 'temperature', label: 'Temperature', path: '/content[id0.0.2]/data[id3]/events[id4]/data[id2]/items[id5.1]/value/magnitude' },
      { key: 'date', label: 'Date', path: '/content[id0.0.2]/data[id3]/events[id4]/time/value' },
    ]
  },
}

/**
 * Helper to check if an archetype has both table and graph configurations
 * @param archetypeId The ID of the archetype
 * @returns {boolean} True if both table and graph configurations are present
 */
function isConfigured(archetypeId) {
  // Now that we de-genericized the graph config, we check if it's handled in getSeriesForArchetype
  const hasTableConfig = !!archetypeTableConfig[archetypeId]
  const hasSeries = getSeriesForArchetype({ archetypeId, compositions: [] }).length > 0
  return hasTableConfig || hasSeries
}

const filteredArchetypeCompositionMap = computed(() => {
  return archetypeCompositionMap.value.filter((pair) => isConfigured(pair.archetypeId))
})

/* SECTION: General helpers */

/**
 * Helper for formatting date values
 * @param value The value to format
 * @returns {string} A formatted date string
 */
function formatDateValue(value) {
  if (!value) return ''
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  return d.toLocaleString('nl', {
    year: 'numeric',
    month: 'long',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
  })
}

/**
 * Helper for getting value from compositionValuesMap
 * @param compositionId The composition id
 * @param path The path of the value that should be returned
 * @returns The value
 */
function getFetchedValue(compositionId, path) {
  if (path === 'id') return compositionId
  return compositionValuesMap.value[compositionId]?.[path]?.[0]
}

/**
 * Helper method for parsing a JsonComposition to a structure we use
 * @param data Raw data fetched from the composition API
 * @returns A parsed composition object with relevant fields
 */
function parseCompositions(data) {
  return data.map((composition) => ({
    id: composition.id,
    clientId: composition.clientId,
    rmObject: JSON.parse(composition.rmObject),
  }))
}

/**
 * Helper method for retrieving paths of values we want to display
 * @param archetypeId The ID of the archetype
 * @returns {Set<string>} A set of paths we want to fetch values of
 */
function getPathsForArchetype(archetypeId) {
  const paths = new Set(['/content/data/events/time/value'])

  const tableConfig = archetypeTableConfig[archetypeId]
  if (tableConfig) {
    tableConfig.columns.forEach((col) => {
      if (col.path !== 'id') paths.add(col.path)
    })
  }

  // Hardcoded paths for Body Temperature
  if (archetypeId === 'openEHR-EHR-COMPOSITION.body_temperature_report.v1.0.0') {
    paths.add('/content[id0.0.2]/data[id3]/events[id4]/data[id2]/items[id5.1]/value/magnitude')
    paths.add('/content[id0.0.2]/data[id3]/events[id4]/data[id2]/items[id5.1]/value/units')
  }

  return paths
}

/**
 * Helper method for retrieving all requested paths for a set of compositions
 * @param compositions The compositions to fetch values for
 * @returns A set of paths to fetch values for
 */
function getAllRequestedPaths(compositions) {
  return compositions.reduce((paths, composition) => {
    const archetypeId = composition.rmObject.archetype_details.archetype_id.value
    getPathsForArchetype(archetypeId).forEach((path) => paths.add(path))
    return paths
  }, new Set())
}

/**
 * Helper method for grouping compositions by archetype
 * @param compositions The compositions to group
 * @returns An array of archetype groups with compositions
 */
function groupCompositionsByArchetype(compositions) {
  return Object.values(
      compositions.reduce((acc, composition) => {
        const archetypeId = composition.rmObject.archetype_details.archetype_id.value

        if (!acc[archetypeId]) {
          acc[archetypeId] = {
            archetypeId,
            name: composition.rmObject.name.value,
            compositions: [],
          }
        }

        acc[archetypeId].compositions.push(composition)
        return acc
      }, {})
  )
}

/* SECTION: Graph configuration */

/**
 * Gets the graph series for a body temperature archetype
 * @param archetypeCompositionsPair The archetype compositions pair
 * @returns An array of graph data points
 */
function getBodyTemperatureSeries(archetypeCompositionsPair) {
  const path = '/content[id0.0.2]/data[id3]/events[id4]/data[id2]/items[id5.1]/value/magnitude'
  const points = archetypeCompositionsPair.compositions
      .map((composition) => {
        const time = getFetchedValue(composition.id, '/content/data/events/time/value')
        const val = getFetchedValue(composition.id, path)
        if (!time || val == null) return null
        return { x: new Date(time).getTime(), y: Number(val) }
      })
      .filter(Boolean)
      .sort((a, b) => a.x - b.x)

  return [{
    name: 'Temperature',
    data: points,
  }]
}

/**
 * Gets the graph series of an archetype
 * @param archetypeCompositionsPair The archetype compositions pair
 * @returns An array of graph data points
 */
function getSeriesForArchetype(archetypeCompositionsPair) {
  const archetypeId = archetypeCompositionsPair.archetypeId

  if (archetypeId === 'openEHR-EHR-COMPOSITION.body_temperature_report.v1.0.0') {
    return getBodyTemperatureSeries(archetypeCompositionsPair)
  }

  return []
}

/**
 * Gets the graph options for an archetype
 * @param archetypeCompositionsPair The archetype compositions pair
 * @returns The configuration options for the archetype graph
 */
function getOptionsForArchetype(archetypeCompositionsPair) {
  const archetypeId = archetypeCompositionsPair.archetypeId
  let units = ''

  if (archetypeId === 'openEHR-EHR-COMPOSITION.body_temperature_report.v1.0.0') {
    const unitsPath = '/content[id0.0.2]/data[id3]/events[id4]/data[id2]/items[id5.1]/value/units'
    const first = archetypeCompositionsPair.compositions?.[0]
    units = first ? getFetchedValue(first.id, unitsPath) : ''
  }

  return {
    chart: {
      id: `chart-${archetypeId}`,
      toolbar: { show: false },
      animations: { enabled: true },
    },
    stroke: { curve: 'smooth', width: 2 },
    dataLabels: { enabled: false },
    markers: { size: 3 },
    xaxis: { type: 'datetime' },
    yaxis: {
      labels: {
        formatter: (val) => (val == null ? '' : `${val}`),
      },
      title: { text: units },
    },
    theme: { mode: isDarkMode.value ? 'dark' : 'light' },
    tooltip: { x: { format: 'dd MMM yyyy HH:mm' } },
  }
}

/* SECTION: Requests */

/**
 * Async method for fetching the values for given compositions
 * @param compositions The compositions to fetch values for
 * @returns A map of composition IDs to their fetched values
 */
async function fetchCompositionValues(compositions) {
  const compositionIds = compositions.map((c) => c.id)
  const paths = getAllRequestedPaths(compositions)

  if (compositionIds.length === 0 || paths.size === 0) return {}

  const queryParams = new URLSearchParams()
  compositionIds.forEach((id) => queryParams.append('ids', id))
  paths.forEach((path) => queryParams.append('paths', path))

  const response = await fetch(
      `/api/compositions/values_for_paths?${queryParams.toString()}`,
      { headers: { 'Content-Type': 'application/json' } }
  )

  if (!response.ok) {
    throw new Error('Failed to fetch composition values')
  }

  const batchValuesData = await response.json()

  return Object.fromEntries(
      Object.entries(batchValuesData).map(([compositionId, valuesData]) => [
        compositionId,
        Object.fromEntries(
            valuesData.valuesForPath.map((entry) => [entry.path, entry.values])
        ),
      ])
  )
}

/**
 * Async method for fetching client details by ID
 * @param clientId The ID of the client to fetch
 * @returns The client details
 */
async function fetchClient(clientId) {
  const response = await fetch(`/api/clients/${clientId}`)
  if (!response.ok) {
    throw new Error('Failed to fetch client details')
  }
  return response.json()
}

/**
 * Async method for fetching client compositions
 * @param clientId The ID of the client to fetch compositions for
 * @returns The client compositions
 */
async function fetchClientCompositions(clientId) {
  const response = await fetch(`/api/compositions/by_client_id/${clientId}`)
  if (!response.ok) {
    throw new Error('Failed to fetch client compositions')
  }
  return response.json()
}

/* SECTION: Client view */

/**
 * Helper method for initializing client view
 */
onMounted(async () => {
  const darkModeMediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  isDarkMode.value = darkModeMediaQuery.matches
  darkModeMediaQuery.addEventListener('change', updateTheme)

  const clientId = String(route.params.clientId ?? '')
  if (!clientId) {
    console.error('Missing client ID in route parameters')
    return
  }

  try {
    client.value = await fetchClient(clientId)
  } catch (error) {
    console.error('Error fetching client details:', error)
  }

  try {
    const compositionData = await fetchClientCompositions(clientId)
    compositions.value = parseCompositions(compositionData)
    compositionValuesMap.value = await fetchCompositionValues(compositions.value)
    archetypeCompositionMap.value = groupCompositionsByArchetype(compositions.value)
  } catch (error) {
    console.error('Error fetching client compositions:', error)
  }
})

/**
 * Helper method for cleaning up client view
 */
onUnmounted(() => {
  const darkModeMediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  darkModeMediaQuery.removeEventListener('change', updateTheme)
})

const updateTheme = (e) => {
  isDarkMode.value = e.matches
}

</script>

<template>
  <div class="client-details" v-if="client">
    <ClientHeader :client="client" />
    <p v-if="filteredArchetypeCompositionMap.length === 0">No compositions found for this client.</p>
    <CompositionSection
      v-for="archetypeCompositionsPair in filteredArchetypeCompositionMap"
      :key="archetypeCompositionsPair.archetypeId"
      :archetypeCompositionsPair="archetypeCompositionsPair"
      :tableColumns="archetypeTableConfig[archetypeCompositionsPair.archetypeId]?.columns || []"
      :graphOptions="getOptionsForArchetype(archetypeCompositionsPair)"
      :graphSeries="getSeriesForArchetype(archetypeCompositionsPair)"
      :getFetchedValue="getFetchedValue"
      :formatDateValue="formatDateValue"
    />
  </div>
  <p v-else>Loading client details...</p>
</template>

<style scoped>
</style>
