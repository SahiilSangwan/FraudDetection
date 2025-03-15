// import React, { useState, useEffect } from "react";
// import { useLocation, useNavigate } from "react-router-dom";
// import axios from "axios";
// import { toast } from "react-toastify";
// import "react-toastify/dist/ReactToastify.css";
// import { assets } from "../assets/assets"; // Bank logos

// const Verification = () => {
//   const location = useLocation();
//   const navigate = useNavigate();
//   const queryParams = new URLSearchParams(location.search);
//   const bank = queryParams.get("bank") || "default";
//   const email = queryParams.get("email") || "";

//   // Bank logos mapping
//   const bankLogos = {
//     sbi: assets?.sbi,
//     hdfc: assets?.hdfc,
//     icici: assets?.icici,
//   };

//   // State
//   const [otp, setOtp] = useState("");
//   const [generatedOtp, setGeneratedOtp] = useState(null);
//   const [isOtpSent, setIsOtpSent] = useState(false);

//   // Function to generate a random OTP
//   const generateOtp = () => Math.floor(100000 + Math.random() * 900000).toString(); // 6-digit OTP

//   // Function to send OTP (Mock API Call)
//   const sendOtp = async () => {
//     if (!email) {
//       toast.error("Email not provided. Please go back and enter your email.");
//       return;
//     }

//     const newOtp = generateOtp();
//     setGeneratedOtp(newOtp);
//     setIsOtpSent(true);

//     try {
//       // Mocking email API - Replace this with your actual backend API
//       await axios.post("https://jsonplaceholder.typicode.com/posts", {
//         email,
//         otp: newOtp,
//       });

//       toast.success(`OTP sent to ${email}. Check your inbox.`);
//     } catch (error) {
//       toast.error("Failed to send OTP. Try again.");
//       console.error("OTP Send Error:", error);
//     }
//   };

//   // Function to verify OTP
//   const verifyOtp = (e) => {
//     e.preventDefault();

//     if (!isOtpSent) {
//       toast.error("Please request an OTP first.");
//       return;
//     }

//     if (otp.trim() === generatedOtp) {
//       toast.success("OTP verified successfully!");
//       setTimeout(() => navigate("/dashboard"), 2000); // Redirect after success
//     } else {
//       toast.error("Invalid OTP. Please try again.");
//     }
//   };

//   // Auto-send OTP when page loads
//   useEffect(() => {
//     sendOtp();
//   }, []);

//   return (
//     <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100">
//       <div className="bg-white p-8 rounded-lg shadow-md w-96">
//         {/* Bank Logo */}
//         <div className="flex justify-center mb-4">
//           <img src={bankLogos[bank] || "fallback-image.png"} alt={`${bank.toUpperCase()} Logo`} className="w-24 h-24" />
//         </div>

//         <h2 className="text-2xl font-bold text-center mb-4">OTP Verification</h2>
//         <p className="text-gray-600 text-center mb-4">Enter the OTP sent to <strong>{email}</strong></p>

//         {/* OTP Form */}
//         <form onSubmit={verifyOtp}>
//           <div className="mb-4">
//             <label className="block text-gray-700 font-semibold">Enter OTP</label>
//             <input
//               type="text"
//               className="w-full p-2 border border-gray-300 rounded text-center text-xl tracking-widest"
//               placeholder="6-digit OTP"
//               value={otp}
//               onChange={(e) => setOtp(e.target.value)}
//               maxLength={6}
//               required
//             />
//           </div>

//           {/* Verify Button */}
//           <button type="submit" className="w-full bg-green-500 text-white p-2 rounded hover:bg-green-600 transition">
//             Verify OTP
//           </button>
//         </form>

//         {/* Resend OTP Button */}
//         <button
//           type="button"
//           onClick={sendOtp}
//           className="mt-4 w-full bg-gray-400 text-white p-2 rounded hover:bg-gray-500 transition"
//         >
//           Resend OTP
//         </button>

//         {/* OTP Rules & Regulations */}
//         <div className="mt-6 text-sm text-gray-600">
//           <h3 className="font-semibold text-center mb-2">OTP Rules & Regulations</h3>
//           <ul className="list-disc list-inside">
//             <li>OTP is valid for 5 minutes.</li>
//             <li>Do not share your OTP with anyone.</li>
//             <li>Make sure your email is correct before requesting an OTP.</li>
//             <li>If you don't receive an OTP, check your spam folder.</li>
//           </ul>
//         </div>
//       </div>
//     </div>
//   );
// };

// export default Verification;







import React, { useState, useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import axios from "axios";
import { toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { assets } from "../assets/assets"; // Bank logos

const Verification = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const queryParams = new URLSearchParams(location.search);
  const bank = queryParams.get("bank") || "default";
  const email = queryParams.get("email") || "";

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
      await axios.post("http://localhost:5000/send-otp", { email });
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
      const response = await axios.post("http://localhost:5000/verify-otp", { email, otp });

      if (response.data.message === "OTP verified successfully") {
        toast.success("OTP verified successfully!");
        setTimeout(() => navigate("/dashboard"), 2000); // Redirect after success
      }
    } catch (error) {
      toast.error(error.response?.data?.error || "Invalid OTP. Please try again.");
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
