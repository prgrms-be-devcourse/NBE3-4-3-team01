import Link from "next/link";
import styles from "./Pagination.module.css";

type PaginationProps = {
  currentPage: number;
  totalPages: number;
  basePath: string; // "/hotels" 같은 경로
};

const Pagination = ({ currentPage, totalPages, basePath }: PaginationProps) => {
  // 5개의 페이지 번호 범위 계산
  const getPageRange = () => {
    const range = [];
    let start = Math.max(1, currentPage - 2); // 2개 이전 페이지부터 시작
    let end = Math.min(totalPages, currentPage + 2); // 2개 이후 페이지까지 표시

    // 페이지 범위가 총 5개로 고정되도록 보장
    if (end - start < 4) {
      if (start === 1) {
        end = Math.min(totalPages, start + 4);
      } else {
        start = Math.max(1, end - 4);
      }
    }

    for (let i = start; i <= end; i++) {
      range.push(i);
    }
    return range;
  };

  const createLink = (page: number) => {
    let path = basePath.startsWith("/") ? basePath : `/${basePath}`;

    return `${path}${path.includes("?") ? "&" : "?"}page=${page}`;
  };

  return (
    <div className={styles.paginationContainer}>
      {/* 이전 페이지 */}
      <Link
        href={createLink(currentPage - 1)}
        className={`${styles.pageLink} ${
          currentPage === 1 ? styles.disabled : ""
        }`}
      >
        이전
      </Link>

      {/* 페이지 번호 */}
      {getPageRange().map((page) => (
        <Link
          key={page}
          href={createLink(page)}
          className={`${styles.pageLink} ${
            page === currentPage ? styles.active : ""
          }`}
        >
          {page}
        </Link>
      ))}

      {/* 다음 페이지 */}
      <Link
        href={createLink(currentPage + 1)}
        className={`${styles.pageLink} ${
          currentPage === totalPages ? styles.disabled : ""
        }`}
      >
        다음
      </Link>
    </div>
  );
};

export default Pagination;
