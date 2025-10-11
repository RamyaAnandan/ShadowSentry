// src/utils/api.js
import axios from "axios";

/**
 * 🌐 Base backend API URL
 * Example: VITE_API_URL="http://localhost:8080/api/v1"
 * (But we'll only include `/api/v1` inside the endpoint paths, not in the base.)
 */
const API_BASE = import.meta.env.VITE_API_URL || "http://localhost:8080";

console.log("🧠 Using backend base URL:", API_BASE);

/**
 * 🧠 Create a single axios instance for the entire app
 */
const client = axios.create({
  baseURL: API_BASE,
  timeout: 20000,
  withCredentials: true, // ✅ allows cookies for refresh tokens
});

/**
 * 🔐 Automatically attach Authorization header if token exists in localStorage
 */
client.interceptors.request.use((config) => {
  const token = localStorage.getItem("ss_access");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

/**
 * Helper for manual Authorization headers
 */
function authHeaders(token) {
  return token ? { Authorization: `Bearer ${token}` } : {};
}

export default {
  /**
   * 🟢 REGISTER — Register a new user
   * Backend expects: { username, email, password, confirmPassword }
   * Returns: { id, username, email, roles }
   */
  register: async (username, email, password, confirmPassword) => {
    const res = await client.post("/api/v1/auth/register", {
      username,
      email,
      password,
      confirmPassword, // ✅ FIXED HERE
    });
    return res.data;
  },

  /**
   * 🟢 LOGIN — Authenticate user
   * Backend expects: { usernameOrEmail, password }
   * Returns: { accessToken, refreshToken, user }
   */
  login: async (usernameOrEmail, password) => {
    const res = await client.post("/api/v1/auth/login", {
      usernameOrEmail,
      password,
    });
    return res.data;
  },

  /**
   * 🟡 REFRESH TOKEN — Refresh session (optional future use)
   */
  refresh: async (refreshToken) => {
    const res = await client.post("/api/v1/auth/refresh", { refreshToken });
    return res.data;
  },

  /**
   * 🔵 ME — Get current logged-in user details
   */
  me: async (accessToken) => {
    const res = await client.get("/api/v1/auth/me", {
      headers: authHeaders(accessToken),
    });
    return res.data;
  },

  /**
   * 🔴 FETCH RISK SCORE — Fetch breach data from backend
   * Endpoint: GET /api/v1/incidents/risk?email={email}
   */
  getRiskScore: async (email, accessToken) => {
    const res = await client.get(
      `/api/v1/incidents/risk?email=${encodeURIComponent(email)}`,
      { headers: authHeaders(accessToken) }
    );
    return res.data;
  },
};
