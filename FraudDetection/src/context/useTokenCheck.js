import { useEffect, useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

export const useTokenCheck = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [tokensValid, setTokensValid] = useState(false);

  useEffect(() => {
    const checkTokens = () => {
      const uToken = localStorage.getItem('uToken');
      const vToken = localStorage.getItem('vToken');
      const isAuthRoute = ['/login', '/verification'].includes(location.pathname);

      if (isAuthRoute) {
        setTokensValid(true);
        return;
      }

      if (!uToken || !vToken) {
        setTokensValid(false);
        navigate('/', { replace: true });
      } else {
        setTokensValid(true);
      }
    };

    const handleCustomStorageChange = () => {
      checkTokens();
    };

    const handleStorageChange = (e) => {
      if (e.key === 'uToken' || e.key === 'vToken') {
        checkTokens();
      }
    };

    checkTokens();

    window.addEventListener('storage', handleStorageChange);
    window.addEventListener('customStorageChange', handleCustomStorageChange);

    const pollInterval = setInterval(checkTokens, 1000);

    return () => {
      window.removeEventListener('storage', handleStorageChange);
      window.removeEventListener('customStorageChange', handleCustomStorageChange);
      clearInterval(pollInterval);
    };
  }, [navigate, location.pathname]);

  return tokensValid;
};