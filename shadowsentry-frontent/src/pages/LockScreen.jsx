import React, { useState } from "react";
import { motion } from "framer-motion";
import { useNavigate } from "react-router-dom";
import { FaLock, FaUnlock } from "react-icons/fa";
import bgImage from "../assets/background.jpg";
import logo from "../assets/logo-small.png";

export default function LockScreen() {
  const [isUnlocked, setUnlocked] = useState(false);
  const navigate = useNavigate();

  const onUnlock = () => {
    if (isUnlocked) return;
    setUnlocked(true);
    setTimeout(() => navigate("/login"), 1800);
  };

  return (
    <div
      className="relative h-screen w-screen overflow-hidden"
      style={{
        backgroundImage: `url(${bgImage})`,
        backgroundSize: "cover",
        backgroundPosition: "center",
      }}
    >
      {/* 🔲 Background overlay */}
      <div className="absolute inset-0 bg-black/75 backdrop-blur-[4px]" />

      {/* 🔥 Glowing Animated Logo (same as LoginPage) */}
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

      {/* 🧠 Project Title & Subtitle (centered header same as login) */}
      <header className="absolute top-14 left-0 right-0 z-40 flex flex-col items-center justify-center text-center pointer-events-none">
        <h1 className="text-5xl font-extrabold text-white drop-shadow-[0_0_25px_#ff3358] tracking-wide">
          ShadowSentry
        </h1>
        <p className="text-gray-300 italic mt-2 text-lg tracking-widest">
          Dark Web Threat Intelligence System
        </p>
      </header>

      {/* 🔐 Lock animation area */}
      <motion.div
        className="relative z-50 h-full flex flex-col items-center justify-center pt-28"
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ duration: 0.8 }}
      >
        {/* Glowing pulse behind lock */}
        {!isUnlocked && (
          <motion.div
            animate={{
              scale: [1, 1.35, 1],
              opacity: [0.4, 0.8, 0.4],
              filter: ["blur(60px)", "blur(90px)", "blur(60px)"],
            }}
            transition={{ repeat: Infinity, duration: 3, ease: "easeInOut" }}
            className="absolute w-[480px] h-[480px] rounded-full bg-[#ff3358]/40"
          />
        )}

        {/* Lock icon container */}
        <motion.div
          onClick={onUnlock}
          whileHover={{ scale: 1.1 }}
          whileTap={{ scale: 0.9 }}
          animate={
            isUnlocked
              ? { scale: 0.5, y: -150, opacity: 0, rotate: -20 }
              : { scale: 1, y: 0, opacity: 1, rotate: 0 }
          }
          transition={{ type: "spring", stiffness: 180, damping: 18 }}
          className="cursor-pointer flex items-center justify-center rounded-full p-24 bg-black/50 
                     border border-[#ff3358]/50 shadow-[0_0_100px_#ff3358] backdrop-blur-md"
        >
          <motion.div
            animate={{
              textShadow: [
                "0 0 20px #ff3358",
                "0 0 40px #ff3358",
                "0 0 20px #ff3358",
              ],
            }}
            transition={{ repeat: Infinity, duration: 2.2, ease: "easeInOut" }}
          >
            {isUnlocked ? (
              <FaUnlock size={200} color="#ff3358" />
            ) : (
              <FaLock size={200} color="#ff3358" />
            )}
          </motion.div>
        </motion.div>
      </motion.div>

      {/* 🎬 Cinematic bottom fade */}
      <div className="absolute bottom-0 left-0 right-0 h-48 bg-gradient-to-t from-black/80 to-transparent pointer-events-none" />
    </div>
  );
}
