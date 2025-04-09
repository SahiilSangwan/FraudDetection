import { useEffect } from 'react';
import { FiActivity, FiShield, FiAlertTriangle, FiTrendingUp, FiBarChart2 } from 'react-icons/fi';

const Landing = () => {
  useEffect(() => {
    // This would be where you'd initialize any animations in a real implementation
    console.log("Secure Pulse Admin Panel loaded");
  }, []);

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-900 to-gray-800 text-white">
      {/* Hero Section */}
      <header className="relative overflow-hidden">
        <div className="absolute inset-0 bg-black opacity-50"></div>
        <video 
          autoPlay 
          loop 
          muted 
          playsInline
          className="absolute inset-0 w-full h-full object-cover"
          poster="https://images.unsplash.com/photo-1639762681057-408e52192e55?q=80&w=2232&auto=format&fit=crop"
        >
          <source src="https://assets.mixkit.co/videos/preview/mixkit-digital-animation-of-a-data-stream-1318-large.mp4" type="video/mp4" />
        </video>
        
        <div className="relative z-10 max-w-7xl mx-auto px-4 py-24 sm:px-6 lg:px-8 text-center">
          <div className="flex justify-center mb-6">
            <div className="bg-indigo-600/20 p-3 rounded-full backdrop-blur-sm">
              <FiShield className="h-10 w-10 text-indigo-400" />
            </div>
          </div>
          <h1 className="text-4xl md:text-6xl font-bold mb-6">
            Secure<span className="text-indigo-400">Pulse</span>
          </h1>
          <p className="text-xl md:text-2xl text-gray-300 max-w-3xl mx-auto">
            Advanced Transaction Monitoring System for Financial Security
          </p>
        </div>
      </header>

      {/* Features Grid */}
      <section className="py-20 px-4 sm:px-6 lg:px-8 max-w-7xl mx-auto">
        <div className="grid md:grid-cols-3 gap-8">
          {/* Feature 1 */}
          <div className="bg-gray-800/50 p-8 rounded-xl border border-gray-700/50 hover:border-indigo-400/30 transition-all duration-300 hover:scale-[1.02]">
            <div className="bg-indigo-600/10 w-14 h-14 rounded-lg flex items-center justify-center mb-6">
              <FiActivity className="h-6 w-6 text-indigo-400" />
            </div>
            <h3 className="text-xl font-bold mb-3">Real-time Monitoring</h3>
            <p className="text-gray-400">
              Instant detection of suspicious transactions across all banking channels with live alerts.
            </p>
            <div className="mt-6">
              <img 
                src="https://images.unsplash.com/photo-1551288049-bebda4e38f71?q=80&w=2070&auto=format&fit=crop" 
                alt="Real-time analytics"
                className="rounded-lg shadow-xl"
              />
            </div>
          </div>

          {/* Feature 2 */}
          <div className="bg-gray-800/50 p-8 rounded-xl border border-gray-700/50 hover:border-indigo-400/30 transition-all duration-300 hover:scale-[1.02]">
            <div className="bg-indigo-600/10 w-14 h-14 rounded-lg flex items-center justify-center mb-6">
              <FiAlertTriangle className="h-6 w-6 text-indigo-400" />
            </div>
            <h3 className="text-xl font-bold mb-3">Fraud Detection</h3>
            <p className="text-gray-400">
              Pattern recognition to identify and flag potential fraudulent activities.
            </p>
            <div className="mt-6">
              <img 
                src="https://images.unsplash.com/photo-1629909613654-28e377c37b09?q=80&w=2068&auto=format&fit=crop" 
                alt="Fraud detection"
                className="rounded-lg shadow-xl"
              />
            </div>
          </div>

          {/* Feature 3 */}
          <div className="bg-gray-800/50 p-8 rounded-xl border border-gray-700/50 hover:border-indigo-400/30 transition-all duration-300 hover:scale-[1.02]">
            <div className="bg-indigo-600/10 w-14 h-14 rounded-lg flex items-center justify-center mb-6">
              <FiBarChart2 className="h-6 w-6 text-indigo-400" />
            </div>
            <h3 className="text-xl font-bold mb-3">Comprehensive Analytics</h3>
            <p className="text-gray-400">
              Detailed reports and visualizations to understand transaction patterns and risks.
            </p>
            <div className="mt-6">
              <img 
                src="https://images.unsplash.com/photo-1460925895917-afdab827c52f?q=80&w=2015&auto=format&fit=crop" 
                alt="Analytics dashboard"
                className="rounded-lg shadow-xl"
              />
            </div>
          </div>
        </div>
      </section>

      {/* Dashboard Preview */}
      <section className="py-20 bg-gray-800/30">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-16">
            <h2 className="text-3xl font-bold mb-4">Powerful Admin Interface</h2>
            <p className="text-xl text-gray-400 max-w-3xl mx-auto">
              Intuitive controls and comprehensive visibility into all transaction activities
            </p>
          </div>
          
          <div className="relative">
            <div className="absolute -inset-2 bg-indigo-600/20 rounded-xl blur-lg"></div>
            <div className="relative bg-gray-900 rounded-xl overflow-hidden border border-gray-700/50">
              <img 
                src="https://images.unsplash.com/photo-1551288049-bebda4e38f71?q=80&w=2070&auto=format&fit=crop" 
                alt="SecurePulse dashboard"
                className="w-full h-auto"
              />
            </div>
          </div>
        </div>
      </section>

      {/* Footer */}
      <footer className="py-12 border-t border-gray-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <div className="flex justify-center mb-6">
            <div className="bg-indigo-600/20 p-3 rounded-full">
              <FiShield className="h-8 w-8 text-indigo-400" />
            </div>
          </div>
          <h2 className="text-2xl font-bold mb-2">SecurePulse</h2>
          <p className="text-gray-400 mb-6">Transaction Monitoring System</p>
          <p className="text-gray-500 text-sm">
            © {new Date().getFullYear()} SecurePulse | Wissen Technologies. All rights reserved.
          </p>
        </div>
      </footer>
    </div>
  );
};

export default Landing;