import React from "react";
import logo from "../assets/logo.png";

export default function LogoHeader() {
  return (
    <div className="fixed top-4 left-4 z-50 flex items-center gap-3">
      <img src={logo} alt="ShadowSentry" className="w-10 h-10 drop-shadow-[0_0_10px_rgba(255,0,60,0.25)] rounded-md" />
      <span className="text-sm text-gray-200 font-semibold">ShadowSentry</span>
    </div>
  );
}
