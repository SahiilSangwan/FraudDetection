import React, { useContext, useState } from 'react'
import axios from 'axios'
import { toast } from 'react-toastify'
import { AdminContext } from '../context/AdminContext'
import { useNavigate } from 'react-router-dom'

const Login = () => {

    const navigate = useNavigate();
    const [email, setEmail] = useState('')
    const [password, setPassword] = useState('')
    const {setAToken, backendUrl} = useContext(AdminContext)

    const onSubmit = async (event) =>{
        event.preventDefault()

        try{
            const {data} = await axios.post(backendUrl + '/api/admin/login', {email,password},{withCredentials:true})
            if(data.success){
                toast.success(data.message)
                navigate("/");
                localStorage.setItem('aToken',data.token)
                localStorage.setItem('id',data.id)
                localStorage.setItem('email',data.email)
                setAToken(data.token)
            }else{
                toast.error(data.message)
            }

        }catch(error){
            toast.error(error.message)
        }
    }

  return (

     <form onSubmit={onSubmit} className="min-h-screen flex items-center justify-center bg-gray-50">
            <div className="flex flex-col gap-6 p-8 w-full max-w-md bg-white rounded-xl shadow-lg border border-gray-200 transform transition-all duration-300 hover:shadow-xl">
                <div className="text-center">
                <h1 className="text-3xl font-bold text-gray-800 mb-2">Admin Login</h1>
                <p className="text-gray-500">Access your admin dashboard</p>
                </div>

                <div className="space-y-4">
                <div className="space-y-1">
                    <label htmlFor="email" className="block text-sm font-medium text-gray-700">Email</label>
                    <div className="relative">
                    <input
                        id="email"
                        onChange={(e) => setEmail(e.target.value)}
                        value={email}
                        className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition"
                        type="email"
                        placeholder="admin@example.com"
                        required
                    />
                    <div className="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none">
                        <svg className="h-5 w-5 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                        <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z" />
                        <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z" />
                        </svg>
                    </div>
                    </div>
                </div>

                <div className="space-y-1">
                    <label htmlFor="password" className="block text-sm font-medium text-gray-700">Password</label>
                    <div className="relative">
                    <input
                        id="password"
                        onChange={(e) => setPassword(e.target.value)}
                        value={password}
                        className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent transition"
                        type="password"
                        placeholder="••••••••"
                        required
                    />
                    <div className="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none">
                        <svg className="h-5 w-5 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                        <path fillRule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clipRule="evenodd" />
                        </svg>
                    </div>
                    </div>
                </div>

                <button
                    type="submit"
                    className="w-full flex justify-center py-3 px-4 border border-transparent rounded-lg shadow-sm text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 transition"
                >
                    Sign in
                </button>
                </div>

                <div className="text-center text-sm text-gray-500">
                <p>Don't have an account? <a href="#" className="font-medium text-indigo-600 hover:text-indigo-500">Contact support</a></p>
                </div>
            </div>
            </form>
  )
}

export default Login