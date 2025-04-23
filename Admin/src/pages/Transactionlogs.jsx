import { useState, useContext, useEffect } from 'react';
import { FiSearch, FiFilter, FiEye, FiDollarSign, FiUser, FiCreditCard } from 'react-icons/fi';
import { AdminContext } from '../context/AdminContext';

const TransactionLogsPage = () => {
  const { getTransactionLogs, TransactionsLogs } = useContext(AdminContext);

  const reference = 'INV20231017-003'
  const location = 'Bangalore'
  const device = 'Laptop'
  const ipAddress = '172.58.10.33'
  
  // State management
  const [searchTerm, setSearchTerm] = useState('');
  const [bankFilter, setBankFilter] = useState('all');
  const [statusFilter, setStatusFilter] = useState('all');
  const [selectedTransaction, setSelectedTransaction] = useState(null);
  const [showDetails, setShowDetails] = useState(false);

  // Filter transactions
  const filteredTransactions = TransactionsLogs?.filter(txn => {
    const matchesSearch = txn.transactionId.toString().includes(searchTerm.toLowerCase());
    const matchesBank = bankFilter === 'all' || txn.senderBank === bankFilter;
    const matchesStatus = statusFilter === 'all' || txn.status === statusFilter;
    
    return matchesSearch && matchesBank && matchesStatus;
  });

  // View transaction details
  const viewTransactionDetails = (txn) => {
    setSelectedTransaction(txn);
    setShowDetails(true);
  };

  useEffect(() => {
    getTransactionLogs();
  }, []);

  // Format date
  const formatDate = (timestamp) => {
    return new Date(timestamp).toLocaleString();
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Transaction Logs</h1>
      
      {/* Filters Section */}
      <div className="bg-white rounded-lg shadow p-4 mb-6">
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {/* Search by Transaction ID */}
          <div className="relative">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <FiSearch className="text-gray-400" />
            </div>
            <input
              type="text"
              placeholder="Search by Transaction ID"
              className="pl-10 pr-4 py-2 w-full border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>
          
          {/* Bank Filter */}
          <div className="flex items-center gap-2 bg-gray-100 rounded-lg px-4">
            <FiFilter className="text-gray-600" />
            <select
              className="bg-transparent py-2 focus:outline-none w-full"
              value={bankFilter}
              onChange={(e) => setBankFilter(e.target.value)}
            >
              <option value="all">All Banks</option>
              <option value="HDFC">HDFC</option>
              <option value="ICICI">ICICI</option>
              <option value="SBI">SBI</option>
            </select>
          </div>
          
          {/* Status Filter */}
          <div className="flex items-center gap-2 bg-gray-100 rounded-lg px-4">
            <FiFilter className="text-gray-600" />
            <select
              className="bg-transparent py-2 focus:outline-none w-full"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="all">All Statuses</option>
              <option value="COMPLETED">Completed</option>
              <option value="FAILED">Failed</option>
              <option value="PENDING">Pending</option>
            </select>
          </div>
        </div>
      </div>

      {/* Transactions Table */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">TXN ID</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Amount</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date & Time</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Sender</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Receiver</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Bank</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {filteredTransactions?.length > 0 ? (
                filteredTransactions.map((txn) => (
                  <tr key={txn.transactionId}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      {txn.transactionId}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      ₹{txn.amount.toLocaleString('en-IN')}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {formatDate(txn.timestamp)}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-500">
                      <div className="font-medium">{txn.senderName}</div>
                      <div className="text-xs">{txn.senderAccountNumber}</div>
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-500">
                      <div className="font-medium">{txn.receiverName}</div>
                      <div className="text-xs">{txn.receiverAccountNumber}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                        ${txn.senderBank === 'HDFC' ? 'bg-blue-100 text-blue-800' : 
                          txn.senderBank === 'ICICI' ? 'bg-purple-100 text-purple-800' : 
                          'bg-green-100 text-green-800'}`}>
                        {txn.senderBank}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                        ${txn.status === 'COMPLETED' ? 'bg-green-100 text-green-800' : 
                          txn.status === 'FAILED' ? 'bg-red-100 text-red-800' : 
                          'bg-yellow-100 text-yellow-800'}`}>
                        {txn.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      <button 
                        onClick={() => viewTransactionDetails(txn)}
                        className="text-blue-600 hover:text-blue-900 flex items-center gap-1"
                      >
                        <FiEye /> View
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="8" className="px-6 py-4 text-center text-sm text-gray-500">
                    No transactions found matching your filters
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Transaction Details Modal */}
      {showDetails && selectedTransaction && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className="px-6 py-4 border-b">
              <div className="flex items-center justify-between">
                <h2 className="text-xl font-bold text-gray-800">
                  Transaction Details: {selectedTransaction.transactionId}
                </h2>
                <button 
                  onClick={() => setShowDetails(false)}
                  className="text-gray-500 hover:text-gray-700"
                >
                  ✕
                </button>
              </div>
            </div>
            
            <div className="p-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* Basic Information */}
                <div className="space-y-4">
                  <h3 className="text-lg font-semibold text-gray-800 flex items-center gap-2">
                    <FiDollarSign /> Transaction Information
                  </h3>
                  <div className="space-y-2">
                    <DetailItem label="Amount" value={`₹${selectedTransaction.amount.toLocaleString('en-IN')}`} />
                    <DetailItem label="Date & Time" value={formatDate(selectedTransaction.timestamp)} />
                    <DetailItem label="Status" value={
                      <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                        ${selectedTransaction.status === 'COMPLETED' ? 'bg-green-100 text-green-800' : 
                          selectedTransaction.status === 'FAILED' ? 'bg-red-100 text-red-800' : 
                          'bg-yellow-100 text-yellow-800'}`}>
                        {selectedTransaction.status}
                      </span>
                    } />
                    {selectedTransaction.marked === "SUSPICIOUS" && (
                      <DetailItem label="Marked As" value={selectedTransaction.marked} />
                    )}
                    <DetailItem label="Description" value={selectedTransaction.description} />
                  </div>
                </div>
                
                {/* Parties Involved */}
                <div className="space-y-4">
                  <h3 className="text-lg font-semibold text-gray-800 flex items-center gap-2">
                    <FiUser /> Parties Involved
                  </h3>
                  <div className="space-y-4">
                    <div className="bg-gray-50 p-3 rounded-lg">
                      <h4 className="text-sm font-medium text-gray-500 mb-1">Sender</h4>
                      <p className="font-medium">{selectedTransaction.senderName}</p>
                      <p className="text-sm text-gray-600">{selectedTransaction.senderAccountNumber}</p>
                      <p className="text-sm text-gray-600">Bank: {selectedTransaction.senderBank}</p>
                    </div>
                    <div className="bg-gray-50 p-3 rounded-lg">
                      <h4 className="text-sm font-medium text-gray-500 mb-1">Receiver</h4>
                      <p className="font-medium">{selectedTransaction.receiverName}</p>
                      <p className="text-sm text-gray-600">{selectedTransaction.receiverAccountNumber}</p>
                      <p className="text-sm text-gray-600">Bank: {selectedTransaction.receiverBank}</p>
                    </div>
                  </div>
                </div>

                {/* Additional Details */}
                <div className="md:col-span-2 space-y-4">
                  <h3 className="text-lg font-semibold text-gray-800 flex items-center gap-2">
                    <FiCreditCard /> Additional Details
                  </h3>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <DetailItem label="Reference ID" value={reference} />
                    <DetailItem label="Location" value={location} />
                    <DetailItem label="Device" value={device} />
                    <DetailItem label="IP Address" value={ipAddress} />
                    <DetailItem label="Bank" value={selectedTransaction.senderBank} />
                  </div>
                </div>
              </div>
            </div>
            
            <div className="px-6 py-4 border-t flex justify-end">
              <button
                onClick={() => setShowDetails(false)}
                className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

const DetailItem = ({ label, value }) => (
  <div>
    <p className="text-sm font-medium text-gray-500">{label}</p>
    <p className="text-sm text-gray-900 mt-1">{value}</p>
  </div>
);

export default TransactionLogsPage;