<template>
  <div class="monaco-editor-container" ref="containerRef">
    <div ref="editorContainer" class="editor"></div>
    <ContextMenu 
      :visible="contextMenuVisible" 
      :position="contextMenuPosition"
      @close="closeContextMenu"
      @select="handleContextMenuSelect"
    />
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import * as monaco from 'monaco-editor'
import { useThemeStore } from '@/stores/theme'
import ContextMenu from './ContextMenu.vue'

const props = defineProps({
  modelValue: String,
  language: {
    type: String,
    default: 'java'
  },
  height: {
    type: String,
    default: '500px'
  },
  readOnly: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:modelValue', 'ai-action'])

const editorContainer = ref(null)
const containerRef = ref(null)
let editor = null
const themeStore = useThemeStore()

const contextMenuVisible = ref(false)
const contextMenuPosition = ref({ x: 0, y: 0 })

const getTheme = () => {
  return themeStore.isDark ? 'vs-dark' : 'vs'
}

const updateTheme = () => {
  if (editor) {
    const newTheme = getTheme()
    monaco.editor.setTheme(newTheme)
  }
}

const getSelectedCode = () => {
  if (!editor) return ''
  const selection = editor.getSelection()
  if (selection.isEmpty()) return ''
  const model = editor.getModel()
  return model.getValueInRange(selection)
}

const handleContextMenu = (e) => {
  const selectedCode = getSelectedCode()
  if (selectedCode.trim()) {
    e.preventDefault()
    e.stopPropagation()
    contextMenuPosition.value = {
      x: e.clientX,
      y: e.clientY
    }
    contextMenuVisible.value = true
  }
}

const closeContextMenu = () => {
  contextMenuVisible.value = false
}

const handleContextMenuSelect = (item) => {
  const selectedCode = getSelectedCode()
  if (selectedCode) {
    emit('ai-action', {
      action: item.id,
      prompt: item.prompt,
      code: selectedCode
    })
  }
}

onMounted(() => {
  editor = monaco.editor.create(editorContainer.value, {
    value: props.modelValue || '',
    language: props.language,
    theme: getTheme(),
    automaticLayout: true,
    fontSize: 14,
    fontFamily: "'Fira Code', 'Monaco', 'Courier New', monospace",
    minimap: { enabled: true },
    scrollBeyondLastLine: false,
    wordWrap: 'on',
    padding: { top: 16, bottom: 16 },
    lineNumbers: 'on',
    renderLineHighlight: 'all',
    cursorBlinking: 'smooth',
    cursorSmoothCaretAnimation: 'on',
    smoothScrolling: true,
    bracketPairColorization: { enabled: true },
    guides: {
      bracketPairs: true,
      indentation: true
    },
    readOnly: props.readOnly
  })

  editor.onDidChangeModelContent(() => {
    emit('update:modelValue', editor.getValue())
  })

  editorContainer.value.addEventListener('contextmenu', handleContextMenu, true)
})

watch(() => props.language, (newLanguage) => {
  if (editor) {
    monaco.editor.setModelLanguage(editor.getModel(), newLanguage)
  }
})

watch(() => props.modelValue, (newValue) => {
  if (editor && newValue !== editor.getValue()) {
    editor.setValue(newValue)
  }
})

watch(() => themeStore.isDark, () => {
  updateTheme()
})

onBeforeUnmount(() => {
  if (editorContainer.value) {
    editorContainer.value.removeEventListener('contextmenu', handleContextMenu, true)
  }
  if (editor) {
    editor.dispose()
  }
})

const getEditor = () => editor

defineExpose({
  getEditor
})
</script>

<style scoped>
.monaco-editor-container {
  width: 100%;
  height: v-bind(height);
  background: var(--bg-card);
  border-radius: var(--card-radius);
  overflow: hidden;
  border: 1px solid var(--border-color);
  transition: all var(--transition-fast);
}

.monaco-editor-container:hover {
  border-color: var(--brand-primary);
  box-shadow: var(--shadow-md);
}

.editor {
  width: 100%;
  height: 100%;
  border-radius: 0;
  overflow: hidden;
  border: none;
  background: transparent;
}

:deep(.monaco-menu) {
  display: none !important;
}

/* 深色主题下的编辑器样式调整 */
:deep(.monaco-editor.vs-dark) {
  --vscode-editor-background: var(--dark-bg-primary);
  --vscode-editorForeground: var(--dark-text-primary);
  --vscode-editor-selectionBackground: var(--brand-primary-light);
  --vscode-editor-lineHighlightBorder: var(--brand-primary-light);
}

/* 亮色主题下的编辑器样式调整 */
:deep(.monaco-editor.vs) {
  --vscode-editor-background: var(--light-bg-secondary);
  --vscode-editorForeground: var(--light-text-primary);
  --vscode-editor-selectionBackground: var(--brand-primary-light);
  --vscode-editor-lineHighlightBorder: var(--brand-primary-light);
}

/* 编辑器滚动条样式 */
:deep(.monaco-editor .scrollbar) {
  background: var(--bg-tertiary);
}

:deep(.monaco-editor .scrollbar .slider) {
  background: var(--border-color);
}

:deep(.monaco-editor .scrollbar .slider:hover) {
  background: var(--brand-primary);
}

/* 编辑器光标样式 */
:deep(.monaco-editor .cursor) {
  color: var(--brand-primary);
}

/* 编辑器行号样式 */
:deep(.monaco-editor .margin) {
  background: var(--bg-tertiary);
  color: var(--text-tertiary);
}

/* 编辑器选中行背景 */
:deep(.monaco-editor .line-numbers) {
  color: var(--text-tertiary);
}

/* 编辑器代码高亮 */
:deep(.monaco-editor .current-line) {
  background: var(--brand-primary-light);
  border: 1px solid var(--brand-primary-light);
}

/* 编辑器小地图样式 */
:deep(.monaco-editor .minimap) {
  background: var(--bg-tertiary);
  opacity: 0.6;
}
</style>
