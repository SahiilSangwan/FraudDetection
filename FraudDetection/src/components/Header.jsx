import React, { useContext, useEffect } from 'react'
import { Link } from "react-router-dom";
import { UserContext } from '../context/UserContext';
import { useNavigate } from 'react-router-dom'
import { toast } from 'react-toastify'
import { assets } from '../assets/assets';

const Header = () => {

  const bank = localStorage.getItem('bank') || "default";

    const bankLogos = {
      sbi: assets?.sbi,
      hdfc: assets?.hdfc,
      icici: assets?.icici,
    };

  const {uToken, setUToken, logoutuser} = useContext(UserContext);
  const {vToken, setVToken,getBankTheme} = useContext(UserContext);

  const navigate = useNavigate()

  const logout = () => {
    navigate('/')
    uToken && setUToken('')
    uToken && localStorage.removeItem('uToken')
    vToken && setVToken('')
    vToken && localStorage.removeItem('vToken')
    localStorage.removeItem('user')
    localStorage.removeItem('lastTriggeredTime')
    localStorage.removeItem('bank')
    localStorage.removeItem('email')
    localStorage.removeItem('id')
    logoutuser()
}

    useEffect(() => {
      const lastTriggeredTime = localStorage.getItem('lastTriggeredTime');
      const currentTime = new Date().getTime();
  
      if (!lastTriggeredTime || currentTime - lastTriggeredTime >= 3600000) {
        toast.warn("Session will expire in 5 minutes. Please save your work.",);
        setTimeout(() => {
          toast.warn("Session expired. Please login again.",);
          localStorage.removeItem('lastTriggeredTime');
          logout();  
        }, 300000);
      }
  
    }, []);


  return (
    <header className={`bg-gradient-to-r ${getBankTheme(bank).header} text-white p-4 shadow-lg`}>
        <div className="max-w-7xl mx-auto flex flex-col md:flex-row justify-between items-center space-y-4 md:space-y-0">
          {/* Left - Bank Logo & Name */}
          <div className="flex items-center space-x-3 group">
            <img 
              src={bankLogos[bank]} 
              alt={`${bank} Logo`} 
              className="h-12 w-12 transition-transform duration-300 group-hover:scale-110" 
            />
            <h1 className="text-2xl font-bold tracking-tight">
              <span className="block text-sm font-normal opacity-80">Welcome to</span>
              {bank.toUpperCase()} Internet Banking
            </h1>
          </div>

          {/* Center - Navigation Links */}
          <nav className="flex flex-wrap justify-center gap-6 px-4">
            {/* Dashboard */}
            <Link 
              to="/user-dashboard" 
              className="relative py-1 font-medium hover:text-white/90 transition-colors"
            >
              Dashboard
              <span className="absolute bottom-0 left-0 w-0 h-0.5 bg-white transition-all duration-300 group-hover:w-full"></span>
            </Link>

            {/* Beneficiary */}
            <Link 
              to="/beneficiary" 
              className="relative py-1 font-medium hover:text-white/90 transition-colors group"
            >
              Beneficiary
              <span className="absolute bottom-0 left-0 w-0 h-0.5 bg-white transition-all duration-300 group-hover:w-full"></span>
            </Link>

            {/* Transfer Money Dropdown */}
            <div className="relative group">
              <button className="flex items-center font-medium py-1 hover:text-white/90 transition-colors">
                Transfer Money
                <svg className="ml-1 w-4 h-4 transition-transform group-hover:rotate-180" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                </svg>
              </button>
              <div className="absolute left-1/2 -translate-x-1/2 mt-3 bg-white text-gray-800 rounded-lg shadow-xl w-56 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 origin-top">
                <Link
                  to="/transactions?bank=same"
                  className="block px-4 py-3 hover:bg-blue-50 transition-colors border-b border-gray-100 first:rounded-t-lg last:rounded-b-lg"
                >
                  <div className="flex items-center">
                    <svg className="w-5 h-5 mr-2 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
                    </svg>
                    Same Bank Transfer
                  </div>
                </Link>
                <Link
                  to="/transactions?bank=notSame"
                  className="block px-4 py-3 hover:bg-blue-50 transition-colors rounded-b-lg"
                >
                  <div className="flex items-center">
                    <svg className="w-5 h-5 mr-2 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
                    </svg>
                    Different Bank Transfer
                  </div>
                </Link>
              </div>
            </div>

            {/* Transaction History */}
            <Link 
              to="/transaction-history" 
              className="relative py-1 font-medium hover:text-white/90 transition-colors group"
            >
              Transactions
              <span className="absolute bottom-0 left-0 w-0 h-0.5 bg-white transition-all duration-300 group-hover:w-full"></span>
            </Link>

            {/* Profile */}
            <Link 
              to="/my-profile" 
              className="relative py-1 font-medium hover:text-white/90 transition-colors group"
            >
              Profile
              <span className="absolute bottom-0 left-0 w-0 h-0.5 bg-white transition-all duration-300 group-hover:w-full"></span>
            </Link>
          </nav>

          {/*Logout Button */}
          <button 
            onClick={logout} 
            className="flex items-center space-x-1 bg-white/20 px-5 py-2 rounded-full hover:bg-white/30 backdrop-blur-sm transition-all duration-300 border border-white/30 hover:border-white/50 shadow-sm"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
            </svg>
            <span>Logout</span>
          </button>
        </div>
      </header>
  );
};

export default Header;
