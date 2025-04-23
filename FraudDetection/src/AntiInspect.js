import { useEffect } from "react";

const AntiInspect = () => {
  useEffect(() => {

    document.addEventListener("keydown", (event) => {
      if (
        event.key === "F12" || 
        (event.ctrlKey && event.shiftKey && event.key === "I") || 
        (event.ctrlKey && event.shiftKey && event.key === "J") || 
        (event.ctrlKey && event.key === "U") 
      ) {
        event.preventDefault();
        alert("Functionality disabled!");
      }
    });

    const detectDevTools = () => {
      let threshold = 100;
      let before = new Date();
      debugger;
      let after = new Date();
      if (after - before > threshold) {
        alert("DevTools detected! Closing the page...");
        window.location.href = "about:blank"; 
      }
    };
    setInterval(detectDevTools, 2000);

    console.log = function () {};
    console.error = function () {};
    console.table = function () {};
    console.warn = function () {};

    return () => {
      document.removeEventListener("contextmenu", (event) =>
        event.preventDefault()
      );
      document.removeEventListener("keydown", () => {});
    };
  }, []);
};

export default AntiInspect;
