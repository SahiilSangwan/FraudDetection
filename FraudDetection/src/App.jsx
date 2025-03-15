import React from 'react'
import { Route, Routes } from 'react-router-dom'
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import Home from './pages/Home'
import Login from './pages/Login'
import UserDashboard from './pages/UserDashboard'
import MyProfile from './pages/MyProfile'
import Verification from './pages/Verification'

const App = () => {
  return (
    <>
    <ToastContainer/>
    <Routes>
      <Route path="/" element={<Home />} />
      <Route path="/login" element={<Login />} />
      <Route path="/user-dashboard" element={<UserDashboard/>} />
      <Route path="/my-profile" element={<MyProfile />} />
      <Route path="/verification" element={<Verification />} />
    </Routes>
    </>
  )
}

export default App