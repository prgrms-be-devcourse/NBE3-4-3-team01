export interface PutHotelRequest {
  hotelName: string;
  hotelEmail: string;
  hotelPhoneNumber: string;
  streetAddress: string;
  zipCode: number;
  hotelGrade: number;
  checkInTime: string;
  checkOutTime: string;
  hotelExplainContent: string;
  hotelStatus: string;
  deleteImageUrls: string[];
  imageExtensions: string[];
  hotelOptions: string[];
}
