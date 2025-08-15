import Pagination from "./pagination";

interface AssessmentPaginationProps {
    currentPage: number;
    totalPages: number;
    onPageChange: (page: number) => void;
    isLoading?: boolean;
}

export default function AssessmentPagination({ 
    currentPage, 
    totalPages, 
    onPageChange, 
    isLoading = false 
}: AssessmentPaginationProps) {
    return (
        <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={onPageChange}
            isLoading={isLoading}
        />
    );
}