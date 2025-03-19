import React, { useEffect, useState } from "react";
import { useContext } from "react";
import { UserContext } from "../context/UserContext";
import Header from "../components/Header";
import Footer from "../components/Footer";
import { assets } from "../assets/assets";

const Profile = () => {
  const {user, getUser} = useContext(UserContext)

  useEffect(()=>{
      getUser()
  },[])

  return (
  <div className="flex flex-col min-h-screen">
    {/* Header */}
    <Header/>


    <div className="flex items-center justify-center h-[75vh] bg-gray-100">
      <div className="bg-white p-6 rounded-lg shadow-lg w-96 text-center">
        {/* Avatar */}
        <div className="flex justify-center">
          <img
            src={assets.avtar}
            alt="User Avatar"
            className="w-24 h-24 rounded-full border-4 border-blue-500"
          />
        </div>

        {/* User Info */}
        <h2 className="text-2xl font-semibold text-gray-800 mt-4">
          {user.firstName} {user.lastName}
        </h2>
        <p className="text-gray-500 text-sm">{user.email}</p>

        {/* User Details */}
        <div className="mt-4 text-left space-y-2">
          <p className="text-gray-700">
            <strong className="text-gray-900">Phone:</strong> {user.phoneNumber}
          </p>
          <p className="text-gray-700">
            <strong className="text-gray-900">Address:</strong> {user.address}
          </p>
          <p className="text-gray-700">
            <strong className="text-gray-900">Date of Birth:</strong> {user.dateOfBirth}
          </p>
          <p className="text-gray-700">
            <strong className="text-gray-900">Aadhar Card:</strong> {user.aadharCard || "Not Provided"}
          </p>
          <p className="text-gray-700">
            <strong className="text-gray-900">PAN Card:</strong> {user.panCard || "Not Provided"}
          </p>
        </div>
      </div>
    </div>

    {/* Footer */}
    <Footer />
  </div>
  );
};

export default Profile;
