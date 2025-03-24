import React, { useContext, useState, useEffect } from "react";
import Header from "../components/Header";
import Footer from "../components/Footer";
import { UserContext } from "../context/UserContext";

const TransactionHistory = () => {
  const [filterType, setFilterType] = useState("month"); // "month" or "date"
  const [selectedValue, setSelectedValue] = useState("");

  const {transactions, getUserTransacions} = useContext(UserContext);

  // Fetch transactions on component mount
  useEffect(() => {
    getUserTransacions();
  }, []);

  // Ensure transactions is an array before filtering
  const filteredTransactions = (transactions?.transactions || []).filter((txn) => {
    const txnDate = new Date(txn.timestamp);
    if (filterType === "month" && selectedValue) {
      return txnDate.getMonth() + 1 === parseInt(selectedValue); // Month filter
    } else if (filterType === "date" && selectedValue) {
      return txnDate.toISOString().split("T")[0] === selectedValue; // Date filter
    }
    return true; // Show all transactions if no filter
  });

  return (

    <div className="flex flex-col min-h-screen">
    {/* Header */}
    <Header />

            <div className="flex-grow w-[75%] mx-auto mt-10 p-6 bg-white shadow-lg rounded-lg">
            {/* Filter Section */}
            <div className="w-full max-w-5xl mx-auto flex items-center justify-between mb-6 p-4 bg-gray-100 rounded-lg">
                <select
                className="p-2 border rounded-lg text-gray-700"
                onChange={(e) => setFilterType(e.target.value)}
                >
                <option value="month">Filter by Month</option>
                <option value="date">Filter by Date</option>
                </select>

                {filterType === "month" ? (
                <select
                    className="p-2 border rounded-lg text-gray-700"
                    onChange={(e) => setSelectedValue(e.target.value)}
                >
                    <option value="">Select Month</option>
                    {Array.from({ length: 12 }, (_, i) => (
                    <option key={i + 1} value={i + 1}>
                        {new Date(0, i).toLocaleString("default", { month: "long" })}
                    </option>
                    ))}
                </select>
                ) : (
                <input
                    type="date"
                    className="p-2 border rounded-lg text-gray-700"
                    onChange={(e) => setSelectedValue(e.target.value)}
                />
                )}
            </div>

            {/* Transactions Table */}
            <div className="overflow-x-auto">
                <table className="w-full text-center border border-gray-300">
                <thead className="bg-gray-700 text-white">
                    <tr>
                    <th className="p-3 border">Timestamp</th>
                    <th className="p-3 border">Description</th>
                    <th className="p-3 border">Credited</th>
                    <th className="p-3 border">Debited</th>
                    <th className="p-3 border">Balance</th>
                    </tr>
                </thead>
                <tbody>
                    {filteredTransactions.length > 0 ? (
                    filteredTransactions.map((txn, index) => (
                        <tr
                        key={index}
                        className="border border-gray-300 hover:bg-gray-100 transition"
                        >
                        <td className="p-3 border">{txn.timestamp}</td>
                        <td className="p-3 border">{txn.description}</td>
                        <td
                            className={`p-3 border ${
                            txn.creditedAmount ? "text-green-600 font-bold" : "text-gray-400"
                            }`}
                        >
                            {txn.creditedAmount || "-"}
                        </td>
                        <td
                            className={`p-3 border ${
                            txn.debitedAmount ? "text-red-600 font-bold" : "text-gray-400"
                            }`}
                        >
                            {txn.debitedAmount || "-"}
                        </td>
                        <td className="p-3 border font-semibold">{txn.currentBalance}</td>
                        </tr>
                    ))
                    ) : (
                    <tr>
                        <td colSpan="5" className="p-3 text-gray-500">
                        No transactions found.
                        </td>
                    </tr>
                    )}
                </tbody>
                </table>
            </div>
            </div>

    {/* Footer */}
      <Footer />
    </div>
  );
};

export default TransactionHistory;
