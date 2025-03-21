import React, { useState } from "react";
import { useLocation } from "react-router-dom";
import Header from "../components/Header";
import Footer from "../components/Footer";
import { assets } from "../assets/assets"; // Import bank logos
import BeneficiaryManager from "../components/BeneficiaryManager";

const Beneficiary = () => {


  return (
    <div className="flex flex-col min-h-screen">
      {/* Header */}
      <Header />

    <div className="w-full max-w-6xl px-6 justify-center items-center mx-auto mt-10">
        <BeneficiaryManager />
    </div>

      {/* Footer */}
      <Footer />
    </div>
  );
};

export default Beneficiary