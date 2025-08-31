import { Menu, X } from "lucide-react"
import React from "react"
import { Button } from "react-day-picker"
import { Link } from "wouter"

export const AuthPageHeader = () => {
    const [menuState, setMenuState] = React.useState(false)
    return (
        <header>
            <nav
                data-state={menuState && 'active'}
                className="group fixed z-20 w-full border-b border-white/20 bg-slate-800/50 backdrop-blur-3xl">
                <div className="mx-auto max-w-6xl px-6 transition-all duration-300">
                    <div className="relative flex flex-wrap items-center justify-between gap-6 py-3 lg:gap-0 lg:py-4">
                        <div className="flex w-full items-center justify-between gap-12 lg:w-auto">
                            <Link
                                href="/"
                                aria-label="home"
                                className="flex items-center space-x-2">
                                {/* <Logo /> */}
                                <span className="text-white text-xl pr-16 font-lora">Themus</span>
                            </Link>
                        </div>
                    </div>
                </div>
            </nav>
        </header>
    )
}