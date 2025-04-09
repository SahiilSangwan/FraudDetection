import React, { useContext } from 'react'
import { assets } from '../assets/assets'
import { AdminContext } from '../context/AdminContext'
import { useNavigate } from 'react-router-dom'



const navbar = () => {

    const {aToken,setAToken,logoutuser } = useContext(AdminContext)

    const navigate = useNavigate()

    const logout = () => {
        navigate('/')
        aToken && setAToken('')
        aToken && localStorage.removeItem('aToken')
        logoutuser();
    }

  return (
    <div className='flex justify-between items-center px-4 sm:px-10 py-3 border-b bg-white '>
        <div className='flex items-center gap-2 text-xs'>
            <img className='w-36 sm:w-40 cursor-pointer' src={assets.logo} alt='' />
            <p className='border px-2.5 py-0.5 rounded-full text-gray-600 border-gray-500'>Admin</p>
        </div>
        <button onClick={logout} className='bg-black text-white text-sm px-10 py-2 rounded-full'>Logout</button>
    </div>
  )
}

export default navbar