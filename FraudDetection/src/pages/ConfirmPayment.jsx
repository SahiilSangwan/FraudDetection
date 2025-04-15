import { useNavigate, useLocation } from "react-router-dom";
import React, { useContext } from 'react';
import { useState, useEffect } from "react";
import Header from "../components/Header";
import Footer from "../components/Footer";
import { UserContext } from '../context/UserContext';
import { toast } from 'react-toastify';

const ConfirmPayment = () => {
  const { state } = useLocation();
  const transactionData = state?.transactionData || {};
  const navigate = useNavigate();

  const bank = localStorage.getItem('bank') || "default";

  const {
    getTransacionsConfirmation,
    confirmationData,
    verifyMpin,
    verifyMpinOtp,
    getBankTheme,
    sendTOTP,
    mpinAmount,mpinLimitAmount,setMpinLimitAmount
  } = useContext(UserContext);

  const limitAmount = mpinLimitAmount || 20000; 

  // State for authentication
  const [showAuthModal, setShowAuthModal] = useState(false);
  const [authType, setAuthType] = useState('pin'); 
  const [pin, setPin] = useState("");
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
    ifsc: confirmationData.data.userIfscCode,
    phoneNumber: confirmationData.data.userPhoneNumber
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

  const sendOTP = async () => {
    setIsLoading(true);
    try {
      // This would be your actual OTP sending API call
      await sendTOTP();
      toast.success("OTP sent to your registered email");
      setOtpSent(true);
      setResendTimer(30);
    } catch (error) {
      toast.error("Failed to send OTP");
    } finally {
      setIsLoading(false);
    }
  };

  const handleConfirm = () => {
    if (!amount) {
      toast.error("Please enter an amount");
      return;
    }

    const amountNum = parseFloat(amount);
    if (amountNum <= 0) {
      toast.error("Amount must be greater than 0");
      return;
    }

    if (amountNum <= limitAmount) {
      // Below limit - only ask for MPIN
      setAuthType('pin');
      setShowAuthModal(true);
    } else {
      // Above limit - ask for both MPIN and OTP
      setAuthType('pinOtp');
      setShowAuthModal(true);
      sendOTP();
    }
  };

  const handlePinSubmit = async () => {
    if (pin.length !== 6) {
      toast.error("MPIN must be 6 digits");
      return;
    }

    setIsSubmitting(true);
    try {
      const isVerified = await verifyMpin(pin, selectedBeneficiaryID, receiverAcc, amount, ifscCodeUser, description);
      if (isVerified) {
        proceedWithPayment();
      } else {
        toast.error("Invalid MPIN. Please try again.");
      }
    } catch (error) {
      toast.error("Error verifying MPIN");
    } finally {
      setIsSubmitting(false);
      setPin("");
    }
  };

  const handlePinOtpSubmit = async () => {
    if (pin.length !== 6) {
      toast.error("MPIN must be 4 digits");
      return;
    }
    if (otp.length !== 6) {
      toast.error("OTP must be 6 digits");
      return;
    }

    setIsSubmitting(true);
    try {
      const isVerified = await verifyMpinOtp(pin, otp, selectedBeneficiaryID, receiverAcc, amount, ifscCodeUser, description);
      if (isVerified) {
        proceedWithPayment();
      } else {
        toast.error("Invalid MPIN or OTP. Please try again.");
      }
    } catch (error) {
      toast.error("Error verifying OTP");
    } finally {
      setIsSubmitting(false);
      setPin("");
      setOtp("");
    }
  };

  const proceedWithPayment = () => {
    // In a real app, you would call your payment API here
    toast.success("Payment processed successfully!");
    navigate('/user-dashboard');
  };

  const handleResendOtp = () => {
    if (resendTimer === 0) {
      setOtp("");
      sendOTP();
    }
  };

  const handleCancelAuth = () => {
    setShowAuthModal(false);
    setPin("");
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

  useEffect(() => {
    mpinAmount()
  }, [setMpinLimitAmount]);

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
                    <span className="font-mono">{receiver.accountNumber || "N/A"}</span>
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
              <p className="text-3xl font-bold text-gray-800 mt-1">₹{parseFloat(amount).toLocaleString()}</p>
              {parseFloat(amount) > limitAmount && (
                <p className="text-sm text-orange-600 mt-1">
                  Amount exceeds your limit of ₹{limitAmount.toLocaleString()}. OTP verification required.
                </p>
              )}
            </div>

            {/* Confirm Button */}
            <button
              onClick={handleConfirm}
              className={`w-full mt-6 py-3 px-4 ${getBankTheme(bank).button} hover:opacity-90 text-white rounded-lg font-semibold shadow-md transition-colors flex items-center justify-center ${
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
          </div>
        </div>
      </main>

      {/* Authentication Modal */}
      {showAuthModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md">
            <div className="p-6">
              <h2 className="text-xl font-bold text-gray-800 mb-4">
                {authType === 'pin' ? 'Enter MPIN' : 'Security Verification'}
              </h2>
              
              {authType === 'pin' ? (
                <>
                  <p className="text-gray-600 mb-6">Please enter your 6-digit MPIN to authorize this transaction.</p>
                  
                  <div className="mb-6">
                    <label className="block text-gray-700 mb-2">MPIN</label>
                    <input
                      type="password"
                      value={pin}
                      onChange={(e) => setPin(e.target.value.replace(/\D/g, '').slice(0, 6))}
                      className="w-full p-3 border border-gray-300 rounded-lg text-center text-xl font-mono tracking-widest"
                      placeholder="••••••"
                      maxLength="6"
                      inputMode="numeric"
                      pattern="[0-9]*"
                      required
                      autoFocus
                    />
                  </div>
                  
                  <div className="flex justify-end space-x-3">
                    <button
                      onClick={handleCancelAuth}
                      disabled={isSubmitting}
                      className="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50"
                    >
                      Cancel
                    </button>
                    <button
                      onClick={handlePinSubmit}
                      disabled={isSubmitting || pin.length !== 6}
                      className={`px-4 py-2 ${getBankTheme(bank).button} text-white rounded-lg ${isSubmitting || pin.length !== 6 ? 'opacity-50' : 'hover:opacity-90'}`}
                    >
                      {isSubmitting ? 'Verifying...' : 'Confirm'}
                    </button>
                  </div>
                </>
              ) : (
                <>
                  <p className="text-gray-600 mb-6">
                    This transaction exceeds your limit of ₹{limitAmount.toLocaleString()}. 
                    Please enter both your MPIN and OTP sent to your registered email.
                  </p>
                  
                  <div className="mb-4">
                    <label className="block text-gray-700 mb-2">MPIN</label>
                    <input
                      type="password"
                      value={pin}
                      onChange={(e) => setPin(e.target.value.replace(/\D/g, '').slice(0, 6))}
                      className="w-full p-3 border border-gray-300 rounded-lg text-center text-xl font-mono tracking-widest"
                      placeholder="••••••"
                      maxLength="6"
                      inputMode="numeric"
                      pattern="[0-9]*"
                      required
                      autoFocus
                    />
                  </div>
                  
                  <div className="mb-6">
                    <label className="block text-gray-700 mb-2">OTP</label>
                    <input
                      type="text"
                      value={otp}
                      onChange={(e) => setOtp(e.target.value.replace(/\D/g, '').slice(0, 6))}
                      className="w-full p-3 border border-gray-300 rounded-lg text-center text-xl font-mono tracking-widest"
                      placeholder="••••••"
                      maxLength="6"
                      inputMode="numeric"
                      pattern="[0-9]*"
                      required
                    />
                    <button
                      onClick={handleResendOtp}
                      disabled={resendTimer > 0}
                      className="text-sm text-blue-600 hover:text-blue-800 mt-2 disabled:text-gray-400"
                    >
                      {resendTimer > 0 ? `Resend OTP in ${resendTimer}s` : 'Resend OTP'}
                    </button>
                  </div>
                  
                  <div className="flex justify-end space-x-3">
                    <button
                      onClick={handleCancelAuth}
                      disabled={isSubmitting}
                      className="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50"
                    >
                      Cancel
                    </button>
                    <button
                      onClick={handlePinOtpSubmit}
                      disabled={isSubmitting || pin.length !== 6 || otp.length !== 6}
                      className={`px-4 py-2 ${getBankTheme(bank).button} text-white rounded-lg ${isSubmitting || pin.length !== 6 || otp.length !== 6 ? 'opacity-50' : 'hover:opacity-90'}`}
                    >
                      {isSubmitting ? 'Verifying...' : 'Confirm'}
                    </button>
                  </div>
                </>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Footer */}
      <Footer />
    </div>
  );
};

export default ConfirmPayment;