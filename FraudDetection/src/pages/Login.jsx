import React, { useState, useEffect, useRef, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { assets } from '../assets/assets'; 
import { UserContext } from '../context/UserContext';
import axios from 'axios';
import { FaArrowLeft } from "react-icons/fa";

const Login = () => {
  const navigate = useNavigate();
  const bank =localStorage.getItem('bank') || "default";
  bank.toUpperCase()

  const bankLogos = {
    finova: assets?.finova,
    wissen: assets?.wissen,
    heritage: assets?.heritage,
  };

  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [captcha, setCaptcha] = useState('');
  const [generatedCaptcha, setGeneratedCaptcha] = useState('');
  const {setUToken, backendUrl, getBankTheme, encryption} = useContext(UserContext);

  const canvasRef = useRef(null);

  const generateCaptcha = () => {
    const chars = 'abcdefghijkmnopqrstuvwxyz23456789';
    let captchaText = '';
    for (let i = 0; i < 5; i++) {
      captchaText += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    setGeneratedCaptcha(captchaText);
    drawCaptcha(captchaText);
  };

  const drawCaptcha = (text) => {
    const canvas = canvasRef.current;
    const ctx = canvas.getContext('2d');

    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.fillStyle = '#f3f3f3';
    ctx.fillRect(0, 0, canvas.width, canvas.height);

    ctx.font = '24px Arial';
    ctx.fillStyle = 'black';

    for (let i = 0; i < text.length; i++) {
      const x = 20 + i * 20;
      const y = 30 + Math.random() * 10;
      ctx.fillText(text[i], x, y);
    }

    ctx.strokeStyle = 'red';
    ctx.beginPath();
    for (let i = 0; i < 3; i++) {
      ctx.moveTo(Math.random() * 100, Math.random() * 40);
      ctx.lineTo(Math.random() * 100, Math.random() * 40);
    }
    ctx.stroke();
  };

  useEffect(() => {
    generateCaptcha();
  }, []);


  const onSubmit = async (event) =>{
    event.preventDefault()

    try{

        if (!captcha.trim() || captcha.trim().toLowerCase() !== generatedCaptcha.toLowerCase()) {
          toast.error('CAPTCHA is incorrect.');
          return;
        }
        const encryptedEmail = encryption(email); 
        const encryptedPassword = encryption(password);
        console.log("pass :"+encryptedPassword);

        const {data} = await axios.post(backendUrl + `/users/login?bank=${bank}`, {encryptedEmail,encryptedPassword},{withCredentials:true})
        if(data.status){
            navigate("/verification");
            localStorage.setItem('uToken',data.utoken)
            localStorage.setItem('user', JSON.stringify(data.user));
            setUToken(data.utoken)
        }else{
            toast.error(data.message)
        }

    }catch(error){
      toast.error(error.message);
    }
}

return (

        <div className={`min-h-screen flex items-center justify-center p-4 ${getBankTheme(bank).background}`}>
          <div className={`bg-white rounded-2xl shadow-xl overflow-hidden w-full max-w-md ${getBankTheme(bank).border}`}>
            {/* Bank Header*/}
            <div className={`${getBankTheme(bank).header} p-6 text-center relative`}>
              <button 
                  onClick={() => navigate('/')}
                  className="absolute left-4 top-6 text-white hover:text-gray-200 transition"
                  aria-label="Go back to login"
                >
                  <FaArrowLeft className="text-xl" />
              </button>
              <div className="flex justify-center mb-3">
                <img
                  src={bankLogos[bank] || 'fallback-image.png'}
                  alt={`${bank.toUpperCase()} Logo`}
                  className="w-16 h-16 object-contain"
                />
              </div>
              <h2 className="text-2xl font-bold text-white">
                Secure Login to {bank.toUpperCase()}
              </h2>
            </div>

            {/* Login Form */}
            <form onSubmit={onSubmit} className="p-8">
              
              <div className="mb-6">
                <label htmlFor="email" className="block text-gray-700 font-medium mb-2">Email Address</label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg className="h-5 w-5 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                      <path d="M2.003 5.884L10 9.882l7.997-3.998A2 2 0 0016 4H4a2 2 0 00-1.997 1.884z" />
                      <path d="M18 8.118l-8 4-8-4V14a2 2 0 002 2h12a2 2 0 002-2V8.118z" />
                    </svg>
                  </div>
                  <input
                    id="email"
                    type="email"
                    className={`w-full pl-10 pr-3 py-3 border border-gray-300 rounded-lg focus:ring-2 ${getBankTheme(bank).focus} transition`}
                    placeholder="your.email@example.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                  />
                </div>
              </div>

              <div className="mb-6">
                <label htmlFor="password" className="block text-gray-700 font-medium mb-2">Password</label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <svg className="h-5 w-5 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clipRule="evenodd" />
                    </svg>
                  </div>
                  <input
                    id="password"
                    type="password"
                    className={`w-full pl-10 pr-3 py-3 border border-gray-300 rounded-lg focus:ring-2 ${getBankTheme(bank).focus} transition`}
                    placeholder="••••••••"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                  />
                </div>
              </div>

              {/* CAPTCHA Section */}
              <div className="mb-6">
                <label htmlFor="captcha" className="block text-gray-700 font-medium mb-2">Security Code</label>
                <div className="flex items-center space-x-2">
                  <canvas 
                    ref={canvasRef} 
                    width="120" 
                    height="50" 
                    className="border border-gray-300 rounded bg-gray-50 flex-grow h-9"
                  />
                  <button
                    type="button"
                    onClick={generateCaptcha}
                    className={`p-1.5 rounded-md ${getBankTheme(bank).buttonSecondary}`}
                    aria-label="Refresh CAPTCHA"
                  >
                    <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
                    </svg>
                  </button>
                </div>
                <input
                  id="captcha"
                  type="text"
                  className={`w-full mt-2 px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 ${getBankTheme(bank).focus} transition`}
                  placeholder="Enter code"
                  value={captcha}
                  onChange={(e) => setCaptcha(e.target.value)}
                  required
                />
              </div>

              {/* Login Button*/}
              <button
                type="submit"
                className={`w-full ${getBankTheme(bank).button} text-white py-3 px-4 rounded-lg font-semibold hover:opacity-90 focus:outline-none focus:ring-2 focus:ring-offset-2 shadow-md transition-all`}
              >
                Sign In
              </button>

            </form>

            {/* Footer */}
            <div className="bg-gray-50 px-8 py-3 border-t border-gray-200">
              <div className="flex items-center justify-center space-x-2">
                <svg className="h-4 w-4 text-green-500" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clipRule="evenodd" />
                </svg>
                <span className="text-xs text-gray-600">Secure strong encryption</span>
              </div>
            </div>
          </div>
        </div>
  );
};

export default Login;

