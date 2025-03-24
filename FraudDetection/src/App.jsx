import React, { useContext, useEffect } from 'react'
import { Route, Routes } from 'react-router-dom'
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import Home from './pages/Home'
import Login from './pages/Login'
import UserDashboard from './pages/UserDashboard'
import MyProfile from './pages/Porfile'
import Verification from './pages/Verification'
import Beneficiary from './pages/Beneficiary';
import { UserContext } from './context/UserContext';
import Transactions from './pages/Transactions';
import AntiInspect from './AntiInspect';
import TransactionHistory from './pages/TransactionHistory';
import ConfirmPayment from './pages/ConfirmPayment';

const App = () => {

  const{uToken} = useContext(UserContext);
  const{vToken} = useContext(UserContext);

  const BlockRightClick = () => {
    useEffect(() => {
      const handleRightClick = (e) => {
        e.preventDefault();
        alert("Functionality disabled!");
      };

      document.addEventListener("contextmenu", handleRightClick);
      return () => {
        document.removeEventListener("contextmenu", handleRightClick);
      };
    }
    , []);
    return null;
  };


  // AntiInspect();

  return uToken && vToken ? (
    <>
      {/* <BlockRightClick /> */}
      <ToastContainer />
      <Routes>
        <Route path="/" element={<UserDashboard />} />
        <Route path="/user-dashboard" element={<UserDashboard />} />
        <Route path="/my-profile" element={<MyProfile />} />
        <Route path="/beneficiary" element={<Beneficiary />} />
        <Route path="/transactions" element={<Transactions />} />
        <Route path="/confirm-payment" element={<ConfirmPayment />} />
        <Route path="/transaction-history" element={<TransactionHistory />} />
      </Routes>
    </>
  ) : uToken ? (
    <>
      {/* <BlockRightClick /> */}
      <ToastContainer />
      <Routes>
        <Route path="/verification" element={<Verification />} />
      </Routes>
    </>
  ) : (
    <>
      {/* <BlockRightClick /> */}
      <ToastContainer />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
      </Routes>
    </>
  );
  
}

export default App