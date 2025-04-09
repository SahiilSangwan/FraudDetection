import { useContext, useEffect, useState } from 'react';
import { FiEdit, FiSave, FiUpload, FiCalendar, FiMail, FiPhone, FiMapPin, FiUser } from 'react-icons/fi';
import { toast } from 'react-toastify'
import { AdminContext } from '../context/AdminContext'

const AdminProfile = () => {

  const {adminData,adminDetail} = useContext(AdminContext)

  const [isEditing, setIsEditing] = useState(false);


    useEffect(()=>{
        adminDetail()
    },[])

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-4xl mx-auto bg-white rounded-lg shadow-md overflow-hidden">
        {/* Header */}
        <div className="bg-indigo-600 p-6 text-white">
          <h1 className="text-2xl font-bold">Admin Profile</h1>
          <p className="opacity-80">Manage your account details</p>
        </div>

        {/* Profile Section */}
        <div className="p-6 md:p-8">
          <div className="flex flex-col md:flex-row gap-8">
            {/* Profile Picture */}
            <div className="flex flex-col items-center">
              <div className="relative mb-4">
                <img 
                  src="https://cdn0.iconfinder.com/data/icons/man-user-human-profile-avatar-business-person/100/09B-1User-512.png"
                  alt="Profile" 
                  className="w-32 h-32 rounded-full object-cover border-4 border-indigo-100"
                />
              </div>
              <h2 className="text-xl font-semibold">{adminData.firstName + " "+ adminData.lastName}</h2>
              <p className="text-gray-500">Administrator</p>
            </div>

            {/* Profile Details */}
            <div className="flex-1">
              <div className="flex justify-between items-center mb-6">
                <h3 className="text-lg font-medium">Personal Information</h3>
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Name */}
                <div className="space-y-1">
                  <label className="text-sm text-gray-500 flex items-center gap-2">
                    <FiUser size={14} /> Full Name
                  </label>
                  <p className="font-medium">{adminData.firstName + " "+ adminData.lastName}</p>
                </div>

                {/* Email */}
                <div className="space-y-1">
                  <label className="text-sm text-gray-500 flex items-center gap-2">
                    <FiMail size={14} /> Email Address
                  </label>
                  <p className="font-medium">{adminData.email}</p>
                </div>

                {/* Phone */}
                <div className="space-y-1">
                  <label className="text-sm text-gray-500 flex items-center gap-2">
                    <FiPhone size={14} /> Phone Number
                  </label>
                  <p className="font-medium">{"+91 " + adminData.phoneNumber}</p>
                </div>

                {/* Address */}
                <div className="space-y-1">
                  <label className="text-sm text-gray-500 flex items-center gap-2">
                    <FiMapPin size={14} /> Address
                  </label>
                  <p className="font-medium">{adminData.address}</p>
                </div>

                {/* Gender */}
                <div className="space-y-1">
                  <label className="text-sm text-gray-500 flex items-center gap-2">
                    <FiUser size={14} /> Gender
                  </label>
                  <p className="font-medium">{"Male"}</p>
                </div>

                {/* Date of Birth */}
                <div className="space-y-1">
                  <label className="text-sm text-gray-500 flex items-center gap-2">
                    <FiCalendar size={14} /> Date of Birth
                  </label>
                  <p className="font-medium">{new Date(adminData.dateOfBirth).toLocaleDateString()}</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default AdminProfile;