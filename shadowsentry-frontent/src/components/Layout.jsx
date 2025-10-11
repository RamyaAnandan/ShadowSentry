import React from "react";
import { motion } from "framer-motion";

export default function Layout({ children }) {
  return (
    <div className="min-h-screen bg-gradient-to-b from-black via-gray-950 to-black flex flex-col items-center justify-center text-white font-[Inter]">
      <motion.div
        initial={{ scale: 0.8, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        transition={{ duration: 0.6 }}
        className="flex flex-col items-center mb-10"
      >
        <img
          src="/shadowsentry-logo.png"
          alt="ShadowSentry"
          className="w-24 h-24 drop-shadow-[0_0_15px_#ff003c]"
        />
        <h1 className="text-4xl mt-3 font-bold text-accent drop-shadow-[0_0_15px_#ff003c]">
          ShadowSentry
        </h1>
        <p className="text-gray-400 text-sm tracking-widest mt-1">
          Cyber Threat Intelligence Platform
        </p>
      </motion.div>

      <div className="w-full max-w-md bg-black/60 backdrop-blur-md border border-accent/40 p-8 rounded-2xl shadow-[0_0_30px_#ff003c33]">
        {children}
      </div>
    </div>
  );
}
