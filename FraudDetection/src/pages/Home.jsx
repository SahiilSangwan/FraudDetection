import React from 'react';
import { useNavigate } from 'react-router-dom';
import { assets } from '../assets/assets';

const Home = () => {
  const navigate = useNavigate();

  const handleBankClick = (bankName) => {
    localStorage.setItem('bank', bankName);
    navigate("/login");
  };

  const banks = [
    { name: 'SBI', logo: assets?.sbi || 'fallback-image.png' },
    { name: 'HDFC', logo: assets?.hdfc || 'fallback-image.png' },
    { name: 'ICICI', logo: assets?.icici || 'fallback-image.png' }
  ];

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex flex-col items-center justify-center p-4">
      {/* header */}
      <div className="text-center mb-12 animate-fade-in">
        <h1 className="text-5xl font-bold text-gray-800 mb-4">
          Welcome to <span className="text-blue-600">Digital</span> Banking
        </h1>
        <p className="text-xl text-gray-600 max-w-2xl">
          Select your bank to continue to secure login
        </p>
      </div>

      {/* Bank card */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-8 max-w-6xl w-full">
        {banks.map((bank, index) => (
          <div 
            key={bank.name}
            className="bg-white rounded-2xl shadow-xl overflow-hidden transition-all duration-300 hover:shadow-2xl hover:-translate-y-2 animate-fade-in-up"
            style={{ animationDelay: `${index * 100}ms` }}
            onClick={() => handleBankClick(bank.name.toLowerCase())}
          >
            <div className="p-8 flex flex-col items-center">
              <div className="w-40 h-40 mb-6 flex items-center justify-center">
                <img 
                  src={bank.logo} 
                  alt={bank.name} 
                  className="max-h-full max-w-full object-contain"
                />
              </div>
              <h3 className="text-2xl font-semibold text-gray-800 mb-2">{bank.name}</h3>
              <button className="mt-4 px-6 py-2 bg-blue-600 text-white rounded-full hover:bg-blue-700 transition-colors">
                Select
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* Footer note */}
      <div className="mt-16 text-center text-gray-500 text-sm">
        <p>Your banking experience is protected with strong encryption for added security.</p>
      </div>
      
    </div>
  );
};

export default Home;