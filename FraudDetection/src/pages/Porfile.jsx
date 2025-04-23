import React, { useEffect, useState } from "react";
import { useContext } from "react";
import { UserContext } from "../context/UserContext";
import Header from "../components/Header";
import Footer from "../components/Footer";
import { assets } from "../assets/assets";
import { toast } from "react-toastify";

const Profile = () => {
  const bank = localStorage.getItem('bank') || "default";
  const { user, getUser, getBankTheme, sendTOTP, verifyPinOTP, verifyLimitOTP, mpinAmount, mpinLimitAmount, setMpinLimitAmount} = useContext(UserContext);
  const [showLimitModal, setShowLimitModal] = useState(false);
  const [showPinModal, setShowPinModal] = useState(false);
  const [otp, setOtp] = useState("");
  const [newLimit, setNewLimit] = useState("");
  const [newPin, setNewPin] = useState("");
  const [confirmPin, setConfirmPin] = useState("");
  const [otpSent, setOtpSent] = useState(false);
  const [activeTab, setActiveTab] = useState("profile");

  useEffect(() => {
    getUser();
  }, []);

  const handleSendOtp = () => {
    sendTOTP()
    setOtpSent(true);
  };

const [isSettingLimit, setIsSettingLimit] = useState(false);

const handleSetLimit = async (e) => {
  e.preventDefault();
  
  // Validate amount
  if (!newLimit || isNaN(newLimit)) {
    toast.error("Please enter a valid amount");
    return;
  }
  console.log("newLimit", newLimit)

  if (newLimit <= 0) {
    toast.error("Amount must be greater than 0");
    setShowLimitModal(false);
    setNewLimit("");
    setOtp("");
    setOtpSent(false);
    return;
  }
  if (newLimit >= 20001) {
    toast.error("Amount must be Smaller or equal to 20,000/-");
    setShowLimitModal(false);
    setNewLimit("");
    setOtp("");
    setOtpSent(false);
    return;
  }

  setIsSettingLimit(true);
  try {
    const isUpdated = await verifyLimitOTP(otp, newLimit);
    if (isUpdated) {
      setShowLimitModal(false);
      setNewLimit("");
      setOtp("");
      setOtpSent(false);
      toast.success(`Transaction limit set to ₹${newLimit}`);
    }
  } catch (error) {
    toast.error("Failed to set transaction limit");
  } finally {
    setIsSettingLimit(false);
  }
};

const [isChangingPin, setIsChangingPin] = useState(false);

const handleChangePin = async (e) => {
  e.preventDefault();
  
  if (newPin.length !== 6) {
    toast.error("PIN must be 6 digits");
    return;
  }

  if (newPin !== confirmPin) {
    toast.error("PINs don't match");
    return;
  }

  setIsChangingPin(true);
  try {
    const isVerified = await verifyPinOTP(otp, newPin);
    if (isVerified) {
      setShowPinModal(false);
      setNewPin("");
      setConfirmPin("");
      setOtp("");
      setOtpSent(false);
    }
  } catch (error) {
    toast.error("Failed to change PIN");
  } finally {
    setIsChangingPin(false);
  }
};

  useEffect(() => {
    mpinAmount();
  }, [setMpinLimitAmount]);

  return (
    <div className="flex flex-col min-h-screen bg-gradient-to-br from-blue-50 to-indigo-50">
      {/* Header */}
      <Header />

      <div className="flex-grow p-4">
        <div className="max-w-4xl mx-auto">
          {/* Tabs */}
          <div className="flex mb-6 border-b border-gray-200">
            <button
              className={`py-2 px-4 font-medium ${activeTab === "profile" ? `text-${getBankTheme(bank).text} border-b-2 border-${getBankTheme(bank).text}` : "text-gray-500"}`}
              onClick={() => setActiveTab("profile")}
            >
              Profile Details
            </button>
            <button
              className={`py-2 px-4 font-medium ${activeTab === "security" ? `text-${getBankTheme(bank).text} border-b-2 border-${getBankTheme(bank).text}` : "text-gray-500"}`}
              onClick={() => setActiveTab("security")}
            >
              Security Settings
            </button>
          </div>

          {activeTab === "profile" ? (
            <div className="bg-white rounded-xl shadow-xl overflow-hidden">
              {/* Profile Header with Bank Theme */}
              <div className={`${getBankTheme(bank).header} p-6 text-center`}>
                <div className="relative inline-block">
                  <img
                    src={assets.avtar}
                    alt="User Avatar"
                    className="w-24 h-24 rounded-full border-4 border-white shadow-md"
                  />
                  <div className="absolute -bottom-2 -right-2 bg-white p-1 rounded-full shadow">
                    <div className={`${getBankTheme(bank).button} w-8 h-8 rounded-full flex items-center justify-center`}>
                      <svg className="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z" />
                      </svg>
                    </div>
                  </div>
                </div>
                <h2 className="text-2xl font-bold text-white mt-4">
                  {user.firstName} {user.lastName}
                </h2>
                <p className="text-white/90">{user.email}</p>
              </div>

              {/* User Details */}
              <div className="p-6 space-y-4">
                <div className="flex items-start">
                  <div className={`${getBankTheme(bank).button} p-2 rounded-lg mr-4`}>
                    <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                    </svg>
                  </div>
                  <div>
                    <h3 className="text-sm font-medium text-gray-500">Phone Number</h3>
                    <p className="text-gray-900">{user.phoneNumber}</p>
                  </div>
                </div>

                <div className="flex items-start">
                  <div className={`${getBankTheme(bank).button} p-2 rounded-lg mr-4`}>
                    <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
                    </svg>
                  </div>
                  <div>
                    <h3 className="text-sm font-medium text-gray-500">Address</h3>
                    <p className="text-gray-900">{user.address}</p>
                  </div>
                </div>

                <div className="flex items-start">
                  <div className={`${getBankTheme(bank).button} p-2 rounded-lg mr-4`}>
                    <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                  </div>
                  <div>
                    <h3 className="text-sm font-medium text-gray-500">Date of Birth</h3>
                    <p className="text-gray-900">{user.dateOfBirth}</p>
                  </div>
                </div>

                <div className="flex items-start">
                  <div className={`${getBankTheme(bank).button} p-2 rounded-lg mr-4`}>
                    <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                    </svg>
                  </div>
                  <div>
                    <h3 className="text-sm font-medium text-gray-500">Aadhar Card</h3>
                    <p className="text-gray-900">{user.aadharCard || "Not Provided"}</p>
                  </div>
                </div>

                <div className="flex items-start">
                  <div className={`${getBankTheme(bank).button} p-2 rounded-lg mr-4`}>
                    <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                    </svg>
                  </div>
                  <div>
                    <h3 className="text-sm font-medium text-gray-500">PAN Card</h3>
                    <p className="text-gray-900">{user.panCard || "Not Provided"}</p>
                  </div>
                </div>
              </div>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Set Transaction Limit Card */}
              <div className="bg-white rounded-xl shadow-xl overflow-hidden">
                <div className={`${getBankTheme(bank).header} p-4`}>
                  <h2 className="text-xl font-bold text-white">Transaction Limit</h2>
                </div>
                <div className="p-6">
                  <div className="flex items-center mb-4">
                    <div className={`${getBankTheme(bank).button} p-2 rounded-lg mr-4`}>
                      <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                    </div>
                    <div>
                      <h3 className="text-lg font-medium text-gray-900">Current Limit</h3>
                      <p className="text-gray-500">₹ {mpinLimitAmount} per transaction</p>
                    </div>
                  </div>
                  <button
                    onClick={() => setShowLimitModal(true)}
                    className={`w-full ${getBankTheme(bank).button} py-2 px-4 rounded-lg text-white font-medium hover:opacity-90 transition`}
                  >
                    Set New Limit
                  </button>
                </div>
              </div>

              {/* Change PIN Card */}
              <div className="bg-white rounded-xl shadow-xl overflow-hidden">
                <div className={`${getBankTheme(bank).header} p-4`}>
                  <h2 className="text-xl font-bold text-white">Change PIN</h2>
                </div>
                <div className="p-6">
                  <div className="flex items-center mb-4">
                    <div className={`${getBankTheme(bank).button} p-2 rounded-lg mr-4`}>
                      <svg className="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                      </svg>
                    </div>
                    <div>
                      <h3 className="text-lg font-medium text-gray-900">Card PIN</h3>
                      <p className="text-gray-500">***********</p>
                    </div>
                  </div>
                  <button
                    onClick={() => setShowPinModal(true)}
                    className={`w-full ${getBankTheme(bank).button} py-2 px-4 rounded-lg text-white font-medium hover:opacity-90 transition`}
                  >
                    Change PIN
                  </button>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Set Limit Modal */}
      {showLimitModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md">
            <div className={`${getBankTheme(bank).header} p-4 rounded-t-xl`}>
              <h2 className="text-xl font-bold text-white">Set Transaction Limit</h2>
            </div>
            <form onSubmit={handleSetLimit} className="p-6">
              <div className="mb-4">
                <label className="block text-gray-700 mb-2" htmlFor="limit">
                  New Transaction Limit (₹)
                </label>
                <input
                  type="number"
                  id="limit"
                  value={newLimit}
                  onChange={(e) => setNewLimit(e.target.value)}
                  className="w-full p-2 border border-gray-300 rounded-lg"
                  placeholder="Enter amount"
                  required
                />
              </div>
              {otpSent ? (
                <div className="mb-4">
                  <label className="block text-gray-700 mb-2" htmlFor="otp">
                    Enter OTP
                  </label>
                  <input
                    type="text"
                    id="otp"
                    value={otp}
                    onChange={(e) => setOtp(e.target.value)}
                    className="w-full p-2 border border-gray-300 rounded-lg"
                    placeholder="Enter 6-digit OTP"
                    maxLength="6"
                    required
                  />
                </div>
              ) : null}
              <div className="flex justify-between mt-6">
                <button
                  type="button"
                  onClick={() => {
                    setShowLimitModal(false);
                    setOtpSent(false);
                    setOtp("");
                  }}
                  className="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
                {otpSent ? (
                  <button
                    type="submit"
                    className={`px-4 py-2 ${getBankTheme(bank).button} rounded-lg text-white hover:opacity-90`}
                  >
                    Confirm
                  </button>
                ) : (
                  <button
                    type="button"
                    onClick={handleSendOtp}
                    className={`px-4 py-2 ${getBankTheme(bank).button} rounded-lg text-white hover:opacity-90`}
                  >
                    Send OTP
                  </button>
                )}
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Change PIN Modal */}
      {showPinModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md">
            <div className={`${getBankTheme(bank).header} p-4 rounded-t-xl`}>
              <h2 className="text-xl font-bold text-white">Change Card PIN</h2>
            </div>
            <form onSubmit={handleChangePin} className="p-6">
              {otpSent ? (
                <>
                  <div className="mb-4">
                    <label className="block text-gray-700 mb-2" htmlFor="newPin">
                      New PIN
                    </label>
                    <input
                      type="password"
                      id="newPin"
                      value={newPin}
                      onChange={(e) => setNewPin(e.target.value)}
                      className="w-full p-2 border border-gray-300 rounded-lg"
                      placeholder="Enter 4-digit PIN"
                      maxLength="6"
                      required
                    />
                  </div>
                  <div className="mb-4">
                    <label className="block text-gray-700 mb-2" htmlFor="confirmPin">
                      Confirm PIN
                    </label>
                    <input
                      type="password"
                      id="confirmPin"
                      value={confirmPin}
                      onChange={(e) => setConfirmPin(e.target.value)}
                      className="w-full p-2 border border-gray-300 rounded-lg"
                      placeholder="Confirm 4-digit PIN"
                      maxLength="6"
                      required
                    />
                  </div>
                  <div className="mb-4">
                    <label className="block text-gray-700 mb-2" htmlFor="otp">
                      Enter OTP
                    </label>
                    <input
                      type="text"
                      id="otp"
                      value={otp}
                      onChange={(e) => setOtp(e.target.value)}
                      className="w-full p-2 border border-gray-300 rounded-lg"
                      placeholder="Enter 6-digit OTP"
                      maxLength="6"
                      required
                    />
                  </div>
                </>
              ) : (
                <div className="mb-6 text-gray-600">
                  <p>We'll send an OTP to your registered email to verify this change.</p>
                </div>
              )}
              <div className="flex justify-between mt-6">
                <button
                  type="button"
                  onClick={() => {
                    setShowPinModal(false);
                    setOtpSent(false);
                    setOtp("");
                  }}
                  className="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
                {otpSent ? (
                  <button
                    type="submit"
                    className={`px-4 py-2 ${getBankTheme(bank).button} rounded-lg text-white hover:opacity-90`}
                  >
                    Change PIN
                  </button>
                ) : (
                  <button
                    type="button"
                    onClick={handleSendOtp}
                    className={`px-4 py-2 ${getBankTheme(bank).button} rounded-lg text-white hover:opacity-90`}
                  >
                    Send OTP
                  </button>
                )}
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Footer */}
      <Footer />
    </div>
  );
};

export default Profile;