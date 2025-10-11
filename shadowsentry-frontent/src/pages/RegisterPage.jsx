import React, { useState } from "react";
import { motion } from "framer-motion";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
import bgImage from "../assets/background.jpg";
import logo from "../assets/logo-small.png";
import api from "../utils/api";

export default function RegisterPage() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [busy, setBusy] = useState(false);
  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();

    if (password !== confirm) {
      toast.error("❌ Passwords do not match!");
      return;
    }

    setBusy(true);
    try {
      // ✅ Corrected function call
      const resp = await api.register(name, email, password, confirm);

      console.log("✅ Register response:", resp);

      // ✅ Success check
      if (resp?.id || resp?.username || resp?.message?.includes("successful")) {
        toast.success("🎉 Account created successfully!");
        setTimeout(() => navigate("/login"), 1500);
        return;
      }

      // ❌ Backend error handling
      if (resp?.error) {
        toast.error("Registration failed: " + resp.error);
      } else {
        toast.error("Registration failed. Please try again.");
      }
    } catch (err) {
      console.error("❌ Registration error:", err);
      if (err.response) {
        toast.error(
          "Registration error: " +
            (err.response.data?.error || err.response.statusText)
        );
      } else {
        toast.error("Network or server error — check backend.");
      }
    } finally {
      setBusy(false);
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
      {/* Overlay */}
      <div className="absolute inset-0 bg-black/65 backdrop-blur-[3px]" />

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

      {/* Header */}
      <header className="absolute top-14 left-0 right-0 z-40 flex flex-col items-center justify-center text-center pointer-events-none">
        <h1 className="text-5xl font-extrabold text-white drop-shadow-[0_0_25px_#ff3358] tracking-wide">
          ShadowSentry
        </h1>
        <p className="text-gray-300 italic mt-2 text-lg tracking-widest">
          Dark Web Threat Intelligence System
        </p>
      </header>

      {/* Main */}
      <main className="relative z-40 min-h-screen flex items-center justify-center px-6 py-10">
        <div className="max-w-6xl w-full grid md:grid-cols-2 gap-10 items-center">
          {/* Left Info */}
          <motion.div
            initial={{ opacity: 0, x: -25 }}
            animate={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.6 }}
            className="hidden md:block text-white"
          >
            <h2 className="text-5xl font-extrabold mb-4 drop-shadow-[0_0_20px_rgba(255,51,88,0.4)]">
              Join ShadowSentry
            </h2>
            <p className="text-gray-300 text-lg leading-relaxed max-w-md">
              Create your account to access dark web threat dashboards,
              breach monitoring, and AI-powered risk intelligence.
            </p>
          </motion.div>

          {/* Right Form */}
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.7 }}
            className="bg-black/60 border border-[#ff3358]/40 backdrop-blur-md p-8 rounded-xl 
                       shadow-[0_0_40px_rgba(255,51,88,0.25)] 
                       hover:shadow-[0_0_60px_rgba(255,51,88,0.5)] 
                       transition-all duration-500"
          >
            <form onSubmit={handleRegister} className="space-y-6">
              {/* Username */}
              <div className="group">
                <label className="text-sm text-gray-300 tracking-wider">
                  Username
                </label>
                <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="JohnDoe"
                  required
                  className="mt-2 w-full bg-transparent border-b border-gray-600 py-2 
                             text-white placeholder-gray-500 focus:outline-none 
                             group-hover:border-[#ff3358] focus:border-[#ff3358] 
                             transition-all duration-300"
                />
              </div>

              {/* Email */}
              <div className="group">
                <label className="text-sm text-gray-300 tracking-wider">
                  Email
                </label>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="you@example.com"
                  required
                  className="mt-2 w-full bg-transparent border-b border-gray-600 py-2 
                             text-white placeholder-gray-500 focus:outline-none 
                             group-hover:border-[#ff3358] focus:border-[#ff3358] 
                             transition-all duration-300"
                />
              </div>

              {/* Password */}
              <div className="group">
                <label className="text-sm text-gray-300 tracking-wider">
                  Password
                </label>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="Create password"
                  required
                  className="mt-2 w-full bg-transparent border-b border-gray-600 py-2 
                             text-white placeholder-gray-500 focus:outline-none 
                             group-hover:border-[#ff3358] focus:border-[#ff3358] 
                             transition-all duration-300"
                />
              </div>

              {/* Confirm Password */}
              <div className="group">
                <label className="text-sm text-gray-300 tracking-wider">
                  Confirm Password
                </label>
                <input
                  type="password"
                  value={confirm}
                  onChange={(e) => setConfirm(e.target.value)}
                  placeholder="Confirm password"
                  required
                  className="mt-2 w-full bg-transparent border-b border-gray-600 py-2 
                             text-white placeholder-gray-500 focus:outline-none 
                             group-hover:border-[#ff3358] focus:border-[#ff3358] 
                             transition-all duration-300"
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
                {busy ? "Registering..." : "Register Now"}
              </motion.button>

              {/* Already have account */}
              <div className="text-center text-base text-gray-300 mt-3">
                Already have an account?{" "}
                <a
                  href="/login"
                  className="text-[#ff3358] hover:underline font-semibold text-lg"
                >
                  Login
                </a>
              </div>
            </form>
          </motion.div>
        </div>
      </main>
    </div>
  );
}
