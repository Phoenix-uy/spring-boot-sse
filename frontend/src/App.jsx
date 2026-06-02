import { useState, useEffect } from 'react'
import './App.css'

const API_URL = import.meta.env.VITE_API_URL || ''

function App() {
  const [data, setData] = useState(null)
  const [connected, setConnected] = useState(false)
  const [events, setEvents] = useState([])

  useEffect(() => {
    const eventSource = new EventSource(`${API_URL}/api/sse/stream`)

    eventSource.addEventListener('data-update', (event) => {
      const newData = JSON.parse(event.data)
      setData(newData)
      addEvent(`📡 Data updated: ${Object.keys(newData).length} keys`)
    })

    eventSource.onopen = () => {
      setConnected(true)
      addEvent('✅ Connected to SSE stream')
    }

    eventSource.onerror = (error) => {
      setConnected(false)
      addEvent('❌ Connection error')
      console.error('SSE Error:', error)
    }

    fetch(`${API_URL}/api/data`)
      .then(res => res.json())
      .then(initialData => {
        setData(initialData)
        addEvent(`📥 Initial data loaded: ${Object.keys(initialData).length} keys`)
      })
      .catch(err => {
        addEvent('❌ Failed to load initial data')
        console.error('Fetch error:', err)
      })

    return () => {
      eventSource.close()
    }
  }, [])

  const addEvent = (message) => {
    const timestamp = new Date().toLocaleTimeString()
    setEvents(prev => [{ timestamp, message }, ...prev].slice(0, 50))
  }

  const renderValue = (value) => {
    if (typeof value === 'object' && value !== null) {
      return <pre>{JSON.stringify(value, null, 2)}</pre>
    }
    return String(value)
  }

  return (
    <div className="app">
      <header className="header">
        <h1>🚀 Server-Sent Events Demo</h1>
        <div className={`status ${connected ? 'connected' : 'disconnected'}`}>
          <div className="status-indicator"></div>
          <span>{connected ? 'Connected' : 'Disconnected'}</span>
        </div>
      </header>

      <div className="container">
        <div className="card">
          <h2>📊 Current Data</h2>
          {data ? (
            <div className="data-display">
              {Object.entries(data).map(([key, value]) => (
                <div key={key} className="data-item">
                  <strong>{key}:</strong>
                  <div className="data-value">{renderValue(value)}</div>
                </div>
              ))}
            </div>
          ) : (
            <div className="empty-state">Waiting for data...</div>
          )}
        </div>

        <div className="card">
          <h2>📝 Event Log</h2>
          <div className="event-log">
            {events.length === 0 ? (
              <div className="empty-state">No events yet</div>
            ) : (
              events.map((event, index) => (
                <div key={index} className="event-entry">
                  <span className="timestamp">[{event.timestamp}]</span>
                  <span className="message">{event.message}</span>
                </div>
              ))
            )}
          </div>
        </div>

        <div className="card info-card">
          <h2>ℹ️ How to Test</h2>
          <ol>
            <li>Open <code>data.json</code> in the backend root directory</li>
            <li>Edit any value and save the file</li>
            <li>Watch this page update automatically!</li>
            <li>Open multiple browser tabs to see all update simultaneously</li>
          </ol>
        </div>
      </div>
    </div>
  )
}

export default App
