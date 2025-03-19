import React, { useState, useEffect, useContext } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import axios from "axios";
import { toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { assets } from "../assets/assets"; // Bank logos
import { UserContext } from '../context/UserContext';

const Verification = () => {
  const navigate = useNavigate();
  const bank =localStorage.getItem('bank') || "default";

  const storedUser = JSON.parse(localStorage.getItem("user"));
  const email = storedUser?.email || "";

  const [purpose, setPurpose] = useState("login");
  const {setVToken, backendUrl} = useContext(UserContext);

  // Bank logos mapping
  const bankLogos = {
    sbi: assets?.sbi,
    hdfc: assets?.hdfc,
    icici: assets?.icici,
  };

  // State
  const [otp, setOtp] = useState("");
  const [isOtpSent, setIsOtpSent] = useState(false);

  // Function to send OTP via backend API
  const sendOtp = async () => {
    if (!email) {
      toast.error("Email not provided. Please go back and enter your email.");
      return;
    }

    try {
      await axios.post(backendUrl + '/users/sendotp', { email },{withCredentials: true});
      setIsOtpSent(true);
      toast.success(`OTP sent to ${email}. Check your inbox.`);
    } catch (error) {
      toast.error("Failed to send OTP. Try again.");
      console.error("OTP Send Error:", error);
    }
  };

  // Function to verify OTP
  const verifyOtp = async (e) => {
    e.preventDefault();

    if (!isOtpSent) {
      toast.error("Please request an OTP first.");
      return;
    }

    try {
      const {data} = await axios.post(backendUrl + '/users/verifyotp', { email, otp, purpose },{withCredentials: true});

      if (data.otpVerified) {
            localStorage.setItem('vToken',data.otp_token)
            setVToken(data.otp_token)
            toast.success("OTP verified successfully!");
            setTimeout(() => navigate("/user-dashboard"), 1000);
      }else{
        toast.error("Invalid OTP. Please try again.");
      }
    } catch (error) {
      toast.error(error.response?.data?.error);
    }
  };

  // Auto-send OTP when page loads
  useEffect(() => {
    sendOtp();
  }, []);

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100">
      <div className="bg-white p-8 rounded-lg shadow-md w-96">
        {/* Bank Logo */}
        <div className="flex justify-center mb-4">
          <img src={bankLogos[bank] || "fallback-image.png"} alt={`${bank.toUpperCase()} Logo`} className="w-24 h-24" />
        </div>

        <h2 className="text-2xl font-bold text-center mb-4">OTP Verification</h2>
        <p className="text-gray-600 text-center mb-4">Enter the OTP sent to <strong>{email}</strong></p>

        {/* OTP Form */}
        <form onSubmit={verifyOtp}>
          <div className="mb-4">
            <label className="block text-gray-700 font-semibold">Enter OTP</label>
            <input
              type="text"
              className="w-full p-2 border border-gray-300 rounded text-center text-xl tracking-widest"
              placeholder="6-digit OTP"
              value={otp}
              onChange={(e) => setOtp(e.target.value)}
              maxLength={6}
              required
            />
          </div>

          {/* Verify Button */}
          <button type="submit" className="w-full bg-green-500 text-white p-2 rounded hover:bg-green-600 transition">
            Verify OTP
          </button>
        </form>

        {/* Resend OTP Button */}
        <button
          type="button"
          onClick={sendOtp}
          className="mt-4 w-full bg-gray-400 text-white p-2 rounded hover:bg-gray-500 transition"
        >
          Resend OTP
        </button>
      </div>
    </div>
  );
};

export default Verification;
