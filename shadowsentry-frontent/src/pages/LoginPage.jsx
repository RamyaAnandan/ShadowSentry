import React, { useState } from "react";
import { motion } from "framer-motion";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import bgImage from "../assets/background.jpg";
import logo from "../assets/logo-small.png";
import api from "../utils/api";

export default function LoginPage() {
  const [emailOrUsername, setEmailOrUsername] = useState("");
  const [password, setPassword] = useState("");
  const [busy, setBusy] = useState(false);
  const [clicked, setClicked] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (clicked || busy) return;

    setClicked(true);
    setBusy(true);

    try {
      const res = await api.login(emailOrUsername, password);
      console.log("✅ Login response:", res);

      // ✅ Backend response expected:
      // { accessToken, refreshToken, expiresIn, user }
      if (res?.accessToken && res?.user) {
        localStorage.setItem("ss_access", res.accessToken);
        localStorage.setItem("ss_refresh", res.refreshToken || "");
        localStorage.setItem("ss_user", JSON.stringify(res.user));

        toast.success(`👋 Welcome back, ${res.user.username}! Redirecting...`);
        setTimeout(() => navigate("/dashboard"), 1500);
      } else if (res?.error) {
        toast.error("❌ Login failed: " + res.error);
      } else {
        toast.error("❌ Invalid credentials. Try again.");
      }
    } catch (err) {
      console.error("❌ Login error:", err);
      if (err.response) {
        toast.error(
          "Login failed: " +
            (err.response.data?.error || err.response.statusText)
        );
      } else {
        toast.error("⚠️ Network error — backend might be offline.");
      }
    } finally {
      setBusy(false);
      setTimeout(() => setClicked(false), 1200);
    }
  };

  return (
    <div
      className="min-h-screen w-full relative overflow-hidden"
      style={{
        backgroundImage: `url(${bgImage})`,
        backgroundSize: "cover",
        backgroundPosition: "center",
      }}
    >
      {/* Background Overlay */}
      <div className="absolute inset-0 bg-black/75 backdrop-blur-[4px]" />

      {/* Glowing Animated Logo */}
      <motion.img
        src={logo}
        alt="ShadowSentry Logo"
        initial={{ opacity: 0, scale: 0.9 }}
        animate={{
          opacity: 1,
          scale: [1.2, 1.1, 1.2],
          filter: [
            "drop-shadow(0 0 30px #ff3358)",
            "drop-shadow(0 0 50px #ff3358)",
            "drop-shadow(0 0 30px #ff3358)",
          ],
        }}
        transition={{ repeat: Infinity, duration: 3 }}
        className="absolute top-8 left-8 w-28 h-28 z-40"
      />

      {/* Project Title */}
      <header className="absolute top-14 left-0 right-0 z-40 flex flex-col items-center justify-center text-center pointer-events-none">
        <h1 className="text-5xl font-extrabold text-white drop-shadow-[0_0_25px_#ff3358] tracking-wide">
          ShadowSentry
        </h1>
        <p className="text-gray-300 italic mt-2 text-lg tracking-widest">
          Dark Web Threat Intelligence System
        </p>
      </header>

      {/* Main Login Form */}
      <main className="relative z-40 min-h-screen flex items-center justify-center px-6 py-10">
        <div className="max-w-6xl w-full grid md:grid-cols-2 gap-10 items-center">
          {/* Left Info */}
          <motion.div
            initial={{ opacity: 0, x: -30 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.6 }}
            className="hidden md:block text-white"
          >
            <h2 className="text-6xl font-extrabold mb-6 drop-shadow-[0_0_20px_rgba(255,255,255,0.2)]">
              Welcome back
            </h2>
            <p className="text-gray-300 text-lg leading-relaxed max-w-md">
              Enter your credentials to access ShadowSentry’s dark web breach
              intelligence dashboard. Analyze threats, monitor leaks, and act
              before adversaries do.
            </p>
          </motion.div>

          {/* Right Form */}
          <motion.div
            initial={{ opacity: 0, y: 15 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.7 }}
            className="bg-black/60 border border-[#ff3358]/40 backdrop-blur-md p-8 rounded-xl 
                       shadow-[0_0_40px_rgba(255,51,88,0.25)] 
                       hover:shadow-[0_0_60px_rgba(255,51,88,0.5)] 
                       transition-all duration-500"
          >
            <form
              onSubmit={handleSubmit}
              className="space-y-7 relative overflow-hidden"
            >
              {clicked && (
                <motion.div
                  initial={{ scale: 0, opacity: 0.9 }}
                  animate={{ scale: 2.5, opacity: 0 }}
                  transition={{ duration: 0.8, ease: "easeOut" }}
                  className="absolute inset-0 rounded-xl border-2 border-[#ff3358]/60"
                />
              )}

              {/* Username/Email */}
              <div className="relative group">
                <label className="text-sm text-gray-300 tracking-wider">
                  Username or Email
                </label>
                <input
                  type="text"
                  value={emailOrUsername}
                  onChange={(e) => setEmailOrUsername(e.target.value)}
                  placeholder="you@example.com or username"
                  required
                  className="mt-2 w-full bg-transparent border-b border-gray-600 py-2 
                             text-white placeholder-gray-500 focus:outline-none 
                             group-hover:border-[#ff3358] focus:border-[#ff3358] 
                             transition-all duration-300"
                />
                <motion.div
                  className="absolute bottom-0 left-0 h-[2px] bg-[#ff3358]"
                  initial={{ width: 0 }}
                  animate={{ width: emailOrUsername ? "100%" : 0 }}
                  transition={{ duration: 0.3 }}
                />
              </div>

              {/* Password */}
              <div className="relative group">
                <label className="text-sm text-gray-300 tracking-wider">
                  Password
                </label>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Your password"
                  required
                  className="mt-2 w-full bg-transparent border-b border-gray-600 py-2 
                             text-white placeholder-gray-500 focus:outline-none 
                             group-hover:border-[#ff3358] focus:border-[#ff3358] 
                             transition-all duration-300"
                />
                <motion.div
                  className="absolute bottom-0 left-0 h-[2px] bg-[#ff3358]"
                  initial={{ width: 0 }}
                  animate={{ width: password ? "100%" : 0 }}
                  transition={{ duration: 0.3 }}
                />
              </div>

              {/* Submit Button */}
              <motion.button
                type="submit"
                disabled={busy}
                whileHover={{ scale: 1.05 }}
                whileTap={{ scale: 0.94 }}
                animate={{
                  boxShadow: [
                    "0 0 15px #ff3358",
                    "0 0 35px #ff3358",
                    "0 0 15px #ff3358",
                  ],
                }}
                transition={{ repeat: Infinity, duration: 2 }}
                className="w-full py-3 rounded-md bg-[#ff3358]/90 hover:bg-[#ff3358] text-white 
                           font-semibold tracking-wide shadow-[0_0_20px_#ff3358] transition"
              >
                {busy ? "Authenticating..." : "Sign in now"}
              </motion.button>

              <div className="text-center text-sm text-gray-400 mt-3">
                Don’t have an account?{" "}
                <a
                  className="text-[#ff3358] hover:underline font-medium text-base"
                  href="/register"
                >
                  Register
                </a>
              </div>
            </form>
          </motion.div>
        </div>
      </main>
    </div>
  );
}
