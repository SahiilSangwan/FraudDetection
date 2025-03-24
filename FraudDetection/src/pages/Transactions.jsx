import React, {useContext} from 'react';
import { useSearchParams } from "react-router-dom";
import { useNavigate } from "react-router-dom";
import { useState, useEffect } from "react";
import Header from "../components/Header";
import Footer from "../components/Footer";
import { UserContext } from '../context/UserContext';
import { toast } from 'react-toastify';

const Transactions = () => {

  const {getUserTransacionBeneficiaries, transactionBeneficiaries,
          setSelectedBeneficiaryID} = useContext(UserContext);

  const [searchParams] = useSearchParams();
  const [bankType, setBankType] = useState(false);

  const [search, setSearch] = useState("");
  const [selectedBeneficiary, setSelectedBeneficiary] = useState(null);

  const [description, setDescription] = useState("");
  const [amount, setAmount] = useState("");

  const filteredBeneficiaries = transactionBeneficiaries.filter((b) =>
    b.beneficiaryName.toLowerCase().includes(search.toLowerCase())
  );

  const navigate = useNavigate();
  const handlePay = () => {
    localStorage.setItem("receiverAcc", selectedBeneficiary.beneficiaryAccountNumber);
    localStorage.setItem("ifscCodeUser", selectedBeneficiary.ifscCode);
    navigate("/confirm-payment");
    toast.success("Transaction processing.....");
  };


  useEffect(() => {
    const bank = searchParams.get("bank");
    setBankType(bank === "same");
    setSelectedBeneficiary(null);
  }, [searchParams]);

  useEffect(() => {
    if (selectedBeneficiary) {
      setSelectedBeneficiaryID(selectedBeneficiary.beneficiaryId);
      localStorage.setItem("selectedBeneficiaryID", selectedBeneficiary.beneficiaryId);
    }
  }, [selectedBeneficiary]);

  useEffect(() => {
    getUserTransacionBeneficiaries(bankType);
  }, [bankType, getUserTransacionBeneficiaries]);

  useEffect(() => {
    localStorage.setItem("description", description);
  }, [description]);

  useEffect(() => {
    localStorage.setItem("amount", amount);
  }, [amount]);

  return (
    <div className="flex flex-col min-h-screen">
    {/* Header */}
    <Header />

    <div className="min-h-[75vh] bg-gray-100 flex items-start justify-center p-6">
      <div className="w-full max-w-4xl">
        {/* Search and Beneficiary List */}
        <div className="bg-white w-full shadow-lg rounded-lg p-6 mx-auto">
          <input
            type="text"
            placeholder="🔍 Search beneficiary..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full p-2 mb-4 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />

            <p className="text-center text-green-500 italic mb-4">
            📌 Recently added or updated beneficiaries (within the past hour) will not be visible.
            </p>

          <ul className="max-h-60 overflow-y-auto">
            {filteredBeneficiaries.length > 0 ? (
              filteredBeneficiaries.map((b) => (
                <li key={b.beneficiaryId} className="flex justify-between items-center p-3 bg-gray-100 rounded-lg mb-2">
                  <div>
                    <p className="text-lg font-semibold">{b.beneficiaryName}</p>
                    <p className="text-sm text-gray-600">{b.beneficiaryBank} - {b.beneficiaryAccountNumber}</p>
                  </div>
                  <button
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700"
                    onClick={() => setSelectedBeneficiary(b)}
                  >
                    Select
                  </button>
                </li>
              ))
            ) : (
              <p className="text-gray-500">No beneficiaries found.</p>
            )}
          </ul>
        </div>

        {/* Transaction Form */}
        {selectedBeneficiary && (
          <div className="bg-white shadow-lg rounded-lg p-6 mt-6">
            <h2 className="text-xl font-semibold mb-4">Transaction Details</h2>

            <div className="mb-4">
              <label className="block text-gray-700">Beneficiary Name</label>
              <input type="text" value={selectedBeneficiary.beneficiaryName} readOnly className="w-full p-2 border border-gray-300 rounded-md bg-gray-100" />
            </div>

            <div className="mb-4">
              <label className="block text-gray-700">Bank</label>
              <input type="text" value={selectedBeneficiary.beneficiaryBank} readOnly className="w-full p-2 border border-gray-300 rounded-md bg-gray-100" />
            </div>

            <div className="mb-4">
              <label className="block text-gray-700">Account Number</label>
              <input type="text" value={selectedBeneficiary.beneficiaryAccountNumber} readOnly className="w-full p-2 border border-gray-300 rounded-md bg-gray-100" />
            </div>

            <div className="mb-4">
              <label className="block text-gray-700">IFSC Code</label>
              <input type="text" value={selectedBeneficiary.ifscCode} readOnly className="w-full p-2 border border-gray-300 rounded-md bg-gray-100" />
            </div>

            <div className="mb-4">
              <label className="block text-gray-700">Description</label>
              <input
                type="text"
                placeholder="Enter description"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div className="mb-4">
              <label className="block text-gray-700">Amount</label>
              <input
                type="number"
                placeholder="Enter amount"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                className="w-full p-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>

            <div className="flex justify-between mt-4">
              <button
                className="px-6 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700"
                onClick={handlePay}
              >
                Pay
              </button>
              <button
                className="px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700"
                onClick={() => setSelectedBeneficiary(null)}
              >
                Cancel
              </button>
            </div>
          </div>
        )}
      </div>
    </div>

    {/* Footer */}
    <Footer />
    </div>
  );
};

export default Transactions;
