import React, { useState, useEffect, useContext } from "react";
import { UserContext } from '../context/UserContext';

const Footer = () => {
    const bank =localStorage.getItem('bank') || "default";

    const {getBankTheme} = useContext(UserContext);
  

  return (

    <footer className={`bg-gradient-to-r ${getBankTheme(bank).header} text-white py-6`}>
      <div className=" text-center text-sm">
          &copy; {new Date().getFullYear()} Secure Pulse | All Rights Reserved | Wissen Technology
      </div>
    </footer>
  );
};

export default Footer;
