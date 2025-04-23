import React, { useContext, useState, useEffect } from "react";
import { useLocation } from "react-router-dom";
import Header from "../components/Header";
import Footer from "../components/Footer";
import { assets } from "../assets/assets";
import { UserContext } from "../context/UserContext";

const Dashboard = () => {
  const bank =localStorage.getItem('bank') || "default";

  const bankLogos = {
    sbi: assets?.sbi,
    hdfc: assets?.hdfc,
    icici: assets?.icici,
  };

  const {account, getUserAccount, transactions, getUserTransacions, getBankTheme} = useContext(UserContext);

  const storedUser = JSON.parse(localStorage.getItem("user"));
  const username = storedUser?.name || "";
  const accountHolder = username;
  const accNumber = String(account.accountNumber);
  const balance = account.balance;
  const [showBalance, setShowBalance] = useState(false);
  const [showTransactions, setShowTransactions] = useState(false);
  const [hoverAccNo, setHoverAccNo] = useState(false);

    useEffect(()=>{
        getUserAccount()
        getUserTransacions()
    },[])

  return (
    
      <div className="flex flex-col min-h-screen bg-gradient-to-br from-blue-50 to-indigo-50">
        {/* Header */}
        <Header />

        {/* Main Content */}
        <main className="flex-grow flex justify-center items-center p-4">
          <div className={`max-w-4xl w-full bg-white rounded-xl shadow-xl overflow-hidden ${getBankTheme(bank).border}`}>
            {/* Bank Header */}
            <div className={`${getBankTheme(bank).header} p-6`}>
              <div className="flex items-center space-x-4">
                <img 
                  src={bankLogos[bank]} 
                  alt={`${bank} Logo`} 
                  className="w-16 h-16 bg-white p-1 rounded-full shadow-md" 
                />
                <div>
                  <h2 className="text-2xl font-bold text-white">{bank.toUpperCase()} Internet Banking</h2>
                  <p className="text-white/90">Welcome back, <span className="font-medium">{accountHolder}</span></p>
                </div>
              </div>
            </div>

            {/* Account Summary */}
            <div className="p-6">
              <div className="bg-white border border-gray-200 rounded-lg shadow-sm p-6">
                <h3 className="text-xl font-semibold text-gray-800 mb-4 flex items-center">
                  <svg className="w-6 h-6 mr-2 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 6l9-4 9 4m-9-4v20m-6-9h12M6 9h12" />
                  </svg>
                  Account Summary
                </h3>

                <div className="space-y-4">
                  {/* Account Number */}
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <div className="flex items-center">
                      <svg className="w-5 h-5 mr-3 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4" />
                      </svg>
                      <span className="text-gray-700">Account Number</span>
                    </div>
                    <div 
                      className="font-mono cursor-pointer relative group"
                      onMouseEnter={() => setHoverAccNo(true)}
                      onMouseLeave={() => setHoverAccNo(false)}
                    >
                      {hoverAccNo ? (
                        <span className="text-gray-900 font-medium">{accNumber}</span>
                      ) : (
                        <span className="tracking-widest">•••• •••• •••• {accNumber.slice(-4)}</span>
                      )}
                      <span className="absolute bottom-0 left-0 w-0 h-0.5 bg-blue-500 transition-all duration-300 group-hover:w-full"></span>
                    </div>
                  </div>

                  {/* Balance */}
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <div className="flex items-center">
                      <svg className="w-5 h-5 mr-3 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      <span className="text-gray-700">Available Balance</span>
                    </div>
                    <div 
                      className="cursor-pointer"
                      onClick={() => setShowBalance(!showBalance)}
                    >
                      {showBalance ? (
                        <span className="text-2xl font-bold text-gray-900">₹{balance.toLocaleString()}</span>
                      ) : (
                        <span className="text-2xl font-bold tracking-widest">•••••</span>
                      )}
                    </div>
                  </div>
                </div>
              </div>

              {/* Transactions Section */}
              <div className="mt-8">
                <button
                  onClick={() => setShowTransactions(!showTransactions)}
                  className={`w-full ${getBankTheme(bank).button} text-white py-3 px-4 rounded-lg font-semibold hover:opacity-90 transition-opacity flex items-center justify-center space-x-2`}
                >
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" />
                  </svg>
                  <span>{showTransactions ? "Hide Transactions" : "View Recent Transactions"}</span>
                </button>

                {showTransactions && (
                  <div className="mt-6 bg-white border border-gray-200 rounded-lg shadow-sm overflow-hidden">
                    <div className="px-6 py-4 border-b border-gray-200">
                      <h3 className="text-lg font-semibold text-gray-800 flex items-center">
                        <svg className="w-5 h-5 mr-2 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
                        </svg>
                        Recent Transactions
                      </h3>
                    </div>
                    <div className="overflow-x-auto">
                      <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                          <tr>
                            <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                            <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Description</th>
                            <th scope="col" className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Credit</th>
                            <th scope="col" className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Debit</th>
                            <th scope="col" className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Balance</th>
                          </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                          {transactions.transactions.slice(0, 10).map((txn, index) => (
                            <tr key={index} className="hover:bg-gray-50 transition-colors">
                              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                                {new Date(txn.timestamp).toLocaleDateString()}
                              </td>
                              <td className="px-6 py-4 text-sm text-gray-900 font-medium">
                                {txn.description}
                              </td>
                              <td className={`px-6 py-4 whitespace-nowrap text-sm text-right ${
                                txn.creditedAmount ? "text-green-600 font-medium" : "text-gray-500"
                              }`}>
                                {txn.creditedAmount ? `+₹${txn.creditedAmount.toLocaleString()}` : "-"}
                              </td>
                              <td className={`px-6 py-4 whitespace-nowrap text-sm text-right ${
                                txn.debitedAmount ? "text-red-600 font-medium" : "text-gray-500"
                              }`}>
                                {txn.debitedAmount ? `-₹${txn.debitedAmount.toLocaleString()}` : "-"}
                              </td>
                              <td className="px-6 py-4 whitespace-nowrap text-sm text-right font-medium text-gray-900">
                                ₹{txn.currentBalance.toLocaleString()}
                              </td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        </main>

        {/* Footer */}
        <Footer />
      </div>
  );
};

export default Dashboard;