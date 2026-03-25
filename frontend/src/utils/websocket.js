class WebSocketManager {
  constructor() {
    this.socket = null
    this.listeners = {}
    this.isConnected = false
  }

  connect(userId) {
    if (this.socket && this.socket.readyState === WebSocket.OPEN) {
      console.log('WebSocket already connected')
      return
    }

    const wsUrl = `ws://localhost:8080/ws/judge-progress`
    this.socket = new WebSocket(wsUrl)

    this.socket.onopen = () => {
      console.log('WebSocket connected')
      this.isConnected = true
      this.trigger('open')
      
      // 发送用户ID进行关联
      if (userId) {
        const message = JSON.stringify({ userId })
        this.socket.send(message)
        console.log('Sent userId for association:', userId)
      }
    }

    this.socket.onmessage = (event) => {
      try {
        const data = JSON.parse(event.data)
        console.log('WebSocket message:', data)
        this.trigger('message', data)
        
        // 根据消息类型触发不同的事件
        if (data.type) {
          this.trigger(data.type, data)
        }
      } catch (error) {
        console.error('Error parsing WebSocket message:', error)
      }
    }

    this.socket.onclose = () => {
      console.log('WebSocket disconnected')
      this.isConnected = false
      this.trigger('close')
    }

    this.socket.onerror = (error) => {
      console.error('WebSocket error:', error)
      this.trigger('error', error)
    }
  }

  disconnect() {
    if (this.socket) {
      this.socket.close()
      this.socket = null
      this.isConnected = false
    }
  }

  on(event, callback) {
    if (!this.listeners[event]) {
      this.listeners[event] = []
    }
    this.listeners[event].push(callback)
  }

  off(event, callback) {
    if (this.listeners[event]) {
      this.listeners[event] = this.listeners[event].filter(cb => cb !== callback)
    }
  }

  trigger(event, data) {
    if (this.listeners[event]) {
      this.listeners[event].forEach(callback => {
        try {
          callback(data)
        } catch (error) {
          console.error('Error in WebSocket callback:', error)
        }
      })
    }
  }

  isOpen() {
    return this.socket && this.socket.readyState === WebSocket.OPEN
  }
}

// 导出单例
export default new WebSocketManager()
