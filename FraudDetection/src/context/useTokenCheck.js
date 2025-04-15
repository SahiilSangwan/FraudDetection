// import { useEffect } from 'react';
// import { useNavigate, useLocation } from 'react-router-dom';

// export const useTokenCheck = () => {
//   const navigate = useNavigate();
//   const location = useLocation();

//   useEffect(() => {
//     const checkTokens = () => {
//       const uToken = localStorage.getItem('uToken');
//       const vToken = localStorage.getItem('vToken');
//       const isAuthRoute = ['/login', '/verification'].includes(location.pathname);

//       // Skip check for authentication routes
//       if (isAuthRoute) return;

//       // Redirect if either token is missing
//       if (!uToken || !vToken) {
//         navigate('/', { replace: true });
//       }
//     };

//     // Initial check
//     checkTokens();

//     // Storage event listener for cross-tab sync
//     const handleStorageChange = (e) => {
//       if (e.key === 'uToken' || e.key === 'vToken') {
//         checkTokens();
//       }
//     };

//     window.addEventListener('storage', handleStorageChange);
//     return () => window.removeEventListener('storage', handleStorageChange);
//   }, [navigate, location.pathname]);
// };




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

    // Create a custom event listener for same-tab changes
    const handleCustomStorageChange = () => {
      checkTokens();
    };

    // Standard storage event for cross-tab changes
    const handleStorageChange = (e) => {
      if (e.key === 'uToken' || e.key === 'vToken') {
        checkTokens();
      }
    };

    // Check immediately
    checkTokens();

    // Set up both listeners
    window.addEventListener('storage', handleStorageChange);
    window.addEventListener('customStorageChange', handleCustomStorageChange);

    // Polling fallback (optional)
    const pollInterval = setInterval(checkTokens, 1000);

    return () => {
      window.removeEventListener('storage', handleStorageChange);
      window.removeEventListener('customStorageChange', handleCustomStorageChange);
      clearInterval(pollInterval);
    };
  }, [navigate, location.pathname]);

  return tokensValid;
};