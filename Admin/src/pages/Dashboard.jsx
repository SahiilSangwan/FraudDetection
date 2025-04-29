import { useState, useEffect, useContext } from 'react';
import { AdminContext } from '../context/AdminContext'
import { FiArrowUp, FiArrowDown, FiDollarSign, FiAlertTriangle, FiCheckCircle, FiTrendingUp, FiActivity, FiRefreshCw } from 'react-icons/fi';
import { Bar } from 'react-chartjs-2';
import {Chart as ChartJS, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend} from 'chart.js';

ChartJS.register(CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend);

const AdminDashBoard = () => {

  const {DashTransaction,bankStats,recentTransactions, banks} = useContext(AdminContext)
  const [loading, setLoading] = useState(false);

  const refreshData = () => {
    setLoading(true);  
    setTimeout(() => {
      setLoading(false);
    }, 1000);
  };

  const getChartData = () => {
    const labels = banks.map(bank => bank.name);
    
    return {
      labels,
      datasets: [
        {
          label: 'Suspicious Transactions',
          data: banks.map(bank => bank.suspicious),
          backgroundColor: 'rgba(255, 159, 64, 0.7)',
          borderColor: 'rgba(255, 159, 64, 1)',
          borderWidth: 1
        },
        {
          label: 'Failed Transactions',
          data: banks.map(bank => bank.failed),
          backgroundColor: 'rgba(255, 99, 132, 0.7)',
          borderColor: 'rgba(255, 99, 132, 1)',
          borderWidth: 1
        },
        {
          label: 'Successful Transactions',
          data: banks.map(bank => bank.success),
          backgroundColor: 'rgba(75, 192, 192, 0.7)',
          borderColor: 'rgba(75, 192, 192, 1)',
          borderWidth: 1
        },
        {
          label: 'Fraud Transactions',
          data: banks.map(bank => bank.fraud),
          backgroundColor: 'rgba(153, 102, 255, 0.7)',
          borderColor: 'rgba(153, 102, 255, 1)',
          borderWidth: 1
        }
      ]
    };
  };

  useEffect(()=>{
    DashTransaction()
    bankStats()
  },[])

  return (
    <div className="flex-1 min-h-screen p-4 md:p-6 ml-0">
      {/* Header */}
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold text-gray-800">Admin Dashboard</h1>
        <button 
          onClick={refreshData}
          className="flex items-center gap-2 bg-indigo-600 text-white px-4 py-2 rounded hover:bg-indigo-700 transition"
          disabled={loading}
        >
          <FiRefreshCw className={`${loading ? 'animate-spin' : ''}`} />
          Refresh
        </button>
      </div>

      {/* Bank Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        {banks.map((bank, index) => (
          <div key={index} className="bg-white rounded-lg shadow-sm p-6 hover:shadow-md transition border border-gray-100">
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-xl font-semibold text-gray-800">{bank.name} Bank</h2>
              <div className={`w-10 h-10 rounded-full flex items-center justify-center 
                ${bank.name === 'HDFC' ? 'bg-blue-100 text-blue-600' : 
                  bank.name === 'ICICI' ? 'bg-purple-100 text-purple-600' : 
                  'bg-green-100 text-green-600'}`}>
                <FiDollarSign size={20} />
              </div>
            </div>

            <div className="space-y-4">
            <div className="flex justify-between items-center">
                   <div className="flex items-center gap-2">
                     <FiArrowDown className="text-green-500" />
                     <span className="text-gray-600">Incoming</span>
                   </div>
                   <span className="font-medium">{bank.incoming.toLocaleString()}</span>
                 </div>

                 <div className="flex justify-between items-center">
                   <div className="flex items-center gap-2">
                     <FiArrowUp className="text-red-500" />
                     <span className="text-gray-600">Outgoing</span>
                   </div>
                   <span className="font-medium">{bank.outgoing.toLocaleString()}</span>
                 </div>

                 <div className="grid grid-cols-2 gap-4 pt-4 border-t">
                   <div className="text-center">
                     <div className="flex items-center justify-center gap-1 mb-1">
                       <FiAlertTriangle className="text-yellow-500" />
                       <span className="text-sm text-gray-600">Suspicious</span>
                     </div>
                     <span className="font-medium text-yellow-600">{bank.suspicious}</span>
                   </div>

                   <div className="text-center">
                     <div className="flex items-center justify-center gap-1 mb-1">
                       <FiCheckCircle className="text-green-500" />
                       <span className="text-sm text-gray-600">Success</span>
                     </div>
                     <span className="font-medium text-green-600">{bank.success}</span>
                   </div>

                   <div className="text-center">
                     <div className="flex items-center justify-center gap-1 mb-1">
                       <FiAlertTriangle className="text-red-500" />
                       <span className="text-sm text-gray-600">Failed</span>
                     </div>
                     <span className="font-medium text-red-600">{bank.failed}</span>
                   </div>

                   <div className="text-center">
                     <div className="flex items-center justify-center gap-1 mb-1">
                       <FiAlertTriangle className="text-purple-500" />
                       <span className="text-sm text-gray-600">Fraud</span>
                     </div>
                    <span className="font-medium text-purple-600">{bank.fraud}</span>
                   </div>
                 </div>
            </div>
          </div>
        ))}
      </div>

      {/* Recent Transactions */}
      <div className="bg-white rounded-lg shadow-sm p-6 mb-8 border border-gray-100">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-semibold text-gray-800">Recent Transactions</h2>
          <button className="text-indigo-600 hover:text-indigo-800 text-sm">
            View All
          </button>
        </div>

        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
             <thead className="bg-gray-50">
                 <tr>
                   <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">TXN ID</th>
                   <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Sender's Bank</th>
                   <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Amount</th>
                   <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Receiver's Bank</th>
                   <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                   <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Time</th>
                 </tr>
               </thead>
               <tbody className="bg-white divide-y divide-gray-200">
                 {recentTransactions.map((txn, index) => (
                  <tr key={index}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">{txn.transactionId}</td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                        ${txn.senderBank === 'WISSEN' ? 'bg-blue-100 text-blue-800' : 
                          txn.senderBank === 'HERITAGE' ? 'bg-purple-100 text-purple-800' : 
                          'bg-green-100 text-green-800'}`}>
                        {txn.senderBank}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      ₹{txn.amount.toLocaleString('en-IN')}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                        ${txn.receiverBank === 'WISSEN' ? 'bg-blue-100 text-blue-800' : 
                          txn.receiverBank === 'HERITAGE' ? 'bg-purple-100 text-purple-800' : 
                          'bg-green-100 text-green-800'}`}>
                        {txn.receiverBank}
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
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {txn.timestamp}
                    </td>
                  </tr>
                ))}
              </tbody>
          </table>
        </div>
      </div>

      {/* Transaction Analytics */}
      <div className="bg-white rounded-lg shadow-sm p-6 border border-gray-100">
        <div className="flex justify-between items-center mb-4">
          <h2 className="text-xl font-semibold text-gray-800">Transaction Analytics</h2>
          <div className="flex gap-2">
            <button className="px-3 py-1 bg-indigo-100 text-indigo-700 rounded-md text-sm">
              Daily
            </button>
            <button className="px-3 py-1 bg-gray-100 text-gray-700 rounded-md text-sm">
                 Weekly
            </button>
            <button className="px-3 py-1 bg-gray-100 text-gray-700 rounded-md text-sm">
                 Monthly
            </button>
          </div>
        </div>

        <div className="h-80">
          <Bar
            data={getChartData()}
            options={{
              responsive: true,
              maintainAspectRatio: false,
              plugins: {
                legend: {
                  position: 'top',
                },
                title: {
                  display: true,
                  text: 'Transaction Status Across Banks',
                },
              },
              scales: {
                y: {
                  beginAtZero: true,
                  title: {
                    display: true,
                    text: 'Number of Transactions'
                  }
                },
                x: {
                  title: {
                    display: true,
                    text: 'Banks'
                  }
                }
              }
            }}
          />
        </div>
      </div>
    </div>
  );
};

export default AdminDashBoard;