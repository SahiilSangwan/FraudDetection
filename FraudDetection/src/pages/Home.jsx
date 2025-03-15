import React from 'react';
import { useNavigate } from 'react-router-dom';
import { assets } from '../assets/assets'; // Ensure assets are correctly imported

const Home = () => {
  const navigate = useNavigate();

  // Handle bank selection and navigate to login
  const handleBankClick = (bankName) => {
    console.log(`Bank clicked: ${bankName}`); // Debugging click event
    navigate(`/login?bank=${bankName}`);
  };

  return (
    <div className="flex flex-col items-center justify-center min-h-screen bg-gray-100">
      <h1 className="text-4xl font-bold mb-8">Choose Your Bank</h1>
      <div className="flex space-x-8">
        {/* Bank Image Buttons */}
        <img
          src={assets?.sbi || 'fallback-image.png'} 
          alt="SBI"
          className="w-32 h-32 cursor-pointer hover:scale-105 transition-transform"
          onClick={() => handleBankClick('sbi')}
        />
        <img
          src={assets?.hdfc || 'fallback-image.png'}
          alt="HDFC"
          className="w-32 h-32 cursor-pointer hover:scale-105 transition-transform"
          onClick={() => handleBankClick('hdfc')}
        />
        <img
          src={assets?.icici || 'fallback-image.png'}
          alt="ICICI"
          className="w-32 h-32 cursor-pointer hover:scale-105 transition-transform"
          onClick={() => handleBankClick('icici')}
        />
      </div>
    </div>
  );
};

export default Home;
