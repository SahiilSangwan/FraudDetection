import React, { useState } from "react";
import { useLocation } from "react-router-dom";
import Header from "../components/Header";
import Footer from "../components/Footer";
import { assets } from "../assets/assets"; // Import bank logos
import BeneficiaryManager from "../components/BeneficiaryManager";

const Beneficiary = () => {
  const location = useLocation();
  const queryParams = new URLSearchParams(location.search);
  const bank = queryParams.get("bank") || "default";

  // Bank logos mapping
  const bankLogos = {
    sbi: assets?.sbi,
    hdfc: assets?.hdfc,
    icici: assets?.icici,
  };

  return (
    <div className="flex flex-col min-h-screen">
      {/* Header */}
      <Header bankLogo={bankLogos[bank]} bankName={bank.toUpperCase()} />

    <div className="w-full max-w-6xl px-6 justify-center items-center mx-auto mt-10">
        <BeneficiaryManager />
    </div>

      {/* Footer */}
      <Footer />
    </div>
  );
};

export default Beneficiary