import React, { useEffect, useState } from "react";
import { useContext } from "react";
import { UserContext } from "../context/UserContext";
import Header from "../components/Header";
import Footer from "../components/Footer";
import { assets } from "../assets/assets";

const Profile = () => {

  const bank =localStorage.getItem('bank') || "default";

  const {user, getUser, getBankTheme} = useContext(UserContext)

  useEffect(()=>{
      getUser()
  },[])

  return (

      <div className="flex flex-col min-h-screen bg-gradient-to-br from-blue-50 to-indigo-50">
          {/* Header */}
          <Header />

          <div className="flex-grow flex items-center justify-center p-4">
            <div className="bg-white rounded-xl shadow-xl overflow-hidden w-full max-w-md">
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
          </div>

          {/* Footer */}
          <Footer />
        </div>
  );
};

export default Profile;
