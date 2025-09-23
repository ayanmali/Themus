import React from 'react';
import { CheckCircle, Clock, Code, ArrowLeft } from 'lucide-react';
import { useParams, useLocation } from 'wouter';
import { Button } from '@/components/ui/button';

export default function AssessmentSubmissionConfirmation() {
    const params = useParams();
    const [, navigate] = useLocation();
    const assessmentId = params.assessment_id;

    const handleGoBack = () => {
        // Navigate back to the assessment preview or a general page
        navigate('/');
    };

    return (
        <div className="min-h-screen bg-gray-900 text-gray-100">
            {/* Header */}
            <div className="bg-gray-800 border-b border-gray-700">
                <div className="max-w-7xl mx-auto py-4">
                    <div className="flex items-center justify-between">
                        <div>
                            <h1 className="text-2xl font-bold text-white">Assessment Submitted</h1>
                            <p className="text-gray-400 mt-1">Thank you for completing the assessment</p>
                        </div>
                        <div className="flex items-center space-x-4">
                            <div className="flex items-center space-x-2 bg-green-900/30 px-3 py-1 rounded-full">
                                <CheckCircle className="h-4 w-4 text-green-400" />
                                <span className="text-sm font-medium text-green-300">Successfully Submitted</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div className="max-w-4xl mx-auto py-12 px-4">
                <div className="text-center">
                    {/* Success Icon */}
                    <div className="mx-auto w-24 h-24 bg-green-900/30 rounded-full flex items-center justify-center mb-8">
                        <CheckCircle className="w-12 h-12 text-green-400" />
                    </div>

                    {/* Main Message */}
                    <h2 className="text-3xl font-bold text-white mb-4">
                        Assessment Successfully Submitted!
                    </h2>
                    <p className="text-xl text-gray-300 mb-8 max-w-2xl mx-auto">
                        Your assessment has been submitted and is now being reviewed. 
                        You will be notified once the evaluation is complete.
                    </p>

                    {/* Details Card */}
                    <div className="bg-gray-800 rounded-lg p-8 border border-gray-700 max-w-2xl mx-auto mb-8">
                        <h3 className="text-xl font-semibold text-white mb-6">Submission Details</h3>
                        <div className="space-y-4">
                            <div className="flex items-center justify-between py-3 border-b border-gray-700">
                                <div className="flex items-center space-x-3">
                                    <CheckCircle className="h-5 w-5 text-green-400" />
                                    <span className="text-gray-300">Assessment Status</span>
                                </div>
                                <span className="text-green-400 font-medium">Completed</span>
                            </div>
                            
                            <div className="flex items-center justify-between py-3 border-b border-gray-700">
                                <div className="flex items-center space-x-3">
                                    <Clock className="h-5 w-5 text-blue-400" />
                                    <span className="text-gray-300">Submitted At</span>
                                </div>
                                <span className="text-gray-300">
                                    {new Date().toLocaleString()}
                                </span>
                            </div>
                            
                            <div className="flex items-center justify-between py-3">
                                <div className="flex items-center space-x-3">
                                    <Code className="h-5 w-5 text-purple-400" />
                                    <span className="text-gray-300">Assessment ID</span>
                                </div>
                                <span className="text-gray-300 font-mono">
                                    {assessmentId || 'N/A'}
                                </span>
                            </div>
                        </div>
                    </div>

                    {/* Next Steps */}
                    <div className="bg-blue-900/20 border border-blue-700/50 rounded-lg p-6 max-w-2xl mx-auto mb-8">
                        <h3 className="text-lg font-semibold text-blue-300 mb-3">What happens next?</h3>
                        <ul className="text-left text-gray-300 space-y-2">
                            <li className="flex items-start space-x-2">
                                <span className="text-blue-400 mt-1">•</span>
                                <span>Your submission will be reviewed by our technical team</span>
                            </li>
                            <li className="flex items-start space-x-2">
                                <span className="text-blue-400 mt-1">•</span>
                                <span>You will receive feedback on your performance</span>
                            </li>
                            <li className="flex items-start space-x-2">
                                <span className="text-blue-400 mt-1">•</span>
                                <span>The employer will be notified of your completion</span>
                            </li>
                        </ul>
                    </div>

                    {/* Action Button */}
                    <div className="flex justify-center">
                        <Button 
                            onClick={handleGoBack}
                            className="bg-blue-600 hover:bg-blue-700 text-white px-8 py-3 rounded-lg transition-colors flex items-center space-x-2"
                        >
                            <ArrowLeft className="h-4 w-4" />
                            <span>Return to Home</span>
                        </Button>
                    </div>
                </div>
            </div>
        </div>
    );
}



