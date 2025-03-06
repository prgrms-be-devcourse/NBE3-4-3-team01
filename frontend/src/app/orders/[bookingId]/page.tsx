import BookingDetails from "@/components/booking/BookingDetails";

const BookingDetailsPage = async ({
  params,
}: {
  params: { bookingId: string };
}) => {
  const id = Number((await params).bookingId);
  return (
    <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white">
      <BookingDetails bookingId={id} />
    </div>
  );
};

export default BookingDetailsPage;
