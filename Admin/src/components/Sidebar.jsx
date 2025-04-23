import React, { useContext } from 'react'
import { AdminContext } from '../context/AdminContext'
import { NavLink } from 'react-router-dom'
import { assets } from '../assets/assets'

const Sidebar = () => {

  const {aToken} = useContext(AdminContext)

  return (
    <div className='min-h-screen bg-white border-r'>
        {
          aToken && <ul className='text-[#515151] mt-5'>

            <NavLink to={'/admin-dashboard'} className={({isActive})=> `flex items-center gap-3 py-3.5 px-3 md:px-9 md:min-w-72 cursor-pointer ${isActive ? 'bg-[#F2F3FF] border-r-4 border-primary' : ''}`}>
              <img src={assets.ab} alt='' /> 
              <p className='hidden md:block'>Dashboard</p>
            </NavLink>
            <NavLink to={'/fraud-transactions'} className={({isActive})=> `flex items-center gap-3 py-3.5 px-3 md:px-9 md:min-w-72 cursor-pointer ${isActive ? 'bg-[#F2F3FF] border-r-4 border-primary' : ''}`}>
              <img src={assets.ab} alt='' /> 
              <p className='hidden md:block'>Fraud-Payments</p>
            </NavLink>
            <NavLink to={'/suspecious-payments'} className={({isActive})=> `flex items-center gap-3 py-3.5 px-3 md:px-9 md:min-w-72 cursor-pointer ${isActive ? 'bg-[#F2F3FF] border-r-4 border-primary' : ''}`}>
              <img src={assets.ab} alt='' /> 
              <p className='hidden md:block'>Suspecious-Payments</p>
            </NavLink>
            <NavLink to={'/transaction-logs'} className={({isActive})=> `flex items-center gap-3 py-3.5 px-3 md:px-9 md:min-w-72 cursor-pointer ${isActive ? 'bg-[#F2F3FF] border-r-4 border-primary' : ''}`}>
              <img src={assets.ab} alt='' /> 
              <p className='hidden md:block'>Transaction Logs</p>
            </NavLink>
            <NavLink to={'/blocked-users'} className={({isActive})=> `flex items-center gap-3 py-3.5 px-3 md:px-9 md:min-w-72 cursor-pointer ${isActive ? 'bg-[#F2F3FF] border-r-4 border-primary' : ''}`}>
              <img src={assets.ab} alt='' /> 
              <p className='hidden md:block'>Blocked Users</p>
            </NavLink>
            <NavLink to={'/profile'} className={({isActive})=> `flex items-center gap-3 py-3.5 px-3 md:px-9 md:min-w-72 cursor-pointer ${isActive ? 'bg-[#F2F3FF] border-r-4 border-primary' : ''}`}>
              <img src={assets.ab} alt='' /> 
              <p className='hidden md:block'>Profile</p>
            </NavLink>
            <NavLink to={'/update-pin'} className={({isActive})=> `flex items-center gap-3 py-3.5 px-3 md:px-9 md:min-w-72 cursor-pointer ${isActive ? 'bg-[#F2F3FF] border-r-4 border-primary' : ''}`}>
              <img src={assets.ab} alt='' /> 
              <p className='hidden md:block'>Update PIN</p>
            </NavLink>

          </ul>
        }
    </div>
  )

  
}

export default Sidebar