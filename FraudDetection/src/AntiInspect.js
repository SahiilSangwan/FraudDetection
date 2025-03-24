import { useEffect } from "react";

const AntiInspect = () => {
  useEffect(() => {

    // ✅ 2. Disable Common DevTools Shortcuts
    document.addEventListener("keydown", (event) => {
      if (
        event.key === "F12" || // Block F12
        (event.ctrlKey && event.shiftKey && event.key === "I") || // Block Ctrl+Shift+I
        (event.ctrlKey && event.shiftKey && event.key === "J") || // Block Ctrl+Shift+J
        (event.ctrlKey && event.key === "U") // Block Ctrl+U
      ) {
        event.preventDefault();
        alert("Functionality disabled!");
      }
    });

    // ✅ 3. Detect DevTools Open (Debugger Trick)
    const detectDevTools = () => {
      let threshold = 100;
      let before = new Date();
      debugger;
      let after = new Date();
      if (after - before > threshold) {
        alert("DevTools detected! Closing the page...");
        window.location.href = "about:blank"; // Redirect to a blank page
      }
    };
    setInterval(detectDevTools, 2000); // Check every 2 seconds

    // ✅ 4. Overwrite Console Functions
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
