import React, { useContext, useState, useEffect } from "react";
import { useLocation } from "react-router-dom";
import Header from "../components/Header";
import Footer from "../components/Footer";
import { assets } from "../assets/assets"; // Import bank logos
import { UserContext } from "../context/UserContext";

const Dashboard = () => {
  const bank =localStorage.getItem('bank') || "default";

  // Bank logos mapping
  const bankLogos = {
    sbi: assets?.sbi,
    hdfc: assets?.hdfc,
    icici: assets?.icici,
  };

  
  const {account, getUserAccount} = useContext(UserContext);
  
  const storedUser = JSON.parse(localStorage.getItem("user"));
  const username = storedUser?.name || "";

  const accountHolder = username;
  const accNumber = String(account.accountNumber);
  const balance = account.balance;
  const [showBalance, setShowBalance] = useState(false);
  const [showTransactions, setShowTransactions] = useState(false);
  const [hoverAccNo, setHoverAccNo] = useState(false);

  const transactions = [
    { date: "2024-03-12", description: "Amazon Purchase", credit: "", debit: "₹1,200", balance: "₹49,800" },
    { date: "2024-03-11", description: "Salary Credit", credit: "₹50,000", debit: "", balance: "₹51,000" },
    { date: "2024-03-10", description: "Electricity Bill", credit: "", debit: "₹3,000", balance: "₹1,000" },
    { date: "2024-03-09", description: "Netflix Subscription", credit: "", debit: "₹499", balance: "₹4,000" },
  ];


    useEffect(()=>{
        getUserAccount()
    },[])

  return (
    <div className="flex flex-col min-h-screen">
      {/* Header */}
      <Header />

      {/* Main Content */}
      <main className="flex-grow flex justify-center items-center bg-gray-100">
        <div className="max-w-4xl w-full p-6 bg-white shadow-lg rounded-lg">
          {/* Bank Logo & User Info */}
          <div className="flex items-center space-x-4 mb-6">
            <img src={bankLogos[bank]} alt={`${bank} Logo`} className="w-16 h-16" />
            <div>
              <h2 className="text-2xl font-bold">{bank.toUpperCase()} Net Banking</h2>
              <p className="text-gray-700"><strong>Account Holder:</strong> {accountHolder}</p>
            </div>
          </div>

          {/* Account Summary */}
          <div className="bg-blue-100 p-4 rounded-lg shadow-md">
            <h3 className="text-lg font-semibold">Account Summary</h3>

            {/* Account Number with Hover Effect */}
            <p className="mt-2 text-gray-700">
              <strong>Account Number: </strong>
              <span
                className="relative font-bold cursor-pointer"
                onMouseEnter={() => setHoverAccNo(true)}
                onMouseLeave={() => setHoverAccNo(false)}
              >
                {hoverAccNo ? accNumber : `**** **** **** ${accNumber.slice(-4)}`}
              </span>
            </p>

            {/* Balance Visibility Toggle */}
            <p className="mt-2 text-gray-700">
              <strong>Balance: </strong>
              <span
                className="cursor-pointer text-blue-600 font-semibold"
                onClick={() => setShowBalance(!showBalance)}
              >
                {showBalance ? balance : "*****"}
              </span>
            </p>
          </div>

          {/* Transactions Section */}
          <div className="mt-6">
            <button
              className="w-full bg-blue-500 text-white p-2 rounded hover:bg-blue-600 transition"
              onClick={() => setShowTransactions(!showTransactions)}
            >
              {showTransactions ? "Hide Transactions" : "Show Last 10 Transactions"}
            </button>

            {showTransactions && (
              <div className="mt-4 bg-gray-100 p-4 rounded-lg shadow-md">
                <h3 className="text-lg font-semibold mb-3">Last 10 Transactions</h3>
                <table className="w-full border-collapse border border-gray-300">
                  <thead>
                    <tr className="bg-blue-500 text-white">
                      <th className="border border-gray-300 p-2">Date</th>
                      <th className="border border-gray-300 p-2">Description</th>
                      <th className="border border-gray-300 p-2">Credit</th>
                      <th className="border border-gray-300 p-2">Debit</th>
                      <th className="border border-gray-300 p-2">Balance</th>
                    </tr>
                  </thead>
                  <tbody>
                    {transactions.map((txn, index) => (
                      <tr key={index} className="text-center hover:bg-gray-200">
                        <td className="border border-gray-300 p-2">{txn.date}</td>
                        <td className="border border-gray-300 p-2">{txn.description}</td>
                        <td className={`border border-gray-300 p-2 ${txn.credit ? "text-green-600 font-bold" : "text-gray-500"}`}>
                          {txn.credit || "-"}
                        </td>
                        <td className={`border border-gray-300 p-2 ${txn.debit ? "text-red-600 font-bold" : "text-gray-500"}`}>
                          {txn.debit || "-"}
                        </td>
                        <td className="border border-gray-300 p-2 font-semibold">{txn.balance}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
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

export default Dashboard;