"use client";

import { Swiper, SwiperSlide } from "swiper/react";
import { Navigation } from "swiper/modules";
import "swiper/css";
import "swiper/css/navigation";
import { useState } from "react";
import { FaImage } from "react-icons/fa"; // 아이콘 추가

interface HotelImagesProps {
  images: string[];
}

const HotelImages: React.FC<HotelImagesProps> = ({ images }) => {
  const [selectedImage, setSelectedImage] = useState<string | null>(null);

  return (
    <div className="container mx-auto px-6">
      {/* 호텔 이미지 제목 */}
      <div className="text-center mb-8">
        <h2 className="text-4xl font-bold text-sky-500">
          <span className="border-b-4 border-yellow-300 inline-block px-4">
            호텔 이미지 갤러리
          </span>
        </h2>
        <p className="text-lg text-gray-600 mt-2">
          호텔의 매력을 미리 경험해보세요!
        </p>
      </div>

      {/* 이미지 슬라이더 */}
      {images.length > 0 ? (
        <div className="relative">
          <Swiper
            modules={[Navigation]}
            spaceBetween={15}
            slidesPerView={3}
            navigation
            loop={true}
            className="rounded-xl shadow-lg border border-gray-200"
          >
            {images.map((src, index) => (
              <SwiperSlide key={index}>
                <img
                  src={src}
                  alt={`호텔 이미지 ${index + 1}`}
                  className="w-full h-64 object-cover rounded-lg cursor-pointer transition-transform duration-300 hover:scale-105"
                  onClick={() => setSelectedImage(src)}
                />
              </SwiperSlide>
            ))}
          </Swiper>
        </div>
      ) : (
        <div className="flex flex-col items-center justify-center bg-gray-100 p-8 rounded-xl shadow-md">
          <FaImage className="text-gray-400 text-6xl mb-4" />
          <p className="text-gray-500 text-xl font-semibold">
            등록된 호텔 이미지가 없습니다.
          </p>
        </div>
      )}

      {/* 모달 - 이미지 확대 */}
      {selectedImage && (
        <div
          className="fixed inset-0 bg-black bg-opacity-80 flex items-center justify-center z-50"
          onClick={() => setSelectedImage(null)}
        >
          <div className="relative p-4 bg-white rounded-2xl shadow-2xl max-w-3xl">
            <button
              className="absolute top-2 right-2 text-gray-600 hover:text-gray-900 text-3xl font-bold"
              onClick={() => setSelectedImage(null)}
            >
              ✖
            </button>
            <img
              src={selectedImage}
              alt="확대한 이미지"
              className="w-full max-h-[80vh] object-contain rounded-lg"
            />
          </div>
        </div>
      )}
    </div>
  );
};

export default HotelImages;
