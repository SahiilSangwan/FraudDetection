import React, { useState } from "react";
import Header from "../components/Header";
import Footer from "../components/Footer";
import BeneficiaryManager from "../components/BeneficiaryManager";

const Beneficiary = () => {

  return (
    <div className="flex flex-col min-h-screen">
      <Header />

    <div className="w-full max-w-6xl px-6 justify-center items-center mx-auto mt-10">
        <BeneficiaryManager />
    </div>

      <Footer />
    </div>
  );
};

export default Beneficiary