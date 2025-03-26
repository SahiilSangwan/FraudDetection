import { useNavigate, useLocation } from "react-router-dom";
import React, {useContext} from 'react';
import { useState, useEffect } from "react";
import Header from "../components/Header";
import Footer from "../components/Footer";
import { UserContext } from '../context/UserContext';
import { toast } from 'react-toastify';

const ConfirmPayment = () => {
  const { state } = useLocation();
  const transactionData = state?.transactionData || {};
  const navigate = useNavigate();

  const bank =localStorage.getItem('bank') || "default";

  const {getTransacionsConfirmation, confirmationData, sendTOTP, verifyTOTP,getBankTheme } = useContext(UserContext);

  // State for OTP functionality
  const [showOtpBox, setShowOtpBox] = useState(false);
  const [otp, setOtp] = useState("");
  const [otpSent, setOtpSent] = useState(false);
  const [resendTimer, setResendTimer] = useState(30);
  const [isLoading, setIsLoading] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Extract transaction details
  const {
    receiverAcc,
    ifscCodeUser,
    amount,
    description,
    beneficiaryName,
    beneficiaryBank,
    selectedBeneficiaryID
  } = transactionData;


    useEffect(() => {
    getTransacionsConfirmation(selectedBeneficiaryID);
    }, []);


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

  // Handle OTP timer
  useEffect(() => {
    let timer;
    if (otpSent && resendTimer > 0) {
      timer = setTimeout(() => setResendTimer(prev => prev - 1), 1000);
    }
    return () => clearTimeout(timer);
  }, [otpSent, resendTimer]);

  // Mock function for sending OTP (replace with real API call)
  const sendOTP = async () => {
    setIsLoading(true);
    try {
      sendTOTP();
      await new Promise(resolve => setTimeout(resolve, 1000));
      setOtpSent(true);
      setResendTimer(30);
    } catch (error) {
      toast.error("Failed to send OTP");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleConfirm = async () => {
    if (!amount) {
      toast.error("Please enter an amount");
      return;
    }
    setShowOtpBox(true);
    sendOTP();
  };

  // Mock function for verifying OTP (replace with real API call)
  const handleOtpSubmit = async () => {
    if (otp.length !== 6) {
      toast.error("Please enter a valid 6-digit OTP");
      return;
    }

    setIsSubmitting(true);
    try {
      verifyTOTP(otp, selectedBeneficiaryID, receiverAcc, amount, ifscCodeUser, description);
      
    } catch (error) {
      toast.error("OTP verification failed");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleResendOtp = () => {
    if (resendTimer === 0) {
      setOtp("");
      sendTOTP();
    }
  };

  const handleCancelOtp = () => {
    setShowOtpBox(false);
    setOtp("");
    setOtpSent(false);
  };

  // If no transaction data, redirect back
  if (!transactionData || !amount) {
    useEffect(() => {
      toast.error("Invalid transaction data");
      navigate('/transactions');
    }, []);
    return null;
  }

  return (


      <div className="flex flex-col min-h-screen bg-gradient-to-br from-blue-50 to-indigo-50">
          {/* Header */}
          <Header />

          <main className="flex-grow flex items-center justify-center p-4">
            <div className="w-full max-w-md bg-white rounded-xl shadow-lg overflow-hidden border border-gray-100">
              {/* Payment Header */}
              <div className={`${getBankTheme(bank).header} p-6 text-center`}>
                <h2 className="text-2xl font-bold text-white">Confirm Payment</h2>
                <svg className="w-10 h-10 mx-auto mt-2 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
                </svg>
              </div>

              {/* Payment Details */}
              <div className="p-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 pb-6 border-b border-gray-200">
                  {/* Sender Card */}
                  <div className="bg-gray-50 p-4 rounded-lg">
                    <h3 className="text-lg font-semibold text-gray-800 mb-3 flex items-center">
                      <svg className="w-5 h-5 mr-2 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                      Sender
                    </h3>
                    <div className="space-y-2 text-sm">
                      <p className="flex items-center">
                        <span className="text-gray-500 w-24">Name:</span>
                        <span className="font-medium">{sender.name || "N/A"}</span>
                      </p>
                      <p className="flex items-center">
                        <span className="text-gray-500 w-24">Account:</span>
                        <span className="font-mono">{sender.accountNumber || "N/A"}</span>
                      </p>
                      <p className="flex items-center">
                        <span className="text-gray-500 w-24">IFSC:</span>
                        <span className="font-medium">{sender.ifsc || "N/A"}</span>
                      </p>
                    </div>
                  </div>

                  {/* Receiver Card */}
                  <div className="bg-gray-50 p-4 rounded-lg">
                    <h3 className="text-lg font-semibold text-gray-800 mb-3 flex items-center">
                      <svg className="w-5 h-5 mr-2 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                      Receiver
                    </h3>
                    <div className="space-y-2 text-sm">
                      <p className="flex items-center">
                        <span className="text-gray-500 w-24">Name:</span>
                        <span className="font-medium">{receiver.name || "N/A"}</span>
                      </p>
                      <p className="flex items-center">
                        <span className="text-gray-500 w-24">Account:</span>
                        <span className="font-mono">{receiver.accountNumber|| "N/A"}</span>
                      </p>
                      <p className="flex items-center">
                        <span className="text-gray-500 w-24">IFSC:</span>
                        <span className="font-medium">{receiver.ifsc || "N/A"}</span>
                      </p>
                    </div>
                  </div>
                </div>

                {/* Amount Section */}
                <div className="mt-6 text-center">
                  <p className="text-gray-500">Transfer Amount</p>
                  <p className="text-3xl font-bold text-gray-800 mt-1">₹{amount.toLocaleString()}</p>
                </div>

                {/* Confirm Button */}
                <button
                  onClick={handleConfirm}
                  className={`w-full mt-6 py-3 px-4 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-semibold shadow-md transition-colors flex items-center justify-center ${
                    isSubmitting ? 'opacity-75 cursor-not-allowed' : ''
                  }`}
                  disabled={isSubmitting}
                >
                  {isSubmitting ? (
                    <>
                      <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                      </svg>
                      Processing...
                    </>
                  ) : (
                    'Confirm Payment'
                  )}
                </button>

                {/* OTP Box */}
                {showOtpBox && (
                  <div className="mt-6 p-4 bg-gray-50 rounded-lg border border-gray-200">
                    <div className="flex items-center mb-3">
                      <svg className="w-5 h-5 mr-2 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 18h.01M8 21h8a2 2 0 002-2V5a2 2 0 00-2-2H8a2 2 0 00-2 2v14a2 2 0 002 2z" />
                      </svg>
                      <h3 className="text-lg font-semibold">Enter OTP</h3>
                    </div>
                    
                    <p className="text-sm text-gray-600 mb-4">
                      We've sent a 6-digit OTP to your registered mobile number ending with ••••{sender.phoneNumber?.slice(-2)}
                    </p>
                    
                    {/* OTP Input */}
                    <div className="relative">
                      <input
                        type="text"
                        inputMode="numeric"
                        value={otp}
                        onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                        maxLength={6}
                        placeholder="• • • • • •"
                        className="w-full px-4 py-3 border border-gray-300 rounded-lg text-center text-xl font-mono tracking-widest focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                        disabled={isSubmitting}
                        autoFocus
                      />
                    </div>
                    
                    {/* OTP Action Buttons */}
                    <div className="flex justify-between mt-4 space-x-2">
                      <button
                        onClick={handleCancelOtp}
                        disabled={isSubmitting}
                        className={`flex-1 py-2 px-3 bg-red-100 hover:bg-red-200 text-red-700 rounded-lg transition-colors ${
                          isSubmitting ? 'opacity-50 cursor-not-allowed' : ''
                        }`}
                      >
                        Cancel
                      </button>
                      
                      <button
                        onClick={handleResendOtp}
                        disabled={isSubmitting || resendTimer > 0}
                        className={`flex-1 py-2 px-3 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-lg transition-colors ${
                          (isSubmitting || resendTimer > 0) ? 'opacity-50 cursor-not-allowed' : ''
                        }`}
                      >
                        {resendTimer > 0 ? `Resend (${resendTimer}s)` : 'Resend OTP'}
                      </button>
                      
                      <button
                        onClick={handleOtpSubmit}
                        disabled={isSubmitting || otp.length !== 6}
                        className={`flex-1 py-2 px-3 bg-green-600 hover:bg-green-700 text-white rounded-lg transition-colors ${
                          (isSubmitting || otp.length !== 6) ? 'opacity-50 cursor-not-allowed' : ''
                        }`}
                      >
                        {isSubmitting ? (
                          <>
                            <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white inline" fill="none" viewBox="0 0 24 24">
                              <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                              <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                            </svg>
                            Verifying...
                          </>
                        ) : 'Submit'}
                      </button>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </main>

          {/* Footer */}
          <Footer />
        </div>
  );
};

export default ConfirmPayment;