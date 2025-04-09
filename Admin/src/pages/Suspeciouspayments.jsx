import React, { useState, useEffect, useContext } from 'react';
import { FiAlertCircle, FiSearch, FiChevronLeft, FiChevronRight, FiX, FiLock, FiLoader, FiCheck } from 'react-icons/fi';
import { AdminContext } from '../context/AdminContext';

const Suspeciouspayments = () => {
  const { getSuspiciousTransactions, verifyTransactionMpin } = useContext(AdminContext);
  
  const [transactions, setTransactions] = useState([]);
  const [currentPage, setCurrentPage] = useState(1);
  const [searchTerm, setSearchTerm] = useState('');
  const [itemsPerPage] = useState(10);
  const [showMpinModal, setShowMpinModal] = useState(false);
  const [selectedTransaction, setSelectedTransaction] = useState(false);
  const [actionType, setActionType] = useState(null);
  const [mpin, setMpin] = useState('');
  const [mpinError, setMpinError] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  // Filter transactions based on search
  const filteredTransactions = transactions.filter(tx =>
    tx.transactionId.toString().includes(searchTerm) ||
    tx.senderAccountNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
    tx.receiverAccountNumber.toLowerCase().includes(searchTerm.toLowerCase())
  );

  // Pagination logic
  const indexOfLastItem = currentPage * itemsPerPage;
  const indexOfFirstItem = indexOfLastItem - itemsPerPage;
  const currentItems = filteredTransactions.slice(indexOfFirstItem, indexOfLastItem);
  const totalPages = Math.ceil(filteredTransactions.length / itemsPerPage);

  // Format date
  const formatDate = (timestamp) => {
    return new Date(timestamp).toLocaleString();
  };

  // Format amount
  const formatAmount = (amount) => {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR'
    }).format(amount);
  };

  useEffect(() => {
    const fetchData = async () => {
      const data = await getSuspiciousTransactions();
      setTransactions(data);
    };
    fetchData();
  }, []);

  const handleActionClick = (transaction, action) => {
    setSelectedTransaction(transaction);
    setActionType(action);
    setMpin('');
    setMpinError('');
    setShowMpinModal(true);
  };

  const handleMpinSubmit = async () => {
    if (!mpin || mpin.length !== 6) {
      setMpinError('Please enter a valid 6-digit MPIN');
      return;
    }

    setIsLoading(true);
    try {
      const success = await verifyTransactionMpin(
        selectedTransaction.transactionId,
        mpin,
        actionType
      );

      if (success) {
        setShowMpinModal(false);
        // Refresh the transactions list
        const data = await getSuspiciousTransactions();
        setTransactions(data);
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="container mx-auto px-4 py-8">
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <h1 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
          <FiAlertCircle className="text-yellow-500" />
          Suspicious Transactions
        </h1>
        <div className="relative">
          <input
            type="text"
            placeholder="Search by ID or Account"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-10 pr-4 py-2 border rounded-lg focus:ring-2 focus:ring-yellow-500 focus:border-yellow-500"
          />
          <FiSearch className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Transaction ID
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Amount
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Date & Time
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Sender Details
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Receiver Details
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Description
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Actions
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {currentItems.map((transaction) => (
                <tr key={transaction.transactionId} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    #{transaction.transactionId}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {formatAmount(transaction.amount)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {formatDate(transaction.timestamp)}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-500">
                    <div className="font-medium">{transaction.senderName}</div>
                    <div className="text-xs">{transaction.senderAccountNumber}</div>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-500">
                    <div className="font-medium">{transaction.receiverName}</div>
                    <div className="text-xs">{transaction.receiverAccountNumber}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-yellow-100 text-yellow-800">
                      {transaction.marked}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-500">
                    {transaction.description}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <button 
                      onClick={() => handleActionClick(transaction, 'fraud')}
                      className="text-red-600 hover:text-red-900 mr-3"
                    >
                      Mark as Fraud
                    </button>
                    <button 
                      onClick={() => handleActionClick(transaction, 'normal')}
                      className="text-green-600 hover:text-green-900"
                    >
                      Clear
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Pagination */}
      <div className="flex items-center justify-between mt-4 bg-white px-4 py-3 border-t border-gray-200 sm:px-6 rounded-lg">
        <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
          <div>
            <p className="text-sm text-gray-700">
              Showing{' '}
              <span className="font-medium">{indexOfFirstItem + 1}</span>
              {' '}-{' '}
              <span className="font-medium">
                {Math.min(indexOfLastItem, filteredTransactions.length)}
              </span>
              {' '}of{' '}
              <span className="font-medium">{filteredTransactions.length}</span>
              {' '}results
            </p>
          </div>
          <div>
            <nav className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px">
              <button
                onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
                disabled={currentPage === 1}
                className="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
              >
                <FiChevronLeft className="h-5 w-5" />
              </button>
              {[...Array(totalPages)].map((_, idx) => (
                <button
                  key={idx + 1}
                  onClick={() => setCurrentPage(idx + 1)}
                  className={`relative inline-flex items-center px-4 py-2 border text-sm font-medium
                    ${currentPage === idx + 1
                      ? 'z-10 bg-yellow-50 border-yellow-500 text-yellow-600'
                      : 'bg-white border-gray-300 text-gray-500 hover:bg-gray-50'
                    }`}
                >
                  {idx + 1}
                </button>
              ))}
              <button
                onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
                disabled={currentPage === totalPages}
                className="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50"
              >
                <FiChevronRight className="h-5 w-5" />
              </button>
            </nav>
          </div>
        </div>
      </div>

      {/* Add MPIN Modal */}
      {showMpinModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg shadow-xl w-full max-w-md">
            <div className="px-6 py-4 border-b flex justify-between items-center">
              <h2 className="text-xl font-bold text-gray-800">
                Verify MPIN to {actionType === 'fraud' ? 'Mark as Fraud' : 'Clear'}
              </h2>
              <button 
                onClick={() => setShowMpinModal(false)}
                className="text-gray-500 hover:text-gray-700 p-1 rounded-full hover:bg-gray-100"
              >
                <FiX size={20} />
              </button>
            </div>
            
            <div className="p-6">
              <div className="mb-6">
                <p className="text-gray-600 mb-2">Transaction Details:</p>
                <p className="font-medium">#{selectedTransaction?.transactionId}</p>
                <p className="text-sm text-gray-500">Amount: {formatAmount(selectedTransaction?.amount)}</p>
              </div>

              <div className="space-y-3">
                <label className="block text-sm font-medium text-gray-700">
                  Enter Your MPIN
                </label>
                <div className="relative">
                  <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                    <FiLock className="text-gray-400" />
                  </div>
                  <input
                    type="password"
                    inputMode="numeric"
                    pattern="\d*"
                    maxLength={6}
                    className="pl-10 pr-4 py-3 w-full border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition text-center text-lg tracking-widest"
                    placeholder="••••••"
                    value={mpin}
                    onChange={(e) => setMpin(e.target.value)}
                  />
                </div>
                {mpinError && (
                  <p className="text-sm text-red-600">{mpinError}</p>
                )}
              </div>
            </div>

            <div className="px-6 py-4 border-t flex justify-end gap-3">
              <button
                onClick={() => setShowMpinModal(false)}
                className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 transition"
                disabled={isLoading}
              >
                Cancel
              </button>
              <button
                onClick={handleMpinSubmit}
                disabled={isLoading}
                className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition flex items-center gap-2"
              >
                {isLoading ? (
                  <>
                    <FiLoader className="animate-spin" />
                    Processing...
                  </>
                ) : (
                  <>
                    <FiCheck /> Confirm
                  </>
                )}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Suspeciouspayments;