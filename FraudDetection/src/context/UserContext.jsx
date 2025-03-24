import { createContext, useState } from "react";
import axios from "axios";
import { toast } from 'react-toastify'
import { useNavigate } from "react-router-dom";

export const UserContext = createContext()

const UserContextProvider = (props) => {

    const navigate = useNavigate(); 

    const storedUser = JSON.parse(localStorage.getItem("user"));
    const userid = storedUser?.userId || "";
    const email = storedUser?.email || "";

    const [uToken,setUToken] = useState(localStorage.getItem('uToken')?localStorage.getItem('uToken'):'')
    const [vToken,setVToken] = useState(localStorage.getItem('vToken')?localStorage.getItem('vToken'):'')
    const [user,setUser] = useState({})
    const [account,setAccount] = useState({})
    const [beneficiaries,setBeneficiaries] = useState([])
    const [transactionBeneficiaries,setTransactionBeneficiaries] = useState([])
    const [transactions,setTransactions] = useState([])
    const [sameBank,setSameBank] = useState(false)

    // Transaction Page
    const [description, setDescription] = useState(localStorage.getItem("description") || "");
    const [amount, setAmount] = useState(localStorage.getItem("amount") || "");
    const [receiverAcc, setReceiverAcc] = useState(localStorage.getItem("receiverAcc") || "");
    const [ifscCodeUser, setIfscCodeUser] = useState(localStorage.getItem("ifscCodeUser") || "");
    const [selectedBeneficiaryID, setSelectedBeneficiaryID] = useState(
        localStorage.getItem("selectedBeneficiaryID") ? Number(localStorage.getItem("selectedBeneficiaryID")) : null);
    const [confirmationData, setConfirmationData] = useState({});

    // New Beneficiary Form State
    const [newBeneficiary, setNewBeneficiary] = useState({
        name: "",
        accountNumber: "",
        confirmAccount: "",
        ifscCode: "",
        amount: ""
    });


    const bank =localStorage.getItem('bank');

    const backendUrl = import.meta.env.VITE_BACKEND_URL

    const getUser = async () =>{

        try{
            const { data } = await axios.get(backendUrl+`/users/${userid}`,{ withCredentials: true });
            if(data != null){
                setUser(data)
            }else{
                toast.error(data.message)
            }

        }catch (error) {
            toast.error(error.message)
        }
    }


    const getUserAccount = async () =>{

        try{
            const { data } = await axios.get(backendUrl+`/accounts/user/${userid}/bank/${bank.toUpperCase()}`,{ withCredentials: true });
            if(data != null){
                setAccount(data[0])
            }else{
                toast.error(data.message)
            }

        }catch (error) {
            toast.error(error.message)
        }
    }

    const logoutuser = async () =>{

        try{
            const { data } = await axios.post(backendUrl+"/users/logout",{}, { withCredentials: true });
        }catch (error) {
            toast.error(error.message)
        }
    }

    const getUserBeneficiaries = async (sameBank) =>{
        setSameBank(sameBank);
        try{
            const { data } = await axios.get(backendUrl+`/beneficiaries/${userid}?same=${sameBank}`,{ withCredentials: true });
            if(data != null){
                console.log(data)
                setBeneficiaries(data)
            }else{
                toast.error(data.message)
            }

        }catch (error) {
            toast.error(error.message)
        }
    }

    const deleteBeneficiaries = async (id) =>{
        try{
            const { data } = await axios.delete(backendUrl+`/beneficiaries/delete/${id}`,{ withCredentials: true });
            if(data.success){
                toast.success("Beneficiary Deleted Successfully")
                getUserBeneficiaries(sameBank)
            }else{
                toast.error(data.message)
            }

        }catch (error) {
            toast.error(error.message)
        }
    }

    const handleUpdateBeneficiary = async (beneficiaryId,amount) =>{
        try{
            const { data } = await axios.put(backendUrl+`/beneficiaries/update`,{beneficiaryId,amount},{ withCredentials: true });
            if(data.success){
                toast.success(data.message)
                getUserBeneficiaries(sameBank)
            }else{
                toast.error(data.message)
            }

        }catch (error) {
            toast.error(error.message)
        }
    }


    const sendOTP = async () => {
        if (!newBeneficiary.accountNumber || !newBeneficiary.confirmAccount || !newBeneficiary.ifscCode || !newBeneficiary.amount || !newBeneficiary.name) {
            console.error("newBeneficiary is missing required fields:", newBeneficiary);
            toast.error("Please fill in all required fields.");
            return;
        }
        if (String(newBeneficiary.accountNumber) !== String(newBeneficiary.confirmAccount)) {
            toast.error("Account Numbers does not match.");
            return false;
        }
        else{
            try {
                await axios.post(backendUrl + '/users/sendotp', { email }, { withCredentials: true });
                toast.success(`OTP sent to ${email}. Check your inbox.`);
                return true; 
            } catch (error) {
                toast.error("Failed to send OTP. Try again.");
                console.error("OTP Send Error:", error);
                return false; 
            }
        }
    };


    const verifyOTP = async (otp) =>{
        try {
            const purpose = "verification"
            const {data} = await axios.post(backendUrl + '/users/verifyotp', { email, otp, purpose },{withCredentials: true});
      
            if (data.otpVerified) {
                  addBeneficiary();
                  toast.success("OTP verified successfully!");
            }else{
              toast.error("Invalid OTP. Please try again.");
            }
          } catch (error) {
            toast.error(error.response?.data?.error);
          }
    }


    const addBeneficiary = async () =>{
        try{
            const { data } = await axios.post(backendUrl+`/beneficiaries/add`,newBeneficiary,{ withCredentials: true });
            if(data.success){
                window.location.reload()
                toast.success(data.message)
            }else{
                toast.error(data.message)
            }

        }catch (error) {
            toast.error(error.message)
        }
    }


    const getUserTransacionBeneficiaries = async (sameBank) =>{
        try{
            const { data } = await axios.get(backendUrl+`/beneficiaries/transaction/${userid}?same=${sameBank}`,{ withCredentials: true });
            if(data != null){
                setTransactionBeneficiaries(data)
            }else{
                toast.error(data.message)
            }

        }catch (error) {
            toast.error(error.message)
        }
    }

    const getUserTransacions = async () =>{
        try{
            const { data } = await axios.get(backendUrl+`/transaction/get/${userid}`,{ withCredentials: true });
            if(data != null){
                setTransactions(data)
            }else{
                toast.error(data.message)
            }

        }catch (error) {
            toast.error(error.message)
        }
    }


    const getTransacionsConfirmation = async () =>{
        try{
            const { data } = await axios.get(backendUrl+`/beneficiaries/compare/${selectedBeneficiaryID}`,{ withCredentials: true });
            if(data != null){
                setConfirmationData(data)
            }else{
                toast.error(data.message)
            }

        }catch (error) {
            toast.error(error.message)
        }
    }

    const sendTOTP = async () => {
            try {
                await axios.post(backendUrl + '/users/sendotp', { email }, { withCredentials: true });
                toast.success(`OTP sent to ${email}. Check your inbox.`);
                return true; 
            } catch (error) {
                toast.error("Failed to send OTP. Try again.");
                console.error("OTP Send Error:", error);
                return false; 
            }
    };

    const verifyTOTP = async (otp) =>{
        try {
            const purpose = "verification"
            const {data} = await axios.post(backendUrl + '/users/verifyotp', { email, otp, purpose },{withCredentials: true});
      
            if (data.otpVerified) {
                  addTransaction();
                  toast.success("OTP verified successfully!");
            }else{
              toast.error("Invalid OTP. Please try again.");
            }
          } catch (error) {
            toast.error(error.response?.data?.error);
          }
    }


    const addTransaction = async () =>{
        try{

            const transactionData = {
                selectedBeneficiaryID,
                receiverAcc,
                amount: Number(amount), 
                ifscCodeUser,
                description
            };

            const { data } = await axios.post(backendUrl+`/transaction/add`,transactionData,{ withCredentials: true });
            if(data.status){
                toast.success(data.message)
                setTimeout(() => {
                    navigate("/user-dashboard");}, 2000); 
            }else{
                toast.error(data.message)
            }

        }catch (error) {
            toast.error(error.message)
        }
    }



    const value ={
        uToken,setUToken,
        vToken,setVToken,
        user,getUser,
        backendUrl,
        logoutuser,
        account,getUserAccount,
        beneficiaries,getUserBeneficiaries,
        deleteBeneficiaries,
        handleUpdateBeneficiary,
        sendOTP,
        verifyOTP,
        setNewBeneficiary,
        newBeneficiary,
        getUserTransacions,
        transactions,
        getUserTransacionBeneficiaries,
        transactionBeneficiaries,
        amount, setAmount,
        description, setDescription,
        selectedBeneficiaryID, setSelectedBeneficiaryID,
        getTransacionsConfirmation, confirmationData,
        sendTOTP,verifyTOTP,
    }

    return (
        <UserContext.Provider value={value}>
            {props.children}
        </UserContext.Provider>
    )

}

export default UserContextProvider