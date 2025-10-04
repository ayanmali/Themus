import { ArrowLeftIcon } from "lucide-react";
import { Link } from "wouter";
import { HeroHeader } from "./landing-page/hero-header";

export default function AboutPage() {
    return (
        // <div className="items-center justify-center h-screen bg-slate-800 text-gray-100 p-20">
        //     <span className="items-start justify-start">
        //         <Link href="/" className="flex items-center gap-2">
        //         <ArrowLeftIcon className="w-4 h-4" />
        //         Back
        //         </Link>
        //     </span>
        //     <div className=" items-center justify-center flex flex-col h-screen bg-slate-800 text-gray-100">
        //         <h1 className="text-4xl font-bold serif-heading">About</h1>
        //         <p>
        //             Coming soon...
        //         </p>
        //     </div>
        // </div>
        <div className="bg-slate-800 text-gray-100">
            <HeroHeader />
            <div className="max-w-6xl mx-auto py-12 px-4 sm:px-6 lg:px-8 pt-32 h-screen">
                <h1 className="text-4xl font-bold serif-heading">About</h1>
                <p>
                    Coming soon...
                </p>
            </div>
        </div>
    )
}