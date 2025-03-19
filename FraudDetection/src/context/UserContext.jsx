import { createContext, useState } from "react";
import axios from "axios";
import { toast } from 'react-toastify'

export const UserContext = createContext()

const UserContextProvider = (props) => {

    const storedUser = JSON.parse(localStorage.getItem("user"));
    const userid = storedUser?.userId || "";

    const [uToken,setUToken] = useState(localStorage.getItem('uToken')?localStorage.getItem('uToken'):'')
    const [vToken,setVToken] = useState(localStorage.getItem('vToken')?localStorage.getItem('vToken'):'')
    const [user,setUser] = useState({})
    const [account,setAccount] = useState({})
    const [userBeneficiaries,setUserBeneficiaries] = useState({})

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
        try{
            const { data } = await axios.get(backendUrl+`/beneficiaries/${userid}?same=${sameBank}`,{ withCredentials: true });
            if(data != null){
                setUserBeneficiaries(data)
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
        userBeneficiaries,getUserBeneficiaries,
    }

    return (
        <UserContext.Provider value={value}>
            {props.children}
        </UserContext.Provider>
    )

}

export default UserContextProvider