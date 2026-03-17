<script setup>
import { ref, onMounted, onUnmounted, nextTick, watch } from 'vue'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'
import 'highlight.js/styles/github.css'

const isAssignment1Visible = ref(false)
const isAssignment2Visible = ref(false)
const isAssignment3Visible = ref(false)
const isBackendLogicVisible = ref(false)
const isFrontendLogicVisible = ref(false)
const isDarkMode = ref(false)

function toggleAssignment1() {
  isAssignment1Visible.value = !isAssignment1Visible.value
}

function toggleAssignment2() {
  isAssignment2Visible.value = !isAssignment2Visible.value
}

function toggleAssignment3() {
  isAssignment3Visible.value = !isAssignment3Visible.value
}

function toggleBackendLogic() {
  isBackendLogicVisible.value = !isBackendLogicVisible.value
}

function toggleFrontendLogic() {
  isFrontendLogicVisible.value = !isFrontendLogicVisible.value
}

const updateTheme = (e) => {
  isDarkMode.value = e.matches
}

onMounted(() => {
  const darkModeMediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  isDarkMode.value = darkModeMediaQuery.matches
  darkModeMediaQuery.addEventListener('change', updateTheme)
  highlightAll()
})

onUnmounted(() => {
  const darkModeMediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  darkModeMediaQuery.removeEventListener('change', updateTheme)
})

function highlightAll() {
  nextTick(() => {
    document.querySelectorAll('pre code').forEach((block) => {
      hljs.highlightElement(block)
    })
  })
}

watch(isDarkMode, () => {
  // Clear highlighted flag so hljs can re-highlight
  document.querySelectorAll('pre code').forEach((block) => {
    delete block.dataset.highlighted
  })
  highlightAll()
})

watch([isAssignment2Visible, isAssignment3Visible, isBackendLogicVisible], () => {
  highlightAll()
})
</script>

<template>
  <div class="assignment-container" :class="{ 'dark-theme': isDarkMode }">
    <div class="assignment-content">
      <h1>Workshop Assignments</h1>
      <p class="intro">
        Welcome to the Syntaxis Workshop! This page contains some assignments you can work on to complete the application.
        The goal is to expand the existing client monitoring system by adding more data visualizations and enabling data entry.
      </p>

      <section class="assignment-section">
        <h2 @click="toggleAssignment1" class="collapsible-heading">
          Expand Archetype Mapping
          <span class="toggle-icon">{{ isAssignment1Visible ? '−' : '+' }}</span>
        </h2>

        <div v-if="isAssignment1Visible" class="collapsible-content">
          <p>
            Currently, only the <strong>Body Temperature</strong> archetype is configured to show in the client view.
            Your task is to add mappings for the remaining archetypes so their data can be visualized in both tables and graphs.
          </p>

          <h3>Steps:</h3>
          <ul>
            <li>Mappings should be added for the following archetypes:
              <ul>
                <li><strong>Blood Pressure</strong>: <code>openEHR-EHR-COMPOSITION.blood_pressure_report.v1.0.0</code></li>
                <li><strong>Body Weight</strong>: <code>openEHR-EHR-COMPOSITION.body_weight_report.v1.0.0</code></li>
              </ul>
            </li>
            <li>Open <code>web/src/views/ClientView.vue</code>.</li>
            <li>Add necessary configuration for:</li>
            <ul>
              <li><code>archetypeTableConfig</code>, in order to map the fetched data to the table</li>
              <li><code>getPathsForArchetype</code>, in order to fetch the correct data from the compositions</li>
              <li><code>getSeriesForArchetype</code>, in order to map the fetched data to graph series</li>
              <li><code>getOptionsForArchetype</code>, in order to configure the correct units for the graphs</li>
            </ul>
            <li>You might notice this task is pretty repetitive, can we make it more generic?</li>
          </ul>

          <div class="hint">
            <strong>Hint:</strong> Example <code>rmObject</code>'s can be found in the <strong>network</strong> tab of your browser's developer tools, the database or <code>seed.sql</code>.
          </div>
        </div>
      </section>

      <section class="assignment-section">
        <h2 @click="toggleAssignment2" class="collapsible-heading">
          Implement Composition Creation
          <span class="toggle-icon">{{ isAssignment2Visible ? '−' : '+' }}</span>
        </h2>

        <div v-if="isAssignment2Visible" class="collapsible-content">
          <p>
            In a proper EHR system, users should be able to create new measurements for clients. Currently, our system is still missing this functionality.
            Your goal is to implement this functionality by posting a composition to the backend API. The API should validate if our system supports the given composition and save it to the database.
          </p>

          <h3 @click="toggleFrontendLogic" class="collapsible-heading sub-collapsible">
            Frontend logic:
            <span class="toggle-icon">{{ isFrontendLogicVisible ? '−' : '+' }}</span>
          </h3>

          <div v-if="isFrontendLogicVisible" class="collapsible-content">
            <ul>
              <li>Introduce an "Add Composition" button in the <code>ClientView.vue</code> (or a separate component).</li>
              <li>Create a form to collect measurement data.</li>
              <li>Construct a valid OpenEHR <code>rmObject</code> and send it to the backend.</li>
            </ul>
          </div>

          <h3 @click="toggleBackendLogic" class="collapsible-heading sub-collapsible">
            Backend logic:
            <span class="toggle-icon">{{ isBackendLogicVisible ? '−' : '+' }}</span>
          </h3>

          <div v-if="isBackendLogicVisible" class="collapsible-content">
            <p>
              For validating received data, we can use <code>archie</code> to do some of this for us.
              Because <code>archie</code> can be quite intimidating (trust us), we'll add some examples of how it can be used.
            </p>

            <div class="hint">
              It's possible to validate a composition by parsing the composition from the rm object string and validate it against its archetype.
            </div>

            <div class="code-block">
              <pre><code class="language-java">
public List&lt;RMObjectValidationMessage&gt; validate(String rmObjectJson) throws Exception {
    Composition composition = JacksonUtil.getObjectMapper().readValue(rmObjectJson, Composition.class);
    if (composition.getArchetypeDetails() == null || composition.getArchetypeDetails().getArchetypeId() == null) {
        throw new IllegalArgumentException("Composition is missing archetype details");
    }

    String archetypeId = composition.getArchetypeDetails().getArchetypeId().getFullId();
    OperationalTemplate opt = getOperationalTemplate(archetypeId);

    if (opt == null) {
        throw new IllegalArgumentException("Archetype not found: " + archetypeId);
    }

    var messages = validator.validate(opt, composition);
    return messages;
}
              </code></pre>
            </div>

            <div class="hint">
              In order to validate a parsed composition, a flattened version of a parsed archetype is required. This is called an operational template.
            </div>

            <div class="code-block">
              <pre><code class="language-java">
public OperationalTemplate getOperationalTemplate(String archetypeId) {
    if (operationalTemplates.containsKey(archetypeId)) {
        return operationalTemplates.get(archetypeId);
    }

    Archetype archetype = repository.getArchetype(archetypeId);
    if (archetype != null) {
        var config = FlattenerConfiguration.forOperationalTemplate();
        Flattener flattener = new Flattener(repository, new SimpleMetaModelProvider(new ReferenceModels(ArchieRMInfoLookup.getInstance()), null), config);
        OperationalTemplate opt = (OperationalTemplate) flattener.flatten(archetype);
        return opt;
    }
    return null;
}
              </code></pre>
            </div>

            <div class="hint">
              Archie's flattener uses Archie's archetype repository to find its archetypes. A repository can be instantiated as follows.
            </div>

            <div class="code-block">
              <pre><code class="language-java">
public void loadArchetypes() throws IOException {
    SimpleArchetypeRepository repository = new SimpleArchetypeRepository()

    PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
    Resource[] resources = resolver.getResources("classpath*:archetypes/**/*.adl*");
    ADLParser parser = new ADLParser();
    for (Resource resource : resources) {
        try {
            Archetype archetype = parser.parse(resource.getInputStream());
            if (archetype != null) {
                repository.addArchetype(archetype);
            }
        } catch (Exception e) {
            System.err.println("Failed to parse archetype: " + resource.getFilename() + " " + e.getMessage());
        }
    }
}
              </code></pre>
            </div>
          </div>
        </div>
      </section>
      <section class="assignment-section">
        <h2 @click="toggleAssignment3" class="collapsible-heading">
          Archetype Summary
          <span class="toggle-icon">{{ isAssignment3Visible ? '−' : '+' }}</span>
        </h2>

        <div v-if="isAssignment3Visible" class="collapsible-content">
          <p>
            To provide a quick summary for each archetype, we want to add an overview section.
            Your task is to create a new component that displays some interesting information about the measurements for an archetype.
          </p>

          <h3>Steps:</h3>
          <ul>
            <li>Create a new component to show the overview.</li>
            <li>Implement the structure of the overview yourself.</li>
            <li>Examples of information to add:
              <ul>
                <li>The <strong>total amount</strong> of measurements.</li>
                <li>The date of the <strong>latest</strong> measurement.</li>
                <li>The <strong>average</strong> value of the measurements (for numeric values like temperature or weight).</li>
                <li>The <strong>maximum</strong> and <strong>minimum</strong> values recorded.</li>
              </ul>
            </li>
          </ul>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.assignment-container {
  padding: 2rem;
  max-width: 800px;
  margin: 0 auto;
  line-height: 1.6;
}

.assignment-container.dark-theme {
}

.code-block {
  background-color: #f6f8fa;
}

.dark-theme .code-block {
  background-color: #0d1117;
}

.assignment-content {
  background-color: var(--color-background-soft);
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
}

h1 {
  color: var(--color-heading);
  margin-bottom: 1.5rem;
  border-bottom: 2px solid var(--color-border);
  padding-bottom: 0.5rem;
}

h2 {
  color: var(--vt-c-indigo);
  margin-top: 2rem;
  margin-bottom: 0; /* Remove margin bottom when used as collapsible heading */
}

.dark-theme h2 {
  color: #64b5f6; /* Lighter blue/indigo for dark mode readability */
}

h3 {
  margin-top: 1.5rem;
  margin-bottom: 0.5rem;
}

.dark-theme h3 {
  color: #e0e0e0;
}

.sub-collapsible {
  margin-top: 1rem;
}

.collapsible-heading {
  cursor: pointer;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem;
  background-color: var(--color-background-soft);
  border: 1px solid var(--color-border);
  border-radius: 4px;
  transition: background-color 0.2s;
}

.collapsible-heading:hover {
  background-color: var(--color-background-mute);
}

.toggle-icon {
  font-family: monospace;
  font-weight: bold;
  font-size: 1.2rem;
}

.collapsible-content {
  padding: 1rem;
  border: 1px solid var(--color-border);
  border-top: none;
  border-bottom-left-radius: 4px;
  border-bottom-right-radius: 4px;
  margin-bottom: 1rem;
}

.intro {
  font-size: 1.1rem;
  margin-bottom: 2rem;
}

.assignment-section {
  margin-bottom: 3rem;
}

ul {
  margin-bottom: 1rem;
  padding-left: 1.5rem;
}

li {
  margin-bottom: 0.5rem;
}

.hint {
  background-color: rgba(52, 152, 219, 0.1);
  border-left: 4px solid #3498db;
  padding: 1rem;
  margin-top: 1rem;
  font-style: italic;
}

.code-block {
  padding: 1rem;
  border-radius: 4px;
  overflow-x: auto;
  margin: 1rem 0;
  border: 1px solid var(--color-border);
}

.code-block pre {
  margin: 0;
}

.code-block code {
  white-space: pre;
  background: transparent !important;
  padding: 0 !important;
}

/* Light mode (default) - only apply github.css styles */
.assignment-container:not(.dark-theme) .hljs {
  background: transparent;
  color: #24292e;
}

/* Dark mode - override with github-dark.css styles */
.dark-theme .hljs {
  background: transparent;
  color: #f0f6fc;
}

/* We need to be careful with specificity here since hljs styles might be more specific */
.dark-theme .hljs-keyword,
.dark-theme .hljs-operator,
.dark-theme .hljs-punctuation,
.dark-theme .hljs-attr { color: #ff7b72; }
.dark-theme .hljs-string { color: #a5d6ff; }
.dark-theme .hljs-comment { color: #8b949e; }
.dark-theme .hljs-function,
.dark-theme .hljs-title.function_ { color: #d2a8ff; }
.dark-theme .hljs-variable,
.dark-theme .hljs-params { color: #ffa657; }
.dark-theme .hljs-number,
.dark-theme .hljs-literal { color: #79c0ff; }
.dark-theme .hljs-class,
.dark-theme .hljs-title.class_ { color: #ffa657; }
.dark-theme .hljs-built_in { color: #79c0ff; }

@media (max-width: 768px) {
  .assignment-container {
    padding: 1rem;
  }
}
</style>
