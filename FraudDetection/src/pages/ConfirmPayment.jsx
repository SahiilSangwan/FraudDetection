import React, {useContext} from 'react';
import { useSearchParams } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import Header from "../components/Header";
import Footer from "../components/Footer";
import { UserContext } from '../context/UserContext';
import { toast } from 'react-toastify';

const ConfirmPayment = () => {

  const {amount, description, getTransacionsConfirmation, confirmationData, sendTOTP, verifyTOTP,} = useContext(UserContext);

  const sender = confirmationData?.data ? {
    name: confirmationData.data.userName,
    accountNumber: confirmationData.data.userAccountNumber,
    ifsc: confirmationData.data.userIfscCode
  } : {};

  const receiver = confirmationData?.data ? {
    name: confirmationData.data.beneficiaryName,
    accountNumber: confirmationData.data.beneficiaryAccountNumber,
    ifsc: confirmationData.data.beneficiaryIfscCode
  } : {};

  const handleConfirm = () => {
    setShowOtpBox(true); // Show OTP input when user clicks Confirm
    sendTOTP();
  };

  const [showOtpBox, setShowOtpBox] = useState(false);
  const [otp, setOtp] = useState("");
  const [otpSent, setOtpSent] = useState(false);
  const [resendTimer, setResendTimer] = useState(30);

  // OTP Resend Timer Effect
  useEffect(() => {
    let timer;
    if (otpSent && resendTimer > 0) {
      timer = setTimeout(() => setResendTimer(resendTimer - 1), 1000);
    }
    return () => clearTimeout(timer);
  }, [otpSent, resendTimer]);

  // Function to Simulate Sending OTP
  const handleSendOtp = async () => {
    sendTOTP();
    setOtpSent(true);
    setResendTimer(30);
  };

  // Function to Resend OTP
  const handleResendOtp = () => {
    if (resendTimer === 0) {
      setOtp(""); // Clear OTP input
      handleSendOtp();
    }
  };

  // Cancel OTP Input
  const handleCancelOtp = () => {
    setShowOtpBox(false);
    setOtp("");
    setOtpSent(false);
    setResendTimer(30);
  };

  // Submit OTP Verification
  const handleOtpSubmit = async () => {
    if (otp.length === 6) {
      verifyTOTP(otp);
    } else {
      alert("Invalid OTP. Please enter a 6-digit OTP.");
    }
  };


  useEffect(() => {
    getTransacionsConfirmation();
  }, []);

  return (
    <div className="flex flex-col min-h-screen">
    {/* Header */}
    <Header />

          <div className="flex-grow max-w-lg mx-auto mt-10 p-6 bg-white shadow-lg rounded-lg border">
            {/* Payment Details Box */}
            <h2 className="text-xl font-bold text-center mb-4">Confirm Payment</h2>
            <div className="grid grid-cols-2 gap-4 border-b pb-4">
              {/* Sender Details */}
              <div>
                <h3 className="font-semibold">Sender</h3>
                <p><strong>Name:</strong> {sender.name || "N/A"}</p>
                <p><strong>Account No.:</strong> {sender.accountNumber || "N/A"}</p>
                <p><strong>IFSC Code:</strong> {sender.ifsc || "N/A"}</p>
              </div>
              {/* Receiver Details */}
              <div>
                <h3 className="font-semibold">Receiver</h3>
                <p><strong>Name:</strong> {receiver.name || "N/A"}</p>
                <p><strong>Account No.:</strong> {receiver.accountNumber || "N/A"}</p>
                <p><strong>IFSC Code:</strong> {receiver.ifsc || "N/A"}</p>
              </div>
            </div>

            {/* Amount & Confirm Button */}
            <div className="mt-4 text-center">
              <p className="text-lg font-semibold mb-2">Amount: ₹{amount}</p>
              <button
                className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700"
                onClick={handleConfirm}
              >
                Confirm Payment
              </button>
            </div>

            {/* OTP Box (Appears After Clicking Confirm) */}
            {showOtpBox && (
              <div className="mt-4 p-4 bg-gray-100 rounded-lg">
                <h3 className="text-lg font-semibold">Enter OTP</h3>
                <input
                  type="text"
                  value={otp}
                  onChange={(e) => setOtp(e.target.value)}
                  maxLength={6}
                  placeholder="Enter 6-digit OTP"
                  className="w-full p-2 mt-2 border rounded-lg text-center"
                />
                <div className="flex justify-between mt-4">
                  <button
                    className="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600"
                    onClick={handleCancelOtp}
                  >
                    Cancel
                  </button>
                  <button
                    className="px-4 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600"
                    onClick={handleResendOtp}
                  >
                    Resend OTP
                  </button>
                  <button
                    className="px-4 py-2 bg-green-500 text-white rounded-lg hover:bg-green-600"
                    onClick={handleOtpSubmit}
                  >
                    Submit
                  </button>
                </div>
              </div>
            )}
          </div>

      {/* Footer */}
      <Footer />
    </div>
  );
};

export default ConfirmPayment;
