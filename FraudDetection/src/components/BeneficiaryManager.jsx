import { useState, useEffect, useContext } from "react";
import { UserContext } from "../context/UserContext";

const BeneficiaryManager = () => {
    const [bankType, setBankType] = useState("same");

    const {beneficiaries, getUserBeneficiaries, deleteBeneficiaries,handleUpdateBeneficiary, sendOTP,
        verifyOTP, setNewBeneficiary, newBeneficiary } = useContext(UserContext);


    // Popup states
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
        <div className="max-w-full mx-auto p-6 bg-gray-100 min-h-screen">
            <h2 className="text-2xl font-semibold mb-4 text-center">Manage Beneficiaries</h2>

            {/* Bank Selection */}
            <div className="flex gap-4 mb-6">
                <label className="flex items-center space-x-2 cursor-pointer">
                    <input type="checkbox" checked={bankType === "same"} onChange={() => handleBankChange("same")} className="w-5 h-5" />
                    <span>Same Bank</span>
                </label>
                <label className="flex items-center space-x-2 cursor-pointer">
                    <input type="checkbox" checked={bankType === "different"} onChange={() => handleBankChange("different")} className="w-5 h-5" />
                    <span>Different Bank</span>
                </label>
            </div>

            {/* Add Beneficiary Button */}
            <button onClick={handleAddBeneficiary} className="bg-blue-600 text-white py-2 px-4 rounded mb-4 hover:bg-blue-700">
                + Add New Beneficiary
            </button>

            {/* Beneficiary List */}
            <table className="w-full bg-white shadow-md rounded-lg overflow-hidden">
                <thead className="bg-gray-800 text-white">
                    <tr>
                        <th className="py-2">Name</th>
                        <th className="py-2">Account No</th>
                        <th className="py-2">IFSC</th>
                        <th className="py-2">Amount</th>
                        <th className="py-2">Actions</th>
                    </tr>
                </thead>
                <tbody>
                    {beneficiaries.map((b,index) => (
                        <tr key={index} className="border-b">
                            <td className="py-2 text-center">{b.beneficiaryName}</td>
                            <td className="py-2 text-center">{b.beneficiaryAccountNumber}</td>
                            <td className="py-2 text-center">{b.beneficiaryBank}</td>
                            <td className="py-2 text-center">₹{b.amount}</td>
                            <td className="py-2 flex justify-center gap-2">
                                <button onClick={() => handleEdit(b.beneficiaryId)} className="bg-green-500 text-white px-3 py-1 rounded hover:bg-green-600">Update</button>
                                <button onClick={() => deleteBeneficiaries(b.beneficiaryId)} className="bg-red-500 text-white px-3 py-1 rounded hover:bg-red-600">Delete</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

            {/* Edit Beneficiary Popup */}
            {isUpdateOpen && (
                <div className="fixed inset-0 flex items-center justify-center bg-gray-900 bg-opacity-50">
                    <div className="bg-white p-6 rounded-lg shadow-lg w-96">
                        <h2 className="text-xl font-bold mb-4">Enter Amount</h2>
                        <input
                            type="number"
                            value={amount}
                            onChange={(e) => setAmount(e.target.value)}
                            className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500"
                            placeholder="Enter amount"
                        />
                        <div className="flex justify-end mt-4">
                            <button
                                onClick={() => setIsUpdateOpen(false)}
                                className="px-4 py-2 bg-gray-400 text-white rounded-lg mr-2 hover:bg-gray-500"
                            >
                                Cancel
                            </button>
                            <button
                                onClick={() => { handleUpdateBeneficiary(selectedBeneficiary, amount); setIsUpdateOpen(false);}}
                                className="px-4 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600"
                            >
                                Update
                            </button>
                        </div>
                    </div>
                </div>
            )}



            {/* Add Beneficiary Popup */}
            {showAddPopup && (
                <div className="fixed inset-0 flex items-center justify-center bg-white bg-opacity-50 backdrop-blur-md">
                    <div className="bg-white p-6 rounded shadow-lg relative w-96 md:w-1/2 lg:w-1/3">
                        {/* Close Button */}
                        <button className="absolute top-2 right-2 text-red-500 text-xl" onClick={() => setShowAddPopup(false)}>✖</button>
                        <h3 className="text-xl font-semibold mb-4">Add Beneficiary</h3>

                        {/* Name */}
                        <input 
                            type="text" 
                            value={newBeneficiary.name} 
                            onChange={(e) => setNewBeneficiary({ ...newBeneficiary, name: e.target.value })} 
                            placeholder="Name" 
                            className="w-full p-2 border rounded mb-2"
                        />

                        {/* Account Number */}
                        <input 
                            type="text" 
                            value={newBeneficiary.accountNumber} 
                            onChange={(e) => setNewBeneficiary({ ...newBeneficiary, accountNumber: e.target.value })} 
                            placeholder="Account Number" 
                            className="w-full p-2 border rounded mb-2"
                        />

                        {/* Confirm Account Number */}
                        <input 
                            type="text" 
                            value={newBeneficiary.confirmAccount} 
                            onChange={(e) => setNewBeneficiary({ ...newBeneficiary, confirmAccount: e.target.value })} 
                            placeholder="Confirm Account Number" 
                            className="w-full p-2 border rounded mb-2"
                        />

                        {/* IFSC Code */}
                        <input 
                            type="text" 
                            value={newBeneficiary.ifscCode} 
                            onChange={(e) => setNewBeneficiary({ ...newBeneficiary, ifscCode: e.target.value })} 
                            placeholder="IFSC Code" 
                            className="w-full p-2 border rounded mb-2"
                        />

                        {/* Amount */}
                        <input 
                            type="number" 
                            value={newBeneficiary.amount} 
                            onChange={(e) => setNewBeneficiary({ ...newBeneficiary, amount: e.target.value })} 
                            placeholder="Amount" 
                            className="w-full p-2 border rounded mb-2"
                        />

                        {/* Send OTP / OTP Verification */}
                        {!otpSent ? (
                            <button onClick={handleSendOtp} className="bg-blue-500 text-white py-2 px-4 rounded w-full">Send OTP</button>
                        ) : (
                            <>
                                <input 
                                    type="text" 
                                    value={otp} 
                                    onChange={(e) => setOtp(e.target.value)} 
                                    placeholder="Enter OTP" 
                                    className="w-full p-2 border rounded mt-2"
                                />
                                <button onClick={() => verifyOTP(otp)} className="bg-green-500 text-white py-2 px-4 rounded w-full mt-2">Confirm OTP</button>
                                
                                {resendTimer === 0 ? (
                                    <button onClick={handleResendOtp} className="text-blue-500 mt-2">Resend OTP</button>
                                ) : (
                                    <p className="text-gray-500 mt-2 text-center">Resend OTP in {resendTimer}s</p>
                                )}
                            </>
                        )}
                    </div>
                </div>
            )}

        </div>
    );
};

export default BeneficiaryManager;