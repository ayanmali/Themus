// import { createContext, useCallback, useContext, useEffect, useRef, useState } from "react"

// // Synchronization Context
// interface TypewriterSyncContextType {
//     registerTypewriter: (id: string) => void
//     unregisterTypewriter: (id: string) => void
//     notifyAnimationComplete: (id: string) => void
//     waitForSync: () => Promise<void>
//     getCurrentPhase: () => 'typing' | 'waiting' | 'deleting'
// }

// const TypewriterSyncContext = createContext<TypewriterSyncContextType | null>(null)

// // Synchronization Provider
// interface TypewriterSyncProviderProps {
//     children: React.ReactNode
//     totalTypewriters: number
// }

// export const TypewriterSyncProvider: React.FC<TypewriterSyncProviderProps> = ({
//     children,
//     totalTypewriters
// }) => {
//     const [registeredTypewriters, setRegisteredTypewriters] = useState<Set<string>>(new Set());
//     const [completedTypewriters, setCompletedTypewriters] = useState<Set<string>>(new Set());
//     const [currentPhase, setCurrentPhase] = useState<'typing' | 'waiting' | 'deleting'>('typing');
//     const waitingPromises = useRef<Map<string, { resolve: () => void }>>(new Map());

//     const registerTypewriter = useCallback((id: string) => {
//         setRegisteredTypewriters(prev => {
//             const newSet = new Set(prev)
//             newSet.add(id)
//             return newSet
//         })
//     }, [])

//     const unregisterTypewriter = useCallback((id: string) => {
//         setRegisteredTypewriters(prev => {
//             const newSet = new Set(prev)
//             newSet.delete(id)
//             return newSet
//         })
//         setCompletedTypewriters(prev => {
//             const newSet = new Set(prev)
//             newSet.delete(id)
//             return newSet
//         })
//     }, [])

//     const notifyAnimationComplete = useCallback((id: string) => {
//         setCompletedTypewriters(prev => {
//             const newSet = new Set(prev)
//             newSet.add(id)
//             return newSet
//         })
//     }, [])

//     const waitForSync = useCallback((): Promise<void> => {
//         return new Promise((resolve) => {
//             const id = Math.random().toString(36).substring(2, 9)
//             waitingPromises.current.set(id, { resolve })
//         })
//     }, [])

//     const getCurrentPhase = useCallback(() => currentPhase, [currentPhase])

//     // Check if all typewriters have completed and release waiting ones
//     useEffect(() => {
//         if (registeredTypewriters.size === totalTypewriters &&
//             completedTypewriters.size === totalTypewriters &&
//             completedTypewriters.size > 0) {

//             // All typewriters completed, release them
//             waitingPromises.current.forEach(({ resolve }) => resolve())
//             waitingPromises.current.clear()

//             // Reset completed set for next cycle
//             setCompletedTypewriters(new Set())

//             // Update phase
//             setCurrentPhase(prev => prev === 'typing' ? 'deleting' : 'typing')
//         }
//     }, [registeredTypewriters.size, completedTypewriters.size, totalTypewriters])

//     return (
//         <TypewriterSyncContext.Provider value={{
//             registerTypewriter,
//             unregisterTypewriter,
//             notifyAnimationComplete,
//             waitForSync,
//             getCurrentPhase
//         }}>
//             {children}
//         </TypewriterSyncContext.Provider>
//     )
// }

// // Hook to use synchronization
// export const useTypewriterSync = () => {
//     const context = useContext(TypewriterSyncContext)
//     if (!context) {
//         throw new Error('useTypewriterSync must be used within TypewriterSyncProvider')
//     }
//     return context
// }