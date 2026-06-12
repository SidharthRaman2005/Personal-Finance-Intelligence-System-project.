import React from "react"
import ReactDOM from "react-dom/client"
import App from "./App"
import "./index.css"
import { BrowserRouter } from "react-router-dom"

const BACKEND_API_BASE = "http://localhost:8080/api/"
const AUTH_API_BASE = "http://localhost:8080/api/auth/"
const originalFetch = window.fetch.bind(window)

window.fetch = async (input, init = {}) => {
  const requestUrl = typeof input === "string"
    ? input
    : input instanceof URL
      ? input.toString()
      : input.url

  const isBackendApi = requestUrl.startsWith(BACKEND_API_BASE)
  const isAuthApi = requestUrl.startsWith(AUTH_API_BASE)

  let nextInit = init

  if (isBackendApi && !isAuthApi) {
    const token = localStorage.getItem("token")

    if (token) {
      const headers = new Headers(nextInit.headers)
      if (!headers.has("Authorization")) {
        headers.set("Authorization", `Bearer ${token}`)
      }
      nextInit = { ...nextInit, headers }
    }
  }

  const response = await originalFetch(input, nextInit)

  if (isBackendApi && !isAuthApi && response.status === 401) {
    localStorage.removeItem("token")
    localStorage.removeItem("tokenType")
    localStorage.removeItem("tokenExpiresAt")
    localStorage.removeItem("username")

    if (window.location.pathname !== "/") {
      window.location.href = "/"
    }
  }

  return response
}

ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <BrowserRouter>
      <App />
    </BrowserRouter>
  </React.StrictMode>
)