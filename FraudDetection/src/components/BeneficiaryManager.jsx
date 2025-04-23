import { useState, useEffect, useContext } from "react";
import { UserContext } from "../context/UserContext";

const BeneficiaryManager = () => {
    const [bankType, setBankType] = useState("same");

    const {beneficiaries, getUserBeneficiaries, deleteBeneficiaries,handleUpdateBeneficiary, sendOTP,
        verifyOTP, setNewBeneficiary, newBeneficiary } = useContext(UserContext);

    const [showAddPopup, setShowAddPopup] = useState(false);
    const [selectedBeneficiary, setSelectedBeneficiary] = useState(0);   
    const [otp, setOtp] = useState("");
    const [otpSent, setOtpSent] = useState(false);
    const [resendTimer, setResendTimer] = useState(30);
    const [isUpdateOpen, setIsUpdateOpen] = useState(false);
    const [amount, setAmount] = useState("");

    // Timer for OTP Resend
    useEffect(() => {
        let timer;
        if (otpSent && resendTimer > 0) {
            timer = setTimeout(() => setResendTimer(resendTimer - 1), 1000);
        }
        return () => clearTimeout(timer);
    }, [otpSent, resendTimer]);

    // Handle checkbox toggle
    const handleBankChange = (type) => setBankType(type);

    // Open Edit Popup
    const handleEdit = (beneficiary) => {
        setIsUpdateOpen(true);
        setSelectedBeneficiary(beneficiary);
    };

    // Open Add Beneficiary Popup
    const handleAddBeneficiary = () => {
        setShowAddPopup(true);
    };

    // Send OTP
    const handleSendOtp = async () => {
        const success = await sendOTP(); 
    
        if (success) {
            setOtpSent(true);
            setResendTimer(30);
        }
    };

    // Resend OTP
    const handleResendOtp = () => {
        setOtp("");
        handleSendOtp();
    };

    useEffect(()=>{
        getUserBeneficiaries(bankType === "same" ? true : false);
    },[bankType])

    return (

        <div className="max-w-7xl mx-auto p-6 min-h-screen">
            {/* Header */}
            <div className="mb-8 text-center">
                <h2 className="text-3xl font-bold text-gray-800 mb-2">Manage Beneficiaries</h2>
                <p className="text-gray-600">Add and manage your payment recipients</p>
            </div>

            {/* Bank Selection */}
            <div className="flex flex-col sm:flex-row gap-6 mb-8 p-4 bg-white rounded-xl shadow-sm border border-gray-100">
                <div className="flex-1">
                <h3 className="text-lg font-medium text-gray-700 mb-3">Filter Beneficiaries</h3>
                <div className="flex flex-wrap gap-4">
                    <label className="inline-flex items-center">
                    <input 
                        type="radio" 
                        className="form-radio h-5 w-5 text-blue-600"
                        checked={bankType === "same"}
                        onChange={() => handleBankChange("same")}
                    />
                    <span className="ml-2 text-gray-700">Same Bank</span>
                    </label>
                    <label className="inline-flex items-center">
                    <input 
                        type="radio" 
                        className="form-radio h-5 w-5 text-blue-600"
                        checked={bankType === "different"}
                        onChange={() => handleBankChange("different")}
                    />
                    <span className="ml-2 text-gray-700">Different Bank</span>
                    </label>
                </div>
                </div>

                {/* Add Beneficiary Button */}
                <button 
                onClick={handleAddBeneficiary}
                className="flex items-center justify-center gap-2 bg-blue-600 hover:bg-blue-700 text-white py-2 px-6 rounded-lg transition-all shadow-md"
                >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                </svg>
                Add New Beneficiary
                </button>
            </div>

            {/* Beneficiary List */}
            <div className="bg-white rounded-xl shadow-sm overflow-hidden border border-gray-100">
                <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                    <tr>
                        <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
                        <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Account No</th>
                        <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">IFSC</th>
                        <th scope="col" className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">Amount</th>
                        <th scope="col" className="px-6 py-3 text-center text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                    </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                    {beneficiaries.length > 0 ? (
                        beneficiaries.map((b, index) => (
                        <tr key={index} className="hover:bg-gray-50 transition-colors">
                            <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{b.beneficiaryName}</td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                            <span className="font-mono">•••• {b.beneficiaryAccountNumber.slice(-4)}</span>
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{b.ifscCode}</td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-right font-medium">
                            ₹{b.amount.toLocaleString()}
                            </td>
                            <td className="px-6 py-4 whitespace-nowrap text-sm text-center">
                            <div className="flex justify-center space-x-2">
                                <button 
                                onClick={() => handleEdit(b.beneficiaryId)}
                                className="inline-flex items-center px-3 py-1 border border-transparent text-xs font-medium rounded-md shadow-sm text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500"
                                >
                                Update
                                </button>
                                <button 
                                onClick={() => deleteBeneficiaries(b.beneficiaryId)}
                                className="inline-flex items-center px-3 py-1 border border-transparent text-xs font-medium rounded-md shadow-sm text-white bg-red-600 hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-red-500"
                                >
                                Delete
                                </button>
                            </div>
                            </td>
                        </tr>
                        ))
                    ) : (
                        <tr>
                        <td colSpan="5" className="px-6 py-4 text-center text-sm text-gray-500">
                            No beneficiaries found. Add one to get started.
                        </td>
                        </tr>
                    )}
                    </tbody>
                </table>
                </div>
            </div>

            {/* Edit Beneficiary Modal */}
            {isUpdateOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 backdrop-blur-sm">
                <div className="bg-white rounded-xl shadow-xl w-full max-w-md mx-4">
                    <div className="p-6">
                    <div className="flex justify-between items-center mb-4">
                        <h3 className="text-xl font-bold text-gray-800">Update Amount</h3>
                        <button 
                        onClick={() => setIsUpdateOpen(false)}
                        className="text-gray-400 hover:text-gray-500"
                        >
                        <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                        </button>
                    </div>
                    
                    <div className="mb-6">
                        <label className="block text-sm font-medium text-gray-700 mb-1">Amount (₹)</label>
                        <input
                        type="number"
                        value={amount}
                        onChange={(e) => setAmount(e.target.value)}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                        placeholder="Enter amount"
                        />
                    </div>
                    
                    <div className="flex justify-end space-x-3">
                        <button
                        onClick={() => setIsUpdateOpen(false)}
                        className="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors"
                        >
                        Cancel
                        </button>
                        <button
                        onClick={() => { handleUpdateBeneficiary(selectedBeneficiary, amount); setIsUpdateOpen(false);}}
                        className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                        >
                        Update Amount
                        </button>
                    </div>
                    </div>
                </div>
                </div>
            )}

            {/* Add Beneficiary Modal */}
            {showAddPopup && (
                <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 backdrop-blur-sm">
                <div className="bg-white rounded-xl shadow-xl w-full max-w-md mx-4">
                    <div className="p-6">
                    <div className="flex justify-between items-center mb-4">
                        <h3 className="text-xl font-bold text-gray-800">Add New Beneficiary</h3>
                        <button 
                        onClick={() => setShowAddPopup(false)}
                        className="text-gray-400 hover:text-gray-500"
                        >
                        <svg className="h-6 w-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                        </button>
                    </div>

                    <div className="space-y-4">
                        <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Full Name</label>
                        <input 
                            type="text" 
                            value={newBeneficiary.name} 
                            onChange={(e) => setNewBeneficiary({ ...newBeneficiary, name: e.target.value })} 
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                            placeholder="Recipient's full name"
                        />
                        </div>

                        <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Account Number</label>
                        <input 
                            type="text" 
                            value={newBeneficiary.accountNumber} 
                            onChange={(e) => setNewBeneficiary({ ...newBeneficiary, accountNumber: e.target.value })} 
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                            placeholder="Account number"
                        />
                        </div>

                        <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Confirm Account Number</label>
                        <input 
                            type="text" 
                            value={newBeneficiary.confirmAccount} 
                            onChange={(e) => setNewBeneficiary({ ...newBeneficiary, confirmAccount: e.target.value })} 
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                            placeholder="Re-enter account number"
                        />
                        </div>

                        <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">IFSC Code</label>
                        <input 
                            type="text" 
                            value={newBeneficiary.ifscCode} 
                            onChange={(e) => setNewBeneficiary({ ...newBeneficiary, ifscCode: e.target.value })} 
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                            placeholder="Bank IFSC code"
                        />
                        </div>

                        <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">Amount (₹)</label>
                        <input 
                            type="number" 
                            value={newBeneficiary.amount} 
                            onChange={(e) => setNewBeneficiary({ ...newBeneficiary, amount: e.target.value })} 
                            className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                            placeholder="Transfer amount"
                        />
                        </div>

                        {!otpSent ? (
                        <button 
                            onClick={handleSendOtp}
                            className="w-full py-2 px-4 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors"
                        >
                            Send OTP
                        </button>
                        ) : (
                        <>
                            <div>
                            <label className="block text-sm font-medium text-gray-700 mb-1">Enter OTP</label>
                            <input 
                                type="text" 
                                value={otp} 
                                onChange={(e) => setOtp(e.target.value)} 
                                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                placeholder="6-digit OTP"
                            />
                            </div>
                            <button 
                            onClick={() => verifyOTP(otp)}
                            className="w-full py-2 px-4 bg-green-600 hover:bg-green-700 text-white rounded-lg transition-colors"
                            >
                            Confirm OTP
                            </button>
                            
                            {resendTimer === 0 ? (
                            <button 
                                onClick={handleResendOtp}
                                className="w-full text-center text-blue-600 hover:text-blue-800 text-sm"
                            >
                                Resend OTP
                            </button>
                            ) : (
                            <p className="text-center text-gray-500 text-sm">
                                Resend OTP in {resendTimer}s
                            </p>
                            )}
                        </>
                        )}
                    </div>
                    </div>
                </div>
                </div>
            )}
            </div>
    );
};

export default BeneficiaryManager;