import { useState, useEffect } from "react";

const BeneficiaryManager = () => {
    const [bankType, setBankType] = useState("same");
    const [beneficiaries, setBeneficiaries] = useState([
        { id: 1, name: "John Doe", account: "123456789", ifsc: "FAME0001234", amount: 5000, bank: "same" },
        { id: 2, name: "Jane Smith", account: "987654321", ifsc: "DIFF0005678", amount: 7000, bank: "different" },
        { id: 3, name: "Alice", account: "456789123", ifsc: "FAME0001234", amount: 3000, bank: "same" },
        { id: 4, name: "Bob", account: "789123456", ifsc: "DIFF0005678", amount: 9000, bank: "different" },
        { id: 5, name: "Charlie", account: "654321987", ifsc: "FAME0001234", amount: 2000, bank: "same" },
        { id: 6, name: "David", account: "321987654", ifsc: "DIFF0005678", amount: 6000, bank: "different" },
        { id: 7, name: "Eve", account: "987123654", ifsc: "FAME0001234", amount: 4000, bank: "same" },
        { id: 8, name: "Frank", account: "654987321", ifsc: "DIFF0005678", amount: 8000, bank: "different" },
        { id: 9, name: "Grace", account: "123789456", ifsc: "FAME0001234", amount: 1000, bank: "same" },
        { id: 10, name: "Harry", account: "987456321", ifsc: "DIFF0005678", amount: 2000, bank: "different" },
    ]);

    // Popup states
    const [showEditPopup, setShowEditPopup] = useState(false);
    const [showAddPopup, setShowAddPopup] = useState(false);
    const [selectedBeneficiary, setSelectedBeneficiary] = useState(null);
    const [updatedAmount, setUpdatedAmount] = useState("");
    const [otp, setOtp] = useState("");
    const [otpSent, setOtpSent] = useState(false);
    const [resendTimer, setResendTimer] = useState(30);

    // New Beneficiary Form State
    const [newBeneficiary, setNewBeneficiary] = useState({
        name: "",
        account: "",
        confirmAccount: "",
        ifsc: "",
        amount: ""
    });

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

    // Filter Beneficiaries
    const filteredBeneficiaries = beneficiaries.filter(b => b.bank === bankType);

    // Open Edit Popup
    const handleEdit = (beneficiary) => {
        setSelectedBeneficiary(beneficiary);
        setUpdatedAmount(beneficiary.amount);
        setShowEditPopup(true);
    };

    // Open Add Beneficiary Popup
    const handleAddBeneficiary = () => {
        setShowAddPopup(true);
    };

    // Handle Amount Change
    const handleAmountChange = (e) => setUpdatedAmount(e.target.value);

    // Send OTP
    const handleSendOtp = () => {
        setOtpSent(true);
        setResendTimer(30);
    };

    // Resend OTP
    const handleResendOtp = () => {
        setOtp("");
        setResendTimer(30);
    };

    // Confirm OTP for Updating Beneficiary
    const handleConfirmOtp = () => {
        if (otp === "1234") {
            setBeneficiaries(prev =>
                prev.map(b => (b.id === selectedBeneficiary.id ? { ...b, amount: updatedAmount } : b))
            );
            alert("Amount Updated Successfully!");
            setShowEditPopup(false);
            setOtp("");
            setOtpSent(false);
        } else {
            alert("Invalid OTP. Try Again.");
        }
    };

    // Confirm OTP for Adding New Beneficiary
    const handleConfirmAddOtp = () => {
        if (otp === "1234") {
            if (newBeneficiary.account !== newBeneficiary.confirmAccount) {
                alert("Account numbers do not match!");
                return;
            }
            const newEntry = {
                id: beneficiaries.length + 1,
                name: newBeneficiary.name,
                account: newBeneficiary.account,
                ifsc: newBeneficiary.ifsc,
                amount: newBeneficiary.amount,
                bank: bankType
            };
            setBeneficiaries([...beneficiaries, newEntry]);
            alert("Beneficiary Added Successfully!");
            setShowAddPopup(false);
            setNewBeneficiary({ name: "", account: "", confirmAccount: "", ifsc: "", amount: "" });
            setOtp("");
            setOtpSent(false);
        } else {
            alert("Invalid OTP. Try Again.");
        }
    };

    // Handle Delete Beneficiary
    const handleDelete = (id) => {
        setBeneficiaries(prev => prev.filter(b => b.id !== id));
    };

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
                    {filteredBeneficiaries.map((b) => (
                        <tr key={b.id} className="border-b">
                            <td className="py-2 text-center">{b.name}</td>
                            <td className="py-2 text-center">{b.account}</td>
                            <td className="py-2 text-center">{b.ifsc}</td>
                            <td className="py-2 text-center">₹{b.amount}</td>
                            <td className="py-2 flex justify-center gap-2">
                                <button onClick={() => handleEdit(b)} className="bg-green-500 text-white px-3 py-1 rounded hover:bg-green-600">Update</button>
                                <button onClick={() => handleDelete(b.id)} className="bg-red-500 text-white px-3 py-1 rounded hover:bg-red-600">Delete</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>

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
                            value={newBeneficiary.account} 
                            onChange={(e) => setNewBeneficiary({ ...newBeneficiary, account: e.target.value })} 
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
                            value={newBeneficiary.ifsc} 
                            onChange={(e) => setNewBeneficiary({ ...newBeneficiary, ifsc: e.target.value })} 
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
                                <button onClick={handleConfirmAddOtp} className="bg-green-500 text-white py-2 px-4 rounded w-full mt-2">Confirm OTP</button>
                                
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