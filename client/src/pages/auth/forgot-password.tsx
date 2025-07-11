import React, { useState } from 'react';
import { Mail } from 'lucide-react';
import { Link } from 'wouter';

const ForgotPassword: React.FC = () => {
  const [email, setEmail] = useState('');

  const handleSubmit = () => {
    // Handle form submission logic here
    console.log('Password reset requested for:', email);
  };

  return (
    <div className="min-h-screen bg-slate-800 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="bg-slate-800 rounded-lg p-8 border border-white/20">
          <div className="text-center mb-8">
            <h1 className="text-2xl font-semibold text-white mb-3">
              Forgot Password
            </h1>
            <p className="text-muted/80 text-sm leading-relaxed">
              Enter your email address and we'll send you<br />
              instructions to reset your password.
            </p>
          </div>

          <div className="space-y-6">
            <div className="relative">
              <Mail className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500 h-5 w-5" />
              <input
                type="email"
                placeholder="Enter your email address"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full pl-12 pr-4 py-3 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all duration-200"
              />
            </div>

            <button
              onClick={handleSubmit}
              className="w-full py-3 px-4 bg-white text-black font-medium rounded-lg hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-white focus:ring-offset-2 focus:ring-offset-gray-900 transition-all duration-200"
            >
              Send Reset Instructions
            </button>
          </div>

          <div className="mt-6 text-center">
            <p className="text-muted/80 text-sm">
              Already have an account?{' '}
              <Link
                href="/login"
                className="text-white underline hover:text-gray-300 transition-colors duration-200"
              >
                Log in
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;