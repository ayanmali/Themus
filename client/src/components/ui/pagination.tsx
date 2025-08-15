import { ChevronLeft, ChevronRight } from "lucide-react";

interface PaginationProps {
    currentPage: number;
    totalPages: number;
    onPageChange: (page: number) => void;
    isLoading?: boolean;
}

export default function Pagination({ 
    currentPage, 
    totalPages, 
    onPageChange, 
    isLoading = false 
}: PaginationProps) {
    // Don't render pagination if there's only one page or no pages
    if (totalPages <= 1) {
        return null;
    }

    // Ensure currentPage is within bounds
    const safeCurrentPage = Math.max(0, Math.min(currentPage, totalPages - 1));

    const getVisiblePages = () => {
        const delta = 2; // Number of pages to show on each side of current page
        const range = [];
        const rangeWithDots = [];

        for (let i = Math.max(2, safeCurrentPage - delta); i <= Math.min(totalPages - 1, safeCurrentPage + delta); i++) {
            range.push(i);
        }

        if (safeCurrentPage - delta > 2) {
            rangeWithDots.push(1, '...');
        } else {
            rangeWithDots.push(1);
        }

        rangeWithDots.push(...range);

        if (safeCurrentPage + delta < totalPages - 1) {
            rangeWithDots.push('...', totalPages);
        } else {
            rangeWithDots.push(totalPages);
        }

        return rangeWithDots;
    };

    const handlePrevious = () => {
        if (safeCurrentPage > 0 && !isLoading) {
            onPageChange(safeCurrentPage - 1);
        }
    };

    const handleNext = () => {
        if (safeCurrentPage < totalPages - 1 && !isLoading) {
            onPageChange(safeCurrentPage + 1);
        }
    };

    const handlePageClick = (page: number) => {
        if (page !== safeCurrentPage + 1 && !isLoading) {
            onPageChange(page - 1); // Convert to 0-based index
        }
    };

    return (
        <div className="mt-14 flex items-center justify-center">
            <nav className="flex items-center space-x-2" aria-label="Pagination">
                {/* Previous Button */}
                <button
                    onClick={handlePrevious}
                    disabled={safeCurrentPage === 0 || isLoading}
                    className="flex items-center gap-2 px-3 py-2 rounded-lg bg-transparent text-gray-200 hover:bg-gray-600 hover:text-white transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                    <ChevronLeft className="h-4 w-4" />
                    Previous
                </button>

                {/* Page Numbers */}
                {getVisiblePages().map((page, index) => (
                    <div key={index} className="bg-transparent text-gray-200 rounded-lg hover:bg-gray-600 hover:text-white transition-colors">
                        {page === '...' ? (
                            <span className="px-3 py-2">...</span>
                        ) : (
                            <button
                                onClick={() => handlePageClick(page as number)}
                                disabled={isLoading}
                                className={`px-3 py-2 rounded-lg transition-colors ${
                                    page === safeCurrentPage + 1
                                        ? 'bg-blue-600 text-white'
                                        : 'hover:bg-gray-600 hover:text-white'
                                } disabled:opacity-50 disabled:cursor-not-allowed`}
                            >
                                {page}
                            </button>
                        )}
                    </div>
                ))}

                {/* Next Button */}
                <button
                    onClick={handleNext}
                    disabled={safeCurrentPage === totalPages - 1 || isLoading}
                    className="flex items-center gap-2 px-3 py-2 rounded-lg bg-transparent text-gray-200 hover:bg-gray-600 hover:text-white transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                    Next
                    <ChevronRight className="h-4 w-4" />
                </button>
            </nav>
        </div>
    );
}
