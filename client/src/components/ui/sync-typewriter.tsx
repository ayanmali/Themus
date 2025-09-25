// import { useTypewriterSync } from "@/lib/sync-typewriter"
// import { motion, Variants } from "framer-motion"
// import { useEffect, useState, useRef } from "react"

// interface SynchronizedTypewriterProps {
//     id: string
//     text: string[]
//     speed?: number
//     initialDelay?: number
//     waitTime?: number
//     deleteSpeed?: number
//     className?: string
//     showCursor?: boolean
//     hideCursorOnType?: boolean
//     cursorChar?: string | React.ReactNode
//     cursorClassName?: string
//     cursorAnimationVariants?: {
//       initial: Variants["initial"]
//       animate: Variants["animate"]
//     }
//   }

// // Synchronized Typewriter Component
// export const SynchronizedTypewriter: React.FC<SynchronizedTypewriterProps> = ({
//     id,
//     text,
//     speed = 50,
//     initialDelay = 0,
//     waitTime = 2000,
//     deleteSpeed = 30,
//     className,
//     showCursor = true,
//     hideCursorOnType = false,
//     cursorChar = "|",
//     cursorClassName = "ml-1",
//     cursorAnimationVariants = {
//       initial: { opacity: 0 },
//       animate: {
//         opacity: 1,
//         transition: {
//           duration: 0.01,
//           repeat: Infinity,
//           repeatDelay: 0.4,
//           repeatType: "reverse",
//         },
//       },
//     },
//   }) => {
//     const [displayText, setDisplayText] = useState("")
//     const [currentIndex, setCurrentIndex] = useState(0)
//     const [isDeleting, setIsDeleting] = useState(false)
//     const [currentTextIndex, setCurrentTextIndex] = useState(0)
//     const [isWaiting, setIsWaiting] = useState(false)
    
//     const { registerTypewriter, unregisterTypewriter, notifyAnimationComplete, waitForSync } = useTypewriterSync()
//     const hasNotifiedComplete = useRef(false)
  
//     // Register/unregister typewriter
//     useEffect(() => {
//       registerTypewriter(id)
//       return () => unregisterTypewriter(id)
//     }, [id, registerTypewriter, unregisterTypewriter])
  
//     useEffect(() => {
//       let timeout: NodeJS.Timeout
  
//       const currentText = text[currentTextIndex]
  
//       const startTyping = async () => {
//         if (isDeleting) {
//           if (displayText === "") {
//             // Finished deleting, notify completion and wait for sync
//             if (!hasNotifiedComplete.current) {
//               notifyAnimationComplete(id)
//               hasNotifiedComplete.current = true
//               setIsWaiting(true)
//               await waitForSync()
//               setIsWaiting(false)
//             }
            
//             setIsDeleting(false)
//             setCurrentTextIndex((prev) => (prev + 1) % text.length)
//             setCurrentIndex(0)
//             hasNotifiedComplete.current = false
//             timeout = setTimeout(() => {}, waitTime)
//           } else {
//             timeout = setTimeout(() => {
//               setDisplayText((prev) => prev.slice(0, -1))
//             }, deleteSpeed)
//           }
//         } else {
//           if (currentIndex < currentText.length) {
//             timeout = setTimeout(() => {
//               setDisplayText((prev) => prev + currentText[currentIndex])
//               setCurrentIndex((prev) => prev + 1)
//             }, speed)
//           } else {
//             // Finished typing, notify completion and wait for sync
//             if (!hasNotifiedComplete.current) {
//               notifyAnimationComplete(id)
//               hasNotifiedComplete.current = true
//               setIsWaiting(true)
//               await waitForSync()
//               setIsWaiting(false)
//             }
            
//             hasNotifiedComplete.current = false
//             timeout = setTimeout(() => {
//               setIsDeleting(true)
//             }, waitTime)
//           }
//         }
//       }
  
//       if (isWaiting) return
  
//       // Apply initial delay only at the start
//       if (currentIndex === 0 && !isDeleting && displayText === "" && currentTextIndex === 0) {
//         timeout = setTimeout(startTyping, initialDelay)
//       } else {
//         startTyping()
//       }
  
//       return () => clearTimeout(timeout)
//     }, [
//       currentIndex,
//       displayText,
//       isDeleting,
//       speed,
//       deleteSpeed,
//       waitTime,
//       text,
//       currentTextIndex,
//       id,
//       notifyAnimationComplete,
//       waitForSync,
//       isWaiting
//     ])
  
//     const cn = (...classes: (string | undefined)[]) => classes.filter(Boolean).join(' ')
  
//     return (
//       <div className={`inline whitespace-pre-wrap tracking-tight ${className}`}>
//         <span>{displayText}</span>
//         {isWaiting && <span className="text-gray-500 ml-2">⏳</span>}
//         {showCursor && (
//           <motion.span
//             variants={cursorAnimationVariants}
//             className={cn(
//               cursorClassName,
//               hideCursorOnType &&
//                 (currentIndex < text[currentTextIndex].length || isDeleting)
//                 ? "hidden"
//                 : ""
//             )}
//             initial="initial"
//             animate="animate"
//           >
//             {cursorChar}
//           </motion.span>
//         )}
//       </div>
//     )
//   }