import { useState, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AdminContext } from '../context/AdminContext';
import axios from 'axios';
import { toast } from 'react-toastify';
import { FiLock, FiMail, FiCheckCircle, FiRefreshCw, FiAlertCircle, FiSend, FiArrowLeft} from 'react-icons/fi';

const UpdatePinPage = () => {

  const {sendAOTP,backendUrl} = useContext(AdminContext)
  const navigate = useNavigate();
  // State management
  const [currentStep, setCurrentStep] = useState(1); // 1: Enter new PIN, 2: OTP verification, 3: Success
  const [newPin, setNewPin] = useState('');
  const [confirmPin, setConfirmPin] = useState('');
  const [otp, setOtp] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  // PIN validation
  const validatePin = (pin) => {
    return /^\d{4,6}$/.test(pin); // 4-6 digit PIN validation
  };

  // Handle PIN submission
  const handlePinSubmit = (e) => {
    e.preventDefault();
    setError('');

    if (!validatePin(newPin)) {
      setError('PIN must be 4-6 digits');
      return;
    }

    if (newPin !== confirmPin) {
      setError('PINs do not match');
      return;
    }

    sendAOTP();
    setIsLoading(true);
    setTimeout(() => {
      setIsLoading(false);
      setCurrentStep(2); // Move to OTP verification step
    }, 1500);
  };

  const mpin = newPin;
  const email = localStorage.getItem('email')

  // Handle OTP submission
  const handleOtpSubmit = (e) => {
    e.preventDefault();
    setError('');

    if (!/^\d{6}$/.test(otp)) {
      setError('OTP must be 6 digits');
      return;
    }

    verifyAOTP();

  };


    const verifyAOTP = async () =>{

      try {
        const purpose = "Admin"
        const {data} = await axios.post(backendUrl + '/api/admin/verifyotp', { email, otp, purpose },{withCredentials: true});
  
        if (data.otpVerified) {
                    toast.success("OTP verified successfully!");
                    const { data } = await axios.put(backendUrl+`/api/admin/update-mpin`,{email,mpin},{ withCredentials: true });
                        if(data.success){
                          setIsLoading(true);
                          setTimeout(() => {
                            setIsLoading(false);
                            setSuccessMessage('PIN successfully updated!');
                            setCurrentStep(3); // Move to success step
                          }, 1500);
                        }else{
                            toast.error("Failed to update PIN. Please try again.");
                        }
        }else{
          toast.error("Invalid OTP. Please try again.");
        }
      } catch (error) {
        toast.error(error.response?.data?.error);
      }
    }

  // Resend OTP
  const handleResendOtp = () => {
    sendAOTP();
    setIsLoading(true);
    setTimeout(() => {
      setIsLoading(false);
      setError('');
      // In a real app, you would show a success message here
    }, 1000);
  };

  // Reset form
  const resetForm = () => {
    setCurrentStep(1);
    setNewPin('');
    setConfirmPin('');
    setOtp('');
    setSuccessMessage('');
    setError('');
    navigate("/admin-dashboard");
  };

  return (
      <div className="flex">
        {/* Main Content */}
        <div className="flex-1 md:ml-64 min-h-screen bg-gray-50 p-4 md:p-8">
          {/* Expanded Card Container */}
          <div className="bg-white rounded-xl shadow-lg w-full max-w-3xl mx-auto overflow-hidden border border-gray-200">
            {/* Header with gradient background */}
            <div className="bg-gradient-to-r from-indigo-600 to-indigo-800 p-6 md:p-8">
              <h1 className="text-2xl md:text-3xl font-bold text-white text-center">
                {currentStep === 1 && 'Update Your Security PIN'}
                {currentStep === 2 && 'Verify Your Identity'}
                {currentStep === 3 && 'PIN Successfully Updated'}
              </h1>
            </div>

            {/* Progress Steps - Enhanced */}
            <div className="px-8 py-6 border-b border-gray-100 bg-gray-50">
              <div className="flex justify-between relative">
                {/* Progress line */}
                <div className="absolute top-5 left-10 right-10 h-1 bg-gray-200 z-0">
                  <div 
                    className="h-full bg-indigo-600 transition-all duration-500 ease-out"
                    style={{ width: currentStep === 1 ? '0%' : currentStep === 2 ? '50%' : '100%' }}
                  ></div>
                </div>
                
                {[1, 2, 3].map((step) => (
                  <div key={step} className="flex flex-col items-center z-10">
                    <div className={`w-12 h-12 rounded-full flex items-center justify-center transition-all duration-300
                      ${currentStep >= step ? 'bg-indigo-600 text-white shadow-lg' : 'bg-white text-gray-400 border-2 border-gray-300'}`}>
                      {step === 3 ? <FiCheckCircle size={24} /> : step}
                    </div>
                    <span className={`text-sm mt-3 font-medium transition-all duration-300
                      ${currentStep >= step ? 'text-indigo-600' : 'text-gray-500'}`}>
                      {step === 1 ? 'Enter PIN' : step === 2 ? 'Verify' : 'Complete'}
                    </span>
                  </div>
                ))}
              </div>
            </div>

            {/* Content Area - Expanded */}
            <div className="p-6 md:p-8">
              {error && (
                <div className="mb-6 p-4 bg-red-50 text-red-600 rounded-lg text-sm border border-red-100 flex items-center">
                  <FiAlertCircle className="mr-3 flex-shrink-0" />
                  <span>{error}</span>
                </div>
              )}

              {successMessage && (
                <div className="mb-6 p-4 bg-green-50 text-green-600 rounded-lg text-sm border border-green-100 flex items-center">
                  <FiCheckCircle className="mr-3 flex-shrink-0" />
                  <span>{successMessage}</span>
                </div>
              )}

              {/* Step 1: Enter New PIN - Enhanced */}
              {currentStep === 1 && (
                <form onSubmit={handlePinSubmit} className="space-y-6 max-w-lg mx-auto">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      New PIN (4-6 digits)
                    </label>
                    <div className="relative">
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <FiLock className="text-gray-400" />
                      </div>
                      <input
                        type="password"
                        inputMode="numeric"
                        pattern="\d*"
                        maxLength={6}
                        className="pl-10 pr-4 py-3 w-full border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition text-lg"
                        placeholder="Enter your new PIN"
                        value={newPin}
                        onChange={(e) => setNewPin(e.target.value.replace(/\D/g, ''))}
                        required
                      />
                    </div>
                    <p className="mt-1 text-xs text-gray-500">Must be 4-6 numeric digits</p>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Confirm New PIN
                    </label>
                    <div className="relative">
                      <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <FiLock className="text-gray-400" />
                      </div>
                      <input
                        type="password"
                        inputMode="numeric"
                        pattern="\d*"
                        maxLength={6}
                        className="pl-10 pr-4 py-3 w-full border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition text-lg"
                        placeholder="Confirm your new PIN"
                        value={confirmPin}
                        onChange={(e) => setConfirmPin(e.target.value.replace(/\D/g, ''))}
                        required
                      />
                    </div>
                  </div>

                  <div className="pt-4">
                    <button
                      type="submit"
                      className="w-full bg-indigo-600 text-white py-3 px-4 rounded-lg hover:bg-indigo-700 transition-all duration-300 shadow-md hover:shadow-lg flex justify-center items-center text-lg font-medium"
                      disabled={isLoading}
                    >
                      {isLoading ? (
                        <>
                          <FiRefreshCw className="animate-spin mr-3" />
                          Processing...
                        </>
                      ) : (
                        <>
                          <FiSend className="mr-3" />
                          Send Verification OTP
                        </>
                      )}
                    </button>
                  </div>
                </form>
              )}

              {/* Step 2: OTP Verification - Enhanced */}
              {currentStep === 2 && (
                <form onSubmit={handleOtpSubmit} className="max-w-lg mx-auto">
                  <div className="text-center mb-8">
                    <div className="bg-indigo-50 w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-4">
                      <FiMail className="text-3xl text-indigo-600" />
                    </div>
                    <h3 className="text-lg font-medium text-gray-800 mb-1">Verify Your Identity</h3>
                    <p className="text-gray-600">We've sent a 6-digit OTP to your registered email</p>
                    <p className="font-medium text-indigo-600 mt-2">admin@example.com</p>
                  </div>

                  <div className="mb-6">
                    <label className="block text-sm font-medium text-gray-700 mb-2">
                      Enter Verification Code
                    </label>
                    <div className="flex justify-center">
                      <input
                        type="text"
                        inputMode="numeric"
                        pattern="\d*"
                        maxLength={6}
                        className="px-4 py-4 w-64 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 text-center text-2xl tracking-widest font-mono transition"
                        placeholder="______"
                        value={otp}
                        onChange={(e) => setOtp(e.target.value.replace(/\D/g, ''))}
                        required
                      />
                    </div>
                  </div>

                  <div className="flex justify-between items-center mb-8">
                    <button
                      type="button"
                      onClick={handleResendOtp}
                      className="text-indigo-600 hover:text-indigo-800 flex items-center transition text-sm font-medium"
                      disabled={isLoading}
                    >
                      <FiRefreshCw className={`mr-2 ${isLoading ? 'animate-spin' : ''}`} />
                      Resend OTP
                    </button>

                    <span className="text-sm text-gray-500">Valid for 5 minutes</span>
                  </div>

                  <button
                    type="submit"
                    className="w-full bg-indigo-600 text-white py-3 px-4 rounded-lg hover:bg-indigo-700 transition-all duration-300 shadow-md hover:shadow-lg flex justify-center items-center text-lg font-medium"
                    disabled={isLoading}
                  >
                    {isLoading ? (
                      <>
                        <FiRefreshCw className="animate-spin mr-3" />
                        Verifying...
                      </>
                    ) : (
                      <>
                        <FiCheckCircle className="mr-3" />
                        Confirm & Update PIN
                      </>
                    )}
                  </button>
                </form>
              )}

              {/* Step 3: Success - Enhanced */}
              {currentStep === 3 && (
                <div className="text-center py-8 max-w-lg mx-auto">
                  <div className="bg-green-50 w-24 h-24 rounded-full flex items-center justify-center mx-auto mb-6">
                    <FiCheckCircle className="text-4xl text-green-600" />
                  </div>
                  <h2 className="text-2xl font-bold text-gray-800 mb-3">PIN Successfully Updated!</h2>
                  <p className="text-gray-600 mb-8 text-lg">
                    Your new security PIN has been activated and is ready for use.
                  </p>
                  <button
                    onClick={resetForm}
                    className="bg-indigo-600 text-white py-3 px-8 rounded-lg hover:bg-indigo-700 transition-all duration-300 shadow-md hover:shadow-lg text-lg font-medium"
                  >
                    Return to Dashboard
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
  );
};

export default UpdatePinPage