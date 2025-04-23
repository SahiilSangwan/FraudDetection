import { useState, useContext, useEffect } from 'react';
import { FiFilter, FiSearch, FiUserX } from 'react-icons/fi';
import { AdminContext } from '../context/AdminContext';
import { FiLock, FiX, FiCheck } from 'react-icons/fi';

const BlockedUsersPage = () => {
  const { getBlockedUsers, aBlockedUsers, verifyUnblockMpin } = useContext(AdminContext);

  const [searchTerm, setSearchTerm] = useState('');
  const [bankFilter, setBankFilter] = useState('all');

  const filteredUsers = aBlockedUsers?.filter(user => {

    const bankMatch = bankFilter === 'all' || user.bankName.toUpperCase() === bankFilter;

    const searchMatch =
      user.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      user.accountNumber?.includes(searchTerm) ||
      user.email?.toLowerCase().includes(searchTerm.toLowerCase());

    return bankMatch && searchMatch;
  }) || [];



  const [showUnblockModal, setShowUnblockModal] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [mpin, setMpin] = useState('');
  const [mpinError, setMpinError] = useState('');

  const handleUnblockClick = (user) => {
    setSelectedUser(user);
    setShowUnblockModal(true);
    setMpin('');
    setMpinError('');
  };

  const handleUnblockSubmit = () => {
    if (!mpin || mpin.length !== 6) {
      setMpinError('Please enter a valid 6-digit MPIN');
      return;
    }
    
    verifyUnblockMpin(selectedUser.id, mpin)
    
    setShowUnblockModal(false);

    setTimeout(() => {
      getBlockedUsers();
    }, 1000);
  };



  useEffect(() => {
    getBlockedUsers();
  }, [setShowUnblockModal]);

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-2xl font-bold text-gray-800 mb-6">Blocked Users</h1>

      {/* Filters Section */}
      <div className="bg-white rounded-lg shadow p-4 mb-6">
        <div className="flex flex-col md:flex-row gap-4">
          {/* Search Input */}
          <div className="relative flex-1">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <FiSearch className="text-gray-400" />
            </div>
            <input
              type="text"
              placeholder="Search by name, account or email"
              className="pl-10 pr-4 py-2 w-full border rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          {/* Bank Filter */}
          <div className="flex items-center gap-2 bg-gray-100 rounded-lg px-4">
            <FiFilter className="text-gray-600" />
            <select
              className="bg-transparent py-2 focus:outline-none"
              value={bankFilter}
              onChange={(e) => setBankFilter(e.target.value)}
            >
              <option value="all">All Banks</option>
              <option value="HDFC">HDFC</option>
              <option value="ICICI">ICICI</option>
              <option value="SBI">SBI</option>
            </select>
          </div>
        </div>
      </div>

      {/* Users Table */}
      <div className="bg-white rounded-lg shadow overflow-hidden">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Account</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Bank</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Blocked Date</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Reason</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {filteredUsers.length > 0 ? (
                filteredUsers.map((user) => (
                  <tr key={user.id}>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <div className="flex-shrink-0 h-10 w-10 rounded-full bg-gray-200 flex items-center justify-center">
                          <FiUserX className="text-gray-600" />
                        </div>
                        <div className="ml-4">
                          <div className="text-sm font-medium text-gray-900">{user.name}</div>
                          <div className="text-sm text-gray-500">{user.email}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {user.accountNumber}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                        ${user.bankName.toUpperCase() === 'HDFC' ? 'bg-blue-100 text-blue-800' : 
                          user.bankName.toUpperCase() === 'ICICI' ? 'bg-purple-100 text-purple-800' : 
                          'bg-green-100 text-green-800'}`}>
                        {user.bankName.toUpperCase()}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {new Date(user.blockedAt).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 text-sm text-gray-500">
                      {user.reason}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <button 
                        onClick={() => handleUnblockClick(user)}
                        className="text-green-600 hover:text-green-900 flex items-center gap-1 transition"
                      >
                        Unblock
                      </button>
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="6" className="px-6 py-4 text-center text-sm text-gray-500">
                    No blocked users found matching your filters
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* MPIN Verification */}
        {showUnblockModal && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
            <div className="bg-white rounded-lg shadow-xl w-full max-w-md">
              <div className="px-6 py-4 border-b flex justify-between items-center">
                <h2 className="text-xl font-bold text-gray-800">
                  Verify MPIN to Unblock
                </h2>
                <button 
                  onClick={() => setShowUnblockModal(false)}
                  className="text-gray-500 hover:text-gray-700 p-1 rounded-full hover:bg-gray-100"
                >
                  <FiX size={20} />
                </button>
              </div>
              
              <div className="p-6">
                <div className="mb-6">
                  <p className="text-gray-600 mb-2">You are unblocking:</p>
                  <p className="font-medium">{selectedUser?.name}</p>
                  <p className="text-sm text-gray-500">{selectedUser?.bankName} • {selectedUser?.accountNumber}</p>
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
                      onChange={(e) => {
                        setMpin(e.target.value.replace(/\D/g, ''));
                        setMpinError('');
                      }}
                    />
                  </div>
                  {mpinError && (
                    <p className="text-sm text-red-600">{mpinError}</p>
                  )}
                </div>
              </div>

              <div className="px-6 py-4 border-t flex justify-end gap-3">
                <button
                  onClick={() => setShowUnblockModal(false)}
                  className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 transition"
                >
                  Cancel
                </button>
                <button
                  onClick={handleUnblockSubmit}
                  className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 transition flex items-center gap-2"
                >
                  <FiCheck /> Confirm Unblock
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default BlockedUsersPage;



