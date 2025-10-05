import React, { useState, useEffect } from 'react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Switch } from '@/components/ui/switch';
import { Label } from '@/components/ui/label';
import { Check } from 'lucide-react';
import { HeroHeader } from '@/pages/landing-page/hero-header';
import { navigate } from 'wouter/use-browser-location';
import useApi from '@/hooks/use-api';
import { useAuth } from '@/contexts/AuthContext';
import { Link } from 'wouter';

interface FeatureItemProps {
    text: string;
}

const FeatureItem: React.FC<FeatureItemProps> = ({ text }) => (
    <div className="flex items-center space-x-2">
        <Check className="h-4 w-4 text-green-500" />
        <span className="text-gray-300 text-sm">{text}</span>
    </div>
);

interface PricingCardProps {
    title: string;
    description: string;
    monthlyPrice: string;
    yearlyPrice: string; // Added for the annual price display
    features: string[];
    buttonText: string;
    isAnnual: boolean; // Prop to determine which price to show
    onClick?: () => Promise<void> // function to call when the Subscribe button is clicked
}

const PricingCard: React.FC<PricingCardProps> = ({
    title,
    description,
    monthlyPrice,
    yearlyPrice,
    features,
    buttonText,
    isAnnual,
    onClick,
}) => (
    <Card className="flex flex-col bg-gray-900 border border-gray-800 p-6 shadow-lg rounded-lg">
        <CardHeader className="p-0 mb-6">
            <CardTitle className="text-2xl font-bold text-white mb-2">{title}</CardTitle>
            <CardDescription className="text-gray-400 text-base">{description}</CardDescription>
        </CardHeader>
        <CardContent className="flex flex-col flex-grow p-0">
            <div className="text-4xl font-bold text-white mb-6">
                ${isAnnual ? yearlyPrice : monthlyPrice}
                <span className="text-lg font-normal text-gray-400">/{isAnnual ? "year" : "month"}</span>
            </div>
            <div className="space-y-3 mb-8 flex-grow">
                {features.map((feature, index) => (
                    <FeatureItem key={index} text={feature} />
                ))}
            </div>
            <Button className="w-full bg-white text-gray-900 hover:bg-gray-200 font-semibold py-2 rounded-md transition-colors duration-200" onClick={onClick ? onClick : () => { }}>
                {buttonText}
            </Button>
        </CardContent>
    </Card>
);

export const PricingPage: React.FC = () => {
    const [isAnnual, setIsAnnual] = useState(false);
    const { apiCall } = useApi();
    const { isAuthenticated } = useAuth();

    // generates a checkout session URL for the user and redirects to Stripe Checkout
    async function generateStripeCheckout() {
        if (!isAuthenticated) {
            navigate("/login");
            return;
        }
        console.log("Generating checkout");
        const response = await apiCall("/api/payments/initiate-checkout?annual=" + isAnnual);
        if (response && response.ok && response.url) {
            window.location.href = response.url;
        } else {
            alert("Error generating checkout");
        }
    }

    async function onCheckoutSuccess() {
        const response = await apiCall("/api/payments/checkout/success", {
            method: 'GET',
        });
        return await response.json();
        // data.ok ? navigate("/dashboard") : alert("Error generating checkout");
        // navigate("/");
    }

    // Remove premature success check on mount; success will be handled on the success page after redirect

    return (
        <div className="bg-slate-800 text-white">
            <HeroHeader />
            <div className="max-w-6xl mx-auto py-12 px-4 sm:px-6 lg:px-8 pt-32">
                {/* Header Section */}
                <div className="flex flex-col sm:flex-row justify-between items-center mb-16">
                    <h1 className="text-4xl mb-6 sm:mb-0 font-lora">Choose Your Plan</h1>
                    <div className="flex items-center space-x-4">
                        <Label htmlFor="pricing-toggle" className="text-gray-300 font-medium">Monthly</Label>
                        <Switch
                            id="pricing-toggle"
                            checked={isAnnual}
                            onCheckedChange={setIsAnnual}
                            className="data-[state=checked]:bg-white data-[state=unchecked]:bg-gray-700"
                        //thumbClassName="data-[state=checked]:translate-x-6 data-[state=unchecked]:translate-x-0 bg-gray-900"
                        />
                        <Label htmlFor="pricing-toggle" className="text-gray-300 font-medium">Yearly</Label>
                    </div>
                </div>

                {/* Pricing Cards Section */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-24">
                    <PricingCard
                        title="Free"
                        description="Get started with a free account"
                        monthlyPrice="0.00"
                        yearlyPrice="0.00" // Example annual price
                        features={[
                            "5 new assessments per month",
                            "Invite and keep track of as many candidates as you want",
                            "Track candidate submissions",
                            "Support from our team"
                        ]}
                        buttonText="Get Started"
                        isAnnual={isAnnual}
                    />
                    <PricingCard
                        title="Pro"
                        description="Advanced features for professionals"
                        monthlyPrice="14.99"
                        yearlyPrice="149.99" // Example annual price
                        features={[
                            "Everything in Free, plus:",
                            "Unlimited new assessments",
                            "24/7 support"
                        ]}
                        buttonText="Choose Pro"
                        isAnnual={isAnnual}
                        onClick={generateStripeCheckout}
                    />
                    {/* <PricingCard
            title="Enterprise"
            description="Comprehensive solution for teams"
            monthlyPrice="49.99"
            yearlyPrice="499.99" // Example annual price
            features={[
              "Unlimited users",
              "500GB storage",
              "24/7 premium support",
              "Custom integrations",
              "Advanced analytics",
              "API access"
            ]}
            buttonText="Choose Enterprise"
            isAnnual={isAnnual}
          /> */}
                </div>

                {/* Why Choose Our Platform Section */}
                <div className="mt-20">
                    <h2 className="text-3xl text-white text-center p-4 serif-heading">Why Choose Themus?</h2>
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                        <Card className="bg-gray-900 border border-gray-800 p-6 shadow-md text-center">
                            <CardTitle className="text-xl font-bold text-white mb-3">Custom Assessments</CardTitle>
                            <CardDescription className="text-gray-400">
                                Design assessments that are tailored exactly to your needs
                            </CardDescription>
                        </Card>
                        <Card className="bg-gray-900 border border-gray-800 p-6 shadow-md text-center">
                            <CardTitle className="text-xl font-bold text-white mb-3">Better Hiring Decisions</CardTitle>
                            <CardDescription className="text-gray-400">
                                Hire the best candidates that can deliver real value
                            </CardDescription>
                        </Card>
                        <Card className="bg-gray-900 border border-gray-800 p-6 shadow-md text-center">
                            <CardTitle className="text-xl font-bold text-white mb-3">Practical Insights</CardTitle>
                            <CardDescription className="text-gray-400">
                                Stay up to date with your assessments and candidates
                            </CardDescription>
                        </Card>
                    </div>
                </div>
            </div>
        </div>
    );
};

// export const SubscriptionSuccessPage: React.FC = () => {
//     const { apiCall } = useApi();
//     const [isProcessing, setIsProcessing] = useState(true);
//     const [success, setSuccess] = useState(false);

//     // checks if the checkout was successful and redirects to the dashboard
//     async function onCheckoutSuccess() {
//         try {
//             const response = await apiCall("/api/payments/checkout/success", {
//                 method: 'GET',
//             });
//             const data = await response.json();
//             return data;
//         } catch (error) {
//             console.error("Error processing checkout success:", error);
//             return { ok: false, error: "Failed to process checkout success" };
//         }
//     }

//     useEffect(() => {
//         onCheckoutSuccess().then((data) => {
//             setIsProcessing(false);
//             if (data && data.ok) {
//                 setSuccess(true);
//                 // Redirect to dashboard after a short delay
//                 setTimeout(() => {
//                     navigate("/dashboard");
//                 }, 2000);
//             } else {
//                 setSuccess(false);
//                 console.error("Checkout success processing failed:", data);
//             }
//         });
//     }, []);

//     return (
//         <div className="bg-slate-800 text-white">
//             <div className="max-w-6xl mx-auto py-12 px-4 sm:px-6 lg:px-8 pt-32">
//                 {isProcessing ? (
//                     <div>
//                         <h1 className="text-4xl font-bold mb-6 sm:mb-0">Processing Payment...</h1>
//                         <p className="text-gray-400">Please wait while we confirm your subscription.</p>
//                     </div>
//                 ) : success ? (
//                     <div>
//                         <h1 className="text-4xl font-bold mb-6 sm:mb-0">Subscription Success!</h1>
//                         <p className="text-gray-400">You are now a member of the Pro plan.</p>
//                         <p className="text-gray-400">Redirecting to dashboard...</p>
//                     </div>
//                 ) : (
//                     <div>
//                         <h1 className="text-4xl font-bold mb-6 sm:mb-0">Payment Processing Error</h1>
//                         <p className="text-gray-400">There was an issue confirming your payment.</p>
//                         <p className="text-gray-400">Please contact support if this persists.</p>
//                     </div>
//                 )}
//             </div>
//         </div>
//     );
// }

export const SubscriptionSuccessPage: React.FC = () => {
    return (
        <div className="bg-slate-800 text-white min-h-screen flex flex-col items-center justify-center gap-4">
            <h1>Subscription Success</h1>
            <p>Your subscription has been successfully created.</p>
            <Link to="/dashboard"><Button className="bg-gray-800 hover:bg-gray-700 text-white px-8 py-3 rounded-full border border-gray-600 transition-colors flex items-center mx-auto">Go to Dashboard</Button></Link>
        </div>
    );
}