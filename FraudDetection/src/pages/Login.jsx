import React, { useState, useEffect, useRef } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import { assets } from '../assets/assets'; 

const Login = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const queryParams = new URLSearchParams(location.search);
  const bank = queryParams.get('bank') || 'default';

  // Dummy user credentials (Replace with real authentication)
  const users = {
    "user1@example.com": "Password123",
    "admin@bank.com": "Admin@123",
    "test@bank.com": "Test@456"
  };

  // Bank logos mapping
  const bankLogos = {
    sbi: assets?.sbi,
    hdfc: assets?.hdfc,
    icici: assets?.icici,
  };

  // Form state
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [captcha, setCaptcha] = useState('');
  const [generatedCaptcha, setGeneratedCaptcha] = useState('');

  const canvasRef = useRef(null);

  // Generate CAPTCHA
  const generateCaptcha = () => {
    const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789';
    let captchaText = '';
    for (let i = 0; i < 5; i++) {
      captchaText += chars.charAt(Math.floor(Math.random() * chars.length));
    }
    setGeneratedCaptcha(captchaText);
    drawCaptcha(captchaText);
  };

  // Draw CAPTCHA on canvas
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

  // Handle Login
  const handleLogin = (e) => {
    e.preventDefault();

    const trimmedEmail = email.trim();
    const trimmedPassword = password.trim();
    const trimmedCaptcha = captcha.trim();

    // Check if email exists and password matches
    if (!users[trimmedEmail] || users[trimmedEmail] !== trimmedPassword) {
      toast.error('Invalid email or password.');
      return;
    }

    // CAPTCHA validation
    if (!trimmedCaptcha || trimmedCaptcha.toLowerCase() !== generatedCaptcha.toLowerCase()) {
      toast.error('CAPTCHA is incorrect.');
      return;
    }

    // Successful login
    toast.success(`Login successful for ${bank.toUpperCase()}!`);
    navigate(`/verification?bank=${bank}&email=${trimmedEmail}`);
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100">

      <div className="bg-white p-8 rounded-lg shadow-md w-96">
        {/* Bank Logo */}
        <div className="flex justify-center mb-4">
          <img
            src={bankLogos[bank] || 'fallback-image.png'}
            alt={`${bank.toUpperCase()} Logo`}
            className="w-24 h-24"
          />
        </div>

        <h2 className="text-2xl font-bold text-center mb-4">
          Login to {bank.toUpperCase()}
        </h2>

        {/* Login Form */}
        <form onSubmit={handleLogin}>
          {/* Email Field */}
          <div className="mb-4">
            <label className="block text-gray-700 font-semibold">Email</label>
            <input
              type="email"
              className="w-full p-2 border border-gray-300 rounded"
              placeholder="Enter your email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />
          </div>

          {/* Password Field */}
          <div className="mb-4">
            <label className="block text-gray-700 font-semibold">Password</label>
            <input
              type="password"
              className="w-full p-2 border border-gray-300 rounded"
              placeholder="Enter your password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          {/* CAPTCHA Section */}
          <div className="mb-4">
            <label className="block text-gray-700 font-semibold">Enter CAPTCHA</label>
            <div className="flex items-center">
              <canvas ref={canvasRef} width="120" height="40" className="border rounded" />
              <button
                type="button"
                onClick={generateCaptcha}
                className="ml-2 text-sm bg-gray-300 px-2 py-1 rounded"
              >
                Refresh
              </button>
            </div>
            <input
              type="text"
              className="w-full p-2 border border-gray-300 rounded mt-2"
              placeholder="Enter CAPTCHA here"
              value={captcha}
              onChange={(e) => setCaptcha(e.target.value)}
              required
            />
          </div>

          {/* Login Button */}
          <button
            type="submit"
            className="w-full bg-blue-500 text-white p-2 rounded hover:bg-blue-600 transition"
          >
            Login
          </button>
        </form>
      </div>
    </div>
  );
};

export default Login;

