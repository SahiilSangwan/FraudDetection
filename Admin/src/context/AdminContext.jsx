import { createContext, useState } from "react";
import axios from "axios";
import { toast } from 'react-toastify'

export const AdminContext = createContext()

const AdminContextProvider = (props) => {

    const [aToken,setAToken] = useState(localStorage.getItem('aToken')?localStorage.getItem('aToken'):'')

    const backendUrl = import.meta.env.VITE_BACKEND_URL

    const [adminData, setAdminData] = useState({ });
    const adminId = localStorage.getItem('id')
    const email = localStorage.getItem('email')

    const [banks, setBanks] = useState([]);
    const [TransactionsLogs, setTransactionsLogs] = useState([]);
    const [aBlockedUsers, setABlockedUsers] = useState([]);
    const [recentTransactions, setRecentTransactions] = useState([]);
    
   
    const logoutuser = async () =>{

        try{
            const { data } = await axios.post(backendUrl+"/api/admin/logout",{}, { withCredentials: true });
        }catch (error) {
            toast.error(error.message)
        }
    }






    const adminDetail = async () =>{

        try{
            const { data } = await axios.get(backendUrl+`/api/admin/${adminId}`, { withCredentials: true });
            if(data != null){
                setAdminData(data)
            }
        }catch (error) {
            toast.error(error.message)
        }
    }

    const bankStats = async () =>{

        try{
            const {data} = await axios.get(backendUrl+`/api/admin/transactions/stats`, { withCredentials: true });
            if(data != null){
                setBanks(data)
            }
        }catch (error) {
            toast.error(error.message)
        }
    }

        // Add this function to your AdminContext
    const getFraudTransactions = async () => {
      try {
        const response = await axios.get(`${backendUrl}/api/admin/transactions/fraud`, { withCredentials: true });
        return response.data;
      } catch (error) {
        console.error('Error fetching fraud transactions:', error);
        return [];
      }
    };

    const DashTransaction = async () =>{

        try{
            const {data} = await axios.get(backendUrl+`/api/admin/transactions/latest`, { withCredentials: true });
            if(data != null){
                setRecentTransactions(data)
            }
        }catch (error) {
            toast.error(error.message)
        }
    }


    const getBlockedUsers = async () =>{

        try{
            const {data} = await axios.get(backendUrl+`/api/admin/blocked-users`, { withCredentials: true });
            if(data != null){
                setABlockedUsers(data)
            }
        }catch (error) {
            toast.error(error.message)
        }
    }
    const getTransactionLogs = async () =>{

        try{
            const {data} = await axios.get(backendUrl+`/api/admin/transactions/all-latest`, { withCredentials: true });
            if(data != null){
                setTransactionsLogs(data)
            }
        }catch (error) {
            toast.error(error.message)
        }
    }

    const getSuspiciousTransactions = async () => {
        try {
          const response = await axios.get(`${backendUrl}/api/admin/transactions/suspicious`, { withCredentials: true });
          return response.data;
        } catch (error) {
          console.error('Error fetching suspicious transactions:', error);
          return [];
        }
      };

      const sendAOTP = async () => {
        try {
            await axios.post(backendUrl + '/api/admin/sendotp', { email }, { withCredentials: true });
            toast.success(`OTP sent to ${email}. Check your inbox.`);
            return true; 
        } catch (error) {
            toast.error("Failed to send OTP. Try again.");
            console.error("OTP Send Error:", error);
            return false; 
        }
    };
   
    const verifyAOTP = async (otp,mpin) =>{

        try {
            const purpose = "Admin"
            const {data} = await axios.post(backendUrl + '/api/admin/verifyotp', { email, otp, purpose },{withCredentials: true});
      
            if (data.otpVerified) {
                  

                        const { data } = await axios.post(backendUrl+`/api/admin/update-mpin`,{email,mpin},{ withCredentials: true });
                            if(data.status){
                                return true; 
                            }else{
                                return false;
                            }
            }else{
              toast.error("Invalid OTP. Please try again.");
            }
          } catch (error) {
            toast.error(error.response?.data?.error);
          }
    }

    const verifyUnblockMpin = async (id,mpin) =>{

        try {
            const {data} = await axios.post(backendUrl + '/api/admin/verify-mpin', { email, mpin },{withCredentials: true});
            console.log(data);
      
            if (data.success) {
                  

                        const { data } = await axios.delete(backendUrl+`/api/admin/blocked-users/${id}`,{ withCredentials: true });
                            if(data.success){
                                toast.success(data.message);
                            }else{
                                toast.error(data.message);
                            }
            }else{
              toast.error(data.message);
            }
          } catch (error) {
            toast.error(error.response?.data?.error);
          }
    }

    const verifyTransactionMpin = async (transactionId, mpin, action) => {
        try {
          const { data } = await axios.post( `${backendUrl}/api/admin/verify-mpin`,{ mpin,email },{ withCredentials: true } );
    
          if (data.success) {
            if (action === 'fraud') {
              await markAsFraud(transactionId);
            } else {
              await markAsNormal(transactionId);
            }
            return true;
          } else {
            toast.error(data.message);
            return false;
          }
        } catch (error) {
          console.error('Error verifying MPIN:', error);
          toast.error(error.response?.data?.message || 'Verification failed');
          return false;
        }
      };



      const markAsFraud = async (transactionId) => {
        try {
          const { data } = await axios.put(
            `${backendUrl}/api/admin/transactions/${transactionId}/mark-fraud`,
                {},
                { withCredentials: true }
            );
            if (data.success) {
                toast.success('Transaction marked as fraud');
                return true;
            }
        } catch (error) {
            toast.error('Failed to mark transaction as fraud');
            return false;
        }
    };

// Mark transaction as normal
const markAsNormal = async (transactionId) => {
  try {
    const { data } = await axios.put(
      `${backendUrl}/api/admin/transactions/${transactionId}/mark-normal`,
      {},
      { withCredentials: true }
    );
    if (data.success) {
      toast.success('Transaction cleared');
      return true;
    }
  } catch (error) {
    toast.error('Failed to clear transaction');
    return false;
  }
};

    const value ={
        aToken,setAToken,
        backendUrl,logoutuser,
        adminDetail,adminData,
        DashTransaction,bankStats,
        recentTransactions,banks,
        getBlockedUsers,aBlockedUsers,
        getTransactionLogs,TransactionsLogs,
        getFraudTransactions,
        getSuspiciousTransactions,
        sendAOTP,verifyAOTP,
        verifyUnblockMpin,
        verifyTransactionMpin,

    }

    return (
        <AdminContext.Provider value={value}>
            {props.children}
        </AdminContext.Provider>
    )

}

export default AdminContextProvider