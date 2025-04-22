import { createContext, useState } from "react";
import axios from "axios";
import { toast } from 'react-toastify'
import { useNavigate } from "react-router-dom";
import { useTokenCheck } from "./useTokenCheck";

export const UserContext = createContext()

const UserContextProvider = (props) => {

    const navigate = useNavigate(); 

    const storedUser = JSON.parse(localStorage.getItem("user"));
    const userid = storedUser?.userId || "";
    const email = storedUser?.email || "";

    const [uToken,setUToken] = useState(localStorage.getItem('uToken')?localStorage.getItem('uToken'):'')
    const [vToken,setVToken] = useState(localStorage.getItem('vToken')?localStorage.getItem('vToken'):'')
    useTokenCheck();
    const [user,setUser] = useState({})
    const [account,setAccount] = useState({})
    const [beneficiaries,setBeneficiaries] = useState([])
    const [transactionBeneficiaries,setTransactionBeneficiaries] = useState([])
    const [transactions,setTransactions] = useState([])
    const [sameBank,setSameBank] = useState(false)
    const [mpinLimitAmount,setMpinLimitAmount] = useState(0)

    // Transaction Page
    const [description, setDescription] = useState("");
    const [amount, setAmount] = useState("");
    const [selectedBeneficiaryID, setSelectedBeneficiaryID] = useState("");
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
            localStorage.removeItem('lastTriggeredTime')
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
              handleFailedAttempt()
            }
          } catch (error) {
            toast.error(error.response?.data?.error);
          }
    }

    const [otpAttempt, setOtpAttempt] = useState(0); 

    const handleFailedAttempt = () => {
    setOtpAttempt(prev => prev + 1);
    const pur = "beneficiary";
    if (otpAttempt + 1 >= 2 && otpAttempt + 1 < 3 ) {
        toast.warning("You have made 2 or more incorrect attempts."); 
        sendWarning(pur);
    } else if (otpAttempt + 1 > 3) {
        toast.error("You have made too many incorrect attempts. Logging out.....");
        uToken && setUToken('')
        uToken && localStorage.removeItem('uToken')
        vToken && setVToken('')
        vToken && localStorage.removeItem('vToken')
        localStorage.removeItem('user')     
        logoutuser();
        setOtpAttempt(0);
        navigate("/")
    }
    };


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


    const getTransacionsConfirmation = async (selectedBeneficiaryID) =>{
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

    const verifyTOTP = async (otp, selectedBeneficiaryID, receiverAcc, amount, ifscCodeUser, description) =>{

        try {
            const purpose = "verification"
            const {data} = await axios.post(backendUrl + '/users/verifyotp', { email, otp, purpose },{withCredentials: true});
      
            if (data.otpVerified) {
                //   addTransaction();
                  toast.success("OTP verified successfully!");


                        const { data } = await axios.post(backendUrl+`/transaction/add`,{selectedBeneficiaryID: Number(selectedBeneficiaryID), receiverAcc, amount: Number(amount), ifscCodeUser, description},{ withCredentials: true });
                            if(data.status){
                                console.log(data)
                                toast.success(data.message)
                                setTimeout(() => {
                                    navigate("/user-dashboard");}, 2000); 
                            }else{
                                toast.error(data.message)
                                handleTFailedAttempt();
                            }


            }else{
              toast.error("Invalid OTP. Please try again.");
            }
          } catch (error) {
            toast.error(error.response?.data?.error);
          }
    }


    const getBankTheme = (bank) => {
        const themes = {
          sbi: {
            background: "bg-blue-50",
            header: "bg-gradient-to-r from-blue-600 to-blue-800",
            button: "bg-gradient-to-r from-blue-600 to-blue-700",
            buttonSecondary: "bg-blue-100 text-blue-600 hover:bg-blue-200",
            focus: "focus:ring-blue-500 focus:border-blue-500",
            link: "text-blue-600 hover:text-blue-800",
            border: "border-t-4 border-blue-600"
          },
          hdfc: {
            background: "bg-red-50",
            header: "bg-gradient-to-r from-red-500 to-red-700",
            button: "bg-gradient-to-r from-red-500 to-red-700",
            buttonSecondary: "bg-red-100 text-red-600 hover:bg-red-200",
            focus: "focus:ring-red-500 focus:border-red-500",
            link: "text-red-600 hover:text-red-800",
            border: "border-t-4 border-red-600"
          },
          icici: {
            background: "bg-orange-50",
            header: "bg-gradient-to-r from-orange-500 to-orange-700",
            button: "bg-gradient-to-r from-orange-400 to-orange-600",
            buttonSecondary: "bg-orange-100 text-orange-600 hover:bg-orange-200",
            focus: "focus:ring-orange-500 focus:border-orange-500",
            link: "text-orange-600 hover:text-orange-800",
            border: "border-t-4 border-orange-600"
          }
        };
        return themes[bank] || themes.sbi; // Default to SBI theme
      };


      const sendWarning = async (purpose) => {
        try {
            const { data } = await axios.get(backendUrl + `/alert/warning/${purpose}`, { withCredentials: true });        
            return true; 
        } catch (error) {
            return false; 
        }
    };

    const blockUser = async (reason) => {
        try {
            const { data } = await axios.post(backendUrl+"/alert/block", {reason}, {withCredentials: true });
            uToken && setUToken('')
            uToken && localStorage.removeItem('uToken')
            vToken && setVToken('')
            vToken && localStorage.removeItem('vToken')
            localStorage.removeItem('user')     
            logoutuser();
            navigate("/")
            return true; 
        } catch (error) {
            return false; 
        }
    };

    const verifyPinOTP = async (otp, newMpin) => {
        try {
          const purpose = "pin change";
          // First verify OTP
          const otpResponse = await axios.post(backendUrl + '/users/verifyotp', { email, otp, purpose }, { withCredentials: true });
      
          if (!otpResponse.data.otpVerified) {
            toast.error("Invalid OTP. Please try again.");
            return false;
          }
      
          toast.success("OTP verified successfully!");
      
          // Then update MPIN
          const updateResponse = await axios.put(backendUrl+`/users/update-mpin`,{email, newMpin},{ withCredentials: true });
      
          if (!updateResponse.data.success) {
            toast.error(updateResponse.data.message);
            return false;
          }
      
          toast.success(updateResponse.data.message);
          return true;
      
        } catch (error) {
          const errorMessage = error.response?.data?.error || "Something went wrong";
          toast.error(errorMessage);
          return false;
        }
      };


    const verifyLimitOTP = async (otp, mpinAmount) => {
        try {
          const purpose = "limit change";
          
          // First verify OTP
          const otpResponse = await axios.post(
            backendUrl + '/users/verifyotp', 
            { email, otp, purpose },
            { withCredentials: true }
          );
      
          if (!otpResponse.data.otpVerified) {
            toast.error("Invalid OTP. Please try again.");
            return false;
          }
      
          toast.success("OTP verified successfully!");
      
          // Then update transaction limit
          const limitResponse = await axios.put(
            backendUrl + '/users/update-mpin-amount',
            { email, mpinAmount },
            { withCredentials: true }
          );
          console.log(limitResponse.data);
      
          if (!limitResponse.data.success) {
            toast.error(limitResponse.data.message || "Failed to update limit");
            return false;
          }
      
          return true;
      
        } catch (error) {
          const errorMessage = error.response?.data?.error || "Something went wrong";
          toast.error(errorMessage);
          return false;
        }
      };


      const mpinAmount = async () => {
        try {
            const {data} = await axios.post(backendUrl + '/users/get-mpin-amount', { email }, { withCredentials: true });
            setMpinLimitAmount(data)
        } catch (error) {
            toast.error("Something went wrong");
            return false; 
        }
    };


    const verifyMpinOtp = async (mpin, otp, selectedBeneficiaryID, receiverAcc, amount, ifscCodeUser, description) => {
        const eMpin = encryption(mpin);
        const eOtp = encryption(otp);
        const eEmail = encryption(email);
        const eSelectedBeneficiaryID = encryption(selectedBeneficiaryID.toString());
        const eReceiverAcc = encryption(receiverAcc);
        const eAmount = encryption(amount.toString());
        const eIfscCodeUser = encryption(ifscCodeUser);
        console.log("Encrypted values:", {
            selectedBeneficiaryID: eSelectedBeneficiaryID,
            receiverAccount: eReceiverAcc,
            amount: eAmount,
            ifscCode: eIfscCodeUser,
            mpin: eMpin,
            otp: eOtp,
            email: eEmail
        });

        try {
            // First verify MPIN and OTP
            const { data } = await axios.post(
                backendUrl + '/users/verify-mpin-otp', 
                { eEmail, eOtp, eMpin }, 
                { withCredentials: true }
            );
    
            if (!data.success) {
                toast.error(data.message || "MPIN/OTP verification failed");
                await handleTFailedAttempt(); // Make sure to await
                return false;
            }
    
            toast.success("MPIN and OTP verified successfully!");
            
            const transactionResponse = await axios.post(
                backendUrl + '/transaction/add',
                {
                    totpAttempt, // Current attempt count
                    eSelectedBeneficiaryID,
                    eReceiverAcc,
                    eAmount,
                    eIfscCodeUser,
                    description,
                    verificationMethod: 'mpin_otp',
                    verifiedAt: new Date().toISOString()
                },
                { withCredentials: true }
            );

            const count = totpAttempt;
            setTOtpAttempt(prev => {
                if (prev !== 0) {
                    console.log("Resetting TOTP attempts from", prev, "to 0");
                    return 0;
                }
                return prev;
            });
            
    
            if (!transactionResponse.data.status) {
                toast.error(transactionResponse.data.message || "Transaction failed");
                return false;
            }

            if(count == 3){
                toast.warning("Transaction was marked as suspicious. Please verify.");
                return true;
            }
    
            toast.success("Transaction completed successfully!");
            return true;
    
        } catch (error) {
            const errorMessage = error.response?.data?.error || 
                               error.response?.data?.message || 
                               "Transaction processing failed";
            toast.error(errorMessage);
            return false;
        }
    };

    const [totpAttempt, setTOtpAttempt] = useState(0);
    
    const handleTFailedAttempt = async () => {
        // Use functional update and await the state change
        await new Promise(resolve => {
            setTOtpAttempt(prev => {
                const newAttempts = prev + 1;
                console.log("Incrementing attempts to", newAttempts);
                
                // Immediate security actions
                if (newAttempts === 2) {
                    toast.warning("You've entered incorrect credentials twice.");
                } 
                else if (newAttempts === 3) {
                    toast.warning("Final attempt! Account will be locked after another failure.");
                    sendWarning("transaction").catch(console.error);
                } 
                else if (newAttempts >= 4) {
                    toast.error("Account locked due to multiple failed attempts.");
                    blockUser("Exceeded maximum OTP attempts (4)").catch(console.error);
                    setTimeout(() => navigate("/", { replace: true }), 2000);
                    resolve(0); // Return 0 immediately for this case
                    return 0;
                }
                
                resolve(newAttempts);
                return newAttempts;
            });
        });
    };
    
    const verifyMpin = async (mpin, selectedBeneficiaryID, receiverAcc, amount, ifscCodeUser, description) => {
        const eMpin = encryption(mpin);
        const eEmail = encryption(email);
        const eSelectedBeneficiaryID = encryption(selectedBeneficiaryID.toString());
        const eReceiverAcc = encryption(receiverAcc);
        const eAmount = encryption(amount.toString());
        const eIfscCodeUser = encryption(ifscCodeUser);
        console.log("Encrypted values:", {
            selectedBeneficiaryID: eSelectedBeneficiaryID,
            receiverAccount: eReceiverAcc,
            amount: eAmount,
            ifscCode: eIfscCodeUser,
            mpin: eMpin,
            email: eEmail
        });

        try {
            // First verify MPIN
            const { data } = await axios.post(
                backendUrl + '/users/verify-mpin', 
                { eEmail, eMpin }, 
                { withCredentials: true }
            );

            console.log(data)
    
            if (!data.success) {
                toast.error(data.message || "Invalid MPIN");
                handleTFailedAttempt();
                return false;
            }
    
            toast.success("MPIN verified successfully!");

            const transactionResponse = await axios.post(
                backendUrl + '/transaction/add',
                {
                    totpAttempt,
                    eSelectedBeneficiaryID,
                    eReceiverAcc,
                    eAmount,
                    eIfscCodeUser,
                    description,
                    verificationMethod: 'mpin_only',
                    verifiedAt: new Date().toISOString()
                },
                { withCredentials: true }
            );

            const count = totpAttempt;
            // RESET ATTEMPTS - Use functional update to ensure correctness
            setTOtpAttempt(prev => {
                if (prev !== 0) {
                    console.log("Resetting TOTP attempts from", prev, "to 0");
                    return 0;
                }
                return prev;
            });
            
    
            if (!transactionResponse.data.status) {
                toast.error(transactionResponse.data.message || "Transaction failed");
                return false;
            }
        
            if(count == 3){
                toast.warning("Transaction was marked as suspicious. Please verify.");
                return true;
            }
            toast.success("Transaction completed successfully!");
            return true;
    
        } catch (error) {
            const errorMessage = error.response?.data?.error || 
                               error.response?.data?.message || 
                               "Transaction processing failed";
            toast.error(errorMessage);
            return false;
        }
    };


    function encryption(input) {
        
        if (!input || typeof input !== "string" || input.trim() === "") {
            throw new Error("Input must be a non-empty string.");
        }

        let encrypted = input;

        for (let round = 1; round <= 3; round++) {
            // Step 1: Shift each character by its index + 1
            let shifted = '';
            for (let i = 0; i < encrypted.length; i++) {
                let charCode = encrypted.charCodeAt(i);
                charCode = (charCode + (i + 1)) % 65536; // Wrap around UTF-16 range
                shifted += String.fromCharCode(charCode);
            }
            encrypted = shifted;
            
            // Step 2: Double the string
            encrypted = encrypted + encrypted;
        }

        return encrypted;
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
        sendTOTP,verifyTOTP,getBankTheme,
        sendWarning,blockUser,verifyPinOTP,
        verifyLimitOTP, mpinAmount,mpinLimitAmount,
        setMpinLimitAmount,verifyMpin,verifyMpinOtp,
        encryption,
    }

    return (
        <UserContext.Provider value={value}>
            {props.children}
        </UserContext.Provider>
    )

}

export default UserContextProvider