import DOMPurify from 'dompurify'
import { marked } from 'marked'

marked.use({
  gfm: true,
  breaks: true
})

export const renderMarkdown = (content) => {
  const rawHtml = marked.parse(String(content || ''), { async: false })
  return DOMPurify.sanitize(rawHtml)
}

export const markdownToPlainText = (content) => String(content || '')
  .replace(/```[\s\S]*?```/g, ' ')
  .replace(/`([^`]*)`/g, '$1')
  .replace(/^#{1,6}\s+/gm, '')
  .replace(/!\[([^\]]*)\]\([^)]+\)/g, '$1')
  .replace(/\[([^\]]+)\]\([^)]+\)/g, '$1')
  .replace(/[*_~>#-]/g, ' ')
  .replace(/<[^>]+>/g, ' ')
  .replace(/\s+/g, ' ')
  .trim()

export const extractMarkdownTitle = (content) => {
  const match = String(content || '').match(/^#(?!#)\s+(.+)$/m)
  return match ? match[1].trim() : ''
}

export const titleFromMarkdownFileName = (fileName) => String(fileName || '')
  .replace(/\.(md|markdown)$/i, '')
  .replace(/[-_]+/g, ' ')
  .trim()
