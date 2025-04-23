import React, { useContext, useState, useEffect } from "react";
import Header from "../components/Header";
import Footer from "../components/Footer";
import { UserContext } from "../context/UserContext";

const TransactionHistory = () => {
  const [filterType, setFilterType] = useState("month");
  const [selectedValue, setSelectedValue] = useState("");
  const {transactions, getUserTransacions} = useContext(UserContext);

  useEffect(() => {
    getUserTransacions();
  }, []);

  const filteredTransactions = (transactions?.transactions || []).filter((txn) => {
    const txnDate = new Date(txn.timestamp);
    if (filterType === "month" && selectedValue) {
      return txnDate.getMonth() + 1 === parseInt(selectedValue);
    } else if (filterType === "date" && selectedValue) {
      return txnDate.toISOString().split("T")[0] === selectedValue; 
    }
    return true;
  });

  return (

    <div className="flex flex-col min-h-screen bg-gray-50">
        {/* Header */}
        <Header />

        <div className="flex-grow w-full max-w-6xl mx-auto py-8 px-4 sm:px-6">
          <div className="bg-white shadow-sm rounded-xl overflow-hidden">
            {/* Filter Section */}
            <div className="p-6 border-b border-gray-100">
              <h1 className="text-2xl font-bold text-gray-800 mb-6">Transaction History</h1>
              
              <div className="flex flex-col sm:flex-row gap-4 items-center">
                <div className="relative flex-1 w-full">
                  <select
                    className="w-full p-3 pl-4 pr-8 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 appearance-none bg-white bg-[url('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNCIgaGVpZ2h0PSIyNCIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSJub25lIiBzdHJva2U9Ii82QjcyODkiIHN0cm9rZS13aWR0aD0iMiIgc3Ryb2tlLWxpbmVjYXA9InJvdW5kIiBzdHJva2UtbGluZWpvaW49InJvdW5kIj48cG9seWxpbmUgcG9pbnRzPSI2IDkgMTIgMTUgMTggOSI+PC9wb2x5bGluZT48L3N2Zz4=')] bg-no-repeat bg-[right_1rem_center]"
                    onChange={(e) => setFilterType(e.target.value)}
                  >
                    <option value="month">Filter by Month</option>
                    <option value="date">Filter by Date</option>
                  </select>
                </div>

                {filterType === "month" ? (
                  <div className="relative flex-1 w-full">
                    <select
                      className="w-full p-3 pl-4 pr-8 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 appearance-none bg-white bg-[url('data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHdpZHRoPSIyNCIgaGVpZ2h0PSIyNCIgdmlld0JveD0iMCAwIDI0IDI0IiBmaWxsPSJub25lIiBzdHJva2U9Ii82QjcyODkiIHN0cm9rZS13aWR0aD0iMiIgc3Ryb2tlLWxpbmVjYXA9InJvdW5kIiBzdHJva2UtbGluZWpvaW49InJvdW5kIj48cG9seWxpbmUgcG9pbnRzPSI2IDkgMTIgMTUgMTggOSI+PC9wb2x5bGluZT48L3N2Zz4=')] bg-no-repeat bg-[right_1rem_center]"
                      onChange={(e) => setSelectedValue(e.target.value)}
                    >
                      <option value="">Select Month</option>
                      {Array.from({ length: 12 }, (_, i) => (
                        <option key={i + 1} value={i + 1}>
                          {new Date(0, i).toLocaleString("default", { month: "long" })}
                        </option>
                      ))}
                    </select>
                  </div>
                ) : (
                  <div className="relative flex-1 w-full">
                    <input
                      type="date"
                      className="w-full p-3 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                      onChange={(e) => setSelectedValue(e.target.value)}
                    />
                  </div>
                )}
              </div>
            </div>

            {/* Transactions Table */}
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Date & Time
                    </th>
                    <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Description
                    </th>
                    <th scope="col" className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Credit
                    </th>
                    <th scope="col" className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Debit
                    </th>
                    <th scope="col" className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                      Balance
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {filteredTransactions.length > 0 ? (
                    filteredTransactions.map((txn, index) => (
                      <tr key={index} className="hover:bg-gray-50 transition-colors">
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {new Date(txn.timestamp).toLocaleString()}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                          {txn.description}
                        </td>
                        <td className={`px-6 py-4 whitespace-nowrap text-sm text-right ${
                          txn.creditedAmount ? "text-green-600 font-medium" : "text-gray-400"
                        }`}>
                          {txn.creditedAmount ? `+₹${txn.creditedAmount.toLocaleString()}` : "-"}
                        </td>
                        <td className={`px-6 py-4 whitespace-nowrap text-sm text-right ${
                          txn.debitedAmount ? "text-red-600 font-medium" : "text-gray-400"
                        }`}>
                          {txn.debitedAmount ? `-₹${txn.debitedAmount.toLocaleString()}` : "-"}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-right font-medium text-gray-900">
                          ₹{txn.currentBalance.toLocaleString()}
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan="5" className="px-6 py-4 text-center text-sm text-gray-500">
                        No transactions found for selected filter
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>
        </div>

        {/* Footer */}
        <Footer />
      </div>
  );
};

export default TransactionHistory;
