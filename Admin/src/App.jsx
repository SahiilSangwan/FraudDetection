import React, { useContext } from 'react'
import Login from './pages/Login'
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { AdminContext } from './context/AdminContext'
import Navbar from './components/Navbar'
import Sidebar from './components/Sidebar';
import { Route, Routes } from 'react-router-dom';
import AdminDashBoard from './pages/Dashboard';
import Profile from './pages/Profile';
import Fraudtransactions from './pages/Fraudtransactions';
import Suspeciouspayments from './pages/Suspeciouspayments';4
import Transactionlogs from './pages/Transactionlogs';
import Blockedusers from './pages/Blockedusers';
import Updatepin from './pages/Updatepin';
import Landing from './pages/Landing';

const App = () => {

  const {aToken} = useContext(AdminContext)

  return aToken
  ? (
    <div className='bg-[#F8F9FD]'>
      <ToastContainer/>
      <Navbar />

      <div className='flex items-start'>
        <Sidebar />

        <Routes>
          {/* Admin Route */}
          <Route path='/' element={<Landing/>} />
          <Route path='/admin-dashboard' element={<AdminDashBoard/>} />
          <Route path='/fraud-transactions' element={<Fraudtransactions/>} />
          <Route path='/suspecious-payments' element={<Suspeciouspayments/>} />
          <Route path='/transaction-logs' element={<Transactionlogs/>} />
          <Route path='/blocked-users' element={<Blockedusers/>} />
          <Route path='/profile' element={<Profile/>} />
          <Route path='/update-pin' element={<Updatepin/>} />

    
        </Routes>

      </div>

    </div>
  ) 
  : (
    <div className='bg-[#F8F9FD]'>
     <ToastContainer/>
        <div>
            <Routes>
            <Route path='/' element={<Login />} />
            </Routes>
        </div>
    </div>
  )
}

export default App