import React, { useContext } from 'react'
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

const App = () => {

  const{uToken} = useContext(UserContext);
  const{vToken} = useContext(UserContext);

  return uToken && vToken ? (
    <>
      <ToastContainer />
      <Routes>
        <Route path="/" element={<UserDashboard />} />
        <Route path="/user-dashboard" element={<UserDashboard />} />
        <Route path="/my-profile" element={<MyProfile />} />
        <Route path="/beneficiary" element={<Beneficiary />} />
      </Routes>
    </>
  ) : uToken ? (
    <>
      <ToastContainer />
      <Routes>
        <Route path="/verification" element={<Verification />} />
      </Routes>
    </>
  ) : (
    <>
      <ToastContainer />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
      </Routes>
    </>
  );
  
}

export default App