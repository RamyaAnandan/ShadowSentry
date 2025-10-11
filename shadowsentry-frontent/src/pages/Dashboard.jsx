import React, { useEffect, useState, useRef } from "react";
import { motion } from "framer-motion";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import api from "../utils/api";
import bgImage from "../assets/background.jpg";
import logo from "../assets/logo-small.png";

export default function Dashboard() {
  const [user, setUser] = useState(null);
  const [riskScore, setRiskScore] = useState(0);
  const [incidents, setIncidents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [refreshing, setRefreshing] = useState(false);
  const navigate = useNavigate();
  const mountedRef = useRef(true);

  useEffect(() => {
    mountedRef.current = true;
    const storedUser = localStorage.getItem("ss_user");
    const token = localStorage.getItem("ss_access");

    // If auth missing, send to login immediately
    if (!storedUser || !token) {
      setLoading(false);
      navigate("/login");
      return;
    }

    const parsedUser = JSON.parse(storedUser);
    setUser(parsedUser);

 const fetchRisk = async () => {
  setLoading(true);
  try {
    const res = await api.getRiskScore(parsedUser.email, token);
    console.log("🔍 Backend Risk Raw Response:", res);

    // Normalize Axios vs direct data
    const payload = res?.data || res || {};

    // ✅ Extract safely from backend response
    const riskValue = payload.riskScore ?? payload.score ?? payload.risk ?? 0;
    const incidentsList = Array.isArray(payload.incidents)
      ? payload.incidents
      : payload.breaches || payload.items || payload.sources || [];

    const normalized = normalizeIncidents(incidentsList);

    if (mountedRef.current) {
      setRiskScore(Math.round(riskValue));
      setIncidents(normalized);
    }
  } catch (err) {
    console.error("❌ Error fetching risk score:", err);

    if (err?.response?.status === 404) {
      if (mountedRef.current) {
        setIncidents([]);
        setRiskScore(0);
      }
    } else if (err?.response?.status === 401) {
      toast.error("Session expired — please login again.");
      localStorage.removeItem("ss_access");
      localStorage.removeItem("ss_refresh");
      navigate("/login");
    } else {
      toast.error("Failed to load risk score. Check backend or network.");
    }
  } finally {
    if (mountedRef.current) setLoading(false);
    setRefreshing(false);
  }
};

    fetchRisk();

    return () => {
      mountedRef.current = false;
    };
  }, [navigate]);

  const handleLogout = () => {
    localStorage.clear();
    toast.success("Logged out successfully.");
    navigate("/login");
  };

  // Normalizes many backend formats into { id, name, domain, date, dataClasses, description, risk }
  function normalizeIncidents(arr) {
    if (!Array.isArray(arr)) return [];
    return arr.map((it, idx) => {
      if (typeof it === "string") {
        return { id: `s-${idx}`, name: it, domain: null, date: null, dataClasses: [], description: null, risk: null, raw: it };
      }
      // HIBP fields: Name, Domain, BreachDate, DataClasses, Description
      const name = it.Name || it.name || it.title || it.source || it.sourceName || "Unknown";
      const domain = it.Domain || it.domain || null;
      const date = it.BreachDate || it.date || it.publishedAt || null;
      const dataClasses = it.DataClasses || it.dataClasses || it.leaked || [];
      const description = it.Description || it.description || it.summary || null;
      const risk = it.riskScore || it.risk || null;

      return {
        id: it.id ?? `${name}-${idx}`,
        name,
        domain,
        date,
        dataClasses: Array.isArray(dataClasses) ? dataClasses : [],
        description,
        risk,
        raw: it,
      };
    });
  }

  // Simple deterministic score when backend doesn't supply one
  function computeRiskScore(incidentsList) {
    if (!incidentsList || incidentsList.length === 0) return 0;
    const base = Math.min(100, incidentsList.length * 25); // each breach is heavy
    const extra = incidentsList.reduce((acc, i) => acc + Math.min(10, (i.dataClasses?.length || 0) * 3), 0);
    return Math.min(100, Math.round(base + extra));
  }

  const getRiskColor = (score) => {
    if (score >= 80) return "text-red-500";
    if (score >= 50) return "text-yellow-400";
    return "text-green-400";
  };

  const getMeterColor = (score) => {
    if (score >= 80) return "#ff3358";
    if (score >= 50) return "#facc15";
    return "#22c55e";
  };

  const getInsightText = (score) => {
    if (score <= 0) {
      return "No breaches detected for your account. You're currently safe.";
    } else if (score < 40) {
      return "✅ Low exposure detected — your credentials are safe on the dark web.";
    } else if (score < 70) {
      return "⚠️ Medium exposure — some credentials may have leaked. Change old passwords.";
    } else {
      return "🚨 High exposure! Immediate action required — rotate passwords and enable 2FA.";
    }
  };

  const onRefresh = async () => {
  setRefreshing(true);
  const token = localStorage.getItem("ss_access");
  const storedUser = localStorage.getItem("ss_user");

  if (!storedUser || !token) {
    toast.error("Session expired — please login again.");
    navigate("/login");
    return;
  }

  try {
    setLoading(true);
    const parsedUser = JSON.parse(storedUser);
    const res = await api.getRiskScore(parsedUser.email, token);
    const payload = res?.data || res || {};

    const riskValue = payload.riskScore ?? payload.score ?? payload.risk ?? 0;
    const incidentsList = Array.isArray(payload.incidents)
      ? payload.incidents
      : payload.breaches || payload.items || payload.sources || [];

    const normalized = normalizeIncidents(incidentsList);

    if (mountedRef.current) {
      setRiskScore(Math.round(riskValue));
      setIncidents(normalized);
    }
  } catch (err) {
    console.error("Refresh error:", err);
    toast.error("Refresh failed.");
  } finally {
    if (mountedRef.current) {
      setLoading(false);
      setRefreshing(false);
    }
  }
};

  if (loading)
    return (
      <div className="min-h-screen flex flex-col items-center justify-center bg-black text-white">
        <motion.div
          animate={{ rotate: 360 }}
          transition={{ duration: 1.5, repeat: Infinity, ease: "linear" }}
          className="w-12 h-12 border-4 border-[#ff3358]/60 border-t-transparent rounded-full mb-6"
        />
        <p className="text-gray-300 text-lg tracking-wider">Loading your dashboard...</p>
      </div>
    );

  return (
    <div
      className="min-h-screen w-full relative overflow-hidden"
      style={{
        backgroundImage: `url(${bgImage})`,
        backgroundSize: "cover",
        backgroundPosition: "center",
      }}
    >
      <div className="absolute inset-0 bg-black/75 backdrop-blur-[5px]" />

      <header className="relative z-40 flex justify-between items-center px-12 pt-8 text-white">
        <div className="flex items-center gap-5">
          <motion.img
            src={logo}
            alt="ShadowSentry Logo"
            className="w-20 h-20 drop-shadow-[0_0_20px_#ff3358]"
            initial={{ scale: 0.9, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ duration: 0.6 }}
          />
          <div>
            <h1 className="text-4xl md:text-5xl font-extrabold tracking-wide drop-shadow-[0_0_25px_#ff3358]">
              ShadowSentry Dashboard
            </h1>
            <p className="text-gray-400 mt-1 tracking-wider text-sm md:text-base">
              Dark Web Threat Intelligence Center
            </p>
          </div>
        </div>

        <div className="flex items-center gap-4">
          <button
            onClick={onRefresh}
            disabled={refreshing}
            className="bg-transparent border border-[#ff3358]/40 text-white px-4 py-2 rounded-lg font-medium"
          >
            {refreshing ? "Refreshing..." : "Refresh"}
          </button>
          <motion.button
            onClick={handleLogout}
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            className="bg-[#ff3358]/90 hover:bg-[#ff3358] text-white px-5 py-2 rounded-lg font-semibold tracking-wide shadow-[0_0_20px_#ff3358]"
          >
            Logout
          </motion.button>
        </div>
      </header>

      <main className="relative z-40 px-10 py-16 text-white flex justify-center">
        <motion.div
          initial={{ opacity: 0, y: 25 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          className="max-w-5xl w-full bg-black/60 border border-[#ff3358]/40 backdrop-blur-md rounded-3xl p-10 shadow-[0_0_40px_rgba(255,51,88,0.25)]"
        >
          <h2 className="text-3xl font-bold mb-4 text-[#ff3358]">Welcome, {user?.username}</h2>
          <p className="text-gray-300 mb-8 text-base">{user?.email}</p>

          <div className="mb-12">
            <h3 className="text-2xl font-semibold">Your Risk Score</h3>
            <motion.p initial={{ scale: 0.9 }} animate={{ scale: [1, 1.05, 1] }} transition={{ repeat: Infinity, duration: 2.5 }} className={`text-7xl font-extrabold mt-4 ${getRiskColor(riskScore)}`}>
              {riskScore}
            </motion.p>
            <p className="text-gray-400 text-sm mt-2 mb-4">(Higher means more exposed to breaches)</p>

            <div className="w-full h-4 bg-gray-800/70 rounded-full overflow-hidden border border-[#ff3358]/40 mt-2">
              <motion.div initial={{ width: 0 }} animate={{ width: `${Math.min(riskScore, 100)}%` }} transition={{ duration: 1.2, ease: "easeOut" }} style={{ backgroundColor: getMeterColor(riskScore), boxShadow: `0 0 20px ${getMeterColor(riskScore)}` }} className="h-full rounded-full" />
            </div>
          </div>

          <motion.div initial={{ opacity: 0, y: 15 }} animate={{ opacity: 1, y: 0 }} transition={{ duration: 0.8 }} className="mb-12 bg-black/50 border border-[#ff3358]/30 rounded-2xl p-6 shadow-[0_0_25px_rgba(255,51,88,0.2)]">
            <div className="flex items-center gap-4">
              <motion.div animate={{ scale: [1, 1.2, 1], opacity: [0.6, 1, 0.6] }} transition={{ repeat: Infinity, duration: 2 }} className="w-4 h-4 rounded-full bg-[#ff3358] shadow-[0_0_20px_#ff3358]" />
              <h3 className="text-xl font-semibold text-[#ff3358] tracking-wide">AI Threat Insight</h3>
            </div>
            <p className="mt-4 text-gray-300 leading-relaxed text-base">{getInsightText(riskScore)}</p>
          </motion.div>

          <div>
            <h3 className="text-2xl font-semibold mb-4">Recent Breach Incidents</h3>
            {incidents.length === 0 ? (
              <div className="text-gray-400 italic text-center py-10">
                <p>No incidents found for your account.</p>
                <p className="text-sm mt-2 text-gray-500">You're currently safe. Keep monitoring your account!</p>
              </div>
            ) : (
              <ul className="space-y-4">
                {incidents.map((incident, i) => (
                  <motion.li key={incident.id ?? i} initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} transition={{ delay: i * 0.05 }} className="bg-black/50 border border-[#ff3358]/30 p-5 rounded-xl shadow-md hover:bg-black/70 transition-all">
                    <p className="text-lg font-semibold text-[#ff3358]">{incident.name}</p>
                    {incident.domain && <p className="text-sm text-gray-300 mt-1">Domain: {incident.domain}</p>}
                    {incident.date && <p className="text-sm text-gray-400 mt-1">Date: {incident.date}</p>}
                    {incident.dataClasses?.length > 0 && <p className="text-sm text-gray-300 mt-2">Data leaked: {incident.dataClasses.join(", ")}</p>}
                    <p className="text-gray-300 text-sm mt-2">{incident.description ?? "No description available."}</p>
                    <p className="text-gray-400 text-xs mt-2">Risk: {incident.risk ?? "N/A"}</p>
                  </motion.li>
                ))}
              </ul>
            )}
          </div>
        </motion.div>
      </main>
    </div>
  );
}
