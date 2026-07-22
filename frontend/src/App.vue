<script setup>
import { ref } from 'vue'

const sharedCode = ref('')
const fileNames = ref([''])
const isLoading = ref(false)
const message = ref('')
const messageType = ref('info')
const maxFileCount = 10
let hideMessageTimeoutId = null

async function downloadFiles(asArchive = false) {
  const code = sharedCode.value.trim()
  const trimmedFileNames = fileNames.value
    .map((fileName) => fileName.trim())
    .map((fileName) => applySharedCode(fileName, code))
    .map(normalizePdfFileName)
    .filter(Boolean)
  clearMessage()

  if (trimmedFileNames.length === 0) {
    showMessage('Введіть хоча б одну назву файлу.', 'error')
    return
  }

  isLoading.value = true

  try {
    if (asArchive) {
      await downloadFilesArchive(trimmedFileNames)
      showMessage(`Архів передано на скачування: ${trimmedFileNames.length} файлів.`, 'success')
    } else {
      const result = await downloadFilesSeparately(trimmedFileNames)
      showMessage(getSeparateDownloadMessage(result), getSeparateDownloadMessageType(result))
      return
    }
  } catch (error) {
    showMessage(error.message, 'error')
  } finally {
    isLoading.value = false
  }
}

async function downloadFilesSeparately(trimmedFileNames) {
  const failedFiles = []
  let downloadedCount = 0

  for (const trimmedFileName of trimmedFileNames) {
    try {
      await downloadFile(trimmedFileName)
      downloadedCount += 1
    } catch (error) {
      failedFiles.push(`${trimmedFileName}: ${error.message}`)
    }
  }

  return {
    downloadedCount,
    failedFiles,
  }
}

async function downloadFile(trimmedFileName) {
  const response = await fetch(`/api/files/download?fileName=${encodeURIComponent(trimmedFileName)}`)

  if (!response.ok) {
    const error = await response.json().catch(() => null)
    throw new Error(error?.detail || `Не вдалося скачати файл "${trimmedFileName}".`)
  }

  await saveResponse(response, trimmedFileName)
}

async function downloadFilesArchive(trimmedFileNames) {
  const response = await fetch('/api/files/download-batch', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ fileNames: trimmedFileNames }),
  })

  if (!response.ok) {
    const error = await response.json().catch(() => null)
    throw new Error(error?.detail || 'Не вдалося скачати файли.')
  }

  await saveResponse(response, 'dorozhni.zip')
}

async function saveResponse(response, fallbackFileName) {
  const blob = await response.blob()
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = getDownloadFileName(response) || fallbackFileName
  document.body.appendChild(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(url)
}

function addFileInput() {
  if (fileNames.value.length < maxFileCount) {
    fileNames.value.push('')
  }
}

function removeFileInput(index) {
  if (fileNames.value.length > 1) {
    fileNames.value.splice(index, 1)
  }
}

function normalizePdfFileName(fileName) {
  if (!fileName) {
    return ''
  }

  return fileName.toLowerCase().endsWith('.pdf') ? fileName : `${fileName}.pdf`
}

function applySharedCode(fileName, code) {
  if (!fileName) {
    return ''
  }

  if (!code) {
    return fileName
  }

  const nameWithoutPdfExtension = fileName.toLowerCase().endsWith('.pdf')
    ? fileName.slice(0, -4)
    : fileName

  return nameWithoutPdfExtension.endsWith(code)
    ? nameWithoutPdfExtension
    : `${nameWithoutPdfExtension}${code}`
}

function getSeparateDownloadMessage(result) {
  const parts = []

  if (result.downloadedCount > 0) {
    parts.push(`Скачано: ${result.downloadedCount}.`)
  }

  if (result.failedFiles.length > 0) {
    parts.push('Не вдалося скачати:')
    parts.push(...result.failedFiles.map((failedFile) => `- ${failedFile}`))
  }

  return parts.join('\n')
}

function getSeparateDownloadMessageType(result) {
  if (result.failedFiles.length === 0) {
    return 'success'
  }

  return result.downloadedCount > 0 ? 'warning' : 'error'
}

function showMessage(text, type) {
  clearMessage()
  message.value = text
  messageType.value = type
  hideMessageTimeoutId = window.setTimeout(clearMessage, 5000)
}

function clearMessage() {
  if (hideMessageTimeoutId) {
    window.clearTimeout(hideMessageTimeoutId)
    hideMessageTimeoutId = null
  }

  message.value = ''
}

function getDownloadFileName(response) {
  const disposition = response.headers.get('content-disposition')
  const utf8Match = disposition?.match(/filename\*=UTF-8''([^;]+)/i)
  const plainMatch = disposition?.match(/filename="?([^"]+)"?/i)
  const encodedName = utf8Match?.[1] || plainMatch?.[1]

  return encodedName ? decodeURIComponent(encodedName) : ''
}
</script>

<template>
  <main class="page">
    <section class="download-panel" aria-labelledby="page-title">
      <form class="download-form" @submit.prevent="downloadFiles(false)">
        <h1 id="page-title" class="sr-only">Завантаження файлів</h1>

        <div class="code-field">
          <label class="code-label" for="shared-code">code</label>
          <input
            id="shared-code"
            v-model="sharedCode"
            class="code-input"
            type="text"
            autocomplete="off"
          />
        </div>

        <div class="file-list">
          <div
            v-for="(_, index) in fileNames"
            :key="index"
            class="file-row"
            :class="{ single: fileNames.length === 1 }"
          >
            <input
              :id="`file-name-${index}`"
              v-model="fileNames[index]"
              class="file-input"
              type="text"
              placeholder="Наприклад: 12345"
              autocomplete="off"
              :aria-label="`Назва файлу ${index + 1}`"
            />
            <button
              v-if="fileNames.length > 1"
              class="remove-button"
              type="button"
              :disabled="isLoading"
              title="Прибрати файл"
              aria-label="Прибрати файл"
              @click="removeFileInput(index)"
            >
              ×
            </button>
          </div>
        </div>

        <div class="actions">
          <button
            class="add-text-button"
            type="button"
            :disabled="isLoading || fileNames.length >= maxFileCount"
            @click="addFileInput"
          >
            Додати
          </button>
          <button class="download-button" type="submit" :disabled="isLoading">
            {{ isLoading ? 'Скачування...' : 'Скачати' }}
          </button>
          <button
            class="archive-button"
            type="button"
            :disabled="isLoading"
            @click="downloadFiles(true)"
          >
            {{ isLoading ? 'Скачування...' : 'Скачати архів' }}
          </button>
        </div>
        <p v-if="message" class="message" :class="messageType">{{ message }}</p>
      </form>
    </section>
  </main>
</template>
