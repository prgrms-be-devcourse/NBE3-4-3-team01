"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import styles from "./Navigation.module.css";
import { getRoleFromCookie, RoleData } from "@/lib/utils/CookieUtil";
import { logout } from "@/lib/api/auth/AuthApi";

interface UserState {
  isLoggedIn: boolean;
  userType: "USER" | "BUSINESS" | "ADMIN" | "ANONYMOUS" | null;
  hasHotel?: boolean;
  hotelId?: number;
}

export default function Navigation() {
  const router = useRouter();

  const [user, setUser] = useState<UserState>({
    isLoggedIn: false,
    userType: null,
    hasHotel: false,
    hotelId: -1,
  });

  useEffect(() => {
    const checkLoginStatus = () => {
      const roleData: RoleData | null = getRoleFromCookie();

      console.log(roleData);

      if (!roleData) {
        setUser({
          isLoggedIn: false,
          userType: "ANONYMOUS",
        });
      } else if (roleData.role === "USER") {
        setUser({
          isLoggedIn: true,
          userType: "USER",
        });
      } else if (roleData.role === "BUSINESS") {
        setUser({
          isLoggedIn: true,
          userType: "BUSINESS",
          hasHotel: roleData.hasHotel,
          hotelId: roleData.hotelId,
        });
      } else if (roleData.role === "ADMIN") {
        setUser({
          isLoggedIn: true,
          userType: "ADMIN",
        });
      }
    };

    checkLoginStatus();
  }, []);

  const handleLogout = async () => {
    try {
      await logout();
      setUser({
        isLoggedIn: false,
        userType: "ANONYMOUS",
      });
      router.push("/");
    } catch (error) {
      console.error("로그아웃 실패:", error);
    }
  };

  return (
    <nav className={styles.navigation}>
      <div className={styles.container}>
        <Link
          href={user?.userType === "ADMIN" ? "/admin" : "/"}
          className={styles.logo}
        >
          서울호텔
        </Link>

        <div className={styles.links}>
          {/* ANONYMOUS 상태 */}
          {!user.isLoggedIn ? (
            <>
              <Link href="/login" className={styles.link}>
                로그인
              </Link>
            </>
          ) : (
            <>
              {/* USER 상태 */}
              {user.userType === "USER" && (
                <>
                  <Link href="/business/register" className={styles.link}>
                    사업자 등록
                  </Link>
                  <Link href="/me/orders" className={styles.link}>
                    내 예약
                  </Link>
                  <Link href="/me/reviews" className={styles.link}>
                    내 리뷰
                  </Link>
                  <Link href="/me/favorites" className={styles.link}>
                    즐겨찾기
                  </Link>
                </>
              )}

              {/* BUSINESS 상태 */}
              {user.userType === "BUSINESS" &&
                (user.hasHotel ? (
                  <>
                    <Link
                      href={`/business/hotel/management`}
                      className={styles.link}
                    >
                      내 호텔 관리
                    </Link>
                    <Link
                      href={`/business/hotel/${user.hotelId}/reviews`}
                      className={styles.link}
                    >
                      호텔 리뷰
                    </Link>
                    <Link href={`/business/bookings`} className={styles.link}>
                      예약 관리
                    </Link>
                    <Link
                      href="/business/hotel/revenue"
                      className={styles.link}
                    >
                      호텔 매출
                    </Link>
                  </>
                ) : (
                  <></>
                ))}

              {/* ADMIN 상태 */}
              {user.userType === "ADMIN" && (
                <>
                  <Link href="/admin/business" className={styles.link}>
                    사업자 관리
                  </Link>
                  <Link href="/admin/hotels" className={styles.link}>
                    호텔 관리
                  </Link>
                  <Link href="/admin/hotel-options" className={styles.link}>
                    호텔 옵션 관리
                  </Link>
                  <Link href="/admin/room-options" className={styles.link}>
                    객실 옵션 관리
                  </Link>
                </>
              )}

              {/* 로그아웃 버튼 */}
              <button onClick={handleLogout} className={styles.logoutButton}>
                로그아웃
              </button>
            </>
          )}
        </div>
      </div>
    </nav>
  );
}
