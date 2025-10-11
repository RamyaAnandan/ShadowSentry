import React from "react";
import logo from "../assets/logo-small.png";

export default function Header({ compact = false, className = "" }) {
  return (
    // NOTE: no absolute positioning here — parent controls placement
    <header
      className={`z-20 flex transition-all duration-500 ${compact ? "flex-row items-center" : "flex-col items-center text-center"} ${compact ? "px-2 py-1" : "mt-6"} ${className}`}
    >
      {/* Logo */}
      <img
        src={logo}
        alt="ShadowSentry Logo"
        className={`object-contain drop-shadow-[0_0_15px_#ff3358b0] transition-all duration-500 ${
          compact ? "w-12 h-12 mr-3" : "w-24 h-24 mb-2 animate-pulse"
        }`}
      />

      {/* Title + Tagline */}
      <div className={`${compact ? "text-left" : "flex flex-col items-center justify-center"}`}>
        <h1
          className={`font-extrabold tracking-wide text-white drop-shadow-[0_0_25px_#ff3358] transition-all duration-500 ${
            compact ? "text-2xl md:text-3xl" : "text-5xl md:text-6xl"
          }`}
        >
          ShadowSentry
        </h1>

        {!compact && (
          <p className="text-sm md:text-lg italic text-gray-300 mt-2 tracking-widest">
            Dark Web Threat Intelligence System
          </p>
        )}
      </div>
    </header>
  );
}
