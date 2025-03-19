import React, { useContext } from 'react'
import Login from './pages/Login'
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';
import { AdminContext } from './context/AdminContext'
import Navbar from './components/Navbar'
import Sidebar from './components/Sidebar';
import { Route, Routes } from 'react-router-dom';
// import Dashboard from './pages/Dashboard';



const App = () => {

  // const {aToken} = useContext(AdminContext)

  // return aToken
  // ? (
  //   <div className='bg-[#F8F9FD]'>
  //     <ToastContainer/>
  //     <Navbar />

  //     <div className='flex items-start'>
  //       <Sidebar />

  //       <Routes>
  //         {/* Admin Route */}
  //         <Route path='/' element={<></>} />
  //         {/* <Route path='/admin-dashboard' element={<Dashboard />} /> */}
  //       </Routes>

  //     </div>

  //   </div>
  // ) 
  // : (
  return(
    <>
      <Login />
      <ToastContainer/>
    </>
  )
  // )
}

export default App