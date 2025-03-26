import React, { useContext, useCallback } from 'react';
import { useSearchParams, useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import Header from "../components/Header";
import Footer from "../components/Footer";
import { UserContext } from '../context/UserContext';
import { toast } from 'react-toastify';

const Transactions = () => {
  const {getUserTransacionBeneficiaries, transactionBeneficiaries, setSelectedBeneficiaryID } = useContext(UserContext);

  const [searchParams] = useSearchParams();
  const [bankType, setBankType] = useState(false);
  const [search, setSearch] = useState("");
  const [selectedBeneficiary, setSelectedBeneficiary] = useState(null);
  const [description, setDescription] = useState("");
  const [amount, setAmount] = useState("");
  const navigate = useNavigate();

  // Filter beneficiaries based on search input
  const filteredBeneficiaries = transactionBeneficiaries.filter((b) =>
    b.beneficiaryName.toLowerCase().includes(search.toLowerCase())
  );

  // Fetch beneficiaries based on bank type
  const fetchBeneficiaries = useCallback(() => {
    getUserTransacionBeneficiaries(bankType);
  }, [bankType, getUserTransacionBeneficiaries]);

  useEffect(() => {
    const bank = searchParams.get("bank");
    setBankType(bank === "same");
    setSelectedBeneficiary(null);
  }, [searchParams]);

  useEffect(() => {
    if (selectedBeneficiary) {
      setSelectedBeneficiaryID(selectedBeneficiary.beneficiaryId);
    }
  }, [selectedBeneficiary, setSelectedBeneficiaryID]);

  useEffect(() => {
    fetchBeneficiaries();
  }, [fetchBeneficiaries]);

  const handlePay = () => {
    if (!selectedBeneficiary || !amount) {
      toast.error("Please select a beneficiary and enter an amount");
      return;
    }

    // Prepare transaction data to pass via navigation state
    const transactionData = {
      receiverAcc: selectedBeneficiary.beneficiaryAccountNumber,
      ifscCodeUser: selectedBeneficiary.ifscCode,
      selectedBeneficiaryID: selectedBeneficiary.beneficiaryId,
      description,
      amount,
      beneficiaryName: selectedBeneficiary.beneficiaryName,
      beneficiaryBank: selectedBeneficiary.beneficiaryBank
    };

    // Navigate with state instead of using localStorage
    navigate("/confirm-payment", { 
      state: { transactionData },
      replace: true // Prevent going back to this page with browser back button
    });
    
    toast.success("Transaction processing...");
  };

  return (
    
      <div className="flex flex-col min-h-screen bg-gradient-to-br from-blue-50 to-indigo-50">
        <Header />

        <main className="flex-grow flex items-center justify-center p-4 sm:p-6">
          <div className="w-full max-w-4xl space-y-6">
            {/* Search and Beneficiary List */}
            <div className="bg-white rounded-xl shadow-md overflow-hidden">
              <div className="p-6">
                <div className="relative">
                  <input
                    type="text"
                    placeholder="Search beneficiary..."
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    className="w-full pl-10 pr-4 py-3 border border-gray-200 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  />
                  <div className="absolute left-3 top-3 text-gray-400">
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                    </svg>
                  </div>
                </div>

                <div className="mt-4 bg-blue-50 border border-blue-100 rounded-lg p-3 text-center">
                  <p className="text-blue-700 text-sm flex items-center justify-center">
                    <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    Recently added or updated beneficiaries (within the past hour) will not be visible
                  </p>
                </div>

                <ul className="mt-4 divide-y divide-gray-200 max-h-96 overflow-y-auto">
                  {filteredBeneficiaries.length > 0 ? (
                    filteredBeneficiaries.map((b) => (
                      <li key={b.beneficiaryId} className="py-4 hover:bg-gray-50 transition-colors">
                        <div className="flex items-center justify-between">
                          <div>
                            <p className="text-lg font-medium text-gray-900">{b.beneficiaryName}</p>
                            <div className="flex flex-wrap items-center mt-1 text-sm text-gray-500">
                              <span className="flex items-center mr-3">
                                <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 6l9-4 9 4m-9-4v20m-6-9h12M6 9h12" />
                                </svg>
                                {b.beneficiaryBank}
                              </span>
                              <span className="flex items-center">
                                <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4" />
                                </svg>
                                •••• •••• •••• {b.beneficiaryAccountNumber.slice(-4)}
                              </span>
                            </div>
                          </div>
                          <button
                            onClick={() => setSelectedBeneficiary(b)}
                            className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors flex items-center"
                          >
                            <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                            </svg>
                            Select
                          </button>
                        </div>
                      </li>
                    ))
                  ) : (
                    <li className="py-8 text-center">
                      <svg className="w-12 h-12 mx-auto text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      <p className="mt-2 text-gray-500">No beneficiaries found</p>
                      <p className="text-sm text-gray-400">Try a different search term</p>
                    </li>
                  )}
                </ul>
              </div>
            </div>

            {/* Transaction Form */}
            {selectedBeneficiary && (
              <div className="bg-white rounded-xl shadow-md overflow-hidden transition-all duration-300">
                <div className="p-6">
                  <div className="flex justify-between items-center mb-6">
                    <h2 className="text-xl font-bold text-gray-800">Transfer Details</h2>
                    <button
                      onClick={() => setSelectedBeneficiary(null)}
                      className="text-gray-400 hover:text-gray-500"
                    >
                      <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                      </svg>
                    </button>
                  </div>

                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                    <div className="bg-gray-50 p-4 rounded-lg">
                      <label className="block text-sm font-medium text-gray-500 mb-1">Beneficiary Name</label>
                      <p className="text-gray-900 font-medium">{selectedBeneficiary.beneficiaryName}</p>
                    </div>
                    <div className="bg-gray-50 p-4 rounded-lg">
                      <label className="block text-sm font-medium text-gray-500 mb-1">Bank</label>
                      <p className="text-gray-900 font-medium">{selectedBeneficiary.beneficiaryBank}</p>
                    </div>
                    <div className="bg-gray-50 p-4 rounded-lg">
                      <label className="block text-sm font-medium text-gray-500 mb-1">Account Number</label>
                      <p className="text-gray-900 font-mono font-medium">{selectedBeneficiary.beneficiaryAccountNumber}</p>
                    </div>
                    <div className="bg-gray-50 p-4 rounded-lg">
                      <label className="block text-sm font-medium text-gray-500 mb-1">IFSC Code</label>
                      <p className="text-gray-900 font-medium">{selectedBeneficiary.ifscCode}</p>
                    </div>
                  </div>

                  <div className="space-y-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
                      <input
                        type="text"
                        placeholder="e.g. Rent payment, Loan repayment"
                        value={description}
                        onChange={(e) => setDescription(e.target.value)}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Amount (₹)</label>
                      <div className="relative">
                        <span className="absolute left-3 top-2 text-gray-500">₹</span>
                        <input
                          type="number"
                          placeholder="0.00"
                          value={amount}
                          onChange={(e) => setAmount(e.target.value)}
                          className="w-full pl-8 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                        />
                      </div>
                    </div>
                  </div>

                  <div className="flex justify-end space-x-3 mt-6">
                    <button
                      onClick={() => setSelectedBeneficiary(null)}
                      className="px-6 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors"
                    >
                      Cancel
                    </button>
                    <button
                      onClick={handlePay}
                      className="px-6 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg transition-colors flex items-center"
                    >
                      <svg className="w-5 h-5 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      Confirm Payment
                    </button>
                  </div>
                </div>
              </div>
            )}
          </div>
        </main>

        <Footer />
      </div>
  );
};

export default Transactions;