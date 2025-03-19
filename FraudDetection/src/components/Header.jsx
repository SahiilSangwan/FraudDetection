import React, { useContext } from 'react'
import { Link } from "react-router-dom";
import { UserContext } from '../context/UserContext';
import { useNavigate } from 'react-router-dom'
import { assets } from '../assets/assets';

const Header = () => {

  const bank = localStorage.getItem('bank') || "default";

    const bankLogos = {
      sbi: assets?.sbi,
      hdfc: assets?.hdfc,
      icici: assets?.icici,
    };

  const {uToken, setUToken, logoutuser} = useContext(UserContext);
  const {vToken, setVToken} = useContext(UserContext);

  const navigate = useNavigate()

  const logout = () => {
    navigate('/')
    uToken && setUToken('')
    uToken && localStorage.removeItem('uToken')
    vToken && setVToken('')
    vToken && localStorage.removeItem('vToken')
    localStorage.removeItem('user')
    logoutuser()
}

  return (
    <header className=" bg-blue-500 text-white p-4 flex justify-between items-center shadow-md">
      {/* Left - Bank Logo & Name */}
      <div className="flex items-center space-x-3">
        <img src={bankLogos[bank]} alt={`${bank} Logo`} className="h-12 w-12" />
        <h1 className="text-xl font-bold">{bank.toUpperCase()} Bank</h1>
      </div>

      {/* Center - Navigation Links */}
      <nav className="flex space-x-8">
        {/* Dashboard */}
        <Link to={"/user-dashboard"} className="hover:underline">
            Dashboard
        </Link>

        {/* Beneficiary */}
        <Link to="/beneficiary" className="hover:underline">
            Add & Manage Beneficiary
        </Link>
        

        {/* Transfer Money Dropdown */}
        <div className="relative group">
          <button className="hover:underline focus:outline-none">
            Transfer Money ▼
          </button>
          <div className="absolute left-0 mt-2 bg-white text-black rounded shadow-lg w-56 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-300 ease-in-out">
            <Link
              to="/transfer/same-bank"
              className="block px-4 py-2 hover:bg-gray-200"
            >
              Same Bank Transfer
            </Link>
            <Link
              to="/transfer/different-bank"
              className="block px-4 py-2 hover:bg-gray-200"
            >
              Different Bank Transfer
            </Link>
          </div>
        </div>

        {/* Transaction History */}
        <Link to="/transactions" className="hover:underline">
          Transaction History
        </Link>

        {/* Profile */}
        <Link to="/my-profile" className="hover:underline">
          Profile
        </Link>
      </nav>

      {/* Right - Logout Button */}
      <button onClick={logout} className='bg-red-500 px-4 py-2 rounded-full hover:bg-red-600 transition duration-300'>Logout</button>
    </header>
  );
};

export default Header;
