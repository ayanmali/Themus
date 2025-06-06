import { Pagination, PaginationNext, PaginationLink, PaginationPrevious, PaginationItem, PaginationEllipsis } from "./pagination";

import { PaginationContent } from "./pagination";

export default function AssessmentPagination() {
    return (
        <Pagination className="mt-14 ">
            <PaginationContent>
                <PaginationItem className="bg-transparent text-gray-200 rounded-lg hover:bg-gray-600 hover:text-white transition-colors">
                    <PaginationPrevious href="#" className="hover:bg-gray-600 hover:text-white transition-colors" />
                </PaginationItem>

                <PaginationItem className="bg-transparent text-gray-200 rounded-lg hover:bg-gray-600 hover:text-white transition-colors">
                    <PaginationLink href="#" className="hover:bg-gray-600 hover:text-white transition-colors">1</PaginationLink>
                </PaginationItem>

                <PaginationItem className="bg-transparent text-gray-200 rounded-lg hover:bg-gray-600 hover:text-white transition-colors">
                    <PaginationLink href="#" className="hover:bg-gray-600 hover:text-white transition-colors">
                        2
                    </PaginationLink>
                </PaginationItem>

                <PaginationItem className="bg-transparent text-gray-200 rounded-lg hover:bg-gray-600 hover:text-white transition-colors">
                    <PaginationLink href="#" className="hover:bg-gray-600 hover:text-white transition-colors">3</PaginationLink>
                </PaginationItem>

                <PaginationItem className="bg-transparent text-gray-200 rounded-lg hover:bg-gray-600 hover:text-white transition-colors">
                    <PaginationEllipsis />
                </PaginationItem>

                <PaginationItem className="bg-transparent text-gray-200 rounded-lg hover:bg-gray-600 hover:text-white transition-colors">
                    <PaginationNext href="#" className="hover:bg-gray-600 hover:text-white transition-colors"/>
                </PaginationItem>

            </PaginationContent>
        </Pagination>
    )
}